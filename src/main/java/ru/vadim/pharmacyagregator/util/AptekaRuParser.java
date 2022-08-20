package ru.vadim.pharmacyagregator.util;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.Data;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import ru.vadim.pharmacyagregator.domain.Pharm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Service
public class AptekaRuParser {

    private static final String PAGE = "?page=";

    public void parse(String link) throws IOException {
        Document doc = seleniumParse(link);
        Elements el = doc.select("div.SidebarCategoriesList > ul");
        Map<String, String> test = null;
        for (Element element : el.get(0).children()) {
            test = parseEveryCatalog1("https://apteka.ru" + element.child(0).attr("href"), "div.ViewRootCategory__subcat");
        }
        System.out.println(test.values());
        
        
        Map<String, String> catalog = parseEveryCatalog1(link,"div.SidebarCategoriesList > ul");
        Map<String, String> innerCatalog;
        for (Map.Entry<String, String> catalogElement : catalog.entrySet()) {
            innerCatalog = parseEveryCatalog1(catalogElement.getValue(), "div.ViewRootCategory__subcat");
        }

    }

    public Map<String, String> parseEveryCatalog1(String link, String query) throws IOException {
        Document doc = getDoc(link);
        Elements innerCatalog = doc.select(query).get(0).children();
        Map<String, String> nameAndPath = new HashMap<>();
        Map<String, List<Pair<String, String>>> categoryAndProductPath = new HashMap<>();
        for (Element groups : innerCatalog) {
            String name = groups.text();
            String categoryLink = "https://apteka.ru" + groups.attr("href");
            nameAndPath.put(name, categoryLink);
            categoryAndProductPath.put(name, getProducts(categoryLink));
        }
        return nameAndPath;
    }

    public List<Pair<String, String>> getProducts(String link) throws IOException {
        int i = 1;
        Elements productElements;
        List<Pair<String, String>> namesAndLinks = new ArrayList<>();;
        do {
            Document doc = getDoc(link + PAGE + i);
            productElements = doc.getElementsByClass("catalog-card card-flex");
            i++;
            if (!productElements.isEmpty()) {
                for (Element productInList : productElements) {
                    namesAndLinks.add(getNameAndHref(productInList.child(0), "catalog-card__name emphasis"));
                }
            }
        }
        while (!productElements.isEmpty());
        System.out.println(namesAndLinks.size());
        return namesAndLinks;
    }

    private Pair<String, String> getNameAndHref(Element element, String clas) {
        String name;
        if (clas != null) {
            name = element.getElementsByClass(clas).text();
        } else name = element.text();
        String link = "https://apteka.ru" + element.attr("href");
        return Pair.of(name, link);
    }

    public Document seleniumParse(String link) {
        WebDriverManager.chromedriver().arch64().setup();
        ChromeOptions chromeOptions = new ChromeOptions();
        WebDriver webDriver = new ChromeDriver(chromeOptions);
        webDriver.get(link);
        webDriver.findElement(By.className("overlay-close")).click();
        webDriver.findElement(By.className("ButtonIcon-icon")).click();
        webDriver.findElement(By.className("SidebarCatalog__tabs")).click();
        Document document = Jsoup.parse(webDriver.getPageSource());
        webDriver.close();
        return document;
    }

    private Document getDoc(String link) throws IOException {
        return Jsoup.connect(link)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.134 Safari/537.36 OPR/89.0.4447.71")
                .get();
    }
}

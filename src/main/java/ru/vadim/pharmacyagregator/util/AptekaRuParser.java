package ru.vadim.pharmacyagregator.util;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.Data;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import ru.vadim.pharmacyagregator.domain.Pharm;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            System.out.println(name + " " + categoryLink);
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
        System.out.println(name + " " + link);
        return Pair.of(name, link);
    }

    public Pharm getProduct(String link) throws IOException, InterruptedException {
        Pharm pharm = new Pharm();
        Document document = getDoc(link);
        Elements elements = document.getElementsByClass("ViewProductPage").get(0).children();
        pharm.setTitle(document.select("h1[itemprop=name]").text());
        pharm.setActiveSubstance(document.getElementsByClass("ux-commas").get(0).children().get(0).text());
        pharm.setProducerPharm(document.getElementsByClass("ProdDescList").get(0).children().get(1).children().get(3).text());
        getPrice(link);
        String a = document.select("span.moneyprice__content").text();
        String b = document.getElementsByClass("div.ProductOffer__price").text();

        return new Pharm();
    }

    public Document seleniumParse(String link) {
        WebDriverManager.chromedriver().arch64().setup();
        ChromeOptions chromeOptions = new ChromeOptions();
        WebDriver webDriver = new ChromeDriver(chromeOptions);
        webDriver.get(link);
        webDriver.findElement(By.xpath("//*[@id=\"search-city\"]")).sendKeys("Учалы");
        webDriver.findElement(By.className("TownSelector__options")).getCssValue("Учалы");
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

    private void getPrice(String link) throws InterruptedException {
        WebDriverManager.chromedriver().arch64().setup();
        ChromeOptions chromeOptions = new ChromeOptions();
        WebDriver webDriver = new ChromeDriver(chromeOptions);
        webDriver.get(link);
        webDriver.findElement(By.xpath("//*[@id=\"search-city\"]")).sendKeys("Учалы");
        WebDriverWait dr = new WebDriverWait(webDriver, Duration.ofSeconds(3));
        dr.until(ExpectedConditions.visibilityOfElementLocated(By.className("TownSelector__options")));
        Document d = Jsoup.parse(webDriver.getPageSource());
        webDriver.findElement(By.xpath(String.format("//*[@id=\"app\"]/div[5]/div/div[3]/div/div[1]/div[2]/div/div/div/div/ol/li[%s]", getCityXPathIndex(d, "Учалы")))).click();
        webDriver.findElement(By.className("ButtonIcon-icon")).click();
        webDriver.findElement(By.className("ProductOffer__price"));
        Document document = Jsoup.parse(webDriver.getPageSource());
        webDriver.close();

    }

    private int getCityXPathIndex(Document document, String chosenCity) {
        Elements cities = document.getElementsByClass("TownSelector__options").get(0).children();
        int cityIndex  = 0;
        for (Element elementCity : cities) {
            String city  = elementCity.getElementsByTag("strong").text();
            final String regex = "[^,]*";
            final Pattern pattern = Pattern.compile(regex);
            final Matcher matcher = pattern.matcher(city);
            if (matcher.find()) {
                String foundCity = matcher.group(0).trim();
                cityIndex++;
                if (chosenCity.equalsIgnoreCase(foundCity)) {
                    return cityIndex;
                }
            }
        }
        return cityIndex;
    }
}

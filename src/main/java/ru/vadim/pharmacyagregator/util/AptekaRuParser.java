package ru.vadim.pharmacyagregator.util;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.json.JSONParser;
import org.json.JSONObject;
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
import ru.vadim.pharmacyagregator.domain.PharmacyType;
import ru.vadim.pharmacyagregator.repository.exception.NotFoundException;
import ru.vadim.pharmacyagregator.service.PharmacyTypeService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bouncycastle.util.io.Streams.readAll;

@Slf4j
@Data
@Service
public class AptekaRuParser {

    private static final String PAGE = "?page=";

    private final PharmacyTypeService pharmacyTypeService;

    public void parse(String link) throws IOException, InterruptedException, NotFoundException {
        Document doc = seleniumParse(link);
        Elements el = doc.select("div.SidebarCategoriesList > ul");
       List<Pharm> parsedPharm = null;
        for (Element element : el.get(0).children()) {
            parsedPharm = parseEveryCatalog1("https://apteka.ru" + element.child(0).attr("href"), "div.ViewRootCategory__subcat");
        }
        System.out.println(parsedPharm);
    }

    public List<Pharm> parseEveryCatalog1(String link, String query) throws IOException, NotFoundException {
        Document doc = getDoc(link);
        PharmacyType currentType = pharmacyTypeService.findById(getTypeNumber(link));
        Elements innerCatalog = doc.select(query).get(0).children();
        Map<String, String> nameAndPath = new HashMap<>();
        List<Pharm> parsedPharms = new ArrayList<>();
        Map<String, List<Pair<String, String>>> categoryAndProductPath = new HashMap<>();
        for (Element groups : innerCatalog) {
            String name = groups.text();
            String categoryLink = "https://apteka.ru" + groups.attr("href");
            nameAndPath.put(name, categoryLink);
            System.out.println(name + " " + categoryLink);
            categoryAndProductPath.put(name, getProducts(categoryLink));
        }
        for (Map.Entry<String, List<Pair<String, String>>> element : categoryAndProductPath.entrySet()) {
            for (Pair<String, String> product : element.getValue()) {
                Pharm pharm = getProduct(product.getSecond());
                pharm.setNumber(currentType);
                parsedPharms.add(pharm);
            }
        }
        return parsedPharms;
    }

    private long getTypeNumber(String link) {
        String category = StringUtils.substring(link, 22, link.length() - 1);
        return switch (category) {
            //лекарства
            case "leka" -> 1;
            //бады
            case "biol" -> 2;
            //медицинские изделия и приборы
            case "medi" -> 4;
            //медицинские изделия и приборы
            case "mib" -> 4;
            // мама и малыш
            case "dets" -> 7;
            // диетическое и диабетическое питание
            case "diet" -> 6;
            // гигиена
            case "gigi" -> 5;
            // дезинфекция
            case "dezi" -> 5;
            // товары для праздников
            case "mil" -> 4;
            default -> 0;
        };
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

    public Pharm getProduct(String link) throws IOException {
        String id = link.substring(link.lastIndexOf('-') + 1, link.lastIndexOf('/'));
        String l = "https://api.apteka.ru/Item/Info?id=";
        String json = IOUtils.toString(URI.create(l + id), Charset.forName("UTF-8"));
        JSONObject jsonOb = new JSONObject(json);
        Pharm pharm = new Pharm();
        Document document = getDoc(link);
        pharm.setId(jsonOb.getString("id"));
        pharm.setTitle(jsonOb.getString("name"));
        pharm.setActiveSubstance(document.getElementsByClass("ux-commas").get(0).children().get(0).text());
        pharm.setProducerPharm(jsonOb.getString("vendor"));
        pharm.setPrice(getPrice(link));
        pharm.setLink(link);
        return pharm;
    }

    public Document seleniumParse(String link) throws InterruptedException, IOException {
        String city = "Учалы";
        WebDriverManager.chromedriver().arch64().setup();
        ChromeOptions chromeOptions = new ChromeOptions();
        WebDriver webDriver = new ChromeDriver(chromeOptions);
        webDriver.get(link);
        webDriver.findElement(By.xpath("//*[@id=\"search-city\"]")).sendKeys(city);
        (new WebDriverWait(webDriver, Duration.ofSeconds(10)))
                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(String.format("//strong[starts-with(text(), '%s')]", city))));
        webDriver.findElement(By.className("TownSelector__options")).getCssValue("Учалы");
        webDriver.findElement(By.className("overlay-close")).click();
        webDriver.findElement(By.className("ButtonIcon-icon")).click();
        webDriver.findElement(By.className("SidebarCatalog__tabs")).click();
        Document document = Jsoup.parse(webDriver.getPageSource());
        webDriver.close();
        return document;
    }

    private Document getDoc(String link) {
        try {
            return Jsoup.connect(link)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.134 Safari/537.36 OPR/89.0.4447.71")
                    .get();
        } catch (IOException e) {
            log.debug(e.getMessage());
            throw new RuntimeException("Something wrong with parsing: " + e.getMessage());
        }
    }

    public double getPrice(String link) {
        String city = "Учалы";
        WebDriverManager.chromedriver().arch64().setup();
        ChromeOptions chromeOptions = new ChromeOptions();
        WebDriver webDriver = new ChromeDriver(chromeOptions);
        webDriver.get(link);
        webDriver.findElement(By.xpath("//*[@id=\"search-city\"]")).sendKeys(city);
        WebElement afterClick = (new WebDriverWait(webDriver, Duration.ofSeconds(10)))
                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(String.format("//strong[starts-with(text(), '%s')]", city))));
        afterClick.click();
        WebElement x = (new WebDriverWait(webDriver, Duration.ofSeconds(10)))
                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class= 'ViewProductPage__title']")));
        WebElement x2 = (new WebDriverWait(webDriver, Duration.ofSeconds(5)))
                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"app\"]/div[5]")));
        System.out.println(x2.getText());
        Document document = Jsoup.parse(webDriver.getPageSource());
        webDriver.close();
        Elements productOffer__price = document.getElementsByClass("ProductOffer__price");
        if (!productOffer__price.isEmpty()) {
            for (Element e : productOffer__price.get(0).children()) {
                String price = StringUtils.chop(e.text().replaceAll(" ", ""));
                return Double.parseDouble(price);
            }
        }
        log.debug("Price doesn't exist");
        return 0.0;
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

package ru.vadim.pharmacyagregator.util;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
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

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        //System.out.println(parsedPharm);
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
            //System.out.println(name + " " + categoryLink);
            categoryAndProductPath.put(name, getProducts(categoryLink));
        }
        for (Map.Entry<String, List<Pair<String, String>>> element : categoryAndProductPath.entrySet()) {
            for (Pair<String, String> product : element.getValue()) {
                for (Pharm ph : getProduct(product.getSecond())) {
                    ph.setNumber(currentType);
                    parsedPharms.add(ph);
                }
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

    public List<Pharm> getProduct(String link) throws IOException {
        String id;
        String city = "uchaly";
        if (link.contains("-")) {
            id = link.substring(link.lastIndexOf('-') + 1, link.lastIndexOf('/'));
        } else {
            String[] splittedPath = link.split("/");
            id = splittedPath[splittedPath.length - 1];
        }
        String linkToProduct = "https://api.apteka.ru/Item/Info?id=";
        String cityRequest = String.format("&cityUrl=%s", city);
        String jsonFileProduct = IOUtils.toString(URI.create(linkToProduct + id + cityRequest), Charset.forName("UTF-8"));
        List<Pharm> pharms = new ArrayList<>();
        if (!jsonFileProduct.isEmpty()) {
            pharms.add(setSinglePharm(jsonFileProduct, link));
        } else {
//            linkToProduct = "https://api.apteka.ru/Item/GroupInfo?itemGroupId=";
//            jsonFileProduct = IOUtils.toString(URI.create(linkToProduct + id + cityRequest), Charset.forName("UTF-8"));
//            pharms = setGroupPharm(jsonFileProduct, link);
        }
        return pharms;
    }

    private Pharm setSinglePharm(String json, String link) {
        JSONObject jsonOb = new JSONObject(json);
        Pharm pharm = new Pharm();
        Document document = getDoc(link);
        pharm.setId(jsonOb.getString("id"));
        pharm.setTitle(jsonOb.getString("name"));
        pharm.setActiveSubstance(jsonOb.getString("struct"));
        pharm.setProducerPharm(jsonOb.getString("vendor"));
        pharm.setPrice(getPrice(link));
        pharm.setLink(link);
        pharm.setExplanation(jsonOb.getString("pharmDyn"));
        pharm.setDelivery(isDelivered(document));
        return pharm;
    }

    // Настроить получение данных из лекрств групп
    private List<Pharm> setGroupPharm(String json, String link) {
        JSONObject jsonOb = new JSONObject(json);
        List<Pharm> pharmFromGroup = new ArrayList<>();
        Pharm pharm = new Pharm();
        Document document = getDoc(link);
        JSONArray jsonArray = jsonOb.getJSONArray("itemInfos");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject nestedProduct = jsonArray.getJSONObject(i);
            pharm.setId(nestedProduct.getString("id"));
            pharm.setProducerPharm(nestedProduct.getString("vendor"));
            pharm.setTitle(nestedProduct.getString("name"));
            pharm.setActiveSubstance(document.getElementsByClass("ux-commas").get(0).children().get(0).text());
            pharm.setPrice(getPrice(link));
            pharm.setExplanation(nestedProduct.getString("indic"));
            pharm.setDelivery(isDelivered(document));
            pharm.setLink(link);
            pharmFromGroup.add(pharm);
        }
        return pharmFromGroup;
    }

    public Document seleniumParse(String link) throws InterruptedException, IOException {
        String city = "Учалы";
        WebDriverManager.chromedriver().arch64().setup();
        ChromeOptions chromeOptions = new ChromeOptions();
        WebDriver webDriver = new ChromeDriver(chromeOptions);
        webDriver.get(link);
        (new WebDriverWait(webDriver, Duration.ofSeconds(5)))
                .until(ExpectedConditions.visibilityOfElementLocated(By.className("TownSelector__input")));
        webDriver.findElement(By.xpath("//*[@id=\"search-city\"]")).sendKeys(city);
        (new WebDriverWait(webDriver, Duration.ofSeconds(5)))
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
        System.out.println(link);
        try {
            return Jsoup.connect(link)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.134 Safari/537.36 OPR/89.0.4447.71")
                    .timeout(100000)
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
        (new WebDriverWait(webDriver, Duration.ofSeconds(5)))
                .until(ExpectedConditions.visibilityOfElementLocated(By.className("TownSelector__input")));
        webDriver.findElement(By.xpath("//*[@id=\"search-city\"]")).sendKeys(city);
        WebElement afterClick = (new WebDriverWait(webDriver, Duration.ofSeconds(20)))
                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(String.format("//strong[starts-with(text(), '%s')]", city))));
        afterClick.click();
        (new WebDriverWait(webDriver, Duration.ofSeconds(20)))
                .until(ExpectedConditions.visibilityOfElementLocated(By.className("NotifyCitychange")));
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

    private boolean isDelivered(Document page) {
        return !page.getElementsByClass("icon icon--delivery").isEmpty();
    }

}

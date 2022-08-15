package ru.vadim.pharmacyagregator.util;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.Data;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.stereotype.Service;

import javax.print.Doc;
import java.io.IOException;

@Data
@Service
public class AptekaRuParser {

    public Document seleniumParse(String link) {
        WebDriverManager.chromedriver().arch64().setup();
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--disable-popup-blocking");
        chromeOptions.addArguments("disable-infobars");
        WebDriver webDriver = new ChromeDriver(chromeOptions);
        webDriver.get(link);
        WebElement goods = webDriver.findElement(By.xpath("//*[@id=\"app\"]/div[2]/div[1]/div/div/div/div[1]/div[1]/button[2]"));
        goods.click();
        Document document = Jsoup.parse(webDriver.getPageSource());
        return document;
    }

    public void parse(String link) throws IOException {
        Document doc = seleniumParse(link);
        Elements catalogElements = doc.select("div.SidebarCategoriesList > ul").get(0).children();
        for (Element groups : catalogElements) {
            Elements group = groups.children();
            String categoryLink = "https://apteka.ru" + group.get(0).attr("href");
            String name = group.get(0).children().text();
            System.out.println(categoryLink);
            System.out.println(name);
        }
    }
}

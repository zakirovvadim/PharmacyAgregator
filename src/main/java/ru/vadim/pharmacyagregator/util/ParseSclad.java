package ru.vadim.pharmacyagregator.util;

import lombok.Data;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import ru.vadim.pharmacyagregator.domain.Pharm;


import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Data
@Service
public class ParseSclad {

    private static final String PAGE = "?PAGEN_1=";

    public List<Pharm> parse(String link) throws IOException {
        Document doc = Jsoup.connect(link)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.134 Safari/537.36 OPR/89.0.4447.71")
                .get();
        Elements pages = null;
        int maxPage;
        if(!doc.getElementsByClass("paginator").isEmpty()) {
            pages = doc.getElementsByClass("paginator").get(0).children();
            maxPage = Integer.parseInt(pages.get(pages.size() - 2).child(0).text());
        } else {
            maxPage = 2;
        }
        List<Pharm> parsedFarm = new ArrayList<>();
        for(int i = 1; i < maxPage; i++) {
            Document everyPageWithPharm = Jsoup.connect(link + PAGE + i)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.134 Safari/537.36 OPR/89.0.4447.71")
                    .timeout(100000)
                    .get();
            Elements itemElements= everyPageWithPharm.select("div[class=cat-item]");
            System.out.println(i);
            for(Element child : itemElements) {
                Pharm pharm = new Pharm();
                assert child.parentNode() != null;
                pharm.setId(Long.parseLong(child.parentNode().attr("data-element-id")));
                Elements s1 = child.select("div.cat-item__title");
                pharm.setTitle(s1.get(0).text());
                Elements s = child.select("div.cat-item__text");
                if(s.size() == 3) {
                    pharm.setExpirationDate(getExpirationDateFromString(s.get(0).text()));
                    pharm.setProducerPharm(getProducerFromString(s.get(1).text()));
                    pharm.setActiveSubstance(s.get(2).text());
                }
                if(s.size() == 2) {
                    pharm.setExpirationDate(null);
                    pharm.setProducerPharm(getProducerFromString(s.get(0).text()));
                    pharm.setActiveSubstance(s.get(1).text());
                }
                parsedFarm.add(pharm);
            }
        }
        return parsedFarm;
    }

    private LocalDate getExpirationDateFromString(String date) {
        String extract = date.replaceAll("\\s", "");
        String [] extractDate = extract.split(":");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return LocalDate.parse(extractDate[1], formatter);
    }

    private String getProducerFromString(String producer) {
        String extract = producer.replaceAll("\\s", "");
        String [] devideString = extract.split(":");
        return devideString[1];
    }
}

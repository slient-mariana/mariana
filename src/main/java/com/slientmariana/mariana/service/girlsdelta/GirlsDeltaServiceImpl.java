package com.slientmariana.mariana.service.girlsdelta;

import com.slientmariana.mariana.vo.Actor;
import com.slientmariana.mariana.vo.MovieNfo;
import com.slientmariana.mariana.vo.MovieRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GirlsDeltaServiceImpl implements GirlsDeltaService{

    @Value("${studio.girlsDelta.japaneseName}")
    private String japaneseName;

    @Value("${studio.girlsDelta.prefix}")
    private String studioPrefix;

    @Value("${studio.girlsDelta.uri}")
    private String baseUri;

    @Value("${studio.girlsDelta.product}")
    private String productEndpoint;

    @Value("${studio.girlsDelta.trailer}")
    private String trailerEndpoint;

    @Value("${app.movie.directory.temporary}")
    private String temporaryDirectory;

    private String movieSecretId;

    @Override
    public MovieNfo CreateGirlsDelta(MovieRequestDTO dto){
        String code = dto.getCode();
        code = "1710";

        // Step 1: Get movie metadata from web
        MovieNfo movieNfo = GetMovieData(code);

        // Step 2: Download Movie MultiMedia
        downloadMultiMedia(code, movieNfo);

        return movieNfo;
    }

    private MovieNfo GetMovieData(String code){
        MovieNfo movieNfo = new MovieNfo();

        String productUri = baseUri + productEndpoint;
        productUri = productUri.replace("{code}", code);

        try {
            Document document = Jsoup.connect(productUri)
                    .userAgent("Mozilla")
                    .timeout(5000)
                    .get();

            Elements productDetailElements = document.getElementById("product-detail")
                    .getElementsByClass("product-detail");
            Element movieElement = productDetailElements.get(0);
            Element actressElement = productDetailElements.get(1);
            Element sampleElement = document.getElementById("sample-video");

            movieSecretId = sampleElement.attr("src").substring(36,68);

            String releaseDate = null;
            List<String> tags = null;
            for (Element element : movieElement.getElementsByTag("li")){
                String elementType = element.getElementsByTag("h4").first().text();
                switch (elementType) {
                    case "公開日" -> releaseDate = element.getElementsByTag("p").first().text();
                    case "作品カテゴリ" -> tags = element.getElementsByTag("a")
                            .stream()
                            .map(Element::text)
                            .collect(Collectors.toList());
                }
            }

            String actorsJapaneseName = "";
            List<String> genres = null;
            for (Element element : actressElement.getElementsByTag("li")){
                String elementType = element.getElementsByTag("h4").first().text();
                switch (elementType) {
                    case "モデル名" -> actorsJapaneseName = element.getElementsByTag("a").first().text();
                    case "モデルカテゴリ" -> genres = element.getElementsByTag("a")
                            .stream()
                            .map(Element::text)
                            .collect(Collectors.toList());
                }
            }

            // 1. Title
            // Custodian Title: [GirlsDelta-{code}] {Title} {Japanese Name}
            String title = document.getElementsByClass("large-5 prod-name").text();
            String custodianTitle = String.format("[%s-%s] %s %s", studioPrefix, code, title, actorsJapaneseName);
            log.info("Title: {}", title);
            log.info("Custodian Title: {}", custodianTitle);
            movieNfo.setOriginalTitle(title);
            movieNfo.setTitle(custodianTitle);
            movieNfo.setSortTitle(custodianTitle);

            // 3. mmpa
            movieNfo.setMpaa("NC-17");

            // 5. Tag and Genre
            log.info("Tags: {}", tags);
            log.info("Genres: {}", genres);
            movieNfo.setTags(tags);
            movieNfo.setGenres(genres);

            // 6. country
            movieNfo.setCountry("Japan");
            movieNfo.setCountryCode("JP");
            movieNfo.setLanguage("ja");

            // 7. Release Date
            movieNfo.setPremiered(releaseDate);
            movieNfo.setReleaseDate(releaseDate);
            movieNfo.setYear(releaseDate.substring(0,4));

            // 8. Studio
            movieNfo.setStudio(japaneseName);

            // 9. Actor
            List<Actor> actors = new ArrayList<>();
            Actor actor = new Actor();
            actor.setOrder(0);
            actor.setName(actorsJapaneseName);
            actor.setRole(actorsJapaneseName);
            actors.add(actor);
            movieNfo.setActors(actors);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return movieNfo;
    }

    private void downloadMultiMedia(String code, MovieNfo movieNfo){
        // Step 2: Download Image
        try {
            // Step 2a: Create Movie Directory
            String movieDirectory = String.format("%s/%s", temporaryDirectory, movieNfo.getTitle());
            Path movieDirectoryPath = Paths.get(movieDirectory);
            Files.createDirectories(movieDirectoryPath);

            // Step 2c: Download Trailer
            // https://girlsdelta.com/pics/product/92930ec2af2db2a19eb35589ba035d01/movie.mp4
            String trailerUriStr = baseUri + trailerEndpoint;
            trailerUriStr = trailerUriStr.replace("{movieSecretId}", movieSecretId);
            URL trailerUrl = new URL(trailerUriStr);
            File trailerFile = new File(String.valueOf(movieDirectoryPath), String.format("%s - trailer.mp4", movieNfo.getTitle()));
            FileUtils.copyURLToFile(trailerUrl, trailerFile);

            // Step 2b: Download poster, fanart, landscape


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

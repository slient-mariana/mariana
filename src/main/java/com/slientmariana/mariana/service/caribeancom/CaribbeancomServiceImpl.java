package com.slientmariana.mariana.service.caribeancom;

import com.slientmariana.mariana.vo.MovieNfo;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.nio.charset.StandardCharsets.*;

@Service
@Slf4j
public class CaribbeancomServiceImpl implements CaribbeancomService {

    final String OLD_FORMAT = "yyyy/MM/dd";
    final String NEW_FORMAT = "yyyy-MM-dd";

    @Value("${studio.caribbeancom.uri}")
    private String caribbeancomUri;

    @Value("${studio.caribbeancom.moviepages}")
    private String moviepages;

    @Override
    public void CreateCaribbeancom(){
        String code = "020222-001";
        // Step 1: Get Movie Data
        GetMovieData(code);

    }

    private Mono<String> GetMovieData_x(String code){

        WebClient client = WebClient.create(caribbeancomUri);
        return client.get()
                .uri(moviepages,code)
                .acceptCharset(UTF_8)
                .accept(MediaType.TEXT_HTML)
                .retrieve()
                .bodyToMono(String.class);
    }

    private MovieNfo GetMovieData(String code){

        SimpleDateFormat sdf = new SimpleDateFormat(OLD_FORMAT);

        MovieNfo movieNfo = new MovieNfo();
        List<String> actors = new ArrayList<>();

        String moviepagesUri = caribbeancomUri + moviepages;
        moviepagesUri = moviepagesUri.replace("{code}", code);
        log.info("Uri: {}", moviepagesUri);

        try {
            Document document = Jsoup.connect(moviepagesUri)
                    .userAgent("Mozilla")
                    .timeout(3000)
                    .get();

            // 1. Plot
            String plot = document.select("p[itemprop=description]").text();
            log.info("Plot: {}", plot);
            movieNfo.setPlot(plot);

            // 2. Title
            String title = document.select("h1[itemprop=name]").text();
            log.info("Title: {}", title);
            movieNfo.setTitle(title);

            // 3. Release Date
            String releaseDate = document.select("span[itemprop=uploadDate]").text();
            log.info("Release Date: {}", releaseDate);
            Date date = sdf.parse(releaseDate);
            sdf.applyPattern(NEW_FORMAT);
            movieNfo.setReleaseDate(sdf.format(date));
            movieNfo.setYear(sdf.format(date).substring(0,4));

            // Actor
            Elements actorElements = document.getElementsByClass("spec__tag");
            actorElements.forEach(element -> actors.add(element.text()));
            log.info("Actor: {}", actors);
            movieNfo.setActors(actors);


        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return movieNfo;
    }
}

package com.slientmariana.mariana.service.heyzo;

import com.slientmariana.mariana.vo.MovieNfo;
import com.slientmariana.mariana.vo.MovieRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;

@Slf4j
@Service
public class HeyzoServiceImpl implements HeyzoService{

    @Value("${studio.heyzo.uri}")
    private String heyzoUri;

    @Value("${studio.heyzo.uriEn}")
    private String heyzoUriEn;

    @Value("${studio.heyzo.prefix}")
    private String studioPrefix;

    @Value("${studio.heyzo.moviepages}")
    private String moviepages;

    @Override
    public MovieNfo CreateHeyzo(MovieRequestDTO dto){
        String code = dto.getCode();

        // Step 1: Get Movie Data
        MovieNfo movieNfo = GetMovieData(code);
        return null;
    }

    private MovieNfo GetMovieData(String code){
        MovieNfo movieNfo = new MovieNfo();

        String moviepagesUri = heyzoUri + moviepages;
        moviepagesUri = moviepagesUri.replace("{code}", code);

        String moviePagesUriEn = heyzoUriEn + moviepages;
        moviePagesUriEn = moviePagesUriEn.replace("{code}", code);

        try {
            Document document = Jsoup.connect(moviepagesUri)
                    .userAgent("Mozilla")
                    .timeout(3000)
                    .get();

            Document documentEn = Jsoup.connect(moviePagesUriEn)
                    .userAgent("Mozilla")
                    .timeout(3000)
                    .get();

            // 1. Title
            String title = document.getElementById("movie").getElementsByTag("h1").get(0).text();
            String titleEn = documentEn.getElementById("movie").getElementsByTag("h1").get(0).text();
            String custodianTitle = String.format("[%s-%s] %s", studioPrefix, code, title);
            log.info("Title: {}", title);
            movieNfo.setOriginalTitle(title);
            movieNfo.setTitle(custodianTitle);
            movieNfo.setSortTitle(custodianTitle);
            if (titleEn != null && !titleEn.isEmpty()){
                movieNfo.setTagline(titleEn);
            }

            // 2. Plot
            //String desc =

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

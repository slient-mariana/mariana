package com.slientmariana.mariana.service.caribeancom;

import com.slientmariana.mariana.vo.MovieNfo;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class CaribbeancomServiceImpl implements CaribbeancomService {

    final String OLD_FORMAT = "yyyy/MM/dd";
    final String NEW_FORMAT = "yyyy-MM-dd";

    @Value("${studio.caribbeancom.uri}")
    private String caribbeancomUri;

    @Value("${studio.caribbeancom.prefix}")
    private String studioPrefix;

    @Value("${studio.caribbeancom.moviepages}")
    private String moviepages;

    @Value("${studio.caribbeancom.gallery}")
    private String galleryUri;

    @Value("${studio.caribbeancom.trailer}")
    private String trailerUri;

    @Value("${studio.caribbeancom.poster}")
    private String posterUri;
    @Override
    public MovieNfo CreateCaribbeancom(){
        String code = "020222-001";

        // Step 1: Get Movie Data
        MovieNfo movieNfo = GetMovieData(code);

        // Step 2: Download Movie MultiMedia
        downloadMultiMedia(code, movieNfo);

        return movieNfo;
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
            String custodianTitle = String.format("[%s-%s] %s", studioPrefix, code, title);
            log.info("Title: {}", title);
            movieNfo.setOriginalTitle(title);
            movieNfo.setTitle(custodianTitle);
            movieNfo.setSortTitle(custodianTitle);

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

    private void downloadMultiMedia(String code, MovieNfo movieNfo){
        //carib-011422-001
        // Step 2: Download Image
        try {
            // Step 2a: Create Movie Directory
            Path movieDirectoryPath = Paths.get(movieNfo.getTitle());
            Files.createDirectories(movieDirectoryPath);

            // Step 2b: Download Gallery Zip
            String galleryFullUriString = caribbeancomUri + galleryUri;
            galleryFullUriString = galleryFullUriString.replace("{code}", code);
            URL galleryUrl = new URL(galleryFullUriString);
            File extraFanArtFile = new File(String.valueOf(movieDirectoryPath), "extrafanart.zip");
            FileUtils.copyURLToFile(galleryUrl, extraFanArtFile);

            // Step 2c: Unzip extrafanart.zip and delete the extrafanart.zip
            File extraFanArtDir = new File(String.valueOf(movieDirectoryPath), "extrafanart");
            new ZipFile(extraFanArtFile).extractAll(extraFanArtDir.toString());
            extraFanArtFile.delete();
            for (File extraFanArt : extraFanArtDir.listFiles()){
                File newExtraFanArt = new File(extraFanArt.getParent(), "fanart"+extraFanArt.getName());
                extraFanArt.renameTo(newExtraFanArt);
            }

            // Step 2d: Download Trailer
            trailerUri = trailerUri.replace("{code}", code);
            trailerUri = trailerUri.replace("{resolution}", "1080p");
            URL trailerUrl = new URL(trailerUri);
            File trailerFile = new File(String.valueOf(movieDirectoryPath), "trailer.mp4");
            FileUtils.copyURLToFile(trailerUrl, trailerFile);

            // Step 2e: Download poster, fanart, landscape
            String galleryFullUriString = caribbeancomUri + galleryUri;
            galleryFullUriString = galleryFullUriString.replace("{code}", code);
            URL galleryUrl = new URL(galleryFullUriString);
            File extraFanArtFile = new File(String.valueOf(movieDirectoryPath), "extrafanart.zip");
            FileUtils.copyURLToFile(galleryUrl, extraFanArtFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

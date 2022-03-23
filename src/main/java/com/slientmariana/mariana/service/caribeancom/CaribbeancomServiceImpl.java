package com.slientmariana.mariana.service.caribeancom;

import com.slientmariana.mariana.tools.Tools;
import com.slientmariana.mariana.vo.Actor;
import com.slientmariana.mariana.vo.MovieNfo;
import com.slientmariana.mariana.vo.MovieRequestDTO;
import com.slientmariana.mariana.vo.Set;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CaribbeancomServiceImpl implements CaribbeancomService {

    final String OLD_FORMAT = "yyyy/MM/dd";
    final String NEW_FORMAT = "yyyy-MM-dd";

    @Value("${studio.caribbeancom.name}")
    private String englishName;

    @Value("${studio.caribbeancom.japaneseName}")
    private String japaneseName;

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

    @Value("${app.movie.directory.temporary}")
    private String temporaryDirectory;

    @Autowired
    private Tools tools;

    @Override
    public MovieNfo CreateCaribbeancom(MovieRequestDTO dto){
        String code = dto.getCode();

        // Step 1: Get Movie Data
        MovieNfo movieNfo = GetMovieData(code);

        // Step 2: Download Movie MultiMedia
        downloadMultiMedia(code, movieNfo);

        // Step 3: Create NFO file
        tools.CreateNFOFile(movieNfo);

        try {
            // Step 4: Upload to emby server
            tools.CopyDirectoryToServer(englishName, movieNfo);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return movieNfo;
    }

    private MovieNfo GetMovieData(String code){

        SimpleDateFormat sdf = new SimpleDateFormat(OLD_FORMAT);

        MovieNfo movieNfo = new MovieNfo();


        String moviepagesUri = caribbeancomUri + moviepages;
        moviepagesUri = moviepagesUri.replace("{code}", code);
        log.info("Uri: {}", moviepagesUri);

        try {
            Document document = Jsoup.connect(moviepagesUri)
                    .userAgent("Mozilla")
                    .timeout(3000)
                    .get();

            // 1. Title
            String title = document.select("h1[itemprop=name]").text();
            String custodianTitle = String.format("[%s-%s] %s", studioPrefix, code, title);
            log.info("Title: {}", title);
            movieNfo.setOriginalTitle(title);
            movieNfo.setTitle(custodianTitle);
            movieNfo.setSortTitle(custodianTitle);

            // 2. Plot
            String plot = document.select("p[itemprop=description]").text();
            log.info("Plot: {}", plot);
            movieNfo.setPlot(plot);

            // 3. mmpa
            movieNfo.setMpaa("NC-17");

            // Sets
            Elements movieSpecElements = document.getElementsByClass("movie-spec");
            List<Set> sets = movieSpecElements
                    .stream()
                    .filter(element -> "シリーズ".equals(element.getElementsByClass("spec-title").text()))
                    .map(element -> {
                        Set set = new Set();
                        String strSet = element.getElementsByClass("spec-content").text();
                        set.setName(strSet);
                        set.setOverview(strSet);
                        log.info(set.toString());
                        return set;
                    })
                    .collect(Collectors.toList());
            movieNfo.setSets(sets);

            // Tag and Genre
            List<String> tags = new ArrayList<>();
            List<String> genres = new ArrayList<>();
            for (Set set : sets){
                tags.add(set.getName());
            }
            Elements specItemElements = document.getElementsByClass("spec-item");
            specItemElements.forEach(element -> {
                if (element.is("a[itemprop=url]")){
                    tags.add(element.text());
                }
                if (element.is("a[itemprop=genre]")){
                    genres.add(element.text());
                }
            });
            log.info("Tags: {}", tags);
            log.info("Genres: {}", genres);
            movieNfo.setTags(tags);
            movieNfo.setGenres(genres);

            // 4. country
            movieNfo.setCountry("Japan");
            movieNfo.setCountryCode("JP");
            movieNfo.setLanguage("ja");

            // 5. Release Date
            String releaseDate = document.select("span[itemprop=uploadDate]").text();
            log.info("Release Date: {}", releaseDate);
            Date date = sdf.parse(releaseDate);
            sdf.applyPattern(NEW_FORMAT);
            movieNfo.setPremiered(sdf.format(date));
            movieNfo.setReleaseDate(sdf.format(date));
            movieNfo.setYear(sdf.format(date).substring(0,4));

            // 6. Studio
            movieNfo.setStudio(japaneseName);

            // Actor
            List<Actor> actors = new ArrayList<>();
            int actorOrder = 0;
            Elements actorElements = document.getElementsByClass("spec__tag");
            for (Element element : actorElements){
                Actor actor = new Actor();
                String actorName = element.text();
                actor.setName(actorName);
                actor.setOrder(actorOrder);
                actor.setRole(actorName);
                log.info(actor.toString());
                actorOrder = actorOrder + 1;
                actors.add(actor);
            }
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
            String movieDirectory = String.format("%s/%s",temporaryDirectory, movieNfo.getTitle());
            Path movieDirectoryPath = Paths.get(movieDirectory);
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
                if (extraFanArt.isDirectory()){
                    String galleryParent = extraFanArt.getParent();
                    for (File extraFanArtInGallery : extraFanArt.listFiles()){
                        File newExtraFanArt = new File(galleryParent, "fanart" + extraFanArtInGallery.getName());
                        extraFanArtInGallery.renameTo(newExtraFanArt);
                    }
                }

                if (extraFanArt.isFile()) {
                    File newExtraFanArt = new File(extraFanArt.getParent(), "fanart" + extraFanArt.getName());
                    extraFanArt.renameTo(newExtraFanArt);
                }
            }

            // Step 2d: Download Trailer
            String trailerUriStr = trailerUri.replace("{code}", code);
            trailerUriStr = trailerUriStr.replace("{resolution}", "1080p");
            URL trailerUrl = new URL(trailerUriStr);
            File trailerFile = new File(String.valueOf(movieDirectoryPath), String.format("%s - trailer.mp4", movieNfo.getTitle()));
            FileUtils.copyURLToFile(trailerUrl, trailerFile);

            // Step 2e: Download poster, fanart, landscape
            String posterFullUriString = caribbeancomUri + posterUri;
            posterFullUriString = posterFullUriString.replace("{code}", code);
            URL posterUrl = new URL(posterFullUriString);
            File posterFile = new File(String.valueOf(movieDirectoryPath), "poster.jpg");
            File fanartFile = new File(String.valueOf(movieDirectoryPath), "fanart.jpg");
            File landscapFile = new File(String.valueOf(movieDirectoryPath), "landscape.jpg");
            FileUtils.copyURLToFile(posterUrl, posterFile);
            Files.copy(posterFile.toPath(), fanartFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(posterFile.toPath(), landscapFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyDirectoryToServer(){

    }
}

package com.slientmariana.mariana.service.pondo;

import com.google.gson.Gson;
import com.slientmariana.mariana.tools.Tools;
import com.slientmariana.mariana.vo.Actor;
import com.slientmariana.mariana.vo.MovieNfo;
import com.slientmariana.mariana.vo.MovieRequestDTO;
import com.slientmariana.mariana.vo.Set;
import com.slientmariana.mariana.vo.pondo.MovieDetail;
import com.slientmariana.mariana.vo.pondo.MovieGallery;
import com.slientmariana.mariana.vo.pondo.Row;
import com.slientmariana.mariana.vo.pondo.SampleFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Slf4j
@Service
public class PondoServiceImpl implements PondoService{

    @Value("${studio.pondo.prefix}")
    private String studioPrefix;

    @Value("${studio.pondo.japaneseName}")
    private String japaneseName;

    @Value("${studio.pondo.uri}")
    private String pondoUri;

    @Value("${studio.pondo.movieDetails}")
    private String movieDetailsApi;

    @Value("${studio.pondo.movieGallery}")
    private String movieGalleryApi;

    @Value("${studio.pondo.movieGalleries}")
    private String movieGalleriesApi;

    @Value("${app.movie.directory.temporary}")
    private String temporaryDirectory;

    @Autowired
    private Tools tools;

    @Override
    public MovieNfo Create1Pondo(MovieRequestDTO dto){

        String code = dto.getCode();
        // Step 1: Get Movie Data
        MovieDetail movieDetail = GetMovieDetail(code);

        // Step 2: Convert Movie Detail to Nfo
        MovieNfo movieNfo = ConvertDetail2Nfo(movieDetail);

        // Step 3: Download Movie MultiMedia
        downloadMultiMedia(movieDetail, movieNfo);

        // Step 4: Create NFO file
        tools.CreateNFOFile(movieNfo);

        return movieNfo;
    }

    private MovieDetail GetMovieDetail(String code){
        RestTemplate restTemplate = new RestTemplate();
        String movieDetailUri = pondoUri + movieDetailsApi;
        movieDetailUri = movieDetailUri.replace("{code}", code);
        log.info("Uri: {}", movieDetailUri);
        String movieDetailJson = restTemplate.getForObject(movieDetailUri, String.class);
        return new Gson().fromJson(movieDetailJson, MovieDetail.class);
    }

    private MovieNfo ConvertDetail2Nfo(MovieDetail movieDetail){
        MovieNfo movieNfo = new MovieNfo();

        // 1. Title
        String title = movieDetail.getTitle();
        String titleEn = movieDetail.getTitleEn();
        log.info("Title: {}", title);
        String custodianTitle = String.format("[%s-%s] %s", studioPrefix, movieDetail.getMovieID(), title);
        movieNfo.setOriginalTitle(title);
        movieNfo.setTitle(custodianTitle);
        movieNfo.setSortTitle(custodianTitle);
        if (Objects.nonNull(titleEn) && !titleEn.equals("null")){
            movieNfo.setTagline(titleEn.replace("\t",""));
        }


        // 2. Plot
        String desc = movieDetail.getDesc();
        String descEn = movieDetail.getDescEn();
        String plot;
        if (descEn != null && !descEn.equals("null") && !descEn.isEmpty()) {
            plot = String.format("%s\r\n%s", descEn, desc);
        } else {
            plot = desc;
        }
        log.info("Plot: {}", plot);
        movieNfo.setPlot(plot);

        // 3. mmpa
        movieNfo.setMpaa("NC-17");

        // 4. Sets
        List<Set> sets = new ArrayList<>();
        String set = movieDetail.getSeries();
        String setEn = movieDetail.getSeriesEn();
        if (set != null && !set.equals("null")){
            Set seriesSet = new Set();
            seriesSet.setName(set);
            if (setEn != null && !setEn.equals("null")){
                seriesSet.setOverview(String.format("%s (%s)", set, setEn));
            }else {
                seriesSet.setOverview(set);
            }
            log.info(seriesSet.toString());
            sets.add(seriesSet);
        }
        movieNfo.setSets(sets);

        // 5. Tag and Genre
        List<String> tags = new ArrayList<>();
        for (Set s : sets){
            tags.add(s.getName());
        }
        List<String> genres = new ArrayList<>(movieDetail.getUCNAME());
        log.info("Tags: {}", tags);
        log.info("Genres: {}", genres);
        movieNfo.setTags(tags);
        movieNfo.setGenres(genres);

        // 6. country
        movieNfo.setCountry("Japan");
        movieNfo.setCountryCode("JP");
        movieNfo.setLanguage("ja");

        // 7. Release Date
        String releaseDate = movieDetail.getRelease();
        log.info("Release Date: {}", releaseDate);
        movieNfo.setPremiered(releaseDate);
        movieNfo.setReleaseDate(releaseDate);
        movieNfo.setYear(movieDetail.getYear());

        // 8. Studio
        movieNfo.setStudio(japaneseName);

        // 9. Actor
        List<Actor> actors = new ArrayList<>();
        int actorOrder = 0;
        for (String actorName : movieDetail.getActressesJa()){
            Actor actor = new Actor();
            String roleName = String.format("%s (%s)", actorName, movieDetail.getActressesEn().get(actorOrder));
            actor.setName(actorName);
            actor.setOrder(actorOrder);
            actor.setRole(roleName);
            log.info(actor.toString());
            actorOrder = actorOrder + 1;
            actors.add(actor);
        }
        movieNfo.setActors(actors);

        return movieNfo;
    }

    private void downloadMultiMedia(MovieDetail movieDetail, MovieNfo movieNfo){

        String code = movieDetail.getMovieID();
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Step 1a: Create Movie Directory
            String movieDirectory = String.format("%s/%s",temporaryDirectory, movieNfo.getTitle());
            Path movieDirectoryPath = Paths.get(movieDirectory);
            Files.createDirectories(movieDirectoryPath);

            // Step 1b. Download Trailer
            Optional<SampleFile> sampleFile = movieDetail.getSampleFiles()
                    .stream()
                    .max(Comparator.comparingInt(sample -> sample.getFileSize()));
            sampleFile.ifPresent(file ->{
                try {
                    URL trailerUrl = new URL(file.getURL());
                    File trailerFile = new File(movieDirectory, String.format("%s - trailer.mp4", movieNfo.getTitle()));
                    FileUtils.copyURLToFile(trailerUrl, trailerFile);
                } catch (Exception e){
                    e.printStackTrace();
                }
            });

            // Step 1c: Download poster, fanart, landscape
            URL posterUrl = new URL(movieDetail.getThumbUltra());
            File posterFile = new File(String.valueOf(movieDirectoryPath), "poster.jpg");
            File fanartFile = new File(String.valueOf(movieDirectoryPath), "fanart.jpg");
            File landscapFile = new File(String.valueOf(movieDirectoryPath), "landscape.jpg");
            FileUtils.copyURLToFile(posterUrl, posterFile);
            Files.copy(posterFile.toPath(), fanartFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(posterFile.toPath(), landscapFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            File extraFanArtDir = new File(String.valueOf(movieDirectoryPath), "extrafanart");

            // Step 2: Movie Gallery API
            String movieGalleryUri = pondoUri + movieGalleryApi;
            movieGalleryUri = movieGalleryUri.replace("{code}", code);
            log.info("Movie Gallery Uri: {}", movieGalleryUri);
            ResponseEntity<String> movieGalleryResponse = restTemplate.getForEntity(movieGalleryUri, String.class);
            if (movieGalleryResponse.getStatusCode().equals(HttpStatus.OK)){
                MovieGallery movieGallery = new Gson().fromJson(movieGalleryResponse.getBody(), MovieGallery.class);
                // https://www.1pondo.tv/dyn/dla/images/
                String movieGalleryBaseUri = pondoUri + "/dyn/dla/images/";
                int rowNumber = 0;
                for (Row row : movieGallery.getRows()){
                    rowNumber = rowNumber + 1;
                    String imageUri = movieGalleryBaseUri + row.getImg();
                    URL imageUrl = new URL(imageUri);
                    File imageFile = new File(String.valueOf(extraFanArtDir), String.format("fanart%s.jpg",rowNumber));
                    FileUtils.copyURLToFile(imageUrl, imageFile);
                }
            }

            // Step 3: Movie Galleries API
            String movieGalleriesUri = pondoUri + movieGalleriesApi;
            movieGalleriesUri = movieGalleriesUri.replace("{code}", code);
            log.info("Movie Galleries Uri: {}", movieGalleriesUri);
            ResponseEntity<String> movieGalleriesResponse = restTemplate.getForEntity(movieGalleryUri, String.class);
            if (movieGalleriesResponse.getStatusCode().equals(HttpStatus.OK)){

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

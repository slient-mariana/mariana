package com.slientmariana.mariana.service.hey;

import com.slientmariana.mariana.tools.Tools;
import com.slientmariana.mariana.vo.Actor;
import com.slientmariana.mariana.vo.MovieNfo;
import com.slientmariana.mariana.vo.MovieRequestDTO;
import com.slientmariana.mariana.vo.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HeyServiceImpl implements HeyService {

    static int GALLERY_SIZE = 21;

    @Value("${studio.heyzo.name}")
    private String englishName;

    @Value("${studio.heyzo.japaneseName}")
    private String japaneseName;

    @Value("${studio.heyzo.uri}")
    private String heyzoUri;

    @Value("${studio.heyzo.uriEn}")
    private String heyzoUriEn;

    @Value("${studio.heyzo.prefix}")
    private String studioPrefix;

    @Value("${studio.heyzo.moviepages}")
    private String moviepages;

    @Value("${studio.heyzo.trailer}")
    private String trailerUri;

    @Value("${studio.heyzo.poster}")
    private String posterUri;

    @Value("${studio.heyzo.gallery.jjgirls}")
    private String jjgirlsUri;

    @Value("${app.movie.directory.temporary}")
    private String temporaryDirectory;

    @Autowired
    private Tools tools;

    @Override
    public List<MovieNfo> CreateHey(List<String> codes){
        return codes.stream()
                .map(this::CreateHeyzo)
                .collect(Collectors.toList());
    }

    @Override
    public MovieNfo CreateHeyzo(String code){

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
        MovieNfo movieNfo = new MovieNfo();

        String moviepagesUri = heyzoUri + moviepages;
        moviepagesUri = moviepagesUri.replace("{code}", code);

        String moviePagesUriEn = heyzoUriEn + moviepages;
        moviePagesUriEn = moviePagesUriEn.replace("{code}", code);

        try {
            Document document = Jsoup.connect(moviepagesUri)
                    .userAgent("Mozilla")
                    .timeout(10 * 1000)
                    .get();

            Document documentEn = Jsoup.connect(moviePagesUriEn)
                    .userAgent("Mozilla")
                    .timeout(10 * 1000)
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

            // Get movieInfo
            Element movieInfoElement = document.getElementsByClass("movieInfo").get(0);
            Element movieInfoElementEn = documentEn.getElementsByClass("movieInfo").get(0);

            // 2. Plot
            String desc = movieInfoElement.select("p[class=memo]").text();
            String descEn = movieInfoElementEn.select("p[class=memo]").text();
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
            String set = movieInfoElement.getElementsByClass("table-series").get(0).getElementsByTag("td").get(1).text();
            String setEn = movieInfoElementEn.getElementsByTag("tr").stream().filter(element -> "Series".equals(element.getElementsByTag("td").get(0).text())).findFirst().get().getElementsByTag("td").get(1).text();
            if (set != null && !set.equals("-----")){
                Set seriesSet = new Set();
                seriesSet.setName(set);
                if (setEn != null && !setEn.equals("-----")){
                    seriesSet.setOverview(String.format("%s (%s)", set, setEn));
                }else {
                    seriesSet.setOverview(set);
                }
                log.info(seriesSet.toString());
                sets.add(seriesSet);
            }
            movieNfo.setSets(sets);

            // 5. Tag and Genre
            List<String> actorTypes = movieInfoElement
                    .getElementsByClass("table-actor-type")
                    .first()
                    .getElementsByTag("a")
                    .stream()
                    .map(Element::text).toList();
            List<String> keywords = movieInfoElement
                    .getElementsByClass("table-tag-keyword-big")
                    .first()
                    .getElementsByTag("a")
                    .stream()
                    .map(Element::text).toList();

            List<String> tags = new ArrayList<>();
            for (Set s : sets){
                tags.add(s.getName());
            }
            List<String> genres = new ArrayList<>(keywords);
            genres.addAll(actorTypes);

            log.info("Tags: {}", tags);
            log.info("Genres: {}", genres);
            movieNfo.setTags(tags);
            movieNfo.setGenres(genres);

            // 6. country
            movieNfo.setCountry("Japan");
            movieNfo.setCountryCode("JP");
            movieNfo.setLanguage("ja");

            // 7. Release Date
            String releaseDate = movieInfoElement
                    .getElementsByClass("table-release-day")
                    .first()
                    .getElementsByTag("td")
                    .get(1)
                    .text();
            log.info("Release Date: {}", releaseDate);
            movieNfo.setPremiered(releaseDate);
            movieNfo.setReleaseDate(releaseDate);
            movieNfo.setYear(releaseDate.substring(0,4));

            // 8. Studio
            movieNfo.setStudio(japaneseName);

            // 9. Actor
            List<String> actorsJapaneseName = movieInfoElement
                    .getElementsByClass("table-actor")
                    .first()
                    .getElementsByTag("span")
                    .stream().map(Element::text).toList();

            List<String> actorsEnglishName = movieInfoElementEn
                    .getElementsByTag("tr")
                    .get(1)
                    .getElementsByTag("a")
                    .stream().map(Element::text).toList();

            List<Actor> actors = new ArrayList<>();
            int actorOrder = 0;
            for (String actorJapaneseName : actorsJapaneseName){
                Actor actor = new Actor();
                String roleName = String.format("%s (%s)", actorJapaneseName, actorsEnglishName.get(actorOrder));
                actor.setName(actorJapaneseName);
                actor.setOrder(actorOrder);
                actor.setRole(roleName);
                log.info(actor.toString());
                actorOrder = actorOrder + 1;
                actors.add(actor);
            }
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
            String movieDirectory = String.format("%s/%s",temporaryDirectory, movieNfo.getTitle());
            Path movieDirectoryPath = Paths.get(movieDirectory);
            Files.createDirectories(movieDirectoryPath);

            // Step 2b: Download poster, fanart, landscape
            String posterFullUriString = heyzoUri + posterUri;
            posterFullUriString = posterFullUriString.replace("{code}", code);
            URL posterUrl = new URL(posterFullUriString);
            File posterFile = new File(String.valueOf(movieDirectoryPath), "poster.jpg");
            File fanartFile = new File(String.valueOf(movieDirectoryPath), "fanart.jpg");
            File landscapFile = new File(String.valueOf(movieDirectoryPath), "landscape.jpg");
            FileUtils.copyURLToFile(posterUrl, posterFile);
            Files.copy(posterFile.toPath(), fanartFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(posterFile.toPath(), landscapFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Step 2c: Download Trailer
            String trailerUriStr = heyzoUri + trailerUri;
            trailerUriStr = trailerUriStr.replace("{code}", code);
            URL trailerUrl = new URL(trailerUriStr);
            File trailerFile = new File(String.valueOf(movieDirectoryPath), String.format("%s - trailer.mp4", movieNfo.getTitle()));
            FileUtils.copyURLToFile(trailerUrl, trailerFile,10 * 1000, 10 * 1000);

            // Step 2d: Download extrafanart
            // find English Name inside ()
            Pattern actorRolePattern = Pattern.compile("\\((.*?)\\)");
            // get name from nfo
            String actorEnglishName = movieNfo
                    .getActors()
                    .stream()
                    .findFirst()
                    .get()
                    .getRole();
            Matcher m = actorRolePattern.matcher(actorEnglishName);
            String actorNameString = null;
            while (m.find()){
                actorNameString = m.group(1)
                        .toLowerCase()
                        .replaceAll(" ","-");
            }

            if (actorNameString != null && !actorNameString.isEmpty()) {
                File extraFanArtDir = new File(String.valueOf(movieDirectoryPath), "extrafanart");
                String galleryUriStr = jjgirlsUri;
                galleryUriStr = galleryUriStr
                        .replace("{code}", code)
                        .replace("{actor}", actorNameString);
                for (int currentPhototCount = 1; currentPhototCount <= GALLERY_SIZE; currentPhototCount++) {
                    String imageUri = galleryUriStr.replace("{currentPhotoNumber}",String.valueOf(currentPhototCount));
                    URL imageUrl = new URL(imageUri);
                    File imageFile = new File(String.valueOf(extraFanArtDir), String.format("fanart%s.jpg",currentPhototCount));
                    FileUtils.copyURLToFile(imageUrl, imageFile);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

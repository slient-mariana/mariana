package com.slientmariana.mariana.rest;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.slientmariana.mariana.service.caribeancom.CaribbeancomService;
import com.slientmariana.mariana.vo.MovieNfo;
import com.slientmariana.mariana.vo.MovieRequestDTO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping("caribbeancom")
public class CaribbeancomController {

    @Autowired
    private CaribbeancomService caribbeancomService;

    @PostMapping("/")
    public MovieNfo getCaribbeancom(@RequestBody MovieRequestDTO dto){
        return caribbeancomService.CreateCaribbeancom(dto);
    }

    @GetMapping("/test")
    public void test() throws IOException {
        String userDirectory = new File("").getAbsolutePath();
        log.info("Current Dir: {}", userDirectory);
        Files.createDirectories(Paths.get("tempFolder"));

        //Path tempDirectory = Files.createTempDirectory("baeldung-exists");
    }

    @GetMapping("/testConnection")
    public void testConnection(){
        Stream.of(new File("/Volumes/Mariana").listFiles())
                .forEachOrdered(file -> log.info("Name: {}", file.getName()));

        try {
            FileUtils.copyDirectory(new File("[Carib-020222-001] イチャラブアンソロジー"), new File("/Volumes/Mariana/Studio/Caribbeancom/2021"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

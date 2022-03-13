package com.slientmariana.mariana.rest;

import com.slientmariana.mariana.service.caribeancom.CaribbeancomService;
import com.slientmariana.mariana.vo.MovieNfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequestMapping("caribbeancom")
public class CaribbeancomController {

    @Autowired
    private CaribbeancomService caribbeancomService;

    @GetMapping("/")
    public MovieNfo getCaribbeancom(){
        return caribbeancomService.CreateCaribbeancom();
    }

    @GetMapping("/test")
    public void test() throws IOException {
        String userDirectory = new File("").getAbsolutePath();
        log.info("Current Dir: {}", userDirectory);
        Files.createDirectories(Paths.get("tempFolder"));

        //Path tempDirectory = Files.createTempDirectory("baeldung-exists");
    }

}

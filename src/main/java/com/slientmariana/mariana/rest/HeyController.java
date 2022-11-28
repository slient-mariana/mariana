package com.slientmariana.mariana.rest;

import com.slientmariana.mariana.rest.dto.RequestDTO;
import com.slientmariana.mariana.service.hey.HeyService;
import com.slientmariana.mariana.vo.MovieNfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/hey")
public class HeyController {

    @Autowired
    private HeyService heyService;

    @PostMapping("/")
    public ResponseEntity<List<MovieNfo>> CreateHey(@ModelAttribute RequestDTO dto){
        log.info("Code count: {}", dto.getCodes().size());
        return ResponseEntity.ok().body(heyService.CreateHey(dto.getCodes()));
    }
}

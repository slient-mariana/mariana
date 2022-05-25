package com.slientmariana.mariana.rest;

import com.slientmariana.mariana.service.girlsdelta.GirlsDeltaService;
import com.slientmariana.mariana.vo.MovieNfo;
import com.slientmariana.mariana.vo.MovieRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("girlsdelta")
public class GirlsDeltaController {

    @Autowired
    private GirlsDeltaService girlsDeltaService;

    @PostMapping("/")
    public MovieNfo getGirlsDelta(@RequestBody MovieRequestDTO dto) {
        return girlsDeltaService.CreateGirlsDelta(dto);
    }
}

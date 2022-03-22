package com.slientmariana.mariana.rest;

import com.slientmariana.mariana.service.pondo.PondoService;
import com.slientmariana.mariana.vo.MovieNfo;
import com.slientmariana.mariana.vo.MovieRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("1pondo")
public class PondoController {

    @Autowired
    PondoService pondoService;

    @PostMapping("/")
    public MovieNfo get1Pondo(@RequestBody MovieRequestDTO dto){
        return pondoService.Create1Pondo(dto);
    }
}

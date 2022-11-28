package com.slientmariana.mariana.rest;

import com.slientmariana.mariana.rest.dto.RequestDTO;
import com.slientmariana.mariana.service.car.CarService;
import com.slientmariana.mariana.vo.MovieNfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/car")
public class CarController extends AbstractController{

    @Autowired
    private CarService carService;

    @PostMapping("/")
    public ResponseEntity<List<MovieNfo>> createCarNFO(@ModelAttribute RequestDTO dto){
        log.info("Code count: {}", dto.getCodes().size());
        return ResponseEntity.ok().body(carService.CreateCar(dto.getCodes()));
    }
}
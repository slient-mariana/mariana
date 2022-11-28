package com.slientmariana.mariana.service.car;

import com.slientmariana.mariana.vo.MovieNfo;
import com.slientmariana.mariana.vo.MovieRequestDTO;

import java.util.List;

public interface CarService {
    MovieNfo CreateCaribbeancom(String code);
    List<MovieNfo> CreateCar(List<String> codes);
}

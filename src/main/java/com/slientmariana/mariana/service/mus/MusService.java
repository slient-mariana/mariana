package com.slientmariana.mariana.service.mus;

import com.slientmariana.mariana.vo.MovieNfo;

import java.util.List;

public interface MusService {
    MovieNfo createMus(String code);

    List<MovieNfo> createMus(List<String> codes);
}

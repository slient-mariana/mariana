package com.slientmariana.mariana.service.pon;

import com.slientmariana.mariana.vo.MovieNfo;
import com.slientmariana.mariana.vo.MovieRequestDTO;

import java.util.List;

public interface PonService {
    MovieNfo Create1Pondo(String code);

    List<MovieNfo> CreatePon(List<String> codes);
}

package com.slientmariana.mariana.service.hey;

import com.slientmariana.mariana.vo.MovieNfo;

import java.util.List;

public interface HeyService {
    MovieNfo CreateHeyzo(String code);

    List<MovieNfo> CreateHey(List<String> codes);
}

package com.slientmariana.mariana.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MovieNfo {
    String plot;
    String title;
    String originalTitle;
    String sortTitle;
    String year;
    String releaseDate;
    String studio;
    List<String> actors;
}

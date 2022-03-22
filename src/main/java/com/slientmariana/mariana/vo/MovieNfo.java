package com.slientmariana.mariana.vo;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JacksonXmlRootElement(localName = "movie")
public class MovieNfo {

    String title;

    @JacksonXmlProperty(localName = "originaltitle")
    String originalTitle;

    @JacksonXmlProperty(localName = "sorttitle")
    String sortTitle;

    String outline;

    String plot;

    String tagline;

    String mpaa;

    @JacksonXmlProperty(localName = "genre")
    @JacksonXmlElementWrapper(useWrapping = false)
    List<String> genres;

    @JacksonXmlProperty(localName = "tag")
    @JacksonXmlElementWrapper(useWrapping = false)
    List<String> tags;

    @JacksonXmlProperty(localName = "set")
    @JacksonXmlElementWrapper(useWrapping = false)
    List<Set> sets;

    String country;

    @JacksonXmlProperty(localName = "countrycode")
    String countryCode;

    String language;

    String premiered;

    @JacksonXmlProperty(localName = "releasedate")
    String releaseDate;

    String year;

    String studio;

    @JacksonXmlProperty(localName = "actor")
    @JacksonXmlElementWrapper(useWrapping = false)
    List<Actor> actors;
}

package com.slientmariana.mariana.vo.pondo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class MovieDetail {
    String MovieID;
    String Actor;
    List<String> ActressesJa;
    List<String> ActressesEn;
    String Desc;
    String DescEn;
    String Release;
    String Series;
    String SeriesEn;
    String ThumbUltra;
    String Title;
    String TitleEn;
    String Year;
    List<String> UCNAME;
    List<String> UCNAMEEn;
    List<SampleFile> SampleFiles;
}

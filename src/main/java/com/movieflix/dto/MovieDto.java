package com.movieflix.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieDto {

    private Integer id;
    private String title;
    private String original_title;
    private String overview;
    private String poster_path;
    private String backdrop_path;
    private String media_type;
    private boolean adult;
    private String original_language;
    private List<Integer> genre_ids;
    private double popularity;
    private String release_date;
    private boolean video;
    private double vote_average;
    private int vote_count;
}

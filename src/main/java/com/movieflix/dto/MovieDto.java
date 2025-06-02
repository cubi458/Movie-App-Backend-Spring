package com.movieflix.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieDto {
    private Integer id;
    private String title;
    private String director;
    private String studio;
    private Integer releaseYear;
    private String posterUrl;
    private String trailerLink;
    private boolean video;
    private String videoUrl;
}

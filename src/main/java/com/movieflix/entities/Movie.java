package com.movieflix.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@NoArgsConstructor
@Getter
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer movieId;

    @Column(nullable = false, length = 200)
    @NotBlank(message = "Please provide movie's title!")
    private String title;

    @Column(nullable = false)
    @NotBlank(message = "Please provide movie's director!")
    private String director;

    @Column(nullable = false)
    @NotBlank(message = "Please provide movie's studio!")
    private String studio;

    @ElementCollection
    @CollectionTable(name = "movie_cast")
    private Set<String> movieCast;

    @Column(nullable = false)
    private Integer releaseYear;

    @Column(nullable = false)
    @NotBlank(message = "Please provide movie's poster!")
    private String poster;

    @Column(nullable = true)
    private String trailerLink; // Link trailer cho phim

    @Column(nullable = true) // Đổi kiểu thành Boolean để có thể nhận giá trị null
    private Boolean video = false; // Đảm bảo giá trị mặc định là false

    public Movie(Integer movieId, String title, String director, String studio, Set<String> movieCast, Integer releaseYear, String poster, String trailerLink, Boolean video) {
        this.movieId = movieId;
        this.title = title;
        this.director = director;
        this.studio = studio;
        this.movieCast = movieCast;
        this.releaseYear = releaseYear;
        this.poster = poster;
        this.trailerLink = trailerLink;
        this.video = video;
    }

    // Getter và Setter cho video
    public Boolean getVideo() {
        return video;
    }

    public void setVideo(Boolean video) {
        this.video = video;
    }

    public void setTrailerLink(String trailerLink) {
        this.trailerLink = trailerLink;
    }
}


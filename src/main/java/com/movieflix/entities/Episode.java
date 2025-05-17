package com.movieflix.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Episode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer episodeId;

    @Column(nullable = false)
    private Integer episodeNumber; // Số tập

    @Column(nullable = false, length = 200)
    private String title; // Tiêu đề tập phim

    @Column(nullable = true)
    private String trailerLink; // Link trailer cho tập phim

    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie; // Mối quan hệ với Movie, mỗi tập thuộc một phim
    public void setEpisodeNumber(Integer episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTrailerLink(String trailerLink) {
        this.trailerLink = trailerLink;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

}

package com.movieflix.dto;

public class EpisodeDto {

    private Integer episodeNumber; // Số tập
    private String title; // Tiêu đề tập
    private String trailerLink; // Link trailer

    // Getter và Setter
    public Integer getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(Integer episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTrailerLink() {
        return trailerLink;
    }

    public void setTrailerLink(String trailerLink) {
        this.trailerLink = trailerLink;
    }
}

package com.movieflix.controllers;

import com.movieflix.dto.MovieDto;
import com.movieflix.service.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class TmdbStyleController {

    private final MovieService movieService;

    public TmdbStyleController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/trending/movie/day")
    public ResponseEntity<List<MovieDto>> getTrending() {
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @GetMapping("/movie/popular")
    public ResponseEntity<List<MovieDto>> getPopular() {
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @GetMapping("/movie/now_playing")
    public ResponseEntity<List<MovieDto>> getNowPlaying() {
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @GetMapping("/movie/upcoming")
    public ResponseEntity<List<MovieDto>> getUpcoming() {
        return ResponseEntity.ok(movieService.getAllMovies());
    }
}

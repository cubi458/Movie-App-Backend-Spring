package com.movieflix.service;

import com.movieflix.dto.EpisodeDto;
import com.movieflix.dto.MovieDto;
import com.movieflix.dto.MoviePageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MovieService {

    MovieDto addMovie(MovieDto movieDto, MultipartFile file, MultipartFile videoFile) throws IOException;

    MovieDto getMovie(Integer movieId);

    List<MovieDto> getAllMovies();

    MovieDto updateMovie(Integer movieId, MovieDto movieDto, MultipartFile file) throws IOException;

    String deleteMovie(Integer movieId) throws IOException;

    MoviePageResponse getAllMoviesWithPagination(Integer pageNumber, Integer pageSize);

    MoviePageResponse getAllMoviesWithPaginationAndSorting(Integer pageNumber, Integer pageSize, String sortBy, String dir);

    MovieDto addTrailer(Integer movieId, String trailerLink);  // Thêm phương thức addTrailer

    EpisodeDto addEpisode(Integer movieId, EpisodeDto episodeDto);  // Thêm phương thức addEpisode

    // Thêm phương thức addVideo
    String addVideo(Integer movieId, MultipartFile videoFile) throws IOException;  // Thêm phương thức addVideo
}

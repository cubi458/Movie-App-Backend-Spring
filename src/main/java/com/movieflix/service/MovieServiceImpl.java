package com.movieflix.service;

import com.movieflix.dto.MovieDto;
import com.movieflix.dto.MoviePageResponse;
import com.movieflix.entities.Movie;
import com.movieflix.exceptions.FileExistsException;
import com.movieflix.exceptions.MovieNotFoundException;
import com.movieflix.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final FileService fileService;

    @Value("${project.poster}")
    private String path;

    @Value("${base.url}")
    private String baseUrl;

    public MovieServiceImpl(MovieRepository movieRepository, FileService fileService) {
        this.movieRepository = movieRepository;
        this.fileService = fileService;
    }

    private MovieDto mapToFlutterDto(Movie movie) {
        String posterUrl = baseUrl + "/file/" + movie.getPoster();
        return new MovieDto(
                movie.getMovieId(),
                movie.getTitle(),
                movie.getTitle(),
                "This is a placeholder overview.",
                posterUrl,
                posterUrl,
                "movie",
                false,
                "en",
                List.of(28, 12),
                100.0,
                String.valueOf(movie.getReleaseYear()),
                false,
                8.5,
                1234
        );
    }

    @Override
    public MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws IOException {
        if (Files.exists(Paths.get(path + File.separator + file.getOriginalFilename()))) {
            throw new FileExistsException("File already exists! Please enter another file name!");
        }
        String uploadedFileName = fileService.uploadFile(path, file);

        Movie movie = new Movie(
                null,
                movieDto.getTitle(),
                "N/A",
                "N/A",
                null,
                2024,
                uploadedFileName
        );

        Movie savedMovie = movieRepository.save(movie);
        return mapToFlutterDto(savedMovie);
    }

    @Override
    public MovieDto getMovie(Integer movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with id = " + movieId));
        return mapToFlutterDto(movie);
    }

    @Override
    public List<MovieDto> getAllMovies() {
        List<Movie> movies = movieRepository.findAll();
        List<MovieDto> movieDtos = new ArrayList<>();
        for (Movie movie : movies) {
            movieDtos.add(mapToFlutterDto(movie));
        }
        return movieDtos;
    }

    @Override
    public MovieDto updateMovie(Integer movieId, MovieDto movieDto, MultipartFile file) throws IOException {
        Movie mv = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with id = " + movieId));

        String fileName = mv.getPoster();
        if (file != null) {
            Files.deleteIfExists(Paths.get(path + File.separator + fileName));
            fileName = fileService.uploadFile(path, file);
        }

        Movie updated = new Movie(
                mv.getMovieId(),
                movieDto.getTitle(),
                "N/A",
                "N/A",
                null,
                2024,
                fileName
        );

        Movie saved = movieRepository.save(updated);
        return mapToFlutterDto(saved);
    }

    @Override
    public String deleteMovie(Integer movieId) throws IOException {
        Movie mv = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with id = " + movieId));
        Files.deleteIfExists(Paths.get(path + File.separator + mv.getPoster()));
        movieRepository.delete(mv);
        return "Movie deleted with id = " + mv.getMovieId();
    }

    @Override
    public MoviePageResponse getAllMoviesWithPagination(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Movie> moviePages = movieRepository.findAll(pageable);
        List<MovieDto> movieDtos = moviePages.getContent().stream()
                .map(this::mapToFlutterDto)
                .toList();
        return new MoviePageResponse(movieDtos, pageNumber, pageSize,
                moviePages.getTotalElements(),
                moviePages.getTotalPages(),
                moviePages.isLast());
    }

    @Override
    public MoviePageResponse getAllMoviesWithPaginationAndSorting(Integer pageNumber, Integer pageSize,
                                                                  String sortBy, String dir) {
        Sort sort = dir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Movie> moviePages = movieRepository.findAll(pageable);
        List<MovieDto> movieDtos = moviePages.getContent().stream()
                .map(this::mapToFlutterDto)
                .toList();
        return new MoviePageResponse(movieDtos, pageNumber, pageSize,
                moviePages.getTotalElements(),
                moviePages.getTotalPages(),
                moviePages.isLast());
    }
}
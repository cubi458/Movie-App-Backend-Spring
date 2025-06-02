package com.movieflix.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieflix.dto.EpisodeDto;
import com.movieflix.dto.MovieDto;
import com.movieflix.dto.MoviePageResponse;
import com.movieflix.exceptions.EmptyFileException;
import com.movieflix.service.MovieService;
import com.movieflix.utils.AppConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/movie")
@CrossOrigin(origins = "*")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping("/add-movie")
    public ResponseEntity<MovieDto> addMovieHandler(
            @RequestPart(value = "file") MultipartFile file,
            @RequestPart(value = "video", required = false) MultipartFile videoFile,
            @RequestPart(value = "movieDto") String movieDtoStr) throws IOException, EmptyFileException {
        if (file.isEmpty()) {
            throw new EmptyFileException("File is empty! Please send another file!");
        }
        MovieDto movieDto = convertToMovieDto(movieDtoStr);
        return new ResponseEntity<>(movieService.addMovie(movieDto, file, videoFile), HttpStatus.CREATED);
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<MovieDto> getMovieHandler(@PathVariable Integer movieId) {
        return ResponseEntity.ok(movieService.getMovie(movieId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<MovieDto>> getAllMoviesHandler() {
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @PutMapping("/update/{movieId}")
    public ResponseEntity<MovieDto> updateMovieHandler(
            @PathVariable Integer movieId,
            @RequestPart(required = false) MultipartFile file,
            @RequestPart String movieDtoStr) throws IOException {
        MovieDto movieDto = convertToMovieDto(movieDtoStr);
        return ResponseEntity.ok(movieService.updateMovie(movieId, movieDto, file));
    }

    @DeleteMapping("/delete/{movieId}")
    public ResponseEntity<String> deleteMovieHandler(@PathVariable Integer movieId) throws IOException {
        return ResponseEntity.ok(movieService.deleteMovie(movieId));
    }

    @GetMapping("/allMoviesPage")
    public ResponseEntity<MoviePageResponse> getMoviesWithPagination(
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize) {
        return ResponseEntity.ok(movieService.getAllMoviesWithPagination(pageNumber, pageSize));
    }

    @GetMapping("/allMoviesPageSort")
    public ResponseEntity<MoviePageResponse> getMoviesWithPaginationAndSorting(
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
            @RequestParam(defaultValue = AppConstants.SORT_DIR, required = false) String dir
    ) {
        return ResponseEntity.ok(movieService.getAllMoviesWithPaginationAndSorting(pageNumber, pageSize, sortBy, dir));
    }

    // Thêm endpoint để thêm trailer cho phim
    @PostMapping("/add-trailer/{movieId}")
    public ResponseEntity<MovieDto> addTrailerToMovie(@PathVariable Integer movieId, @RequestParam String trailerLink) {
        return ResponseEntity.ok(movieService.addTrailer(movieId, trailerLink));
    }

    // Endpoint để thêm tập phim
    @PostMapping("/add-episode/{movieId}")
    public ResponseEntity<EpisodeDto> addEpisodeToMovie(@PathVariable Integer movieId, @RequestBody EpisodeDto episodeDto) {
        return ResponseEntity.ok(movieService.addEpisode(movieId, episodeDto));
    }

    // Endpoint để upload video cho phim
    @PostMapping("/add-video/{movieId}")
    public ResponseEntity<MovieDto> addVideoToMovie(@PathVariable Integer movieId, @RequestParam MultipartFile videoFile) throws IOException {
        // Gọi phương thức addVideo từ MovieService để lưu video
        String videoUrl = movieService.addVideo(movieId, videoFile);

        // Lấy thông tin movie sau khi video được thêm vào
        MovieDto movieDto = movieService.getMovie(movieId);

        // Cập nhật trailerLink với video URL
        movieDto.setTrailerLink(videoUrl);  // Trả về URL video

        // Trả về MovieDto đã cập nhật
        return ResponseEntity.ok(movieDto);
    }

    private MovieDto convertToMovieDto(String movieDtoStr) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(movieDtoStr, MovieDto.class);
    }
}

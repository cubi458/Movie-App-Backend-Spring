package com.movieflix.service;

import com.movieflix.dto.MovieDto;
import com.movieflix.entities.Movie;
import com.movieflix.repositories.EpisodeRepository;
import com.movieflix.repositories.MovieRepository;
import com.movieflix.exceptions.FileExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MovieServiceImplTest {

    private MovieServiceImpl movieService;
    private MovieRepository movieRepository;
    private FileService fileService;
    private EpisodeRepository episodeRepository;  // Mock EpisodeRepository
    private MultipartFile file;

    @Value("${project.poster}")
    private String path;

    @BeforeEach
    public void setUp() {
        movieRepository = mock(MovieRepository.class);
        fileService = mock(FileService.class);
        episodeRepository = mock(EpisodeRepository.class);  // Mock EpisodeRepository
        movieService = new MovieServiceImpl(movieRepository, fileService, episodeRepository);  // Truyền EpisodeRepository vào constructor
    }

    @Test
    public void testAddMovie() throws IOException {
        // Mock input data with the updated MovieDto constructor
        MovieDto movieDto = new MovieDto(
                null,                    // id
                "Movie Title",            // title
                "Original Movie Title",   // original_title
                "Overview of the movie",  // overview
                "poster.jpg",             // poster_path -> poster
                "backdrop.jpg",           // backdrop_path
                "movie",                  // media_type
                false,                    // adult
                "en",                     // original_language
                List.of(28, 12),          // genre_ids
                100.0,                    // popularity
                "2024-05-01",             // release_date
                false,                    // video
                8.5,                      // vote_average
                1234                       // vote_count
        );
        MultipartFile file = mock(MultipartFile.class);
        String uploadedFileName = "uploadedPoster.jpg";

        // Mock the value of baseUrl (ensure it is correctly set)
        String baseUrl = "http://localhost:8080";  // Mock baseUrl

        when(fileService.uploadFile(any(), eq(file))).thenReturn(uploadedFileName);
        when(movieRepository.save(any(Movie.class))).thenReturn(new Movie(1, "Movie Title", "N/A", "N/A", null, 2024, uploadedFileName, null));

        // Manually set the baseUrl in MovieServiceImpl
        movieService.setBaseUrl(baseUrl);

        // Call method
        MovieDto result = movieService.addMovie(movieDto, file);

        // Assert the result
        assertEquals("Movie Title", result.getTitle());
        assertEquals("http://localhost:8080/file/uploadedPoster.jpg", result.getPoster());  // Verify the complete URL
    }

    @Test
    public void testAddMovieWithFileExistsException() throws IOException {
        // Mock input data
        MovieDto movieDto = new MovieDto(
                null,                    // id
                "Movie Title",            // title
                "Original Movie Title",   // original_title
                "Overview of the movie",  // overview
                "poster.jpg",             // poster_path
                "backdrop.jpg",           // backdrop_path
                "movie",                  // media_type
                false,                    // adult
                "en",                     // original_language
                List.of(28, 12),          // genre_ids
                100.0,                    // popularity
                "2024-05-01",             // release_date
                false,                    // video
                8.5,                      // vote_average
                1234                       // vote_count
        );
        MultipartFile file = mock(MultipartFile.class);

        when(fileService.uploadFile(any(), eq(file))).thenThrow(new FileExistsException("File already exists"));

        // Call method and assert exception
        assertThrows(FileExistsException.class, () -> movieService.addMovie(movieDto, file));
    }
}

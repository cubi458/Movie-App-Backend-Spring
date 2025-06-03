package com.movieflix.service;

import com.movieflix.dto.EpisodeDto;
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
import com.movieflix.entities.Movie;
import com.movieflix.entities.Episode;
import com.movieflix.dto.EpisodeDto;
import com.movieflix.repositories.EpisodeRepository; // Đảm bảo import này để sử dụng repository cho Episode
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final FileService fileService;

    @Value("${project.poster}")
    private String posterPath;

    @Value("${project.video}")
    private String videoPath;

    @Value("${project.trailer}")
    private String trailerPath;

    @Value("${base.url}")
    private String baseUrl;

    private final EpisodeRepository episodeRepository;

    public MovieServiceImpl(MovieRepository movieRepository, FileService fileService, EpisodeRepository episodeRepository) {
        this.movieRepository = movieRepository;
        this.fileService = fileService;
        this.episodeRepository = episodeRepository;  // Inject EpisodeRepository
    }
    // Thêm setter cho baseUrl
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    private MovieDto mapToFlutterDto(Movie movie) {
        String posterUrl = null;
        if (movie.getPoster() != null) {
            posterUrl = baseUrl + "/file/" + movie.getPoster();
        }

        return new MovieDto(
                movie.getMovieId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getStudio(),
                movie.getReleaseYear(),
                posterUrl,
                movie.getTrailerLink(),  // YouTube trailer link
                movie.getVideo() != null && movie.getVideo(),
                movie.getVideoUrl()  // URL của video file
        );
    }


    // Thêm phương thức xử lý upload video
    @Override
    public String addVideo(Integer movieId, MultipartFile videoFile) throws IOException {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id " + movieId));

        String originalFilename = videoFile.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        String videoFileName = System.currentTimeMillis() + extension;
        
        // Save video file with new name
        Path videoPath = Paths.get(this.videoPath + File.separator + videoFileName);
        Files.copy(videoFile.getInputStream(), videoPath, StandardCopyOption.REPLACE_EXISTING);
        
        String videoUrl = baseUrl + "/video/" + videoFileName;
        
        movie.setTrailerLink(videoUrl);
        movie.setVideo(true);
        movieRepository.save(movie);

        return videoUrl;
    }


    @Override
    public MovieDto addMovie(MovieDto movieDto, MultipartFile file, MultipartFile videoFile) throws IOException {
        // Xử lý poster
        String uploadedFileName = null;
        if (file != null && !file.isEmpty()) {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            uploadedFileName = System.currentTimeMillis() + extension;
            
            Path posterFilePath = Paths.get(posterPath + File.separator + uploadedFileName);
            Files.copy(file.getInputStream(), posterFilePath, StandardCopyOption.REPLACE_EXISTING);
        }
        
        // Xử lý video và trailer riêng biệt
        String videoUrl = null;
        String trailerLink = null;
        boolean hasVideo = false;

        // Nếu có video file
        if (videoFile != null && !videoFile.isEmpty()) {
            String originalFilename = videoFile.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            String videoFileName = System.currentTimeMillis() + extension;
            
            Path videoFilePath = Paths.get(videoPath + File.separator + videoFileName);
            Files.copy(videoFile.getInputStream(), videoFilePath, StandardCopyOption.REPLACE_EXISTING);
            
            videoUrl = baseUrl + "/video/" + videoFileName;
            hasVideo = true;
        }

        // Xử lý trailer link (YouTube)
        if (movieDto.getTrailerLink() != null && !movieDto.getTrailerLink().isEmpty()) {
            String ytLink = movieDto.getTrailerLink();
            if (ytLink.contains("youtube.com/watch?v=")) {
                String videoId = ytLink.split("v=")[1];
                int ampersandPosition = videoId.indexOf('&');
                if (ampersandPosition != -1) {
                    videoId = videoId.substring(0, ampersandPosition);
                }
                trailerLink = "https://www.youtube.com/embed/" + videoId;
            } else if (ytLink.contains("youtu.be/")) {
                String videoId = ytLink.substring(ytLink.lastIndexOf("/") + 1);
                trailerLink = "https://www.youtube.com/embed/" + videoId;
            } else {
                trailerLink = ytLink; // Giữ nguyên nếu đã là embed link
            }
        }

        Movie movie = new Movie(
                null,
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                new HashSet<>(),
                movieDto.getReleaseYear(),
                uploadedFileName,
                videoUrl,  // Video URL cho file video
                hasVideo,
                trailerLink  // Thêm trailer link riêng
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
    public MovieDto updateMovie(Integer movieId, MovieDto movieDto, MultipartFile file, MultipartFile videoFile) throws IOException {
        Movie mv = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with id = " + movieId));

        String fileName = mv.getPoster();
        if (file != null && !file.isEmpty()) {
            // Delete old poster if exists with retry mechanism
            if (fileName != null) {
                Path posterFilePath = Paths.get(posterPath + File.separator + fileName);
                boolean deleted = false;
                for (int i = 0; i < 3; i++) {
                    try {
                        Files.deleteIfExists(posterFilePath);
                        deleted = true;
                        break;
                    } catch (IOException e) {
                        if (i == 2) {
                            // If this is the last attempt, log error but continue
                            System.err.println("Could not delete poster file after 3 attempts: " + e.getMessage());
                        }
                        try {
                            Thread.sleep(100); // Wait 100ms before retrying
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
                if (deleted) {
                    fileName = fileService.uploadFile(posterPath, file);
                } else {
                    // Generate new unique filename if we couldn't delete the old one
                    String originalFilename = file.getOriginalFilename();
                    String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
                    fileName = System.currentTimeMillis() + extension;
                    Path newPosterPath = Paths.get(posterPath + File.separator + fileName);
                    Files.copy(file.getInputStream(), newPosterPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                fileName = fileService.uploadFile(posterPath, file);
            }
        }

        // Convert trailer link to embed format if needed
        String trailerLink = movieDto.getTrailerLink();
        if (trailerLink != null && !trailerLink.isEmpty()) {
            if (trailerLink.contains("youtube.com/watch?v=")) {
                String videoId = trailerLink.split("v=")[1];
                int ampersandPosition = videoId.indexOf('&');
                if (ampersandPosition != -1) {
                    videoId = videoId.substring(0, ampersandPosition);
                }
                trailerLink = "https://www.youtube.com/embed/" + videoId;
            } else if (trailerLink.contains("youtu.be/")) {
                String videoId = trailerLink.substring(trailerLink.lastIndexOf("/") + 1);
                trailerLink = "https://www.youtube.com/embed/" + videoId;
            }
        }

        // Handle video file update
        String videoUrl = mv.getVideoUrl();
        boolean hasVideo = mv.getVideo();
        
        // Handle new video file upload
        if (videoFile != null && !videoFile.isEmpty()) {
            // Delete old video if exists
            if (mv.getVideoUrl() != null) {
                String oldVideoFileName = mv.getVideoUrl().substring(mv.getVideoUrl().lastIndexOf('/') + 1);
                Files.deleteIfExists(Paths.get(videoPath + File.separator + oldVideoFileName));
            }
            
            String originalFilename = videoFile.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            String videoFileName = System.currentTimeMillis() + extension;
            
            Path videoFilePath = Paths.get(videoPath + File.separator + videoFileName);
            Files.copy(videoFile.getInputStream(), videoFilePath, StandardCopyOption.REPLACE_EXISTING);
            
            videoUrl = baseUrl + "/video/" + videoFileName;
            hasVideo = true;
        }

        Movie updated = new Movie(
                movieId,                         // movieId
                movieDto.getTitle(),             // title
                movieDto.getDirector(),          // director
                movieDto.getStudio(),            // studio
                mv.getMovieCast(),              // keep existing movieCast
                movieDto.getReleaseYear(),       // releaseYear
                fileName,                        // poster
                videoUrl,                        // videoUrl (updated)
                hasVideo,                        // video flag
                trailerLink                      // trailerLink
        );

        Movie saved = movieRepository.save(updated);
        return mapToFlutterDto(saved);
    }

    @Override
    public String deleteMovie(Integer movieId) throws IOException {
        Movie mv = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with id = " + movieId));
        
        boolean hasFileErrors = false;
        StringBuilder errorMessages = new StringBuilder();
        
        // Xóa poster
        if (mv.getPoster() != null) {
            Path posterFilePath = Paths.get(posterPath + File.separator + mv.getPoster());
            try {
                if (Files.exists(posterFilePath)) {
                    // Thử xóa file 3 lần
                    for (int i = 0; i < 3; i++) {
                        try {
                            Files.deleteIfExists(posterFilePath);
                            break; // Nếu xóa thành công thì thoát loop
                        } catch (IOException e) {
                            if (i == 2) { // Nếu lần thử cuối cùng vẫn thất bại
                                hasFileErrors = true;
                                errorMessages.append("Could not delete poster file. ");
                            }
                            try {
                                Thread.sleep(100); // Đợi 100ms trước khi thử lại
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                hasFileErrors = true;
                errorMessages.append("Error accessing poster file. ");
            }
        }
        
        // Xóa video file nếu có
        if (mv.getVideoUrl() != null && mv.getVideo()) {
            try {
                String videoFileName = mv.getVideoUrl().substring(mv.getVideoUrl().lastIndexOf('/') + 1);
                Path videoFilePath = Paths.get(videoPath + File.separator + videoFileName);
                if (Files.exists(videoFilePath)) {
                    try {
                        Files.deleteIfExists(videoFilePath);
                    } catch (IOException e) {
                        hasFileErrors = true;
                        errorMessages.append("Could not delete video file. ");
                    }
                }
            } catch (Exception e) {
                hasFileErrors = true;
                errorMessages.append("Error accessing video file. ");
            }
        }
        
        // Luôn xóa record trong database, ngay cả khi có lỗi xóa file
        movieRepository.delete(mv);
        
        if (hasFileErrors) {
            return "Movie deleted from database with id = " + mv.getMovieId() + 
                   ", but there were some file deletion errors: " + errorMessages.toString();
        }
        
        return "Movie successfully deleted with id = " + mv.getMovieId();
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
    public MovieDto addTrailer(Integer movieId, String trailerLink) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id " + movieId));

        movie.setTrailerLink(trailerLink);  // Cập nhật trailer link cho movie
        movie.setVideo(true);  // Đánh dấu rằng movie này có video

        movieRepository.save(movie);  // Lưu lại Movie với trailer link và video

        return mapToFlutterDto(movie);
    }



    private String encodeUrl(String rawUrl) {
        try {
            int lastSlashIndex = rawUrl.lastIndexOf('/');
            if (lastSlashIndex == -1) {
                // Nếu URL không có dấu '/', encode toàn bộ
                return URLEncoder.encode(rawUrl, StandardCharsets.UTF_8.toString())
                        .replace("+", "%20");
            }
            String base = rawUrl.substring(0, lastSlashIndex + 1);
            String fileName = rawUrl.substring(lastSlashIndex + 1);
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replace("+", "%20");
            return base + encodedFileName;
        } catch (Exception e) {
            // Nếu lỗi thì trả về URL gốc
            return rawUrl;
        }
    }
    public EpisodeDto addEpisode(Integer movieId, EpisodeDto episodeDto) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id " + movieId));

        Episode episode = new Episode();
        episode.setEpisodeNumber(episodeDto.getEpisodeNumber());
        episode.setTitle(episodeDto.getTitle());
        episode.setTrailerLink(episodeDto.getTrailerLink());
        episode.setMovie(movie); // Set mối quan hệ với phim

        episodeRepository.save(episode); // Lưu tập phim vào cơ sở dữ liệu
        return episodeDto;  // Trả về DTO của tập phim
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
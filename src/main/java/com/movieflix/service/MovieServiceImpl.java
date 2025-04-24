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
    private String path;

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
                movie.getVideo() != null && movie.getVideo(),  // Kiểm tra video (true nếu có video)
                8.5,
                1234,
                movie.getTrailerLink()  // Lấy trailerLink từ Movie
        );
    }



    // Thêm phương thức xử lý upload video
    public String addVideo(Integer movieId, MultipartFile videoFile) throws IOException {
        // Tìm kiếm phim theo movieId
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id " + movieId));

        // Tạo thư mục lưu video (nếu chưa có)
        String videoFolderPath = "D:\\Bi\\Study\\TIEU LUAN TOT NGHIEP\\Movie-Video";  // Đường dẫn mới để lưu video
        Path videoFolder = Paths.get(videoFolderPath);
        if (!Files.exists(videoFolder)) {
            Files.createDirectories(videoFolder);  // Tạo thư mục nếu chưa có
        }

        // Lưu video vào thư mục cục bộ
        String videoFileName = videoFile.getOriginalFilename();
        Path videoPath = videoFolder.resolve(videoFileName);
        Files.copy(videoFile.getInputStream(), videoPath, StandardCopyOption.REPLACE_EXISTING);

        // Cập nhật video URL vào Movie (Bạn có thể lưu một URL tương đối hoặc tuyệt đối)
        String videoUrl = baseUrl + "/file/videos/" + videoFileName;  // Chú ý sử dụng baseUrl và thư mục videos
        movie.setTrailerLink(videoUrl);  // Cập nhật trailerLink với đường dẫn video
        movie.setVideo(true);  // Đảm bảo video được đánh dấu là có

        // Lưu Movie với video URL
        movieRepository.save(movie);  // Lưu Movie với trailerLink (video URL)

        return videoUrl;  // Trả về đường dẫn video vừa lưu
    }


    @Override
    public MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws IOException {
        if (Files.exists(Paths.get(path + File.separator + file.getOriginalFilename()))) {
            throw new FileExistsException("File already exists! Please enter another file name!");
        }
        String uploadedFileName = fileService.uploadFile(path, file);

        Movie movie = new Movie(
                null, // movieId
                movieDto.getTitle(),
                "N/A",  // director
                "N/A",  // studio
                new HashSet<>(),  // movieCast (dùng Set rỗng nếu không có giá trị)
                2024,  // releaseYear
                uploadedFileName, // poster
                null,  // trailerLink (Có thể truyền vào nếu cần)
                false  // video, mặc định là false nếu không có video
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
                "N/A",  // director
                "N/A",  // studio
                new HashSet<>(),  // movieCast (dùng Set rỗng nếu không có giá trị)
                2024,  // releaseYear
                fileName, // poster
                movieDto.getTrailerLink(),  // trailerLink, lấy từ MovieDto (hoặc giữ nguyên nếu không có)
                movieDto.isVideo() // video, nếu có giá trị trong MovieDto thì sử dụng, nếu không thì mặc định là false
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
    public MovieDto addTrailer(Integer movieId, String trailerLink) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id " + movieId));

        movie.setTrailerLink(trailerLink);  // Cập nhật trailer link cho movie
        movie.setVideo(true);  // Đánh dấu rằng movie này có video

        movieRepository.save(movie);  // Lưu lại Movie với trailer link và video

        return new MovieDto(
                movie.getMovieId(),
                movie.getTitle(),
                movie.getTitle(),
                "This is a placeholder overview.",
                baseUrl + "/file/" + movie.getPoster(),
                baseUrl + "/file/" + movie.getPoster(),
                "movie",
                false,
                "en",
                List.of(28, 12),
                100.0,
                String.valueOf(movie.getReleaseYear()),
                movie.getVideo() != null && movie.getVideo(),  // Sử dụng video (true nếu có video)
                8.5,
                1234,
                movie.getTrailerLink()
        );
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
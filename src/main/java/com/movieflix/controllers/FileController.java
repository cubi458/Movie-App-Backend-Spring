package com.movieflix.controllers;

import com.movieflix.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/file/")
@CrossOrigin(origins = "*")
public class FileController {

    private final FileService fileService;

    @Value("${project.poster}")
    private String posterPath;

    @Value("${project.video}")
    private String videoPath;

    @Value("${project.trailer}")
    private String trailerPath;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload/poster")
    public ResponseEntity<String> uploadPosterHandler(@RequestPart MultipartFile file) throws IOException {
        String uploadedFileName = fileService.uploadFile(posterPath, file);
        return ResponseEntity.ok("Poster uploaded: " + uploadedFileName);
    }

    @PostMapping("/upload/video")
    public ResponseEntity<String> uploadVideoHandler(@RequestPart MultipartFile file) throws IOException {
        String uploadedFileName = fileService.uploadFile(videoPath, file);
        return ResponseEntity.ok("Video uploaded: " + uploadedFileName);
    }

    @PostMapping("/upload/trailer")
    public ResponseEntity<String> uploadTrailerHandler(@RequestPart MultipartFile file) throws IOException {
        String uploadedFileName = fileService.uploadFile(trailerPath, file);
        return ResponseEntity.ok("Trailer uploaded: " + uploadedFileName);
    }

    @GetMapping(value = "/{fileName}")
    public void serveFileHandler(@PathVariable String fileName, HttpServletResponse response) throws IOException {
        String fileExtension = getFileExtension(fileName);
        String path;

        // Xác định đường dẫn dựa trên định dạng file
        if (isVideoFormat(fileExtension)) {
            path = videoPath;
            response.setContentType("video/mp4");
        } else if (isImageFormat(fileExtension)) {
            path = posterPath;
            response.setContentType(MediaType.IMAGE_PNG_VALUE);
        } else {
            response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        }

        InputStream resourceFile = fileService.getResourceFile(path, fileName);
        StreamUtils.copy(resourceFile, response.getOutputStream());
    }

    private boolean isVideoFormat(String extension) {
        return extension.equalsIgnoreCase("mp4") || 
               extension.equalsIgnoreCase("mkv") || 
               extension.equalsIgnoreCase("avi");
    }

    private boolean isImageFormat(String extension) {
        return extension.equalsIgnoreCase("jpg") || 
               extension.equalsIgnoreCase("png") || 
               extension.equalsIgnoreCase("jpeg");
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }
}

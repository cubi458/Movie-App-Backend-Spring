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

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @Value("${project.poster}")
    private String path;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFileHandler(@RequestPart MultipartFile file) throws IOException {
        String uploadedFileName = fileService.uploadFile(path, file);
        return ResponseEntity.ok("File uploaded : " + uploadedFileName);
    }

    @GetMapping(value = "/{fileName}")
    public void serveFileHandler(@PathVariable String fileName, HttpServletResponse response) throws IOException {
        InputStream resourceFile = fileService.getResourceFile(path, fileName);

        // Kiểm tra định dạng file (video hay hình ảnh)
        String fileExtension = getFileExtension(fileName);

        // Kiểm tra định dạng video
        if (fileExtension.equalsIgnoreCase("mp4") || fileExtension.equalsIgnoreCase("mkv") || fileExtension.equalsIgnoreCase("avi")) {
            // Định dạng video
            response.setContentType("video/mp4");  // Hoặc "video/x-matroska" cho MKV
        } else if (fileExtension.equalsIgnoreCase("jpg") || fileExtension.equalsIgnoreCase("png") || fileExtension.equalsIgnoreCase("jpeg")) {
            // Định dạng hình ảnh
            response.setContentType(MediaType.IMAGE_PNG_VALUE);
        } else {
            // Nếu không phải định dạng video hay hình ảnh, trả về mã lỗi
            response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        }

        // Truyền file tới response
        StreamUtils.copy(resourceFile, response.getOutputStream());
    }

    // Hàm để lấy phần mở rộng của file
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }
}

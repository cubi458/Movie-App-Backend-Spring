package com.movieflix.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileServiceImpl implements FileService {

    @Override
    public String uploadFile(String path, MultipartFile file) throws IOException {
        // Lấy tên file
        String fileName = file.getOriginalFilename();

        // Tạo đường dẫn file sử dụng Paths.get() thay vì File.separator
        Path filePath = Paths.get(path, fileName);

        // Kiểm tra và tạo thư mục nếu chưa có (sử dụng mkdirs() để tạo tất cả thư mục cha nếu cần)
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs(); // tạo thư mục và tất cả thư mục cha nếu chưa có
        }

        // Sao chép file từ MultipartFile vào đường dẫn
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return fileName; // Trả về tên file vừa upload
    }

    @Override
    public InputStream getResourceFile(String path, String fileName) throws FileNotFoundException {
        // Tạo đường dẫn file bằng Paths.get() để đảm bảo tính tương thích
        Path filePath = Paths.get(path, fileName);
        return new FileInputStream(filePath.toFile()); // Trả về InputStream từ file
    }
}


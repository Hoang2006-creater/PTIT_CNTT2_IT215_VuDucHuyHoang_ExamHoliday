package com.re.examholiday.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface CloudinaryService {
    String uploadFile(MultipartFile file) throws IOException;
    void deleteFile(String publicId) throws IOException;
    String getPublicId(String imageUrl);
}

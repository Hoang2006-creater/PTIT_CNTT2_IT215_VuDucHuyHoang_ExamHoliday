package com.re.examholiday.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.re.examholiday.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", "rms_menu_items"
        ));
        return (String) uploadResult.get("secure_url");
    }

    @Override
    public void deleteFile(String publicId) throws IOException {
        if (publicId == null || publicId.isEmpty()) {
            return;
        }
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    @Override
    public String getPublicId(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        try {
            int uploadIndex = imageUrl.indexOf("/upload/");
            if (uploadIndex == -1) return null;
            
            String pathAfterUpload = imageUrl.substring(uploadIndex + 8); // Skip "/upload/"
            if (pathAfterUpload.matches("v\\d+/.*")) {
                int firstSlash = pathAfterUpload.indexOf('/');
                if (firstSlash != -1) {
                    pathAfterUpload = pathAfterUpload.substring(firstSlash + 1);
                }
            }
            int dotIndex = pathAfterUpload.lastIndexOf('.');
            if (dotIndex != -1) {
                pathAfterUpload = pathAfterUpload.substring(0, dotIndex);
            }
            return pathAfterUpload;
        } catch (Exception e) {
            return null;
        }
    }
}

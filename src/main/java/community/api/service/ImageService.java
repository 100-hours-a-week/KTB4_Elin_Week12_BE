package community.api.service;

import community.api.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageService {

    private final Path uploadPath = Paths.get("uploads")
            .toAbsolutePath()
            .normalize();

    public String uploadImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new BusinessException("image_required", HttpStatus.BAD_REQUEST);
        }

        String contentType = image.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("invalid_image_file", HttpStatus.BAD_REQUEST);
        }

        String extension = getExtension(contentType);
        String savedFileName = UUID.randomUUID() + extension;

        try {
            Files.createDirectories(uploadPath);

            Path targetPath = uploadPath.resolve(savedFileName);
            image.transferTo(targetPath.toFile());

            return "/uploads/" + savedFileName;
        } catch (IOException e) {
            throw new BusinessException("image_upload_failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String getExtension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            default -> throw new BusinessException("unsupported_image_type", HttpStatus.BAD_REQUEST);
        };
    }
}
package community.api.controller;

import community.api.response.ApiResponse;
import community.api.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadImage(
            @RequestPart("image") MultipartFile image
    ) {
        String imageUrl = imageService.uploadImage(image);

        return ResponseEntity.ok(
                ApiResponse.of("image_upload_success", imageUrl)
        );
    }
}

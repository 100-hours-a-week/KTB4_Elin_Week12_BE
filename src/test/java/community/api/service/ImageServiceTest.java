package community.api.service;

import community.api.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @InjectMocks
    private ImageService imageService;

    @Mock
    private MultipartFile file;

    @Test
    @DisplayName("빈 파일을 업로드하면 예외가 발생한다")
    void uploadImage_EmptyFile_ThrowException() {

        when(file.isEmpty()).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> imageService.uploadImage(file)
        );

        assertEquals("image_required", exception.getCode());
    }

    @Test
    @DisplayName("이미지가 아닌 파일을 업로드하면 예외가 발생한다")
    void uploadImage_NotImageContentType_ThrowException() {

        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("text/plain");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> imageService.uploadImage(file)
        );

        assertEquals("invalid_image_file", exception.getCode());
    }

    @Test
    @DisplayName("jpg나 png가 아닌 파일을 업로드하면 예외가 발생한다")
    void uploadImage_UnsupportedImageType_ThrowException() {

        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/gif");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> imageService.uploadImage(file)
        );

        assertEquals("unsupported_image_type", exception.getCode());
    }
}
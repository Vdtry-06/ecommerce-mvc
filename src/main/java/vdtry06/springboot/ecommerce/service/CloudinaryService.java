package vdtry06.springboot.ecommerce.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vdtry06.springboot.ecommerce.exception.AppException;
import vdtry06.springboot.ecommerce.exception.ErrorCode;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Log4j2
public class CloudinaryService {
    Cloudinary cloudinary;

    public String uploadFile(MultipartFile file, String folder) {

        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_FILE);
        }
        try {
            Map options = ObjectUtils.asMap(
                    "folder", folder
            );
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            log.error("Failed to read file bytes", e);
            throw new AppException(ErrorCode.INVALID_FILE);
        } catch (Exception e) {
            log.error("File upload failed", e);
            throw new AppException(ErrorCode.UPLOAD_FAILED);
        }
    }


    public void deleteFile(String imageUrl) {
        try {
            String publicId = extractPublicId(imageUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Deleted file: " + publicId);
        } catch (Exception e) {
            log.error("Failed to delete file", e);
            throw new AppException(ErrorCode.DELETE_FAILED);
        }
    }

    private String extractPublicId(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("/upload/")) {
            throw new AppException(ErrorCode.INVALID_FILE);
        }

        try {
            String publicIdWithExtension = imageUrl.substring(imageUrl.indexOf("/upload/") + 8);
            String[] parts = publicIdWithExtension.split("/");
            String publicId = parts[parts.length - 1].replaceAll("\\.[^.]+$", "");
            return String.join("/", Arrays.copyOf(parts, parts.length - 1)) + "/" + publicId;
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_FILE);
        }
    }
}

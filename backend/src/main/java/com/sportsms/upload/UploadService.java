package com.sportsms.upload;

import com.sportsms.common.NotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadService {
    private final Path uploadDir;
    private final String baseUrl;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/png", "image/jpeg", "image/webp");

    public UploadService(@Value("${app.upload.directory}") String uploadDir,
                         @Value("${app.upload.base-url}") String baseUrl) {
        this.uploadDir = Path.of(uploadDir);
        this.baseUrl = baseUrl;
    }

    public UploadResponse upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Invalid file type");
        }
        try {
            Files.createDirectories(uploadDir);
            String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String safeName = UUID.randomUUID().toString();
            if (ext != null && !ext.isBlank()) {
                safeName = safeName + "." + ext;
            }
            Path target = uploadDir.resolve(safeName);
            file.transferTo(target);
            return new UploadResponse(baseUrl + "/" + safeName, safeName);
        } catch (IOException ex) {
            throw new NotFoundException("Unable to store file");
        }
    }

    public record UploadResponse(String url, String filename) {}
}

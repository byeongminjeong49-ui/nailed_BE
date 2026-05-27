package com.nailed.web.member.service;

import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ProfileImageStorageService {

    private static final String PROFILE_IMAGE_DIR = "src/main/resources/static/images/profileImg";
    private static final String PROFILE_IMAGE_URL_PREFIX = "/images/profileImg/";
    private static final DateTimeFormatter FILE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "webp");

    public String store(String memberId, MultipartFile file) {
        validateFile(file);

        String extension = extractExtension(file.getOriginalFilename());
        Path directory = Paths.get(PROFILE_IMAGE_DIR);
        String savedFileName = createUniqueFileName(directory, memberId, extension);
        Path targetPath = directory.resolve(savedFileName);

        try {
            Files.createDirectories(directory);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return PROFILE_IMAGE_URL_PREFIX + savedFileName;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String extension = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private String extractExtension(String originalFileName) {
        if (originalFileName == null || !originalFileName.contains(".")) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private String createUniqueFileName(Path directory, String memberId, String extension) {
        String safeMemberId = memberId.replaceAll("[^a-zA-Z0-9_-]", "_");
        String timestamp = LocalDateTime.now().format(FILE_TIME_FORMATTER);
        String baseName = safeMemberId + "_" + timestamp;
        String fileName = baseName + "." + extension;

        int sequence = 1;
        while (Files.exists(directory.resolve(fileName))) {
            fileName = baseName + "_" + sequence + "." + extension;
            sequence++;
        }
        return fileName;
    }
}

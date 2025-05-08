package edu.ntu.ai.exam.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    private final Path rootLocation;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        initializeStorage();
    }

    private void initializeStorage() {
        try {
            Files.createDirectories(rootLocation);
            logger.info("文件存储目录初始化完成：{}", rootLocation);
        } catch (IOException e) {
            logger.error("无法创建文件存储目录", e);
            throw new StorageException("无法初始化文件存储", e);
        }
    }

    public String storeFile(MultipartFile file, Long questionId, String prefix) {
        validateFile(file);
        final String filename = generateFilename(questionId, prefix, file);
        final Path targetPath = rootLocation.resolve(filename);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            logger.debug("文件存储成功：{}", filename);
            return filename;
        } catch (IOException e) {
            logger.error("文件存储失败：{}", filename, e);
            throw new StorageException("文件存储失败: " + filename, e);
        }
    }

    public void deleteFile(String filename) {
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        final Path filePath = rootLocation.resolve(filename).normalize();
        try {
            Files.deleteIfExists(filePath);
            logger.info("文件删除成功：{}", filename);
        } catch (IOException e) {
            logger.error("文件删除失败：{}", filename, e);
            throw new StorageException("文件删除失败: " + filename, e);
        }
    }
    public  void deleteFilesByQuestionId(Long questionId) {
        try {
            // 获取存储目录路径
            Path directory = rootLocation;
            // 列出所有以题目ID开头的文件
            Files.list(directory)
                    .filter(path -> path.getFileName().toString().startsWith(questionId + "-"))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            logger.info("删除题目相关文件: {}", path);
                        } catch (IOException e) {
                            logger.error("文件删除失败: {}", path, e);
                        }
                    });
        } catch (IOException e) {
            logger.error("无法列出题目 {} 的文件", questionId, e);
            throw new StorageException("无法删除关联文件", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new StorageException("上传文件不能为空");
        }

        if (!file.getContentType().startsWith("image/")) {
            throw new StorageException("仅支持图片文件格式");
        }
    }

    private String generateFilename(Long questionId, String prefix, MultipartFile file) {
        return String.format("%d-%s-%s%s",
                questionId,
                prefix,
                UUID.randomUUID(),
                getFileExtension(file));
    }

    private String getFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return ".jpg";
    }

    public static class StorageException extends RuntimeException {
        public StorageException(String message) {
            super(message);
        }

        public StorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}


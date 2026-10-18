package com.example.payroll_service.feature.task.service;

import com.example.payroll_service.common.exception.BusinessException;
import com.example.payroll_service.common.response.DomainCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskEvidenceStorage {
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final int MAX_FILE_COUNT = 5;
    private static final String EVIDENCE_PATH = "/api/tasks/evidence/";

    @Value("${task.evidence.storage-dir:./uploads/task-evidence}")
    private String storageDirectory;

    public List<String> store(List<MultipartFile> files, String publicBaseUrl) {
        if (files == null || files.isEmpty()) return List.of();
        List<MultipartFile> uploads = files.stream().filter(file -> file != null && !file.isEmpty()).toList();
        if (uploads.size() > MAX_FILE_COUNT) {
            throw new BusinessException(DomainCode.INVALID_PARAMETER, "mỗi lần nộp tối đa 5 file minh chứng");
        }
        Path root = root();
        try {
            Files.createDirectories(root);
            List<String> links = new ArrayList<>(uploads.size());
            for (MultipartFile upload : uploads) {
                if (upload.getSize() > MAX_FILE_SIZE) {
                    throw new BusinessException(DomainCode.INVALID_PARAMETER, "mỗi file minh chứng không được vượt quá 10 MB");
                }
                String originalName = upload.getOriginalFilename() == null ? "evidence" : upload.getOriginalFilename();
                String safeName = originalName.replaceAll("[^A-Za-z0-9._-]", "_").replaceAll("^\\.+", "");
                if (safeName.isBlank()) safeName = "evidence";
                if (safeName.length() > 180) safeName = safeName.substring(0, 180);
                String fileKey = UUID.randomUUID() + "_" + safeName;
                Path target = root.resolve(fileKey).normalize();
                if (!target.startsWith(root)) {
                    throw new BusinessException(DomainCode.INVALID_PARAMETER, "tên file minh chứng không hợp lệ");
                }
                try (var input = upload.getInputStream()) {
                    Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
                }
                links.add(publicBaseUrl + EVIDENCE_PATH + fileKey);
            }
            return List.copyOf(links);
        } catch (IOException exception) {
            throw new IllegalStateException("Không thể lưu file minh chứng task", exception);
        }
    }

    public DownloadableEvidence load(String fileKey) {
        if (fileKey == null || !fileKey.matches("[0-9a-fA-F-]{36}_[A-Za-z0-9._-]{1,255}")) {
            throw new BusinessException(DomainCode.NOT_FOUND, "file minh chứng task");
        }
        Path root = root();
        Path file = root.resolve(fileKey).normalize();
        if (!file.startsWith(root) || !Files.isRegularFile(file)) {
            throw new BusinessException(DomainCode.NOT_FOUND, "file minh chứng task");
        }
        String contentType = contentType(fileKey);
        return new DownloadableEvidence(new FileSystemResource(file), fileKey.substring(fileKey.indexOf('_') + 1), contentType);
    }

    private Path root() {
        return Path.of(storageDirectory).toAbsolutePath().normalize();
    }

    private String contentType(String fileKey) {
        String lower = fileKey.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".bmp")) return "image/bmp";
        try {
            return Files.probeContentType(root().resolve(fileKey));
        } catch (IOException exception) {
            return "application/octet-stream";
        }
    }

    public record DownloadableEvidence(Resource resource, String fileName, String contentType) {
        public boolean isImage() {
            return "image/png".equals(contentType) || "image/jpeg".equals(contentType)
                    || "image/gif".equals(contentType) || "image/webp".equals(contentType)
                    || "image/bmp".equals(contentType);
        }
    }
}

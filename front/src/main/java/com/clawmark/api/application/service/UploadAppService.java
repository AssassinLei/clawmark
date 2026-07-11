package com.clawmark.api.application.service;

import com.clawmark.api.common.constant.BizCode;
import com.clawmark.api.common.exception.BizException;
import com.clawmark.api.common.util.IdGenerator;
import com.clawmark.api.config.UploadProperties;
import com.clawmark.api.interfaces.vo.upload.UploadFileVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class UploadAppService {

    private static final long MAX_ORIGINAL_SIZE = 30L * 1024L * 1024L;
    private static final long MAX_THUMBNAIL_SIZE = 200L * 1024L;

    @Resource
    private UploadProperties uploadProperties;

    public UploadFileVO uploadLocal(String userId, MultipartFile file, String imageType, String requestBaseUrl) {
        if (file == null || file.isEmpty()) {
            throw new BizException(BizCode.BAD_REQUEST, "file不能为空");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.startsWith("image/")) {
            throw new BizException(BizCode.BAD_REQUEST, "仅支持图片上传");
        }

        String finalImageType = normalizeImageType(imageType);
        long fileSize = file.getSize();
        if ("original".equals(finalImageType) && fileSize > MAX_ORIGINAL_SIZE) {
            throw new BizException(BizCode.BAD_REQUEST, "原图大小不能超过30MB");
        }
        if ("thumbnail".equals(finalImageType) && fileSize > MAX_THUMBNAIL_SIZE) {
            throw new BizException(BizCode.BAD_REQUEST, "缩略图大小不能超过200KB");
        }

        LocalDateTime now = LocalDateTime.now();
        String extension = detectExtension(file.getOriginalFilename(), contentType);
        String relativePath = String.format(Locale.ROOT,
                "photos/%s/%d/%02d/%s_%s.%s",
                userId,
                now.getYear(),
                now.getMonthValue(),
                finalImageType,
                IdGenerator.nextId("img"),
                extension);

        Path target = Paths.get(uploadProperties.getLocalDir()).toAbsolutePath().normalize().resolve(relativePath);
        try {
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            file.transferTo(target.toFile());
        } catch (IOException ex) {
            throw new BizException(BizCode.INTERNAL_ERROR, "保存文件失败");
        }

        String publicPrefix = normalizePublicPathPrefix(uploadProperties.getPublicPathPrefix());
        String baseUrl = trimTail(StringUtils.hasText(requestBaseUrl) ? requestBaseUrl : uploadProperties.getPublicBaseUrl());
        String fileUrl = baseUrl + publicPrefix + "/" + relativePath.replace('\\', '/');

        UploadFileVO vo = new UploadFileVO();
        vo.setFileUrl(fileUrl);
        vo.setImageType(finalImageType);
        vo.setFileSize(fileSize);
        return vo;
    }

    private String normalizeImageType(String imageType) {
        String normalized = StringUtils.hasText(imageType) ? imageType.trim().toLowerCase(Locale.ROOT) : "original";
        if (!"original".equals(normalized) && !"thumbnail".equals(normalized)) {
            throw new BizException(BizCode.BAD_REQUEST, "image_type仅支持original|thumbnail");
        }
        return normalized;
    }

    private String detectExtension(String originalName, String contentType) {
        if (StringUtils.hasText(originalName) && originalName.contains(".")) {
            String ext = originalName.substring(originalName.lastIndexOf('.') + 1).trim().toLowerCase(Locale.ROOT);
            if (StringUtils.hasText(ext)) {
                return ext;
            }
        }
        if ("image/png".equals(contentType)) {
            return "png";
        }
        if ("image/webp".equals(contentType)) {
            return "webp";
        }
        if ("image/gif".equals(contentType)) {
            return "gif";
        }
        return "jpg";
    }

    private String normalizePublicPathPrefix(String prefix) {
        String value = StringUtils.hasText(prefix) ? prefix.trim() : "/uploads";
        if (!value.startsWith("/")) {
            value = "/" + value;
        }
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private String trimTail(String text) {
        String value = StringUtils.hasText(text) ? text.trim() : "http://localhost:8080";
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}

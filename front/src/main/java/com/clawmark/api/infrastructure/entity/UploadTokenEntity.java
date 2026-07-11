package com.clawmark.api.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("upload_tokens")
public class UploadTokenEntity {

    @TableId(type = IdType.INPUT)
    private String id;

    private String userId;

    private String coupleId;

    private String fileType;

    private Long fileSize;

    private String imageType;

    private String uploadKey;

    private String cdnUrl;

    private LocalDateTime expiresAt;

    private LocalDateTime usedAt;

    private LocalDateTime createdAt;
}

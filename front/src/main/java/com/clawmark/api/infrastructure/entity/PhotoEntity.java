package com.clawmark.api.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("photos")
public class PhotoEntity {

    @TableId(type = IdType.INPUT)
    private String id;

    private String coupleId;

    private String uploaderId;

    private String originalUrl;

    private String thumbnailUrl;

    private String cityCode;

    private String cityName;

    private LocalDate shotDate;

    private Integer width;

    private Integer height;

    private Integer isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}

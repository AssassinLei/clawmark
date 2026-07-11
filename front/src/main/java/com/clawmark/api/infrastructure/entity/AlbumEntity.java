package com.clawmark.api.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("albums")
public class AlbumEntity {

    @TableId(type = IdType.INPUT)
    private String id;

    private String coupleId;

    private String type;

    private String title;

    private String description;

    private String cityCode;

    private String coverThumbnailUrl;

    private String createdBy;

    private Integer photoCount;

    private Integer isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}

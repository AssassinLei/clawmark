package com.clawmark.api.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("album_photos")
public class AlbumPhotoEntity {

    private String albumId;

    private String photoId;

    private String addedBy;

    private LocalDateTime createdAt;
}

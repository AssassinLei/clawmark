package com.clawmark.api.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clawmark.api.infrastructure.entity.AlbumPhotoEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AlbumPhotoMapper extends BaseMapper<AlbumPhotoEntity> {
}

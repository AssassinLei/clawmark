package com.clawmark.api.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clawmark.api.infrastructure.entity.UploadTokenEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UploadTokenMapper extends BaseMapper<UploadTokenEntity> {
}

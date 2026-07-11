package com.clawmark.api.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clawmark.api.infrastructure.entity.TodoUrgeLogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TodoUrgeLogMapper extends BaseMapper<TodoUrgeLogEntity> {
}

package com.clawmark.api.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("city_dict")
public class CityDictEntity {

    @TableId
    private String cityCode;

    private String cityName;

    private String province;

    private String countryCode;

    private Integer enabled;
}

package com.clawmark.api.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clawmark.api.infrastructure.entity.CityDictEntity;
import com.clawmark.api.infrastructure.entity.PhotoEntity;
import com.clawmark.api.infrastructure.mapper.CityDictMapper;
import com.clawmark.api.infrastructure.mapper.PhotoMapper;
import com.clawmark.api.interfaces.vo.map.CityLightListVO;
import com.clawmark.api.interfaces.vo.map.CityLightVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MapAppService {

    @Resource
    private CoupleAccessService coupleAccessService;

    @Resource
    private PhotoMapper photoMapper;

    @Resource
    private CityDictMapper cityDictMapper;

    public CityLightListVO cities(String coupleId, String userId) {
        coupleAccessService.checkAndGet(coupleId, userId);

        List<PhotoEntity> photos = photoMapper.selectList(new LambdaQueryWrapper<PhotoEntity>()
                .eq(PhotoEntity::getCoupleId, coupleId)
                .eq(PhotoEntity::getIsDeleted, 0));

        Map<String, CityLightVO> cityMap = new HashMap<String, CityLightVO>();
        for (PhotoEntity photo : photos) {
            CityLightVO vo = cityMap.get(photo.getCityCode());
            if (vo == null) {
                vo = new CityLightVO();
                vo.setCityCode(photo.getCityCode());
                vo.setCityName(photo.getCityName());
                vo.setPhotoCount(0);
                vo.setCoverThumbnail(photo.getThumbnailUrl());
                vo.setLatestDate(photo.getShotDate() == null ? null : photo.getShotDate().toString());
                cityMap.put(photo.getCityCode(), vo);
            }
            vo.setPhotoCount(vo.getPhotoCount() + 1);
            LocalDate currentLatest = vo.getLatestDate() == null ? null : LocalDate.parse(vo.getLatestDate());
            if (photo.getShotDate() != null && (currentLatest == null || photo.getShotDate().isAfter(currentLatest))) {
                vo.setLatestDate(photo.getShotDate().toString());
                vo.setCoverThumbnail(photo.getThumbnailUrl());
            }
        }

        for (CityLightVO city : cityMap.values()) {
            CityDictEntity cityDict = cityDictMapper.selectById(city.getCityCode());
            city.setProvince(cityDict == null ? "" : cityDict.getProvince());
        }

        CityLightListVO result = new CityLightListVO();
        result.setCities(new ArrayList<CityLightVO>(cityMap.values()));
        result.setTotalCities(result.getCities().size());
        return result;
    }
}

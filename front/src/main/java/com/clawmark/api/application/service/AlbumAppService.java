package com.clawmark.api.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clawmark.api.common.constant.BizCode;
import com.clawmark.api.common.exception.BizException;
import com.clawmark.api.common.util.IdGenerator;
import com.clawmark.api.common.util.TimeUtil;
import com.clawmark.api.infrastructure.entity.AlbumEntity;
import com.clawmark.api.infrastructure.entity.AlbumPhotoEntity;
import com.clawmark.api.infrastructure.entity.CityDictEntity;
import com.clawmark.api.infrastructure.entity.PhotoCommentEntity;
import com.clawmark.api.infrastructure.entity.PhotoEntity;
import com.clawmark.api.infrastructure.mapper.AlbumMapper;
import com.clawmark.api.infrastructure.mapper.AlbumPhotoMapper;
import com.clawmark.api.infrastructure.mapper.CityDictMapper;
import com.clawmark.api.infrastructure.mapper.PhotoCommentMapper;
import com.clawmark.api.infrastructure.mapper.PhotoMapper;
import com.clawmark.api.interfaces.dto.albums.CreateAlbumRequest;
import com.clawmark.api.interfaces.vo.albums.AlbumItemVO;
import com.clawmark.api.interfaces.vo.albums.AlbumListVO;
import com.clawmark.api.interfaces.vo.albums.CreateAlbumVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AlbumAppService {

    @Resource
    private CoupleAccessService coupleAccessService;

    @Resource
    private AlbumMapper albumMapper;

    @Resource
    private AlbumPhotoMapper albumPhotoMapper;

    @Resource
    private CityDictMapper cityDictMapper;

    @Resource
    private PhotoMapper photoMapper;

    @Resource
    private PhotoCommentMapper photoCommentMapper;



    public AlbumListVO list(String coupleId, String userId, String type, Integer page, Integer pageSize) {
        coupleAccessService.checkAndGet(coupleId, userId);

        String finalType = StringUtils.hasText(type) ? type : "all";
        if (!"all".equals(finalType) && !"custom".equals(finalType)) {
            throw new BizException(BizCode.BAD_REQUEST, "type仅支持custom|all");
        }

        int finalPage = page == null || page < 1 ? 1 : page;
        int finalPageSize = pageSize == null ? 20 : pageSize;
        if (finalPageSize < 1 || finalPageSize > 50) {
            throw new BizException(BizCode.BAD_REQUEST, "page_size范围应为1-50");
        }

        LambdaQueryWrapper<AlbumEntity> query = new LambdaQueryWrapper<AlbumEntity>()
                .eq(AlbumEntity::getCoupleId, coupleId)
                .eq(AlbumEntity::getIsDeleted, 0)
                .orderByDesc(AlbumEntity::getCreatedAt);
        if (!"all".equals(finalType)) {
            query.eq(AlbumEntity::getType, finalType);
        }

        Page<AlbumEntity> dataPage = albumMapper.selectPage(new Page<AlbumEntity>(finalPage, finalPageSize), query);

        Set<String> cityCodes = new HashSet<String>();
        for (AlbumEntity album : dataPage.getRecords()) {
            if (StringUtils.hasText(album.getCityCode())) {
                cityCodes.add(album.getCityCode());
            }
        }

        Map<String, String> cityNameByCode = new HashMap<String, String>();
        if (!cityCodes.isEmpty()) {
            List<CityDictEntity> cityDictList = cityDictMapper.selectList(new LambdaQueryWrapper<CityDictEntity>()
                    .in(CityDictEntity::getCityCode, cityCodes)
                    .eq(CityDictEntity::getEnabled, 1));
            for (CityDictEntity city : cityDictList) {
                cityNameByCode.put(city.getCityCode(), city.getCityName());
            }
        }

        List<AlbumItemVO> items = new ArrayList<AlbumItemVO>();
        for (AlbumEntity album : dataPage.getRecords()) {
            AlbumItemVO item = new AlbumItemVO();
            item.setAlbumId(album.getId());
            item.setType(album.getType());
            item.setTitle(album.getTitle());
            item.setCityCode(album.getCityCode());
            item.setCityName(cityNameByCode.get(album.getCityCode()));
            item.setCoverThumbnail(album.getCoverThumbnailUrl());
            item.setPhotoCount(album.getPhotoCount() == null ? 0 : album.getPhotoCount());
            item.setCreatedAt(TimeUtil.toIsoUtc(album.getCreatedAt()));
            items.add(item);
        }

        AlbumListVO vo = new AlbumListVO();
        vo.setAlbums(items);
        vo.setTotal(dataPage.getTotal());
        vo.setPage(finalPage);
        vo.setPageSize(finalPageSize);
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public CreateAlbumVO createCustom(String coupleId, String userId, CreateAlbumRequest request) {
        coupleAccessService.checkAndGet(coupleId, userId);

        String title = request.getTitle().trim();
        String description = StringUtils.hasText(request.getDescription()) ? request.getDescription().trim() : null;
        String coverPhotoUrl = StringUtils.hasText(request.getCoverPhotoUrl()) ? request.getCoverPhotoUrl().trim() : null;
        String cityName = request.getCityName().trim();

        CityDictEntity city = cityDictMapper.selectOne(new LambdaQueryWrapper<CityDictEntity>()
                .eq(CityDictEntity::getCityName, cityName)
                .eq(CityDictEntity::getEnabled, 1)
                .last("limit 1"));
        if (city == null || !StringUtils.hasText(city.getCityCode())) {
            throw new BizException(BizCode.BAD_REQUEST, "城市不存在或未启用: " + cityName);
        }

        String cityCode = city.getCityCode().trim();
        LocalDateTime now = LocalDateTime.now();

        AlbumEntity album = new AlbumEntity();
        album.setId(IdGenerator.nextId("alb"));
        album.setCoupleId(coupleId);
        album.setType("custom");
        album.setTitle(title);
        album.setDescription(description);
        album.setCityCode(cityCode);
        album.setCoverThumbnailUrl(coverPhotoUrl);
        album.setCreatedBy(userId);
        album.setPhotoCount(0);
        album.setIsDeleted(0);
        album.setCreatedAt(now);
        album.setUpdatedAt(now);
        albumMapper.insert(album);

        return toCreateAlbumVO(album);
    }

    private CreateAlbumVO toCreateAlbumVO(AlbumEntity album) {
        CreateAlbumVO vo = new CreateAlbumVO();
        vo.setAlbumId(album.getId());
        vo.setType(album.getType());
        vo.setTitle(album.getTitle());
        vo.setDescription(album.getDescription());
        vo.setPhotoCount(album.getPhotoCount() == null ? 0 : album.getPhotoCount());
        vo.setCreatedAt(TimeUtil.toIsoUtc(album.getCreatedAt()));
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(String coupleId, String userId, String albumId) {
        coupleAccessService.checkAndGet(coupleId, userId);

        AlbumEntity album = albumMapper.selectOne(new LambdaQueryWrapper<AlbumEntity>()
                .eq(AlbumEntity::getId, albumId)
                .eq(AlbumEntity::getCoupleId, coupleId)
                .eq(AlbumEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (album == null) {
            throw new BizException(BizCode.NOT_FOUND, "资源不存在");
        }

        List<AlbumPhotoEntity> relations = albumPhotoMapper.selectList(new LambdaQueryWrapper<AlbumPhotoEntity>()
                .eq(AlbumPhotoEntity::getAlbumId, albumId));
        Set<String> photoIds = new HashSet<String>();
        for (AlbumPhotoEntity relation : relations) {
            if (StringUtils.hasText(relation.getPhotoId())) {
                photoIds.add(relation.getPhotoId());
            }
        }

        LocalDateTime now = LocalDateTime.now();
        albumMapper.update(null, new LambdaUpdateWrapper<AlbumEntity>()
                .eq(AlbumEntity::getId, albumId)
                .set(AlbumEntity::getIsDeleted, 1)
                .set(AlbumEntity::getDeletedAt, now)
                .set(AlbumEntity::getUpdatedAt, now));

        albumPhotoMapper.delete(new LambdaQueryWrapper<AlbumPhotoEntity>()
                .eq(AlbumPhotoEntity::getAlbumId, albumId));

        if (photoIds.isEmpty()) {
            return;
        }

        Set<String> orphanPhotoIds = new HashSet<String>();
        for (String photoId : photoIds) {
            Long left = albumPhotoMapper.selectCount(new LambdaQueryWrapper<AlbumPhotoEntity>()
                    .eq(AlbumPhotoEntity::getPhotoId, photoId));
            if (left == null || left == 0L) {
                orphanPhotoIds.add(photoId);
            }
        }

        if (orphanPhotoIds.isEmpty()) {
            return;
        }

        photoMapper.update(null, new LambdaUpdateWrapper<PhotoEntity>()
                .in(PhotoEntity::getId, orphanPhotoIds)
                .eq(PhotoEntity::getCoupleId, coupleId)
                .eq(PhotoEntity::getIsDeleted, 0)
                .set(PhotoEntity::getIsDeleted, 1)
                .set(PhotoEntity::getDeletedAt, now)
                .set(PhotoEntity::getUpdatedAt, now));

        photoCommentMapper.update(null, new LambdaUpdateWrapper<PhotoCommentEntity>()
                .in(PhotoCommentEntity::getPhotoId, orphanPhotoIds)
                .eq(PhotoCommentEntity::getCoupleId, coupleId)
                .eq(PhotoCommentEntity::getIsDeleted, 0)
                .set(PhotoCommentEntity::getIsDeleted, 1)
                .set(PhotoCommentEntity::getDeletedAt, now)
                .set(PhotoCommentEntity::getUpdatedAt, now));
    }
}
package com.clawmark.api.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import com.clawmark.api.interfaces.dto.photos.CreatePhotoBatchItemRequest;
import com.clawmark.api.interfaces.dto.photos.CreatePhotoBatchRequest;
import com.clawmark.api.interfaces.dto.photos.CreatePhotoRequest;
import com.clawmark.api.interfaces.vo.photos.CreatePhotoVO;
import com.clawmark.api.interfaces.vo.photos.PhotoItemVO;
import com.clawmark.api.interfaces.vo.photos.PhotoListVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PhotoAppService {

    @Resource
    private CoupleAccessService coupleAccessService;

    @Resource
    private AlbumMapper albumMapper;

    @Resource
    private PhotoMapper photoMapper;

    @Resource
    private AlbumPhotoMapper albumPhotoMapper;

    @Resource
    private PhotoCommentMapper photoCommentMapper;

    @Resource
    private CityDictMapper cityDictMapper;


    @Transactional(rollbackFor = Exception.class)
    public CreatePhotoVO create(String coupleId, String userId, CreatePhotoRequest request) {
        coupleAccessService.checkAndGet(coupleId, userId);

        String originalUrl = trimRequired(request.getOriginalUrl(), "original_url不能为空");
        String thumbnailUrl = trimRequired(request.getThumbnailUrl(), "thumbnail_url不能为空");
        LocalDate shotDate = parseShotDate(request.getShotDate());
        Integer width = validateDimension(request.getWidth(), "width必须大于0");
        Integer height = validateDimension(request.getHeight(), "height必须大于0");

        AlbumContext albumContext = resolveAlbumContext(coupleId, request.getAlbumIds());
        return createOne(coupleId, userId, originalUrl, thumbnailUrl, shotDate, width, height, albumContext);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<CreatePhotoVO> createBatch(String coupleId, String userId, CreatePhotoBatchRequest request) {
        coupleAccessService.checkAndGet(coupleId, userId);

        AlbumContext albumContext = resolveAlbumContext(coupleId, request.getAlbumIds());
        List<CreatePhotoVO> result = new ArrayList<CreatePhotoVO>();

        for (CreatePhotoBatchItemRequest item : request.getPhotos()) {
            String originalUrl = trimRequired(item.getOriginalUrl(), "original_url不能为空");
            String thumbnailUrl = trimRequired(item.getThumbnailUrl(), "thumbnail_url不能为空");
            LocalDate shotDate = parseShotDate(item.getShotDate());
            Integer width = validateDimension(item.getWidth(), "width必须大于0");
            Integer height = validateDimension(item.getHeight(), "height必须大于0");

            result.add(createOne(coupleId, userId, originalUrl, thumbnailUrl, shotDate, width, height, albumContext));
        }
        return result;
    }


    public PhotoListVO list(String coupleId, String userId, String albumId, String cityCode, Integer year, String cursor, Integer limit) {
        coupleAccessService.checkAndGet(coupleId, userId);

        int finalLimit = limit == null ? 20 : limit;
        if (finalLimit < 1 || finalLimit > 50) {
            throw new BizException(BizCode.BAD_REQUEST, "limit范围应为1-50");
        }

        LambdaQueryWrapper<PhotoEntity> query = new LambdaQueryWrapper<PhotoEntity>()
                .eq(PhotoEntity::getCoupleId, coupleId)
                .eq(PhotoEntity::getIsDeleted, 0);

        if (StringUtils.hasText(cityCode)) {
            query.eq(PhotoEntity::getCityCode, cityCode.trim());
        }

        if (year != null) {
            LocalDate start = LocalDate.of(year, 1, 1);
            LocalDate end = LocalDate.of(year, 12, 31);
            query.between(PhotoEntity::getShotDate, start, end);
        }

        if (StringUtils.hasText(albumId)) {
            String normalizedAlbumId = albumId.trim();
            AlbumEntity album = albumMapper.selectOne(new LambdaQueryWrapper<AlbumEntity>()
                    .eq(AlbumEntity::getId, normalizedAlbumId)
                    .eq(AlbumEntity::getCoupleId, coupleId)
                    .eq(AlbumEntity::getIsDeleted, 0)
                    .last("limit 1"));
            if (album == null) {
                throw new BizException(BizCode.NOT_FOUND, "相册不存在");
            }

            List<AlbumPhotoEntity> relations = albumPhotoMapper.selectList(new LambdaQueryWrapper<AlbumPhotoEntity>()
                    .eq(AlbumPhotoEntity::getAlbumId, normalizedAlbumId));
            if (relations.isEmpty()) {
                PhotoListVO empty = new PhotoListVO();
                empty.setPhotos(new ArrayList<PhotoItemVO>());
                empty.setHasMore(false);
                empty.setNextCursor(null);
                return empty;
            }
            List<String> photoIds = new ArrayList<String>();
            for (AlbumPhotoEntity relation : relations) {
                photoIds.add(relation.getPhotoId());
            }
            query.in(PhotoEntity::getId, photoIds);
        }

        if (StringUtils.hasText(cursor)) {
            PhotoEntity cursorPhoto = photoMapper.selectById(cursor.trim());
            if (cursorPhoto != null && coupleId.equals(cursorPhoto.getCoupleId())) {
                query.and(w -> w.lt(PhotoEntity::getCreatedAt, cursorPhoto.getCreatedAt())
                        .or().eq(PhotoEntity::getCreatedAt, cursorPhoto.getCreatedAt()).lt(PhotoEntity::getId, cursorPhoto.getId()));
            }
        }

        query.orderByDesc(PhotoEntity::getCreatedAt).orderByDesc(PhotoEntity::getId).last("limit " + (finalLimit + 1));
        List<PhotoEntity> rows = photoMapper.selectList(query);

        boolean hasMore = rows.size() > finalLimit;
        if (hasMore) {
            rows = rows.subList(0, finalLimit);
        }

        List<PhotoItemVO> items = new ArrayList<PhotoItemVO>();
        for (PhotoEntity row : rows) {
            PhotoItemVO item = new PhotoItemVO();
            item.setPhotoId(row.getId());
            item.setThumbnailUrl(row.getThumbnailUrl());
            item.setOriginalUrl(row.getOriginalUrl());
            item.setCityName(row.getCityName());
            item.setShotDate(row.getShotDate() == null ? null : row.getShotDate().toString());
            item.setUploaderId(row.getUploaderId());
            Long commentCount = photoCommentMapper.selectCount(new LambdaQueryWrapper<PhotoCommentEntity>()
                    .eq(PhotoCommentEntity::getPhotoId, row.getId())
                    .eq(PhotoCommentEntity::getIsDeleted, 0));
            item.setCommentCount(commentCount == null ? 0 : commentCount.intValue());
            items.add(item);
        }

        PhotoListVO vo = new PhotoListVO();
        vo.setPhotos(items);
        vo.setHasMore(hasMore);
        vo.setNextCursor(hasMore && !items.isEmpty() ? items.get(items.size() - 1).getPhotoId() : null);
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(String coupleId, String userId, String photoId) {
        coupleAccessService.checkAndGet(coupleId, userId);

        PhotoEntity photo = photoMapper.selectOne(new LambdaQueryWrapper<PhotoEntity>()
                .eq(PhotoEntity::getId, photoId)
                .eq(PhotoEntity::getCoupleId, coupleId)
                .eq(PhotoEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (photo == null) {
            throw new BizException(BizCode.NOT_FOUND, "资源不存在");
        }

        LocalDateTime now = LocalDateTime.now();

        photoMapper.update(null, new LambdaUpdateWrapper<PhotoEntity>()
                .eq(PhotoEntity::getId, photoId)
                .eq(PhotoEntity::getCoupleId, coupleId)
                .eq(PhotoEntity::getIsDeleted, 0)
                .set(PhotoEntity::getIsDeleted, 1)
                .set(PhotoEntity::getDeletedAt, now)
                .set(PhotoEntity::getUpdatedAt, now));

        photoCommentMapper.update(null, new LambdaUpdateWrapper<PhotoCommentEntity>()
                .eq(PhotoCommentEntity::getPhotoId, photoId)
                .eq(PhotoCommentEntity::getCoupleId, coupleId)
                .eq(PhotoCommentEntity::getIsDeleted, 0)
                .set(PhotoCommentEntity::getIsDeleted, 1)
                .set(PhotoCommentEntity::getDeletedAt, now)
                .set(PhotoCommentEntity::getUpdatedAt, now));

        List<AlbumPhotoEntity> relations = albumPhotoMapper.selectList(new LambdaQueryWrapper<AlbumPhotoEntity>()
                .eq(AlbumPhotoEntity::getPhotoId, photoId));
        albumPhotoMapper.delete(new LambdaQueryWrapper<AlbumPhotoEntity>()
                .eq(AlbumPhotoEntity::getPhotoId, photoId));

        for (AlbumPhotoEntity relation : relations) {
            AlbumEntity album = albumMapper.selectById(relation.getAlbumId());
            if (album == null || (album.getIsDeleted() != null && album.getIsDeleted() == 1)) {
                continue;
            }
            Long left = albumPhotoMapper.selectCount(new LambdaQueryWrapper<AlbumPhotoEntity>()
                    .eq(AlbumPhotoEntity::getAlbumId, relation.getAlbumId()));
            int leftCount = left == null ? 0 : left.intValue();

            LambdaUpdateWrapper<AlbumEntity> update = new LambdaUpdateWrapper<AlbumEntity>()
                    .eq(AlbumEntity::getId, relation.getAlbumId())
                    .set(AlbumEntity::getPhotoCount, leftCount)
                    .set(AlbumEntity::getUpdatedAt, now);

            if (leftCount == 0) {
                update.set(AlbumEntity::getCoverThumbnailUrl, null);
            } else {
                AlbumPhotoEntity latestRelation = albumPhotoMapper.selectOne(new LambdaQueryWrapper<AlbumPhotoEntity>()
                        .eq(AlbumPhotoEntity::getAlbumId, relation.getAlbumId())
                        .orderByDesc(AlbumPhotoEntity::getCreatedAt)
                        .last("limit 1"));
                PhotoEntity latestPhoto = latestRelation == null ? null : photoMapper.selectOne(new LambdaQueryWrapper<PhotoEntity>()
                        .select(PhotoEntity::getThumbnailUrl)
                        .eq(PhotoEntity::getId, latestRelation.getPhotoId())
                        .eq(PhotoEntity::getCoupleId, coupleId)
                        .eq(PhotoEntity::getIsDeleted, 0)
                        .last("limit 1"));
                update.set(AlbumEntity::getCoverThumbnailUrl, latestPhoto == null ? null : latestPhoto.getThumbnailUrl());
            }
            albumMapper.update(null, update);
        }
    }






    private AlbumContext resolveAlbumContext(String coupleId, List<String> albumIds) {
        Set<String> targetAlbumIds = new HashSet<String>();
        String cityCode = null;
        if (albumIds != null) {
            for (String rawAlbumId : albumIds) {
                if (!StringUtils.hasText(rawAlbumId)) {
                    continue;
                }
                String albumId = rawAlbumId.trim();
                AlbumEntity album = albumMapper.selectOne(new LambdaQueryWrapper<AlbumEntity>()
                        .eq(AlbumEntity::getId, albumId)
                        .eq(AlbumEntity::getCoupleId, coupleId)
                        .eq(AlbumEntity::getType, "custom")
                        .eq(AlbumEntity::getIsDeleted, 0)
                        .last("limit 1"));
                if (album == null) {
                    throw new BizException(BizCode.NOT_FOUND, "相册不存在或不可用: " + albumId);
                }
                if (!StringUtils.hasText(album.getCityCode())) {
                    throw new BizException(BizCode.BAD_REQUEST, "相册缺少城市信息: " + albumId);
                }

                String albumCityCode = album.getCityCode().trim();
                String currentCityCode = cityCode == null ? "" : cityCode.trim();
                if (!StringUtils.hasText(currentCityCode)) {
                    cityCode = albumCityCode;
                } else if (!currentCityCode.equals(albumCityCode)) {
                    throw new BizException(BizCode.BAD_REQUEST, "一次上传仅支持同一城市的相册");
                }
                targetAlbumIds.add(albumId);
            }
        }

        if (targetAlbumIds.isEmpty()) {
            throw new BizException(BizCode.BAD_REQUEST, "请传入album_ids，且相册需为custom类型");
        }

        String finalCityCode = trimRequired(cityCode, "相册缺少城市信息");
        CityDictEntity cityDict = cityDictMapper.selectById(finalCityCode);
        if (cityDict == null || (cityDict.getEnabled() != null && cityDict.getEnabled() != 1)) {
            throw new BizException(BizCode.BAD_REQUEST, "city_code不存在或未启用: " + finalCityCode);
        }
        String cityName = StringUtils.hasText(cityDict.getCityName()) ? cityDict.getCityName().trim() : finalCityCode;

        AlbumContext context = new AlbumContext();
        context.targetAlbumIds = targetAlbumIds;
        context.cityCode = finalCityCode;
        context.cityName = cityName;
        return context;
    }

    private CreatePhotoVO createOne(String coupleId,
                                    String userId,
                                    String originalUrl,
                                    String thumbnailUrl,
                                    LocalDate shotDate,
                                    Integer width,
                                    Integer height,
                                    AlbumContext albumContext) {
        LocalDateTime now = LocalDateTime.now();

        PhotoEntity photo = new PhotoEntity();
        photo.setId(IdGenerator.nextId("ph"));
        photo.setCoupleId(coupleId);
        photo.setUploaderId(userId);
        photo.setOriginalUrl(originalUrl);
        photo.setThumbnailUrl(thumbnailUrl);
        photo.setCityCode(albumContext.cityCode);
        photo.setCityName(albumContext.cityName);
        photo.setShotDate(shotDate);
        photo.setWidth(width);
        photo.setHeight(height);
        photo.setIsDeleted(0);
        photo.setCreatedAt(now);
        photo.setUpdatedAt(now);
        photoMapper.insert(photo);

        for (String albumId : albumContext.targetAlbumIds) {
            AlbumPhotoEntity relation = new AlbumPhotoEntity();
            relation.setAlbumId(albumId);
            relation.setPhotoId(photo.getId());
            relation.setAddedBy(userId);
            relation.setCreatedAt(now);
            albumPhotoMapper.insert(relation);

            albumMapper.update(null, new LambdaUpdateWrapper<AlbumEntity>()
                    .eq(AlbumEntity::getId, albumId)
                    .setSql("photo_count = IFNULL(photo_count,0) + 1")
                    .set(AlbumEntity::getCoverThumbnailUrl, photo.getThumbnailUrl())
                    .set(AlbumEntity::getUpdatedAt, now));
        }

        CreatePhotoVO vo = new CreatePhotoVO();
        vo.setPhotoId(photo.getId());
        vo.setOriginalUrl(photo.getOriginalUrl());
        vo.setThumbnailUrl(photo.getThumbnailUrl());
        vo.setCityCode(photo.getCityCode());
        vo.setCityName(photo.getCityName());
        vo.setShotDate(photo.getShotDate().toString());
        vo.setUploaderId(photo.getUploaderId());
        vo.setCreatedAt(TimeUtil.toIsoUtc(photo.getCreatedAt()));
        return vo;
    }

    private Integer validateDimension(Integer value, String message) {
        if (value != null && value <= 0) {
            throw new BizException(BizCode.BAD_REQUEST, message);
        }
        return value;
    }

    private static class AlbumContext {
        private Set<String> targetAlbumIds;
        private String cityCode;
        private String cityName;
    }

    private String trimRequired(String text, String message) {
        if (!StringUtils.hasText(text)) {
            throw new BizException(BizCode.BAD_REQUEST, message);
        }
        String normalized = text.trim();
        if (!StringUtils.hasText(normalized)) {
            throw new BizException(BizCode.BAD_REQUEST, message);
        }
        return normalized;
    }


    private LocalDate parseShotDate(String text) {
        String normalized = trimRequired(text, "shot_date不能为空");
        try {
            return LocalDate.parse(normalized);
        } catch (DateTimeParseException ex) {
            throw new BizException(BizCode.BAD_REQUEST, "shot_date格式必须为YYYY-MM-DD");
        }
    }
}

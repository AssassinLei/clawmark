package com.clawmark.api.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.clawmark.api.common.constant.BizCode;
import com.clawmark.api.common.exception.BizException;
import com.clawmark.api.common.util.IdGenerator;
import com.clawmark.api.common.util.TimeUtil;
import com.clawmark.api.infrastructure.entity.PhotoCommentEntity;
import com.clawmark.api.infrastructure.entity.PhotoEntity;
import com.clawmark.api.infrastructure.entity.UserEntity;
import com.clawmark.api.infrastructure.mapper.PhotoCommentMapper;
import com.clawmark.api.infrastructure.mapper.PhotoMapper;
import com.clawmark.api.infrastructure.mapper.UserMapper;
import com.clawmark.api.interfaces.dto.comments.AddCommentRequest;
import com.clawmark.api.interfaces.vo.comments.CommentAuthorVO;
import com.clawmark.api.interfaces.vo.comments.CommentCreateVO;
import com.clawmark.api.interfaces.vo.comments.CommentItemVO;
import com.clawmark.api.interfaces.vo.comments.CommentListVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CommentAppService {

    @Resource
    private CoupleAccessService coupleAccessService;

    @Resource
    private PhotoMapper photoMapper;

    @Resource
    private PhotoCommentMapper photoCommentMapper;

    @Resource
    private UserMapper userMapper;

    @Transactional(rollbackFor = Exception.class)
    public CommentCreateVO create(String coupleId, String photoId, String userId, AddCommentRequest request) {
        coupleAccessService.checkAndGet(coupleId, userId);
        ensurePhoto(coupleId, photoId);

        PhotoCommentEntity comment = new PhotoCommentEntity();
        comment.setId(IdGenerator.nextId("cm"));
        comment.setCoupleId(coupleId);
        comment.setPhotoId(photoId);
        comment.setAuthorId(userId);
        comment.setContent(request.getContent().trim());
        comment.setIsDeleted(0);
        photoCommentMapper.insert(comment);

        CommentCreateVO vo = new CommentCreateVO();
        vo.setCommentId(comment.getId());
        vo.setContent(comment.getContent());
        vo.setAuthorId(userId);
        vo.setCreatedAt(TimeUtil.toIsoUtc(comment.getCreatedAt()));
        return vo;
    }

    public CommentListVO list(String coupleId, String photoId, String userId) {
        coupleAccessService.checkAndGet(coupleId, userId);
        ensurePhoto(coupleId, photoId);

        List<PhotoCommentEntity> comments = photoCommentMapper.selectList(new LambdaQueryWrapper<PhotoCommentEntity>()
                .eq(PhotoCommentEntity::getCoupleId, coupleId)
                .eq(PhotoCommentEntity::getPhotoId, photoId)
                .eq(PhotoCommentEntity::getIsDeleted, 0)
                .orderByAsc(PhotoCommentEntity::getCreatedAt));

        List<CommentItemVO> list = new ArrayList<CommentItemVO>();
        for (PhotoCommentEntity comment : comments) {
            UserEntity author = userMapper.selectById(comment.getAuthorId());

            CommentAuthorVO authorVO = new CommentAuthorVO();
            authorVO.setUserId(comment.getAuthorId());
            authorVO.setNickname(author == null ? "" : author.getNickname());
            authorVO.setAvatarUrl(author == null ? null : author.getAvatarUrl());

            CommentItemVO item = new CommentItemVO();
            item.setCommentId(comment.getId());
            item.setContent(comment.getContent());
            item.setAuthor(authorVO);
            item.setCreatedAt(TimeUtil.toIsoUtc(comment.getCreatedAt()));
            list.add(item);
        }

        CommentListVO vo = new CommentListVO();
        vo.setComments(list);
        vo.setTotal(list.size());
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(String coupleId, String photoId, String commentId, String userId) {
        coupleAccessService.checkAndGet(coupleId, userId);
        ensurePhoto(coupleId, photoId);

        PhotoCommentEntity comment = photoCommentMapper.selectOne(new LambdaQueryWrapper<PhotoCommentEntity>()
                .eq(PhotoCommentEntity::getId, commentId)
                .eq(PhotoCommentEntity::getPhotoId, photoId)
                .eq(PhotoCommentEntity::getCoupleId, coupleId)
                .eq(PhotoCommentEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (comment == null) {
            throw new BizException(BizCode.NOT_FOUND, "资源不存在");
        }
        if (!userId.equals(comment.getAuthorId())) {
            throw new BizException(BizCode.FORBIDDEN, "无权限");
        }

        photoCommentMapper.update(null, new LambdaUpdateWrapper<PhotoCommentEntity>()
                .eq(PhotoCommentEntity::getId, commentId)
                .set(PhotoCommentEntity::getIsDeleted, 1)
                .set(PhotoCommentEntity::getDeletedAt, LocalDateTime.now()));
    }

    private void ensurePhoto(String coupleId, String photoId) {
        PhotoEntity photo = photoMapper.selectOne(new LambdaQueryWrapper<PhotoEntity>()
                .eq(PhotoEntity::getId, photoId)
                .eq(PhotoEntity::getCoupleId, coupleId)
                .eq(PhotoEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (photo == null) {
            throw new BizException(BizCode.NOT_FOUND, "资源不存在");
        }
    }
}

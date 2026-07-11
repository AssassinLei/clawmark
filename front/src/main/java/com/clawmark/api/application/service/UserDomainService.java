package com.clawmark.api.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.clawmark.api.common.constant.BizCode;
import com.clawmark.api.common.exception.BizException;
import com.clawmark.api.common.util.TimeUtil;
import com.clawmark.api.infrastructure.entity.CoupleEntity;
import com.clawmark.api.infrastructure.entity.UserEntity;
import com.clawmark.api.infrastructure.mapper.CoupleMapper;
import com.clawmark.api.infrastructure.mapper.UserMapper;
import com.clawmark.api.interfaces.dto.users.UpdateMeRequest;
import com.clawmark.api.interfaces.vo.users.PartnerVO;
import com.clawmark.api.interfaces.vo.users.UserMeVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class UserDomainService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private CoupleMapper coupleMapper;

    public UserEntity getById(String userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(BizCode.UNAUTHORIZED, "未登录或Token已失效");
        }
        return user;
    }

    @Transactional(rollbackFor = Exception.class)
    public UserEntity updateMe(String userId, UpdateMeRequest request) {
        if (!StringUtils.hasText(request.getNickname()) && !StringUtils.hasText(request.getAvatarUrl())) {
            throw new BizException(BizCode.BAD_REQUEST, "nickname或avatar_url至少传一个");
        }

        LambdaUpdateWrapper<UserEntity> updateWrapper = new LambdaUpdateWrapper<UserEntity>()
                .eq(UserEntity::getId, userId);

        if (StringUtils.hasText(request.getNickname())) {
            updateWrapper.set(UserEntity::getNickname, request.getNickname().trim());
        }
        if (StringUtils.hasText(request.getAvatarUrl())) {
            updateWrapper.set(UserEntity::getAvatarUrl, request.getAvatarUrl().trim());
        }

        userMapper.update(null, updateWrapper);
        return getById(userId);
    }

    public UserMeVO buildMe(String userId) {
        UserEntity current = getById(userId);

        UserMeVO vo = new UserMeVO();
        vo.setUserId(current.getId());
        vo.setNickname(current.getNickname());
        vo.setAvatarUrl(current.getAvatarUrl());

        CoupleEntity couple = findActiveCouple(userId);
        if (couple == null) {
            vo.setCoupleId(null);
            vo.setPartner(null);
            vo.setTogetherDays(0);
            vo.setBoundAt(null);
            return vo;
        }

        vo.setCoupleId(couple.getId());
        String partnerId = couple.getInviterUserId().equals(userId) ? couple.getInviteeUserId() : couple.getInviterUserId();
        UserEntity partner = userMapper.selectById(partnerId);

        PartnerVO partnerVO = new PartnerVO();
        partnerVO.setUserId(partnerId);
        partnerVO.setNickname(partner == null ? "" : partner.getNickname());
        partnerVO.setAvatarUrl(partner == null ? null : partner.getAvatarUrl());
        vo.setPartner(partnerVO);

        vo.setBoundAt(TimeUtil.toIsoUtc(couple.getBoundAt()));
        vo.setTogetherDays(calculateTogetherDays(couple.getBoundAt()));
        return vo;
    }

    public CoupleEntity findActiveCouple(String userId) {
        return coupleMapper.selectOne(new LambdaQueryWrapper<CoupleEntity>()
                .eq(CoupleEntity::getStatus, "active")
                .and(w -> w.eq(CoupleEntity::getInviterUserId, userId).or().eq(CoupleEntity::getInviteeUserId, userId))
                .last("limit 1"));
    }

    private Integer calculateTogetherDays(LocalDateTime boundAt) {
        if (boundAt == null) {
            return 0;
        }
        long days = Duration.between(boundAt.toLocalDate().atStartOfDay(), LocalDate.now().atStartOfDay()).toDays();
        return (int) Math.max(days, 0);
    }
}

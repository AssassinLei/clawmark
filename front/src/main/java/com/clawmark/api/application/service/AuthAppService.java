package com.clawmark.api.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clawmark.api.common.constant.BizCode;
import com.clawmark.api.common.exception.BizException;
import com.clawmark.api.common.util.IdGenerator;
import com.clawmark.api.infrastructure.entity.CoupleEntity;
import com.clawmark.api.infrastructure.entity.UserEntity;
import com.clawmark.api.infrastructure.mapper.UserMapper;
import com.clawmark.api.interfaces.dto.auth.AuthLoginRequest;
import com.clawmark.api.interfaces.vo.auth.AuthLoginUserVO;
import com.clawmark.api.interfaces.vo.auth.AuthLoginVO;
import com.clawmark.api.interfaces.vo.auth.AuthRefreshVO;
import com.clawmark.api.security.JwtTokenProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class AuthAppService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private WeChatAuthService weChatAuthService;

    @Resource
    private JwtTokenProvider jwtTokenProvider;

    @Resource
    private UserDomainService userDomainService;

    @Transactional(rollbackFor = Exception.class)
    public AuthLoginVO login(AuthLoginRequest request) {
        String openid = weChatAuthService.exchangeOpenId(request.getCode().trim());
        UserEntity user = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getOpenid, openid)
                .last("limit 1"));

        boolean isNew = false;
        if (user == null) {
            isNew = true;
            user = new UserEntity();
            user.setId(IdGenerator.nextId("u"));
            user.setOpenid(openid);
            user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname().trim() : "微信用户");
            user.setAvatarUrl(StringUtils.hasText(request.getAvatarUrl()) ? request.getAvatarUrl().trim() : null);
            user.setStatus("active");
            user.setLastLoginAt(LocalDateTime.now());
            userMapper.insert(user);
        } else {
            user.setLastLoginAt(LocalDateTime.now());
            if (StringUtils.hasText(request.getNickname())) {
                user.setNickname(request.getNickname().trim());
            }
            if (StringUtils.hasText(request.getAvatarUrl())) {
                user.setAvatarUrl(request.getAvatarUrl().trim());
            }
            userMapper.updateById(user);
        }

        CoupleEntity couple = userDomainService.findActiveCouple(user.getId());

        AuthLoginUserVO userVO = new AuthLoginUserVO();
        userVO.setUserId(user.getId());
        userVO.setOpenid(user.getOpenid());
        userVO.setNickname(user.getNickname());
        userVO.setAvatarUrl(user.getAvatarUrl());
        userVO.setIsNewUser(isNew);
        userVO.setCoupleId(couple == null ? null : couple.getId());

        AuthLoginVO vo = new AuthLoginVO();
        vo.setAccessToken(jwtTokenProvider.generateToken(user.getId()));
        vo.setExpiresIn(jwtTokenProvider.getExpireSeconds());
        vo.setUser(userVO);
        return vo;
    }

    public AuthRefreshVO refresh(String accessToken) {
        String userId;
        try {
            userId = jwtTokenProvider.parseUserId(accessToken);
        } catch (Exception ex) {
            throw new BizException(BizCode.UNAUTHORIZED, "未登录或Token已失效");
        }

        if (userMapper.selectById(userId) == null) {
            throw new BizException(BizCode.UNAUTHORIZED, "未登录或Token已失效");
        }

        AuthRefreshVO vo = new AuthRefreshVO();
        vo.setAccessToken(jwtTokenProvider.generateToken(userId));
        vo.setExpiresIn(jwtTokenProvider.getExpireSeconds());
        return vo;
    }
}

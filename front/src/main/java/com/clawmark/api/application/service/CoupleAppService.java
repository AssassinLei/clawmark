package com.clawmark.api.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.clawmark.api.common.constant.BizCode;
import com.clawmark.api.common.exception.BizException;
import com.clawmark.api.common.util.IdGenerator;
import com.clawmark.api.common.util.InviteCodeGenerator;
import com.clawmark.api.common.util.TimeUtil;
import com.clawmark.api.infrastructure.entity.CoupleEntity;
import com.clawmark.api.infrastructure.entity.CoupleInviteEntity;
import com.clawmark.api.infrastructure.entity.UserEntity;
import com.clawmark.api.infrastructure.mapper.CoupleInviteMapper;
import com.clawmark.api.infrastructure.mapper.CoupleMapper;
import com.clawmark.api.infrastructure.mapper.UserMapper;
import com.clawmark.api.interfaces.vo.couples.BindPartnerVO;
import com.clawmark.api.interfaces.vo.couples.BindVO;
import com.clawmark.api.interfaces.vo.couples.InviteVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class CoupleAppService {

    @Resource
    private CoupleMapper coupleMapper;

    @Resource
    private CoupleInviteMapper coupleInviteMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserDomainService userDomainService;

    @Transactional(rollbackFor = Exception.class)
    public InviteVO createInvite(String currentUserId) {
        if (userDomainService.findActiveCouple(currentUserId) != null) {
            throw new BizException(BizCode.CONFLICT, "已绑定情侣，不能重复生成邀请码");
        }

        coupleInviteMapper.update(null, new LambdaUpdateWrapper<CoupleInviteEntity>()
                .eq(CoupleInviteEntity::getInviterUserId, currentUserId)
                .eq(CoupleInviteEntity::getStatus, "active")
                .set(CoupleInviteEntity::getStatus, "revoked"));

        CoupleInviteEntity invite = new CoupleInviteEntity();
        invite.setId(IdGenerator.nextId("inv"));
        invite.setInviterUserId(currentUserId);
        invite.setStatus("active");
        invite.setExpiresAt(LocalDateTime.now().plusHours(24));
        invite.setInviteCode(generateUniqueInviteCode());
        coupleInviteMapper.insert(invite);

        InviteVO vo = new InviteVO();
        vo.setInviteCode(invite.getInviteCode());
        vo.setExpiresAt(TimeUtil.toIsoUtc(invite.getExpiresAt()));
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public BindVO bind(String currentUserId, String inviteCode) {
        if (userDomainService.findActiveCouple(currentUserId) != null) {
            throw new BizException(BizCode.CONFLICT, "当前用户已绑定情侣");
        }

        CoupleInviteEntity invite = coupleInviteMapper.selectOne(new LambdaQueryWrapper<CoupleInviteEntity>()
                .eq(CoupleInviteEntity::getInviteCode, inviteCode)
                .last("limit 1"));
        if (invite == null || !"active".equals(invite.getStatus()) || invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BizException(BizCode.NOT_FOUND, "邀请码不存在或已失效");
        }

        if (currentUserId.equals(invite.getInviterUserId())) {
            throw new BizException(BizCode.CONFLICT, "不能绑定自己的邀请码");
        }

        if (userDomainService.findActiveCouple(invite.getInviterUserId()) != null) {
            throw new BizException(BizCode.CONFLICT, "邀请码发起方已绑定情侣");
        }

        CoupleEntity couple = new CoupleEntity();
        couple.setId(IdGenerator.nextId("cp"));
        couple.setInviterUserId(invite.getInviterUserId());
        couple.setInviteeUserId(currentUserId);
        couple.setDataOwnerUserId(invite.getInviterUserId());
        couple.setStatus("active");
        couple.setBoundAt(LocalDateTime.now());
        coupleMapper.insert(couple);

        invite.setStatus("used");
        invite.setUsedByUserId(currentUserId);
        invite.setUsedAt(LocalDateTime.now());
        invite.setCoupleId(couple.getId());
        coupleInviteMapper.updateById(invite);

        UserEntity partner = userMapper.selectById(invite.getInviterUserId());

        BindPartnerVO partnerVO = new BindPartnerVO();
        partnerVO.setUserId(invite.getInviterUserId());
        partnerVO.setNickname(partner == null ? "" : partner.getNickname());
        partnerVO.setAvatarUrl(partner == null ? null : partner.getAvatarUrl());

        BindVO vo = new BindVO();
        vo.setCoupleId(couple.getId());
        vo.setPartner(partnerVO);
        vo.setBoundAt(TimeUtil.toIsoUtc(couple.getBoundAt()));
        vo.setTogetherDays(0);
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void unbind(String currentUserId) {
        CoupleEntity couple = userDomainService.findActiveCouple(currentUserId);
        if (couple == null) {
            throw new BizException(BizCode.NOT_FOUND, "未找到绑定关系");
        }

        couple.setStatus("unbound");
        couple.setUnboundAt(LocalDateTime.now());
        coupleMapper.updateById(couple);

        coupleInviteMapper.update(null, new LambdaUpdateWrapper<CoupleInviteEntity>()
                .eq(CoupleInviteEntity::getStatus, "active")
                .and(w -> w.eq(CoupleInviteEntity::getInviterUserId, couple.getInviterUserId())
                        .or().eq(CoupleInviteEntity::getInviterUserId, couple.getInviteeUserId()))
                .set(CoupleInviteEntity::getStatus, "revoked"));
    }

    private String generateUniqueInviteCode() {
        for (int i = 0; i < 10; i++) {
            String code = InviteCodeGenerator.generate();
            Long count = coupleInviteMapper.selectCount(new LambdaQueryWrapper<CoupleInviteEntity>()
                    .eq(CoupleInviteEntity::getInviteCode, code));
            if (count == null || count == 0) {
                return code;
            }
        }
        throw new BizException(BizCode.INTERNAL_ERROR, "邀请码生成失败，请稍后重试");
    }
}

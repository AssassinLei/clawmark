package com.clawmark.api.application.service;

import com.clawmark.api.common.constant.BizCode;
import com.clawmark.api.common.exception.BizException;
import com.clawmark.api.infrastructure.entity.CoupleEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class CoupleAccessService {

    @Resource
    private UserDomainService userDomainService;

    public CoupleEntity checkAndGet(String coupleId, String userId) {
        CoupleEntity couple = userDomainService.findActiveCouple(userId);
        if (couple == null || !couple.getId().equals(coupleId)) {
            throw new BizException(BizCode.FORBIDDEN, "无权限");
        }
        return couple;
    }
}

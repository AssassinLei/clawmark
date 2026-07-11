package com.clawmark.api.interfaces.rest;

import com.clawmark.api.application.service.CoupleAppService;
import com.clawmark.api.common.api.ApiResponse;
import com.clawmark.api.common.context.UserContext;
import com.clawmark.api.interfaces.dto.couples.BindCoupleRequest;
import com.clawmark.api.interfaces.vo.couples.BindVO;
import com.clawmark.api.interfaces.vo.couples.InviteVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/couples")
public class CouplesController {

    @Resource
    private CoupleAppService coupleAppService;

    @PostMapping("/invite")
    public ApiResponse<InviteVO> createInvite() {
        return ApiResponse.success(coupleAppService.createInvite(UserContext.getUserId()));
    }

    @PostMapping("/bind")
    public ApiResponse<BindVO> bind(@Valid @RequestBody BindCoupleRequest request) {
        return ApiResponse.success(coupleAppService.bind(UserContext.getUserId(), request.getInviteCode()));
    }

    @DeleteMapping("/bind")
    public ApiResponse<Void> unbind() {
        coupleAppService.unbind(UserContext.getUserId());
        return ApiResponse.successMessage("解绑成功");
    }
}

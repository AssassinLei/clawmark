package com.clawmark.api.interfaces.rest;

import com.clawmark.api.application.service.AuthAppService;
import com.clawmark.api.common.api.ApiResponse;
import com.clawmark.api.interfaces.dto.auth.AuthLoginRequest;
import com.clawmark.api.interfaces.dto.auth.AuthRefreshRequest;
import com.clawmark.api.interfaces.vo.auth.AuthLoginVO;
import com.clawmark.api.interfaces.vo.auth.AuthRefreshVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Resource
    private AuthAppService authAppService;

    @PostMapping("/login")
    public ApiResponse<AuthLoginVO> login(@Valid @RequestBody AuthLoginRequest request) {
        return ApiResponse.success(authAppService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthRefreshVO> refresh(@Valid @RequestBody AuthRefreshRequest request) {
        return ApiResponse.success(authAppService.refresh(request.getAccessToken()));
    }
}

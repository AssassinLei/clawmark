package com.clawmark.api.interfaces.rest;

import com.clawmark.api.application.service.UserDomainService;
import com.clawmark.api.common.api.ApiResponse;
import com.clawmark.api.common.context.UserContext;
import com.clawmark.api.interfaces.dto.users.UpdateMeRequest;
import com.clawmark.api.interfaces.vo.users.UserMeVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/users")
public class UsersController {

    @Resource
    private UserDomainService userDomainService;

    @GetMapping("/me")
    public ApiResponse<UserMeVO> me() {
        return ApiResponse.success(userDomainService.buildMe(UserContext.getUserId()));
    }

    @PutMapping("/me")
    public ApiResponse<UserMeVO> updateMe(@Valid @RequestBody UpdateMeRequest request) {
        userDomainService.updateMe(UserContext.getUserId(), request);
        return ApiResponse.success(userDomainService.buildMe(UserContext.getUserId()));
    }
}

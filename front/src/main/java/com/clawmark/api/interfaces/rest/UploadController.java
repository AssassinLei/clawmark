package com.clawmark.api.interfaces.rest;

import com.clawmark.api.application.service.UploadAppService;
import com.clawmark.api.common.api.ApiResponse;
import com.clawmark.api.common.context.UserContext;
import com.clawmark.api.interfaces.vo.upload.UploadFileVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


@Validated
@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {

    @Resource
    private UploadAppService uploadAppService;

    @PostMapping("/file")
    public ApiResponse<UploadFileVO> uploadFile(@RequestParam("file") MultipartFile file,
                                                @RequestParam(value = "image_type", required = false) String imageType,
                                                HttpServletRequest request) {
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();
        return ApiResponse.success(uploadAppService.uploadLocal(UserContext.getUserId(), file, imageType, baseUrl));
    }
}

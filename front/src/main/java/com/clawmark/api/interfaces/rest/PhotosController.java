package com.clawmark.api.interfaces.rest;

import com.clawmark.api.application.service.PhotoAppService;
import com.clawmark.api.common.api.ApiResponse;
import com.clawmark.api.common.context.UserContext;
import com.clawmark.api.interfaces.dto.photos.CreatePhotoBatchRequest;
import com.clawmark.api.interfaces.dto.photos.CreatePhotoRequest;
import com.clawmark.api.interfaces.vo.photos.CreatePhotoVO;
import com.clawmark.api.interfaces.vo.photos.PhotoListVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/couples/{coupleId}/photos")
public class PhotosController {

    @Resource
    private PhotoAppService photoAppService;

    @PostMapping
    public ApiResponse<CreatePhotoVO> create(@PathVariable("coupleId") String coupleId,
                                             @Valid @RequestBody CreatePhotoRequest request) {
        return ApiResponse.success(photoAppService.create(coupleId, UserContext.getUserId(), request));
    }

    @PostMapping("/batch")
    public ApiResponse<List<CreatePhotoVO>> createBatch(@PathVariable("coupleId") String coupleId,
                                                         @Valid @RequestBody CreatePhotoBatchRequest request) {
        return ApiResponse.success(photoAppService.createBatch(coupleId, UserContext.getUserId(), request));
    }


    @GetMapping
    public ApiResponse<PhotoListVO> list(@PathVariable("coupleId") String coupleId,
                                         @RequestParam(value = "album_id", required = false) String albumId,
                                         @RequestParam(value = "city_code", required = false) String cityCode,
                                         @RequestParam(value = "year", required = false) Integer year,
                                         @RequestParam(value = "cursor", required = false) String cursor,
                                         @RequestParam(value = "limit", required = false) Integer limit) {
        return ApiResponse.success(photoAppService.list(coupleId, UserContext.getUserId(), albumId, cityCode, year, cursor, limit));
    }

    @DeleteMapping("/{photoId}")
    public ApiResponse<Void> delete(@PathVariable("coupleId") String coupleId,
                                    @PathVariable("photoId") String photoId) {
        photoAppService.delete(coupleId, UserContext.getUserId(), photoId);
        return ApiResponse.successMessage("删除成功");
    }
}

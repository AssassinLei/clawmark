package com.clawmark.api.interfaces.rest;

import com.clawmark.api.application.service.AlbumAppService;
import com.clawmark.api.common.api.ApiResponse;
import com.clawmark.api.common.context.UserContext;
import com.clawmark.api.interfaces.dto.albums.CreateAlbumRequest;
import com.clawmark.api.interfaces.vo.albums.AlbumListVO;
import com.clawmark.api.interfaces.vo.albums.CreateAlbumVO;
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

@Validated
@RestController
@RequestMapping("/api/v1/couples/{coupleId}/albums")
public class AlbumsController {

    @Resource
    private AlbumAppService albumAppService;

    @GetMapping
    public ApiResponse<AlbumListVO> list(@PathVariable("coupleId") String coupleId,
                                         @RequestParam(value = "type", required = false) String type,
                                         @RequestParam(value = "page", required = false) Integer page,
                                         @RequestParam(value = "page_size", required = false) Integer pageSize) {
        return ApiResponse.success(albumAppService.list(coupleId, UserContext.getUserId(), type, page, pageSize));
    }

    @PostMapping
    public ApiResponse<CreateAlbumVO> create(@PathVariable("coupleId") String coupleId,
                                             @Valid @RequestBody CreateAlbumRequest request) {
        return ApiResponse.success(albumAppService.createCustom(coupleId, UserContext.getUserId(), request));
    }

    @DeleteMapping("/{albumId}")
    public ApiResponse<Void> delete(@PathVariable("coupleId") String coupleId,
                                    @PathVariable("albumId") String albumId) {
        albumAppService.delete(coupleId, UserContext.getUserId(), albumId);
        return ApiResponse.successMessage("删除成功");
    }
}

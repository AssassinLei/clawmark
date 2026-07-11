package com.clawmark.api.interfaces.rest;

import com.clawmark.api.application.service.CommentAppService;
import com.clawmark.api.common.api.ApiResponse;
import com.clawmark.api.common.context.UserContext;
import com.clawmark.api.interfaces.dto.comments.AddCommentRequest;
import com.clawmark.api.interfaces.vo.comments.CommentCreateVO;
import com.clawmark.api.interfaces.vo.comments.CommentListVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/couples/{coupleId}/photos/{photoId}/comments")
public class CommentsController {

    @Resource
    private CommentAppService commentAppService;

    @PostMapping
    public ApiResponse<CommentCreateVO> create(@PathVariable("coupleId") String coupleId,
                                               @PathVariable("photoId") String photoId,
                                               @Valid @RequestBody AddCommentRequest request) {
        return ApiResponse.success(commentAppService.create(coupleId, photoId, UserContext.getUserId(), request));
    }

    @GetMapping
    public ApiResponse<CommentListVO> list(@PathVariable("coupleId") String coupleId,
                                           @PathVariable("photoId") String photoId) {
        return ApiResponse.success(commentAppService.list(coupleId, photoId, UserContext.getUserId()));
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> delete(@PathVariable("coupleId") String coupleId,
                                    @PathVariable("photoId") String photoId,
                                    @PathVariable("commentId") String commentId) {
        commentAppService.delete(coupleId, photoId, commentId, UserContext.getUserId());
        return ApiResponse.successMessage("删除成功");
    }
}

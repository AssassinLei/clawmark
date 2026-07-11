package com.clawmark.api.interfaces.rest;

import com.clawmark.api.application.service.TodoAppService;
import com.clawmark.api.common.api.ApiResponse;
import com.clawmark.api.common.context.UserContext;
import com.clawmark.api.interfaces.dto.todos.CreateTodoRequest;
import com.clawmark.api.interfaces.dto.todos.UpdateTodoRequest;
import com.clawmark.api.interfaces.vo.todos.TodoCompleteVO;
import com.clawmark.api.interfaces.vo.todos.TodoCreateVO;
import com.clawmark.api.interfaces.vo.todos.TodoListVO;
import com.clawmark.api.interfaces.vo.todos.TodoUpdateVO;
import com.clawmark.api.interfaces.vo.todos.TodoUrgeVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/couples/{coupleId}/todos")
public class TodosController {

    @Resource
    private TodoAppService todoAppService;

    @GetMapping
    public ApiResponse<TodoListVO> list(@PathVariable("coupleId") String coupleId,
                                        @RequestParam(value = "type", required = false) String type,
                                        @RequestParam(value = "status", required = false) String status,
                                        @RequestParam(value = "page", required = false) Integer page,
                                        @RequestParam(value = "page_size", required = false) Integer pageSize) {
        return ApiResponse.success(todoAppService.list(coupleId, UserContext.getUserId(), type, status, page, pageSize));
    }

    @PostMapping
    public ApiResponse<TodoCreateVO> create(@PathVariable("coupleId") String coupleId,
                                            @Valid @RequestBody CreateTodoRequest request) {
        return ApiResponse.success(todoAppService.create(coupleId, UserContext.getUserId(), request));
    }

    @PutMapping("/{todoId}")
    public ApiResponse<TodoUpdateVO> update(@PathVariable("coupleId") String coupleId,
                                            @PathVariable("todoId") String todoId,
                                            @Valid @RequestBody UpdateTodoRequest request) {
        return ApiResponse.success(todoAppService.update(coupleId, todoId, UserContext.getUserId(), request));
    }

    @PatchMapping("/{todoId}/complete")
    public ApiResponse<TodoCompleteVO> complete(@PathVariable("coupleId") String coupleId,
                                                @PathVariable("todoId") String todoId) {
        return ApiResponse.success(todoAppService.complete(coupleId, todoId, UserContext.getUserId()));
    }

    @PostMapping("/{todoId}/urge")
    public ApiResponse<TodoUrgeVO> urge(@PathVariable("coupleId") String coupleId,
                                        @PathVariable("todoId") String todoId) {
        return ApiResponse.success(todoAppService.urge(coupleId, todoId, UserContext.getUserId()));
    }

    @DeleteMapping("/{todoId}")
    public ApiResponse<Void> delete(@PathVariable("coupleId") String coupleId,
                                    @PathVariable("todoId") String todoId) {
        todoAppService.delete(coupleId, todoId, UserContext.getUserId());
        return ApiResponse.successMessage("删除成功");
    }
}

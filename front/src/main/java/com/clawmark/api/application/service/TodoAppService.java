package com.clawmark.api.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clawmark.api.common.constant.BizCode;
import com.clawmark.api.common.exception.BizException;
import com.clawmark.api.common.util.IdGenerator;
import com.clawmark.api.common.util.TimeUtil;
import com.clawmark.api.infrastructure.entity.CoupleEntity;
import com.clawmark.api.infrastructure.entity.TodoEntity;
import com.clawmark.api.infrastructure.entity.TodoUrgeLogEntity;
import com.clawmark.api.infrastructure.mapper.TodoMapper;
import com.clawmark.api.infrastructure.mapper.TodoUrgeLogMapper;
import com.clawmark.api.interfaces.dto.todos.CreateTodoRequest;
import com.clawmark.api.interfaces.dto.todos.UpdateTodoRequest;
import com.clawmark.api.interfaces.vo.todos.TodoCompleteVO;
import com.clawmark.api.interfaces.vo.todos.TodoCreateVO;
import com.clawmark.api.interfaces.vo.todos.TodoItemVO;
import com.clawmark.api.interfaces.vo.todos.TodoListVO;
import com.clawmark.api.interfaces.vo.todos.TodoUpdateVO;
import com.clawmark.api.interfaces.vo.todos.TodoUrgeVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TodoAppService {

    @Resource
    private CoupleAccessService coupleAccessService;

    @Resource
    private TodoMapper todoMapper;

    @Resource
    private TodoUrgeLogMapper todoUrgeLogMapper;

    @Resource
    private NotificationTriggerService notificationTriggerService;

    public TodoListVO list(String coupleId, String userId, String type, String status, Integer page, Integer pageSize) {
        coupleAccessService.checkAndGet(coupleId, userId);

        String typeValue = StringUtils.hasText(type) ? type : "all";
        if (!"all".equals(typeValue) && !"mine".equals(typeValue) && !"partner".equals(typeValue) && !"shared".equals(typeValue)) {
            throw new BizException(BizCode.BAD_REQUEST, "type仅支持mine|partner|shared|all");
        }

        String statusValue = StringUtils.hasText(status) ? status : "all";
        if (!"all".equals(statusValue) && !"pending".equals(statusValue) && !"done".equals(statusValue) && !"expired".equals(statusValue)) {
            throw new BizException(BizCode.BAD_REQUEST, "status仅支持pending|done|expired|all");
        }

        int finalPage = page == null || page < 1 ? 1 : page;
        int finalPageSize = pageSize == null ? 20 : pageSize;
        if (finalPageSize < 1 || finalPageSize > 50) {
            throw new BizException(BizCode.BAD_REQUEST, "page_size范围应为1-50");
        }

        LambdaQueryWrapper<TodoEntity> query = new LambdaQueryWrapper<TodoEntity>()
                .eq(TodoEntity::getCoupleId, coupleId)
                .eq(TodoEntity::getIsDeleted, 0)
                .orderByDesc(TodoEntity::getCreatedAt);

        if (!"all".equals(typeValue)) {
            query.eq(TodoEntity::getType, typeValue);
        }

        LocalDateTime now = LocalDateTime.now();
        if ("pending".equals(statusValue)) {
            query.eq(TodoEntity::getStatus, "pending")
                    .and(w -> w.isNull(TodoEntity::getDeadline).or().ge(TodoEntity::getDeadline, now));
        } else if ("done".equals(statusValue)) {
            query.eq(TodoEntity::getStatus, "done");
        } else if ("expired".equals(statusValue)) {
            query.and(w -> w.eq(TodoEntity::getStatus, "expired")
                    .or(t -> t.eq(TodoEntity::getStatus, "pending").lt(TodoEntity::getDeadline, now)));
        }

        Page<TodoEntity> p = todoMapper.selectPage(new Page<TodoEntity>(finalPage, finalPageSize), query);
        List<TodoItemVO> items = new ArrayList<TodoItemVO>();
        for (TodoEntity row : p.getRecords()) {
            items.add(toItemVO(row));
        }

        TodoListVO vo = new TodoListVO();
        vo.setTodos(items);
        vo.setTotal(p.getTotal());
        vo.setPage(finalPage);
        vo.setPageSize(finalPageSize);
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public TodoCreateVO create(String coupleId, String userId, CreateTodoRequest request) {
        CoupleEntity couple = coupleAccessService.checkAndGet(coupleId, userId);

        LocalDateTime deadline = TimeUtil.parseIsoDateTime(request.getDeadline(), "deadline");
        String partnerId = getPartnerId(couple, userId);

        TodoEntity todo = new TodoEntity();
        todo.setId(IdGenerator.nextId("td"));
        todo.setCoupleId(coupleId);
        todo.setTitle(request.getTitle().trim());
        todo.setDescription(request.getDescription());
        todo.setType(request.getType());
        todo.setStatus("pending");
        todo.setDeadline(deadline);
        todo.setIsUrging(0);
        todo.setCreatedBy(userId);
        todo.setAssigneeId(resolveAssignee(request.getType(), userId, partnerId));
        todo.setIsDeleted(0);
        todoMapper.insert(todo);

        if ("partner".equals(todo.getType())) {
            notificationTriggerService.onPartnerTodoCreated(userId, partnerId, todo.getTitle());
        }
        if ("shared".equals(todo.getType())) {
            notificationTriggerService.onSharedTodoCreated(userId, partnerId, todo.getTitle());
        }

        TodoCreateVO vo = new TodoCreateVO();
        vo.setTodoId(todo.getId());
        vo.setTitle(todo.getTitle());
        vo.setType(todo.getType());
        vo.setStatus(todo.getStatus());
        vo.setDeadline(TimeUtil.toIsoUtc(todo.getDeadline()));
        vo.setCreatedBy(todo.getCreatedBy());
        vo.setCreatedAt(TimeUtil.toIsoUtc(todo.getCreatedAt()));
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public TodoUpdateVO update(String coupleId, String todoId, String userId, UpdateTodoRequest request) {
        coupleAccessService.checkAndGet(coupleId, userId);
        TodoEntity todo = mustGetTodo(coupleId, todoId);

        if (!userId.equals(todo.getCreatedBy())) {
            throw new BizException(BizCode.FORBIDDEN, "无权限");
        }
        if (!StringUtils.hasText(request.getTitle()) && request.getDescription() == null && request.getDeadline() == null) {
            throw new BizException(BizCode.BAD_REQUEST, "至少传入一个可更新字段");
        }

        LambdaUpdateWrapper<TodoEntity> update = new LambdaUpdateWrapper<TodoEntity>().eq(TodoEntity::getId, todoId);
        if (StringUtils.hasText(request.getTitle())) {
            update.set(TodoEntity::getTitle, request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            update.set(TodoEntity::getDescription, request.getDescription());
        }
        if (request.getDeadline() != null) {
            update.set(TodoEntity::getDeadline, TimeUtil.parseIsoDateTime(request.getDeadline(), "deadline"));
        }
        todoMapper.update(null, update);

        TodoEntity latest = mustGetTodo(coupleId, todoId);
        TodoUpdateVO vo = new TodoUpdateVO();
        vo.setTodoId(latest.getId());
        vo.setTitle(latest.getTitle());
        vo.setDeadline(TimeUtil.toIsoUtc(latest.getDeadline()));
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public TodoCompleteVO complete(String coupleId, String todoId, String userId) {
        coupleAccessService.checkAndGet(coupleId, userId);
        TodoEntity todo = mustGetTodo(coupleId, todoId);

        if ("partner".equals(todo.getType()) && (todo.getAssigneeId() == null || !todo.getAssigneeId().equals(userId))) {
            throw new BizException(BizCode.FORBIDDEN, "partner任务仅assignee可完成");
        }
        if ("done".equals(todo.getStatus())) {
            TodoCompleteVO doneVo = new TodoCompleteVO();
            doneVo.setTodoId(todo.getId());
            doneVo.setStatus("done");
            doneVo.setCompletedBy(todo.getCompletedBy());
            doneVo.setCompletedAt(TimeUtil.toIsoUtc(todo.getCompletedAt()));
            return doneVo;
        }

        LocalDateTime now = LocalDateTime.now();
        todoMapper.update(null, new LambdaUpdateWrapper<TodoEntity>()
                .eq(TodoEntity::getId, todoId)
                .set(TodoEntity::getStatus, "done")
                .set(TodoEntity::getCompletedBy, userId)
                .set(TodoEntity::getCompletedAt, now));

        notificationTriggerService.onTodoCompleted(userId, todo.getCreatedBy(), todo.getTitle());

        TodoCompleteVO vo = new TodoCompleteVO();
        vo.setTodoId(todo.getId());
        vo.setStatus("done");
        vo.setCompletedBy(userId);
        vo.setCompletedAt(TimeUtil.toIsoUtc(now));
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public TodoUrgeVO urge(String coupleId, String todoId, String userId) {
        CoupleEntity couple = coupleAccessService.checkAndGet(coupleId, userId);
        TodoEntity todo = mustGetTodo(coupleId, todoId);

        if ("done".equals(todo.getStatus())) {
            throw new BizException(BizCode.BAD_REQUEST, "已完成任务不能催办");
        }

        LocalDateTime now = LocalDateTime.now();
        if (todo.getNextUrgeAvailableAt() != null && now.isBefore(todo.getNextUrgeAvailableAt())) {
            throw new BizException(BizCode.RATE_LIMIT, "同一任务24小时内只能催办1次");
        }

        String receiverUserId = getPartnerId(couple, userId);
        LocalDateTime next = now.plusHours(24);
        todoMapper.update(null, new LambdaUpdateWrapper<TodoEntity>()
                .eq(TodoEntity::getId, todoId)
                .set(TodoEntity::getIsUrging, 1)
                .set(TodoEntity::getLastUrgedAt, now)
                .set(TodoEntity::getNextUrgeAvailableAt, next));

        TodoUrgeLogEntity log = new TodoUrgeLogEntity();
        log.setTodoId(todoId);
        log.setSenderUserId(userId);
        log.setReceiverUserId(receiverUserId);
        todoUrgeLogMapper.insert(log);

        notificationTriggerService.onTodoUrged(userId, receiverUserId, todo.getTitle());

        TodoUrgeVO vo = new TodoUrgeVO();
        vo.setTodoId(todoId);
        vo.setNextUrgeAvailableAt(TimeUtil.toIsoUtc(next));
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(String coupleId, String todoId, String userId) {
        coupleAccessService.checkAndGet(coupleId, userId);
        TodoEntity todo = mustGetTodo(coupleId, todoId);

        if (!userId.equals(todo.getCreatedBy())) {
            throw new BizException(BizCode.FORBIDDEN, "无权限");
        }

        todoMapper.update(null, new LambdaUpdateWrapper<TodoEntity>()
                .eq(TodoEntity::getId, todoId)
                .set(TodoEntity::getIsDeleted, 1)
                .set(TodoEntity::getDeletedAt, LocalDateTime.now()));
    }

    @Transactional(rollbackFor = Exception.class)
    public int markExpiredTodos() {
        LocalDateTime now = LocalDateTime.now();
        return todoMapper.update(null, new LambdaUpdateWrapper<TodoEntity>()
                .eq(TodoEntity::getIsDeleted, 0)
                .eq(TodoEntity::getStatus, "pending")
                .isNotNull(TodoEntity::getDeadline)
                .lt(TodoEntity::getDeadline, now)
                .set(TodoEntity::getStatus, "expired"));
    }

    private TodoEntity mustGetTodo(String coupleId, String todoId) {
        TodoEntity todo = todoMapper.selectOne(new LambdaQueryWrapper<TodoEntity>()
                .eq(TodoEntity::getId, todoId)
                .eq(TodoEntity::getCoupleId, coupleId)
                .eq(TodoEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (todo == null) {
            throw new BizException(BizCode.NOT_FOUND, "资源不存在");
        }
        return todo;
    }


    private String resolveAssignee(String type, String userId, String partnerId) {
        if ("mine".equals(type)) {
            return userId;
        }
        if ("partner".equals(type)) {
            return partnerId;
        }
        return null;
    }

    private String getPartnerId(CoupleEntity couple, String currentUserId) {
        if (couple.getInviterUserId().equals(currentUserId)) {
            return couple.getInviteeUserId();
        }
        return couple.getInviterUserId();
    }

    private TodoItemVO toItemVO(TodoEntity row) {
        TodoItemVO item = new TodoItemVO();
        item.setTodoId(row.getId());
        item.setTitle(row.getTitle());
        item.setDescription(row.getDescription());
        item.setType(row.getType());
        item.setStatus(resolveViewStatus(row));
        item.setDeadline(TimeUtil.toIsoUtc(row.getDeadline()));
        item.setIsUrging(row.getIsUrging() != null && row.getIsUrging() == 1);
        item.setCreatedBy(row.getCreatedBy());
        item.setAssigneeId(row.getAssigneeId());
        item.setCompletedBy(row.getCompletedBy());
        item.setCompletedAt(TimeUtil.toIsoUtc(row.getCompletedAt()));
        item.setCreatedAt(TimeUtil.toIsoUtc(row.getCreatedAt()));
        return item;
    }

    private String resolveViewStatus(TodoEntity row) {
        if ("done".equals(row.getStatus())) {
            return "done";
        }
        if ("expired".equals(row.getStatus())) {
            return "expired";
        }
        if (row.getDeadline() != null && row.getDeadline().isBefore(LocalDateTime.now())) {
            return "expired";
        }
        return "pending";
    }
}

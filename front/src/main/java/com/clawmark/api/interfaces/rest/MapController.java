package com.clawmark.api.interfaces.rest;

import com.clawmark.api.application.service.MapAppService;
import com.clawmark.api.common.api.ApiResponse;
import com.clawmark.api.common.context.UserContext;
import com.clawmark.api.interfaces.vo.map.CityLightListVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1/couples/{coupleId}/map")
public class MapController {

    @Resource
    private MapAppService mapAppService;

    @GetMapping("/cities")
    public ApiResponse<CityLightListVO> cities(@PathVariable("coupleId") String coupleId) {
        return ApiResponse.success(mapAppService.cities(coupleId, UserContext.getUserId()));
    }
}

package com.clawmark.api.application.service;

import com.clawmark.api.common.constant.BizCode;
import com.clawmark.api.common.exception.BizException;
import com.clawmark.api.config.WeChatProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.net.URI;
import java.util.Map;
import java.util.Objects;

@Service
public class WeChatAuthService {

    private static final Logger log = LoggerFactory.getLogger(WeChatAuthService.class);

    @Resource
    private WeChatProperties weChatProperties;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeChatAuthService() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(requestFactory);
    }

    public String exchangeOpenId(String code) {
        if (!StringUtils.hasText(code)) {
            throw new BizException(BizCode.BAD_REQUEST, "code不能为空");
        }
        if (!StringUtils.hasText(weChatProperties.getAppid()) || !StringUtils.hasText(weChatProperties.getSecret())) {
            throw new BizException(BizCode.INTERNAL_ERROR, "微信登录配置缺失，请联系管理员");
        }

        String code2sessionUrl = StringUtils.hasText(weChatProperties.getCode2sessionUrl())
                ? weChatProperties.getCode2sessionUrl().trim()
                : "https://api.weixin.qq.com/sns/jscode2session";

        URI uri = UriComponentsBuilder.fromHttpUrl(Objects.requireNonNull(code2sessionUrl))
                .queryParam("appid", weChatProperties.getAppid().trim())
                .queryParam("secret", weChatProperties.getSecret().trim())
                .queryParam("js_code", code.trim())
                .queryParam("grant_type", "authorization_code")
                .build(true)
                .toUri();

        Map<String, Object> body;
        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, null, String.class);
            String rawBody = response.getBody();
            if (!StringUtils.hasText(rawBody)) {
                throw new BizException(BizCode.INTERNAL_ERROR, "微信服务返回为空");
            }
            body = objectMapper.readValue(rawBody, new TypeReference<Map<String, Object>>() {
            });
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("调用微信code2session失败, uri={}", uri, ex);
            String reason = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
            throw new BizException(BizCode.INTERNAL_ERROR, "微信服务调用失败: " + (StringUtils.hasText(reason) ? reason : "请稍后重试"));
        }

        Object errCode = body.get("errcode");
        if (errCode != null && !"0".equals(String.valueOf(errCode))) {
            Object errMsg = body.get("errmsg");
            throw new BizException(BizCode.UNAUTHORIZED, "微信登录失败: " + String.valueOf(errMsg));
        }

        String openid = asString(body.get("openid"));
        if (!StringUtils.hasText(openid)) {
            throw new BizException(BizCode.INTERNAL_ERROR, "微信登录返回缺少openid");
        }
        return openid;
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}


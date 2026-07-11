package com.clawmark.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wechat")
public class WeChatProperties {

    private String appid;

    private String secret;

    private String code2sessionUrl = "https://api.weixin.qq.com/sns/jscode2session";
}

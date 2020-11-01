package com.imooc.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author pengfei.zhao
 * @date 2020/11/1 16:34
 */
@Component
@ConfigurationProperties(prefix = "alipay")
@Data
public class AliPayAccountConfig {
    private String appId;

    private String privateKey;

    private String aliPayPublicKey;

    private String notifyUrl;

    private String returnUrl;

}

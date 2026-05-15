package com.tuling.mall.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "gateway.whitelist")
public class WhitelistProperties {

    private boolean enabled = true;

    private List<String> paths = new ArrayList<>();
}

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

    private List<Rule> rules = new ArrayList<>();

    @Data
    public static class Rule {
        /** 匹配类型: exact, prefix, regex, ant, ip, header */
        private String type;
        /** 路径匹配值（exact/prefix/regex/ant 类型使用） */
        private String path;
        /** 通用匹配值（ip 类型使用，支持单IP或CIDR） */
        private String value;
        /** 请求头名称（header 类型使用） */
        private String name;
        /** IP 匹配值（非 ip 类型规则中作为附加 IP 条件） */
        private String ip;
    }
}

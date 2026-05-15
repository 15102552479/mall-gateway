package com.tuling.mall.gateway.filter;

import com.tuling.mall.gateway.config.WhitelistProperties;
import com.tuling.mall.gateway.filter.matcher.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(value = "gateway.whitelist.enabled", havingValue = "true", matchIfMissing = true)
public class WhitelistFilter implements GlobalFilter, Ordered {

    private final WhitelistProperties whitelistProperties;
    private final List<RuleMatcherGroup> ruleGroups = new ArrayList<>();

    public WhitelistFilter(WhitelistProperties whitelistProperties) {
        this.whitelistProperties = whitelistProperties;
    }

    @PostConstruct
    public void init() {
        ruleGroups.clear();

        // 旧版 paths 配置兼容：自动转为 regex 规则
        for (String pathRegex : whitelistProperties.getPaths()) {
            try {
                RuleMatcherGroup group = new RuleMatcherGroup();
                group.add(new PathRegexMatcher(pathRegex));
                ruleGroups.add(group);
            } catch (Exception e) {
                log.warn("白名单旧版 paths 规则解析失败，跳过: {}，原因: {}", pathRegex, e.getMessage());
            }
        }

        // 新版 rules 配置
        for (WhitelistProperties.Rule rule : whitelistProperties.getRules()) {
            try {
                RuleMatcherGroup group = buildGroup(rule);
                if (group != null) {
                    ruleGroups.add(group);
                }
            } catch (Exception e) {
                log.warn("白名单规则解析失败，跳过: type={}, path={}，原因: {}", rule.getType(), rule.getPath(), e.getMessage());
            }
        }

        log.info("白名单过滤器已启用，加载 {} 条规则", ruleGroups.size());
    }

    private RuleMatcherGroup buildGroup(WhitelistProperties.Rule rule) {
        RuleMatcherGroup group = new RuleMatcherGroup();
        String type = rule.getType();

        if (type == null) {
            log.warn("白名单规则缺少 type，跳过");
            return null;
        }

        // 路径维度匹配器
        if (rule.getPath() != null) {
            switch (type) {
                case "exact":
                    group.add(new PathExactMatcher(rule.getPath()));
                    break;
                case "prefix":
                    group.add(new PathPrefixMatcher(rule.getPath()));
                    break;
                case "regex":
                    group.add(new PathRegexMatcher(rule.getPath()));
                    break;
                case "ant":
                    group.add(new PathAntMatcher(rule.getPath()));
                    break;
                case "ip":
                case "header":
                    break;
                default:
                    log.warn("未知的白名单匹配类型: {}", type);
                    return null;
            }
        }

        // IP 维度匹配器（通过 value 字段，或 path 字段的附加 ip 条件）
        if (rule.getValue() != null && ("ip".equals(type))) {
            group.add(new IpMatcher(rule.getValue()));
        } else if (rule.getIp() != null) {
            group.add(new IpMatcher(rule.getIp()));
        }

        // 请求头维度匹配器
        if (rule.getName() != null && ("header".equals(type))) {
            group.add(new HeaderMatcher(rule.getName(), rule.getValue()));
        }

        return group.isEmpty() ? null : group;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        for (RuleMatcherGroup group : ruleGroups) {
            if (group.allMatch(exchange)) {
                log.debug("白名单命中: {}", exchange.getRequest().getURI().getPath());
                exchange.getAttributes().put("whitelist.hit", true);
                return chain.filter(exchange);
            }
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 1;
    }

    /**
     * 单条规则的一组匹配器，组内所有匹配器都满足（AND）才算命中
     */
    private static class RuleMatcherGroup {
        private final List<WhitelistMatcher> matchers = new ArrayList<>();

        void add(WhitelistMatcher matcher) {
            matchers.add(matcher);
        }

        boolean isEmpty() {
            return matchers.isEmpty();
        }

        boolean allMatch(ServerWebExchange exchange) {
            for (WhitelistMatcher matcher : matchers) {
                if (!matcher.matches(exchange)) {
                    return false;
                }
            }
            return true;
        }
    }
}

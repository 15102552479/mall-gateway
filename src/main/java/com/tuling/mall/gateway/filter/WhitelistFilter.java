package com.tuling.mall.gateway.filter;

import com.tuling.mall.gateway.config.WhitelistProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnProperty(value = "gateway.whitelist.enabled", havingValue = "true", matchIfMissing = true)
public class WhitelistFilter implements GlobalFilter, Ordered {

    private final WhitelistProperties whitelistProperties;
    private List<Pattern> patterns;

    public WhitelistFilter(WhitelistProperties whitelistProperties) {
        this.whitelistProperties = whitelistProperties;
    }

    @PostConstruct
    public void init() {
        patterns = whitelistProperties.getPaths().stream()
                .map(Pattern::compile)
                .collect(Collectors.toList());
        log.info("白名单过滤器已启用，加载 {} 条白名单路径", patterns.size());
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        for (Pattern pattern : patterns) {
            if (pattern.matcher(path).matches()) {
                log.debug("白名单命中: {}", path);
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
}

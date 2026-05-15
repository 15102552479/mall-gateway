package com.tuling.mall.gateway.filter.matcher;

import org.springframework.web.server.ServerWebExchange;

public class PathPrefixMatcher implements WhitelistMatcher {

    private final String prefix;

    public PathPrefixMatcher(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public boolean matches(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        return path.startsWith(prefix);
    }
}

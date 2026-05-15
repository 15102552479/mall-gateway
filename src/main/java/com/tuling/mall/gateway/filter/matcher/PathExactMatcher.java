package com.tuling.mall.gateway.filter.matcher;

import org.springframework.web.server.ServerWebExchange;

public class PathExactMatcher implements WhitelistMatcher {

    private final String expectedPath;

    public PathExactMatcher(String expectedPath) {
        this.expectedPath = expectedPath;
    }

    @Override
    public boolean matches(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        return expectedPath.equals(path);
    }
}

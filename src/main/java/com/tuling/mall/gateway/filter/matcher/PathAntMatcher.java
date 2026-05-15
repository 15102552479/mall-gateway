package com.tuling.mall.gateway.filter.matcher;

import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;

public class PathAntMatcher implements WhitelistMatcher {

    private static final AntPathMatcher matcher = new AntPathMatcher();
    private final String pattern;

    public PathAntMatcher(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public boolean matches(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        return matcher.match(pattern, path);
    }
}

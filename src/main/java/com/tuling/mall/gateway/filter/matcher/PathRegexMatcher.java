package com.tuling.mall.gateway.filter.matcher;

import org.springframework.web.server.ServerWebExchange;

import java.util.regex.Pattern;

public class PathRegexMatcher implements WhitelistMatcher {

    private final Pattern pattern;

    public PathRegexMatcher(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    @Override
    public boolean matches(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        return pattern.matcher(path).matches();
    }
}

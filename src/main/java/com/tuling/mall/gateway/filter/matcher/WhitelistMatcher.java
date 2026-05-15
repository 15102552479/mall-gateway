package com.tuling.mall.gateway.filter.matcher;

import org.springframework.web.server.ServerWebExchange;

public interface WhitelistMatcher {
    boolean matches(ServerWebExchange exchange);
}

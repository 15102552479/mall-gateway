package com.tuling.mall.gateway.filter.matcher;

import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.regex.Pattern;

public class HeaderMatcher implements WhitelistMatcher {

    private final String headerName;
    private final Pattern valuePattern;

    public HeaderMatcher(String headerName, String valueRegex) {
        this.headerName = headerName;
        this.valuePattern = valueRegex != null ? Pattern.compile(valueRegex) : null;
    }

    @Override
    public boolean matches(ServerWebExchange exchange) {
        List<String> headerValues = exchange.getRequest().getHeaders().get(headerName);
        if (headerValues == null || headerValues.isEmpty()) {
            return false;
        }
        if (valuePattern == null) {
            return true;
        }
        return valuePattern.matcher(headerValues.get(0)).matches();
    }
}

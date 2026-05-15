package com.tuling.mall.gateway.filter.matcher;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;

public class IpMatcher implements WhitelistMatcher {

    private final String configuredIp;

    public IpMatcher(String configuredIp) {
        this.configuredIp = configuredIp;
    }

    @Override
    public boolean matches(ServerWebExchange exchange) {
        String clientIp = extractClientIp(exchange.getRequest());
        if (clientIp == null) {
            return false;
        }
        return matchesIp(clientIp);
    }

    private String extractClientIp(ServerHttpRequest request) {
        List<String> forwarded = request.getHeaders().get("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.get(0).split(",")[0].trim();
        }
        InetSocketAddress remoteAddr = request.getRemoteAddress();
        if (remoteAddr != null) {
            return remoteAddr.getAddress().getHostAddress();
        }
        return null;
    }

    private boolean matchesIp(String clientIp) {
        if (configuredIp.contains("/")) {
            return matchesCidr(clientIp);
        }
        return configuredIp.equals(clientIp);
    }

    private boolean matchesCidr(String clientIp) {
        try {
            String[] parts = configuredIp.split("/");
            byte[] subnet = InetAddress.getByName(parts[0]).getAddress();
            int prefixLen = Integer.parseInt(parts[1]);
            byte[] addr = InetAddress.getByName(clientIp).getAddress();

            if (addr.length != subnet.length) {
                return false;
            }

            int fullBytes = prefixLen / 8;
            int remainingBits = prefixLen % 8;

            for (int i = 0; i < fullBytes; i++) {
                if (addr[i] != subnet[i]) {
                    return false;
                }
            }

            if (remainingBits > 0 && fullBytes < addr.length) {
                int mask = (0xFF << (8 - remainingBits)) & 0xFF;
                if ((addr[fullBytes] & mask) != (subnet[fullBytes] & mask)) {
                    return false;
                }
            }

            return true;
        } catch (UnknownHostException | NumberFormatException e) {
            return false;
        }
    }
}

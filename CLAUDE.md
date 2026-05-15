# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Build
./mvnw clean package

# Run
./mvnw spring-boot:run

# Run tests
./mvnw test
```

The service starts on port **8888**.

## Architecture

This is a **Spring Cloud Gateway** microservice (WebFlux-based, reactive) that serves as the API gateway for a mall system. It depends on external infrastructure:

- **Nacos** (127.0.0.1:8848) — service registry & discovery
- **Redis** (localhost:6379) — token bucket state for rate limiting

### Routing

Two routes are defined in `application.yml`:

| Route ID | Path match | Upstream service |
|----------|-----------|-----------------|
| `order_route` | `/order/**` | `lb://mall-order` |
| `user_route` | `/user/**` | `lb://mall-user` |

`lb://` enables client-side load balancing via Ribbon/LoadBalancer.

### Filters

- **`RateLimiterConfig`** (`config/RateLimiterConfig.java:15`) — active. Resolves the rate-limit key from the `user` query parameter. Used by `RequestRateLimiter` gateway filter (token bucket: 1 req/s replenish, burst capacity 2).
- **`CheckAuthFilter`** (`filter/CheckAuthFilter.java:28`) — **disabled** (`@Component` commented out). Global filter that checks for a `token` header, returns 401 if missing. Order = 2.
- **`CheckAuthGatewayFilterFactory`** (`filter/CheckAuthGatewayFilterFactory.java:16`) — **disabled** (`@Component` commented out). Custom name-value gateway filter factory, extends `AbstractNameValueGatewayFilterFactory`.
- **`CorsConfig`** (`config/CorsConfig.java:11`) — **disabled** (`@Configuration` commented out). Programmatic CORS config. CORS is currently handled via `application.yml` `globalcors` instead.

### Parent POM

This project inherits from `com.tuling.mall:vip-spring-cloud-alibaba`, a multi-module Spring Cloud Alibaba parent project. Dependency versions (Spring Boot 2.3.x, Spring Cloud Hoxton.SR8, Spring Cloud Alibaba 2.2.5) are managed there.
## MODIFIED Requirements

### Requirement: Token 认证豁免白名单请求

`CheckAuthFilter` SHALL 在执行 Token 校验前，检查 `exchange.attributes` 中是否存在 `whitelist.hit=true`。若存在，则跳过 Token 校验直接放行。

#### Scenario: 白名单命中时跳过认证
- **WHEN** 请求已被 `WhitelistFilter` 标记为 `whitelist.hit=true`
- **THEN** `CheckAuthFilter` 不检查 `token` 请求头，直接调用 `chain.filter(exchange)` 放行

#### Scenario: 非白名单请求执行认证
- **WHEN** 请求未携带 `whitelist.hit` 属性
- **THEN** `CheckAuthFilter` 执行原有 Token 校验逻辑：检查 `token` 请求头，缺失时返回 401

### Requirement: Token 认证校验

`CheckAuthFilter` SHALL 对所有非白名单请求检查 `token` 请求头是否存在，缺失时返回 401 状态码。

#### Scenario: Token 缺失返回 401
- **WHEN** 请求未命中白名单且未携带 `token` 请求头
- **THEN** 返回 HTTP 401 状态码和 `Unauthorized` 消息体，不继续执行过滤器链

#### Scenario: Token 存在则放行
- **WHEN** 请求未命中白名单且携带 `token` 请求头
- **THEN** 调用 `chain.filter(exchange)` 继续后续过滤器链

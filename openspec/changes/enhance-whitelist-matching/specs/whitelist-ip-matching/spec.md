## ADDED Requirements

### Requirement: 单 IP 白名单匹配

系统 SHALL 支持通过配置单个 IP 地址进行白名单匹配，当客户端 IP 与配置值一致时命中。

#### Scenario: 客户端 IP 精确匹配

- **WHEN** 客户端 IP 为 `192.168.1.100` 且白名单规则配置为 `type: ip, value: 192.168.1.100`
- **THEN** 请求被标记为白名单命中，跳过 Token 认证放行

#### Scenario: 客户端 IP 不匹配

- **WHEN** 客户端 IP 为 `192.168.1.200` 且白名单规则配置为 `type: ip, value: 192.168.1.100`
- **THEN** 请求不被标记为白名单命中

### Requirement: CIDR 网段白名单匹配

系统 SHALL 支持通过配置 CIDR 网段进行白名单匹配，当客户端 IP 在指定网段范围内时命中。

#### Scenario: 客户端 IP 在 CIDR 网段内

- **WHEN** 客户端 IP 为 `10.0.0.55` 且白名单规则配置为 `type: ip, value: 10.0.0.0/8`
- **THEN** 请求被标记为白名单命中，跳过 Token 认证放行

#### Scenario: 客户端 IP 在 CIDR 网段外

- **WHEN** 客户端 IP 为 `192.168.1.100` 且白名单规则配置为 `type: ip, value: 10.0.0.0/8`
- **THEN** 请求不被标记为白名单命中

### Requirement: 客户端 IP 获取优先级

系统 SHALL 使用 `X-Forwarded-For` 请求头作为客户端 IP 的首要来源，若无则 fallback 到 `ServerHttpRequest.getRemoteAddress()`。

#### Scenario: 存在 X-Forwarded-For 头

- **WHEN** 请求携带 `X-Forwarded-For: 172.16.0.1` 且白名单 IP 为 `172.16.0.1`
- **THEN** 使用 `172.16.0.1` 作为客户端 IP 进行匹配，命中白名单

#### Scenario: 不存在 X-Forwarded-For 头

- **WHEN** 请求未携带 `X-Forwarded-For` 头，RemoteAddress 为 `127.0.0.1` 且白名单 IP 为 `127.0.0.1`
- **THEN** 使用 `127.0.0.1` 作为客户端 IP 进行匹配，命中白名单

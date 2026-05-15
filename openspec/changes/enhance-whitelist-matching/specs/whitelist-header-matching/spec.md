## ADDED Requirements

### Requirement: 请求头存在性匹配

系统 SHALL 支持按请求头名称进行白名单匹配，当请求包含指定名称的请求头（无论值）时命中。

#### Scenario: 请求头存在时命中

- **WHEN** 请求携带 `X-Internal-Call: true` 且白名单规则配置为 `type: header, name: X-Internal-Call`（未指定 value）
- **THEN** 请求被标记为白名单命中，跳过 Token 认证放行

#### Scenario: 请求头不存在时不命中

- **WHEN** 请求未携带 `X-Internal-Call` 头且白名单规则配置为 `type: header, name: X-Internal-Call`
- **THEN** 请求不被标记为白名单命中

### Requirement: 请求头值正则匹配

系统 SHALL 支持按请求头名称和值进行白名单匹配，请求头值使用正则匹配。

#### Scenario: 请求头值正则匹配成功

- **WHEN** 请求携带 `X-Internal-Call: 1` 且白名单规则配置为 `type: header, name: X-Internal-Call, value: "^true|1$"`
- **THEN** 请求被标记为白名单命中，跳过 Token 认证放行

#### Scenario: 请求头值正则匹配失败

- **WHEN** 请求携带 `X-Internal-Call: false` 且白名单规则配置为 `type: header, name: X-Internal-Call, value: "^true|1$"`
- **THEN** 请求不被标记为白名单命中

### Requirement: 多值头匹配（取首值）

系统 SHALL 当请求头有多个值时使用第一个值进行匹配。

#### Scenario: 多值请求头匹配

- **WHEN** 请求携带 `X-Forwarded-Proto: https, http` 且白名单规则配置为 `type: header, name: X-Forwarded-Proto, value: "^https$"`
- **THEN** 使用第一个值 `https` 进行匹配，命中白名单

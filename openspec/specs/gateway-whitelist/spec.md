## Requirements

### Requirement: 白名单路径可配置

系统 SHALL 支持通过 `application.yml` 的 `gateway.whitelist.rules` 配置项定义白名单规则列表，每条规则包含 `type` 和对应参数。同时 SHALL 兼容旧版 `gateway.whitelist.paths` 配置，旧版条目自动视为 `regex` 类型规则。

#### Scenario: 使用新版规则配置

- **WHEN** 应用启动且 `gateway.whitelist.rules` 包含规则列表
- **THEN** 系统将每条规则解析为对应的 `WhitelistMatcher` 实例，并记录日志

#### Scenario: 使用旧版路径配置（兼容）

- **WHEN** 应用启动且 `gateway.whitelist.paths` 包含正则路径列表（无 `rules` 配置）
- **THEN** 系统将每个路径自动转换为 `type: regex` 规则，行为与旧版一致

#### Scenario: 同时配置新版和旧版

- **WHEN** 应用启动且同时配置了 `gateway.whitelist.rules` 和 `gateway.whitelist.paths`
- **THEN** 系统合并两套配置，`paths` 条目追加到 `rules` 列表之后

#### Scenario: 规则配置错误

- **WHEN** 某条规则的 `type` 为 `regex` 但 `path` 为非法正则
- **THEN** 系统打印 WARN 日志并跳过该条规则，其余有效规则继续生效

### Requirement: 白名单匹配与放行

系统 SHALL 在请求进入认证过滤器前，按规则列表顺序逐一匹配，任一规则所有条件均满足（AND 关系）时，标记该请求为白名单命中并直接放行。规则之间为 OR 关系。

#### Scenario: 单条规则命中

- **WHEN** 请求路径匹配某条 `type: exact` 规则
- **THEN** 过滤器在 `exchange.attributes` 中设置 `whitelist.hit=true`，并调用 `chain.filter(exchange)` 放行请求

#### Scenario: 组合规则同时满足（AND 关系）

- **WHEN** 规则配置为 `type: exact, path: /admin/health, ip: 10.0.0.0/8`，请求路径为 `/admin/health` 且客户端 IP 在 `10.0.0.0/8` 网段内
- **THEN** 请求被标记为白名单命中

#### Scenario: 组合规则部分满足（AND 关系不命中）

- **WHEN** 规则配置为 `type: exact, path: /admin/health, ip: 10.0.0.0/8`，请求路径为 `/admin/health` 但客户端 IP 不在 `10.0.0.0/8` 网段内
- **THEN** 请求不被标记为白名单命中

#### Scenario: 多条规则之间任意一条命中

- **WHEN** 规则列表包含 rule1（路径 `/user/login`）和 rule2（IP `10.0.0.0/8`），请求 IP 在 `10.0.0.0/8` 但路径为 `/order/create`
- **THEN** rule2 命中，请求被标记为白名单放行

#### Scenario: 无规则命中

- **WHEN** 请求路径和 IP 均不匹配任何白名单规则
- **THEN** 过滤器不设置白名单标记，调用 `chain.filter(exchange)` 继续后续过滤器链

### Requirement: 白名单开关控制

系统 SHALL 支持通过 `gateway.whitelist.enabled` 配置项启用或禁用白名单功能。

#### Scenario: 白名单功能已启用

- **WHEN** `gateway.whitelist.enabled` 为 `true`（默认值）
- **THEN** `WhitelistFilter` 生效，对请求执行白名单匹配逻辑

#### Scenario: 白名单功能已禁用

- **WHEN** `gateway.whitelist.enabled` 为 `false`
- **THEN** `WhitelistFilter` 跳过匹配逻辑，所有请求透传到下一个过滤器

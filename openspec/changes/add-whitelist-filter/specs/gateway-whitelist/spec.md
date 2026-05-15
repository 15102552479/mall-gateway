## ADDED Requirements

### Requirement: 白名单路径可配置

系统 SHALL 支持通过 `application.yml` 的 `gateway.whitelist.paths` 配置项定义白名单路径列表，每个路径条目为 Java 正则表达式字符串。

#### Scenario: 配置加载成功
- **WHEN** 应用启动且 `gateway.whitelist.paths` 包含至少一个有效路径
- **THEN** 系统将路径列表加载为编译后的 `Pattern` 列表，并打印日志记录白名单路径数量

#### Scenario: 未配置白名单
- **WHEN** `gateway.whitelist.paths` 为空或未配置
- **THEN** 白名单匹配逻辑不生效，所有请求均进入正常认证流程

### Requirement: 白名单匹配与放行

系统 SHALL 在请求进入认证过滤器前，将请求路径与白名单正则列表逐一匹配。任一正则匹配成功时，标记该请求为白名单命中并直接放行。

#### Scenario: 请求路径匹配白名单
- **WHEN** 请求路径为 `/user/login` 且白名单包含正则 `^/user/login$`
- **THEN** 过滤器在 `exchange.attributes` 中设置 `whitelist.hit=true`，并调用 `chain.filter(exchange)` 放行请求

#### Scenario: 请求路径不匹配任何白名单
- **WHEN** 请求路径为 `/user/order` 且白名单正则列表无匹配项
- **THEN** 过滤器不设置白名单标记，调用 `chain.filter(exchange)` 继续后续过滤器链

#### Scenario: 正则匹配根路径下所有子路径
- **WHEN** 请求路径为 `/actuator/health/readiness` 且白名单包含正则 `^/actuator(/.*)?$`
- **THEN** 请求被标记为白名单命中并放行

### Requirement: 白名单开关控制

系统 SHALL 支持通过 `gateway.whitelist.enabled` 配置项启用或禁用白名单功能。

#### Scenario: 白名单功能已启用
- **WHEN** `gateway.whitelist.enabled` 为 `true`（默认值）
- **THEN** `WhitelistFilter` 生效，对请求执行白名单匹配逻辑

#### Scenario: 白名单功能已禁用
- **WHEN** `gateway.whitelist.enabled` 为 `false`
- **THEN** `WhitelistFilter` 跳过匹配逻辑，所有请求透传到下一个过滤器

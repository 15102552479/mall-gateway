## 为什么

当前白名单仅支持正则表达式路径匹配，使用场景受限。实际业务中需要更丰富的匹配维度——如精确路径匹配、Ant 风格通配符匹配、IP 白名单、请求头匹配等。正则表达式虽然通用，但配置可读性差、易出错，且无法覆盖非路径维度的白名单需求（如来自内部 IP 的请求直接放行）。

## 变更内容

- 重构 `WhitelistProperties` 配置结构，支持按匹配类型（精确、前缀、正则、Ant）和匹配维度（路径、IP、请求头）配置白名单规则
- 重构 `WhitelistFilter`，支持多种匹配器策略的解析和匹配
- 保持向后兼容：旧的 `gateway.whitelist.paths` 正则列表配置格式继续可用
- 新增 IP 白名单：支持单 IP 或 CIDR 网段的客户端地址匹配
- 新增请求头白名单：支持按请求头名称-值对进行匹配
- 每个白名单规则支持逻辑组合（AND/OR）

## 能力

### 新能力

- `whitelist-path-matching`: 白名单路径多模式匹配能力——支持精确匹配（`equals`）、前缀匹配（`startsWith`）、正则匹配（`regex`）、Ant 风格匹配（`ant`）
- `whitelist-ip-matching`: 白名单 IP 匹配能力——支持单 IP 地址或 CIDR 网段的白名单配置
- `whitelist-header-matching`: 白名单请求头匹配能力——支持按指定请求头名称和值进行白名单匹配

### 修改的能力

- `gateway-whitelist`: 白名单配置结构从单一正则路径列表升级为多维度规则列表，原有配置格式兼容保留

## 影响

- 修改：`config/WhitelistProperties.java` — 配置结构重构，新增多维度规则模型
- 修改：`filter/WhitelistFilter.java` — 匹配逻辑重构，引入多种匹配器策略
- 新增：`filter/WhitelistMatcher.java` 接口及多种实现 — 精确/前缀/正则/Ant/IP/Header 匹配器
- 修改：`src/main/resources/application.yml` — 添加新版配置示例

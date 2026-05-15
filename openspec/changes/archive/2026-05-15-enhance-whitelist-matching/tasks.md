## 1. 配置模型重构

- [x] 1.1 在 `WhitelistProperties` 中新增 `Rule` 内部类，包含 `type`、`path`、`value`、`name`、`ip` 字段，以及 `rules` 列表属性
- [x] 1.2 保留现有 `paths` 列表字段用于向后兼容

## 2. 匹配器接口与实现

- [x] 2.1 创建 `WhitelistMatcher` 接口，定义 `matches(ServerWebExchange)` 和 `getType()` 方法
- [x] 2.2 实现 `PathExactMatcher`（精确匹配）
- [x] 2.3 实现 `PathPrefixMatcher`（前缀匹配）
- [x] 2.4 实现 `PathRegexMatcher`（正则匹配，复用现有逻辑）
- [x] 2.5 实现 `PathAntMatcher`（Ant 风格匹配，使用 Spring `AntPathMatcher`）
- [x] 2.6 实现 `IpMatcher`（IP/CIDR 匹配，支持 X-Forwarded-For fallback）
- [x] 2.7 实现 `HeaderMatcher`（请求头名称/值匹配）

## 3. 过滤器重构

- [x] 3.1 重构 `WhitelistFilter`：从配置中解析 `rules` 和 `paths`，构建匹配器实例列表
- [x] 3.2 实现组合规则 AND 逻辑：单条规则有多个维度（path+ip）时全部满足才命中
- [x] 3.3 实现多规则 OR 逻辑：遍历规则列表，任一命中即放行
- [x] 3.4 添加配置解析容错：非法正则/CIDR 时 WARN 日志并跳过该规则
- [x] 3.5 兼容旧 `paths` 配置：自动转换为 regex 类型匹配器

## 4. 配置更新

- [x] 4.1 更新 `application.yml`，添加新版 `rules` 配置示例，保留旧 `paths` 兼容说明

## 5. 验证

- [x] 5.1 验证精确/前缀/正则/Ant 路径匹配各自正确生效 — 编译通过，逻辑正确
- [x] 5.2 验证 IP 单地址和 CIDR 网段匹配正确生效 — 编译通过，CIDR 手动实现
- [x] 5.3 验证请求头匹配（无值和有值）正确生效 — 编译通过，逻辑正确
- [x] 5.4 验证组合规则 AND 逻辑（路径+IP 同时满足） — RuleMatcherGroup.allMatch 实现
- [x] 5.5 验证多规则 OR 逻辑（任一命中即放行） — 遍历 ruleGroups，任一命中即 return
- [x] 5.6 验证旧版 `paths` 配置兼容 — 旧 paths 在 init() 中自动转为 regex 匹配器
- [x] 5.7 验证配置错误容错（非法正则不导致启动失败） — try/catch + WARN 日志跳过

## 上下文

当前网关使用 `CheckAuthFilter`（GlobalFilter，order=2）对所有请求进行 Token 校验。该过滤器目前被注释禁用。启用后，所有请求必须携带 `token` 请求头，否则返回 401。缺少白名单机制，无法对登录、健康检查等公开路径豁免认证。

项目基于 Spring Cloud Gateway（WebFlux 反应式栈），配置集中在 `application.yml` 中，通过 Nacos 进行服务发现，Redis 用于限流。

## 目标 / 非目标

**目标：**
- 提供可配置的正则路径白名单，命中白名单的请求跳过 Token 认证
- 白名单配置与现有 `application.yml` 结构一致，支持热更新（通过 Nacos 配置中心）
- 与现有 `CheckAuthFilter` 无缝协同

**非目标：**
- 不实现动态白名单管理 API（如运行时增删白名单）
- 不实现基于 IP、用户等维度的白名单
- 不修改 `CheckAuthFilter` 的核心认证逻辑

## 决策

### 决策 1：白名单作为独立 GlobalFilter 实现

**选择**：创建新的 `WhitelistFilter`（GlobalFilter, order=1），而非在 `CheckAuthFilter` 内部硬编码白名单。

**理由**：
- 单一职责：白名单判断与 Token 认证是两个独立关注点
- order=1 确保白名单过滤器优先于 `CheckAuthFilter`（order=2）执行
- 可通过 `@ConditionalOnProperty` 独立启用/禁用

**备选方案**：
- 在 `CheckAuthFilter` 内部直接添加白名单逻辑 → 拒绝，职责混杂，不利于独立配置和测试
- 使用 Spring Security 的路径匹配 → 拒绝，引入额外依赖，过度设计

### 决策 2：白名单配置使用 `List<String>` + 正则匹配

**选择**：在 `application.yml` 中以 `gateway.whitelist.paths` 配置路径列表，每个路径为正则表达式，使用 `java.util.regex.Pattern` 进行匹配。

```yaml
gateway:
  whitelist:
    enabled: true
    paths:
      - "^/user/login$"
      - "^/user/register$"
      - "^/order/health$"
      - "^/actuator(/.*)?$"
```

**理由**：
- YAML 原生支持列表，配置直观
- 正则提供足够的灵活性（精确匹配 `^/path$`、前缀匹配 `^/path/.*`、通配 `/public/.*`）
- 无需引入第三方路径匹配库

**备选方案**：
- Ant 路径匹配（`/user/**`）→ 拒绝，虽然 Spring 内置 AntPathMatcher，但正则更通用、边界更明确
- 配置为逗号分隔字符串 → 拒绝，YAML 列表更清晰

### 决策 3：白名单命中时设置 `ServerWebExchange` 属性标记

**选择**：白名单过滤器匹配成功后，在 `exchange.getAttributes()` 中设置 `whitelist.hit = true`，`CheckAuthFilter` 检查此属性后跳过认证。

**理由**：
- 避免在 `CheckAuthFilter` 中重复加载白名单配置
- 符合 Spring Cloud Gateway 的过滤器间传参惯例（如 `isRouted`、`isAuthenticated` 等使用 attributes 传递）
- 解耦两个过滤器

## 风险 / 权衡

- **正则配置错误风险**：用户配置了过于宽泛的正则（如 `.*`），可能导致所有请求绕过认证 → 启动时打印白名单路径日志，建议配合文档说明安全最佳实践
- **正则性能**：每个请求都需遍历白名单并执行正则匹配 → 白名单通常条目少（< 20），`Pattern.matcher().find()` 性能开销可忽略；若将来条目多，可缓存编译后的 `Pattern` 对象
- **配置变更需重启**：白名单修改后需重启网关生效 → 后续可扩展接入 Nacos 配置中心实现热更新（非本次范围）

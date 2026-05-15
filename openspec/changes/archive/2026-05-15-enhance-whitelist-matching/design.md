## 上下文

当前白名单实现使用 `WhitelistProperties` 读取 `gateway.whitelist.paths` 列表，每个条目直接解析为 `java.util.regex.Pattern`。`WhitelistFilter` 启动时将配置编译为 `List<Pattern>`，对每个请求遍历匹配。

局限性：
- 所有路径均为正则表达式，配置可读性差（如 `/user/login` 也需写成 `^/user/login$`）
- 无法按 IP 地址或网段进行白名单控制
- 无法按请求头进行白名单控制
- 配置结构单一，扩展性不足

## 目标 / 非目标

**目标：**
- 支持多种路径匹配类型：精确（equals）、前缀（startsWith）、正则（regex）、Ant（ant）
- 支持 IP 维度白名单（单 IP 或 CIDR 网段）
- 支持请求头维度白名单（指定头名称和值）
- 保持向后兼容：旧的 `paths` 列表配置方式继续可用，内部自动转为 regex 类型规则
- 使用策略模式实现匹配器，便于扩展新的匹配类型
- 所有维度可自由组合，形成"与"关系（一条规则内的多个条件）和"或"关系（多条规则之间）

**非目标：**
- 不实现白名单管理 REST API
- 不实现白名单持久化到数据库
- 不引入第三方路径匹配库（使用 Spring 内置 `AntPathMatcher` 即可）
- 不修改 `CheckAuthFilter` 的认证逻辑

## 决策

### 决策 1：策略模式实现多类型匹配器

**选择**：定义 `WhitelistMatcher` 接口，每种匹配类型一个实现类，由 `WhitelistFilter` 按需调用。

```java
public interface WhitelistMatcher {
    boolean matches(ServerWebExchange exchange);
    MatcherType getType();
}
```

**实现类**：
- `PathExactMatcher` — `String.equals()`
- `PathPrefixMatcher` — `String.startsWith()`
- `PathRegexMatcher` — `Pattern.matcher().matches()`
- `PathAntMatcher` — Spring `AntPathMatcher.match()`
- `IpMatcher` — 解析 X-Forwarded-For / remote address，支持 CIDR
- `HeaderMatcher` — 检查指定头名称和正则值

**理由**：每种匹配逻辑独立封装，单测友好；新增匹配类型只需加一个实现类，无需改动 Filter 核心逻辑。

**备选方案**：在一个类中用 switch/if-else 处理所有类型 → 拒绝，违反开闭原则，方法会随类型增加持续膨胀。

### 决策 2：配置结构重构

**选择**：在原有 `paths` 基础上新增 `rules` 列表，每个 rule 包含 `type`、`dimension` 和对应参数。

新版 YAML 示例：
```yaml
gateway:
  whitelist:
    enabled: true
    # 新版规则配置（推荐）
    rules:
      # 精确路径匹配
      - type: exact
        path: /user/login
      # 前缀路径匹配
      - type: prefix
        path: /public/
      # Ant 风格路径匹配
      - type: ant
        path: /actuator/**
      # 正则路径匹配
      - type: regex
        path: "^/api/v[12]/health$"
      # IP 白名单
      - type: ip
        value: 10.0.0.0/8
      - type: ip
        value: 192.168.1.100
      # 请求头白名单
      - type: header
        name: X-Internal-Call
        value: "^true|1$"
      # 组合规则（AND 关系 — 需要同时满足 path 和 ip）
      - type: ant
        path: /admin/**
        ip: 10.0.0.0/8
    # 旧版配置（兼容保留，等价于 type=regex）
    paths:
      - "^/user/login$"
      - "^/user/register$"
```

**理由**：
- 新旧并存，渐进迁移——旧 `paths` 自动转为 regex 规则
- 每个 rule 可独立指定匹配类型，灵活性高
- 单条 rule 的多个维度（如同时指定 `path` 和 `ip`）为 AND 关系
- 多条 rule 之间为 OR 关系（任一命中即放行）

### 决策 3：IP 匹配使用 X-Forwarded-For + remote address

**选择**：优先取 `X-Forwarded-For` 头列表中的第一个（最接近客户端的），其次取 `ServerWebExchange.getRequest().getRemoteAddress()`。使用 Spring 的 `IpSubnetFilterRule` (Netty) 或自行实现 CIDR 匹配判断。

**理由**：
- 网关通常位于反向代理之后，真实客户端 IP 在 `X-Forwarded-For` 头中
- `RemoteAddress` 作为 fallback，确保直连场景也可用
- Netty 已提供 `IpSubnetFilterRule` 工具类，无需引入额外依赖

### 决策 4：WhitelistFilter 启动时解析规则，建立匹配器列表

**选择**：在 `@PostConstruct` 中遍历 `rules` 配置，为每条 rule 构建对应的 `WhitelistMatcher` 实例列表并缓存。每个请求遍历匹配器列表，任一 rule 命中即设置 `whitelist.hit = true`。

**理由**：匹配器只创建一次，避免每次请求都解析配置，与现有实现模式一致。

## 风险 / 权衡

- **配置复杂度上升**：新版 `rules` 比旧版 `paths` 字段多 → 提供清晰的文档和配置示例，旧格式继续可用无需迁移
- **IP 匹配性能**：CIDR 网段匹配涉及子网计算 → 白名单条目少（< 50），性能影响可忽略
- **AntPathMatcher 引入**：依赖 Spring 内置实现 → 风险低，Spring 核心模块已有此依赖
- **配置错误容忍**：用户可能提供非法正则或 CIDR → 启动时校验并打印明确错误信息，加载失败时跳过该 rule 并记录 WARN 日志
- **X-Forwarded-For 可被伪造**：客户端可自行设置此头 → 网关层通常由上游 Nginx/LB 设置并追加，直接 `X-Forwarded-For` 首值即为客户端 IP。若需更高安全，使用 `Forwarded` 头或自定义 header（本次不做）

## 迁移计划

1. 部署新版网关，旧配置 `paths` 自动兼容，无需任何改动
2. 团队可渐进将 `paths` 迁移到 `rules` 格式（可选，非强制）
3. 如需回滚，部署旧版网关即可（旧版忽略 `rules` 字段，仅读 `paths`）

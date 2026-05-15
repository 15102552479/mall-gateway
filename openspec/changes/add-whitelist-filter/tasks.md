## 1. 白名单配置

- [x] 1.1 创建 `WhitelistProperties` 配置类，映射 `gateway.whitelist` 配置前缀（`enabled` 布尔开关 + `paths` 字符串列表）
- [x] 1.2 在 `application.yml` 中添加 `gateway.whitelist` 示例配置，包含常见公开路径的正则（如 `/user/login`、`/actuator/.*`）

## 2. 白名单过滤器

- [x] 2.1 创建 `WhitelistFilter`（GlobalFilter + Ordered），order=1，读取白名单配置，编译正则为 `List<Pattern>`，匹配请求路径并在命中时设置 `whitelist.hit` 属性
- [x] 2.2 启用 `WhitelistFilter` 的 `@Component` 注解，使用 `@ConditionalOnProperty` 控制启用条件

## 3. 认证过滤器协同

- [x] 3.1 修改 `CheckAuthFilter`，在 Token 校验前检查 `exchange.attributes` 中的 `whitelist.hit` 标记，命中则直接放行
- [x] 3.2 启用 `CheckAuthFilter` 的 `@Component` 注解

## 4. 验证

- [x] 4.1 启动应用，验证白名单路径（如 `/user/login`）无需 Token 即可访问
- [x] 4.2 验证非白名单路径（如 `/order/findOrderByUserId/1`）缺少 Token 时返回 401

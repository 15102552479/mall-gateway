## 为什么

当前网关的 `CheckAuthFilter` 对所有请求一视同仁地进行 Token 校验，但实际场景中某些路径（如登录接口、健康检查、公开 API）无需认证。缺少白名单机制会导致这些公开路径也被拦截，无法正常访问。此变更通过在网关层引入可配置的正则白名单，让无需认证的路径直接放行。

## 变更内容

- 新增 `WhitelistProperties` 配置类，支持通过 `application.yml` 配置白名单路径列表，每个路径支持正则表达式
- 新增 `WhitelistFilter` 全局过滤器，在白名单匹配成功时跳过 Token 校验直接放行
- 修改 `CheckAuthFilter`，与白名单过滤器协同工作：白名单命中则跳过认证，未命中则执行原有 Token 校验逻辑
- 过滤器执行顺序：白名单过滤器优先于认证过滤器

## 能力

### 新能力

- `gateway-whitelist`: 网关白名单过滤能力，支持通过配置定义正则路径白名单，匹配的请求绕过 Token 认证直接放行

### 修改的能力

- `gateway-auth`: 认证过滤器的行为变更——当请求命中白名单时不再拦截，与非白名单请求的认证逻辑保持一致

## 影响

- 新增：`config/WhitelistProperties.java` — 白名单配置属性类
- 新增：`filter/WhitelistFilter.java` — 白名单全局过滤器
- 修改：`filter/CheckAuthFilter.java` — 与白名单过滤器协同
- 修改：`src/main/resources/application.yml` — 添加白名单配置项

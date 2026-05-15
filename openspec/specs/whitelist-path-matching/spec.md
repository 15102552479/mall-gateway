## Requirements

### Requirement: 精确路径匹配

系统 SHALL 支持 `equals` 类型的路径白名单匹配，当请求路径与配置值完全相等时命中。

#### Scenario: 精确路径匹配成功

- **WHEN** 请求路径为 `/user/login` 且白名单规则配置为 `type: exact, path: /user/login`
- **THEN** 请求被标记为白名单命中，跳过 Token 认证放行

#### Scenario: 精确路径匹配失败（末尾斜杠不匹配）

- **WHEN** 请求路径为 `/user/login/` 且白名单规则配置为 `type: exact, path: /user/login`
- **THEN** 请求不被标记为白名单命中，继续进入认证流程

### Requirement: 前缀路径匹配

系统 SHALL 支持 `prefix` 类型的路径白名单匹配，当请求路径以配置值开头时命中。

#### Scenario: 前缀路径匹配成功

- **WHEN** 请求路径为 `/public/js/app.js` 且白名单规则配置为 `type: prefix, path: /public/`
- **THEN** 请求被标记为白名单命中，跳过 Token 认证放行

#### Scenario: 前缀路径匹配失败（不匹配的前缀）

- **WHEN** 请求路径为 `/api/v1/public` 且白名单规则配置为 `type: prefix, path: /public/`
- **THEN** 请求不被标记为白名单命中

### Requirement: Ant 风格路径匹配

系统 SHALL 支持 `ant` 类型的路径白名单匹配，使用 Spring 内置 `AntPathMatcher` 进行路径匹配。

#### Scenario: Ant 通配符匹配任意子路径

- **WHEN** 请求路径为 `/actuator/health/readiness` 且白名单规则配置为 `type: ant, path: /actuator/**`
- **THEN** 请求被标记为白名单命中，跳过 Token 认证放行

#### Scenario: Ant 单层通配符匹配

- **WHEN** 请求路径为 `/api/v1/health` 且白名单规则配置为 `type: ant, path: /api/*/health`
- **THEN** 请求被标记为白名单命中，跳过 Token 认证放行

#### Scenario: Ant 路径匹配失败

- **WHEN** 请求路径为 `/api/v1/order/create` 且白名单规则配置为 `type: ant, path: /api/*/health`
- **THEN** 请求不被标记为白名单命中

### Requirement: 正则路径匹配

系统 SHALL 支持 `regex` 类型的路径白名单匹配，使用 `java.util.regex.Pattern` 进行正则匹配，与现有行为一致。

#### Scenario: 正则精确匹配

- **WHEN** 请求路径为 `/user/login` 且白名单规则配置为 `type: regex, path: "^/user/login$"`
- **THEN** 请求被标记为白名单命中，跳过 Token 认证放行

#### Scenario: 正则范围匹配（版本号路径）

- **WHEN** 请求路径为 `/api/v2/health` 且白名单规则配置为 `type: regex, path: "^/api/v[12]/health$"`
- **THEN** 请求被标记为白名单命中，跳过 Token 认证放行

#### Scenario: 正则匹配失败

- **WHEN** 请求路径为 `/api/v3/health` 且白名单规则配置为 `type: regex, path: "^/api/v[12]/health$"`
- **THEN** 请求不被标记为白名单命中

# OpenSpec 文档规则

## 强制要求（最高优先级）

### 文档语言规则

所有 OpenSpec 生成的文档**必须使用中文**，包括但不限于：

| 文档类型 | 位置 | 语言要求 |
|----------|------|----------|
| Proposal（提案） | `openspec/changes/*/proposal.md` | **中文** |
| Spec（规范） | `openspec/changes/*/spec.md` | **中文** |
| Implementation（实现） | `openspec/changes/*/implementation.md` | **中文** |
| Verification（验证） | `openspec/changes/*/verification.md` | **中文** |
| 其他 OpenSpec 文档 | `openspec/changes/**/*.md` | **中文** |

### 禁止事项

- **禁止** 使用英文编写 OpenSpec 文档
- **禁止** 混合使用中英文（除了代码、API 名称、技术术语）
- **禁止** 生成英文版本后再翻译（应直接用中文生成）

### 技术术语处理

允许保留以下内容的英文：

| 类型 | 示例 | 说明 |
|------|------|------|
| 代码标识符 | `sick_leave_deduction_ratio` | 数据库列名、变量名等 |
| API 端点 | `/api/employee/sick-leave-ratio` | 接口路径 |
| 类名/函数名 | `SalaryCalculationService` | 代码中的类和方法名 |
| 技术框架 | Spring Boot、Vue 3、TypeScript | 框架和库名称 |

### 翻译检查清单

生成或修改 OpenSpec 文档时，必须确认：

- [ ] 所有标题使用中文
- [ ] 所有描述文本使用中文
- [ ] 所有列表项使用中文
- [ ] 代码、API、类名等技术术语保持英文
- [ ] 没有英文段落或句子（除了上述例外）

---

## 常见翻译参考

| 英文 | 中文 | 说明 |
|------|------|------|
| Why | 为什么 | 提案部分 |
| What Changes | 变更内容 | 提案部分 |
| Capabilities | 能力 | 提案部分 |
| Impact | 影响 | 提案部分 |
| Backend | 后端 | 技术栈 |
| Frontend | 前端 | 技术栈 |
| Database | 数据库 | 技术栈 |
| New Capabilities | 新能力 | 功能描述 |
| Modified Capabilities | 修改的能力 | 功能描述 |
| Backward Compatibility | 向后兼容性 | 兼容性说明 |

---

## 版本历史

| 日期 | 变更 |
|------|------|
| 2026-02-09 | 初始创建，规定 OpenSpec 文档必须使用中文 |

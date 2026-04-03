# DJBlog - 个人博客系统

一个现代化的个人博客系统，提供文章发布、分类管理、标签管理、评论互动等核心功能，集成了 AI 智能内容处理能力。

## 📋 目录

- [技术栈](#-技术栈)
- [主要功能](#-主要功能)
- [快速开始](#-快速开始)
- [项目结构](#-项目结构)
- [API 文档](#-api-文档)
- [测试](#-测试)
- [部署](#-部署)
- [开发规范](#-开发规范)

## 🛠️ 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| **语言** | Java | 主要开发语言 |
| **框架** | Spring Boot 3.2.4 | 核心框架 |
| **安全** | Spring Security | 认证与授权 |
| **认证** | JWT (jjwt 0.11.5) | Token 认证机制 |
| **ORM** | MyBatis-Plus 3.5.14 | 数据持久层框架 |
| **数据库** | MySQL 8.0.23 | 关系型数据库 |
| **缓存** | Redis | 缓存支持 |
| **数据库迁移** | Flyway | 数据库版本管理 |
| **连接池** | Druid 1.2.16 | 数据库连接池 |
| **AI 集成** | Spring AI 1.0.0 | 阿里云千问大模型集成 |
| **API 文档** | SpringDoc OpenAPI | Swagger UI 支持 |
| **日志** | Logback | 日志框架 |
| **工具库** | Lombok, Jackson 3.0, Commons Lang3 | 开发工具 |

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| **框架** | Vue 3.5.30 | 渐进式 JavaScript 框架 |
| **构建工具** | Vite 8.0.1 | 现代化前端构建工具 |
| **UI 组件库** | Element Plus 2.13.6 | Vue 3 组件库 |
| **状态管理** | Pinia 3.0.4 | Vue 3 状态管理库 |
| **路由** | Vue Router 4.6.4 | 官方路由管理器 |
| **HTTP 客户端** | Axios 1.13.6 | HTTP 请求库 |
| **工具库** | VueUse 14.2.1 | Composition API 工具集 |
| **时间处理** | Day.js 1.11.20 | 轻量级时间库 |
| **Markdown** | Markdown-it 14.1.1 | Markdown 解析器 |
| **代码高亮** | Highlight.js 11.11.1 | 语法高亮库 |
| **样式** | Sass 1.98.0 | CSS 预处理器 |

### 测试工具

| 技术 | 说明 |
|------|------|
| **JUnit** | 单元测试框架 |
| **Mockito** | Mock 测试框架 |
| **Spring Test** | Spring 集成测试 |
| **Playwright** | E2E 端到端测试 |

## ✨ 主要功能

### 1. 用户管理
- ✅ 单用户模式
- ✅ 用户登录（JWT 认证）
- ✅ 个人资料管理
- ✅ 密码修改
- ✅ 头像上传

### 2. 文章管理
- ✅ 富文本编辑器（Markdown 支持）
- ✅ 文章发布/编辑/删除
- ✅ 草稿箱功能
- ✅ 封面图片上传
- ✅ 文章分类管理
- ✅ 文章标签管理
- ✅ 文章搜索（关键词、分类、标签筛选）
- ✅ 文章归档（按年月）
- ✅ 浏览量统计
- ✅ 上一篇/下一篇导航
- ✅ 相关文章推荐

### 3. 分类管理
- ✅ 多级分类（树形结构）
- ✅ 分类创建/编辑/删除
- ✅ 自动生成 Slug（URL 友好标识）
- ✅ 分类下文章数量统计

### 4. 标签管理
- ✅ 标签创建/编辑/删除
- ✅ 自动生成 Slug
- ✅ 标签云展示
- ✅ 热门标签排行

### 5. 评论系统
- ✅ 支持游客评论（填写昵称、邮箱、网址）
- ✅ 登录用户评论（自动关联用户信息）
- ✅ 多层嵌套回复
- ✅ 评论审核机制
- ✅ 敏感词过滤
- ✅ 白名单用户免审核
- ✅ 垃圾评论标记

### 6. AI 智能功能
- ✅ **AI Slug 生成**：根据标题自动生成 URL 友好的 Slug
  - 中文标题：AI 自动翻译为英文后生成 Slug
  - 英文标题：直接转换为小写、空格替换为连字符
  - 降级策略：AI 服务不可用时自动降级为时间戳生成
  - 重试机制：支持最多 3 次重试
- ✅ **模型支持**：阿里云千问（qwen-turbo、qwen-plus、qwen-max）
- ✅ **配置灵活**：支持温度参数、API Key 环境变量配置

### 7. 系统管理（管理员）
- ✅ 用户管理（禁用/启用、重置密码、角色修改）
- ✅ 内容审核（文章、评论）
- ✅ 系统配置（站点信息、SEO 设置）
- ✅ 评论设置管理

### 8. 其他功能
- ✅ RSS 订阅
- ✅ 响应式设计（支持 PC 和移动端）
- ✅ 代码语法高亮
- ✅ 图片上传与管理
- ✅ 浏览记录统计

## 🚀 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 7.0+
- Node.js 18+
- Maven 3.6+

### 后端启动

```bash
# 1. 克隆项目
git clone <repository-url>
cd djblog

# 2. 配置数据库（创建数据库并修改配置文件）
# 在 backend/src/main/resources/application.yml 中配置 MySQL 和 Redis 连接信息

# 3. 配置 AI API Key（可选）
# 设置环境变量：AI_DASHSCOPE_API_KEY=your-api-key
# 或在 application.yml 中配置

# 4. 编译打包
mvn clean install

# 5. 运行
cd backend
mvn spring-boot:run
```

启动成功后访问：http://localhost:8080

Swagger API 文档：http://localhost:8080/swagger-ui.html

### 前端启动

```bash
# 1. 进入前端目录
cd frontend

# 2. 安装依赖
npm install

# 3. 配置环境变量
cp .env.example .env
# 修改 .env 中的 API 基础地址

# 4. 启动开发服务器
npm run dev

# 5. 构建生产版本
npm run build
```

启动成功后访问：http://localhost:5173

## 📁 项目结构

```
djblog/
├── backend/                    # 后端项目
│   ├── src/main/java/          # Java 源代码
│   │   └── site/dengwei/blog/
│   │       ├── controller/     # REST API 控制器
│   │       ├── service/        # 业务逻辑层
│   │       ├── mapper/         # 数据访问层
│   │       ├── entity/         # 实体类
│   │       ├── dto/            # 数据传输对象
│   │       ├── config/         # 配置类
│   │       ├── exception/      # 异常处理
│   │       ├── enums/          # 枚举类
│   │       └── util/           # 工具类
│   ├── src/main/resources/     # 资源文件
│   │   ├── db/migration/       # Flyway 数据库迁移脚本
│   │   ├── mapper/             # MyBatis XML 映射文件
│   │   └── application.yml     # 应用配置文件
│   └── pom.xml                 # Maven 依赖配置
│
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── api/                # API 接口封装
│   │   ├── assets/             # 静态资源
│   │   ├── components/         # Vue 组件
│   │   ├── layouts/            # 布局组件
│   │   ├── router/             # 路由配置
│   │   ├── stores/             # Pinia 状态管理
│   │   ├── utils/              # 工具函数
│   │   ├── views/              # 页面视图
│   │   ├── App.vue             # 根组件
│   │   └── main.js             # 入口文件
│   ├── tests/                  # Playwright E2E 测试
│   ├── public/                 # 公共资源文件
│   ├── package.json            # 依赖配置
│   └── vite.config.js          # Vite 配置
│
├── doc/                        # 项目文档
│   ├── 博客后端设计文档.md
│   ├── 博客前端设计文档.md
│   ├── 技术栈和约束.md
│   └── 开发流程与文档维护规范.md
│
└── deploy/                     # 部署相关脚本
    ├── redis-compose.yml
    └── setup-ubuntu.sh
```

## 📖 API 文档

项目使用 SpringDoc OpenAPI 生成 API 文档，启动后端服务后访问：

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### 主要 API 端点

#### 认证相关
```
POST /api/v1/auth/register    # 用户注册
POST /api/v1/auth/login       # 用户登录
POST /api/v1/auth/logout      # 用户登出
```

#### 用户相关
```
GET  /api/v1/users/profile    # 获取当前用户信息
PUT  /api/v1/users/profile    # 更新用户信息
PUT  /api/v1/users/password   # 修改密码
```

#### 文章相关
```
GET    /api/v1/posts          # 文章列表
GET    /api/v1/posts/{id}     # 文章详情
POST   /api/v1/posts          # 创建文章
PUT    /api/v1/posts/{id}     # 更新文章
DELETE /api/v1/posts/{id}     # 删除文章
GET    /api/v1/posts/archive  # 文章归档
```

#### 分类相关
```
GET    /api/v1/categories            # 分类列表
GET    /api/v1/categories/{id}/posts # 分类下文章
POST   /api/v1/categories            # 创建分类
PUT    /api/v1/categories/{id}       # 更新分类
DELETE /api/v1/categories/{id}       # 删除分类
```

#### 标签相关
```
GET    /api/v1/tags            # 标签列表
GET    /api/v1/tags/{id}/posts # 标签下文章
POST   /api/v1/tags            # 创建标签
PUT    /api/v1/tags/{id}       # 更新标签
DELETE /api/v1/tags/{id}       # 删除标签
```

#### 评论相关
```
GET    /api/v1/posts/{id}/comments  # 文章评论列表
POST   /api/v1/posts/{id}/comments  # 发表评论
DELETE /api/v1/comments/{id}        # 删除评论
```

#### AI 相关
```
POST /api/v1/ai/slug     # AI 生成 Slug
GET  /api/v1/ai/status   # 获取 AI 服务状态
```

## 🧪 测试

### 后端单元测试

```bash
cd backend
mvn test
```

### 前端 E2E 测试

```bash
cd frontend

# 运行所有测试
npm run test

# 带 UI 界面运行
npm run test:ui

# 有头模式运行（显示浏览器）
npm run test:headed

# 查看测试报告
npm run test:report
```

测试覆盖率报告位于 `frontend/playwright-report/`

## 🚢 部署

### Docker 部署（推荐）

后端使用 Jib Maven 插件直接构建 Docker 镜像：

```bash
cd backend

# 构建 Docker 镜像
mvn jib:build -Dimage=your-registry/djblog-backend:latest

# 或使用 Docker Compose
docker-compose up -d
```

### 传统部署

```bash
# 1. 构建后端
cd backend
mvn clean package

# 2. 运行 JAR
java -jar target/dblog-1.0.0.jar

# 3. 构建前端
cd ../frontend
npm run build

# 4. 将 dist 目录部署到 Web 服务器（Nginx/Apache）
```

### 生产环境配置

修改 `backend/src/main/resources/application-prod.yml` 配置生产环境参数：
- 数据库连接
- Redis 连接
- AI API Key
- 日志级别
- 端口配置

## 📏 开发规范

### 代码规范

- 变量命名使用 camelCase 格式
- 类名使用 PascalCase 格式
- 常量使用 UPPER_SNAKE_CASE 格式
- Controller 层只负责校验、转发和组装返回结果
- Service 层包含业务逻辑
- Mapper 层只负责数据库操作
- 所有方法必须有详细的注释（包括参数和返回值说明）
- 异常处理统一，返回格式一致

### 提交规范

遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

```
feat: 新功能
fix: 修复 bug
docs: 文档更新
style: 代码格式调整
refactor: 重构代码
test: 测试相关
chore: 构建/工具链相关
```

示例：
```bash
git commit -m "feat: 添加文章搜索功能"
git commit -m "fix: 修复评论嵌套层级显示错误"
git commit -m "docs: 更新 API 文档说明"
```

### 文档规范

- 所有 Controller 方法必须添加 Swagger 注解
- 数据库变更必须创建 Flyway 迁移脚本
- 功能完成后及时更新设计文档
- 提交信息包含 `[文档更新]` 标记（如涉及文档变更）

详细规范请查看 [开发流程与文档维护规范.md](doc/开发流程与文档维护规范.md)

## 📄 许可证

本项目采用 MIT 许可证。详见 LICENSE 文件。

## 👥 贡献

欢迎贡献代码、报告问题或提出建议！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📞 联系方式

- 作者：dengwei
- Email: [your-email@example.com]

## 🙏 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [MyBatis-Plus](https://baomidou.com/)
- [阿里云 DashScope](https://www.aliyun.com/product/bailian)

---

**Happy Coding! 🎉**

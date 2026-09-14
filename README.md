<p align="center">
  <img src="docs/logo-title.png" width="598" alt="Art Engine" />
</p>


<p align="center">基于 <b>Java 21 + Spring Boot 3.x</b> 的现代化后台基础引擎，开箱即用，适合作为企业级项目的基础框架或二次开发底座。</p>

<div align="center">简体中文 | <a href="./README.en.md">English</a></div>

<br />

<div align="center">

[![license](https://img.shields.io/badge/license-MIT-green.svg)](./LICENSE)
[![stars](https://img.shields.io/github/stars/Luminous-Moon-X/art-engine)](https://github.com/Luminous-Moon-X/art-engine/stargazers)
[![forks](https://img.shields.io/github/forks/Luminous-Moon-X/art-engine)](https://github.com/Luminous-Moon-X/art-engine/network/members)

</div>

<br />

## 📖 项目简介

**Art Engine** 是一个基于 **Java 21 + Spring Boot 3.x** 构建的后台基础引擎，采用模块化、低耦合的设计，自带一套完整的通用后台能力（权限、多租户、缓存、日志、AI 等），开箱即用，可直接作为企业级项目的基础框架或二次开发底座。

配套前端：[Art Design Pro（art-engine-front）](https://github.com/Luminous-Moon-X/art-engine-front)

## 🚀 功能特性

- ✅ **账号与权限**：登录认证、角色权限、按钮级权限、数据权限，权限配置灵活清晰
- ✅ **系统管理**：用户、角色、菜单、部门、字典、规则等常用管理功能一应俱全
- ✅ **多租户**：一套系统服务多个租户，数据自动隔离互不干扰，支持租户套餐管理
- ✅ **缓存加速**：常用数据自动缓存（本地 + Redis 两级），访问更快更稳
- ✅ **操作日志**：自动记录接口调用、登录、操作等日志，敏感信息自动脱敏
- ✅ **AI 对话**：内置 AI 助手，支持流式回复与思考模式，可自定义系统提示词
- ✅ **知识库问答**：上传文档建立知识库，基于文档内容智能问答（RAG）
- ✅ **文件管理**：支持 S3 兼容的对象存储（OSS），开箱即用
- ✅ **实时通信**：内置 WebSocket 支持
- ✅ **接口文档**：自动生成 Swagger 接口文档，方便前后端联调

## 🏗️ 项目结构

多模块 Gradle 工程（根包 `com.art`）：

| 模块 | 说明 |
| --- | --- |
| `art-runner` | 启动入口，打包为可执行的 Spring Boot JAR |
| `art-core` | 基础层：通用配置、鉴权、缓存、对象存储、多租户、公共基类 |
| `art-system` | 业务层：用户、角色、菜单、部门、字典、租户、知识库、AI 对话等接口 |
| `art-ai` | AI 能力：大模型对接、文档向量化与知识检索 |
| `art-log` | 日志能力：接口 / 登录 / 操作日志自动记录 |
| `art-websocket` | WebSocket 实时通信支持 |

## 🛠️ 技术栈

Java 21 · Spring Boot 3.4 · Gradle · MyBatis-Flex · PostgreSQL · Redis · Sa-Token + JWT · Caffeine · Redisson · AgentsFlex（AI）· SpringDoc（接口文档）

## ⚙️ 环境要求

- **JDK 21**
- **PostgreSQL**（默认数据库 `art-engine`）
- **Redis**
- 可选：**Ollama**（知识库问答需要）、大模型 API Key（AI 对话需要）

## 🚀 快速开始

```bash
# 1. 克隆项目
git clone https://github.com/Luminous-Moon-X/art-engine.git

# 2. 初始化数据库：创建 PostgreSQL 数据库（默认 art-engine），导入建表及种子数据脚本

# 3. 修改连接配置：编辑 art-runner/src/main/resources/application-dev.yml
#    （数据库、Redis 等信息也支持通过环境变量覆盖，如 DB_HOST、REDIS_HOST 等）

# 4. 启动应用（默认端口 8089，接口前缀 /api）
./gradlew :art-runner:bootRun

# 5. 查看接口文档
#    http://localhost:8089/api/swagger-ui.html
```

> Windows 环境请使用 `gradlew.bat`。

### 常用命令

| 命令 | 说明 |
| --- | --- |
| `./gradlew build` | 全量构建（含测试） |
| `./gradlew clean build` | 清理后重新构建 |
| `./gradlew :art-runner:bootRun` | 启动应用 |
| `./gradlew test` | 运行测试 |

## 🔌 前端联调

配套前端为 [Art Design Pro（art-engine-front）](https://github.com/Luminous-Moon-X/art-engine-front)。开发环境前端默认通过 Vite 代理将 `/api` 请求转发到本服务（见前端 `.env.development` 中的 `VITE_API_PROXY_URL`）。

## 📄 License

本项目基于 [MIT](./LICENSE) 开源。
<p align="center">
  <img src="docs/logo-title.png" width="598" alt="Art Engine" />
</p>

<p align="center">A modern backend foundation engine built on <b>Java 21 + Spring Boot 3.x</b> — ready to use out of the box as a base framework or a starting point for secondary development.</p>

<div align="center"><a href="./README.md">简体中文</a> | English</div>

<br />

<div align="center">

[![license](https://img.shields.io/badge/license-MIT-green.svg)](./LICENSE)
[![stars](https://img.shields.io/github/stars/Luminous-Moon-X/art-engine)](https://github.com/Luminous-Moon-X/art-engine/stargazers)
[![forks](https://img.shields.io/github/forks/Luminous-Moon-X/art-engine)](https://github.com/Luminous-Moon-X/art-engine/network/members)

</div>

<br />

## 📖 Introduction

**Art Engine** is a backend foundation engine built with **Java 21 + Spring Boot 3.x**. Designed to be modular and loosely coupled, it ships with a complete set of common admin-backend capabilities — permissions, multi-tenancy, caching, logging, AI and more — and can be used directly as a base framework for enterprise projects or as a foundation for secondary development.

Companion frontend: [Art Design Pro (art-engine-front)](https://github.com/Luminous-Moon-X/art-engine-front)

## 🚀 Features

- ✅ **Accounts & permissions**: login authentication, role-based permissions, button-level permissions and data permissions, with clear and flexible configuration
- ✅ **System management**: users, roles, menus, departments, dictionaries, rules and other everyday admin features
- ✅ **Multi-tenancy**: one system serves multiple tenants with automatic data isolation, plus tenant package management
- ✅ **Caching**: frequently used data is cached automatically (local + Redis tiers) for faster, steadier access
- ✅ **Operation logs**: API calls, logins and operations are logged automatically, with sensitive information masked
- ✅ **AI chat**: a built-in AI assistant with streaming replies and thinking mode, plus a customizable system prompt
- ✅ **Knowledge-base Q&A**: upload documents to build a knowledge base and ask questions based on its content (RAG)
- ✅ **File management**: S3-compatible object storage (OSS), ready to use
- ✅ **Real-time communication**: built-in WebSocket support
- ✅ **API docs**: auto-generated Swagger documentation for easy integration

## 🏗️ Project Structure

A multi-module Gradle project (root package `com.art`):

| Module | Description |
| --- | --- |
| `art-runner` | Boot entry, packaged as an executable Spring Boot JAR |
| `art-core` | Foundation: common configuration, auth, caching, object storage, multi-tenancy, base classes |
| `art-system` | Business: APIs for users, roles, menus, departments, dictionaries, tenants, knowledge base, AI chat, etc. |
| `art-ai` | AI capabilities: model integration, document vectorization and knowledge retrieval |
| `art-log` | Logging: automatic API / login / operation logs |
| `art-websocket` | WebSocket real-time communication support |

## 🛠️ Tech Stack

Java 21 · Spring Boot 3.4 · Gradle · MyBatis-Flex · PostgreSQL · Redis · Sa-Token + JWT · Caffeine · Redisson · AgentsFlex (AI) · SpringDoc (API docs)

## ⚙️ Requirements

- **JDK 21**
- **PostgreSQL** (default database `art-engine`)
- **Redis**
- Optional: **Ollama** (for knowledge-base Q&A) and a model API key (for AI chat)

## 🚀 Quick Start

```bash
# 1. Clone the repository
git clone https://github.com/Luminous-Moon-X/art-engine.git

# 2. Initialize the database: create the PostgreSQL database (default art-engine)
#    and import the table schema plus seed-data scripts

# 3. Adjust connection settings: edit art-runner/src/main/resources/application-dev.yml
#    (database, Redis, etc. can also be overridden with environment variables
#    such as DB_HOST, REDIS_HOST)

# 4. Start the application (default port 8080, API prefix /api)
./gradlew :art-runner:bootRun

# 5. Open the API documentation
#    http://localhost:8080/api/swagger-ui.html
```

> On Windows, use `gradlew.bat`.

### Common Commands

| Command | Description |
| --- | --- |
| `./gradlew build` | Full build (including tests) |
| `./gradlew clean build` | Clean and rebuild |
| `./gradlew :art-runner:bootRun` | Start the application |
| `./gradlew test` | Run tests |

## 🔌 Frontend Integration

The companion frontend is [Art Design Pro (art-engine-front)](https://github.com/Luminous-Moon-X/art-engine-front). In development, the frontend proxies `/api` requests to this service via Vite (see `VITE_API_PROXY_URL` in the frontend's `.env.development`).

## 📄 License

This project is open source under the [MIT](./LICENSE) license.
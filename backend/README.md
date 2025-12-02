# NutriAI Backend - AI健康饮食规划助手后端服务

基于Spring Boot 3.2构建的现代化后端应用。

## 技术栈

- **框架**: Spring Boot 3.2.0
- **Java版本**: JDK 17
- **数据库**: MySQL 8.0
- **缓存**: Redis 6.0
- **ORM**: Spring Data JPA (Hibernate)
- **安全**: Spring Security + JWT
- **AI框架**: LangChain4j 0.25.0
- **AI服务**: 阿里云通义千问 (qwen-max)
- **构建工具**: Maven 3.9+

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.9+
- MySQL 8.0
- Redis 6.0

### 2. 数据库初始化

```bash
# 创建数据库
mysql -u root -p

CREATE DATABASE nutriai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 执行初始化脚本
mysql -u root -p nutriai < ../docs/sql/V1__init_schema.sql
```

### 3. 配置环境变量

```bash
# 复制配置文件
cp src/main/resources/application.yml src/main/resources/application-local.yml

# 编辑配置文件，填入以下信息：
# - 数据库密码
# - Redis密码
# - JWT密钥
# - 通义千问API Key
```

### 4. 启动应用

```bash
# 使用Maven启动
mvn spring-boot:run

# 或者打包后启动
mvn clean package
java -jar target/nutriai-backend-1.0.0.jar
```

访问 http://localhost:8080/api/health 检查服务状态。

## 项目结构

```
src/main/java/com/nutriai/
├── common/              # 公共类
│   ├── ApiResponse.java # 统一响应格式
│   └── ErrorCode.java   # 错误码定义
├── config/              # 配置类
│   ├── CorsConfig.java  # 跨域配置
│   ├── SecurityConfig.java      # 安全配置
│   ├── WebSocketConfig.java     # WebSocket配置
│   └── LangChain4jConfig.java   # AI配置
├── controller/          # 控制器
│   ├── AuthController.java      # 认证接口
│   ├── UserController.java      # 用户接口
│   ├── AIChatController.java    # AI聊天接口
│   └── AdminController.java     # 后台管理接口
├── service/             # 业务逻辑层
│   ├── AuthService.java
│   ├── UserService.java
│   ├── AIDietPlanService.java
│   └── AdminService.java
├── repository/          # 数据访问层
│   ├── UserRepository.java
│   ├── DietRecordRepository.java
│   └── ChatMessageRepository.java
├── entity/              # 实体类
│   ├── User.java
│   ├── DietRecord.java
│   └── ChatMessage.java
├── dto/                 # 数据传输对象
├── security/            # 安全相关
│   ├── JwtTokenProvider.java
│   └── JwtAuthenticationFilter.java
├── websocket/           # WebSocket处理器
├── exception/           # 异常处理
└── NutriaiApplication.java  # 启动类
```

## API文档

### 健康检查

```
GET /api/health
```

响应示例：
```json
{
  "status": "UP",
  "service": "nutriai-backend",
  "version": "1.0.0",
  "timestamp": "2025-12-02T15:30:00"
}
```

### 详细API文档

请参考 `docs/06-接口文档.md`

## 配置说明

### JWT配置

```yaml
jwt:
  secret: your-secret-key  # 生产环境必须修改
  expiration: 86400000     # Token过期时间（24小时）
```

### 通义千问API配置

```yaml
tongyi:
  api-key: your-api-key    # 阿里云通义千问API Key
  model: qwen-max          # 模型名称
  max-tokens: 2000         # 最大tokens
```

## 开发规范

### 代码规范

- 使用Lombok简化代码
- 遵循阿里巴巴Java开发手册
- 所有API返回统一的`ApiResponse`格式

### 提交规范

- feat: 新功能
- fix: Bug修复
- docs: 文档更新
- refactor: 代码重构
- perf: 性能优化
- test: 测试相关

## 测试

```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify
```

## 打包部署

```bash
# 打包
mvn clean package -DskipTests

# Docker部署
docker build -t nutriai-backend:1.0.0 .
docker run -d -p 8080:8080 nutriai-backend:1.0.0
```

## 常见问题

### Q1: 启动时提示数据库连接失败？
A: 检查MySQL是否启动，配置文件中的数据库信息是否正确。

### Q2: 通义千问API调用失败？
A: 检查API Key是否正确，是否有足够的免费额度。

### Q3: Redis连接超时？
A: 检查Redis是否启动，防火墙是否开放6379端口。

---

**开发时间**: 2025年12月  
**开发者**: NutriAI Team

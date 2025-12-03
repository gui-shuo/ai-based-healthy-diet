# 用户认证API测试指南

## 🎯 测试前准备

### 1. 启动服务

```bash
# 确保MySQL和Redis已启动
# 启动后端服务
cd backend
mvn spring-boot:run
```

### 2. 测试工具

**本指南使用 Apifox 进行自动化测试**

- 下载地址：https://apifox.com/
- 版本要求：v2.0+
- 优势：支持接口调试、自动化测试、Mock数据、接口文档一体化

---

## 📋 API接口列表

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取验证码 | GET | `/api/auth/captcha` | 获取图形验证码 |
| 用户注册 | POST | `/api/auth/register` | 用户注册 |
| 用户登录 | POST | `/api/auth/login` | 用户登录 |
| 退出登录 | POST | `/api/auth/logout` | 退出登录 |
| 健康检查 | GET | `/api/health` | 服务健康检查 |

---

## 🛠️ Apifox 配置步骤

### 配置1：创建项目

**步骤**：
1. 打开Apifox应用
2. 点击左上角"新建项目"
3. **输入项目名称**：`AI健康饮食规划助手`
4. **选择项目类型**：`Web项目`
5. 点击"确定"

**结果**：创建成功，进入项目主界面

---

### 配置2：配置环境变量

**步骤**：
1. 点击顶部"环境"图标
2. 点击"添加环境"
3. **输入环境名称**：`开发环境`
4. 添加以下变量：

| 变量名 | 初始值 | 当前值 | 说明 |
|--------|--------|--------|------|
| `baseUrl` | `http://localhost:8080` | `http://localhost:8080` | API基础地址 |
| `token` | (留空) | (自动获取) | 登录token |
| `captchaKey` | (留空) | (自动获取) | 验证码Key |
| `captcha` | `ABCD` | `ABCD` | 验证码内容（测试用） |
| `username` | `testuser2025` | `testuser2025` | 测试用户名 |
| `password` | `Test123456` | `Test123456` | 测试密码 |

5. 点击"保存"
6. 选择"开发环境"为当前环境

**结果**：环境变量配置完成

---

### 配置3：创建接口目录

**步骤**：
1. 在左侧接口列表，点击"新建目录"
2. **输入目录名称**：`用户认证模块`
3. 在该目录下创建5个接口：
   - 健康检查
   - 获取验证码
   - 用户注册
   - 用户登录
   - 退出登录

**结果**：目录结构创建完成

---

## 🧪 Apifox 接口测试步骤

### 步骤1：健康检查

**目的**：验证后端服务是否正常启动

#### Apifox 操作步骤：

**1. 创建接口**
- 点击"用户认证模块"目录
- 点击"新建接口"
- **输入接口名称**：`健康检查`
- **选择请求方法**：`GET`
- **输入接口路径**：`{{baseUrl}}/api/health`
- 点击"保存"

**2. 配置接口**
- 无需请求参数
- 无需请求头
- 无需请求体

**3. 发送请求**
- 点击右上角"发送"按钮

**4. 查看响应**

**预期输出**（状态码：200）：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "status": "UP",
    "service": "nutriai-backend",
    "version": "1.0.0",
    "timestamp": "2025-12-02T16:30:00",
    "message": "AI健康饮食规划助手后端服务运行正常"
  },
  "timestamp": 1733133000000
}
```

**验证点**：
- ✅ 状态码为 200
- ✅ code 为 200
- ✅ data.status 为 "UP"

**如果失败**：
- ❌ 无法连接：检查后端服务是否启动
- ❌ 404错误：检查URL路径是否正确
- ❌ 500错误：查看后端控制台日志

---

### 步骤2：获取验证码

**目的**：获取图形验证码，用于注册和登录（失败3次后）

#### Apifox 操作步骤：

**1. 创建接口**
- 点击"用户认证模块"目录
- 点击"新建接口"
- **输入接口名称**：`获取验证码`
- **选择请求方法**：`GET`
- **输入接口路径**：`{{baseUrl}}/api/auth/captcha`

**2. 配置后置脚本**（自动提取captchaKey）

点击"后置操作" → "提取变量" → 添加以下脚本：

```javascript
// 自动提取captchaKey到环境变量
const jsonData = pm.response.json();
if (jsonData.code === 200 && jsonData.data) {
    pm.environment.set("captchaKey", jsonData.data.captchaKey);
    console.log("验证码Key已保存:", jsonData.data.captchaKey);
    console.log("请查看响应中的captchaImage，识别验证码内容");
}
```

**3. 发送请求**
- 点击"发送"按钮

**4. 查看响应**

**预期输出**（状态码：200）：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "captchaKey": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "captchaImage": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMgAAABkCAIAAABM5OhcAA...",
    "expiresIn": 300
  },
  "timestamp": 1733133000000
}
```

**5. 查看验证码图片**

**方法1：使用Apifox图片预览**
- 在响应面板，找到 `captchaImage` 字段
- Apifox会自动识别Base64图片并显示预览

**方法2：使用在线工具**
- 复制 `captchaImage` 的值
- 访问 https://base64.guru/converter/decode/image
- 粘贴并查看图片
- **记录验证码内容**（如：ABCD）

**6. 手动更新环境变量**
- 点击顶部"环境"
- 找到 `captcha` 变量
- **输入当前值**：从图片中识别的验证码（如：`AB3D`）
- 点击"保存"

**验证点**：
- ✅ captchaKey 已自动保存到环境变量
- ✅ captchaImage 是以 "data:image/png;base64," 开头的长字符串
- ✅ expiresIn 为 300（5分钟）

**字段说明**：
- `captchaKey`: 验证码唯一标识，5分钟内有效
- `captchaImage`: Base64编码的PNG图片
- `expiresIn`: 有效期（秒）

---

### 步骤3：用户注册

**目的**：注册新用户账号

#### Apifox 操作步骤：

**1. 创建接口**
- 点击"新建接口"
- **输入接口名称**：`用户注册`
- **选择请求方法**：`POST`
- **输入接口路径**：`{{baseUrl}}/api/auth/register`

**2. 配置请求头**
- 点击"Headers"标签
- 添加请求头：
  - Key: `Content-Type`
  - Value: `application/json`

**3. 配置请求体**
- 点击"Body"标签
- 选择"JSON"格式
- **输入请求体**：

```json
{
  "username": "{{username}}",
  "password": "{{password}}",
  "confirmPassword": "{{password}}",
  "email": "{{username}}@example.com",
  "phone": "13900139000",
  "nickname": "测试用户2025",
  "captcha": "{{captcha}}",
  "captchaKey": "{{captchaKey}}"
}
```

**4. 配置前置脚本**（可选：动态生成用户名）

点击"前置操作" → 添加脚本：

```javascript
// 生成唯一用户名（避免重复注册）
const timestamp = Date.now();
const randomStr = Math.random().toString(36).substring(7);
pm.environment.set("username", `testuser${timestamp}`);
console.log("生成用户名:", pm.environment.get("username"));
```

**5. 发送请求**
- 确保已在步骤2获取验证码
- 确保环境变量 `captcha` 已更新为正确的验证码
- 点击"发送"按钮

**6. 查看响应**

**成功输出**（状态码：200）：
```json
{
  "code": 200,
  "message": "注册成功",
  "data": null,
  "timestamp": 1733133060000
}
```

**验证点**：
- ✅ code 为 200
- ✅ message 为 "注册成功"
- ✅ 可在数据库users表中查到新用户

**常见失败情况**：

**失败1：密码不一致**（状态码：200）
```json
{
  "code": 40004,
  "message": "两次密码输入不一致",
  "data": null
}
```
**解决**：检查 `password` 和 `confirmPassword` 是否相同

**失败2：用户名已存在**（状态码：200）
```json
{
  "code": 40001,
  "message": "用户名已存在",
  "data": null
}
```
**解决**：修改环境变量 `username` 为新的用户名

**失败3：验证码错误**（状态码：200）
```json
{
  "code": 40005,
  "message": "验证码错误或已过期",
  "data": null
}
```
**解决**：重新获取验证码（步骤2），更新 `captcha` 环境变量

**失败4：参数验证失败**（状态码：400）
```json
{
  "code": 400,
  "message": "参数校验失败",
  "data": {
    "password": "密码必须包含大小写字母和数字",
    "username": "用户名长度必须在3-20个字符之间"
  }
}
```
**解决**：检查各字段是否符合规则

**字段验证规则**：
| 字段 | 规则 | 示例 |
|------|------|------|
| username | 3-20字符，字母数字下划线 | testuser2025 |
| password | 6-20字符，含大小写字母+数字 | Test123456 |
| confirmPassword | 必须与password一致 | Test123456 |
| email | 有效邮箱格式 | test@example.com |
| phone | 11位数字（可选） | 13900139000 |
| nickname | 1-20字符（可选） | 测试用户 |
| captcha | 4字符验证码 | AB3D |
| captchaKey | UUID格式 | 从步骤2获取 |

**失败响应示例**：

1. **密码不一致**：
```json
{
  "code": 40004,
  "message": "两次密码输入不一致",
  "data": null,
  "timestamp": "2025-12-02T16:31:00"
}
```

2. **用户名已存在**：
```json
{
  "code": 40001,
  "message": "用户名已存在",
  "data": null,
  "timestamp": "2025-12-02T16:31:00"
}
```

3. **验证码错误**：
```json
{
  "code": 40005,
  "message": "验证码错误或已过期",
  "data": null,
  "timestamp": "2025-12-02T16:31:00"
}
```

4. **参数验证失败**：
```json
{
  "code": 400,
  "message": "参数校验失败",
  "data": {
    "password": "密码必须包含大小写字母和数字",
    "username": "用户名长度必须在3-20个字符之间"
  },
  "timestamp": "2025-12-02T16:31:00"
}
```

---

### 步骤4：用户登录

**目的**：使用注册的账号登录，获取访问令牌

#### Apifox 操作步骤：

**1. 创建接口**
- 点击"新建接口"
- **输入接口名称**：`用户登录`
- **选择请求方法**：`POST`
- **输入接口路径**：`{{baseUrl}}/api/auth/login`

**2. 配置请求头**
- 添加：`Content-Type: application/json`

**3. 配置请求体**（首次登录，不需要验证码）

点击"Body" → 选择"JSON"：

```json
{
  "username": "{{username}}",
  "password": "{{password}}",
  "rememberMe": false
}
```

**4. 配置后置脚本**（自动保存token）

点击"后置操作" → 添加脚本：

```javascript
// 自动提取并保存token
const jsonData = pm.response.json();
if (jsonData.code === 200 && jsonData.data) {
    const accessToken = jsonData.data.accessToken;
    const refreshToken = jsonData.data.refreshToken;
    
    // 保存到环境变量
    pm.environment.set("token", accessToken);
    pm.environment.set("refreshToken", refreshToken);
    
    // 打印日志
    console.log("登录成功！");
    console.log("用户名:", jsonData.data.userInfo.username);
    console.log("角色:", jsonData.data.userInfo.role);
    console.log("Token有效期:", jsonData.data.expiresIn, "秒");
    console.log("Token已保存到环境变量");
}
```

**5. 发送请求**
- 点击"发送"按钮

**6. 查看响应**

**成功输出**（状态码：200）：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoidGVzdHVzZXIyMDI1Iiwicm9sZSI6IlVTRVIiLCJ0eXBlIjoiYWNjZXNzIiwiaWF0IjoxNzMzMTMzMDAwLCJleHAiOjE3MzMxMzY2MDB9.abc123def456...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoidGVzdHVzZXIyMDI1IiwidHlwZSI6InJlZnJlc2giLCJpYXQiOjE3MzMxMzMwMDAsImV4cCI6MTczMzczNzgwMH0.def456ghi789...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "userInfo": {
      "id": 2,
      "username": "testuser2025",
      "nickname": "测试用户2025",
      "email": "testuser2025@example.com",
      "phone": "13900139000",
      "avatar": null,
      "role": "USER",
      "status": "ACTIVE",
      "lastLoginTime": "2025-12-02T17:32:00"
    }
  },
  "timestamp": 1733133120000
}
```

**7. 验证token已保存**
- 点击顶部"环境"
- 查看 `token` 变量
- **当前值**应该已自动填充为accessToken

**验证点**：
- ✅ code 为 200
- ✅ accessToken 已自动保存到环境变量
- ✅ refreshToken 已自动保存
- ✅ userInfo 包含完整用户信息

**常见失败情况**：

**失败1：密码错误**（状态码：200）
```json
{
  "code": 40006,
  "message": "用户名或密码错误",
  "data": null
}
```
**解决**：检查密码是否正确

**失败2：用户不存在**（状态码：200）
```json
{
  "code": 40007,
  "message": "用户不存在",
  "data": null
}
```
**解决**：先执行步骤3注册用户

**失败3：账号已禁用**（状态码：200）
```json
{
  "code": 40008,
  "message": "账号已被禁用",
  "data": null
}
```
**解决**：联系管理员解封账号

**Token字段说明**：
| 字段 | 说明 | 有效期 |
|------|------|--------|
| accessToken | 访问令牌，用于API调用 | 1小时（rememberMe=true时为7天） |
| refreshToken | 刷新令牌，用于获取新token | 7天 |
| tokenType | 令牌类型 | Bearer |
| expiresIn | 令牌过期时间（秒） | 3600或604800 |

---

### 步骤5：测试登录失败计数（验证码保护）

**目的**：测试连续失败3次后强制验证码功能

#### Apifox 操作步骤：

**测试场景：连续输错密码3次**

**1. 修改登录请求体**

在步骤4的"用户登录"接口中，修改密码为错误密码：

```json
{
  "username": "{{username}}",
  "password": "WrongPassword123",
  "rememberMe": false
}
```

**2. 第1次失败**
- 点击"发送"
- **预期输出**：
```json
{
  "code": 40006,
  "message": "用户名或密码错误",
  "data": null
}
```

**3. 第2次失败**
- 再次点击"发送"
- **预期输出**：同上

**4. 第3次失败**
- 第三次点击"发送"
- **预期输出**：同上

**5. 第4次尝试登录**
- 第四次点击"发送"
- **预期输出**（需要验证码）：
```json
{
  "code": 40011,
  "message": "登录失败次数过多，需要验证码",
  "data": null
}
```

**6. 获取验证码**
- 切换到"获取验证码"接口
- 点击"发送"，获取新验证码
- 记录验证码内容（如：`XY5Z`）
- 更新环境变量 `captcha`

**7. 携带验证码登录**

修改登录请求体：

```json
{
  "username": "{{username}}",
  "password": "{{password}}",
  "captcha": "{{captcha}}",
  "captchaKey": "{{captchaKey}}",
  "rememberMe": false
}
```

**8. 发送请求**
- 点击"发送"
- **预期输出**（登录成功）：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "...",
    ...
  }
}
```

**验证点**：
- ✅ 前3次失败返回"用户名或密码错误"
- ✅ 第4次失败返回"需要验证码"
- ✅ 携带正确验证码后登录成功
- ✅ 登录成功后失败计数清零

**失败计数规则**：
- 同一IP + 同一用户名
- Redis存储，30分钟过期
- 登录成功后自动清零
- 失败3次后强制验证码

---

### 步骤6：退出登录

**目的**：退出登录，将token加入黑名单

#### Apifox 操作步骤：

**1. 创建接口**
- 点击"新建接口"
- **输入接口名称**：`退出登录`
- **选择请求方法**：`POST`
- **输入接口路径**：`{{baseUrl}}/api/auth/logout`

**2. 配置请求头**

点击"Headers" → 添加认证头：

| Key | Value |
|-----|-------|
| Authorization | `Bearer {{token}}` |

**说明**：`{{token}}` 会自动替换为步骤4保存的token

**3. 配置Auth（推荐方式）**

或者使用Apifox的Auth功能：
- 点击"Auth"标签
- **选择类型**：`Bearer Token`
- **输入Token**：`{{token}}`

**4. 发送请求**
- 确保已在步骤4成功登录
- 点击"发送"按钮

**5. 查看响应**

**成功输出**（状态码：200）：
```json
{
  "code": 200,
  "message": "退出成功",
  "data": null,
  "timestamp": 1733133300000
}
```

**验证点**：
- ✅ code 为 200
- ✅ message 为 "退出成功"

**6. 验证token已失效**

- 再次点击"发送"（使用同一token）
- **预期输出**（token已失效）：
```json
{
  "code": 401,
  "message": "Token已失效或已退出登录",
  "data": null
}
```

**Token黑名单机制**：
- 退出后，token立即加入Redis黑名单
- 黑名单有效期 = token剩余有效期
- 黑名单中的token无法再次使用
- 需要重新登录获取新token

**常见失败情况**：

**失败1：未提供token**（状态码：401）
```json
{
  "code": 401,
  "message": "未提供认证令牌",
  "data": null
}
```
**解决**：检查Authorization头是否正确配置

**失败2：token格式错误**（状态码：401）
```json
{
  "code": 401,
  "message": "Token格式错误",
  "data": null
}
```
**解决**：确保格式为 `Bearer <token>`，注意中间有空格

---

## 🔍 测试场景

### 场景1：完整注册登录流程

```bash
# 1. 获取验证码
GET /api/auth/captcha

# 2. 注册新用户
POST /api/auth/register
# 使用步骤1返回的captchaKey和图片中的验证码

# 3. 登录
POST /api/auth/login
# 使用注册的用户名和密码

# 4. 访问需要认证的接口（将来实现）
GET /api/user/profile
Authorization: Bearer {token}

# 5. 退出登录
POST /api/auth/logout
Authorization: Bearer {token}
```

### 场景2：记住我功能

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "Test123456",
  "rememberMe": true
}
```

**效果**：
- token有效期从1小时延长到7天
- 7天内无需重新登录

### 场景3：使用默认管理员账号

数据库初始化后，有一个默认管理员账号：

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

---

## 🤖 Apifox 自动化测试

### 方式1：创建测试用例

**目的**：自动化执行完整的注册登录流程

#### 操作步骤：

**1. 创建测试用例**
- 点击左侧"自动化测试"
- 点击"新建测试用例"
- **输入用例名称**：`用户注册登录完整流程`

**2. 添加测试步骤**

点击"添加步骤"，按顺序添加以下接口：

| 步骤 | 接口 | 说明 |
|------|------|------|
| 1 | 健康检查 | 验证服务运行 |
| 2 | 获取验证码 | 自动保存captchaKey |
| 3 | 用户注册 | 使用环境变量 |
| 4 | 用户登录 | 自动保存token |
| 5 | 退出登录 | 使用token |

**3. 配置每个步骤**

**步骤2（获取验证码）后置操作**：
```javascript
pm.environment.set("captcha", "ABCD"); // 固定验证码用于测试
```

**步骤3（用户注册）前置操作**：
```javascript
// 生成唯一用户名
const username = `test${Date.now()}`;
pm.environment.set("username", username);
pm.environment.set("password", "Test123456");
```

**4. 运行测试用例**
- 点击"运行"按钮
- 选择"开发环境"
- 点击"开始测试"

**5. 查看测试报告**

**预期结果**：
- ✅ 健康检查：通过
- ✅ 获取验证码：通过
- ✅ 用户注册：通过
- ✅ 用户登录：通过
- ✅ 退出登录：通过

---

### 方式2：快捷测试（快速调试）

**适用场景**：快速测试单个接口

**步骤**：
1. 选择要测试的接口
2. 点击"快捷请求"
3. 选择环境"开发环境"
4. 点击"发送"

---

### 方式3：批量运行（接口集合）

**目的**：一键运行所有认证接口

#### 操作步骤：

**1. 选择接口目录**
- 点击"用户认证模块"目录
- 点击右键 → "运行目录"

**2. 配置运行选项**
- **选择环境**：开发环境
- **运行顺序**：按接口顺序
- **失败时停止**：否（继续执行）

**3. 开始运行**
- 点击"开始运行"
- 等待所有接口执行完成

**4. 查看结果**
- 绿色：通过
- 红色：失败
- 点击查看详细响应

---

### 断言配置（高级）

**目的**：自动验证响应是否符合预期

#### 为每个接口添加断言：

**健康检查断言**：
```javascript
pm.test("状态码为200", function() {
    pm.response.to.have.status(200);
});

pm.test("服务状态为UP", function() {
    const jsonData = pm.response.json();
    pm.expect(jsonData.data.status).to.eql("UP");
});
```

**用户注册断言**：
```javascript
pm.test("注册成功", function() {
    const jsonData = pm.response.json();
    pm.expect(jsonData.code).to.eql(200);
    pm.expect(jsonData.message).to.include("成功");
});
```

**用户登录断言**：
```javascript
pm.test("登录成功并获得token", function() {
    const jsonData = pm.response.json();
    pm.expect(jsonData.code).to.eql(200);
    pm.expect(jsonData.data.accessToken).to.be.a('string');
    pm.expect(jsonData.data.userInfo).to.have.property('username');
});
```

---

### 性能测试（可选）

**目的**：测试接口响应时间

**步骤**：
1. 点击接口右键 → "性能测试"
2. **配置参数**：
   - 虚拟用户数：10
   - 持续时间：30秒
   - 目标RPS：100
3. 点击"开始测试"
4. 查看响应时间、吞吐量等指标

**性能指标**：
- 响应时间 < 200ms：优秀
- 响应时间 < 500ms：良好
- 响应时间 > 1s：需优化

---

## ⚠️ 常见问题

### Q1: 验证码总是提示错误？
**A**: 检查以下几点：
1. 验证码是否过期（5分钟）
2. 验证码内容是否正确（不区分大小写）
3. captchaKey是否匹配
4. Redis是否正常运行

### Q2: 密码验证不通过？
**A**: 密码必须满足：
- 长度6-20字符
- 至少包含1个大写字母
- 至少包含1个小写字母
- 至少包含1个数字
- 示例：`Test123456`

### Q3: JWT token无效？
**A**: 检查：
1. token是否过期
2. token格式是否正确（Bearer + 空格 + token）
3. 是否已退出登录（token在黑名单中）

### Q4: 登录失败次数如何重置？
**A**: 有两种方式：
1. 登录成功后自动清零
2. 等待30分钟自动过期

---

## ✅ 完整测试检查清单

使用以下清单确保所有功能测试完整：

### 环境准备
- [ ] MySQL 8.0 已启动（端口3306）
- [ ] Redis 6.0 已启动（端口6379）
- [ ] 数据库已初始化（13张表）
- [ ] 后端服务已启动（端口8080）
- [ ] Apifox已安装并配置环境

### 基础功能测试
- [ ] ✅ 健康检查接口返回200
- [ ] ✅ 服务状态为UP

### 验证码功能
- [ ] ✅ 获取验证码成功
- [ ] ✅ captchaKey自动保存到环境变量
- [ ] ✅ captchaImage为Base64格式
- [ ] ✅ 验证码有效期为300秒
- [ ] ✅ 过期验证码无法使用

### 用户注册测试
- [ ] ✅ 使用正确参数注册成功
- [ ] ✅ 用户名重复返回40001错误
- [ ] ✅ 邮箱重复返回40002错误
- [ ] ✅ 密码不一致返回40004错误
- [ ] ✅ 验证码错误返回40005错误
- [ ] ✅ 用户名格式验证（3-20字符）
- [ ] ✅ 密码强度验证（含大小写+数字）
- [ ] ✅ 邮箱格式验证
- [ ] ✅ 手机号格式验证（11位）

### 用户登录测试
- [ ] ✅ 正确用户名密码登录成功
- [ ] ✅ accessToken自动保存
- [ ] ✅ refreshToken自动保存
- [ ] ✅ 返回完整userInfo
- [ ] ✅ 错误密码返回40006
- [ ] ✅ 不存在用户返回40007
- [ ] ✅ 禁用账号返回40008

### 登录失败计数
- [ ] ✅ 第1次失败：提示密码错误
- [ ] ✅ 第2次失败：提示密码错误
- [ ] ✅ 第3次失败：提示密码错误
- [ ] ✅ 第4次尝试：提示需要验证码
- [ ] ✅ 携带验证码登录成功
- [ ] ✅ 登录成功后计数清零

### 记住我功能
- [ ] ✅ rememberMe=false，token有效期1小时
- [ ] ✅ rememberMe=true，token有效期7天
- [ ] ✅ expiresIn字段正确

### 退出登录测试
- [ ] ✅ 携带token退出成功
- [ ] ✅ token加入黑名单
- [ ] ✅ 黑名单token无法使用
- [ ] ✅ 未提供token返回401
- [ ] ✅ 错误格式token返回401

### 自动化测试
- [ ] ✅ 完整流程测试用例运行通过
- [ ] ✅ 所有断言验证通过
- [ ] ✅ 环境变量自动传递正确
- [ ] ✅ 接口响应时间 < 500ms

### 默认管理员账号
- [ ] ✅ admin/admin123 登录成功
- [ ] ✅ role为ADMIN
- [ ] ✅ 管理员权限正常

---

## 📊 测试报告示例

### 测试执行摘要

**测试日期**：2025-12-02  
**测试工具**：Apifox v2.0  
**测试环境**：开发环境

| 接口 | 状态 | 响应时间 | 备注 |
|------|------|----------|------|
| 健康检查 | ✅ 通过 | 45ms | - |
| 获取验证码 | ✅ 通过 | 120ms | captchaKey已保存 |
| 用户注册 | ✅ 通过 | 230ms | 新用户创建成功 |
| 用户登录 | ✅ 通过 | 180ms | token已保存 |
| 退出登录 | ✅ 通过 | 65ms | token已加入黑名单 |

**总测试数**：5  
**通过数**：5  
**失败数**：0  
**通过率**：100%

---

## 🎉 测试完成！

### 测试结论

如果所有测试通过，说明：
- ✅ 用户认证API功能正常
- ✅ 验证码机制工作正常
- ✅ JWT token生成和验证正确
- ✅ 登录失败计数功能正常
- ✅ Token黑名单机制生效

### 下一步行动

**Sprint 3 完成后续工作**：
1. ✅ 后端API测试完成
2. 🔄 前端页面功能测试
3. 🔄 前后端联调测试
4. 📝 编写用户使用文档

**Sprint 4 计划**：
- 个人中心模块开发
- 用户资料管理
- 头像上传功能
- 密码修改功能

---

## 📚 附录

### Apifox导入导出

**导出接口集合**：
1. 右键"用户认证模块"
2. 选择"导出"
3. 选择格式：Apifox格式
4. 保存为：`nutriai-auth-apis.apifox.json`

**导入到其他环境**：
1. 点击"导入"
2. 选择文件
3. 确认导入

### 常用快捷键

| 快捷键 | 功能 |
|--------|------|
| Ctrl + S | 保存接口 |
| Ctrl + Enter | 发送请求 |
| Ctrl + K | 搜索接口 |
| Ctrl + N | 新建接口 |
| Ctrl + D | 复制接口 |

### 团队协作

**共享接口**：
1. 点击右上角"协作"
2. 邀请团队成员
3. 设置权限
4. 发送邀请链接

**同步更新**：
- 接口修改实时同步
- 环境变量团队共享
- 测试用例统一管理

---

## 🆘 技术支持

### 问题反馈

如遇到测试问题，请提供：
1. Apifox版本号
2. 接口请求详情
3. 完整错误信息
4. 后端日志

### 联系方式

- 📧 Email: support@nutriai.com
- 💬 微信群：NutriAI开发群
- 🐛 Issue: https://gitee.com/nutriai/issues

---

**祝测试顺利！🎉**

**NutriAI Backend Team**  
**更新时间：2025-12-02**

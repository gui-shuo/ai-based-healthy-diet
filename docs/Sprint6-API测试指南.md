# Sprint 6 - 会员系统API测试指南

**创建时间**: 2025-12-03  
**测试工具**: PowerShell + Invoke-RestMethod

---

## 📋 前置准备

### 1. 启动后端服务
```powershell
cd backend
mvn spring-boot:run
```

### 2. 登录获取Token
```powershell
# 登录admin账号
$loginBody = @{
    username = "admin"
    password = "Admin123456"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
    -Method Post `
    -Headers @{"Content-Type"="application/json"} `
    -Body $loginBody

$token = $response.data.accessToken
Write-Host "Token获取成功: $token"

# 设置请求头
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}
```

---

## 🧪 API测试

### 1. 获取会员信息
**接口**: `GET /api/member/info`

```powershell
$memberInfo = Invoke-RestMethod -Uri "http://localhost:8080/api/member/info" `
    -Method Get `
    -Headers $headers

# 查看结果
$memberInfo.data | ConvertTo-Json -Depth 10
```

**预期响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "memberId": 1,
    "userId": 1,
    "username": "admin",
    "currentLevel": {
      "levelId": 1,
      "levelCode": "ROOKIE",
      "levelName": "新手会员",
      "levelOrder": 1,
      "growthRequired": 0,
      "benefits": {...},
      "iconUrl": "/icons/level-rookie.png",
      "color": "#95a5a6"
    },
    "nextLevel": {
      "levelId": 2,
      "levelCode": "BRONZE",
      "levelName": "青铜会员",
      ...
    },
    "totalGrowth": 0,
    "currentGrowth": 0,
    "upgradeProgress": 0.0,
    "growthToNextLevel": 100,
    "invitationCode": "INV000001XXXXXX",
    "invitationLink": "http://localhost:3000/register?code=INV000001XXXXXX",
    "invitationCount": 0,
    "isActive": true,
    "activatedAt": "2025-12-03T12:00:00",
    "memberDays": 0
  }
}
```

---

### 2. 获取成长值记录
**接口**: `GET /api/member/growth-records`

```powershell
# 获取第一页，每页10条
$growthRecords = Invoke-RestMethod -Uri "http://localhost:8080/api/member/growth-records?page=0&size=10" `
    -Method Get `
    -Headers $headers

# 查看结果
$growthRecords.data | ConvertTo-Json -Depth 10
```

**预期响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "growthValue": 50,
        "growthType": "INVITATION",
        "growthTypeName": "邀请好友",
        "description": "成功邀请新用户注册",
        "createdAt": "2025-12-03T12:00:00"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "number": 0,
    "size": 10
  }
}
```

---

### 3. 生成邀请链接
**接口**: `GET /api/member/invitation/generate`

```powershell
$invitationLink = Invoke-RestMethod -Uri "http://localhost:8080/api/member/invitation/generate" `
    -Method Get `
    -Headers $headers

# 查看结果
$invitationLink.data | ConvertTo-Json
```

**预期响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "invitationCode": "INV000001XXXXXX",
    "invitationLink": "http://localhost:3000/register?code=INV000001XXXXXX",
    "invitationText": "邀请你加入AI健康饮食规划助手！使用我的邀请码 INV000001XXXXXX 注册，我们都能获得成长值奖励！"
  }
}
```

---

### 4. 查询邀请记录
**接口**: `GET /api/member/invitation/records`

```powershell
# 获取第一页，每页10条
$invitationRecords = Invoke-RestMethod -Uri "http://localhost:8080/api/member/invitation/records?page=0&size=10" `
    -Method Get `
    -Headers $headers

# 查看结果
$invitationRecords.data | ConvertTo-Json -Depth 10
```

**预期响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "inviterId": 1,
        "inviterName": "admin",
        "inviteeId": 2,
        "inviteeName": "testuser",
        "invitationCode": "INV000001XXXXXX",
        "status": "ACCEPTED",
        "statusName": "已接受",
        "invitedAt": "2025-12-03T12:00:00",
        "acceptedAt": "2025-12-03T12:05:00",
        "rewardGrowth": 50,
        "isRewarded": true
      }
    ],
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

## 🔄 完整测试流程

### 场景1：新用户通过邀请码注册

```powershell
# 1. 获取admin的邀请码
$inviteResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/member/invitation/generate" `
    -Method Get `
    -Headers $headers

$inviteCode = $inviteResponse.data.invitationCode
Write-Host "邀请码: $inviteCode"

# 2. 新用户注册（携带邀请码）
$registerBody = @{
    username = "newuser"
    password = "Password123"
    email = "newuser@test.com"
    invitationCode = $inviteCode  # 携带邀请码
} | ConvertTo-Json

$registerResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" `
    -Method Post `
    -Headers @{"Content-Type"="application/json"} `
    -Body $registerBody

Write-Host "注册成功"

# 3. 查看admin的邀请记录（应该增加了一条）
$invitationRecords = Invoke-RestMethod -Uri "http://localhost:8080/api/member/invitation/records?page=0&size=10" `
    -Method Get `
    -Headers $headers

Write-Host "邀请记录数: $($invitationRecords.data.totalElements)"

# 4. 查看admin的成长值记录（应该增加了50成长值）
$growthRecords = Invoke-RestMethod -Uri "http://localhost:8080/api/member/growth-records?page=0&size=10" `
    -Method Get `
    -Headers $headers

Write-Host "最新成长值记录:"
$growthRecords.data.content[0] | ConvertTo-Json
```

### 场景2：查看会员升级情况

```powershell
# 1. 查看当前会员信息
$memberInfo = Invoke-RestMethod -Uri "http://localhost:8080/api/member/info" `
    -Method Get `
    -Headers $headers

Write-Host "当前等级: $($memberInfo.data.currentLevel.levelName)"
Write-Host "总成长值: $($memberInfo.data.totalGrowth)"
Write-Host "当前等级成长值: $($memberInfo.data.currentGrowth)"
Write-Host "升级进度: $($memberInfo.data.upgradeProgress)%"
Write-Host "距离下一等级: $($memberInfo.data.growthToNextLevel)"

# 2. 如果接近升级，可以通过邀请等方式获取成长值

# 3. 再次查看会员信息，确认是否升级
```

---

## 📊 数据验证

### 查看数据库数据

```sql
-- 查看会员等级配置
SELECT * FROM member_levels ORDER BY level_order;

-- 查看会员信息
SELECT m.*, u.username FROM members m
JOIN users u ON m.user_id = u.id;

-- 查看成长值记录
SELECT gr.*, u.username FROM growth_records gr
JOIN users u ON gr.user_id = u.id
ORDER BY gr.created_at DESC
LIMIT 10;

-- 查看邀请记录
SELECT i.*, 
       u1.username as inviter_name,
       u2.username as invitee_name
FROM invitations i
LEFT JOIN users u1 ON i.inviter_id = u1.id
LEFT JOIN users u2 ON i.invitee_id = u2.id
ORDER BY i.invited_at DESC;
```

---

## ⚠️ 注意事项

1. **数据库迁移**
   - 确保Flyway迁移成功执行
   - 检查`member_levels`表是否有5个等级数据
   - 检查现有用户是否自动创建了会员记录

2. **邀请码生成**
   - 邀请码格式：`INV + 6位用户ID + 6位随机字符`
   - 确保唯一性

3. **成长值计算**
   - 邀请奖励：50成长值
   - 升级时会扣除相应成长值
   - 总成长值永远累加

4. **等级升级**
   - 新手会员(ROOKIE): 0成长值
   - 青铜会员(BRONZE): 100成长值
   - 白银会员(SILVER): 500成长值
   - 黄金会员(GOLD): 2000成长值
   - 铂金会员(PLATINUM): 5000成长值

---

## 🐛 常见问题

### Q1: 获取会员信息失败
**检查**: 
- Token是否有效
- 用户是否存在会员记录
- 数据库迁移是否成功

### Q2: 邀请码注册不生效
**检查**:
- 注册接口是否正确处理邀请码参数
- MemberService.processInvitation是否被调用
- 邀请码是否存在且有效

### Q3: 成长值未增加
**检查**:
- 事务是否正常提交
- 数据库约束是否满足
- 日志中是否有错误信息

---

## 📝 测试检查清单

- [ ] 数据库迁移成功
- [ ] 会员等级数据初始化完成
- [ ] 现有用户自动创建会员记录
- [ ] 获取会员信息成功
- [ ] 获取成长值记录成功
- [ ] 生成邀请链接成功
- [ ] 查询邀请记录成功
- [ ] 新用户通过邀请码注册成功
- [ ] 邀请人获得成长值奖励
- [ ] 成长值达到要求自动升级

---

**测试完成时间**: _____________  
**测试人员**: _____________  
**测试结果**: _____________

# Sprint 5 - 饮食记录模块 API 测试指南

## API接口列表

### 1. 创建饮食记录
**接口**: `POST /api/food/records`  
**需要认证**: 是（Bearer Token）

**请求体示例**:
```json
{
  "mealType": "BREAKFAST",
  "foodName": "煎蛋三明治",
  "photoUrl": "http://localhost:8080/api/uploads/foods/food_xxx.jpg",
  "portion": 150.00,
  "calories": 350.50,
  "protein": 15.20,
  "carbohydrates": 45.30,
  "fat": 12.80,
  "fiber": 3.50,
  "recordTime": "2024-12-02T08:30:00",
  "notes": "早餐很丰富"
}
```

**PowerShell测试命令**:
```powershell
$token = "你的JWT_TOKEN"
$body = @{
    mealType = "BREAKFAST"
    foodName = "煎蛋三明治"
    portion = 150.00
    calories = 350.50
    protein = 15.20
    carbohydrates = 45.30
    fat = 12.80
    fiber = 3.50
    recordTime = "2024-12-02T08:30:00"
    notes = "早餐很丰富"
} | ConvertTo-Json

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

Invoke-RestMethod -Uri "http://localhost:8080/api/food/records" -Method Post -Headers $headers -Body $body
```

---

### 2. 查询饮食记录（分页）
**接口**: `GET /api/food/records`  
**需要认证**: 是（Bearer Token）

**查询参数**:
- `page`: 页码（从0开始，默认0）
- `size`: 每页数量（默认10）
- `startDate`: 开始日期（可选，格式：2024-01-01）
- `endDate`: 结束日期（可选，格式：2024-01-31）
- `mealType`: 餐次类型（可选，BREAKFAST/LUNCH/DINNER/SNACK）

**PowerShell测试命令**:
```powershell
# 1. 查询所有记录（分页）
$token = "你的JWT_TOKEN"
$headers = @{
    "Authorization" = "Bearer $token"
}

Invoke-RestMethod -Uri "http://localhost:8080/api/food/records?page=0&size=10" -Method Get -Headers $headers

# 2. 按日期范围查询
Invoke-RestMethod -Uri "http://localhost:8080/api/food/records?startDate=2024-12-01&endDate=2024-12-02" -Method Get -Headers $headers

# 3. 按餐次类型查询
Invoke-RestMethod -Uri "http://localhost:8080/api/food/records?mealType=BREAKFAST" -Method Get -Headers $headers
```

---

### 3. 获取饮食记录详情
**接口**: `GET /api/food/records/{id}`  
**需要认证**: 是（Bearer Token）

**PowerShell测试命令**:
```powershell
$token = "你的JWT_TOKEN"
$recordId = 1

$headers = @{
    "Authorization" = "Bearer $token"
}

Invoke-RestMethod -Uri "http://localhost:8080/api/food/records/$recordId" -Method Get -Headers $headers
```

---

### 4. 删除饮食记录
**接口**: `DELETE /api/food/records/{id}`  
**需要认证**: 是（Bearer Token）

**PowerShell测试命令**:
```powershell
$token = "你的JWT_TOKEN"
$recordId = 1

$headers = @{
    "Authorization" = "Bearer $token"
}

Invoke-RestMethod -Uri "http://localhost:8080/api/food/records/$recordId" -Method Delete -Headers $headers
```

---

### 5. 获取营养摄入统计
**接口**: `GET /api/food/stats`  
**需要认证**: 是（Bearer Token）

**查询参数**:
- `date`: 统计日期（可选，默认今天，格式：2024-12-02）

**PowerShell测试命令**:
```powershell
$token = "你的JWT_TOKEN"

$headers = @{
    "Authorization" = "Bearer $token"
}

# 查询今天的统计
Invoke-RestMethod -Uri "http://localhost:8080/api/food/stats" -Method Get -Headers $headers

# 查询指定日期的统计
Invoke-RestMethod -Uri "http://localhost:8080/api/food/stats?date=2024-12-02" -Method Get -Headers $headers
```

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "date": "2024-12-02",
    "totalCalories": 1850.50,
    "totalProtein": 85.60,
    "totalCarbohydrates": 210.80,
    "totalFat": 65.40,
    "totalFiber": 28.50,
    "breakfastCalories": 450.00,
    "lunchCalories": 750.50,
    "dinnerCalories": 600.00,
    "snackCalories": 50.00,
    "recordCount": 5,
    "mealStats": {
      "breakfastCount": 1,
      "lunchCount": 2,
      "dinnerCount": 1,
      "snackCount": 1
    }
  }
}
```

---

### 6. 上传食物照片
**接口**: `POST /api/food/photo`  
**需要认证**: 是（Bearer Token）  
**Content-Type**: `multipart/form-data`

**PowerShell测试命令**:
```powershell
$token = "你的JWT_TOKEN"
$filePath = "D:\test\food.jpg"

$headers = @{
    "Authorization" = "Bearer $token"
}

$formData = @{
    file = Get-Item -Path $filePath
}

Invoke-RestMethod -Uri "http://localhost:8080/api/food/photo" -Method Post -Headers $headers -Form $formData
```

**响应示例**:
```json
{
  "code": 200,
  "message": "上传成功",
  "data": "http://localhost:8080/api/uploads/foods/food_xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx.jpg"
}
```

---

## 完整测试流程

### 1. 获取Token（使用admin账号登录）
```powershell
$loginBody = @{
    username = "admin"
    password = "Admin123456"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Headers @{"Content-Type"="application/json"} -Body $loginBody
$token = $loginResponse.data.token

Write-Host "Token: $token"
```

### 2. 创建饮食记录（早餐）
```powershell
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

$breakfast = @{
    mealType = "BREAKFAST"
    foodName = "燕麦粥+水煮蛋"
    portion = 200.00
    calories = 280.00
    protein = 12.50
    carbohydrates = 40.00
    fat = 8.50
    fiber = 5.00
    recordTime = "2024-12-02T07:30:00"
    notes = "健康早餐"
} | ConvertTo-Json

$breakfastResult = Invoke-RestMethod -Uri "http://localhost:8080/api/food/records" -Method Post -Headers $headers -Body $breakfast
Write-Host "早餐记录ID: $($breakfastResult.data.id)"
```

### 3. 创建饮食记录（午餐）
```powershell
$lunch = @{
    mealType = "LUNCH"
    foodName = "鸡胸肉沙拉"
    portion = 350.00
    calories = 420.00
    protein = 45.00
    carbohydrates = 25.00
    fat = 15.00
    fiber = 8.00
    recordTime = "2024-12-02T12:00:00"
    notes = "减脂午餐"
} | ConvertTo-Json

$lunchResult = Invoke-RestMethod -Uri "http://localhost:8080/api/food/records" -Method Post -Headers $headers -Body $lunch
Write-Host "午餐记录ID: $($lunchResult.data.id)"
```

### 4. 查询今天的所有记录
```powershell
$todayRecords = Invoke-RestMethod -Uri "http://localhost:8080/api/food/records?page=0&size=20" -Method Get -Headers $headers
Write-Host "今天共有 $($todayRecords.data.totalElements) 条记录"
$todayRecords.data.content | Format-Table -Property id, foodName, mealTypeName, calories, recordTime
```

### 5. 查询今天的营养统计
```powershell
$stats = Invoke-RestMethod -Uri "http://localhost:8080/api/food/stats" -Method Get -Headers $headers
Write-Host "今日营养统计:"
Write-Host "总卡路里: $($stats.data.totalCalories) 千卡"
Write-Host "总蛋白质: $($stats.data.totalProtein) 克"
Write-Host "总碳水: $($stats.data.totalCarbohydrates) 克"
Write-Host "总脂肪: $($stats.data.totalFat) 克"
Write-Host "总纤维: $($stats.data.totalFiber) 克"
Write-Host "记录数: $($stats.data.recordCount) 条"
```

---

## 餐次类型说明

| 类型 | 值 | 中文名称 |
|------|------|----------|
| BREAKFAST | BREAKFAST | 早餐 |
| LUNCH | LUNCH | 午餐 |
| DINNER | DINNER | 晚餐 |
| SNACK | SNACK | 加餐 |

---

## 常见响应码

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证或Token过期 |
| 403 | 无权限访问 |
| 404 | 记录不存在 |
| 500 | 服务器内部错误 |

---

## 注意事项

1. 所有接口都需要在请求头中携带JWT Token
2. `recordTime`字段必须使用ISO 8601格式：`YYYY-MM-DDTHH:mm:ss`
3. 营养成分数据（calories、protein等）使用`BigDecimal`类型，精确到小数点后2位
4. 分页查询的page参数从0开始
5. 删除记录时会验证记录所属用户，只能删除自己的记录
6. 照片上传支持JPG、PNG、GIF格式，最大10MB

---

## 数据库表结构

```sql
CREATE TABLE food_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    meal_type VARCHAR(20) NOT NULL,
    food_name VARCHAR(100) NOT NULL,
    photo_url VARCHAR(500),
    portion DECIMAL(8,2),
    calories DECIMAL(8,2),
    protein DECIMAL(8,2),
    carbohydrates DECIMAL(8,2),
    fat DECIMAL(8,2),
    fiber DECIMAL(8,2),
    record_time DATETIME NOT NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_record_time (record_time),
    INDEX idx_user_time (user_id, record_time)
);
```

# Sprint 9S - AI饮食计划生成模块优化方案

## 任务清单

### 1. ✅ 删除快速生成功能（已完成）
- ✅ 删除后端 `/generate/quick` 接口
- ✅ 删除前端快速生成UI和相关代码
- ✅ 删除快速生成相关CSS样式

### 2. 拓展历史记录功能（待实现）
**需求**：点击某条历史记录可以查看详情

**实现方案**：
- 后端：添加 `/history` 接口，返回用户的历史计划列表
- 后端：添加 `/history/{planId}` 接口，返回指定计划的详情
- 前端：在页面添加"历史记录"侧边栏或抽屉
- 前端：点击历史记录项，加载并显示计划详情

### 3. 添加返回首页按钮（待实现）
**需求**：给此页面添加可以返回到首页的按钮

**实现方案**：
- 在页面头部添加"返回首页"按钮
- 使用 `router.push('/')` 导航到首页

### 4. 后台继续生成（待实现）
**需求**：任何方式返回首页后或者离开此页面了，生成计划任然要继续进行

**实现方案**：
- 后端：改为异步生成，使用任务队列
- 后端：添加任务状态查询接口 `/task/{taskId}/status`
- 前端：离开页面时不取消请求
- 前端：返回页面时检查任务状态

### 5. 添加取消生成功能（待实现）
**需求**：只有点击取消后才停止生成

**实现方案**：
- 后端：添加 `/task/{taskId}/cancel` 接口
- 前端：在生成中显示"取消"按钮
- 前端：点击取消后调用取消接口

## 技术实现细节

### 异步任务架构
```
用户发起生成请求
  ↓
后端创建任务，返回taskId
  ↓
后端异步执行生成（逐天生成）
  ↓
前端轮询任务状态
  ↓
生成完成，前端显示结果
```

### 数据库设计
需要添加任务表：
```sql
CREATE TABLE diet_plan_tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL, -- pending, running, completed, failed, cancelled
    progress INT DEFAULT 0,
    total_days INT NOT NULL,
    current_day INT DEFAULT 0,
    plan_id VARCHAR(50),
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### API设计

#### 1. 生成饮食计划（异步）
```
POST /api/diet-plan/generate
Response: {
  "code": 200,
  "message": "任务已创建",
  "data": {
    "taskId": "task_xxx",
    "status": "pending"
  }
}
```

#### 2. 查询任务状态
```
GET /api/diet-plan/task/{taskId}/status
Response: {
  "code": 200,
  "data": {
    "taskId": "task_xxx",
    "status": "running",
    "progress": 42,
    "totalDays": 7,
    "currentDay": 3,
    "planId": "plan_xxx" // 完成后才有
  }
}
```

#### 3. 取消任务
```
POST /api/diet-plan/task/{taskId}/cancel
Response: {
  "code": 200,
  "message": "任务已取消"
}
```

#### 4. 历史记录列表
```
GET /api/diet-plan/history?page=1&size=10
Response: {
  "code": 200,
  "data": {
    "total": 50,
    "list": [
      {
        "planId": "plan_xxx",
        "title": "健康计划 - 7天",
        "days": 7,
        "goal": "lose_weight",
        "createdAt": "2025-12-05 14:00:00"
      }
    ]
  }
}
```

#### 5. 历史记录详情
```
GET /api/diet-plan/history/{planId}
Response: {
  "code": 200,
  "data": {
    "planId": "plan_xxx",
    "title": "健康计划 - 7天",
    "days": 7,
    "markdownContent": "..."
  }
}
```

## 前端UI设计

### 页面布局
```
┌─────────────────────────────────────────┐
│ [返回首页] AI饮食计划生成    [历史记录] │
├─────────────────────────────────────────┤
│                                         │
│  ┌───────────────┐  ┌─────────────────┐│
│  │               │  │                 ││
│  │  参数设置表单  │  │  生成的计划     ││
│  │               │  │                 ││
│  │  [开始生成]    │  │                 ││
│  └───────────────┘  └─────────────────┘│
│                                         │
│  生成中：[进度条] [取消]                 │
└─────────────────────────────────────────┘
```

### 历史记录抽屉
```
┌─────────────────────┐
│ 历史记录      [关闭] │
├─────────────────────┤
│ □ 健康计划 - 7天    │
│   2025-12-05 14:00  │
├─────────────────────┤
│ □ 减脂计划 - 14天   │
│   2025-12-04 10:30  │
├─────────────────────┤
│ ...                 │
└─────────────────────┘
```

## 实现优先级
1. ✅ 删除快速生成功能
2. 🔄 添加返回首页按钮（简单）
3. 🔄 后台继续生成 + 取消功能（核心）
4. 🔄 历史记录功能（增强）

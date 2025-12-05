# Sprint 9S - 异步任务和历史记录功能实现总结

## ✅ 后端已完成

### 1. 数据库表
- ✅ `diet_plan_tasks` - 任务表
- ✅ `diet_plan_history` - 历史记录表

### 2. 实体类
- ✅ `DietPlanTask` - 任务实体
- ✅ `DietPlanHistory` - 历史记录实体

### 3. Repository
- ✅ `DietPlanTaskRepository`
- ✅ `DietPlanHistoryRepository`

### 4. DTO
- ✅ `TaskStatusResponse` - 任务状态响应
- ✅ `HistoryListItem` - 历史记录列表项

### 5. Service
- ✅ `DietPlanTaskService` - 异步任务服务
- ✅ `DietPlanHistoryService` - 历史记录服务

### 6. Controller新增接口

#### 生成计划（异步）
```
POST /api/diet-plan/generate
Response: {
  "code": 200,
  "data": {
    "taskId": "task_xxx",
    "status": "pending"
  }
}
```

#### 查询任务状态
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
    "planId": null
  }
}
```

#### 取消任务
```
POST /api/diet-plan/task/{taskId}/cancel
Response: {
  "code": 200,
  "message": "任务已取消"
}
```

#### 历史记录列表
```
GET /api/diet-plan/history?page=1&size=10
Response: {
  "code": 200,
  "data": {
    "content": [...],
    "totalElements": 50,
    "totalPages": 5
  }
}
```

#### 历史记录详情
```
GET /api/diet-plan/history/{planId}
Response: {
  "code": 200,
  "data": {
    "planId": "plan_xxx",
    "title": "健康计划 - 7天",
    "markdownContent": "..."
  }
}
```

## 🔄 前端待实现

### 1. 修改生成流程为异步
```javascript
// 发起生成请求，获取taskId
const response = await fetch('/api/diet-plan/generate', {...})
const { taskId } = response.data

// 轮询任务状态
const pollInterval = setInterval(async () => {
  const status = await fetch(`/api/diet-plan/task/${taskId}/status`)
  
  if (status.data.status === 'completed') {
    // 获取计划详情
    const plan = await fetch(`/api/diet-plan/history/${status.data.planId}`)
    generatedPlan.value = plan.data
    clearInterval(pollInterval)
  }
}, 2000)
```

### 2. 实现历史记录抽屉
```vue
<el-drawer v-model="historyDrawerVisible" title="历史记录">
  <el-timeline>
    <el-timeline-item v-for="item in historyList" :key="item.planId">
      <div @click="loadHistoryDetail(item.planId)">
        {{ item.title }}
        <br>
        {{ item.createdAt }}
      </div>
    </el-timeline-item>
  </el-timeline>
</el-drawer>
```

### 3. 实现取消功能
```javascript
const handleCancelGenerate = async () => {
  await fetch(`/api/diet-plan/task/${currentTaskId}/cancel`, {
    method: 'POST'
  })
  isGenerating.value = false
}
```

### 4. 离开页面不中断
```javascript
// 保存taskId到localStorage
localStorage.setItem('currentTaskId', taskId)

// 页面加载时检查是否有进行中的任务
onMounted(() => {
  const taskId = localStorage.getItem('currentTaskId')
  if (taskId) {
    checkTaskStatus(taskId)
  }
})
```

## 📊 工作流程

```
用户点击生成
  ↓
创建任务（返回taskId）
  ↓
前端开始轮询状态
  ↓
后端异步生成（@Async）
  ↓
生成完成，保存到历史
  ↓
前端检测到completed
  ↓
加载并显示计划
```

## 🎯 优势

1. **不会超时** - 后端异步执行，前端轮询
2. **可以取消** - 真正的任务取消
3. **后台继续** - 离开页面任务继续
4. **历史记录** - 所有计划都保存
5. **可恢复** - 刷新页面可恢复进度

## ⚠️ 注意事项

1. 需要在`application.yml`中启用异步：
```yaml
spring:
  task:
    execution:
      pool:
        core-size: 2
        max-size: 5
```

2. 需要在主类添加`@EnableAsync`注解

3. 轮询间隔建议2-3秒，避免频繁请求

## 下一步

1. 启动后端测试API
2. 实现前端轮询逻辑
3. 实现历史记录UI
4. 测试完整流程

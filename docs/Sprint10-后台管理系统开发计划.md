# Sprint 10 - 后台管理系统开发计划

## 📋 开发概览

**开发时间**：2025-12-05 23:10 开始  
**预计完成**：后台管理核心功能  
**目标**：实现完整的后台管理系统，包括数据看板、用户管理、AI日志查询和系统配置

---

## 🎯 功能模块

### 1. 数据看板（Dashboard）
**功能描述**：
- 实时数据统计展示
- 用户增长趋势图表
- AI使用情况分析
- 系统运行状态监控

**数据指标**：
- 总用户数、今日新增用户
- 总对话数、今日对话数
- AI调用次数、成功率
- 会员统计（各等级人数）
- 系统资源使用情况

### 2. 用户管理（User Management）
**功能描述**：
- 用户列表查询（分页、搜索、筛选）
- 用户详情查看
- 用户状态管理（启用/禁用）
- 会员等级调整
- 用户数据导出

### 3. AI日志查询（AI Logs）
**功能描述**：
- AI对话日志查询
- 错误日志查询
- 日志详情查看
- 日志导出
- 统计分析

### 4. 系统配置（System Config）
**功能描述**：
- AI模型配置
- 系统参数配置
- 会员等级配置
- 公告管理

---

## 🗄️ 数据库设计

### 1. AI日志表（ai_chat_log）
```sql
CREATE TABLE ai_chat_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    session_id VARCHAR(100) COMMENT '会话ID',
    user_message TEXT NOT NULL COMMENT '用户消息',
    ai_response TEXT COMMENT 'AI回复',
    model VARCHAR(50) COMMENT 'AI模型',
    tokens_used INT COMMENT '使用的token数',
    response_time INT COMMENT '响应时间(ms)',
    status VARCHAR(20) COMMENT '状态：success/error',
    error_message TEXT COMMENT '错误信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_session_id (session_id),
    INDEX idx_created_at (created_at),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI聊天日志表';
```

### 2. 系统配置表（system_config）
```sql
CREATE TABLE system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT NOT NULL COMMENT '配置值',
    config_type VARCHAR(20) NOT NULL COMMENT '配置类型：string/number/boolean/json',
    description VARCHAR(500) COMMENT '配置描述',
    category VARCHAR(50) COMMENT '配置分类',
    is_public BOOLEAN DEFAULT FALSE COMMENT '是否公开（前端可访问）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';
```

### 3. 系统公告表（system_announcement）
```sql
CREATE TABLE system_announcement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(200) NOT NULL COMMENT '公告标题',
    content TEXT NOT NULL COMMENT '公告内容',
    type VARCHAR(20) NOT NULL COMMENT '公告类型：info/warning/error',
    priority INT DEFAULT 0 COMMENT '优先级（数字越大越优先）',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    start_time TIMESTAMP COMMENT '开始时间',
    end_time TIMESTAMP COMMENT '结束时间',
    created_by BIGINT COMMENT '创建人ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_is_active (is_active),
    INDEX idx_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';
```

### 4. 管理员操作日志表（admin_operation_log）
```sql
CREATE TABLE admin_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    admin_id BIGINT NOT NULL COMMENT '管理员ID',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    operation_desc VARCHAR(500) COMMENT '操作描述',
    target_type VARCHAR(50) COMMENT '目标类型：user/config/announcement',
    target_id BIGINT COMMENT '目标ID',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_admin_id (admin_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员操作日志表';
```

---

## 🔧 后端API设计

### 1. 数据看板API

#### 1.1 获取统计数据
```
GET /api/admin/dashboard/stats
Response: {
  "code": 200,
  "data": {
    "userStats": {
      "totalUsers": 1000,
      "todayNewUsers": 50,
      "activeUsers": 300
    },
    "chatStats": {
      "totalChats": 5000,
      "todayChats": 200,
      "avgResponseTime": 1500
    },
    "aiStats": {
      "totalCalls": 10000,
      "successRate": 98.5,
      "avgTokens": 500
    },
    "memberStats": {
      "free": 800,
      "bronze": 150,
      "silver": 40,
      "gold": 10
    }
  }
}
```

#### 1.2 获取用户增长趋势
```
GET /api/admin/dashboard/user-growth?days=7
Response: {
  "code": 200,
  "data": [
    {"date": "2025-12-01", "count": 10},
    {"date": "2025-12-02", "count": 15},
    ...
  ]
}
```

#### 1.3 获取AI使用趋势
```
GET /api/admin/dashboard/ai-usage?days=7
Response: {
  "code": 200,
  "data": [
    {"date": "2025-12-01", "calls": 100, "success": 98},
    ...
  ]
}
```

### 2. 用户管理API

#### 2.1 用户列表查询
```
GET /api/admin/users?page=1&size=20&keyword=&status=&memberLevel=
Response: {
  "code": 200,
  "data": {
    "content": [...],
    "totalElements": 100,
    "totalPages": 5
  }
}
```

#### 2.2 用户详情
```
GET /api/admin/users/{id}
```

#### 2.3 更新用户状态
```
PUT /api/admin/users/{id}/status
Body: {"status": "active/disabled"}
```

#### 2.4 更新会员等级
```
PUT /api/admin/users/{id}/member-level
Body: {"memberLevel": "FREE/BRONZE/SILVER/GOLD"}
```

#### 2.5 导出用户数据
```
GET /api/admin/users/export
```

### 3. AI日志查询API

#### 3.1 日志列表
```
GET /api/admin/ai-logs?page=1&size=20&userId=&status=&startDate=&endDate=
```

#### 3.2 日志详情
```
GET /api/admin/ai-logs/{id}
```

#### 3.3 日志统计
```
GET /api/admin/ai-logs/stats?startDate=&endDate=
```

### 4. 系统配置API

#### 4.1 配置列表
```
GET /api/admin/config?category=
```

#### 4.2 更新配置
```
PUT /api/admin/config/{key}
Body: {"value": "..."}
```

#### 4.3 公告管理
```
GET /api/admin/announcements
POST /api/admin/announcements
PUT /api/admin/announcements/{id}
DELETE /api/admin/announcements/{id}
```

---

## 🎨 前端页面设计

### 1. 布局结构
```
后台管理系统
├── 侧边栏导航
│   ├── 数据看板
│   ├── 用户管理
│   ├── AI日志
│   ├── 系统配置
│   └── 公告管理
├── 顶部栏
│   ├── 面包屑导航
│   ├── 用户信息
│   └── 退出登录
└── 主内容区
```

### 2. 数据看板页面
**组件**：
- 统计卡片（总用户数、今日新增等）
- 用户增长趋势图（ECharts折线图）
- AI使用情况图（ECharts柱状图）
- 会员分布图（ECharts饼图）
- 实时数据表格

### 3. 用户管理页面
**组件**：
- 搜索筛选栏
- 用户列表表格（分页）
- 用户详情对话框
- 状态切换开关
- 会员等级选择器
- 导出按钮

### 4. AI日志页面
**组件**：
- 日期范围选择器
- 状态筛选器
- 日志列表表格
- 日志详情对话框
- 统计图表

### 5. 系统配置页面
**组件**：
- 配置分类标签
- 配置表单
- 保存按钮
- 重置按钮

---

## 🔐 权限控制

### 1. 管理员角色
在`User`表中添加`role`字段：
- `USER` - 普通用户
- `ADMIN` - 管理员
- `SUPER_ADMIN` - 超级管理员

### 2. 权限验证
- 后端：使用`@PreAuthorize`注解
- 前端：路由守卫检查用户角色

---

## 📝 开发步骤

### 阶段1：数据库和后端基础（当前）
1. ✅ 创建数据库迁移脚本
2. ⏳ 创建Entity实体类
3. ⏳ 创建Repository接口
4. ⏳ 创建DTO类
5. ⏳ 创建Service服务层
6. ⏳ 创建Controller控制器

### 阶段2：数据看板功能
1. 实现统计数据查询
2. 实现趋势数据查询
3. 前端数据看板页面
4. 集成ECharts图表

### 阶段3：用户管理功能
1. 实现用户查询API
2. 实现用户管理API
3. 前端用户管理页面

### 阶段4：AI日志和系统配置
1. 实现日志查询API
2. 实现配置管理API
3. 前端相关页面

---

## 🧪 测试清单

- [ ] 数据看板数据准确性
- [ ] 用户管理功能完整性
- [ ] AI日志查询性能
- [ ] 权限控制有效性
- [ ] 数据导出功能
- [ ] 响应式布局适配

---

**开始时间**：2025-12-05 23:10  
**当前状态**：⏳ 开始数据库设计和后端开发

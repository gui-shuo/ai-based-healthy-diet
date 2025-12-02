# NutriAI Frontend - AI健康饮食规划助手

基于Vue 3 + Vite构建的现代化前端应用。

## 技术栈

- **框架**: Vue 3.3.4 (Composition API)
- **构建工具**: Vite 4.4.9
- **状态管理**: Pinia 2.1.6
- **路由**: Vue Router 4.2.4
- **UI组件**: Element Plus 2.3.12
- **CSS**: TailwindCSS 3.3.3 + SCSS
- **HTTP客户端**: Axios 1.5.0
- **图表库**: ECharts 5.4.3
- **代码规范**: ESLint + Prettier

## 快速开始

### 1. 安装依赖

```bash
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:3000

### 3. 构建生产版本

```bash
npm run build
```

### 4. 预览生产构建

```bash
npm run preview
```

## 项目结构

```
src/
├── assets/          # 静态资源
├── components/      # 公共组件
├── composables/     # 组合式函数
├── layouts/         # 页面布局
├── router/          # 路由配置
├── services/        # API服务
├── stores/          # Pinia状态管理
├── styles/          # 样式文件
├── utils/           # 工具函数
├── views/           # 页面组件
├── App.vue          # 根组件
└── main.js          # 入口文件
```

## 开发规范

### 组件命名

- 使用PascalCase命名
- 文件名与组件名保持一致
- 例如: `UserProfile.vue`

### 代码风格

- 使用Composition API
- 使用`<script setup>`语法糖
- 遵循Vue3官方风格指南

### 提交规范

- feat: 新功能
- fix: Bug修复
- docs: 文档更新
- style: 代码格式调整
- refactor: 代码重构
- perf: 性能优化
- test: 测试相关
- chore: 构建工具或辅助工具变动

## 环境变量

开发环境配置见 `.env` 文件。

## 代码检查

```bash
# ESLint检查
npm run lint

# Prettier格式化
npm run format
```

## 浏览器支持

- Chrome >= 90
- Firefox >= 88
- Safari >= 14
- Edge >= 90

---

**开发时间**: 2025年12月  
**开发者**: NutriAI Team

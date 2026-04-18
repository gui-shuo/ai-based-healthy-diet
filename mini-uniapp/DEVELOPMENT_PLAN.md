# NutriAI 小程序开发计划

## 一、项目架构

### 技术栈
- **框架**: uni-app + Vue 3 (Composition API `<script setup>`)
- **状态管理**: Pinia
- **样式**: SCSS (设计系统变量) + rpx自适应
- **API通信**: uni.request封装 + JWT双Token认证
- **图标方案**: Material Symbols (通过iconfont/Unicode或本地SVG)
- **设计体系**: "Digital Greenhouse" — 绿色有机主题、玻璃态效果、无边框规则、编辑式排版

### 目录结构
```
mini-uniapp/
├── pages/                      # 页面
│   ├── index/index.vue         # 首页 (TabBar)
│   ├── meal/index.vue          # 营养餐列表 (TabBar)
│   ├── meal/detail.vue         # 营养餐详情
│   ├── meal/search.vue         # 营养餐搜索
│   ├── meal/checkout.vue       # 营养餐结算
│   ├── shop/index.vue          # 商城首页 (TabBar)
│   ├── shop/detail.vue         # 商品详情
│   ├── shop/search.vue         # 商城搜索
│   ├── shop/cart.vue           # 购物车
│   ├── profile/index.vue       # 我的 (TabBar)
│   ├── profile/edit.vue        # 编辑资料
│   ├── profile/orders.vue      # 我的订单
│   ├── profile/coupons.vue     # 优惠券
│   ├── profile/notifications.vue # 通知中心
│   ├── profile/address.vue     # 收货地址
│   ├── profile/settings.vue    # 设置
│   ├── profile/feedback.vue    # 反馈
│   └── auth/login.vue          # 登录
├── components/                 # 公共组件
│   ├── NavBar.vue              # 顶部导航栏
│   ├── MealCard.vue            # 营养餐卡片
│   ├── ProductCard.vue         # 商品卡片
│   ├── SearchBar.vue           # 搜索栏
│   ├── EmptyState.vue          # 空状态
│   └── PriceTag.vue            # 价格标签
├── stores/                     # Pinia状态管理
│   ├── user.js                 # 用户认证状态
│   └── cart.js                 # 购物车状态
├── services/                   # API服务
│   └── api.js                  # 所有API端点
├── utils/                      # 工具函数
│   ├── request.js              # 请求封装
│   └── common.js               # 通用工具
├── styles/                     # 全局样式
│   └── design-system.scss      # 设计系统变量
├── static/                     # 静态资源
│   ├── icons/                  # 图标
│   └── images/                 # 图片
└── uni.scss                    # uni-app全局SCSS变量
```

### 后端API对接 (核心接口)
| 模块 | 端点 | 说明 |
|------|------|------|
| 认证 | POST /auth/login | 登录 |
| 认证 | POST /auth/wx-login | 微信小程序登录 |
| 认证 | POST /auth/refresh | 刷新Token |
| 用户 | GET /user/profile | 获取用户信息 |
| 用户 | PUT /user/profile | 更新用户信息 |
| 用户 | POST /user/avatar | 上传头像 |
| 地址 | CRUD /user/addresses | 收货地址管理 |
| 营养餐 | GET /meals | 营养餐列表 |
| 营养餐 | GET /meals/:id | 营养餐详情 |
| 营养餐 | POST /meal-orders | 创建订单 |
| 商品 | GET /products | 商品列表 |
| 商品 | GET /products/search | 搜索商品 |
| 商品 | GET /products/:id | 商品详情 |
| 商品 | GET /products/categories | 商品分类 |
| 购物车 | GET /cart | 购物车列表 |
| 购物车 | POST /cart/add | 加入购物车 |
| 购物车 | PUT /cart/:id/quantity | 修改数量 |
| 购物车 | DELETE /cart/:id | 删除商品 |
| 订单 | POST /products/orders | 创建商品订单 |
| 优惠券 | GET /coupons/my | 我的优惠券 |
| 通知 | GET /announcements | 系统公告 |
| 反馈 | POST /feedback | 提交反馈 |
| 会员 | GET /member/info | 会员信息 |
| 公共 | GET /public/config | 公共配置 |

---

## 二、开发阶段

### Phase 1: 基础架构搭建 (当前)
1. 创建设计系统SCSS变量文件
2. 封装uni.request请求工具 (JWT认证、自动刷新、错误处理)
3. 创建Pinia用户Store (登录态、Token管理)
4. 创建Pinia购物车Store
5. 创建API服务层
6. 配置pages.json (TabBar + 所有页面路由)
7. 更新manifest.json
8. 创建通用组件 (NavBar等)

### Phase 2: 首页模块
- 静态宣传展示页面
- 顶部AppBar组件
- 三个宣传Banner区域
- 底部TabBar导航

### Phase 3: 营养餐模块
- 分类列表页 (左侧导航抽屉 + 右侧商品列表)
- 门店选择功能
- 商品详情页 (营养成分展示)
- 搜索页 (热门搜索 + 搜索历史)
- 结算确认订单页

### Phase 4: 商城模块
- 商城首页 (分类Pill导航 + 产品网格)
- 商品详情页 (营养Bento网格)
- 购物车页面
- 搜索页

### Phase 5: 我的页面
- 个人中心首页 (用户卡片 + 功能入口)
- 编辑个人资料
- 我的订单
- 优惠券
- 通知中心
- 收货地址管理
- 设置
- 反馈

---

## 三、设计规范

### 颜色体系 (Digital Greenhouse)
- Primary: #0d631b (健康绿)
- Primary-Container: #2e7d32
- Primary-Fixed: #a3f69c
- Surface: #f7faf7
- Surface-Container: #ebefec
- On-Surface: #181c1b
- On-Surface-Variant: #40493d
- Tertiary: #923357 (警告/超标)
- Error: #ba1a1a

### 设计规则
1. **无边框规则**: 不使用1px实线边框，用背景色差异定义边界
2. **玻璃态效果**: 顶栏和浮动元素使用半透明+模糊
3. **编辑式排版**: 大标题配小标签、非对称布局
4. **有机圆角**: xl=12px 主卡片, lg=8px 容器, full=9999px 胶囊
5. **环境阴影**: rgba(24,28,27,0.06) 32px模糊 8px Y偏移
6. **空间奢侈**: 宁多勿少的留白

### 字体
- 标题: Manrope (几何无衬线)
- 正文: Inter (人文无衬线)
- 小程序降级: 系统默认字体

# 数据库初始化指南

## 📊 数据库表结构

本项目共包含 **13张核心表**：

### 1. 用户相关表（2张）
- `users` - 用户基础表
- `user_profiles` - 用户健康档案表

### 2. 会员相关表（2张）
- `memberships` - 会员信息表
- `growth_records` - 成长值记录表

### 3. 饮食相关表（1张）
- `diet_records` - 饮食记录表

### 4. AI功能表（2张）
- `chat_messages` - AI聊天消息表
- `diet_plans` - AI生成饮食计划表

### 5. 营养数据表（2张）
- `nutrition_data` - 食物营养数据表
- `food_categories` - 食物分类表

### 6. 系统日志表（2张）
- `operation_logs` - 操作日志表
- `api_logs` - 第三方API调用日志表

### 7. 其他功能表（2张）
- `favorites` - 用户收藏表
- `system_configs` - 系统配置表

---

## 🚀 初始化步骤

### 方式1：手动执行（推荐）

```bash
# 1. 登录MySQL
mysql -u root -p

# 2. 创建数据库
CREATE DATABASE nutriai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 3. 使用数据库
USE nutriai;

# 4. 执行初始化脚本
source d:/ProgrammingLanguage/Java/Projects/ai-based-healthy-diet/database/V1__init_schema.sql;

# 5. 验证表创建
SHOW TABLES;

# 6. 查看表数量
SELECT COUNT(*) AS table_count FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'nutriai';
# 应该显示：13
```

### 方式2：使用命令行一键执行

```bash
# Windows
cd d:\ProgrammingLanguage\Java\Projects\ai-based-healthy-diet\database
mysql -u root -p123456 -e "CREATE DATABASE IF NOT EXISTS nutriai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p123456 nutriai < V1__init_schema.sql
```

### 方式3：使用Navicat等GUI工具

1. 打开Navicat，连接到MySQL
2. 右键点击连接 -> 新建数据库
   - 数据库名：`nutriai`
   - 字符集：`utf8mb4`
   - 排序规则：`utf8mb4_unicode_ci`
3. 打开 `V1__init_schema.sql` 文件
4. 点击"运行"按钮执行

---

## ✅ 验证初始化结果

执行以下SQL验证：

```sql
-- 1. 查看所有表
SHOW TABLES;

-- 2. 验证表数量
SELECT COUNT(*) FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'nutriai';
-- 期望结果：13

-- 3. 检查默认管理员账号
SELECT username, email, role FROM users WHERE role = 'super_admin';
-- 期望结果：admin | admin@nutriai.com | super_admin

-- 4. 检查食物分类数据
SELECT COUNT(*) FROM food_categories;
-- 期望结果：8

-- 5. 检查食物数据
SELECT COUNT(*) FROM nutrition_data;
-- 期望结果：12

-- 6. 检查系统配置
SELECT COUNT(*) FROM system_configs;
-- 期望结果：7
```

---

## 📦 初始数据说明

### 默认管理员账号
- **用户名**：`admin`
- **密码**：`admin123`（BCrypt加密）
- **邮箱**：`admin@nutriai.com`
- **角色**：`super_admin`

### 预置食物分类（8个）
1. 谷物类 (Grains) 🌾
2. 蔬菜类 (Vegetables) 🥬
3. 水果类 (Fruits) 🍎
4. 肉类 (Meat) 🥩
5. 水产类 (Seafood) 🐟
6. 蛋奶类 (Dairy) 🥛
7. 豆制品 (Legumes) 🫘
8. 坚果类 (Nuts) 🌰

### 预置食物数据（12种）
- 米饭、全麦面包
- 鸡胸肉、牛肉、三文鱼
- 鸡蛋、牛奶
- 西蓝花、苹果、香蕉
- 豆腐、核桃

### 系统配置（7项）
- 系统名称和版本
- AI每日配额（免费/白银/黄金）
- 会员升级所需成长值

---

## 🔄 数据库更新管理

### 版本命名规则

使用Flyway风格的版本命名：

```
V{版本号}__{描述}.sql
V1__init_schema.sql        # 初始化脚本
V2__add_user_fields.sql    # 添加用户字段
V3__update_nutrition.sql   # 更新营养数据
```

### 未来升级步骤

1. 创建新版本SQL文件（如 `V2__xxx.sql`）
2. 将文件放在 `database/` 目录
3. 执行升级脚本
4. 更新 `README.md` 记录变更

---

## ⚠️ 注意事项

### 1. 字符集设置
- 必须使用 `utf8mb4` 字符集
- 必须使用 `utf8mb4_unicode_ci` 排序规则
- 确保支持emoji和中文

### 2. 外键约束
- 使用了外键约束保证数据完整性
- 删除用户时会级联删除相关数据
- 注意操作顺序，避免外键冲突

### 3. JSON字段
- `allergies`、`dietary_restrictions` 等使用JSON类型
- 需要MySQL 5.7.8+支持
- 示例：`["花生", "海鲜", "乳糖"]`

### 4. ENUM字段
- 角色：`user`、`admin`、`super_admin`
- 会员等级：`free`、`silver`、`gold`
- 修改ENUM需要ALTER TABLE

### 5. 索引优化
- 已为常用查询字段添加索引
- 外键字段自动添加索引
- 注意索引维护成本

---

## 🛠️ 常见问题

### Q1: 执行SQL时报错 "Unknown database"
**解决**：先创建数据库
```sql
CREATE DATABASE nutriai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Q2: 外键约束创建失败
**解决**：检查MySQL版本和InnoDB引擎是否启用
```sql
SHOW ENGINES;  -- 确认InnoDB状态为YES
```

### Q3: JSON字段不支持
**解决**：升级MySQL到5.7.8或更高版本

### Q4: 表已存在错误
**解决**：SQL脚本已包含 `DROP TABLE IF EXISTS`，如手动删除：
```sql
DROP DATABASE IF EXISTS nutriai;
CREATE DATABASE nutriai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Q5: 密码加密值无法登录
**解决**：默认密码是 `admin123`，BCrypt加密后的值已固定在脚本中

---

## 📊 数据库ER图

详细的ER图请查看：`docs/05-数据库设计.md`

---

## 📞 技术支持

如遇到数据库初始化问题：

1. 查看MySQL错误日志
2. 检查MySQL版本（需要8.0+）
3. 确认用户权限
4. 查看项目文档 `docs/05-数据库设计.md`
5. 提交Issue到Gitee

---

**祝您使用愉快！**

**NutriAI Team**

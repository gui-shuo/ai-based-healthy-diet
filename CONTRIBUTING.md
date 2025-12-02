# 贡献指南

感谢您对AI健康饮食规划助手项目的关注！本文档将指导您如何为项目做出贡献。

## 📋 开始之前

1. **Fork项目**：点击右上角的Fork按钮
2. **克隆仓库**：
   ```bash
   git clone https://github.com/your-username/ai-based-healthy-diet.git
   cd ai-based-healthy-diet
   ```
3. **安装依赖**：参考 `快速启动指南.md`

## 🌿 分支管理

我们使用Git Flow工作流：

- `main` - 生产分支，始终保持可发布状态
- `develop` - 开发分支，最新的开发代码
- `feature/*` - 功能分支，从develop分支创建
- `bugfix/*` - Bug修复分支
- `hotfix/*` - 紧急修复分支，从main分支创建

### 创建功能分支

```bash
# 切换到develop分支
git checkout develop
git pull origin develop

# 创建功能分支
git checkout -b feature/your-feature-name
```

## 💻 开发规范

### 代码风格

#### 前端（Vue 3）

- 使用Composition API和`<script setup>`语法
- 组件命名使用PascalCase：`UserProfile.vue`
- 遵循Vue3官方风格指南
- 使用ESLint和Prettier保持代码一致性

```javascript
// ✅ 推荐
<script setup>
import { ref, computed } from 'vue'

const count = ref(0)
const doubleCount = computed(() => count.value * 2)
</script>

// ❌ 不推荐
<script>
export default {
  data() {
    return { count: 0 }
  }
}
</script>
```

#### 后端（Spring Boot）

- 遵循阿里巴巴Java开发手册
- 使用Lombok简化代码
- Service层方法必须有事务注解
- Controller层统一返回`ApiResponse`

```java
// ✅ 推荐
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    
    @Transactional
    public User createUser(UserDTO dto) {
        // ...
    }
}

// ❌ 不推荐
public class UserService {
    @Autowired
    private UserRepository userRepository;
}
```

### 提交规范

使用Conventional Commits规范：

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Type类型**：
- `feat`: 新功能
- `fix`: Bug修复
- `docs`: 文档更新
- `style`: 代码格式调整（不影响功能）
- `refactor`: 代码重构
- `perf`: 性能优化
- `test`: 测试相关
- `chore`: 构建工具或辅助工具变动

**示例**：

```bash
# 新功能
git commit -m "feat(auth): 添加JWT token刷新功能"

# Bug修复
git commit -m "fix(ai): 修复AI聊天流式输出中断问题"

# 文档更新
git commit -m "docs(readme): 更新快速启动指南"

# 性能优化
git commit -m "perf(api): 优化数据库查询，添加索引"
```

### 测试要求

#### 前端测试

- 新增功能必须有对应的单元测试
- 使用Vitest或Jest编写测试

```javascript
// UserProfile.spec.js
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import UserProfile from '@/components/UserProfile.vue'

describe('UserProfile', () => {
  it('renders user info correctly', () => {
    const wrapper = mount(UserProfile, {
      props: { user: { name: '张三' } }
    })
    expect(wrapper.text()).toContain('张三')
  })
})
```

#### 后端测试

- Service层必须有单元测试
- Controller层建议有集成测试

```java
@SpringBootTest
class UserServiceTest {
    
    @Autowired
    private UserService userService;
    
    @Test
    void testCreateUser() {
        UserDTO dto = new UserDTO();
        dto.setUsername("test");
        
        User user = userService.createUser(dto);
        assertNotNull(user.getId());
        assertEquals("test", user.getUsername());
    }
}
```

## 📝 Pull Request流程

### 1. 提交前检查

```bash
# 前端
cd frontend
npm run lint
npm run test

# 后端
cd backend
mvn clean test
```

### 2. 推送代码

```bash
git push origin feature/your-feature-name
```

### 3. 创建Pull Request

- 在GitHub上创建Pull Request
- 选择`develop`作为目标分支
- 填写PR描述模板

### 4. PR描述模板

```markdown
## 变更类型
- [ ] 新功能
- [ ] Bug修复
- [ ] 文档更新
- [ ] 代码重构
- [ ] 性能优化

## 变更说明
简要描述本次变更的内容和原因

## 测试
- [ ] 已添加单元测试
- [ ] 已通过所有测试
- [ ] 已手动测试

## 截图（如果适用）
添加相关截图

## 相关Issue
关联的Issue编号：#123
```

### 5. 代码审查

- 等待至少1位维护者审查
- 根据反馈修改代码
- 所有CI检查必须通过

### 6. 合并

- 维护者会在审查通过后合并PR
- 使用Squash and Merge保持提交历史整洁

## 🐛 报告Bug

使用Issue模板报告Bug：

```markdown
**Bug描述**
简要描述Bug现象

**复现步骤**
1. 进入'...'
2. 点击'...'
3. 看到错误

**期望行为**
描述您期望发生的事情

**实际行为**
描述实际发生的事情

**环境信息**
- 操作系统：Windows 11
- 浏览器：Chrome 120
- Node.js版本：18.17.0
- Java版本：17

**截图**
如果适用，添加截图

**额外信息**
其他相关信息
```

## 💡 提出新功能

使用Issue模板提出功能建议：

```markdown
**功能描述**
清晰简洁地描述您想要的功能

**使用场景**
描述该功能解决什么问题

**解决方案**
您希望如何实现这个功能

**替代方案**
您考虑过的其他解决方案

**额外信息**
其他相关信息或截图
```

## 📚 文档贡献

文档同样重要！您可以：

- 改进现有文档
- 添加代码注释
- 编写教程或示例
- 翻译文档

## ⚖️ 行为准则

- 尊重所有贡献者
- 建设性地提出意见
- 专注于项目目标
- 遵守开源协议

## 🎉 成为贡献者

第一次贡献？这里有一些适合新手的Issue：

- 标签：`good first issue`
- 标签：`help wanted`
- 标签：`documentation`

## 📞 联系我们

如有任何问题，请：

1. 查看 `docs/` 目录下的文档
2. 搜索现有的Issues（Gitee）
3. 创建新的Issue
4. 发送邮件至：dev@nutriai.com

## 🔗 平台说明

本项目使用**Gitee**作为代码托管平台：

- 代码仓库：https://gitee.com/your-username/ai-based-healthy-diet
- CI/CD：Gitee Go（`.gitee/workflows/`）
- Issue追踪：Gitee Issues
- Pull Request：Gitee PR

---

再次感谢您的贡献！🙏

**NutriAI Team**

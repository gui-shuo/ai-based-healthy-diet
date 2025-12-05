# Sprint 9 - 快速开始指南

## 🚀 5分钟快速配置百度AI

### 第1步: 获取百度AI凭证（2分钟）

1. 访问：https://ai.baidu.com/
2. 登录并完成实名认证
3. 进入控制台 → 图像识别 → 创建应用
4. 获取三个值：
   - AppID
   - API Key  
   - Secret Key

### 第2步: 配置环境变量（1分钟）

**Windows**：
```
Win+R → sysdm.cpl → 高级 → 环境变量
添加：
BAIDU_APP_ID=你的AppID
BAIDU_API_KEY=你的APIKey
BAIDU_SECRET_KEY=你的SecretKey
```

**Mac/Linux**：
```bash
echo 'export BAIDU_APP_ID="你的AppID"' >> ~/.bashrc
echo 'export BAIDU_API_KEY="你的APIKey"' >> ~/.bashrc
echo 'export BAIDU_SECRET_KEY="你的SecretKey"' >> ~/.bashrc
source ~/.bashrc
```

### 第3步: 重启服务（2分钟）

```bash
# 重新编译
cd backend
mvn clean package -DskipTests

# 启动
java -jar target/nutriai-backend-1.0.0.jar
```

### 第4步: 测试

1. 登录系统
2. 进入"AI食物识别"
3. 上传食物图片
4. 查看识别结果

---

## ✅ 完成！

现在您可以：
- ✅ 文本识别食物
- ✅ 图片识别食物
- ✅ 查看营养数据
- ✅ 查看识别历史

---

## 📞 遇到问题？

查看详细文档：
- `Sprint9-百度AI配置指南.md`
- `Sprint9-AI食物识别使用指南.md`

---

**免费额度**：500次/天，足够测试使用！

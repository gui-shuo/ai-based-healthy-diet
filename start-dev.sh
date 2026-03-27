#!/bin/bash
# ====================================================================
# NutriAI 本地开发启动脚本
# 用法: ./start-dev.sh
# 说明: 自动加载项目根目录的 .env 文件，然后启动后端 + 前端
# ====================================================================
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ENV_FILE="$SCRIPT_DIR/.env"

# 加载 .env 环境变量
if [ -f "$ENV_FILE" ]; then
  echo "✅ 加载环境变量: $ENV_FILE"
  set -a
  source "$ENV_FILE"
  set +a
else
  echo "⚠️  未找到 .env 文件，将使用默认本地配置 (localhost)"
  echo "   如需连接云资源，请执行: cp .env.example .env 并填入实际值"
fi

echo ""
echo "📦 当前环境配置:"
echo "   DB_HOST   = ${DB_HOST:-localhost}"
echo "   REDIS_HOST= ${REDIS_HOST:-localhost}"
echo "   COS_REGION= ${COS_REGION:-(未配置)}"
echo ""

# 启动后端
echo "🚀 启动后端服务..."
cd "$SCRIPT_DIR/backend"
mvn spring-boot:run -Dspring-boot.run.profiles=dev &
BACKEND_PID=$!

# 启动前端
echo "🚀 启动前端服务..."
cd "$SCRIPT_DIR/frontend"
npm run dev &
FRONTEND_PID=$!

echo ""
echo "✅ 服务已启动:"
echo "   后端: http://localhost:8080/api"
echo "   前端: http://localhost:5173"
echo "   按 Ctrl+C 停止所有服务"

# 捕获退出信号
trap "kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; exit" INT TERM
wait

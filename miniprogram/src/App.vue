<script setup lang="ts">
import { onLaunch, onShow } from "@dcloudio/uni-app";
import { useUserStore } from "@/stores/user";
import { useAppStore } from "@/stores/app";

onLaunch(() => {
  const userStore = useUserStore();
  const appStore = useAppStore();
  userStore.restore();
  appStore.fetchConfig();
});

onShow(() => {});
</script>

<style>
page {
  background-color: #f5f5f5;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
}

.container {
  padding: 20rpx 30rpx;
}

/* 主题色 */
.text-primary { color: #07c160; }
.text-secondary { color: #666; }
.text-danger { color: #ee0a24; }
.text-warning { color: #ff976a; }
.bg-primary { background-color: #07c160; }
.bg-white { background-color: #fff; }

/* 通用卡片 */
.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

/* 按钮 */
.btn-primary {
  background: linear-gradient(135deg, #07c160, #06ad56);
  color: #fff;
  border: none;
  border-radius: 44rpx;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 32rpx;
  font-weight: 500;
  text-align: center;
}
.btn-primary::after { border: none; }

.btn-outline {
  background: #fff;
  color: #07c160;
  border: 2rpx solid #07c160;
  border-radius: 44rpx;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 32rpx;
  text-align: center;
}

/* 输入框 */
.input-group {
  background: #f7f8fa;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
  margin-bottom: 20rpx;
}
.input-group input {
  height: 48rpx;
  font-size: 28rpx;
}
.input-group .label {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 8rpx;
}

/* 分割线 */
.divider {
  height: 1rpx;
  background: #eee;
  margin: 20rpx 0;
}

/* Flex helpers */
.flex { display: flex; }
.flex-col { display: flex; flex-direction: column; }
.flex-center { display: flex; align-items: center; justify-content: center; }
.flex-between { display: flex; align-items: center; justify-content: space-between; }
.flex-wrap { flex-wrap: wrap; }
.flex-1 { flex: 1; }
.items-center { align-items: center; }

/* Spacing */
.mt-10 { margin-top: 10rpx; }
.mt-20 { margin-top: 20rpx; }
.mt-30 { margin-top: 30rpx; }
.mb-20 { margin-bottom: 20rpx; }
.ml-10 { margin-left: 10rpx; }
.mr-10 { margin-right: 10rpx; }
.p-20 { padding: 20rpx; }
.p-30 { padding: 30rpx; }

/* Text */
.text-center { text-align: center; }
.text-sm { font-size: 24rpx; }
.text-base { font-size: 28rpx; }
.text-lg { font-size: 32rpx; }
.text-xl { font-size: 36rpx; }
.font-bold { font-weight: 600; }

/* Empty state */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
}
.empty-state .empty-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}
.empty-state .empty-text {
  color: #999;
  font-size: 28rpx;
}
</style>


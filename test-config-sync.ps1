Write-Host "=== 配置同步测试 ===" -ForegroundColor Cyan
Write-Host ""

# 登录获取token
Write-Host "1. 登录管理员账号..." -ForegroundColor Yellow
$loginBody = '{"username":"admin","password":"Admin123456"}'
$response = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/login' -Method Post -Body $loginBody -ContentType 'application/json'
$token = $response.data.accessToken
$headers = @{'Authorization' = "Bearer $token"}
Write-Host "   登录成功!" -ForegroundColor Green
Write-Host ""

# 创建测试配置
Write-Host "2. 创建测试配置..." -ForegroundColor Yellow
$configBody = @{
    configKey = "system.site_name"
    configValue = "我的健康助手"
    configType = "string"
    description = "网站显示名称"
    category = "系统"
    isPublic = $true
} | ConvertTo-Json

try {
    $result = Invoke-RestMethod -Uri 'http://localhost:8080/api/admin/config' -Method Post -Headers $headers -Body $configBody -ContentType 'application/json'
    Write-Host "   配置创建成功!" -ForegroundColor Green
} catch {
    Write-Host "   配置可能已存在，尝试更新..." -ForegroundColor Yellow
    $updateBody = @{ value = "我的健康助手" } | ConvertTo-Json
    $result = Invoke-RestMethod -Uri 'http://localhost:8080/api/admin/config/system.site_name' -Method Put -Headers $headers -Body $updateBody -ContentType 'application/json'
    Write-Host "   配置更新成功!" -ForegroundColor Green
}
Write-Host ""

# 验证公开配置
Write-Host "3. 验证公开配置API..." -ForegroundColor Yellow
$publicConfig = Invoke-RestMethod -Uri 'http://localhost:8080/api/public/config'
if ($publicConfig.data.'system.site_name') {
    Write-Host "   成功! 网站名称: $($publicConfig.data.'system.site_name')" -ForegroundColor Green
} else {
    Write-Host "   警告: 配置未公开或不存在" -ForegroundColor Yellow
}
Write-Host ""

# 测试删除功能
Write-Host "4. 测试配置删除..." -ForegroundColor Yellow
Write-Host "   创建临时配置..." -ForegroundColor Gray
$tempConfig = @{
    configKey = "test.temp_config"
    configValue = "临时配置"
    configType = "string"
    description = "测试删除功能"
    category = "系统"
    isPublic = $false
} | ConvertTo-Json

try {
    $result = Invoke-RestMethod -Uri 'http://localhost:8080/api/admin/config' -Method Post -Headers $headers -Body $tempConfig -ContentType 'application/json'
    Write-Host "   临时配置创建成功" -ForegroundColor Gray
    
    Write-Host "   删除临时配置..." -ForegroundColor Gray
    $result = Invoke-RestMethod -Uri 'http://localhost:8080/api/admin/config/test.temp_config' -Method Delete -Headers $headers
    Write-Host "   删除成功!" -ForegroundColor Green
} catch {
    Write-Host "   删除测试失败: $_" -ForegroundColor Red
}
Write-Host ""

Write-Host "=== 测试完成 ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Open frontend: http://localhost:3000" -ForegroundColor Gray
Write-Host "2. Refresh page to see site name changed" -ForegroundColor Gray
Write-Host "3. Edit config in admin panel" -ForegroundColor Gray
Write-Host "4. Refresh frontend to verify sync" -ForegroundColor Gray

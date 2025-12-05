-- 创建饮食计划任务表
CREATE TABLE IF NOT EXISTS diet_plan_tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id VARCHAR(50) UNIQUE NOT NULL COMMENT '任务ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '任务状态: pending, running, completed, failed, cancelled',
    progress INT DEFAULT 0 COMMENT '进度百分比',
    total_days INT NOT NULL COMMENT '总天数',
    current_day INT DEFAULT 0 COMMENT '当前生成到第几天',
    plan_id VARCHAR(50) COMMENT '生成的计划ID（完成后）',
    request_data TEXT COMMENT '请求参数JSON',
    error_message TEXT COMMENT '错误信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_task_id (task_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='饮食计划生成任务表';

-- 创建饮食计划历史记录表
CREATE TABLE IF NOT EXISTS diet_plan_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id VARCHAR(50) UNIQUE NOT NULL COMMENT '计划ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(200) NOT NULL COMMENT '计划标题',
    days INT NOT NULL COMMENT '计划天数',
    goal VARCHAR(50) COMMENT '目标',
    markdown_content LONGTEXT COMMENT 'Markdown内容',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_plan_id (plan_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='饮食计划历史记录表';

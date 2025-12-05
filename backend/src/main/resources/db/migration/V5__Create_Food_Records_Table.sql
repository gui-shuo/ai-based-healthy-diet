-- V5__Create_Food_Records_Table.sql
-- 创建饮食记录表

CREATE TABLE IF NOT EXISTS food_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    meal_type VARCHAR(20) NOT NULL COMMENT '餐次类型：BREAKFAST/LUNCH/DINNER/SNACK',
    food_name VARCHAR(100) NOT NULL COMMENT '食物名称',
    photo_url VARCHAR(500) COMMENT '食物照片URL',
    portion DECIMAL(8,2) COMMENT '份量（克）',
    calories DECIMAL(8,2) COMMENT '卡路里（千卡）',
    protein DECIMAL(8,2) COMMENT '蛋白质（克）',
    carbohydrates DECIMAL(8,2) COMMENT '碳水化合物（克）',
    fat DECIMAL(8,2) COMMENT '脂肪（克）',
    fiber DECIMAL(8,2) COMMENT '纤维（克）',
    record_time DATETIME NOT NULL COMMENT '记录时间',
    notes VARCHAR(500) COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_record_time (record_time),
    INDEX idx_user_meal (user_id, meal_type),
    INDEX idx_user_time (user_id, record_time),
    CONSTRAINT fk_food_record_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='饮食记录表';

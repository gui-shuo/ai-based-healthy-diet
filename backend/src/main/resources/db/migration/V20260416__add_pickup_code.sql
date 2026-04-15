-- Add pickup code fields to meal_orders
ALTER TABLE meal_orders ADD COLUMN pickup_code VARCHAR(6) DEFAULT NULL COMMENT '取餐码';
ALTER TABLE meal_orders ADD COLUMN pickup_code_verified_at DATETIME DEFAULT NULL COMMENT '取餐码核验时间';
CREATE INDEX idx_meal_order_pickup_code ON meal_orders (pickup_code);

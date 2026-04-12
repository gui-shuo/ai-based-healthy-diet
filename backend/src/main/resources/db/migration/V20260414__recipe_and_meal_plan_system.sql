-- =============================================
-- 健康食谱 & 健康餐 系统表
-- =============================================

-- 1. 食谱主表
CREATE TABLE IF NOT EXISTS recipes (
  id BIGINT NOT NULL AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL COMMENT '食谱标题',
  description TEXT COMMENT '食谱简介',
  cover_image VARCHAR(500) COMMENT '封面图URL',
  category VARCHAR(30) NOT NULL DEFAULT 'LUNCH' COMMENT '餐类: BREAKFAST/LUNCH/DINNER/SNACK/DESSERT/SOUP',
  cuisine_type VARCHAR(30) DEFAULT 'CHINESE' COMMENT '菜系: CHINESE/WESTERN/JAPANESE/KOREAN/SOUTHEAST_ASIAN/OTHER',
  diet_type VARCHAR(30) DEFAULT 'NORMAL' COMMENT '饮食类型: NORMAL/LOW_FAT/HIGH_PROTEIN/VEGETARIAN/VEGAN/KETO/MEDITERRANEAN',
  difficulty VARCHAR(20) DEFAULT 'MEDIUM' COMMENT '难度: EASY/MEDIUM/HARD',
  cooking_time_minutes INT DEFAULT 30 COMMENT '烹饪时间(分钟)',
  prep_time_minutes INT DEFAULT 15 COMMENT '准备时间(分钟)',
  servings INT DEFAULT 2 COMMENT '几人份',
  calories_per_serving INT DEFAULT 0 COMMENT '每份热量(kcal)',
  protein_per_serving DECIMAL(6,1) DEFAULT 0 COMMENT '每份蛋白质(g)',
  fat_per_serving DECIMAL(6,1) DEFAULT 0 COMMENT '每份脂肪(g)',
  carbs_per_serving DECIMAL(6,1) DEFAULT 0 COMMENT '每份碳水(g)',
  fiber_per_serving DECIMAL(6,1) DEFAULT 0 COMMENT '每份膳食纤维(g)',
  tips TEXT COMMENT '小贴士',
  is_featured TINYINT(1) DEFAULT 0 COMMENT '是否精选推荐',
  is_active TINYINT(1) DEFAULT 1 COMMENT '是否上架',
  view_count INT DEFAULT 0 COMMENT '浏览量',
  favorite_count INT DEFAULT 0 COMMENT '收藏数',
  rating_avg DECIMAL(2,1) DEFAULT 0.0 COMMENT '平均评分',
  rating_count INT DEFAULT 0 COMMENT '评分人数',
  created_by BIGINT COMMENT '创建者用户ID',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_category (category),
  KEY idx_diet_type (diet_type),
  KEY idx_cuisine_type (cuisine_type),
  KEY idx_is_featured (is_featured),
  KEY idx_is_active (is_active),
  KEY idx_rating_avg (rating_avg),
  FULLTEXT KEY ft_title_desc (title, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食谱';

-- 2. 食谱食材表
CREATE TABLE IF NOT EXISTS recipe_ingredients (
  id BIGINT NOT NULL AUTO_INCREMENT,
  recipe_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL COMMENT '食材名称',
  amount VARCHAR(50) COMMENT '用量',
  unit VARCHAR(20) COMMENT '单位',
  is_main TINYINT(1) DEFAULT 1 COMMENT '1=主料 0=辅料/调料',
  sort_order INT DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_recipe_id (recipe_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食谱食材';

-- 3. 食谱步骤表
CREATE TABLE IF NOT EXISTS recipe_steps (
  id BIGINT NOT NULL AUTO_INCREMENT,
  recipe_id BIGINT NOT NULL,
  step_number INT NOT NULL COMMENT '步骤序号',
  description TEXT NOT NULL COMMENT '步骤描述',
  image_url VARCHAR(500) COMMENT '步骤图片URL',
  tips VARCHAR(500) COMMENT '步骤小贴士',
  PRIMARY KEY (id),
  KEY idx_recipe_id (recipe_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食谱步骤';

-- 4. 食谱标签表
CREATE TABLE IF NOT EXISTS recipe_tags (
  id BIGINT NOT NULL AUTO_INCREMENT,
  recipe_id BIGINT NOT NULL,
  tag VARCHAR(50) NOT NULL COMMENT '标签',
  PRIMARY KEY (id),
  KEY idx_recipe_id (recipe_id),
  KEY idx_tag (tag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食谱标签';

-- 5. 食谱收藏表
CREATE TABLE IF NOT EXISTS recipe_favorites (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  recipe_id BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_recipe (user_id, recipe_id),
  KEY idx_recipe_id (recipe_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食谱收藏';

-- 6. 食谱评分表
CREATE TABLE IF NOT EXISTS recipe_ratings (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  recipe_id BIGINT NOT NULL,
  score TINYINT NOT NULL COMMENT '评分1-5',
  comment TEXT COMMENT '评价内容',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_recipe (user_id, recipe_id),
  KEY idx_recipe_id (recipe_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食谱评分';

-- 7. 健康餐计划主表
CREATE TABLE IF NOT EXISTS meal_plans (
  id BIGINT NOT NULL AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL COMMENT '计划标题',
  description TEXT COMMENT '计划简介',
  cover_image VARCHAR(500) COMMENT '封面图URL',
  plan_type VARCHAR(20) DEFAULT 'DAILY' COMMENT '类型: DAILY/WEEKLY',
  diet_goal VARCHAR(30) DEFAULT 'BALANCED' COMMENT '目标: BALANCED/WEIGHT_LOSS/MUSCLE_GAIN/WELLNESS/DIABETES_FRIENDLY/PREGNANCY/POSTPARTUM/ELDERLY',
  target_calories INT DEFAULT 0 COMMENT '目标总热量(kcal)',
  target_protein DECIMAL(6,1) DEFAULT 0 COMMENT '目标蛋白质(g)',
  target_fat DECIMAL(6,1) DEFAULT 0 COMMENT '目标脂肪(g)',
  target_carbs DECIMAL(6,1) DEFAULT 0 COMMENT '目标碳水(g)',
  duration_days INT DEFAULT 1 COMMENT '计划天数',
  suitable_crowd VARCHAR(200) COMMENT '适用人群描述',
  is_featured TINYINT(1) DEFAULT 0 COMMENT '是否精选推荐',
  is_active TINYINT(1) DEFAULT 1 COMMENT '是否上架',
  view_count INT DEFAULT 0 COMMENT '浏览量',
  favorite_count INT DEFAULT 0 COMMENT '收藏数',
  created_by BIGINT COMMENT '创建者用户ID',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_plan_type (plan_type),
  KEY idx_diet_goal (diet_goal),
  KEY idx_is_featured (is_featured),
  KEY idx_is_active (is_active),
  FULLTEXT KEY ft_title_desc (title, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康餐计划';

-- 8. 健康餐每日安排表
CREATE TABLE IF NOT EXISTS meal_plan_days (
  id BIGINT NOT NULL AUTO_INCREMENT,
  meal_plan_id BIGINT NOT NULL,
  day_number INT NOT NULL COMMENT '第几天',
  title VARCHAR(100) COMMENT '当日主题(如: 高蛋白日)',
  total_calories INT DEFAULT 0 COMMENT '当日总热量',
  total_protein DECIMAL(6,1) DEFAULT 0,
  total_fat DECIMAL(6,1) DEFAULT 0,
  total_carbs DECIMAL(6,1) DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_meal_plan_id (meal_plan_id),
  UNIQUE KEY uk_plan_day (meal_plan_id, day_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康餐每日安排';

-- 9. 健康餐每餐条目表
CREATE TABLE IF NOT EXISTS meal_plan_items (
  id BIGINT NOT NULL AUTO_INCREMENT,
  meal_plan_day_id BIGINT NOT NULL,
  meal_type VARCHAR(20) NOT NULL COMMENT 'BREAKFAST/LUNCH/DINNER/SNACK',
  recipe_id BIGINT COMMENT '关联食谱ID(可选)',
  food_name VARCHAR(200) NOT NULL COMMENT '食物名称',
  description VARCHAR(500) COMMENT '简要说明',
  portion VARCHAR(50) COMMENT '分量(如: 1碗, 200g)',
  calories INT DEFAULT 0,
  protein DECIMAL(6,1) DEFAULT 0,
  fat DECIMAL(6,1) DEFAULT 0,
  carbs DECIMAL(6,1) DEFAULT 0,
  image_url VARCHAR(500) COMMENT '食物图片',
  sort_order INT DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_meal_plan_day_id (meal_plan_day_id),
  KEY idx_recipe_id (recipe_id),
  KEY idx_meal_type (meal_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康餐每餐条目';

-- 10. 健康餐收藏表
CREATE TABLE IF NOT EXISTS meal_plan_favorites (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  meal_plan_id BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_plan (user_id, meal_plan_id),
  KEY idx_meal_plan_id (meal_plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康餐收藏';

-- =============================================
-- 插入示范数据
-- =============================================

-- 示范食谱1: 地中海风味烤鸡胸沙拉
INSERT INTO recipes (title, description, cover_image, category, cuisine_type, diet_type, difficulty, cooking_time_minutes, prep_time_minutes, servings, calories_per_serving, protein_per_serving, fat_per_serving, carbs_per_serving, fiber_per_serving, tips, is_featured, is_active) VALUES
('地中海风味烤鸡胸沙拉', '低脂高蛋白的经典减脂餐，富含优质蛋白和不饱和脂肪酸，搭配新鲜蔬菜和橄榄油，营养均衡又美味。', NULL, 'LUNCH', 'WESTERN', 'HIGH_PROTEIN', 'EASY', 25, 15, 2, 320, 35.0, 12.0, 15.0, 4.5, '鸡胸肉提前用柠檬汁和香料腌制30分钟口感更佳；烤箱预热至200°C效果最好。', 1, 1),
('香菇鸡肉粥', '养胃暖身的经典中式早餐，香菇的鲜味与鸡肉完美融合，适合全家人享用。', NULL, 'BREAKFAST', 'CHINESE', 'NORMAL', 'EASY', 45, 10, 3, 180, 12.0, 3.5, 28.0, 1.2, '大米提前浸泡1小时，煮出的粥更绵密；最后加入姜丝提鲜去腥。', 1, 1),
('日式三文鱼牛油果饭', '富含Omega-3脂肪酸的日式料理，新鲜三文鱼搭配牛油果，口感丝滑营养丰富。', NULL, 'LUNCH', 'JAPANESE', 'NORMAL', 'MEDIUM', 20, 15, 1, 450, 28.0, 22.0, 42.0, 3.0, '三文鱼选择刺身级别；寿司醋可用白醋加少量糖替代。', 1, 1),
('低卡西兰花鸡蛋杯', '减脂期完美早餐，高蛋白低碳水，制作简单快捷，10分钟搞定营养早餐。', NULL, 'BREAKFAST', 'WESTERN', 'LOW_FAT', 'EASY', 15, 5, 2, 150, 14.0, 8.0, 5.0, 2.0, '可以提前一晚准备好放冰箱，早上直接烤制即可。', 1, 1),
('红烧豆腐煲', '植物蛋白的优质来源，入味的豆腐外酥里嫩，搭配蔬菜营养全面，素食者的美味之选。', NULL, 'DINNER', 'CHINESE', 'VEGETARIAN', 'MEDIUM', 30, 10, 2, 220, 16.0, 10.0, 18.0, 3.5, '豆腐先煎至两面金黄再红烧，口感更好；老抽调色，生抽调味。', 0, 1),
('紫薯燕麦能量碗', '富含花青素和膳食纤维的超级碗，饱腹感强热量低，是减脂期间的理想加餐选择。', NULL, 'SNACK', 'WESTERN', 'LOW_FAT', 'EASY', 10, 5, 1, 200, 6.0, 4.0, 38.0, 5.0, '可加入蓝莓、奇亚籽等超级食物增加营养。', 0, 1),
('生酮牛排配芦笋', '高脂低碳的经典生酮餐，优质牛排搭配新鲜芦笋，满足口腹之欲的同时保持生酮状态。', NULL, 'DINNER', 'WESTERN', 'KETO', 'MEDIUM', 20, 10, 1, 480, 38.0, 32.0, 6.0, 2.5, '牛排室温静置20分钟再煎；中火煎每面3-4分钟达到medium-rare。', 1, 1),
('五彩时蔬鸡丝面', '色彩丰富营养均衡的快手午餐，多种蔬菜搭配鸡胸肉丝，兼顾口感和营养。', NULL, 'LUNCH', 'CHINESE', 'NORMAL', 'EASY', 20, 10, 2, 380, 25.0, 8.0, 52.0, 4.0, '面条煮至八分熟过凉水，口感更Q弹。', 0, 1);

-- 食谱食材
INSERT INTO recipe_ingredients (recipe_id, name, amount, unit, is_main, sort_order) VALUES
-- 地中海烤鸡胸沙拉
(1, '鸡胸肉', '300', 'g', 1, 1), (1, '混合生菜', '150', 'g', 1, 2), (1, '小番茄', '100', 'g', 1, 3),
(1, '黄瓜', '1', '根', 1, 4), (1, '牛油果', '1', '个', 1, 5), (1, '橄榄油', '2', '汤匙', 0, 6),
(1, '柠檬汁', '1', '汤匙', 0, 7), (1, '黑胡椒', '适量', NULL, 0, 8), (1, '海盐', '适量', NULL, 0, 9),
(1, '意大利综合香料', '1', '茶匙', 0, 10),
-- 香菇鸡肉粥
(2, '大米', '150', 'g', 1, 1), (2, '鸡胸肉', '100', 'g', 1, 2), (2, '干香菇', '5', '朵', 1, 3),
(2, '姜', '3', '片', 0, 4), (2, '葱花', '适量', NULL, 0, 5), (2, '盐', '适量', NULL, 0, 6), (2, '白胡椒粉', '少许', NULL, 0, 7),
-- 日式三文鱼牛油果饭
(3, '三文鱼（刺身级）', '150', 'g', 1, 1), (3, '牛油果', '1', '个', 1, 2), (3, '寿司米', '150', 'g', 1, 3),
(3, '寿司醋', '2', '汤匙', 0, 4), (3, '酱油', '1', '汤匙', 0, 5), (3, '芥末', '适量', NULL, 0, 6), (3, '白芝麻', '少许', NULL, 0, 7),
-- 低卡西兰花鸡蛋杯
(4, '鸡蛋', '4', '个', 1, 1), (4, '西兰花', '100', 'g', 1, 2), (4, '低脂奶酪', '30', 'g', 0, 3), (4, '盐', '适量', NULL, 0, 4), (4, '黑胡椒', '适量', NULL, 0, 5);

-- 食谱步骤
INSERT INTO recipe_steps (recipe_id, step_number, description, tips) VALUES
-- 地中海烤鸡胸沙拉
(1, 1, '鸡胸肉用盐、黑胡椒和意大利香料腌制15分钟。', '提前腌制效果更好'),
(1, 2, '烤箱预热200°C，鸡胸肉放入烤盘，烤15-18分钟至熟透。', '注意不要烤过干'),
(1, 3, '混合生菜洗净沥干，小番茄对半切，黄瓜切片，牛油果切块。', NULL),
(1, 4, '烤好的鸡胸肉切片，铺在蔬菜上。', '稍微放凉再切，汁水更多'),
(1, 5, '淋上橄榄油和柠檬汁调味即可享用。', NULL),
-- 香菇鸡肉粥
(2, 1, '大米洗净浸泡30分钟，干香菇泡发后切丁，鸡胸肉切小丁。', NULL),
(2, 2, '锅中加水，大火烧开后放入大米，转小火慢煮30分钟。', '期间需要搅拌防止粘底'),
(2, 3, '加入香菇丁和鸡肉丁，继续煮10分钟至鸡肉熟透。', NULL),
(2, 4, '加盐和白胡椒粉调味，撒上葱花即可。', NULL),
-- 日式三文鱼牛油果饭
(3, 1, '寿司米煮熟后拌入寿司醋，放凉备用。', '醋饭用扇子扇凉更有光泽'),
(3, 2, '三文鱼切成约1cm厚的片，牛油果切薄片。', '刀蘸水切三文鱼更顺滑'),
(3, 3, '碗中盛入醋饭，摆上三文鱼和牛油果。', NULL),
(3, 4, '淋上酱油，撒上白芝麻，配芥末食用。', NULL);

-- 食谱标签
INSERT INTO recipe_tags (recipe_id, tag) VALUES
(1, '减脂'), (1, '高蛋白'), (1, '快手菜'), (1, '沙拉'),
(2, '养胃'), (2, '早餐'), (2, '家常'), (2, '老少皆宜'),
(3, '日料'), (3, '三文鱼'), (3, 'Omega-3'), (3, '简单'),
(4, '减脂'), (4, '低碳水'), (4, '快手菜'), (4, '10分钟'),
(5, '素食'), (5, '豆腐'), (5, '家常'), (5, '下饭'),
(6, '减脂'), (6, '低卡'), (6, '代餐'), (6, '纤维'),
(7, '生酮'), (7, '牛排'), (7, '低碳水'), (7, '高脂'),
(8, '快手菜'), (8, '午餐'), (8, '均衡'), (8, '面食');

-- 示范健康餐计划1: 7日减脂餐
INSERT INTO meal_plans (title, description, plan_type, diet_goal, target_calories, target_protein, target_fat, target_carbs, duration_days, suitable_crowd, is_featured, is_active) VALUES
('7日科学减脂餐', '专业营养师精心设计的7天减脂饮食计划，每日热量控制在1500kcal左右，高蛋白低脂肪，帮助您科学减脂不反弹。', 'WEEKLY', 'WEIGHT_LOSS', 1500, 120.0, 45.0, 150.0, 7, '体重管理人群、想要科学减脂的人', 1, 1),
('增肌黄金餐单', '高蛋白增肌饮食方案，每日2200kcal，蛋白质摄入充足，碳水合理分配，助您高效增肌。', 'WEEKLY', 'MUSCLE_GAIN', 2200, 165.0, 70.0, 250.0, 7, '健身增肌人群、运动爱好者', 1, 1),
('一日轻食体验', '一天的轻食搭配方案，清淡健康又美味，适合想要尝试轻食的入门者。', 'DAILY', 'BALANCED', 1400, 60.0, 40.0, 180.0, 1, '上班族、轻食爱好者', 1, 1),
('糖友均衡周餐', '专为糖尿病患者设计的一周饮食计划，低GI食材搭配，控糖不控味，血糖更稳定。', 'WEEKLY', 'DIABETES_FRIENDLY', 1600, 80.0, 50.0, 160.0, 7, '2型糖尿病患者、血糖偏高人群', 1, 1),
('孕期营养日餐', '孕中期一日营养搭配，富含叶酸、铁、钙和DHA，满足妈妈和宝宝的双重需求。', 'DAILY', 'PREGNANCY', 2000, 80.0, 65.0, 260.0, 1, '孕中期准妈妈', 0, 1);

-- 健康餐每日安排 (7日减脂餐前3天 + 一日轻食)
INSERT INTO meal_plan_days (meal_plan_id, day_number, title, total_calories, total_protein, total_fat, total_carbs) VALUES
(1, 1, '高蛋白启动日', 1480, 125.0, 42.0, 145.0),
(1, 2, '低碳水代谢日', 1450, 118.0, 48.0, 130.0),
(1, 3, '均衡营养日', 1520, 122.0, 44.0, 155.0),
(3, 1, '轻食一日餐', 1400, 60.0, 40.0, 180.0);

-- 健康餐每餐条目
INSERT INTO meal_plan_items (meal_plan_day_id, meal_type, recipe_id, food_name, description, portion, calories, protein, fat, carbs, sort_order) VALUES
-- Day1 早餐
(1, 'BREAKFAST', 4, '低卡西兰花鸡蛋杯', '高蛋白低碳水早餐', '2个', 300, 28.0, 16.0, 10.0, 1),
(1, 'BREAKFAST', NULL, '全麦吐司', '优质碳水来源', '1片', 80, 3.0, 1.0, 15.0, 2),
(1, 'BREAKFAST', NULL, '脱脂牛奶', '补充钙质', '250ml', 90, 8.0, 0.5, 12.0, 3),
-- Day1 午餐
(1, 'LUNCH', 1, '地中海风味烤鸡胸沙拉', '低脂高蛋白主菜', '1份', 320, 35.0, 12.0, 15.0, 1),
(1, 'LUNCH', NULL, '糙米饭', '低GI主食', '100g', 130, 3.0, 1.0, 28.0, 2),
-- Day1 加餐
(1, 'SNACK', NULL, '希腊酸奶', '补充蛋白质', '150g', 100, 15.0, 0.5, 6.0, 1),
(1, 'SNACK', NULL, '坚果仁', '健康脂肪来源', '15g', 90, 3.0, 8.0, 3.0, 2),
-- Day1 晚餐
(1, 'DINNER', NULL, '清蒸鲈鱼', '优质蛋白、低脂', '150g', 180, 28.0, 5.0, 0.0, 1),
(1, 'DINNER', NULL, '蒜蓉西兰花', '补充维生素和膳食纤维', '150g', 60, 4.0, 2.0, 8.0, 2),
(1, 'DINNER', NULL, '紫薯', '优质碳水，富含花青素', '100g', 130, 2.0, 0.0, 30.0, 3),
-- 一日轻食
(4, 'BREAKFAST', NULL, '牛油果吐司配水波蛋', '优质脂肪+蛋白质', '1份', 350, 15.0, 18.0, 30.0, 1),
(4, 'BREAKFAST', NULL, '鲜榨橙汁', '补充维C', '200ml', 90, 1.0, 0.0, 22.0, 2),
(4, 'LUNCH', NULL, '藜麦鸡胸蔬菜碗', '超级谷物+优质蛋白', '1份', 420, 30.0, 12.0, 50.0, 1),
(4, 'SNACK', 6, '紫薯燕麦能量碗', '低卡高纤维加餐', '1份', 200, 6.0, 4.0, 38.0, 1),
(4, 'DINNER', NULL, '番茄蔬菜浓汤配全麦面包', '暖胃低卡晚餐', '1份', 340, 8.0, 6.0, 40.0, 1);

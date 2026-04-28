CREATE TABLE IF NOT EXISTS `game_werewolf_room` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '房间ID',
  `room_name` VARCHAR(100) NOT NULL COMMENT '房间名称',
  `password` VARCHAR(64) NULL COMMENT '房间密码（MD5）',
  `max_players` INT DEFAULT 12 COMMENT '最大人数',
  `status` INT DEFAULT 0 COMMENT '状态：0-等待中，1-游戏中，2-结束',
  `creator_id` BIGINT NOT NULL COMMENT '创建者ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='狼人杀房间表';

CREATE TABLE IF NOT EXISTS `game_werewolf_player` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '玩家ID',
  `room_id` BIGINT NOT NULL COMMENT '房间ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role` INT DEFAULT 0 COMMENT '角色：0-未分配，1-狼人，2-平民，3-预言家，4-女巫，5-猎人，6-白痴',
  `status` INT DEFAULT 0 COMMENT '状态：0-存活，1-死亡',
  `vote_target` BIGINT NULL COMMENT '投票目标',
  `is_speaking` BOOLEAN DEFAULT FALSE COMMENT '是否正在发言',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  CONSTRAINT `fk_werewolf_player_room` FOREIGN KEY (`room_id`) REFERENCES `game_werewolf_room`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='狼人杀玩家表';

CREATE TABLE IF NOT EXISTS `game_werewolf_role` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `camp` INT NOT NULL COMMENT '阵营：0-狼人，1-好人',
  `description` TEXT NULL COMMENT '角色描述',
  `icon` VARCHAR(255) NULL COMMENT '角色图标'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='狼人杀角色配置表';

CREATE TABLE IF NOT EXISTS `game_drawing_room` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '房间ID',
  `room_name` VARCHAR(100) NOT NULL COMMENT '房间名称',
  `password` VARCHAR(64) NULL COMMENT '房间密码',
  `max_players` INT DEFAULT 8 COMMENT '最大人数',
  `status` INT DEFAULT 0 COMMENT '状态：0-等待中，1-游戏中，2-结束',
  `current_round` INT DEFAULT 1 COMMENT '当前轮次',
  `current_drawer` BIGINT NULL COMMENT '当前绘画者',
  `creator_id` BIGINT NOT NULL COMMENT '创建者ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='你画我猜房间表';

CREATE TABLE IF NOT EXISTS `game_drawing_player` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '玩家ID',
  `room_id` BIGINT NOT NULL COMMENT '房间ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `score` INT DEFAULT 0 COMMENT '分数',
  `is_drawer` BOOLEAN DEFAULT FALSE COMMENT '是否为当前绘画者',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  CONSTRAINT `fk_drawing_player_room` FOREIGN KEY (`room_id`) REFERENCES `game_drawing_room`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='你画我猜玩家表';

CREATE TABLE IF NOT EXISTS `game_drawing_word` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '词语ID',
  `word` VARCHAR(100) NOT NULL COMMENT '词语内容',
  `category` VARCHAR(50) NULL COMMENT '分类',
  `difficulty` INT DEFAULT 1 COMMENT '难度：1-简单，2-中等，3-困难',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='你画我猜词语库';

CREATE TABLE IF NOT EXISTS `game_drawing_record` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
  `room_id` BIGINT NOT NULL COMMENT '房间ID',
  `round` INT NOT NULL COMMENT '轮次',
  `word_id` BIGINT NOT NULL COMMENT '词语ID',
  `drawer_id` BIGINT NOT NULL COMMENT '绘画者ID',
  `drawing_data` TEXT NULL COMMENT '绘画数据（JSON）',
  `guess_result` TEXT NULL COMMENT '猜测结果',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  CONSTRAINT `fk_drawing_record_room` FOREIGN KEY (`room_id`) REFERENCES `game_drawing_room`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_drawing_record_word` FOREIGN KEY (`word_id`) REFERENCES `game_drawing_word`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='你画我猜绘画记录表';

INSERT INTO `game_werewolf_role` (`id`, `role_name`, `camp`, `description`, `icon`) VALUES
(1, '狼人', 0, '夜间可以刀人，目标是杀死所有好人', NULL),
(2, '平民', 1, '没有特殊技能，白天通过投票找出狼人', NULL),
(3, '预言家', 1, '每晚可以查验一名玩家的身份', NULL),
(4, '女巫', 1, '拥有一瓶解药和一瓶毒药', NULL),
(5, '猎人', 1, '被投票出局时可以开枪带走一人', NULL),
(6, '白痴', 1, '被投票出局时可以翻牌免疫', NULL);

INSERT INTO `game_drawing_word` (`word`, `category`, `difficulty`) VALUES
('苹果', '水果', 1),
('香蕉', '水果', 1),
('西瓜', '水果', 1),
('葡萄', '水果', 1),
('草莓', '水果', 1),
('汽车', '交通工具', 1),
('飞机', '交通工具', 1),
('火车', '交通工具', 1),
('自行车', '交通工具', 1),
('公交车', '交通工具', 1),
('太阳', '自然', 1),
('月亮', '自然', 1),
('星星', '自然', 1),
('云朵', '自然', 1),
('彩虹', '自然', 1),
('房子', '建筑', 1),
('树木', '植物', 1),
('花朵', '植物', 1),
('动物', '动物', 1),
('老虎', '动物', 2),
('熊猫', '动物', 2),
('大象', '动物', 2),
('狮子', '动物', 2),
('海豚', '动物', 2),
('电脑', '电子产品', 2),
('手机', '电子产品', 2),
('电视', '电子产品', 2),
('相机', '电子产品', 2),
('耳机', '电子产品', 2),
('蛋糕', '食物', 1),
('汉堡', '食物', 1),
('披萨', '食物', 2),
('冰淇淋', '食物', 1),
('巧克力', '食物', 1),
('篮球', '运动', 1),
('足球', '运动', 1),
('羽毛球', '运动', 1),
('游泳', '运动', 2),
('跑步', '运动', 1),
('钢琴', '乐器', 2),
('吉他', '乐器', 2),
('小提琴', '乐器', 3),
('鼓', '乐器', 2),
('笛子', '乐器', 2),
('恐龙', '动物', 2),
('鲨鱼', '动物', 2),
('企鹅', '动物', 2),
('北极熊', '动物', 2),
('章鱼', '动物', 2),
('火箭', '科技', 3),
('机器人', '科技', 3),
('外星人', '科幻', 3),
('超级英雄', '影视', 3),
('城堡', '建筑', 2),
('金字塔', '建筑', 3),
('长城', '建筑', 3),
('埃菲尔铁塔', '建筑', 3),
('自由女神', '建筑', 3),
('圣诞老人', '节日', 2),
('礼物', '节日', 1),
('生日蛋糕', '节日', 1),
('烟花', '节日', 2),
('灯笼', '节日', 1),
('雨伞', '日常', 1),
('帽子', '日常', 1),
('眼镜', '日常', 1),
('手表', '日常', 2),
('钱包', '日常', 1),
('书本', '学习', 1),
('铅笔', '学习', 1),
('书包', '学习', 1),
('台灯', '学习', 1),
('眼镜', '学习', 1),
('蝴蝶', '动物', 1),
('蜜蜂', '动物', 1),
('蚂蚁', '动物', 1),
('蜗牛', '动物', 1),
('青蛙', '动物', 1),
('猫', '动物', 1),
('狗', '动物', 1),
('鱼', '动物', 1),
('鸟', '动物', 1),
('兔子', '动物', 1),
('冰淇淋', '食物', 1),
('薯条', '食物', 1),
('爆米花', '食物', 1),
('甜甜圈', '食物', 1),
('棉花糖', '食物', 1),
('圣诞树', '节日', 2),
('雪人', '节日', 1),
('南瓜', '节日', 2),
('彩蛋', '节日', 1),
('红包', '节日', 2),
('飞机', '交通工具', 1),
('火箭', '交通工具', 3),
('热气球', '交通工具', 2),
('潜艇', '交通工具', 3),
('坦克', '交通工具', 2),
('直升机', '交通工具', 2),
('闪电', '自然', 2),
('龙卷风', '自然', 3),
('火山', '自然', 3),
('瀑布', '自然', 2),
('冰山', '自然', 2),
('彩虹', '自然', 1),
('极光', '自然', 3),
('流星', '自然', 2),
('沙漠', '自然', 2),
('森林', '自然', 1),
('海洋', '自然', 1),
('山脉', '自然', 1);

ALTER TABLE `game_werewolf_player` ADD INDEX `idx_werewolf_player_room` (`room_id`);
ALTER TABLE `game_werewolf_player` ADD INDEX `idx_werewolf_player_user` (`user_id`);
ALTER TABLE `game_drawing_player` ADD INDEX `idx_drawing_player_room` (`room_id`);
ALTER TABLE `game_drawing_player` ADD INDEX `idx_drawing_player_user` (`user_id`);
ALTER TABLE `game_drawing_word` ADD INDEX `idx_drawing_word_category` (`category`);
ALTER TABLE `game_drawing_word` ADD INDEX `idx_drawing_word_difficulty` (`difficulty`);
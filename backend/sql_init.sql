-- ==========================================
-- Trader 校园二手交易平台 - 完整数据库结构 (最终报告版)
-- 修正说明：
-- 1. 统一移除 AUTO_INCREMENT，配合 MyBatis-Plus 雪花算法(ASSIGN_ID)
-- 2. 增加外键约束 (Foreign Keys) 以保证数据完整性
-- 3. 统一字符集与引擎配置
-- ==========================================

CREATE DATABASE IF NOT EXISTS trader DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
USE trader;

SET FOREIGN_KEY_CHECKS = 0;
-- 清理旧表
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `prod`;
DROP TABLE IF EXISTS `fav`;
DROP TABLE IF EXISTS `refresh_token`;
DROP TABLE IF EXISTS `order_entity`;
DROP TABLE IF EXISTS `order_item`;
DROP TABLE IF EXISTS `cart_item`;
DROP TABLE IF EXISTS `comment`;
DROP TABLE IF EXISTS `report`;
DROP TABLE IF EXISTS `notification`;
DROP TABLE IF EXISTS `price_history`;
DROP TABLE IF EXISTS `chat_message`;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. 用户表
CREATE TABLE `user` (
  `id` bigint NOT NULL COMMENT '用户ID (雪花算法)',
  `username` varchar(100) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '加密密码',
  `email` varchar(255) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(50) DEFAULT NULL COMMENT '手机号',
  `role` varchar(20) DEFAULT 'USER' COMMENT '角色',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像URL',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- 2. 商品表
CREATE TABLE `prod` (
  `id` bigint NOT NULL COMMENT '商品ID',
  `user_id` bigint NOT NULL COMMENT '卖家ID',
  `title` varchar(255) NOT NULL COMMENT '标题',
  `descr` text COMMENT '描述',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `stock` int DEFAULT 1 COMMENT '库存',
  `images` text COMMENT '图片列表',
  `category` varchar(100) DEFAULT NULL COMMENT '分类',
  `status` varchar(30) DEFAULT 'AVAILABLE' COMMENT '状态',
  `lat` double DEFAULT NULL COMMENT '纬度',
  `lng` double DEFAULT NULL COMMENT '经度',
  `view_count` int DEFAULT 0 COMMENT '浏览量',
  `created_at` datetime DEFAULT NULL COMMENT '发布时间',
  PRIMARY KEY (`id`),
  KEY `idx_prod_status` (`status`),
  KEY `idx_prod_user` (`user_id`),
  CONSTRAINT `fk_prod_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品信息表';

-- 3. 收藏表
CREATE TABLE `fav` (
  `id` bigint NOT NULL COMMENT '收藏ID',
  `user_id` bigint NOT NULL,
  `prod_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_fav_user_prod` (`user_id`,`prod_id`),
  CONSTRAINT `fk_fav_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_fav_prod` FOREIGN KEY (`prod_id`) REFERENCES `prod` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';

-- 4. 刷新令牌表
CREATE TABLE `refresh_token` (
  `id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `token` varchar(512) NOT NULL,
  `expires_at` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_rt_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='JWT刷新令牌表';

-- 5. 订单主表
CREATE TABLE `order_entity` (
  `id` bigint NOT NULL COMMENT '订单ID',
  `buyer_id` bigint NOT NULL COMMENT '买家ID',
  `seller_id` bigint DEFAULT NULL COMMENT '卖家ID',
  `status` varchar(50) DEFAULT 'CREATED' COMMENT '状态',
  `total_amount` decimal(10,2) NOT NULL COMMENT '总金额',
  `address` varchar(255) DEFAULT NULL COMMENT '收货地址',
  `created_at` bigint DEFAULT NULL COMMENT '创建时间戳',
  PRIMARY KEY (`id`),
  KEY `idx_order_buyer` (`buyer_id`),
  KEY `idx_order_seller` (`seller_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- 5.1 订单明细表
CREATE TABLE `order_item` (
  `id` bigint NOT NULL COMMENT '明细ID',
  `order_id` bigint NOT NULL COMMENT '关联订单ID',
  `prod_id` bigint NOT NULL COMMENT '关联商品ID',
  `prod_name` varchar(255) NOT NULL COMMENT '商品名快照',
  `prod_image` varchar(255) DEFAULT NULL COMMENT '图片快照',
  `price` decimal(10,2) NOT NULL COMMENT '成交单价',
  `quantity` int NOT NULL COMMENT '购买数量',
  PRIMARY KEY (`id`),
  KEY `idx_oi_order` (`order_id`),
  CONSTRAINT `fk_oi_order` FOREIGN KEY (`order_id`) REFERENCES `order_entity` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- 6. 购物车表
CREATE TABLE `cart_item` (
  `id` bigint NOT NULL COMMENT '购物车项ID',
  `user_id` bigint NOT NULL,
  `prod_id` bigint NOT NULL,
  `qty` int DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_cart_user_prod` (`user_id`,`prod_id`),
  CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cart_prod` FOREIGN KEY (`prod_id`) REFERENCES `prod` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 7. 评论表
CREATE TABLE `comment` (
  `id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `prod_id` bigint NOT NULL,
  `content` text,
  `rating` int DEFAULT 5,
  `created_at` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_prod` FOREIGN KEY (`prod_id`) REFERENCES `prod` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评论表';

-- 8. 举报表
CREATE TABLE `report` (
  `id` bigint NOT NULL,
  `reporter_id` bigint NOT NULL,
  `prod_id` bigint NOT NULL,
  `reason` text,
  `status` varchar(30) DEFAULT 'OPEN',
  `created_at` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报记录表';

-- 9. 通知表
CREATE TABLE `notification` (
  `id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  `body` text,
  `read` tinyint(1) DEFAULT 0,
  `created_at` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_notif_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知表';

-- 10. 价格历史
CREATE TABLE `price_history` (
  `id` bigint NOT NULL,
  `prod_id` bigint NOT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `changed_at` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='价格变动历史表';

-- 11. 聊天消息
CREATE TABLE `chat_message` (
  `id` bigint NOT NULL,
  `from_user_id` bigint NOT NULL,
  `to_user_id` bigint NOT NULL,
  `message` text,
  `created_at` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_chat_users` (`from_user_id`,`to_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天记录表';

-- ==================== 数据注入 (测试用) ====================
-- 注意：这里的ID是手动指定的，实际运行时Java代码会生成更大的雪花ID
INSERT INTO `user` (id, username, password, email, phone, role, avatar) VALUES
(1001, 'alice', '$2a$10$7QeYh1bV1J8KZq8Zr1aWJeYkqg3h2VZr3fY8Qz0b3Bz1b2c3d4eFG', 'alice@edu.com', '13800138001', 'USER', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'),
(1002, 'wzj',   '$2a$10$7QeYh1bV1J8KZq8Zr1aWJeYkqg3h2VZr3fY8Qz0b3Bz1b2c3d4eFG', 'wzj@edu.com',   '13800138002', 'ADMIN', 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'),
(1003, 'carol', '$2a$10$7QeYh1bV1J8KZq8Zr1aWJeYkqg3h2VZr3fY8Qz0b3Bz1b2c3d4eFG', 'carol@edu.com', '13800138003', 'USER', NULL);

INSERT INTO `prod` (id, user_id, title, descr, price, stock, images, category, status, created_at) VALUES
(2001, 1001, 'iPhone 13 Pro Max', '闲置手机，99新', 4500.00, 1, 'https://images.unsplash.com/photo-1632661674596-df8be070a5c5', '电子产品', 'AVAILABLE', NOW()),
(2002, 1001, 'Sony 耳机', '降噪耳机', 1200.00, 1, 'https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb', '电子产品', 'AVAILABLE', NOW()),
(2004, 1003, '考研数学全套', '带笔记，祝上岸', 50.00, 1, 'https://images.unsplash.com/photo-1544947950-fa07a98d237f', '书籍', 'AVAILABLE', NOW());
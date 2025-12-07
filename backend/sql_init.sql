-- ==========================================
-- Trader 校园二手交易平台 - 完整数据库结构 (最终报告版)
-- ==========================================

CREATE DATABASE IF NOT EXISTS trader DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
USE trader;

SET FOREIGN_KEY_CHECKS = 0;
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
  `id` bigint PRIMARY KEY COMMENT '用户ID',
  `username` varchar(100) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '加密后的密码',
  `email` varchar(255) COMMENT '邮箱',
  `phone` varchar(50) COMMENT '手机号',
  `role` varchar(20) DEFAULT 'USER' COMMENT '角色',
  `avatar` varchar(500) COMMENT '头像URL'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE UNIQUE INDEX idx_user_username ON `user`(username);

-- 2. 商品表 (新增 stock 字段)
CREATE TABLE `prod` (
  `id` bigint PRIMARY KEY COMMENT '商品ID',
  `user_id` bigint NOT NULL COMMENT '卖家ID',
  `title` varchar(255) NOT NULL COMMENT '商品标题',
  `descr` text COMMENT '商品描述',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `stock` int DEFAULT 1 COMMENT '库存 (符合报告)',
  `images` text COMMENT '图片列表',
  `category` varchar(100) COMMENT '分类',
  `status` varchar(30) DEFAULT 'AVAILABLE' COMMENT '状态',
  `lat` DOUBLE NULL COMMENT '纬度',
  `lng` DOUBLE NULL COMMENT '经度',
  `view_count` INT DEFAULT 0 COMMENT '浏览量',
  `created_at` datetime COMMENT '发布时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_prod_status ON `prod`(status);

-- 3. 收藏表
CREATE TABLE `fav` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `prod_id` bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE UNIQUE INDEX idx_fav_user_prod ON `fav`(user_id, prod_id);

-- 4. 刷新令牌表
CREATE TABLE `refresh_token` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `token` varchar(512) NOT NULL,
  `expires_at` bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 订单主表
CREATE TABLE `order_entity` (
  `id` bigint PRIMARY KEY COMMENT '订单ID',
  `buyer_id` bigint NOT NULL COMMENT '买家ID',
  `seller_id` bigint COMMENT '卖家ID',
  `status` varchar(50) COMMENT '状态',
  `total_amount` decimal(10,2) NOT NULL COMMENT '总金额',
  `address` varchar(255) COMMENT '收货地址',
  `created_at` bigint COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5.1 订单明细表
CREATE TABLE `order_item` (
  `id` bigint PRIMARY KEY COMMENT '明细ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `prod_id` bigint NOT NULL COMMENT '商品ID',
  `prod_name` varchar(255) NOT NULL,
  `prod_image` varchar(255),
  `price` decimal(10,2) NOT NULL,
  `quantity` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. 购物车表
CREATE TABLE `cart_item` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `prod_id` bigint NOT NULL,
  `qty` int DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE UNIQUE INDEX idx_cart_user_prod ON `cart_item`(user_id, prod_id);

-- 7. 评论表
CREATE TABLE `comment` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `prod_id` bigint NOT NULL,
  `content` text,
  `rating` int DEFAULT 5,
  `created_at` bigint
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. 举报表
CREATE TABLE `report` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `reporter_id` bigint NOT NULL,
  `prod_id` bigint NOT NULL,
  `reason` text,
  `status` varchar(30) DEFAULT 'OPEN',
  `created_at` bigint
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. 通知表
CREATE TABLE `notification` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `title` varchar(255),
  `body` text,
  `read` tinyint(1) DEFAULT 0,
  `created_at` bigint
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. 价格历史
CREATE TABLE `price_history` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `prod_id` bigint NOT NULL,
  `price` decimal(10,2),
  `changed_at` bigint
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. 聊天消息
CREATE TABLE `chat_message` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `from_user_id` bigint NOT NULL,
  `to_user_id` bigint NOT NULL,
  `message` text,
  `created_at` bigint
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== 数据注入 ====================
INSERT INTO `user` (id, username, password, email, phone, role, avatar) VALUES
(1001, 'alice', '$2a$10$7QeYh1bV1J8KZq8Zr1aWJeYkqg3h2VZr3fY8Qz0b3Bz1b2c3d4eFG', 'alice@edu.com', '13800138001', 'USER', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'),
(1002, 'wzj',   '$2a$10$7QeYh1bV1J8KZq8Zr1aWJeYkqg3h2VZr3fY8Qz0b3Bz1b2c3d4eFG', 'wzj@edu.com',   '13800138002', 'ADMIN', 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'),
(1003, 'carol', '$2a$10$7QeYh1bV1J8KZq8Zr1aWJeYkqg3h2VZr3fY8Qz0b3Bz1b2c3d4eFG', 'carol@edu.com', '13800138003', 'USER', NULL);

-- 商品数据 (Stock 默认为 1)
INSERT INTO `prod` (id, user_id, title, descr, price, stock, images, category, status, created_at) VALUES
(2001, 1001, 'iPhone 13 Pro Max', '闲置手机', 4500.00, 1, 'https://images.unsplash.com/photo-1632661674596-df8be070a5c5', '电子产品', 'AVAILABLE', NOW()),
(2002, 1001, 'Sony 耳机', '95新', 1200.00, 1, 'https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb', '电子产品', 'AVAILABLE', NOW()),
(2004, 1003, '考研数学全套', '带笔记', 50.00, 1, 'https://images.unsplash.com/photo-1544947950-fa07a98d237f', '书籍', 'AVAILABLE', NOW());
CREATE DATABASE IF NOT EXISTS trader DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
USE trader;

-- 為了確保表結構更新，開發階段我們先刪除舊表 (注意：這會清空數據)
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `prod`;
DROP TABLE IF EXISTS `fav`;
DROP TABLE IF EXISTS `refresh_token`;
DROP TABLE IF EXISTS `order_entity`;
DROP TABLE IF EXISTS `cart_item`;
DROP TABLE IF EXISTS `comment`;
DROP TABLE IF EXISTS `report`;
DROP TABLE IF EXISTS `notification`;
DROP TABLE IF EXISTS `price_history`;
DROP TABLE IF EXISTS `chat_message`;

CREATE TABLE `user` (
  `id` bigint PRIMARY KEY,
  `username` varchar(100),
  `password` varchar(255),
  `email` varchar(255),
  `phone` varchar(50)
);

-- 將原本透過 ALTER TABLE 添加的欄位 (lat, lng, view_count) 直接合併到這裡
CREATE TABLE `prod` (
  `id` bigint PRIMARY KEY,
  `user_id` bigint,
  `title` varchar(255),
  `descr` text,
  `price` decimal(10,2),
  `images` text,
  `category` varchar(100),
  `status` varchar(30),
  `created_at` datetime,
  `lat` DOUBLE NULL,
  `lng` DOUBLE NULL,
  `view_count` INT DEFAULT 0
);

CREATE TABLE `fav` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint,
  `prod_id` bigint
);


CREATE TABLE `refresh_token` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint,
  `token` varchar(512),
  `expires_at` bigint
);

-- sample data
INSERT INTO `user` (id, username, password, email, phone) VALUES
(1001, 'alice', '$2a$10$7QeYh1bV1J8KZq8Zr1aWJeYkqg3h2VZr3fY8Qz0b3Bz1b2c3d4eFG', 'alice@example.com', '1234567890'),
(1002, 'bob', '$2a$10$7QeYh1bV1J8KZq8Zr1aWJeYkqg3h2VZr3fY8Qz0b3Bz1b2c3d4eFG', 'bob@example.com', '0987654321');

-- 插入數據時也需要包含新定義的欄位預設值（如果有需要），或者依賴 NULL/DEFAULT
INSERT INTO `prod` (id, user_id, title, descr, price, images, category, status, created_at, lat, lng, view_count) VALUES
(2001, 1001, 'Calculus Textbook', 'Used calculus textbook, good condition', 30.00, '', 'Books', 'AVAILABLE', NOW(), 0.0, 0.0, 0),
(2002, 1002, 'Wireless Mouse', 'Lightly used wireless mouse', 15.50, '', 'Electronics', 'AVAILABLE', NOW(), 0.0, 0.0, 0);


CREATE TABLE `order_entity` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `buyer_id` bigint,
  `seller_id` bigint,
  `prod_id` bigint,
  `status` varchar(50),
  `price` decimal(10,2),
  `created_at` bigint
);

CREATE TABLE `cart_item` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint,
  `prod_id` bigint,
  `qty` int
);

CREATE TABLE `comment` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint,
  `prod_id` bigint,
  `content` text,
  `rating` int,
  `created_at` bigint
);

CREATE TABLE `report` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `reporter_id` bigint,
  `prod_id` bigint,
  `reason` text,
  `status` varchar(30),
  `created_at` bigint
);

CREATE TABLE `notification` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint,
  `title` varchar(255),
  `body` text,
  `read` tinyint(1) DEFAULT 0,
  `created_at` bigint
);

CREATE TABLE `price_history` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `prod_id` bigint,
  `price` decimal(10,2),
  `changed_at` bigint
);


CREATE TABLE `chat_message` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `from_user_id` bigint,
  `to_user_id` bigint,
  `message` text,
  `created_at` bigint
);
CREATE DATABASE IF NOT EXISTS trader DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
USE trader;

CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint PRIMARY KEY,
  `username` varchar(100),
  `password` varchar(255),
  `email` varchar(255),
  `phone` varchar(50)
);

CREATE TABLE IF NOT EXISTS `prod` (
  `id` bigint PRIMARY KEY,
  `user_id` bigint,
  `title` varchar(255),
  `descr` text,
  `price` decimal(10,2),
  `images` text,
  `category` varchar(100),
  `status` varchar(30),
  `created_at` datetime
);

CREATE TABLE IF NOT EXISTS `fav` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint,
  `prod_id` bigint
);


CREATE TABLE IF NOT EXISTS `refresh_token` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint,
  `token` varchar(512),
  `expires_at` bigint
);

-- sample data
USE trader;
INSERT INTO `user` (id, username, password, email, phone) VALUES
(1001, 'alice', '$2a$10$7QeYh1bV1J8KZq8Zr1aWJeYkqg3h2VZr3fY8Qz0b3Bz1b2c3d4eFG', 'alice@example.com', '1234567890'),
(1002, 'bob', '$2a$10$7QeYh1bV1J8KZq8Zr1aWJeYkqg3h2VZr3fY8Qz0b3Bz1b2c3d4eFG', 'bob@example.com', '0987654321');

INSERT INTO `prod` (id, user_id, title, descr, price, images, category, status, created_at) VALUES
(2001, 1001, 'Calculus Textbook', 'Used calculus textbook, good condition', 30.00, '', 'Books', 'AVAILABLE', NOW()),
(2002, 1002, 'Wireless Mouse', 'Lightly used wireless mouse', 15.50, '', 'Electronics', 'AVAILABLE', NOW());


ALTER TABLE `prod` ADD COLUMN IF NOT EXISTS `lat` DOUBLE NULL, ADD COLUMN IF NOT EXISTS `lng` DOUBLE NULL, ADD COLUMN IF NOT EXISTS `view_count` INT DEFAULT 0;

CREATE TABLE IF NOT EXISTS `order_entity` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `buyer_id` bigint,
  `seller_id` bigint,
  `prod_id` bigint,
  `status` varchar(50),
  `price` decimal(10,2),
  `created_at` bigint
);

CREATE TABLE IF NOT EXISTS `cart_item` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint,
  `prod_id` bigint,
  `qty` int
);

CREATE TABLE IF NOT EXISTS `comment` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint,
  `prod_id` bigint,
  `content` text,
  `rating` int,
  `created_at` bigint
);

CREATE TABLE IF NOT EXISTS `report` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `reporter_id` bigint,
  `prod_id` bigint,
  `reason` text,
  `status` varchar(30),
  `created_at` bigint
);

CREATE TABLE IF NOT EXISTS `notification` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint,
  `title` varchar(255),
  `body` text,
  `read` tinyint(1) DEFAULT 0,
  `created_at` bigint
);

CREATE TABLE IF NOT EXISTS `price_history` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `prod_id` bigint,
  `price` decimal(10,2),
  `changed_at` bigint
);


CREATE TABLE IF NOT EXISTS `chat_message` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `from_user_id` bigint,
  `to_user_id` bigint,
  `message` text,
  `created_at` bigint
);

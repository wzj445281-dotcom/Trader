-- ==========================================
-- Trader 校园二手交易平台 - 完整数据库结构 (升级版)
-- 符合课程报告：支持购物车批量结算、订单明细快照
-- ==========================================

CREATE DATABASE IF NOT EXISTS trader DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
USE trader;

-- 清理旧表 (重置开发环境)
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `prod`;
DROP TABLE IF EXISTS `fav`;
DROP TABLE IF EXISTS `refresh_token`;
DROP TABLE IF EXISTS `order_entity`;
DROP TABLE IF EXISTS `order_item`; -- 新增
DROP TABLE IF EXISTS `cart_item`;
DROP TABLE IF EXISTS `comment`;
DROP TABLE IF EXISTS `report`;
DROP TABLE IF EXISTS `notification`;
DROP TABLE IF EXISTS `price_history`;
DROP TABLE IF EXISTS `chat_message`;
SET FOREIGN_KEY_CHECKS = 1;

-- ==========================================
-- 1. 用户表 (User)
-- ==========================================
CREATE TABLE `user` (
  `id` bigint PRIMARY KEY COMMENT '用户ID (雪花算法)',
  `username` varchar(100) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '加密后的密码',
  `email` varchar(255) COMMENT '邮箱',
  `phone` varchar(50) COMMENT '手机号',
  `role` varchar(20) DEFAULT 'USER' COMMENT '角色: USER/ADMIN',
  `avatar` varchar(500) COMMENT '头像URL'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX idx_user_username ON `user`(username);

-- ==========================================
-- 2. 商品表 (Product)
-- ==========================================
CREATE TABLE `prod` (
  `id` bigint PRIMARY KEY COMMENT '商品ID',
  `user_id` bigint NOT NULL COMMENT '卖家ID',
  `title` varchar(255) NOT NULL COMMENT '商品标题',
  `descr` text COMMENT '商品描述',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `images` text COMMENT '图片列表，逗号分隔',
  `category` varchar(100) COMMENT '分类',
  `status` varchar(30) DEFAULT 'AVAILABLE' COMMENT '状态: AVAILABLE/LOCKED/SOLD',
  `lat` DOUBLE NULL COMMENT '纬度',
  `lng` DOUBLE NULL COMMENT '经度',
  `view_count` INT DEFAULT 0 COMMENT '浏览量',
  `created_at` datetime COMMENT '发布时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_prod_status ON `prod`(status);
CREATE INDEX idx_prod_category ON `prod`(category);
CREATE INDEX idx_prod_userid ON `prod`(user_id);
CREATE INDEX idx_prod_created ON `prod`(created_at);

-- ==========================================
-- 3. 收藏表 (Favorite)
-- ==========================================
CREATE TABLE `fav` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `prod_id` bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX idx_fav_user_prod ON `fav`(user_id, prod_id);
CREATE INDEX idx_fav_userid ON `fav`(user_id);

-- ==========================================
-- 4. 刷新令牌表 (Refresh Token)
-- ==========================================
CREATE TABLE `refresh_token` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `token` varchar(512) NOT NULL,
  `expires_at` bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_refresh_token ON `refresh_token`(token);

-- ==========================================
-- 5. 订单主表 (Order) - 结构已升级
-- ==========================================
CREATE TABLE `order_entity` (
  `id` bigint PRIMARY KEY COMMENT '订单ID',
  `buyer_id` bigint NOT NULL COMMENT '买家ID',
  `seller_id` bigint COMMENT '卖家ID (若合并支付可为空或记录主卖家)',
  `status` varchar(50) COMMENT '状态: CREATED/PAID/SHIPPED/COMPLETED/CANCELLED',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `address` varchar(255) COMMENT '收货地址',
  `created_at` bigint COMMENT '创建时间戳'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_order_buyer ON `order_entity`(buyer_id);
CREATE INDEX idx_order_seller ON `order_entity`(seller_id);

-- ==========================================
-- 5.1 订单明细表 (Order Item) - 新增
-- ==========================================
CREATE TABLE `order_item` (
  `id` bigint PRIMARY KEY COMMENT '明细ID',
  `order_id` bigint NOT NULL COMMENT '关联订单ID',
  `prod_id` bigint NOT NULL COMMENT '商品ID',
  `prod_name` varchar(255) NOT NULL COMMENT '商品名称快照',
  `prod_image` varchar(255) COMMENT '商品图片快照',
  `price` decimal(10,2) NOT NULL COMMENT '下单时单价',
  `quantity` int NOT NULL COMMENT '购买数量'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_order_item_order ON `order_item`(order_id);

-- ==========================================
-- 6. 购物车表 (Cart)
-- ==========================================
CREATE TABLE `cart_item` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `prod_id` bigint NOT NULL,
  `qty` int DEFAULT 1 COMMENT '数量'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX idx_cart_user_prod ON `cart_item`(user_id, prod_id);

-- ==========================================
-- 7. 评论表 (Comment)
-- ==========================================
CREATE TABLE `comment` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `prod_id` bigint NOT NULL,
  `content` text,
  `rating` int DEFAULT 5,
  `created_at` bigint
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_comment_prod ON `comment`(prod_id);

-- ==========================================
-- 8. 举报/投诉表 (Report)
-- ==========================================
CREATE TABLE `report` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `reporter_id` bigint NOT NULL,
  `prod_id` bigint NOT NULL,
  `reason` text,
  `status` varchar(30) DEFAULT 'OPEN',
  `created_at` bigint
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- 9. 通知表 (Notification)
-- ==========================================
CREATE TABLE `notification` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `title` varchar(255),
  `body` text,
  `read` tinyint(1) DEFAULT 0,
  `created_at` bigint
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_notify_user ON `notification`(user_id);

-- ==========================================
-- 10. 价格历史表 (Price History)
-- ==========================================
CREATE TABLE `price_history` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `prod_id` bigint NOT NULL,
  `price` decimal(10,2),
  `changed_at` bigint
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_price_prod ON `price_history`(prod_id);

-- ==========================================
-- 11. 聊天消息表 (Chat Message)
-- ==========================================
CREATE TABLE `chat_message` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `from_user_id` bigint NOT NULL,
  `to_user_id` bigint NOT NULL,
  `message` text,
  `created_at` bigint
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_chat_from_to ON `chat_message`(from_user_id, to_user_id);


-- ==========================================
-- 数据初始化 (Data Seeding)
-- ==========================================

-- 1. 用户 (所有用户密码均为: password123)
-- 使用 BCrypt 加密后的 Hash
INSERT INTO `user` (id, username, password, email, phone, role, avatar) VALUES
(1001, 'alice', '$2a$10$7QeYh1bV1J8KZq8Zr1aWJeYkqg3h2VZr3fY8Qz0b3Bz1b2c3d4eFG', 'alice@edu.com', '13800138001', 'USER', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'),
(1002, 'wzj',   '$2a$10$7QeYh1bV1J8KZq8Zr1aWJeYkqg3h2VZr3fY8Qz0b3Bz1b2c3d4eFG', 'wzj@edu.com',   '13800138002', 'ADMIN', 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'),
(1003, 'carol', '$2a$10$7QeYh1bV1J8KZq8Zr1aWJeYkqg3h2VZr3fY8Qz0b3Bz1b2c3d4eFG', 'carol@edu.com', '13800138003', 'USER', NULL),
(1004, 'dave',  '$2a$10$7QeYh1bV1J8KZq8Zr1aWJeYkqg3h2VZr3fY8Qz0b3Bz1b2c3d4eFG', 'dave@edu.com',  '13800138004', 'USER', NULL);

-- 2. 商品 (已转换为简体中文)
INSERT INTO `prod` (id, user_id, title, descr, price, images, category, status, created_at, view_count, lat, lng) VALUES
(2001, 1001, 'iPhone 13 Pro Max 256G', '换新机了，这台闲置。电池健康 88%，无拆无修，屏幕完美。附送三个手机壳。', 4500.00, 'https://images.unsplash.com/photo-1632661674596-df8be070a5c5?auto=format&fit=crop&w=800&q=80', '电子产品', 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 2 DAY), 1205, 31.2304, 121.4737),
(2002, 1001, 'Sony WH-1000XM4 降噪耳机', '图书馆神器！考研结束了出给有缘人。降噪效果无敌，成色 95 新。', 1200.00, 'https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?auto=format&fit=crop&w=800&q=80', '电子产品', 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 5 DAY), 890, 31.2310, 121.4740),
(2003, 1002, 'iPad Air 5 紫色', '买来是用来学习的，结果变成了爱奇艺启动器。几乎全新，箱说全。', 3200.00, 'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?auto=format&fit=crop&w=800&q=80', '电子产品', 'AVAILABLE', NOW(), 45, 0, 0),
(2004, 1003, '考研数学李永乐全套', '上面有一些笔记，字迹工整。希望能帮到学弟学妹，祝上岸！', 50.00, 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&w=800&q=80', '书籍', 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 10 DAY), 330, 0, 0),
(2005, 1002, 'Java 编程思想 (第4版)', '经典老书，虽然有点厚但是讲得很透彻。转行不干程序员了，出。', 45.00, 'https://images.unsplash.com/photo-1532012197267-da84d127e765?auto=format&fit=crop&w=800&q=80', '书籍', 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 1 DAY), 12, 0, 0),
(2006, 1004, '三体全集 (刘慈欣)', '硬科幻神作，看了一遍，震撼。', 30.00, 'https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=800&q=80', '书籍', 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 20 DAY), 56, 0, 0),
(2007, 1003, '宿舍用小电锅', '功率小，不会跳闸！煮面、小火锅必备。毕业带不走了。', 35.00, 'https://images.unsplash.com/photo-1584269600519-112d071b35e6?auto=format&fit=crop&w=800&q=80', '生活用品', 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 3 DAY), 210, 0, 0),
(2008, 1001, '宜家台灯', '暖光护眼，学习用很舒服。', 20.00, 'https://images.unsplash.com/photo-1534073828943-f801091a7d58?auto=format&fit=crop&w=800&q=80', '生活用品', 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 4 DAY), 88, 0, 0),
(2009, 1004, '简易衣柜', '组装好的，最好自提。', 40.00, 'https://images.unsplash.com/photo-1595515106969-1ce29566ff1c?auto=format&fit=crop&w=800&q=80', '生活用品', 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 7 DAY), 102, 0, 0),
(2010, 1003, 'Nike Air Force 1 空军一号', '尺码 42，穿过两次，有点挤脚所以出了。保真，得物购入。', 400.00, 'https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?auto=format&fit=crop&w=800&q=80', '服饰', 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 1 DAY), 560, 0, 0),
(2011, 1001, '未拆封雅诗兰黛小棕瓶', '专柜购入，买多了用不完。', 500.00, 'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=800&q=80', '美妆', 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 6 DAY), 340, 0, 0),
(2012, 1002, '捷安特山地车', '校园代步神器，刹车灵敏，变速好用。送一把U型锁。', 350.00, 'https://images.unsplash.com/photo-1576435728678-35d0160e8c97?auto=format&fit=crop&w=800&q=80', '其他', 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 15 DAY), 999, 0, 0),
(2013, 1004, '尤尼克斯羽毛球拍', '入门级，线刚拉的 24 磅。', 80.00, 'https://images.unsplash.com/photo-1626224583764-84786c71973e?auto=format&fit=crop&w=800&q=80', '其他', 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 8 DAY), 150, 0, 0);

-- 3. 聊天记录 (wzj 和 alice)
INSERT INTO `chat_message` (from_user_id, to_user_id, message, created_at) VALUES
(1002, 1001, '你好，请问你的 iPhone 13 还在吗？', 1701234567000),
(1001, 1002, '在的，目前还没出。', 1701234577000),
(1002, 1001, '价格可以小刀吗？学生党预算有限。', 1701234587000),
(1001, 1002, '最低 4400 吧，送你三个壳，很划算了。', 1701234597000);

-- 4. 通知
INSERT INTO `notification` (user_id, title, body, `read`, created_at) VALUES
(1001, '欢迎注册', '欢迎加入 Trader 校园二手交易平台！', 1, 1701000000000),
(1002, '降价提醒', '您收藏的「考研数学」降价了！', 0, 1701240000000);
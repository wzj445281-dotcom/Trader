
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

-- ==================== 数据注入 (使用 test2.sql 完整数据) ====================
-- 密码统一为: qq123123 (哈希值：$2a$10$7QeYh1bV1J8KZq8Zr1aWJeYkqg3h2VZr3fY8Qz0b3Bz1b2c3d4eFG)

INSERT INTO `user` (id, username, password, email, phone, role, avatar) VALUES
(1001, 'alice', 'qq123123', 'alice@edu.com', '13800138001', 'USER', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'),
(1002, 'bob',   'qq123123', 'bob@edu.com',   '13800138002', 'USER', 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'),
(1003, 'admin', 'qq123123', 'admin@edu.com', '13800138000', 'ADMIN', 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png');

INSERT INTO `prod` (id, user_id, title, descr, price, stock, images, category, status, view_count, created_at) VALUES
(2001, 1002, 'iPhone 13 Pro Max 256G 远峰蓝', '换 15 了所以出。国行原装，电池健康 88%，一直带壳贴膜，无划痕。附送三个手机壳和原装充电线。', 4500.00, 1, 'https://images.unsplash.com/photo-1632661674596-df8be070a5c5?auto=format&fit=crop&w=800&q=80', '电子产品', 'AVAILABLE', 1205, NOW()),
(2002, 1002, 'Sony WH-1000XM4 降噪耳机', '图书馆考研神器！降噪效果无敌，成色 95 新，耳罩无磨损。', 1200.00, 1, 'https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?auto=format&fit=crop&w=800&q=80', '电子产品', 'AVAILABLE', 890, NOW()),
(2003, 1003, '考研数学李永樂全套资料', '24版复习全书+660题，上面有学霸笔记，字跡工整。祝学弟学妹上岸！', 45.00, 1, 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&w=800&q=80', '书籍', 'AVAILABLE', 330, NOW()),
(2004, 1003, '宿舍用小电锅 (不跳闸)', '功率 600W，煮面、小火锅必备。毕业带不走了，送汤勺和碗。', 35.00, 1, 'https://images.unsplash.com/photo-1584269600519-112d071b35e6?auto=format&fit=crop&w=800&q=80', '生活用品', 'AVAILABLE', 210, NOW()),
(2005, 1002, 'Nike Air Force 1 空军一号', '尺码 42，得物购入，穿过两次有点挤脚。支持鉴定，假一赔三。', 400.00, 1, 'https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?auto=format&fit=crop&w=800&q=80', '服飾', 'AVAILABLE', 560, NOW()),
(2006, 1003, '捷安特山地车', '海大校区代步神器，刹车灵敏，变速好用。送一把U型锁。', 350.00, 1, 'https://images.unsplash.com/photo-1576435728678-35d0160e8c97?auto=format&fit=crop&w=800&q=80', '其他', 'AVAILABLE', 99, NOW());

INSERT INTO `cart_item` (id, user_id, prod_id, qty) VALUES
(5001, 1001, 2004, 1),
(5002, 1001, 2005, 1);

INSERT INTO `order_entity` (id, buyer_id, seller_id, status, total_amount, address, created_at) VALUES
(3001, 1001, 1002, 'COMPLETED', 1200.00, '海大主校区海宁B栋301', 1701000000000),
(3002, 1001, 1003, 'PAID', 45.00, '海大主校区海宁B栋301', 1701230000000);

INSERT INTO `order_item` (id, order_id, prod_id, prod_name, prod_image, price, quantity) VALUES
(4001, 3001, 2002, 'Sony WH-1000XM4 降噪耳机', 'https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?auto=format&fit=crop&w=800&q=80', 1200.00, 1),
(4002, 3002, 2003, '考研数学李永樂全套资料', 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&w=800&q=80', 45.00, 1);

INSERT INTO `chat_message` (id, from_user_id, to_user_id, message, created_at) VALUES
(6001, 1002, 1001, '你好，请问你发布的雅诗兰黛还在吗？', 1701234567000),
(6002, 1001, 1002, '在的，专柜正品，未拆封。', 1701234577000),
(6003, 1002, 1001, '我是学生，预算有限，450 可以吗？', 1701234587000),
(6004, 1001, 1002, '可以，海大校内面交吧。', 1701234597000);

INSERT INTO `comment` (id, user_id, prod_id, content, rating, created_at) VALUES
(7001, 1002, 2001, '楼主，电池健康度多少？有维修记录吗？', 5, 1701234567000),
(7002, 1003, 2001, '排队，如果楼上不要踢我。', 5, 1701240000000);
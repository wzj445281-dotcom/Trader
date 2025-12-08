USE trader;

-- ==========================================
-- 0. 清空旧数据 (防止主键冲突)
-- ==========================================
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `user`;
TRUNCATE TABLE `prod`;
TRUNCATE TABLE `order_entity`;
TRUNCATE TABLE `order_item`;
TRUNCATE TABLE `cart_item`;
TRUNCATE TABLE `chat_message`;
TRUNCATE TABLE `comment`;
TRUNCATE TABLE `notification`;
SET FOREIGN_KEY_CHECKS = 1;

-- ==========================================
-- 1. 初始化演示用户 (密码统一为: password123)
-- ==========================================
-- 哈希值 $2a$10$7QeYh1bV1J8KZq8Zr1aWJeYkqg3h2VZr3fY8Qz0b3Bz1b2c3d4eFG 对应明文密码 'password123'
INSERT INTO `user` (id, username, password, email, phone, role, avatar) VALUES
(1001, 'alice', '$2a$10$7QeYh1bV1J8KZq8Zr1aWJeYkqg3h2VZr3fY8Qz0b3Bz1b2c3d4eFG', 'alice@edu.com', '13800138001', 'USER', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'),
(1002, 'bob',   '$2a$10$7QeYh1bV1J8KZq8Zr1aWJeYkqg3h2VZr3fY8Qz0b3Bz1b2c3d4eFG', 'bob@edu.com',   '13800138002', 'USER', 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'),
(1003, 'admin', '$2a$10$7QeYh1bV1J8KZq8Zr1aWJeYkqg3h2VZr3fY8Qz0b3Bz1b2c3d4eFG', 'admin@edu.com', '13800138000', 'ADMIN', 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png');

-- ==========================================
-- 2. 初始化热门商品 (使用 Unsplash 真实图片)
-- ==========================================
INSERT INTO `prod` (id, user_id, title, descr, price, stock, images, category, status, view_count, created_at) VALUES
(2001, 1002, 'iPhone 13 Pro Max 256G 远峰蓝', '换 15 了所以出。国行原装，电池健康 88%，一直带壳贴膜，无划痕。附送三个手机壳和原装充电线。', 4500.00, 1, 'https://images.unsplash.com/photo-1632661674596-df8be070a5c5?auto=format&fit=crop&w=800&q=80', '电子产品', 'AVAILABLE', 1205, NOW()),
(2002, 1002, 'Sony WH-1000XM4 降噪耳机', '图书馆考研神器！降噪效果无敌，成色 95 新，耳罩无磨损。', 1200.00, 1, 'https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?auto=format&fit=crop&w=800&q=80', '电子产品', 'AVAILABLE', 890, NOW()),
(2003, 1003, '考研数学李永樂全套资料', '24版复习全书+660题，上面有学霸笔记，字跡工整。祝学弟学妹上岸！', 45.00, 1, 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&w=800&q=80', '书籍', 'AVAILABLE', 330, NOW()),
(2004, 1003, '宿舍用小电锅 (不跳闸)', '功率 600W，煮面、小火锅必备。毕业带不走了，送汤勺和碗。', 35.00, 1, 'https://images.unsplash.com/photo-1584269600519-112d071b35e6?auto=format&fit=crop&w=800&q=80', '生活用品', 'AVAILABLE', 210, NOW()),
(2005, 1002, 'Nike Air Force 1 空军一号', '尺码 42，得物购入，穿过两次有点挤脚。支持鉴定，假一赔三。', 400.00, 1, 'https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?auto=format&fit=crop&w=800&q=80', '服飾', 'AVAILABLE', 560, NOW()),
(2006, 1003, '捷安特山地车', '海大校区代步神器，刹车灵敏，变速好用。送一把U型锁。', 350.00, 1, 'https://images.unsplash.com/photo-1576435728678-35d0160e8c97?auto=format&fit=crop&w=800&q=80', '其他', 'AVAILABLE', 99, NOW());

-- ==========================================
-- 3. 填充购物车 (让截图不为空) -> 属于 Alice
-- ==========================================
INSERT INTO `cart_item` (user_id, prod_id, qty) VALUES
(1001, 2004, 1),
(1001, 2005, 1);

-- ==========================================
-- 4. 模拟历史订单 (用于个人中心截图) -> Alice 买过
-- ==========================================
-- 订单1：已完成
INSERT INTO `order_entity` (id, buyer_id, seller_id, status, total_amount, address, created_at) VALUES
(3001, 1001, 1002, 'COMPLETED', 1200.00, '海大主校区海宁B栋301', 1701000000000);

INSERT INTO `order_item` (id, order_id, prod_id, prod_name, prod_image, price, quantity) VALUES
(4001, 3001, 2002, 'Sony WH-1000XM4 降噪耳机', 'https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?auto=format&fit=crop&w=800&q=80', 1200.00, 1);

-- 订单2：待发货
INSERT INTO `order_entity` (id, buyer_id, seller_id, status, total_amount, address, created_at) VALUES
(3002, 1001, 1003, 'PAID', 45.00, '海大主校区海宁B栋301', 1701230000000);

INSERT INTO `order_item` (id, order_id, prod_id, prod_name, prod_image, price, quantity) VALUES
(4002, 3002, 2003, '考研数学李永樂全套资料', 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&w=800&q=80', 45.00, 1);

-- ==========================================
-- 5. 聊天记录 (Alice 与 Bob)
-- ==========================================
INSERT INTO `chat_message` (from_user_id, to_user_id, message, created_at) VALUES
(1002, 1001, '你好，请问你发布的雅诗兰黛还在吗？', 1701234567000),
(1001, 1002, '在的，专柜正品，未拆封。', 1701234577000),
(1002, 1001, '我是学生，预算有限，450 可以吗？', 1701234587000),
(1001, 1002, '可以，海大校内面交吧。', 1701234597000);

-- ==========================================
-- 6. 商品评论 (详情页互动)
-- ==========================================
INSERT INTO `comment` (user_id, prod_id, content, rating, created_at) VALUES
(1002, 2001, '楼主，电池健康度多少？有维修记录吗？', 5, 1701234567000),
(1003, 2001, '排队，如果楼上不要踢我。', 5, 1701240000000);
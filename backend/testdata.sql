USE trader;

-- ==========================================
-- 1. 初始化用戶數據 (密碼統一為: password123)
-- ==========================================
-- 先清理舊數據(可選)
DELETE FROM `user` WHERE id >= 1000;

INSERT INTO `user` (id, username, password, email, phone) VALUES
(1001, 'alice', '$2a$10$7QeYh1bV1J8KZq8Zr1aWJeYkqg3h2VZr3fY8Qz0b3Bz1b2c3d4eFG', 'alice@edu.com', '13800138001'),
(1002, 'bob',   '$2a$10$7QeYh1bV1J8KZq8Zr1aWJeYkqg3h2VZr3fY8Qz0b3Bz1b2c3d4eFG', 'bob@edu.com',   '13800138002'),
(1003, 'carol', '$2a$10$7QeYh1bV1J8KZq8Zr1aWJeYkqg3h2VZr3fY8Qz0b3Bz1b2c3d4eFG', 'carol@edu.com', '13800138003'),
(1004, 'dave',  '$2a$10$7QeYh1bV1J8KZq8Zr1aWJeYkqg3h2VZr3fY8Qz0b3Bz1b2c3d4eFG', 'dave@edu.com',  '13800138004');

-- ==========================================
-- 2. 初始化商品數據 (使用網上隨機圖片)
-- ==========================================
DELETE FROM `prod` WHERE id >= 2000;

-- 電子產品 (熱門)
INSERT INTO `prod` (id, user_id, title, descr, price, images, category, status, created_at, view_count, lat, lng) VALUES
(2001, 1001, 'iPhone 13 Pro Max 256G', '換新機了，這台閒置。電池健康 88%，無拆無修，屏幕完美。附送三個手機殼。', 4500.00, 'https://images.unsplash.com/photo-1632661674596-df8be070a5c5?auto=format&fit=crop&w=800&q=80', '電子產品', 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 2 DAY), 1205, 0, 0),
(2002, 1001, 'Sony WH-1000XM4 降噪耳機', '圖書館神器！考研結束了出給有緣人。降噪效果無敵，成色 95 新。', 1200.00, 'https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?auto=format&fit=crop&w=800&q=80', '電子產品', 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 5 DAY), 890, 0, 0),
(2003, 1002, 'iPad Air 5 紫色', '買來是用來學習的，結果變成了愛奇藝啟動器。幾乎全新，箱說全。', 3200.00, 'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?auto=format&fit=crop&w=800&q=80', '電子產品', 'AVAILABLE', NOW(), 45, 0, 0);

-- 書籍 (剛需)
INSERT INTO `prod` (id, user_id, title, descr, price, images, category, status, created_at, view_count, lat, lng) VALUES
(2004, 1003, '考研數學李永樂全套', '上面有一些筆記，字跡工整。希望能幫到學弟學妹，祝上岸！', 50.00, 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&w=800&q=80', '書籍', 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 10 DAY), 330, 0, 0),
(2005, 1002, 'Java 編程思想 (第4版)', '經典老書，雖然有點厚但是講得很透徹。轉行不干程序員了，出。', 45.00, 'https://images.unsplash.com/photo-1532012197267-da84d127e765?auto=format&fit=crop&w=800&q=80', '書籍', 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 1 DAY), 12, 0, 0),
(2006, 1004, '三體全集 (劉慈欣)', '硬科幻神作，看了一遍，震撼。', 30.00, 'https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=800&q=80', '書籍', 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 20 DAY), 56, 0, 0);

-- 生活用品
INSERT INTO `prod` (id, user_id, title, descr, price, images, category, status, created_at, view_count, lat, lng) VALUES
(2007, 1003, '宿舍用小電鍋', '功率小，不會跳閘！煮麵、小火鍋必備。畢業帶不走了。', 35.00, 'https://images.unsplash.com/photo-1584269600519-112d071b35e6?auto=format&fit=crop&w=800&q=80', '生活用品', 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 3 DAY), 210, 0, 0),
(2008, 1001, '宜家檯燈', '暖光護眼，學習用很舒服。', 20.00, 'https://images.unsplash.com/photo-1534073828943-f801091a7d58?auto=format&fit=crop&w=800&q=80', '生活用品', 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 4 DAY), 88, 0, 0),
(2009, 1004, '簡易衣櫃', '組裝好的，最好自提。', 40.00, 'https://images.unsplash.com/photo-1595515106969-1ce29566ff1c?auto=format&fit=crop&w=800&q=80', '生活用品', 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 7 DAY), 102, 0, 0);

-- 服飾與美妝
INSERT INTO `prod` (id, user_id, title, descr, price, images, category, status, created_at, view_count, lat, lng) VALUES
(2010, 1003, 'Nike Air Force 1 空軍一號', '尺碼 42，穿過兩次，有點擠腳所以出了。保真，得物購入。', 400.00, 'https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?auto=format&fit=crop&w=800&q=80', '服飾', 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 1 DAY), 560, 0, 0),
(2011, 1001, '未拆封雅詩蘭黛小棕瓶', '專櫃購入，買多了用不完。', 500.00, 'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=800&q=80', '美妝', 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 6 DAY), 340, 0, 0);

-- 交通與其他
INSERT INTO `prod` (id, user_id, title, descr, price, images, category, status, created_at, view_count, lat, lng) VALUES
(2012, 1002, '捷安特山地車', '校園代步神器，剎車靈敏，變速好用。送一把U型鎖。', 350.00, 'https://images.unsplash.com/photo-1576435728678-35d0160e8c97?auto=format&fit=crop&w=800&q=80', '其他', 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 15 DAY), 999, 0, 0),
(2013, 1004, '尤尼克斯羽毛球拍', '入門級，線剛拉的 24 磅。', 80.00, 'https://images.unsplash.com/photo-1626224583764-84786c71973e?auto=format&fit=crop&w=800&q=80', '其他', 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 8 DAY), 150, 0, 0);

-- ==========================================
-- 3. 初始化聊天記錄 (Alice 和 Bob)
-- ==========================================
INSERT INTO `chat_message` (from_user_id, to_user_id, message, created_at) VALUES
(1002, 1001, '你好，請問你的 iPhone 13 還在嗎？', 1701234567000),
(1001, 1002, '在的，目前還沒出。', 1701234577000),
(1002, 1001, '價格可以小刀嗎？學生黨預算有限。', 1701234587000),
(1001, 1002, '最低 4400 吧，送你三個殼，很划算了。', 1701234597000),
(1002, 1001, '好的，我考慮一下，晚點聯繫你。', 1701234607000);

-- ==========================================
-- 4. 初始化通知 (Notification)
-- ==========================================
INSERT INTO `notification` (user_id, title, body, `read`, created_at) VALUES
(1001, '歡迎註冊', '歡迎加入 Trader 校園二手交易平台！', 1, 1701000000000),
(1001, '商品審核通過', '您發布的「iPhone 13 Pro Max」已通過審核並上架。', 0, 1701230000000),
(1002, '降價提醒', '您收藏的「考研數學」降價了！', 0, 1701240000000);
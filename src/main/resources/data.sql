
-- EXEC sp_MSforeachtable "ALTER TABLE ? NOCHECK CONSTRAINT ALL";

-- -- 清空所有表的資料 (順序不重要了，因為約束已關閉)
-- -- 注意：這裡要列出你有參考關係的所有表
-- TRUNCATE TABLE [dbo].[order_item];
-- TRUNCATE TABLE [dbo].[orders];
-- DELETE FROM [dbo].[books]; -- 如果有 Identity 建議用 DELETE 或確保 TRUNCATE 重置 ID

-- -- 重新啟用所有表的外鍵約束
-- EXEC sp_MSforeachtable "ALTER TABLE ? WITH CHECK CHECK CONSTRAINT ALL";

-- =============================================
-- 1. 書籍資料 (books) - 以 ISBN 13碼 為準
-- =============================================
--SET IDENTITY_INSERT [dbo].[books] ON;

INSERT INTO books 
(book_id, book_name, author, translator, press, price, isbn, stock, short_desc, created_at, on_shelf, version)
VALUES 
(1, '原子習慣', 'James Clear', '蔡世偉', '悅知文化', 330.00, '9789865101039', 50, '細微改變帶來巨大成就的實證法則', CURRENT_TIMESTAMP, 1, 1),
(2, '資料庫設計與開發實務', '陳會安', NULL, '旗標', 650.00, '9789863127458', 30, '系統架構設計的實戰手冊', CURRENT_TIMESTAMP, 1, 1),
(3, 'Clean Code：無瑕的程式碼', 'Robert C. Martin', '戴廷州', '博碩', 520.00, '9789862017050', 100, '代碼整潔之道', CURRENT_TIMESTAMP, 1, 1),
(4, 'Python 程式設計的樂趣', 'Cory Althoff', '許武切', '博碩', 450.00, '9789865022211', 25, '自學 Python 的第一本書', CURRENT_TIMESTAMP, 1, 1),
(5, 'Python 資料科學與機器學習實務', '施威銘研究室', NULL, '旗標', 580.00, '9789863124921', 80, '使用 Pandas, NumPy, Matplotlib', CURRENT_TIMESTAMP, 1, 1),
(6, '演算法圖解', 'Aditya Bhargava', '張子庭', '博碩', 450.00, '9789864341851', 120, '用插圖輕鬆學會演算法', CURRENT_TIMESTAMP, 1, 1),
(7, '領域驅動設計 (DDD) 指南', 'Vaughn Vernon', '張三', '博碩', 880.00, '9789864343169', 15, '軟體核心複雜性的應對之道', CURRENT_TIMESTAMP, 1, 1),
(8, '重構：改善既有程式碼的設計 (第2版)', 'Martin Fowler', '路曉睿', '電子工業', 680.00, '9787121245812', 40, '提升代碼質量的必讀之作', CURRENT_TIMESTAMP, 1, 1),
(9, '設計模式：可複用物件導向軟體之基礎', 'Erich Gamma等', '葉秉哲', '培生', 800.00, '9789861543321', 10, '軟體工程界的聖經', CURRENT_TIMESTAMP, 1, 1),
(10, 'ASP.NET Core 8 網頁開發實戰', '周家安', NULL, '深智數位', 880.00, '9786263337213', 60, 'ASP.NET Core 全端開發立即上手', CURRENT_TIMESTAMP, 1, 1),
(11, 'Kubernetes 雲端原生實戰指南', 'Brendan Burns', '王小明', '歐萊禮', 700.00, '9789865021573', 35, '掌握雲端時代的容器佈署', CURRENT_TIMESTAMP, 1, 1),
(12, 'Microsoft SQL Server 2005 T-SQL 程式設計核心基礎', 'Itzik Ben-Gan', NULL, '技術', 690.00, '9789866826123', 45, '讓你的 SQL 跑得比誰都快', CURRENT_TIMESTAMP, 1, 1),

(14, '底層邏輯：看清這個世界的底牌', '劉潤', NULL, '時報出版', 450.00, '9789571399431', 50, '看清事物本質', CURRENT_TIMESTAMP, 1, 1),
(15, '底層邏輯 2: 帶你升級思考, 挖掘數字裡蘊含的商業寶藏', '劉潤', NULL, '時報出版', 480.00, '9786263531475', 50, '商業進化的軸線與數學邏輯', CURRENT_TIMESTAMP, 1, 1),
(16, '灰階思考', '謝孟恭', NULL, '天下文化', 400.00, '9789865251147', 100, '股癌製作人帶你跳脫黑白思考陷阱', CURRENT_TIMESTAMP, 1, 1),
(17, '這個世界運作的真相', 'Vaclav Smil', '周寶蓮', '天下雜誌', 550.00, '9786263063525', 30, '科學解讀能源、糧食與全球化', CURRENT_TIMESTAMP, 1, 1),
(18, '邏輯學入門', '尼古拉斯．史密斯', '杜世忠', '商務印書館', 520.00, '9787100156943', 20, '系統性學習正確推理的基礎', CURRENT_TIMESTAMP, 1, 1),
(19, '多巴胺國度', 'Anna Lembke', '胡琦君', '麥田出版', 420.00, '9786263102170', 60, '在成癮年代找到平衡', CURRENT_TIMESTAMP, 1, 1),
(20, '深入淺出 Java 程式設計 (第三版)', 'Kathy Sierra', '黃銘信', '歐萊禮', 980.00, '9786263242272', 40, '最有趣的 Java 入門書', CURRENT_TIMESTAMP, 1, 1),
(21, '一日一頁醫學知識', 'David S. Kidder', '王惟芬', '墨刻出版股份有限公司', 450.00, '9789862896730', 25, '每天5分鐘建立醫學架構', CURRENT_TIMESTAMP, 1, 1);

--SET IDENTITY_INSERT [dbo].[books] OFF;

-- =============================================
-- 2. 書籍圖片 (book_images) - URL 取名為「書名.jpg」
-- =============================================
--SET IDENTITY_INSERT  [book_images] ON;

INSERT INTO  book_images (image_id, book_id, image_url)
VALUES 
(1, 1, '原子習慣.jpg'),
(2, 2, '資料庫設計與開發實務.jpg'),
(3, 3, 'Clean Code：無瑕的程式碼.jpg'),
(4, 4, 'Python 程式設計的樂趣.jpg'),
(5, 5, 'Python 資料科學與機器學習實務.jpg'),
(6, 6, '演算法圖解.jpg'),
(7, 7, '領域驅動設計 (DDD) 指南.jpg'),
(8, 8, '重構：改善既有程式碼的設計 (第2版).jpg'),
(9, 9, '設計模式：可複用物件導向軟體之基礎.jpg'),
(10, 10, 'ASP.NET Core 8 網頁開發實戰.jpg'),
(11, 11, 'Kubernetes 雲端原生實戰指南.jpg'),
(12, 12,'Microsoft SQL Server 2005 T-SQL 程式設計核心基礎.jpg'),

(14, 14, '底層邏輯：看清這個世界的底牌.jpg'),
(15, 15, '底層邏輯 2.jpg'),
(16, 16, '灰階思考.jpg'),
(17, 17, '這個世界運作的真相.jpg'),
(18, 18, '邏輯學入門.jpg'),
(19, 19, '多巴胺國度.jpg'),
(20, 20, '深入淺出 Java 程式設計 (第三版).jpg'),
(21, 21, '一日一頁醫學知識.jpg');

--SET IDENTITY_INSERT [dbo].[book_images] OFF;



--分類資料
--SET IDENTITY_INSERT [dbo].[genres] ON;


INSERT INTO  genres (genre_id, genre_name)
VALUES 
(1, '商業思維'),
(2, '心理勵志'),
(3, '社會科學'),
(4, '醫療保健'),
(5, '電腦科學'),
(6, '文學小說'),      -- 包含翻譯文學、經典名著
(7, '藝術設計'),      -- 包含攝影、建築、平面設計
(8, '自然科普'),      -- 包含天文、生物、環境科學
(9, '親子教育'),      -- 包含童書、青少年教養
(10, '生活風格'),     -- 包含旅遊、美食、家事管理
(11, '語言學習'),     -- 包含多益、日檢、英文檢定
(12, '考試用書'),     -- 包含公職、證照考取
(13, '財務管理'),     -- 包含個人理財、投資分析（與商業思維分開）
(14, '歷史哲學'),     -- 包含世界史、中西方哲學
(15, '軟體架構');

--SET IDENTITY_INSERT [dbo].[genres] OFF;




--書籍類型
-- 技術類 (電腦科學/軟體架構)
INSERT INTO book_genre_map (book_id, genre_id)
VALUES 
(1, 5), (1, 15), (2, 5), (3, 5), (3, 15), (4, 5), (4, 15), (5, 5), (6, 5), 
(7, 5), (7, 15), (8, 5), (9, 5), (9, 15), (10, 5), (11, 5), (11, 15), (12, 5),
(20, 5);

-- 商業/財務類
INSERT INTO book_genre_map (book_id, genre_id)
VALUES 
(14, 1), (15, 1), (16, 13), (17, 1), (17, 3);

-- 心理/科普/醫療類
INSERT INTO book_genre_map (book_id, genre_id)
VALUES 
(16, 2), (18, 14), (19, 2), (19, 4), (21, 4), (21, 10);


--會員資料
--SET IDENTITY_INSERT [dbo].[users] ON;
--
--INSERT INTO [dbo].[users] 
--([user_id], [user_name], [user_pwd], [email], [gender], [phone_num], [address], [points], [user_type], [status], [created_at], [created_by])
--VALUES 
--(1, N'林木森', '$2a$10$GRLdNijSQMUvl/au9ShLquqnAcnGZIdscu0M5w1uC/Lg/5hA1Kdqm', 'TreeorTree@bookstore.com', 'M', '0987654321', NULL, 0, 0, 1, CURRENT_TIMESTAMP, 'System'),
--(2, N'林木楓', '$2a$10$GRLdNijSQMUvl/au9ShLquqnAcnGZIdscu0M5w1uC/Lg/5hA1Kdqm', 'Maple@bookstore.com', 'M', '0912345678', N'桃園市中壢區中華路999號', 0, 1, 1, CURRENT_TIMESTAMP, 'System'),
--(3, N'李鐵柱', '$2a$10$GRLdNijSQMUvl/au9ShLquqnAcnGZIdscu0M5w1uC/Lg/5hA1Kdqm', 'user1@yahoo.com', 'M', '0987454135', N'桃園市中壢區新生路991號', 450, 2, 1, CURRENT_TIMESTAMP, 'System'),
--(4, N'王翠花', '$2a$10$GRLdNijSQMUvl/au9ShLquqnAcnGZIdscu0M5w1uC/Lg/5hA1Kdqm', 'user2@yahoo.com', 'F', '0974745241', N'桃園市中壢區新生路992號', 1200, 2, 1, CURRENT_TIMESTAMP, 'System'),
--(5, N'林志玲', '$2a$10$GRLdNijSQMUvl/au9ShLquqnAcnGZIdscu0M5w1uC/Lg/5hA1Kdqm', 'user3@yahoo.com', 'F', '0954123547', N'桃園市中壢區新生路993號', 880, 2, 1, CURRENT_TIMESTAMP, 'System'),
--(6, N'張大寶', '$2a$10$GRLdNijSQMUvl/au9ShLquqnAcnGZIdscu0M5w1uC/Lg/5hA1Kdqm', 'user4@yahoo.com', 'M', '0985412534', N'桃園市中壢區新生路994號', 150, 2, 1, CURRENT_TIMESTAMP, 'System'),
--(7, N'陳汁瀚', '$2a$10$GRLdNijSQMUvl/au9ShLquqnAcnGZIdscu0M5w1uC/Lg/5hA1Kdqm', 'user5@yahoo.com', 'M', '0958745666', N'桃園市中壢區新生路995號', 1950, 2, 1, CURRENT_TIMESTAMP, 'System'),
--(8, N'周餅倫', '$2a$10$GRLdNijSQMUvl/au9ShLquqnAcnGZIdscu0M5w1uC/Lg/5hA1Kdqm', 'user6@yahoo.com', 'M', '0965845333', N'桃園市中壢區新生路996號', 300, 2, 1, CURRENT_TIMESTAMP, 'System'),
--(9, N'王小陸', '$2a$10$GRLdNijSQMUvl/au9ShLquqnAcnGZIdscu0M5w1uC/Lg/5hA1Kdqm', 'user7@yahoo.com', 'M', '0954254855', N'桃園市中壢區新生路997號', 720, 2, 1, CURRENT_TIMESTAMP, 'System'),
--(10, N'范承仁', '$2a$10$GRLdNijSQMUvl/au9ShLquqnAcnGZIdscu0M5w1uC/Lg/5hA1Kdqm', 'user8@yahoo.com', 'M', '0985412578', N'桃園市中壢區新生路998號', 1100, 2, 1, CURRENT_TIMESTAMP, 'System'),
--(11, N'李建輝', '$2a$10$GRLdNijSQMUvl/au9ShLquqnAcnGZIdscu0M5w1uC/Lg/5hA1Kdqm', 'user9@yahoo.com', 'M', '0985412544', N'桃園市中壢區新生路999號', 560, 2, 1, CURRENT_TIMESTAMP, 'System');
--
--SET IDENTITY_INSERT [dbo].[users] OFF;

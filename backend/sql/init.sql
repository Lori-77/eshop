-- 网上购物商城 数据库初始化脚本
-- Database: eshop

CREATE DATABASE IF NOT EXISTS eshop DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE eshop;

-- 用户表（统一存储用户、商家、管理员）
DROP TABLE IF EXISTS user;
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名/手机号',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt)',
    nickname VARCHAR(50) COMMENT '昵称',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像URL',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: USER/MERCHANT/ADMIN',
    store_name VARCHAR(100) COMMENT '店铺名称(商家)',
    store_desc TEXT COMMENT '店铺描述(商家)',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1正常 0冻结',
    login_error_count INT DEFAULT 0 COMMENT '登录错误次数',
    lock_until DATETIME COMMENT '锁定截止时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除'
) COMMENT '用户表';

-- 商品分类表
DROP TABLE IF EXISTS category;
CREATE TABLE category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    icon VARCHAR(255) COMMENT '图标',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) COMMENT '商品分类表';

-- 商品表
DROP TABLE IF EXISTS product;
CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT '商品名称',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    merchant_id BIGINT NOT NULL COMMENT '商家ID',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存',
    description TEXT COMMENT '商品详情(HTML)',
    main_image VARCHAR(255) COMMENT '主图',
    images TEXT COMMENT '商品图片(JSON数组)',
    specs TEXT COMMENT '规格参数(JSON)',
    status VARCHAR(20) DEFAULT 'ON_SALE' COMMENT '状态: ON_SALE在售, OFF_SHELF下架',
    sales INT DEFAULT 0 COMMENT '销量',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) COMMENT '商品表';

-- 购物车表
DROP TABLE IF EXISTS cart_item;
CREATE TABLE cart_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    selected TINYINT DEFAULT 1 COMMENT '是否选中',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '购物车表';

-- 收货地址表
DROP TABLE IF EXISTS address;
CREATE TABLE address (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    receiver_name VARCHAR(50) NOT NULL COMMENT '收货人',
    phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    province VARCHAR(50) COMMENT '省',
    city VARCHAR(50) COMMENT '市',
    district VARCHAR(50) COMMENT '区',
    detail VARCHAR(255) NOT NULL COMMENT '详细地址',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '收货地址表';

-- 订单表
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    merchant_id BIGINT NOT NULL COMMENT '商家ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_PAYMENT' COMMENT '订单状态: PENDING_PAYMENT待支付, PENDING_DELIVERY待发货, DELIVERED已发货, COMPLETED已完成, CANCELLED已取消, REFUNDING退款中, REFUNDED已退款',
    receiver_name VARCHAR(50) COMMENT '收货人',
    phone VARCHAR(20) COMMENT '电话',
    address VARCHAR(255) COMMENT '收货地址',
    remark VARCHAR(500) COMMENT '备注',
    pay_time DATETIME COMMENT '支付时间',
    delivery_time DATETIME COMMENT '发货时间',
    complete_time DATETIME COMMENT '完成时间',
    cancel_time DATETIME COMMENT '取消时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) COMMENT '订单表';

-- 订单明细表
DROP TABLE IF EXISTS order_item;
CREATE TABLE order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(200) COMMENT '商品名称',
    product_image VARCHAR(255) COMMENT '商品图片',
    price DECIMAL(10,2) NOT NULL COMMENT '单价',
    quantity INT NOT NULL COMMENT '数量',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '小计',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '订单明细表';

-- 支付记录表(模拟支付)
DROP TABLE IF EXISTS payment;
CREATE TABLE payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    pay_method VARCHAR(30) DEFAULT 'ALIPAY' COMMENT '支付方式: ALIPAY/WECHAT/BANK_CARD',
    status VARCHAR(20) DEFAULT 'SUCCESS' COMMENT '状态: SUCCESS/FAILED',
    pay_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '支付时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '支付记录表';

-- 退款申请表
DROP TABLE IF EXISTS refund;
CREATE TABLE refund (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    merchant_id BIGINT NOT NULL COMMENT '商家ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '退款金额',
    reason VARCHAR(500) COMMENT '退款原因',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING待审核, APPROVED已同意, REJECTED已拒绝',
    reject_reason VARCHAR(500) COMMENT '拒绝原因',
    result_time DATETIME COMMENT '处理时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '退款申请表';

-- 物流信息表
DROP TABLE IF EXISTS logistics;
CREATE TABLE logistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    company VARCHAR(50) NOT NULL COMMENT '物流公司',
    tracking_no VARCHAR(100) NOT NULL COMMENT '运单号',
    status VARCHAR(30) DEFAULT 'IN_TRANSIT' COMMENT '状态: IN_TRANSIT运输中, DELIVERED已签收',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '物流信息表';

-- 系统配置表
DROP TABLE IF EXISTS system_config;
CREATE TABLE system_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(255) COMMENT '描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '系统配置表';

-- 操作日志表
DROP TABLE IF EXISTS operation_log;
CREATE TABLE operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT COMMENT '操作人ID',
    username VARCHAR(50) COMMENT '操作人',
    action VARCHAR(100) NOT NULL COMMENT '操作类型',
    target VARCHAR(200) COMMENT '操作对象',
    detail TEXT COMMENT '详情',
    ip VARCHAR(50) COMMENT 'IP地址',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '操作日志表';

-- ==================== 初始数据 ====================

-- 管理员账号: admin / admin123
INSERT INTO user (username, password, nickname, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'ADMIN', 1);

-- 商家测试账号: shop1 / shop123
INSERT INTO user (username, password, nickname, role, status, store_name) VALUES
('shop1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试商家', 'MERCHANT', 1, '优质数码旗舰店');

-- 用户测试账号: user1 / user123
INSERT INTO user (username, password, nickname, phone, role, status) VALUES
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户', '13800138000', 'USER', 1);

-- 商品分类
INSERT INTO category (id, name, parent_id, sort_order) VALUES
(1, '手机数码', 0, 1),
(2, '电脑办公', 0, 2),
(3, '家用电器', 0, 3),
(4, '服装鞋帽', 0, 4),
(5, '食品生鲜', 0, 5),
(6, '手机', 1, 1),
(7, '平板电脑', 1, 2),
(8, '笔记本电脑', 2, 1),
(9, '台式机', 2, 2);

-- 示例商品
INSERT INTO product (name, category_id, merchant_id, price, stock, description, main_image, status) VALUES
('高端智能手机 Pro Max', 6, 2, 5999.00, 100, '<p>最新款高端智能手机，性能强劲，拍照出色</p>', '/uploads/product1.jpg', 'ON_SALE'),
('轻薄笔记本电脑 Air', 8, 2, 4999.00, 50, '<p>超轻薄设计，续航长达12小时</p>', '/uploads/product2.jpg', 'ON_SALE'),
('无线蓝牙耳机', 1, 2, 299.00, 200, '<p>主动降噪，音质出色，佩戴舒适</p>', '/uploads/product3.jpg', 'ON_SALE'),
('智能手表运动版', 1, 2, 1299.00, 80, '<p>健康监测，运动记录，超长续航</p>', '/uploads/product4.jpg', 'ON_SALE'),
('平板电脑 11英寸', 7, 2, 3299.00, 60, '<p>高分辨率屏幕，支持手写笔</p>', '/uploads/product5.jpg', 'ON_SALE');

-- 系统配置
INSERT INTO system_config (config_key, config_value, description) VALUES
('site_name', '网上购物商城', '站点名称'),
('order_timeout_minutes', '15', '订单支付超时时间(分钟)'),
('refund_days', '7', '收货后可申请退款天数'),
('max_login_attempts', '3', '最大登录尝试次数'),
('lock_duration_minutes', '15', '账号锁定时间(分钟)');

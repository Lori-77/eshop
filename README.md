# 网上购物商城

软件工程大作业 - 基于 Spring Boot + Vue 3 的在线购物系统

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2.x |
| 安全框架 | Spring Security + JWT |
| ORM | MyBatis-Plus 3.5.x |
| 数据库 | MySQL 8.0 |
| 前端框架 | Vue 3 + Vite |
| UI 组件库 | Element Plus |
| 状态管理 | Pinia |
| HTTP 客户端 | Axios |

## 项目结构

```
├── backend/                    # 后端 Spring Boot 项目
│   ├── pom.xml
│   ├── sql/init.sql           # 数据库初始化脚本
│   └── src/main/java/com/eshop/
│       ├── EshopApplication.java
│       ├── config/            # 安全、JWT、CORS 等配置
│       ├── controller/        # REST API 控制器
│       ├── service/           # 业务逻辑层
│       │   └── impl/          # 实现类
│       ├── mapper/            # MyBatis Mapper 接口
│       ├── entity/            # 数据库实体类
│       ├── dto/               # 请求 DTO
│       ├── vo/                # 响应 VO
│       └── common/            # 通用类（Result, 异常处理等）
│
└── frontend/                   # 前端 Vue 3 项目
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── main.js
        ├── App.vue
        ├── router/            # 路由配置
        ├── stores/            # Pinia 状态管理
        ├── api/               # API 接口封装
        ├── utils/             # 工具函数（Axios 封装）
        └── views/
            ├── user/          # 用户端页面
            ├── merchant/      # 商家端页面
            └── admin/         # 管理员端页面
```

## 功能模块

### 用户端（UC-01 ~ UC-09）
- 注册账号 / 登录系统
- 浏览商品（分类、搜索、排序）
- 加入购物车
- 提交订单 / 模拟支付
- 查看订单 / 确认收货
- 申请退款

### 商家端（UC-10 ~ UC-12）
- 登录商家后台
- 商品管理（新增、编辑、上下架、删除）
- 订单管理（发货、查看订单）
- 退款处理（同意/拒绝退款）

### 管理员端（UC-13 ~ UC-17）
- 登录管理后台
- 用户管理（冻结/解冻）
- 商家管理（冻结/解冻）
- 数据统计（仪表盘、图表）
- 系统配置（分类管理、参数配置）

## 快速开始

### 1. 数据库初始化

```bash
# 创建数据库并导入初始数据
mysql -u root -p < backend/sql/init.sql
```

数据库配置在 `backend/src/main/resources/application.yml` 中，默认：
- 数据库名: `eshop`
- 用户名: `root`
- 密码: `root`

### 2. 启动后端（用 IntelliJ IDEA）

1. 用 IntelliJ IDEA 打开 `backend/` 目录
2. 等待 Maven 依赖下载完成
3. 运行 `com.eshop.EshopApplication` 主类
4. 后端启动在 http://localhost:8080

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动在 http://localhost:3000

### 4. 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 用户 | user1 | user123 |
| 商家 | shop1 | shop123 |
| 管理员 | admin | admin123 |

## API 接口概览

所有接口前缀: `/api/`

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 认证 | POST | /auth/login | 登录 |
| 认证 | POST | /auth/register | 注册 |
| 认证 | GET | /auth/me | 获取当前用户 |
| 商品 | GET | /products | 商品列表/搜索 |
| 商品 | GET | /products/{id} | 商品详情 |
| 商品 | POST | /products | 新增商品(商家) |
| 购物车 | GET/POST | /cart | 查看/添加到购物车 |
| 订单 | POST | /orders | 提交订单 |
| 订单 | GET | /orders | 用户订单列表 |
| 支付 | POST | /payment/pay | 模拟支付 |
| 退款 | POST | /refunds | 申请退款 |
| 商家 | POST | /merchant/ship | 发货 |
| 管理 | GET | /admin/stats | 数据统计 |

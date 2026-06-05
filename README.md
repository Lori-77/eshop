# 网上购物商城

软件工程大作业 — Spring Boot + Vue 3 全栈在线购物系统

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.2.5 |
| 安全框架 | Spring Security + JWT | 0.12.5 |
| ORM | MyBatis-Plus | 3.5.6 |
| 数据库 | MySQL | 8.0 |
| Java | JDK | 17 |
| 前端框架 | Vue 3 + Vite | 5.x |
| UI 组件库 | Element Plus | 2.7 |
| 状态管理 | Pinia | 2.1 |

## 功能模块

### 用户端
- 注册 / 登录 / 个人中心
- 商品浏览（分类、搜索、排序）
- 购物车（加入、修改数量、删除）
- 下单结算 / 模拟支付
- 订单管理（查看、取消、确认收货）
- 退款申请 / 收货地址管理

### 商家端
- 仪表盘（数据概览）
- 商品管理（新增、编辑、上下架、删除）
- 订单管理（发货、物流跟踪）
- 退款处理（同意 / 拒绝）

### 管理员端
- 仪表盘（平台数据统计 + 近7日图表）
- 用户管理（查询、冻结 / 解冻）
- 商家管理（查询、冻结 / 解冻）
- 分类管理（两级分类树）
- 系统配置

## 项目结构

```
├── backend/                     # Spring Boot 后端
│   ├── pom.xml
│   ├── sql/init.sql             # 建库 + 初始化数据
│   └── src/main/
│       ├── resources/
│       │   ├── application.yml
│       │   └── mapper/          # MyBatis XML
│       └── java/com/eshop/
│           ├── config/          # Security, JWT, CORS
│           ├── controller/      # 12 个 REST 控制器
│           ├── service/         # 11 个业务接口 + 实现
│           ├── mapper/          # 12 个 Mapper 接口
│           ├── entity/          # 12 个实体类
│           ├── dto/             # 12 个请求 DTO
│           ├── vo/              # 7 个响应 VO
│           └── common/          # 统一返回、异常处理
│
├── frontend/                    # Vue 3 前端
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── api/                 # API 封装（Axios）
│       ├── stores/              # Pinia 状态（auth, cart）
│       ├── router/              # 路由 + 权限守卫
│       ├── utils/               # Axios 拦截器
│       └── views/
│           ├── user/            # 11 个用户端页面
│           ├── merchant/        # 5 个商家端页面
│           └── admin/           # 6 个管理员页面
│
└── README.md
```

## 快速开始

### 1. 数据库

```bash
mysql -u root -p123456 < backend/sql/init.sql
```

默认配置（`application.yml`）：用户名 `root`，密码 `123456`，数据库名 `eshop`

### 2. 启动后端

IntelliJ IDEA → File → Open → 选择 `backend/` → Maven 装完依赖 → 运行 `EshopApplication`

后端：http://localhost:8080

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端：http://localhost:3000（自动代理 API 到 8080）

### 4. 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 用户 | user1 | user123 |
| 商家 | shop1 | shop123 |
| 管理员 | admin | admin123 |

## 添加商品图片

把图片放到 `backend/uploads/` 目录，数据库里改 `product` 表的 `main_image` 字段：

```sql
UPDATE product SET main_image = '/uploads/图片名.jpg' WHERE id = 1;
```

前端会自动通过 `/uploads/` 路径加载图片。

## API 概览

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 认证 | POST | /api/auth/login | 登录 |
| 认证 | POST | /api/auth/register | 注册 |
| 商品 | GET | /api/products | 商品列表（支持分类、搜索、排序） |
| 商品 | GET | /api/products/{id} | 商品详情 |
| 分类 | GET | /api/categories | 分类树 |
| 购物车 | GET POST DELETE | /api/cart | 购物车 CRUD |
| 订单 | POST GET | /api/orders | 下单、订单列表 |
| 支付 | POST | /api/payment/pay | 模拟支付 |
| 退款 | POST | /api/refunds | 申请退款 |
| 商家 | POST | /api/merchant/ship | 发货 |
| 管理 | GET | /api/admin/stats | 数据统计 |

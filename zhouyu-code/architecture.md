# 登录功能架构设计方案

## 技术选型
- 后端：Java + Spring Boot
- 前端：Vue.js 3 + Vue Router + Axios
- 数据库：MySQL
- 构建工具：Maven (后端), Vite (前端)

## 接口设计

### 用户登录接口
- **URL**: `/api/auth/login`
- **Method**: POST
- **Request Body**:
  ```json
  {
    "username": "string",
    "password": "string"
  }
  ```
- **Success Response**:
  ```json
  {
    "code": 200,
    "message": "登录成功",
    "data": {
      "token": "string"
    }
  }
  ```
- **Error Response**:
  ```json
  {
    "code": 401,
    "message": "用户名或密码错误",
    "data": null
  }
  ```

### 获取用户信息接口
- **URL**: `/api/user/info`
- **Method**: GET
- **Headers**: 
  - Authorization: Bearer {token}
- **Success Response**:
  ```json
  {
    "code": 200,
    "message": "获取用户信息成功",
    "data": {
      "id": "number",
      "username": "string",
      "email": "string"
    }
  }
  ```

## 数据库表设计

### users 表
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 项目结构

### 后端结构 (Spring Boot)
```
src/
├── main/
│   ├── java/com/example/demo/
│   │   ├── DemoApplication.java
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   └── UserController.java
│   │   ├── service/
│   │   │   ├── UserService.java
│   │   │   └── AuthService.java
│   │   ├── repository/
│   │   │   └── UserRepository.java
│   │   ├── model/
│   │   │   └── User.java
│   │   └── config/
│   │       └── WebConfig.java
│   └── resources/
│       ├── application.properties
│       └── data.sql (初始化测试数据)
```

### 前端结构 (Vue.js)
```
src/
├── main.js
├── App.vue
├── router/
│   └── index.js
├── views/
│   ├── Login.vue
│   └── Dashboard.vue
├── components/
│   └── HelloWorld.vue
├── services/
│   └── api.js
└── store/
    └── index.js (简单状态管理)
```

## 功能流程
1. 用户访问登录页面
2. 输入用户名和密码，点击登录按钮
3. 前端调用登录接口，验证用户凭据
4. 登录成功后，后端返回 token
5. 前端保存 token (localStorage)，并跳转到仪表盘页面
6. 仪表盘页面加载时，调用获取用户信息接口显示用户数据

## 注意事项
- 本方案不考虑安全性，密码明文存储，无加密传输
- Token 使用简单的 JWT 或随机字符串
- 无权限控制和角色管理
- 无密码重置、注册等功能
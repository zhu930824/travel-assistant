# 智能旅游规划助手

基于 Spring AI Alibaba 的多Agent监督者模式实现的智能旅游规划系统。

## 项目结构

```
travel-assistant/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/
│   │   └── cn/sdh/travel/
│   │       ├── TravelAssistantApplication.java
│   │       ├── config/         # 配置类
│   │       ├── agent/          # Agent模块
│   │       │   ├── supervisor/ # 监督者Agent
│   │       │   ├── expert/     # 专家Agent
│   │       │   └── tool/       # 自定义AgentTool
│   │       ├── context/        # SSE连接管理
│   │       ├── controller/     # REST接口
│   │       ├── dto/            # 数据传输对象
│   │       └── common/         # 通用模块
│   └── pom.xml
│
├── fronted/                    # Vue 3 前端
│   ├── src/
│   │   ├── main.js
│   │   ├── App.vue
│   │   ├── components/         # 组件
│   │   ├── composables/        # Hooks
│   │   └── assets/styles/      # 样式
│   ├── package.json
│   └── vite.config.js
│
└── doc/                        # 文档
```

## 技术栈

### 后端
- Spring Boot 3.5.2
- Spring AI Alibaba 1.1.2.0
- 阿里云百炼大模型 (qwen-plus)

### 前端
- Vue 3.4
- Vite 5
- Tailwind CSS 3.4
- Glassmorphism UI 设计

## 快速开始

### 1. 环境准备

- JDK 21+
- Node.js 18+
- Maven 3.8+
- 阿里云百炼 API Key

### 2. 配置后端

编辑 `backend/src/main/resources/application.yml`:

```yaml
spring:
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}  # 设置环境变量或直接填写
```

或设置环境变量:
```bash
export DASHSCOPE_API_KEY=your-api-key-here
```

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端服务运行在 http://localhost:8080

### 4. 启动前端

```bash
cd fronted
npm install
npm run dev
```

前端服务运行在 http://localhost:5173

### 5. 访问应用

打开浏览器访问 http://localhost:5173

## 核心功能

### 多Agent协作架构

```
用户请求
    ↓
监督者Agent (理解需求，制定计划)
    ↓
调用专家Agent Tool
    ├─→ 景点推荐Agent
    ├─→ 住宿规划Agent
    └─→ 交通规划Agent
    ↓
监督者Agent (整合结果，输出方案)
```

### 实时流式输出

通过自定义 AgentTool 实现：
- 子Agent流式执行
- SSE实时推送到前端
- 前端实时展示各Agent输出

### Glassmorphism UI

现代 SaaS 风格界面：
- 半透明毛玻璃卡片
- 动态渐变背景
- 流畅的动画效果

## API 接口

### POST /api/travel/plan/stream

流式旅游规划接口（SSE）

**请求体:**
```json
{
  "destination": "北京",
  "days": 3,
  "budget": "5000-8000元",
  "preferences": "历史文化、美食"
}
```

**响应 (SSE事件):**
```
event: session_start
data: {"sessionId":"xxx"}

event: message
data: {"stage":"attraction","content":"推荐景点...","contentType":"delta","hasContent":true}

event: complete
data: {"sessionId":"xxx","status":"completed"}
```

## 开发说明

### 添加新的专家Agent

1. 在 `agent/expert/` 创建新Agent类
2. 在 `TravelSupervisorPlanAgent` 中注入并注册
3. 更新前端组件显示新Agent

### 自定义系统提示词

修改各Agent类中的 `SYSTEM_PROMPT` 常量。

## License

MIT

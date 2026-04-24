# Agent 工作模式详解

本文档详细介绍 AI Agent 的核心工作模式，包括 ReAct、CoT 以及多 Agent 协作模式。

---

## 目录

1. [ReAct 模式](#1-react-模式)
2. [CoT 思维链](#2-cot-思维链)
3. [Sequential Chain 顺序链模式](#3-sequential-chain-顺序链模式)
4. [Parallel Execution 并行执行模式](#4-parallel-execution-并行执行模式)
5. [Router Dispatch 路由分发模式](#5-router-dispatch-路由分发模式)
6. [Workflow 工作流模式](#6-workflow-工作流模式)
7. [Hierarchical Supervisor 层级监督模式](#7-hierarchical-supervisor-层级监督模式)
8. [Debate Negotiation 辩论协商模式](#8-debate-negotiation-辩论协商模式)
9. [AutoGen 人机协作模式](#9-autogen-人机协作模式)
10. [模式对比总结](#10-模式对比总结)

---

## 1. ReAct 模式

### 1.1 概念

**ReAct = Reasoning（推理）+ Acting（行动）**

让 Agent 能够：
- **思考（Thought）**：分析问题、规划下一步
- **行动（Action）**：调用工具获取信息或执行操作
- **观察（Observation）**：根据结果继续推理

### 1.2 工作原理

```
┌─────────────────────────────────────────────────────────────┐
│                      ReAct 循环                              │
│                                                              │
│    用户问题                                                   │
│        ↓                                                     │
│   ┌─────────┐                                                │
│   │ Thought │ ← 思考：我需要什么信息？下一步做什么？            │
│   └────┬────┘                                                │
│        ↓                                                     │
│   ┌─────────┐                                                │
│   │ Action  │ ← 行动：调用工具 / 回答问题                      │
│   └────┬────┘                                                │
│        ↓                                                     │
│   ┌──────────┐                                               │
│   │Observation│ ← 观察：工具返回结果                           │
│   └────┬─────┘                                               │
│        │                                                     │
│        ↓──────────→ 继续思考（如果未完成）                     │
│        ↓                                                     │
│     最终答案                                                   │
└─────────────────────────────────────────────────────────────┘
```

### 1.3 执行示例

**问题：北京今天天气怎么样？适合去哪些景点？**

```
Thought 1: 用户想知道北京天气，我需要调用天气查询工具
Action 1: call_weather("北京")
Observation 1: 北京今天晴天，气温 18-25°C

Thought 2: 天气晴好，适合户外活动，我来推荐景点
Action 2: call_attractions("北京", "outdoor")
Observation 2: 故宫、颐和园、天坛公园...

Thought 3: 我已经有了足够信息，可以回答了
Action 3: Final Answer: 北京今天天气晴好，气温18-25°C...
```

### 1.4 代码实现

```java
// 创建 ReAct Agent
ReactAgent analyzer = ReactAgent.builder()
    .name("need_analyzer")
    .model(chatModel)
    .systemPrompt("你是旅游需求分析专家，分析用户需求并提取关键信息。")
    .build();

// 调用 Agent
String prompt = String.format("""
    分析以下旅游需求：
    目的地：%s
    天数：%d天
    预算：%s
    偏好：%s
    """, destination, days, budget, preferences);

String analysis = analyzer.call(prompt).getText();
```

### 1.5 特点分析

| 优点 | 缺点 |
|------|------|
| 自主决策能力强 | 可能陷入循环 |
| 可调用外部工具 | Token 消耗较多 |
| 推理过程可解释 | 需要精心设计提示词 |

### 1.6 适用场景

- 需要从外部获取信息的任务
- 需要执行操作的任务
- 不确定需要多少轮交互的复杂问题

---

## 2. CoT 思维链

### 2.1 概念

**CoT（Chain of Thought）** 让模型**显式展示推理过程**，通过"一步一步思考"来提高复杂问题的准确率。

### 2.2 工作原理

```
┌─────────────────────────────────────────┐
│           传统直接回答                     │
│                                          │
│  问题 ──────────────────→ 答案           │
│        (可能出错，尤其是复杂问题)          │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│           CoT 思维链                      │
│                                          │
│  问题                                    │
│    ↓                                     │
│  步骤1: 分析问题组成部分                   │
│    ↓                                     │
│  步骤2: 处理第一部分                       │
│    ↓                                     │
│  步骤3: 处理第二部分                       │
│    ↓                                     │
│  ...                                     │
│    ↓                                     │
│  最终答案                                 │
└─────────────────────────────────────────┘
```

### 2.3 对比示例

**问题：小明有5个苹果，吃了2个，妈妈又给了他3个，他送给小红4个，问小明现在有几个苹果？**

```
【没有 CoT】
答案：2个  ← 可能直接猜错

【使用 CoT】
让我们一步一步思考：
1. 小明原有苹果：5个
2. 吃了2个后：5 - 2 = 3个
3. 妈妈给了3个：3 + 3 = 6个
4. 送给小红4个：6 - 4 = 2个
答案：2个  ← 推理过程清晰，结果可靠
```

### 2.4 CoT 变体

| 变体 | 说明 | 提示词示例 |
|------|------|-----------|
| **Zero-shot CoT** | 零样本，直接要求 | "让我们一步一步思考" |
| **Few-shot CoT** | 少样本，给出示例 | 给出几个示例 + "按照上面的方式思考" |
| **Self-Consistency CoT** | 自一致性 | 生成多个推理路径，投票选择 |

### 2.5 代码实现

```java
// 在 Agent 系统提示词中使用 CoT
String systemPrompt = """
    你是旅游规划专家。在回答问题时，请按以下步骤思考：

    第一步：分析用户的核心需求是什么
    第二步：考虑有哪些因素需要权衡
    第三步：列出可能的选项
    第四步：评估每个选项的优缺点
    第五步：给出最终建议并说明理由

    请在回答中展示你的思考过程。
    """;
```

### 2.6 特点分析

| 优点 | 缺点 |
|------|------|
| 提高复杂推理准确率 | 输出更长，消耗更多 Token |
| 推理过程可解释 | 简单问题反而啰嗦 |
| 减少逻辑错误 | 可能产生错误的推理链 |

---

## 3. Sequential Chain 顺序链模式

### 3.1 概念

按**固定顺序依次执行**各 Agent，前一个的输出作为后一个的输入。

### 3.2 架构图

```
┌─────────────────────────────────────────────────────────────┐
│                   SequentialChainOrchestrator               │
│                                                              │
│   用户请求                                                    │
│       ↓                                                      │
│   ┌─────────────────┐                                        │
│   │ AttractionAgent │ 景点推荐                               │
│   └────────┬────────┘                                        │
│            ↓ 景点列表                                         │
│   ┌─────────────────┐                                        │
│   │   HotelAgent    │ 住宿推荐（根据景点位置选择）             │
│   └────────┬────────┘                                        │
│            ↓ 住宿安排                                         │
│   ┌─────────────────┐                                        │
│   │ TransportAgent  │ 交通规划（连接景点和住宿）               │
│   └────────┬────────┘                                        │
│            ↓ 交通方案                                         │
│   ┌─────────────────┐                                        │
│   │  整合最终方案    │                                        │
│   └─────────────────┘                                        │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### 3.3 核心代码

```java
public SequentialPlanResult planSequential(String destination, int days,
                                            String budget, String preferences) {
    log.info("【顺序链模式】开始执行");

    // Step 1: 景点推荐
    ReactAgent attractionReactAgent = attractionAgent.createAgent();
    String attractionResult = attractionReactAgent.call(
        buildAttractionPrompt(destination, days, preferences)).getText();

    // Step 2: 住宿推荐（依赖景点结果）
    ReactAgent hotelReactAgent = hotelAgent.createAgent();
    String hotelResult = hotelReactAgent.call(
        buildHotelPrompt(destination, days, budget, attractionResult)).getText();

    // Step 3: 交通规划（依赖景点和住宿结果）
    ReactAgent transportReactAgent = transportAgent.createAgent();
    String transportResult = transportReactAgent.call(
        buildTransportPrompt(destination, days, attractionResult, hotelResult)).getText();

    // Step 4: 整合最终方案
    String finalPlan = aggregateResults(attractionResult, hotelResult, transportResult);

    return new SequentialPlanResult(attractionResult, hotelResult, transportResult, finalPlan);
}
```

### 3.4 特点分析

| 优点 | 缺点 |
|------|------|
| 实现简单，易于理解 | 执行效率低（串行） |
| 数据流转清晰 | 前置步骤失败影响全局 |
| 适合有依赖关系的任务 | 无法并行优化 |

### 3.5 适用场景

- 任务之间有明确依赖关系
- 后一步需要前一步的结果作为输入
- 流程固定、顺序确定的业务

---

## 4. Parallel Execution 并行执行模式

### 4.1 概念

多个 Agent **同时独立执行**，最后由整合 Agent 合并结果。

### 4.2 架构图

```
┌─────────────────────────────────────────────────────────────┐
│                ParallelExecutionOrchestrator                │
│                                                              │
│                      用户请求                                 │
│                         ↓                                    │
│              ┌──────────┴──────────┐                        │
│              ↓                     ↓                        │
│   ┌─────────────────┐   ┌─────────────────┐                │
│   │ AttractionAgent │   │   HotelAgent    │                │
│   │   (线程 1)       │   │   (线程 2)       │                │
│   └────────┬────────┘   └────────┬────────┘                │
│            │                     │                          │
│            │  ┌─────────────────┐│                          │
│            │  │ TransportAgent  ││                          │
│            │  │   (线程 3)       ││                          │
│            │  └────────┬────────┘│                          │
│            ↓           ↓         ↓                          │
│            └───────────┴─────────┘                          │
│                        ↓                                     │
│            ┌─────────────────────┐                          │
│            │  Aggregator Agent   │                          │
│            │    整合所有结果      │                          │
│            └─────────────────────┘                          │
│                        ↓                                     │
│                   最终旅游方案                                │
└─────────────────────────────────────────────────────────────┘
```

### 4.3 核心代码

```java
public ParallelPlanResult planParallel(String destination, int days,
                                        String budget, String preferences) {
    log.info("【并行模式】开始执行并行规划");

    String basePrompt = buildBasePrompt(destination, days, budget, preferences);

    // 并行启动三个 Agent
    CompletableFuture<String> attractionFuture = CompletableFuture.supplyAsync(() -> {
        ReactAgent agent = attractionAgent.createAgent();
        return agent.call("作为景点推荐专家，" + basePrompt).getText();
    }, executorService);

    CompletableFuture<String> transportFuture = CompletableFuture.supplyAsync(() -> {
        ReactAgent agent = transportAgent.createAgent();
        return agent.call("作为交通规划专家，" + basePrompt).getText();
    }, executorService);

    CompletableFuture<String> hotelFuture = CompletableFuture.supplyAsync(() -> {
        ReactAgent agent = hotelAgent.createAgent();
        return agent.call("作为住宿推荐专家，" + basePrompt).getText();
    }, executorService);

    // 等待所有任务完成
    CompletableFuture.allOf(attractionFuture, transportFuture, hotelFuture).join();

    // 整合结果
    String finalPlan = aggregateResults(
        attractionFuture.join(), transportFuture.join(), hotelFuture.join(), destination);

    return new ParallelPlanResult(...);
}
```

### 4.4 特点分析

| 优点 | 缺点 |
|------|------|
| 执行效率高（并行） | 子任务间无法共享信息 |
| 互不干扰，独立性强 | 整合时可能出现冲突 |
| 适合无依赖的任务 | 需要额外的整合逻辑 |

### 4.5 适用场景

- 各子任务之间无依赖关系
- 需要快速获取多个维度的结果
- 结果可以并行生成后合并

---

## 5. Router Dispatch 路由分发模式

### 5.1 概念

根据请求类型**智能路由**到相应的专家 Agent 处理。

### 5.2 架构图

```
┌─────────────────────────────────────────────────────────────┐
│                 RouterDispatchOrchestrator                  │
│                                                              │
│                      用户请求                                 │
│                         ↓                                    │
│            ┌────────────────────────┐                       │
│            │     Router Agent       │                       │
│            │   分析请求类型          │                       │
│            └────────────┬───────────┘                       │
│                         ↓                                    │
│        ┌────────────────┼────────────────┐                  │
│        ↓                ↓                ↓                  │
│   ┌─────────┐     ┌──────────┐     ┌───────────┐           │
│   │ATTRACTION│    │  HOTEL   │     │ TRANSPORT │           │
│   │ 景点请求  │    │ 住宿请求  │     │ 交通请求   │           │
│   └────┬────┘     └────┬─────┘     └─────┬─────┘           │
│        ↓               ↓                 ↓                  │
│   AttractionAgent  HotelAgent      TransportAgent           │
│        │               │                 │                  │
│        └───────────────┴─────────────────┘                  │
│                        ↓                                     │
│                  ┌───────────┐                              │
│                  │COMPREHENSIVE│ ← 综合请求，调用所有专家     │
│                  └───────────┘                              │
└─────────────────────────────────────────────────────────────┘
```

### 5.3 核心代码

```java
public RoutedPlanResult planWithRoute(String destination, int days,
                                       String budget, String preferences) {
    // Step 1: 路由分析
    AgentType agentType = analyzeRequestType(destination, days, budget, preferences);
    log.info("【路由模式】路由结果: {}", agentType);

    // Step 2: 根据路由结果执行对应 Agent
    String result = executeAgent(agentType, destination, days, budget, preferences);

    return new RoutedPlanResult(agentType, result);
}

private AgentType analyzeRequestType(...) {
    ReactAgent routerAgent = ReactAgent.builder()
        .name("router_agent")
        .model(chatModel)
        .systemPrompt("""
            你是一个请求分类器。根据用户的旅游需求，判断应该由哪个专家处理。

            类型说明:
            - ATTRACTION: 用户主要关心景点推荐
            - HOTEL: 用户主要关心住宿安排
            - TRANSPORT: 用户主要关心交通出行
            - COMPREHENSIVE: 需要全面规划（包含景点、住宿、交通）
            """)
        .build();
    // ...
}
```

### 5.4 特点分析

| 优点 | 缺点 |
|------|------|
| 按需调用，节省资源 | 路由可能误判 |
| 响应更精准 | 需要设计分类逻辑 |
| 支持多类型请求 | 分类粒度需要权衡 |

### 5.5 适用场景

- 用户请求有多种类型
- 不同类型需要不同专业 Agent 处理
- 请求类型可以明确区分

---

## 6. Workflow 工作流模式

### 6.1 概念

预定义**固定的执行步骤**，支持条件分支和循环。

### 6.2 架构图

```
┌──────────────────────────────────────────────────────────────┐
│                    WorkflowOrchestrator                      │
│                                                               │
│   Step 1: ANALYZE (分析需求)                                   │
│       ↓                                                       │
│   Step 2: ATTRACTION (景点推荐)                                │
│       ↓                                                       │
│   Step 3: HOTEL (住宿推荐)                                     │
│       │                                                       │
│       ├── 预算高? ──→ recommendPremiumHotel()                  │
│       └── 预算低? ──→ recommendStandardHotel()                 │
│       ↓                                                       │
│   Step 4: TRANSPORT (交通规划)                                │
│       ↓                                                       │
│   Step 5: VALIDATE (验证方案)                                  │
│       │                                                       │
│       ├── 验证通过? ──→ Step 7                                │
│       └── 验证失败? ──→ Step 6: OPTIMIZE (优化方案)             │
│                           ↓                                   │
│   Step 7: FINALIZE (生成最终方案)                              │
│                                                               │
└──────────────────────────────────────────────────────────────┘
```

### 6.3 核心代码

```java
public WorkflowPlanResult executeWorkflow(String destination, int days,
                                          String budget, String preferences) {
    WorkflowContext context = new WorkflowContext(destination, days, budget, preferences);

    // 步骤1: 分析需求
    executeStep(context, WorkflowStep.ANALYZE, this::analyzeNeeds);

    // 步骤2: 景点推荐
    executeStep(context, WorkflowStep.ATTRACTION, this::recommendAttractions);

    // 步骤3: 条件分支 - 根据预算选择不同路径
    if (shouldRecommendPremiumHotel(context)) {
        executeStep(context, WorkflowStep.HOTEL, this::recommendPremiumHotel);
    } else {
        executeStep(context, WorkflowStep.HOTEL, this::recommendStandardHotel);
    }

    // 步骤4: 交通规划
    executeStep(context, WorkflowStep.TRANSPORT, this::planTransport);

    // 步骤5: 验证方案
    boolean valid = executeStep(context, WorkflowStep.VALIDATE, this::validatePlan);

    // 步骤6: 条件分支 - 验证不通过则优化
    if (!valid) {
        executeStep(context, WorkflowStep.OPTIMIZE, this::optimizePlan);
    }

    // 步骤7: 生成最终方案
    executeStep(context, WorkflowStep.FINALIZE, this::finalizePlan);

    return new WorkflowPlanResult(context);
}
```

### 6.4 工作流步骤定义

```java
public enum WorkflowStep {
    ANALYZE("分析需求"),
    ATTRACTION("景点推荐"),
    HOTEL("住宿推荐"),
    TRANSPORT("交通规划"),
    VALIDATE("验证方案"),
    OPTIMIZE("优化方案"),
    FINALIZE("生成最终方案");
}
```

### 6.5 特点分析

| 优点 | 缺点 |
|------|------|
| 流程可控可预测 | 缺乏灵活性 |
| 易于调试和追踪 | 需要提前设计流程 |
| 支持条件分支 | 不适合探索性任务 |

### 6.6 适用场景

| 场景 | 说明 |
|------|------|
| 流程固定的业务 | 如订单处理、审批流程 |
| 需要条件判断 | 根据不同条件走不同分支 |
| 需要精细化控制 | 控制每个步骤的输入输出 |
| 需要记录执行过程 | 追踪每步耗时和结果 |

---

## 7. Hierarchical Supervisor 层级监督模式

### 7.1 概念

**监督者 Agent + 专家 Agent** 的分层架构：
- **监督者（Supervisor）**：分析任务、分配任务、审核结果
- **专家（Experts）**：执行具体领域任务

### 7.2 架构图

```
                    ┌────────────────────┐
                    │   Supervisor Agent  │
                    │                    │
                    │  ┌──────────────┐  │
                    │  │ 1. 分析任务   │  │
                    │  │ 2. 分配任务   │  │
                    │  │ 3. 审核结果   │  │
                    │  │ 4. 迭代优化   │  │
                    │  └──────────────┘  │
                    └─────────┬──────────┘
                              │
            ┌─────────────────┼─────────────────┐
            ↓                 ↓                 ↓
    ┌───────────────┐ ┌───────────────┐ ┌───────────────┐
    │ Attraction    │ │    Hotel      │ │  Transport    │
    │    Agent      │ │    Agent      │ │    Agent      │
    │               │ │               │ │               │
    │ 景点推荐专家   │ │ 住宿推荐专家   │ │ 交通规划专家   │
    └───────────────┘ └───────────────┘ └───────────────┘
            │                 │                 │
            └─────────────────┴─────────────────┘
                              ↓
                    SupervisionDecision
                    (审核通过 / 需要修改)
```

### 7.3 执行流程

```java
public SupervisedPlanResult planWithSupervisor(...) {
    // Phase 1: 监督者分析任务并分配
    List<TaskAssignment> assignments = analyzeAndAssign(...);

    // Phase 2: 专家执行任务
    List<TaskResult> taskResults = new ArrayList<>();
    for (TaskAssignment assignment : assignments) {
        String result = executeAssignment(assignment);
        taskResults.add(new TaskResult(...));
    }

    // Phase 3: 监督者审核结果
    SupervisionDecision decision = reviewResults(taskResults, ...);

    // Phase 4: 迭代优化（如果不满意）
    while (!decision.approved() && iteration < MAX_ITERATIONS) {
        // 根据反馈重新执行特定任务
        for (String taskId : decision.needsRevision()) {
            // 修正并重新执行...
        }
        decision = reviewResults(taskResults, ...);
    }

    // Phase 5: 整合最终结果
    return new SupervisedPlanResult(...);
}
```

### 7.4 监督者提示词设计

```java
ReactAgent supervisor = ReactAgent.builder()
    .name("supervisor_agent")
    .systemPrompt("""
        你是旅游规划监督者。负责:
        1. 分析用户需求
        2. 制定任务计划
        3. 分配任务给专家

        可用专家:
        - attraction: 景点推荐专家
        - hotel: 住宿推荐专家
        - transport: 交通规划专家

        按以下格式输出任务分配:
        TASK_ID|AGENT_TYPE|ASSIGNMENT
        """)
    .build();
```

### 7.5 特点分析

| 优点 | 缺点 |
|------|------|
| 任务分解专业化 | 需要设计好监督者 |
| 支持迭代优化 | 可能需要多轮沟通 |
| 质量有保障 | Token 消耗较大 |

### 7.6 适用场景

| 场景 | 说明 |
|------|------|
| 复杂任务需分解 | 大任务拆成小任务 |
| 需要质量把控 | 监督者审核保证质量 |
| 任务间有依赖 | 协调各专家的工作 |
| 支持迭代优化 | 不满意可重新分配 |

---

## 8. Debate Negotiation 辩论协商模式

### 8.1 概念

多个 Agent **通过辩论讨论**，相互质疑、评论，最终达成共识。

### 8.2 架构图

```
┌─────────────────────────────────────────────────────────────┐
│                     辩论协商流程                              │
│                                                              │
│  Round 1:                                                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │ 景点专家    │  │ 住宿专家    │  │ 交通专家    │          │
│  │ 提出方案 A  │  │ 提出方案 B  │  │ 提出方案 C  │          │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘          │
│         │                │                │                 │
│         └────────────────┴────────────────┘                 │
│                          ↓                                   │
│              各专家相互评论其他方案                            │
│         ┌────── 讨论和质疑 ──────┐                          │
│         ↓           ↓           ↓                            │
│    景点评论B、C  住宿评论A、C  交通评论A、B                     │
│                          ↓                                   │
│              根据评论修正各自的方案                            │
│                                                              │
│  Round 2, Round 3... (重复以上过程)                          │
│                          ↓                                   │
│              检查是否达成共识                                  │
│           ┌─────────┴─────────┐                             │
│           ↓                   ↓                              │
│        达成共识            未达成共识 → 继续辩论               │
│           ↓                                                   │
│      整合最终方案                                             │
└─────────────────────────────────────────────────────────────┘
```

### 8.3 执行流程

```java
public DebatePlanResult planWithDebate(...) {
    // Phase 1: 各专家提出初始方案
    List<AgentProposal> proposals = generateInitialProposals(...);

    // Phase 2: 多轮辩论
    for (int round = 1; round <= MAX_ROUNDS; round++) {
        // 收集各专家对其他方案的评论
        List<AgentComment> comments = collectComments(proposals, round);

        // 根据评论修正方案
        proposals = reviseProposals(proposals, comments, round);

        // 检查是否达成共识
        if (checkConsensus(proposals)) {
            break;
        }
    }

    // Phase 3: 最终共识整合
    String finalPlan = generateConsensusPlan(proposals);
    return new DebatePlanResult(debateRounds, finalPlan);
}
```

### 8.4 评论机制

```java
ReactAgent commentator = ReactAgent.builder()
    .systemPrompt("""
        你是旅游规划辩论专家。从你的专业角度评论其他专家的方案。

        评论要求：
        1. 指出方案的优点
        2. 指出可能的不足或风险
        3. 提出改进建议
        4. 考虑与其他方案的协调性

        输出格式：
        STRENGTHS: 方案优点
        WEAKNESSES: 方案不足
        SUGGESTIONS: 改进建议
        """)
    .build();
```

### 8.5 特点分析

| 优点 | 缺点 |
|------|------|
| 综合多观点 | 耗时较长 |
| 减少单一偏见 | 可能无法达成共识 |
| 方案更全面 | Token 消耗大 |

### 8.6 适用场景

| 场景 | 说明 |
|------|------|
| 需要综合多观点 | 没有单一正确答案 |
| 权衡多种因素 | 平衡成本、时间、体验等 |
| 复杂决策问题 | 需要多角度分析 |
| 减少单一偏见 | 避免一个专家的局限性 |

---

## 9. AutoGen 人机协作模式

### 9.1 概念

**AutoGen** 是微软开发的多 Agent 对话框架，核心特点是支持**人类参与对话循环**。

核心特性：
- **Human-in-the-loop** - 用户可以在对话过程中提供反馈、修改建议
- **多轮对话协作** - Agent 之间通过对话交流，逐步完善方案
- **可配置的 Agent 角色** - AssistantAgent、UserProxyAgent 等
- **自动终止条件** - 当任务完成或满足条件时自动停止

### 9.2 架构图

```
┌─────────────────────────────────────────────────────────────┐
│                     AutoGenOrchestrator                     │
│                                                              │
│   ┌─────────────────┐                                        │
│   │ UserProxyAgent  │ ← 用户代理（可请求人类输入）             │
│   │   发送初始请求   │                                        │
│   └────────┬────────┘                                        │
│            ↓                                                  │
│   ┌─────────────────────────────────────────┐               │
│   │          GroupChatManager                │               │
│   │        选择下一个发言者                   │               │
│   └────────────────┬────────────────────────┘               │
│                     ↓                                         │
│   ┌────────────────┼────────────────┐                        │
│   ↓                ↓                ↓                        │
│ ┌───────────┐ ┌───────────┐ ┌───────────┐                   │
│ │Assistant  │ │Assistant  │ │Assistant  │                   │
│ │景点专家    │ │住宿专家   │ │交通专家    │                   │
│ └───────────┘ └───────────┘ └───────────┘                   │
│       ↓              ↓              ↓                        │
│   ┌─────────────────────────────────────────┐               │
│   │         共享对话历史                      │               │
│   │   所有 Agent 可以看到之前的对话           │               │
│   └────────────────┬────────────────────────┘               │
│                     ↓                                         │
│   ┌─────────────────────────────────────────┐               │
│   │       TerminationCondition              │               │
│   │   检查终止条件（关键词/最大轮数/用户确认）│               │
│   └────────────────┬────────────────────────┘               │
│                     ↓                                         │
│              ┌──────┴──────┐                                  │
│              ↓             ↓                                  │
│          继续对话        终止对话                              │
│              ↓             ↓                                  │
│                      生成最终方案                             │
└─────────────────────────────────────────────────────────────┘
```

### 9.3 核心组件

#### 9.3.1 ConversableAgent（可对话 Agent 基类）

```java
public abstract class ConversableAgent {
    protected String name;
    protected String systemPrompt;
    protected ChatModel chatModel;
    protected List<ConversationMessage> conversationHistory;

    // 发送消息
    public String send(String message, ConversationState state);

    // 接收消息并生成回复
    public abstract String receive(String message, ConversableAgent sender, ConversationState state);

    // 是否请求人工输入
    public abstract boolean shouldRequestHumanInput();
}
```

#### 9.3.2 AssistantAgent（AI 助手 Agent）

```java
public class AssistantAgent extends ConversableAgent {
    // 使用 ReactAgent 执行工具调用
    // 可以看到完整的对话历史
    // 不会请求人工输入

    @Override
    protected String generateReply(String message, ConversationState state) {
        // 优先使用带工具的 ReactAgent
        if (!tools.isEmpty()) {
            return generateReplyWithTools(message, state);
        }
        return callLlm(message);
    }
}
```

#### 9.3.3 UserProxyAgent（用户代理 Agent）

```java
public class UserProxyAgent extends ConversableAgent {
    private HumanInputMode humanInputMode; // ALWAYS, NEVER, TERMINATE
    private Function<String, String> humanInputProvider; // 获取用户输入的回调

    public enum HumanInputMode {
        ALWAYS,    // 每次都请求用户输入
        NEVER,     // 从不请求用户输入
        TERMINATE  // 仅在终止条件时请求用户确认
    }
}
```

#### 9.3.4 GroupChat（群聊管理）

```java
public class GroupChat {
    private List<ConversableAgent> agents;
    private GroupChatManager manager;
    private TerminationCondition terminationCondition;

    public GroupChatResult run(String initialMessage) {
        while (!state.isTerminated()) {
            // 选择下一个发言者
            currentSpeaker = manager.selectNextSpeaker(agents, state);

            // 让当前发言者回复
            String reply = currentSpeaker.receive(currentMessage, sender, state);

            // 检查终止条件
            if (terminationCondition.shouldTerminate(state)) {
                break;
            }
        }
        return new GroupChatResult(...);
    }
}
```

### 9.4 人类输入模式

| 模式 | 说明 | 适用场景 |
|------|------|----------|
| `ALWAYS` | 每轮都请求用户输入 | 完全人工参与，精细控制 |
| `NEVER` | 从不请求用户输入 | 完全自动，无需人工干预 |
| `TERMINATE` | 仅在终止条件时请求用户确认 | 平衡自动与人工控制 |

### 9.5 使用示例

```java
@Autowired
private AutoGenOrchestrator autoGenOrchestrator;

// 自动模式（无人工参与）
AutoGenPlanResult result = autoGenOrchestrator.planAuto(
    "北京", 3, "中等", "文化历史");

// 人工参与模式
AutoGenPlanResult result = autoGenOrchestrator.planWithAutoGen(
    "北京", 3, "中等", "文化历史",
    (prompt) -> {
        // 通过 API/控制台/WebSocket 获取用户输入
        System.out.println(prompt);
        return scanner.nextLine(); // 用户输入
    });
```

### 9.6 对话示例

```
=== Round 1 ===
[user_proxy]: 我需要一个北京3天的旅游规划，预算中等，偏好文化历史景点。

[attraction_expert]: 根据您的偏好，我推荐以下景点：
Day1: 故宫 → 景山公园 → 南锣鼓巷
Day2: 颐和园 → 圆明园
Day3: 天坛 → 前门大街

[hotel_expert]: 基于景点分布，我建议住宿安排：
- Day1-2: 住西城区，靠近地铁站
- Day3: 可考虑前门附近

[transport_expert]: 交通建议：
- 地铁为主，推荐购买三日卡
- 故宫到颐和园建议打车约30分钟

[coordinator]: 各位专家的建议已汇总。请问您对方案是否满意？
如果需要调整，请告诉我具体需求。

=== Round 2 ===
[user_proxy]: 第一天行程有点赶，可以调整吗？

[attraction_expert]: 好的，我调整一下：
Day1: 故宫（详细游览）→ 景山公园（看日落）
Day2: 天坛（上午）→ 颐和园（下午）
Day3: 南锣鼓巷 → 圆明园拾遗

...

[coordinator]: 方案已完成，请确认。
```

### 9.7 特点分析

| 优点 | 缺点 |
|------|------|
| 支持人工干预和审核 | 实现复杂度较高 |
| 多轮对话逐步优化 | 需要设计终止条件 |
| 对话历史共享，上下文连贯 | Token 消耗可能很大 |
| 灵活的人类输入模式 | 需要回调机制支持 |

### 9.8 适用场景

| 场景 | 说明 |
|------|------|
| 需要人工审核 | 用户希望参与决策 |
| 逐步细化方案 | 通过多轮对话完善 |
| 复杂规划任务 | 需要综合多个专家意见 |
| 用户偏好不确定 | 需要通过对话明确需求 |

---

## 10. 模式对比总结

### 10.1 核心对比

| 模式 | 核心思想 | 控制方式 | 人类参与 | Token消耗 | 适用场景 |
|------|----------|----------|----------|-----------|----------|
| **ReAct** | 思考→行动→观察 | Agent自主 | 无 | 中等 | 需要工具调用的任务 |
| **CoT** | 显式推理步骤 | 嵌入提示词 | 无 | 低 | 需要复杂推理的问题 |
| **Sequential** | 串行执行 | 代码控制 | 无 | 中等 | 有依赖关系的任务 |
| **Parallel** | 并行执行 | 代码控制 | 无 | 中等 | 无依赖的独立任务 |
| **Router** | 智能路由 | Agent分类 | 无 | 低 | 多类型请求分发 |
| **Workflow** | 预定义步骤 | 代码控制 | 无 | 可控 | 流程固定的业务 |
| **Hierarchical** | 监督者协调 | Agent协调 | 无 | 高 | 复杂任务需分解质控 |
| **Debate** | 辩论协商 | 议事规则 | 无 | 很高 | 需要权衡多方观点 |
| **AutoGen** | 人机对话协作 | 对话循环 | 可选 | 高 | 需要人工参与决策 |

### 10.2 选择指南

```
                    ┌─────────────────────────────┐
                    │      是否需要人类参与？       │
                    └─────────────┬───────────────┘
                           ↓
                 ┌─────────┴─────────┐
                 ↓                   ↓
              需要                不需要
                 ↓                   ↓
           ┌─────┴─────┐     ┌──────┴──────┐
           │  AutoGen  │     │ 任务是否可分解│
           └───────────┘     └──────┬──────┘
                                    ↓
                          ┌─────────┴─────────┐
                          ↓                   ↓
                       可分解              不可分解
                          ↓                   ↓
                  ┌───────┴───────┐     ┌─────┴─────┐
                  │ 任务间有依赖?  │     │需要工具?  │
                  └───────┬───────┘     └─────┬─────┘
                     ↓         ↓           ↓       ↓
                   是        否          是      否
                     ↓         ↓           ↓       ↓
               Workflow   Parallel     ReAct    Router
                   ↓         ↓
               Hierarchical Debate
```

### 10.3 实际应用建议

1. **简单推理任务**：使用 CoT，一步到位
2. **需要工具调用**：使用 ReAct，自主决策
3. **有依赖的串行任务**：使用 Sequential Chain
4. **无依赖的并行任务**：使用 Parallel Execution
5. **多类型请求**：使用 Router Dispatch
6. **流程固定业务**：使用 Workflow，可预测可控
7. **复杂分解任务**：使用 Hierarchical，质量有保障
8. **多方权衡决策**：使用 Debate，综合全面
9. **需要人工参与**：使用 AutoGen，人机协作

---

## 附录：项目文件索引

```
backend/src/main/java/cn/sdh/travel/agent/
├── expert/
│   ├── AttractionAgent.java           # 景点推荐专家
│   ├── HotelAgent.java                # 住宿推荐专家
│   └── TransportAgent.java            # 交通规划专家
├── orchestrator/
│   ├── SequentialChainOrchestrator.java    # 顺序链模式
│   ├── ParallelExecutionOrchestrator.java  # 并行执行模式
│   ├── RouterDispatchOrchestrator.java     # 路由分发模式
│   ├── WorkflowOrchestrator.java           # 工作流模式
│   ├── HierarchicalSupervisorOrchestrator.java  # 层级监督模式
│   ├── DebateNegotiationOrchestrator.java # 辩论协商模式
│   └── AutoGenOrchestrator.java           # AutoGen人机协作模式
├── autogen/
│   ├── ConversableAgent.java           # 可对话Agent基类
│   ├── AssistantAgent.java             # AI助手Agent
│   ├── UserProxyAgent.java             # 用户代理Agent
│   ├── GroupChat.java                  # 群聊管理
│   ├── GroupChatManager.java           # 发言者选择
│   ├── GroupChatResult.java            # 对话结果
│   ├── ConversationState.java          # 对话状态
│   ├── ConversationMessage.java        # 消息记录
│   ├── TerminationCondition.java       # 终止条件
│   ├── HumanInputMode.java             # 人类输入模式
│   └── MessageType.java                # 消息类型
├── supervisor/
│   └── TravelSupervisorPlanAgent.java  # 旅行规划监督者
└── tool/
    ├── GaodeMapTool.java               # 高德地图工具
    ├── AgentTool.java                  # Agent工具定义
    └── AgentToolExecutor.java          # 工具执行器
```

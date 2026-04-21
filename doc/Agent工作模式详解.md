# Agent 工作模式详解

本文档详细介绍 AI Agent 的核心工作模式，包括 ReAct、CoT 以及多 Agent 协作模式。

---

## 目录

1. [ReAct 模式](#1-react-模式)
2. [CoT 思维链](#2-cot-思维链)
3. [Workflow 工作流模式](#3-workflow-工作流模式)
4. [Hierarchical Supervisor 层级监督模式](#4-hierarchical-supervisor-层级监督模式)
5. [Debate Negotiation 辩论协商模式](#5-debate-negotiation-辩论协商模式)
6. [模式对比总结](#6-模式对比总结)

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

## 3. Workflow 工作流模式

### 3.1 概念

预定义**固定的执行步骤**，支持条件分支和循环。

### 3.2 架构图

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

### 3.3 核心代码

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

### 3.4 执行器模式

```java
// 统一的步骤执行器
private <T> T executeStep(WorkflowContext context, WorkflowStep step,
                          StepExecutor<T> executor) throws GraphRunnerException {
    log.info("【工作流模式】执行步骤: {} - {}", step.name(), step.getDescription());
    long startTime = System.currentTimeMillis();

    T result = executor.execute(context);  // 执行具体步骤

    // 记录执行时间和结果
    context.addStepResult(step.name(), result);
    context.addStepDuration(step.name(), System.currentTimeMillis() - startTime);

    return result;
}

// 函数式接口
@FunctionalInterface
private interface StepExecutor<T> {
    T execute(WorkflowContext context) throws GraphRunnerException;
}
```

### 3.5 工作流步骤定义

```java
public enum WorkflowStep {
    ANALYZE("分析需求"),
    ATTRACTION("景点推荐"),
    HOTEL("住宿推荐"),
    TRANSPORT("交通规划"),
    VALIDATE("验证方案"),
    OPTIMIZE("优化方案"),
    FINALIZE("生成最终方案");

    private final String description;
    // ...
}
```

### 3.6 特点分析

| 优点 | 缺点 |
|------|------|
| 流程可控可预测 | 缺乏灵活性 |
| 易于调试和追踪 | 需要提前设计流程 |
| 支持条件分支 | 不适合探索性任务 |

### 3.7 适用场景

| 场景 | 说明 |
|------|------|
| 流程固定的业务 | 如订单处理、审批流程 |
| 需要条件判断 | 根据不同条件走不同分支 |
| 需要精细化控制 | 控制每个步骤的输入输出 |
| 需要记录执行过程 | 追踪每步耗时和结果 |

---

## 4. Hierarchical Supervisor 层级监督模式

### 4.1 概念

**监督者 Agent + 专家 Agent** 的分层架构：
- **监督者（Supervisor）**：分析任务、分配任务、审核结果
- **专家（Experts）**：执行具体领域任务

### 4.2 架构图

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

### 4.3 执行流程

```java
public SupervisedPlanResult planWithSupervisor(String destination, int days,
                                               String budget, String preferences) {
    // Phase 1: 监督者分析任务并分配
    List<TaskAssignment> assignments = analyzeAndAssign(destination, days, budget, preferences);
    // 输出示例:
    // task_1 | attraction | 推荐北京3天主要景点
    // task_2 | hotel      | 推荐北京3天住宿区域
    // task_3 | transport  | 规划北京3天交通路线

    // Phase 2: 专家执行任务
    List<TaskResult> taskResults = new ArrayList<>();
    for (TaskAssignment assignment : assignments) {
        String result = executeAssignment(assignment);
        taskResults.add(new TaskResult(assignment.taskId(), assignment.agentType(), result));
    }

    // Phase 3: 监督者审核结果
    SupervisionDecision decision = reviewResults(taskResults, destination, days, budget, preferences);

    // Phase 4: 迭代优化（如果不满意）
    int iteration = 0;
    while (!decision.approved() && iteration < MAX_ITERATIONS) {
        iteration++;

        // 根据反馈重新执行特定任务
        for (String taskId : decision.needsRevision()) {
            TaskAssignment assignment = // 找到对应任务...;
            String revisedPrompt = assignment.assignment()
                + "\n\n请注意以下反馈：" + decision.feedback();
            String result = executeAssignment(/*...*/);

            // 更新结果
            // ...
        }

        // 重新审核
        decision = reviewResults(taskResults, destination, days, budget, preferences);
    }

    // Phase 5: 整合最终结果
    String finalPlan = generateFinalPlan(taskResults);
    return new SupervisedPlanResult(assignments, taskResults, finalPlan, decision.approved());
}
```

### 4.4 监督者提示词设计

```java
private List<TaskAssignment> analyzeAndAssign(...) {
    ReactAgent supervisor = ReactAgent.builder()
        .name("supervisor_agent")
        .model(chatModel)
        .systemPrompt("""
            你是旅游规划监督者。负责:
            1. 分析用户需求
            2. 制定任务计划
            3. 分配任务给专家

            可用专家:
            - attraction: 景点推荐专家
            - hotel: 住宿推荐专家
            - transport: 交通规划专家

            按以下格式输出任务分配(每个任务一行):
            TASK_ID|AGENT_TYPE|ASSIGNMENT
            例如:
            task_1|attraction|推荐北京3天的主要景点
            task_2|hotel|推荐北京3天的住宿区域
            task_3|transport|规划北京3天的交通路线
            """)
        .build();
    // ...
}
```

### 4.5 审核机制

```java
private SupervisionDecision reviewResults(List<TaskResult> results, ...) {
    ReactAgent reviewer = ReactAgent.builder()
        .name("supervisor_reviewer")
        .model(chatModel)
        .systemPrompt("""
            你是旅游规划审核专家。审核各专家的结果，判断是否满足用户需求。

            输出格式:
            APPROVED: true/false
            FEEDBACK: 具体反馈意见
            NEEDS_REVISION: 需要修改的任务ID列表(逗号分隔)

            如果所有结果都满意，输出 APPROVED: true
            如果需要修改，输出需要修改的任务ID。
            """)
        .build();
    // ...
}
```

### 4.6 特点分析

| 优点 | 缺点 |
|------|------|
| 任务分解专业化 | 需要设计好监督者 |
| 支持迭代优化 | 可能需要多轮沟通 |
| 质量有保障 | Token 消耗较大 |

### 4.7 适用场景

| 场景 | 说明 |
|------|------|
| 复杂任务需分解 | 大任务拆成小任务 |
| 需要质量把控 | 监督者审核保证质量 |
| 任务间有依赖 | 协调各专家的工作 |
| 支持迭代优化 | 不满意可重新分配 |

---

## 5. Debate Negotiation 辩论协商模式

### 5.1 概念

多个 Agent **通过辩论讨论**，相互质疑、评论，最终达成共识。

### 5.2 架构图

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

### 5.3 执行流程

```java
public DebatePlanResult planWithDebate(String destination, int days,
                                        String budget, String preferences) {
    // Phase 1: 各专家提出初始方案
    List<AgentProposal> initialProposals = generateInitialProposals(destination, days, budget, preferences);

    List<DebateRound> debateRounds = new ArrayList<>();

    // Phase 2: 多轮辩论
    for (int round = 1; round <= MAX_ROUNDS; round++) {
        log.info("【辩论模式】第{}轮辩论开始", round);

        // 2.1 收集各专家对其他方案的评论
        List<AgentComment> comments = new ArrayList<>();
        for (AgentProposal proposal : initialProposals) {
            comments.addAll(collectComments(proposal, initialProposals, round));
        }

        // 2.2 根据评论修正方案
        List<AgentProposal> revisedProposals = reviseProposals(initialProposals, comments, round);

        DebateRound debateRound = new DebateRound(round, comments, revisedProposals);
        debateRounds.add(debateRound);

        // 2.3 检查是否达成共识
        if (checkConsensus(revisedProposals)) {
            log.info("【辩论模式】第{}轮达成共识", round);
            break;
        }

        initialProposals = revisedProposals;
    }

    // Phase 3: 最终共识整合
    String finalPlan = generateConsensusPlan(initialProposals);

    return new DebatePlanResult(debateRounds, finalPlan);
}
```

### 5.4 评论机制

```java
private List<AgentComment> collectComments(AgentProposal targetProposal,
                                           List<AgentProposal> allProposals,
                                           int round) {
    ReactAgent commentator = ReactAgent.builder()
        .name("debate_commentator")
        .model(chatModel)
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
    // ...
}
```

### 5.5 辩论示例

```
用户需求：北京3天旅行，预算中等

=== Round 1 ===

【景点专家方案 A】
Day1: 故宫 → 景山 → 南锣鼓巷
Day2: 颐和园 → 圆明园
Day3: 天坛 → 前门大街

【住宿专家评论】
STRENGTHS: 景点选择经典
WEAKNESSES: Day1行程都在市中心，住宿成本高
SUGGESTIONS: 建议第一天住在地铁沿线，节省住宿费

【交通专家评论】
STRENGTHS: 路线设计合理
WEAKNESSES: 颐和园和圆明园一天太赶
SUGGESTIONS: 建议拆分到两天

=== Round 2 ===

【修正后的方案 A'】
Day1: 故宫 → 景山（住西直门附近）
Day2: 颐和园（住附近）
Day3: 天坛 → 前门 → 圆明园拾遗

...

=== 最终整合方案 ===
...
```

### 5.6 特点分析

| 优点 | 缺点 |
|------|------|
| 综合多观点 | 耗时较长 |
| 减少单一偏见 | 可能无法达成共识 |
| 方案更全面 | Token 消耗大 |

### 5.7 适用场景

| 场景 | 说明 |
|------|------|
| 需要综合多观点 | 没有单一正确答案 |
| 权衡多种因素 | 平衡成本、时间、体验等 |
| 复杂决策问题 | 需要多角度分析 |
| 减少单一偏见 | 避免一个专家的局限性 |

---

## 6. 模式对比总结

### 6.1 核心对比

| 模式 | 核心思想 | 控制方式 | Token消耗 | 适用场景 |
|------|----------|----------|-----------|----------|
| **ReAct** | 思考→行动→观察循环 | Agent自主决策 | 中等 | 需要工具调用的任务 |
| **CoT** | 显式展示推理步骤 | 嵌入提示词 | 低 | 需要复杂推理的问题 |
| **Workflow** | 预定义步骤+条件分支 | 代码控制流程 | 可控 | 流程固定的业务 |
| **Hierarchical** | 监督者分配+专家执行 | 监督者协调 | 高 | 复杂任务需分解质控 |
| **Debate** | 多Agent辩论协商 | 议事规则 | 很高 | 需要权衡多方观点 |

### 6.2 选择指南

```
                    ┌─────────────────────────────┐
                    │      任务是否需要外部信息？   │
                    └─────────────┬───────────────┘
                           ↓
                 ┌─────────┴─────────┐
                 ↓                   ↓
              需要                不需要
                 ↓                   ↓
         ┌──────┴──────┐     ┌──────┴──────┐
         ↓             ↓     ↓             ↓
      单Agent      多Agent  单Agent     多Agent
         ↓             ↓     ↓             ↓
      ReAct      任务可分解?  CoT      需要共识?
                      ↓                  ↓
               ┌──────┴──────┐     ┌──────┴──────┐
               ↓             ↓     ↓             ↓
              是            否    是            否
               ↓             ↓     ↓             ↓
          Workflow     Debate  Debate      Hierarchical
```

### 6.3 实际应用建议

1. **简单推理任务**：使用 CoT，一步到位
2. **需要工具调用**：使用 ReAct，自主决策
3. **流程固定业务**：使用 Workflow，可预测可控
4. **复杂分解任务**：使用 Hierarchical，质量有保障
5. **多方权衡决策**：使用 Debate，综合全面

---

## 附录：项目文件索引

```
backend/src/main/java/cn/sdh/travel/agent/
├── expert/
│   ├── AttractionAgent.java      # 景点推荐专家
│   ├── HotelAgent.java           # 住宿推荐专家
│   └── TransportAgent.java       # 交通规划专家
├── orchestrator/
│   ├── WorkflowOrchestrator.java           # 工作流模式
│   ├── HierarchicalSupervisorOrchestrator.java  # 层级监督模式
│   ├── DebateNegotiationOrchestrator.java # 辩论协商模式
│   ├── SequentialChainOrchestrator.java    # 顺序链模式
│   ├── ParallelExecutionOrchestrator.java # 并行执行模式
│   └── RouterDispatchOrchestrator.java    # 路由分发模式
└── tool/
    ├── AgentTool.java            # Agent工具定义
    └── AgentToolExecutor.java    # 工具执行器
```

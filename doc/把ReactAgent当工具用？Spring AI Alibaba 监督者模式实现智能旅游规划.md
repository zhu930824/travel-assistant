# 把ReactAgent当工具用？Spring AI Alibaba 监督者模式实现智能旅游规划

![图片](https://mmbiz.qpic.cn/sz_mmbiz_png/7eiapngsNZCDx9dcP2xPiayuEgRHhWuDqsGCNpuGiaMLYcKdAbtEBb3TOsyWEsCURFibJG3VuUubMC7lNmzpLG7W4traV5xBFzGnaMPBp5dTF38/640?wx_fmt=png&from=appmsg&tp=wxpic&wxfrom=5&wx_lazy=1&watermark=1#imgIndex=0)

## **1. 引言** 

前面的几篇分别介绍四种不同的多智能体交接策略，有串行、并行、路由、监督者策略的，在前一篇[多智能体实战 | Spring AI Alibaba 监督者交接策略实现旅游行程规划智能体](https://mp.weixin.qq.com/s?__biz=MzU3MTAzNTMzMQ==&mid=2247488685&idx=1&sn=e0c9d628e55f565447e6a04a3602da8c&scene=21#wechat_redirect)中，也提到了在SpringAI alibaba的最新版本中，移除了SupervisorAgent，推荐使用AgentAsTool的方式来实现。因此接下来这一篇将介绍如何将ReactAgent作为工具来交接给主Agent使用

SpringAI Alibaba框架提供了Sub Agent机制，允许我们将多个专业化的Agent组合在一起，通过一个监督者Agent来协调调度，实现任务的分工与协作。



本文将以一个旅游规划智能体为例，详细介绍如何使用Sub Agent实现监督者策略，并解决官方AgentTool因为同步阻塞的调用方式，从而导致的无法流式输出响应给前端的问题。

我们这里的设计目标，依然是沿用上篇文章中提到的《旅游行程规划智能体》

## **2. Sub Agent 实现方式** 

### 2.1 什么是Sub Agent

Sub Agent是一种多智能体协作模式，核心思想是：

- 监督者Agent

  ：负责理解用户需求，协调调度各个专家Agent

- 专家Agent

  ：专注于特定领域的任务执行（如景点推荐、住宿规划、交通安排）

- 工具注入

  ：将专家Agent作为Tool注册到监督者Agent中

### 2.2 官方Agent Tool支持

SpringAI Alibaba官方提供了Agent Tool支持，可以将ReactAgent作为Tool注入到另一个Agent中。详细参考：Agent Tool官方文档

**基本用法**：

```
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.AgentTool;

// 创建专家Agent
ReactAgentexpertAgent= ReactAgent.builder()
    .name("expert_agent")
    .model(chatModel)
    .description("专家Agent的描述")
    .systemPrompt("你是一个专业领域的专家...")
    .build();

// 创建监督者Agent，将专家Agent作为Tool注入
ReactAgentsupervisorAgent= ReactAgent.builder()
    .name("supervisor_agent")
    .model(chatModel)
    .systemPrompt("你是监督者，负责协调专家...")
    .tools(AgentTool.create(expertAgent))  // 将专家Agent作为Tool
    .build();
```

### 2.3 Sub Agent的工作流程

![图片](https://mmbiz.qpic.cn/sz_mmbiz_png/7eiapngsNZCBNkiau7nt0UicA44brSU3KeaLAu8MXfmosGmDibSbKISsNMmdj9ghxhiaL76hOEa9OCry3zHkXDUYhPzApl9A97tVBNRuZsnnfsa8/640?wx_fmt=png&from=appmsg&tp=wxpic&wxfrom=5&wx_lazy=1&watermark=1#imgIndex=1)

```
用户请求
    ↓
监督者Agent（理解需求，制定计划）
    ↓
调用专家Agent Tool
    ├─→ 专家Agent 1（景点推荐）
    ├─→ 专家Agent 2（住宿规划）
    └─→ 专家Agent 3（交通安排）
    ↓
监督者Agent（整合结果，输出方案）
```

## **3. 旅游规划智能体实现** 

基于Sub Agent机制，我们实现了一个旅游规划智能体系统。该系统包含一个监督者Agent和三个专家Agent。

### 3.1 系统架构

![图片](https://mmbiz.qpic.cn/sz_mmbiz_png/7eiapngsNZCDjmYjoQx2XO2B5yzX0qGAzBfs7DySV2mw04gPUbUaXXtuMbxSyyu2orQqCyg6hsanGj3KmZy9HP95AUCPJCE9DwxJHtHvE4Hk/640?wx_fmt=png&from=appmsg&tp=wxpic&wxfrom=5&wx_lazy=1&watermark=1#imgIndex=2)

![图片](https://mmbiz.qpic.cn/sz_mmbiz_png/7eiapngsNZCCX60NaqmuibcDrGWEr0B63ljIxDm0ZGrWEYmvqnVrJe7noqiccsJqbWqmPLKQuFofHBukDv0b5sKCKVKGoxUsv7mYFKayz3F1icM/640?wx_fmt=png&from=appmsg&tp=wxpic&wxfrom=5&wx_lazy=1&watermark=1#imgIndex=3)

``

### 3.2 专家Agent实现

每个专家Agent都是独立的ReactAgent，拥有自己的系统提示和职责：

**景点推荐专家**：

```
@Component
publicclassAttractionAgent {
    privatefinal ChatModel chatModel;

    public ReactAgent agent() {
        return ReactAgent.builder()
                .name("attraction_agent")
                .model(chatModel)
                .description("景点推荐专家，善于根据用户的需求推荐各种适合游玩的景点")
                .systemPrompt("""
                        你是一个旅行景点推荐专家。请根据用户的目的地、天数、偏好，
                        推荐合适的景点和游览顺序。如果某些信息不完整，请基于已有信息尽量给出合理建议。
                        直接返回景点计划，包含每日安排。
                        """)
                .outputKey("attraction_plan")
                .enableLogging(true)
                .build();
    }
}
```

**住宿推荐专家**：

```
@Component
publicclassHotelAgent {
    privatefinal ChatModel chatModel;

    public ReactAgent agent() {
        return ReactAgent.builder()
                .name("hotel_agent")
                .model(chatModel)
                .description("住宿推荐专家，善于根据用户需求推荐适合的住宿")
                .systemPrompt("""
                        你是一个住宿推荐专家。请根据用户的景点计划、预算，推荐合适的住宿地点和酒店。
                        请返回住宿建议，包括区域和酒店示例。
                        """)
                .outputKey("hotel_plan")
                .enableLogging(true)
                .build();
    }
}
```

**交通规划专家**：

```
@Component
publicclassTransportAgent {
    privatefinal ChatModel chatModel;

    public ReactAgent agent() {
        return ReactAgent.builder()
                .name("transport_agent")
                .model(chatModel)
                .description("交通规划专家，善于根据用户需求规划最佳的出行方式")
                .systemPrompt("""
                        你是一个交通规划专家。请根据用户的出发地、目的地、景点安排，规划交通方式。
                        请返回详细的交通方案，包括城际交通和市内交通建议。
                        """)
                .outputKey("transport_plan")
                .enableLogging(true)
                .build();
    }
}
```

### 3.3 监督者Agent实现

监督者Agent负责协调各个专家，通过工具调用的方式调度子Agent：

```
@Component
publicclassTravelSupervisorPlanAgent {
    privatefinal AttractionAgent attractionAgent;
    privatefinal HotelAgent hotelAgent;
    privatefinal TransportAgent transportAgent;
    privatefinal ChatModel chatModel;

    public Agent agent(String sessionId) {
        ReactAgentagent= ReactAgent.builder()
                .name("supervisor_agent")
                .model(chatModel)
                .systemPrompt("""
                        你是一个旅行规划监督者，你的任务是帮助用户制定旅行计划。
                        你可以协调以下专家：
                        - attraction_agent：负责推荐景点、安排游览路线。
                        - transport_agent：负责规划城市间交通和市内交通。
                        - hotel_agent：负责推荐酒店、民宿等住宿。

                        请记得在制定计划时，始终先调用 attraction_agent 来推荐景点、安排旅游路线；
                        然后再根据推荐的景点来调用 transport_agent 和 hotel_agent，用于规划行程和住宿情况。
                        最后需要将所有的专家输出的内容进行重新组织整合，输出完整的方案给用户。
                        """)
                .saver(newMemorySaver())
                .tools(
                    AgentTool.create(attractionAgent.agent()),
                    AgentTool.create(hotelAgent.agent()),
                    AgentTool.create(transportAgent.agent())
                )
                .build();
        return agent;
    }
}
```

## **4. AgentAsTool同步调用的问题** 

### 4.1 官方实现的局限性

当我们使用官方提供的`AgentTool.create()`将子Agent作为Tool时，会遇到一个关键问题：**同步阻塞调用**。

查看官方AgentTool的实现，我们可以看到它是采用 **invoke(xxx)** 的方式执行的，这种同步执行subAgent的方案倒也没有什么问题，但是对于我们前端需要获取子Agent的流式输出时，就不太友好了（特别是子Agent的执行耗时较长时，傻等着就显得有点呆了🤣）

```
public AssistantMessage executeAgent(String input, ToolContext toolContext) {
    // Extract the actual input value from the wrapped JSON structure
    // The input parameter is wrapped as {"input": "actual_value"}
    StringactualInput= extractInputValue(input);

    // Build the messages list to add
    // Add instruction first if present, then the user input
    // Note: We must add all messages at once because cloneState doesn't copy keyStrategies,
    // so multiple updateState calls would overwrite instead of append
    List<Message> messagesToAdd = newArrayList<>();
    if (StringUtils.hasLength(agent.instruction())) {
        messagesToAdd.add(AgentInstructionMessage.builder().text(agent.instruction()).build());
    }
    messagesToAdd.add(newUserMessage(actualInput));

    // 官方的核心实现，这里是同步的调用方式
    Optional<OverAllState> resultState = agent.getAndCompileGraph().invoke(Map.of("messages", messagesToAdd));

    Optional<List> messages = resultState.flatMap(overAllState -> overAllState.value("messages", List.class));
    if (messages.isPresent()) {
        @SuppressWarnings("unchecked")
        List<Message> messageList = (List<Message>) messages.get();
        // Use messageList
        return (AssistantMessage) messageList.get(messageList.size() - 1);
    }
    
    thrownewRuntimeException("Failed to execute agent tool or failed to get agent tool result");
}
```

### 4.2 问题分析

这种同步阻塞方式导致了几个问题：

| 问题             | 影响                                    |
| ---------------- | --------------------------------------- |
| **无法流式输出** | 子Agent的执行过程无法实时传递给前端     |
| **用户体验差**   | 用户只能看到"加载中"，无法了解进度      |
| **丢失中间结果** | 子Agent的思考过程、工具调用过程都丢失了 |
| **响应时间长**   | 需要等待所有子Agent完成才能返回         |

### 4.3 前端体验对比

![图片](https://mmbiz.qpic.cn/mmbiz_png/7eiapngsNZCCG9SuBTTjPnPe8Y5uXsE1xLFcJL0FCqmvFpP3ia4R56QM81lEJ3TfjHQJwBz4VgpiaWTIZpKbyRHS3yytocQXJJSTHG6BOMwiad0/640?wx_fmt=png&from=appmsg&tp=wxpic&wxfrom=5&wx_lazy=1&watermark=1#imgIndex=4)

**使用官方AgentTool（同步阻塞）**：

```
用户点击"开始规划"
    ↓
等待...（无反馈）
    ↓
等待...（无反馈）
    ↓
突然显示完整结果
```

**理想的流式输出体验**：

```
用户点击"开始规划"
    ↓
"正在为您推荐景点..."
    ↓
景点推荐内容逐步显示...
    ↓
"正在为您规划住宿..."
    ↓
住宿推荐内容逐步显示...
    ↓
"正在为您安排交通..."
    ↓
完整方案呈现
```



##  

## **5. 自定义AgentTool实现流式输出** 

为了解决官方AgentTool的同步阻塞问题，我们实现了自定义的AgentTool，核心改进是支持将子Agent的流式输出实时转发给前端。

### 5.1 整体思路

```
子Agent流式输出 → PlanContext → SSE → 前端实时显示
```

关键点：

1. 在Controller层建立SSE连接时，将连接存储到PlanContext
2. 自定义AgentTool在执行子Agent时，从PlanContext获取SSE连接
3. 将子Agent的流式输出实时推送到前端

### 5.2 PlanContext实现

PlanContext用于管理SSE连接，支持按sessionId存储和获取：

```
public classPlanContext {
    privatestatic InheritableThreadLocal<FluxSink> context = newInheritableThreadLocal<>();
    privatestatic Map<String, FluxSink> sessionEmitter = newConcurrentHashMap<>();

    publicstaticvoidset(FluxSink sink) {
        context.set(sink);
    }

    publicstatic FluxSink get() {
        return context.get();
    }

    publicstaticvoidsetSessionEmitter(String sessionId, FluxSink sink) {
        sessionEmitter.put(sessionId, sink);
    }

    publicstatic FluxSink getSessionEmitter(String sessionId) {
        return sessionEmitter.get(sessionId);
    }

    publicstaticvoidremoveSessionEmitter(String sessionId) {
        sessionEmitter.remove(sessionId);
    }
}
```

### 5.3 自定义AgentTool实现

自定义AgentTool的核心改进在于执行子Agent时，将流式输出实时转发：

```
@Slf4j
publicclassAgentTool {
    privatestaticfinalToolCallResultConverterCONVERTER=newMessageToolCallResultConverter();

    publicstatic ToolCallback create(ReactAgent agent) {
        // 使用反射获取Agent的输入Schema
        StringoriginalSchema= getInputSchemaFromAgent(agent);
        
        // 构建工具定义
        DefaultToolDefinition.Builderbuilder= ToolDefinitions
                .builder(method)
                .name(agent.name())
                .description(agent.description());
        
        if (StringUtils.hasLength(originalSchema)) {
            StringwrappedInputSchema= wrapSchemaInInputParameter(originalSchema);
            builder.inputSchema(wrappedInputSchema);
        }
        
        ToolDefinitiontoolDefinition= builder.build();
        AgentToolExecutorexecutor=newAgentToolExecutor(agent);
        
        return MethodToolCallback.builder()
                .toolDefinition(toolDefinition)
                .toolMethod(method)
                .toolObject(executor)
                .toolCallResultConverter(CONVERTER)
                .build();
    }
}
```

### 5.4 AgentToolExecutor核心逻辑

AgentToolExecutor是执行子Agent的核心，我们将之前的invoe的调用方式改为 stream() 的调用方式

```
Flux<NodeOutput> outputFlux = graph.stream(Map.of("messages", messagesToAdd));
```

实现了流式输出的转发：

```
public static class AgentToolExecutor {
    privatefinal ReactAgent agent;

    publicAgentToolExecutor(ReactAgent agent) {
        this.agent = agent;
    }

    public AssistantMessage executeAgent(String input, ToolContext toolContext) {
        StringactualInput= extractInputValue(input);
        
        // 构建消息列表
        List<Message> messagesToAdd = newArrayList<>();
        if (StringUtils.hasLength(agent.instruction())) {
            messagesToAdd.add(AgentInstructionMessage.builder()
                    .text(agent.instruction()).build());
        }
        messagesToAdd.add(newUserMessage(actualInput));
        
        // 获取Graph
        vargraph= agent.getAndCompileGraph();
        
        // 从ToolContext获取sessionId
        finalStringsessionId= (String) toolContext.getContext().get("sessionId");

        // 流式调用子Agent
        Flux<NodeOutput> outputFlux = graph.stream(Map.of("messages", messagesToAdd));
        
        // 处理流式输出，实时转发给前端
        NodeOutputlastOutput= outputFlux.map(nodeOutput -> {
            Stringnode= nodeOutput.node();
            StringagentName= agent.name();

            log.info("【子Agent执行】收到节点输出 - node: {}, agent: {}", node, agentName);

            // 构建响应数据
            Map<String, Object> data = newHashMap<>();
            data.put("node", node);
            data.put("agent", agentName);

            StringBuildercontentBuilder=newStringBuilder();
            booleanhasContent=false;

            // 提取内容
            if (nodeOutput instanceof StreamingOutput<?> streamingOutput) {
                Messagemessage= streamingOutput.message();
                
                if (message instanceof AssistantMessage assistantMessage) {
                    if (assistantMessage.hasToolCalls()) {
                        // 工具调用
                        data.put("contentType", "tool_execute");
                        vartoolCalls= assistantMessage.getToolCalls().get(0);
                        contentBuilder.append("\n[工具执行]：")
                                .append(toolCalls.name());
                        hasContent = true;
                    } else {
                        // 流式消息内容
                        Stringtext= streamingOutput.message().getText();
                        if (text != null && !text.trim().isEmpty()) {
                            contentBuilder.append(text);
                            hasContent = true;
                            data.put("contentType", "delta");
                        }
                    }
                } elseif (message instanceof ToolResponseMessage) {
                    data.put("contentType", "tool_complete");
                    contentBuilder.append("[工具执行完成]");
                    hasContent = true;
                }
            }

            data.put("content", contentBuilder.toString());
            data.put("hasContent", hasContent);
            data.put("stage", determineStage(agentName));

            // 关键：通过PlanContext获取SSE连接，实时推送数据
            FluxSinkcontextSink= PlanContext.getSessionEmitter(sessionId);
            if (contextSink != null) {
                Stringjson= toJsonStr(data);
                contextSink.next(ServerSentEvent.<String>builder()
                        .event("message")
                        .data(json)
                        .build());
            }
            
            return nodeOutput;
        }).blockLast();  // 等待子Agent执行完成

        // 提取最终结果返回给监督者Agent
        OverAllStatelastState= lastOutput != null ? lastOutput.state() : null;
        Optional<List> messages = Optional.ofNullable(lastState)
                .flatMap(state -> state.value("messages", List.class));
        
        if (messages.isPresent()) {
            List<Message> messageList = messages.get();
            return (AssistantMessage) messageList.get(messageList.size() - 1);
        }
        
        thrownewRuntimeException("Failed to execute agent tool");
    }

    /**
     * 根据Agent名称确定阶段标识
     */
    private String determineStage(String agentName) {
        if (agentName != null) {
            if (agentName.contains("attraction")) {
                return"attraction";
            } elseif (agentName.contains("hotel")) {
                return"hotel";
            } elseif (agentName.contains("transport")) {
                return"transport";
            } elseif (agentName.contains("supervisor")) {
                return"supervisor";
            }
        }
        return"unknown";
    }
}
```

### 5.5 Controller层实现

Controller负责建立SSE连接，并将连接存储到PlanContext：

```
@RestController
@RequestMapping("/api/travel")
publicclassTravelPlanController {
    privatefinal TravelSupervisorPlanAgent travelSupervisorPlanAgent;

    @GetMapping(value = "/plan", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> planTravelStream(
            @RequestParam String destination,
            @RequestParam(required = false, defaultValue = "3") Integer days,
            @RequestParam(required = false) String budget,
            @RequestParam(required = false) String preferences) {

        StringsessionId= UUID.randomUUID().toString();
        
        AgentsupervisorAgent= travelSupervisorPlanAgent.agent(sessionId);
        
        // 构建用户请求
        Stringprompt= buildPrompt(destination, days, budget, preferences);
        
        RunnableConfigconfig= RunnableConfig.builder()
                .threadId("1")
                .addMetadata("sessionId", sessionId)
                .build();

        // 获取监督者Agent的流式输出
        Flux<NodeOutput> agentStream = supervisorAgent.stream(prompt, config);

        return Flux.create(sink -> {
            // 关键：将SSE连接存储到PlanContext
            PlanContext.set(sink);
            PlanContext.setSessionEmitter(sessionId, sink);
            
            // 异步处理Agent流
            CompletableFuture.runAsync(() -> {
                agentStream.subscribe(
                    nodeOutput -> {
                        // 处理监督者Agent的输出...
                    },
                    error -> {
                        log.error("流式规划过程中发生错误", error);
                        sink.next(buildErrorEvent(error));
                    },
                    () -> {
                        // 完成时清理
                        PlanContext.remove();
                        PlanContext.removeSessionEmitter(sessionId);
                    }
                );
            });
        });
    }
}
```

## **6. 数据流转详解** 

### 6.1 完整的数据流

![图片](https://mmbiz.qpic.cn/mmbiz_png/7eiapngsNZCA5vHRVto5szWa7w8BjiaTJTQqIibrmlaZOZ9PnMzQsjelLDxUSOF8BrjtN8N8k0wEs9eJam53e6IlFCBRndqlUOOMQPFgxHM7eY/640?wx_fmt=png&from=appmsg&tp=wxpic&wxfrom=5&wx_lazy=1&watermark=1#imgIndex=5)

```
用户输入
    ↓
Controller建立SSE连接
    ↓
PlanContext存储SSE连接 (sessionId → FluxSink)
    ↓
监督者Agent开始执行
    ↓
调用子Agent Tool
    ↓
自定义AgentToolExecutor执行子Agent
    ├─ 子Agent流式输出NodeOutput
    ├─ 从PlanContext获取SSE连接
    └─ 实时推送到前端
    ↓
前端接收SSE事件，更新UI
```

### 6.2 数据格式

前端接收的SSE数据格式：

```
{
  "node":"attraction_agent",
"agent":"attraction_agent",
"stage":"attraction",
"contentType":"delta",
"content":"推荐景点：黄鹤楼、东湖...",
"hasContent":true
}
```

| 字段          | 说明                                              |
| ------------- | ------------------------------------------------- |
| `node`        | 当前执行的节点名称                                |
| `agent`       | 当前执行的Agent名称                               |
| `stage`       | 阶段标识（attraction/hotel/transport/supervisor） |
| `contentType` | 内容类型（delta/tool_execute/tool_complete）      |
| `content`     | 具体内容                                          |
| `hasContent`  | 是否包含有效内容                                  |

## **7. 前端实现** 

前端使用EventSource API接收SSE数据，实现实时更新：

```
let eventSource = null;

functionstartPlanning() {
    const destination = document.getElementById('destination').value.trim();
    const days = document.getElementById('days').value || '3';
    
    // 建立SSE连接
    const url = `/api/travel/plan?destination=${encodeURIComponent(destination)}&days=${days}`;
    eventSource = newEventSource(url);

    // 监听消息事件
    eventSource.addEventListener('message', function(event) {
        const data = JSON.parse(event.data);
        
        if (data.stage && data.hasContent) {
            // 根据阶段更新对应的面板
            updateStageContent(data.stage, data.content);
            activatePanel(data.stage);
        }
    });
}

// 内容累加器
const contentAccumulators = {
    attraction: '',
    hotel: '',
    transport: '',
    supervisor: ''
};

functionupdateStageContent(stage, content) {
    // 累加内容
    contentAccumulators[stage] += content;
    
    // 更新对应面板
    const contentElement = document.getElementById(stage + 'Content');
    if (contentElement) {
        contentElement.innerHTML = marked.parse(contentAccumulators[stage]);
        contentElement.scrollTop = contentElement.scrollHeight;
    }
}
```

实际效果演示：(启动项目后，访问 http://localhost:8080)

![图片](https://mmbiz.qpic.cn/sz_mmbiz_png/7eiapngsNZCClILJVqLO516cOfNuia8V1hzmmGIVGMdXQMiaBtv2UFJnfSkW6xfMy70reFyCpdbIibbn8UZKtq73tYl5k3p52ibf7PfMfPdXlT2w/640?wx_fmt=png&from=appmsg&tp=wxpic&wxfrom=5&wx_lazy=1&watermark=1#imgIndex=6)





## **8. 总结** 

### 8.1 方案对比

| 对比项     | 官方AgentTool    | 自定义AgentTool |
| ---------- | ---------------- | --------------- |
| 调用方式   | 同步阻塞         | 流式调用        |
| 输出支持   | 仅最终结果       | 实时流式输出    |
| 前端体验   | 等待完成         | 实时进度展示    |
| 实现复杂度 | 简单             | 中等            |
| 适用场景   | 对实时性要求不高 | 需要实时反馈    |

### 8.2 核心要点

![图片](https://mmbiz.qpic.cn/mmbiz_png/7eiapngsNZCC2MJ0EEC5pq1c1ibAESkkQeyZ5r3ic7w3GpvERnDx1CB5ib3kvArLRYibfU9fbXz42ROtd3PcI3gmVElyDQ7THSb0Zgyia8eUL2QnU/640?wx_fmt=png&from=appmsg&tp=wxpic&wxfrom=5&wx_lazy=1&watermark=1#imgIndex=7)

1. Sub Agent机制

   ：通过将ReactAgent作为Tool注入监督者Agent，实现多智能体协作

2. 自定义AgentTool

   ：解决官方方案同步阻塞的问题，支持流式输出

3. PlanContext

   ：管理SSE连接，实现子Agent输出到前端的流式转发

4. 实时反馈

   ：提升用户体验，让用户了解AI的思考和执行过程

### 8.3 扩展方向

- 支持动态加载专家Agent
- 实现专家Agent的并行执行
- 添加Agent执行超时控制
- 支持Agent执行结果缓存
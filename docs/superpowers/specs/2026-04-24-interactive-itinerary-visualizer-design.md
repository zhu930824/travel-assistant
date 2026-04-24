# 交互式行程地图 + 时间轴视图

## 概述

将旅游规划的纯文本结果转化为可视化地图和时间轴，提升用户对行程的直观理解。

**核心问题**：当前规划结果以 markdown 文本呈现，用户难以直观把握行程全貌、景点位置关系、时间安排。

**解决方案**：
1. 交互式地图 - 在地图上展示景点、酒店、餐厅位置和路线
2. 日程时间轴 - 按天展示行程，支持查看详情
3. 路线可视化 - 显示交通路线、距离、预计时间
4. 实时信息卡片 - 天气预报、景点信息

---

## 一、数据结构改造

### 1.1 数据库变更

`plan_record` 表新增字段：

```sql
ALTER TABLE plan_record ADD COLUMN plan_data JSON COMMENT '结构化行程数据';
```

### 1.2 行程数据结构

```json
{
  "sessionId": "xxx",
  "destination": "北京",
  "days": 3,
  "budget": "中等",
  "itinerary": [
    {
      "day": 1,
      "date": "2026-05-01",
      "summary": "故宫深度游",
      "weather": {
        "condition": "晴",
        "tempMin": 18,
        "tempMax": 25,
        "windDirection": "东南风",
        "windPower": "3级"
      },
      "activities": [
        {
          "id": "act_001",
          "type": "ATTRACTION",
          "name": "故宫博物院",
          "location": { "lng": 116.397, "lat": 39.909 },
          "address": "北京市东城区景山前街4号",
          "startTime": "09:00",
          "endTime": "12:00",
          "description": "中国明清两代皇家宫殿",
          "tips": "建议从午门进入",
          "cost": 60,
          "duration": 180,
          "metadata": {
            "ticketPrice": "60元",
            "openHours": "08:30-17:00",
            "rating": "4.9"
          }
        },
        {
          "id": "act_002",
          "type": "TRANSPORT",
          "name": "步行至景山公园",
          "from": { "lng": 116.397, "lat": 39.909 },
          "to": { "lng": 116.396, "lat": 39.920 },
          "duration": 10,
          "distance": 500,
          "transportMode": "WALK",
          "cost": 0
        }
      ]
    }
  ],
  "summary": {
    "totalCost": 3200,
    "currency": "CNY",
    "highlights": ["故宫", "颐和园", "长城"]
  }
}
```

### 1.3 活动类型定义

| Type | 图标 | 颜色 | 说明 |
|------|------|------|------|
| ATTRACTION | 🏛️ | 紫色 | 景点游览 |
| HOTEL | 🏨 | 粉色 | 住宿 |
| RESTAURANT | 🍽️ | 橙色 | 餐厅 |
| TRANSPORT | 🚶 | 蓝色 | 交通移动 |

### 1.4 交通方式定义

| transportMode | 说明 | 路线样式 |
|---------------|------|----------|
| WALK | 步行 | 蓝色虚线 |
| BUS | 公交 | 绿色实线 |
| SUBWAY | 地铁 | 绿色实线 |
| TAXI | 打车 | 紫色实线 |
| DRIVE | 自驾 | 紫色实线 |

---

## 二、交互式地图

### 2.1 技术选型

- **地图SDK**：高德地图 JS API 2.0
- **原因**：与后端高德 API 生态一致，免费额度充足，文档完善

### 2.2 功能列表

| 功能 | 描述 |
|------|------|
| 多类型标记 | 景点紫/酒店粉/餐厅橙/交通蓝，不同图标 |
| 点击弹窗 | 显示名称、评分、价格、地址、天气 |
| 路线连线 | 同天彩色实线，交通虚线，不同天不同颜色 |
| 天筛选器 | Tab 切换全部/Day1/Day2/Day3 |
| 自适应视野 | 自动 fitBounds 到可见标记 |
| 双向联动 | 点击地图↔高亮时间轴卡片 |

### 2.3 地图配色方案

```
Day1: #8b5cf6 (紫色)
Day2: #10b981 (绿色)
Day3: #3b82f6 (蓝色)
Day4: #f59e0b (橙色)
Day5: #ec4899 (粉色)

景点标记: #8b5cf6
酒店标记: #ec4899
餐厅标记: #f59e0b
交通点: #3b82f6
```

### 2.4 组件结构

```
MapView.vue
├── AMapContainer     # 地图容器
├── DayFilter         # 天筛选 Tab
├── MapLegend         # 图例说明
└── InfoPopup         # 点击标记弹窗
```

---

## 三、日程时间轴

### 3.1 功能列表

| 功能 | 描述 |
|------|------|
| 垂直时间轴 | 左侧时间刻度，活动卡片按时间排列 |
| 类型图标 | 景点/酒店/餐厅/交通，图标+颜色区分 |
| 详情展开 | 地址、费用、小贴士、评分 |
| 天切换 | Tab 切换 Day1/Day2/Day3 |
| 每日汇总 | 日期、天气、预算 |
| 地图联动 | 点击卡片→地图居中并高亮 |

### 3.2 组件结构

```
ItineraryTimeline.vue
├── DayTabs           # 天切换 Tab
├── DayCard           # 单日卡片
│   ├── DayHeader     # 日期+天气+概述
│   ├── ActivityItem[] # 活动条目
│   └── DaySummary    # 当日预算汇总
└── TotalSummary      # 总预算汇总
```

### 3.3 活动卡片设计

```
┌─────────────────────────────────────┐
│ 🏛️ 故宫博物院                        │
│ 09:00 - 12:00 · 预计 3 小时           │
│ ─────────────────────────────────── │
│ 📍 北京市东城区景山前街4号            │
│ 💰 门票: 60元                        │
│ ⭐ 4.9 分                            │
│ 💡 建议: 从午门进入，神武门出         │
│ ─────────────────────────────────── │
│ [导航前往] [查看详情]                 │
└─────────────────────────────────────┘
         ↓ 步行 10分钟
```

---

## 四、路线可视化与实时信息

### 4.1 路线样式

| 交通方式 | 路线样式 | 宽度 | 动画 |
|----------|----------|------|------|
| 步行 | 蓝色虚线 | 3px | 小人移动动画 |
| 公交/地铁 | 绿色实线 | 4px | 无 |
| 打车/自驾 | 紫色实线 | 4px | 无 |

### 4.2 路线详情面板

点击交通路段显示：
- 起点 → 终点名称
- 距离（米/公里）
- 预计时间（分钟）
- 费用估算（如有）
- 公交：显示换乘方案、途经站数
- 导航按钮：跳转高德地图 App/Web

### 4.3 实时信息

| 信息类型 | 数据来源 | 显示位置 |
|----------|----------|----------|
| 天气预报 | GaodeMapTool.queryWeather | 每日卡片顶部 |
| 景点信息 | GaodeMapTool.searchPoi | 地图弹窗/卡片详情 |
| 路线规划 | GaodeMapTool.*Route | 点击交通路段 |

### 4.4 智能提醒

- 时间提醒：距离下一活动开始时间
- 天气提醒：出行日有雨提醒带伞
- 预算提醒：总费用超支警告

---

## 五、技术架构

### 5.1 后端改造

**修改文件**：
- `TravelSupervisorPlanAgent.java` - 修改 prompt 要求输出 JSON
- `PlanRecord.java` - 新增 planData 字段
- `TravelPlanController.java` - 新增获取结构化数据接口
- `PlanRecordService.java` - 支持 planData 存取

**新增接口**：
```
GET /api/travel/plan/{id}/data
Response: PlanDataResponse (结构化行程数据)

GET /api/travel/plan/{id}/weather
Response: WeatherResponse (目的地天气预报)
```

### 5.2 前端改造

**新增组件**：
- `MapView.vue` - 地图组件
- `ItineraryTimeline.vue` - 时间轴组件
- `ActivityCard.vue` - 活动卡片
- `TransportSegment.vue` - 交通段
- `DayFilter.vue` - 天筛选器
- `InfoPopup.vue` - 信息弹窗

**改造组件**：
- `PlanningPanel.vue` - 整合地图+时间轴

**新增工具**：
- `useMap.js` - 地图逻辑 composable
- `formatters.js` - 数据格式化工具

### 5.3 布局设计

**桌面端**：
```
┌────────────────────────────────────────────────────┐
│                    Header                           │
├─────────────────────────────┬──────────────────────┤
│                             │                      │
│      地图 (60%)              │    时间轴 (40%)       │
│                             │                      │
│                             │                      │
│                             │                      │
├─────────────────────────────┴──────────────────────┤
│                    预算汇总                          │
└────────────────────────────────────────────────────┘
```

**移动端**：
```
┌──────────────────────┐
│       Header         │
├──────────────────────┤
│   天筛选 Tab          │
├──────────────────────┤
│                      │
│      地图 (40vh)      │
│                      │
├──────────────────────┤
│                      │
│     时间轴 (60vh)     │
│                      │
├──────────────────────┤
│     预算汇总          │
└──────────────────────┘
```

---

## 六、数据库迁移

```sql
-- V{version}__add_plan_data_column.sql

ALTER TABLE plan_record 
ADD COLUMN plan_data JSON COMMENT '结构化行程数据' AFTER plan_content;

-- 为已有记录补充 plan_data（可选，需要重新生成）
-- UPDATE plan_record SET plan_data = '{}' WHERE plan_data IS NULL;
```

---

## 七、文件结构

```
后端：
├── entity/domain/PlanRecord.java          # 新增 planData 字段
├── entity/dto/response/PlanDataResponse.java  # 新增 DTO
├── controller/TravelPlanController.java   # 新增接口
├── agent/supervisor/TravelSupervisorPlanAgent.java  # 修改 prompt
└── resources/db/migration/V*.sql          # 数据库迁移

前端：
├── components/planning/
│   ├── MapView.vue              # 新增 - 地图
│   ├── ItineraryTimeline.vue    # 新增 - 时间轴
│   ├── ActivityCard.vue         # 新增 - 活动卡片
│   ├── TransportSegment.vue     # 新增 - 交通段
│   ├── DayFilter.vue            # 新增 - 天筛选
│   ├── InfoPopup.vue            # 新增 - 信息弹窗
│   └── PlanningPanel.vue        # 改造 - 整合新组件
├── composables/
│   └── useMap.js                # 新增 - 地图逻辑
└── utils/
    └── formatters.js            # 新增 - 格式化工具
```

---

## 八、实现优先级

| 优先级 | 模块 | 预计工时 |
|--------|------|----------|
| P0 | 数据结构改造 + 后端接口 | 2天 |
| P1 | 交互式地图基础功能 | 3天 |
| P1 | 日程时间轴 | 2天 |
| P2 | 地图-时间轴联动 | 1天 |
| P2 | 路线可视化 | 2天 |
| P3 | 实时信息集成 | 2天 |
| P3 | 智能提醒 | 1天 |

**总计：约 13 天**

---

## 九、验收标准

1. 地图正确显示所有景点、酒店、餐厅标记
2. 点击标记显示信息弹窗
3. 路线按交通方式正确渲染
4. 天筛选器切换正常，地图视野自适应
5. 时间轴按天展示，活动卡片信息完整
6. 点击卡片与地图标记联动高亮
7. 天气信息正确显示
8. 导航按钮可跳转高德地图
9. 移动端布局适配正常

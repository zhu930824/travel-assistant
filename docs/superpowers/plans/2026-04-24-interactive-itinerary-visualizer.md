# 交互式行程地图 + 时间轴视图 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将旅游规划的纯文本结果转化为可视化地图和时间轴，提升用户对行程的直观理解。

**Architecture:** 后端新增结构化数据字段存储行程详情，前端使用高德地图 JS API 2.0 实现交互式地图，配合 Vue 3 组件化的时间轴展示，实现地图与时间轴双向联动。

**Tech Stack:** Spring Boot, MyBatis, Flyway, Vue 3, Composition API, 高德地图 JS API 2.0, marked

---

## 文件结构

### 后端新增/修改
```
backend/src/main/java/cn/sdh/travel/
├── entity/domain/PlanRecord.java          # 新增 planData 字段
├── entity/dto/response/PlanDataResponse.java  # 新增 DTO
├── mapper/PlanRecordMapper.java           # 新增方法
├── service/PlanRecordService.java         # 新增接口方法
├── service/impl/PlanRecordServiceImpl.java    # 实现新方法
├── controller/TravelPlanController.java   # 新增 API 端点
└── resources/db/migration/V7__Add_plan_data_column.sql  # 数据库迁移
```

### 前端新增/修改
```
fronted/src/
├── components/planning/
│   ├── MapView.vue              # 新增 - 地图组件
│   ├── ItineraryTimeline.vue    # 新增 - 时间轴组件
│   ├── ActivityCard.vue         # 新增 - 活动卡片
│   ├── TransportSegment.vue     # 新增 - 交通段
│   └── DayFilter.vue            # 新增 - 天筛选器
├── composables/
│   └── useMap.js                # 新增 - 地图逻辑
├── utils/
│   └── planFormatters.js        # 新增 - 数据格式化
└── views/Plan.vue               # 改造 - 整合新组件
```

---

## Phase 1: 后端数据结构

### Task 1: 数据库迁移 - 添加 plan_data 字段

**Files:**
- Create: `backend/src/main/resources/db/migration/V7__Add_plan_data_column.sql`

- [ ] **Step 1: 创建数据库迁移文件**

```sql
-- V7__Add_plan_data_column.sql

ALTER TABLE plan_record 
ADD COLUMN plan_data JSON COMMENT '结构化行程数据' AFTER plan_content;
```

- [ ] **Step 2: 验证迁移文件语法正确**

运行后端应用启动，Flyway 会自动执行迁移。检查日志确认迁移成功。

- [ ] **Step 3: 提交**

```bash
git add backend/src/main/resources/db/migration/V7__Add_plan_data_column.sql
git commit -m "feat(db): add plan_data column for structured itinerary data"
```

---

### Task 2: 创建 PlanDataResponse DTO

**Files:**
- Create: `backend/src/main/java/cn/sdh/travel/entity/dto/response/PlanDataResponse.java`

- [ ] **Step 1: 创建 DTO 类**

```java
package cn.sdh.travel.entity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 结构化行程数据响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanDataResponse {

    private String sessionId;
    private String destination;
    private Integer days;
    private String budget;
    private List<DayItinerary> itinerary;
    private PlanSummary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayItinerary {
        private Integer day;
        private String date;
        private String summary;
        private WeatherInfo weather;
        private List<Activity> activities;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeatherInfo {
        private String condition;
        private Integer tempMin;
        private Integer tempMax;
        private String windDirection;
        private String windPower;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Activity {
        private String id;
        private String type; // ATTRACTION, HOTEL, RESTAURANT, TRANSPORT
        private String name;
        private Location location;
        private String address;
        private String startTime;
        private String endTime;
        private String description;
        private String tips;
        private Integer cost;
        private Integer duration;
        private ActivityMetadata metadata;

        // For TRANSPORT type
        private Location from;
        private Location to;
        private String transportMode; // WALK, BUS, SUBWAY, TAXI, DRIVE
        private Integer distance;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        private Double lng;
        private Double lat;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityMetadata {
        private String ticketPrice;
        private String openHours;
        private String rating;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanSummary {
        private Integer totalCost;
        private String currency;
        private List<String> highlights;
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
cd backend && mvn compile -q
```

- [ ] **Step 3: 提交**

```bash
git add backend/src/main/java/cn/sdh/travel/entity/dto/response/PlanDataResponse.java
git commit -m "feat(dto): add PlanDataResponse for structured itinerary"
```

---

### Task 3: 更新 PlanRecord 实体

**Files:**
- Modify: `backend/src/main/java/cn/sdh/travel/entity/domain/PlanRecord.java`

- [ ] **Step 1: 添加 planData 字段**

在 PlanRecord.java 中添加字段：

```java
// 在现有字段后添加
@TableField(typeHandler = org.apache.ibatis.type.JsonTypeHandler.class)
private String planData;
```

- [ ] **Step 2: 验证编译通过**

```bash
cd backend && mvn compile -q
```

- [ ] **Step 3: 提交**

```bash
git add backend/src/main/java/cn/sdh/travel/entity/domain/PlanRecord.java
git commit -m "feat(entity): add planData field to PlanRecord"
```

---

### Task 4: 扩展 PlanRecordMapper

**Files:**
- Modify: `backend/src/main/java/cn/sdh/travel/mapper/PlanRecordMapper.java`

- [ ] **Step 1: 添加更新 planData 的方法**

```java
// 在 PlanRecordMapper.java 中添加

@Update("UPDATE plan_record SET plan_data = #{planData} WHERE id = #{id}")
int updatePlanData(@Param("id") Long id, @Param("planData") String planData);

@Select("SELECT plan_data FROM plan_record WHERE id = #{id}")
String getPlanData(@Param("id") Long id);
```

需要添加导入：

```java
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Select;
```

- [ ] **Step 2: 验证编译**

```bash
cd backend && mvn compile -q
```

- [ ] **Step 3: 提交**

```bash
git add backend/src/main/java/cn/sdh/travel/mapper/PlanRecordMapper.java
git commit -m "feat(mapper): add planData update and query methods"
```

---

### Task 5: 扩展 PlanRecordService

**Files:**
- Modify: `backend/src/main/java/cn/sdh/travel/service/PlanRecordService.java`
- Modify: `backend/src/main/java/cn/sdh/travel/service/impl/PlanRecordServiceImpl.java`

- [ ] **Step 1: 在接口中添加方法声明**

在 PlanRecordService.java 中添加：

```java
/**
 * 更新结构化行程数据
 */
void updatePlanData(Long id, String planData);

/**
 * 获取结构化行程数据
 */
String getPlanData(Long id);
```

- [ ] **Step 2: 在实现类中实现方法**

在 PlanRecordServiceImpl.java 中添加：

```java
@Override
public void updatePlanData(Long id, String planData) {
    planRecordMapper.updatePlanData(id, planData);
}

@Override
public String getPlanData(Long id) {
    return planRecordMapper.getPlanData(id);
}
```

- [ ] **Step 3: 验证编译**

```bash
cd backend && mvn compile -q
```

- [ ] **Step 4: 提交**

```bash
git add backend/src/main/java/cn/sdh/travel/service/PlanRecordService.java \
        backend/src/main/java/cn/sdh/travel/service/impl/PlanRecordServiceImpl.java
git commit -m "feat(service): add planData methods to PlanRecordService"
```

---

### Task 6: 新增 API 端点获取结构化数据

**Files:**
- Modify: `backend/src/main/java/cn/sdh/travel/controller/TravelPlanController.java`

- [ ] **Step 1: 添加获取结构化数据的端点**

在 TravelPlanController.java 中添加：

```java
@GetMapping("/plan/{id}/data")
public Result<PlanDataResponse> getPlanData(@PathVariable Long id) {
    log.info("获取结构化行程数据: recordId={}", id);
    
    String planDataJson = planRecordService.getPlanData(id);
    if (planDataJson == null || planDataJson.isEmpty()) {
        return Result.error("行程数据不存在");
    }
    
    try {
        PlanDataResponse response = JSON.parseObject(planDataJson, PlanDataResponse.class);
        return Result.success(response);
    } catch (Exception e) {
        log.error("解析行程数据失败", e);
        return Result.error("行程数据格式错误");
    }
}
```

添加必要的导入：

```java
import cn.sdh.travel.entity.dto.response.PlanDataResponse;
```

- [ ] **Step 2: 验证编译**

```bash
cd backend && mvn compile -q
```

- [ ] **Step 3: 提交**

```bash
git add backend/src/main/java/cn/sdh/travel/controller/TravelPlanController.java
git commit -m "feat(api): add endpoint to get structured plan data"
```

---

### Task 7: 添加天气查询 API 端点

**Files:**
- Modify: `backend/src/main/java/cn/sdh/travel/controller/TravelPlanController.java`

- [ ] **Step 1: 添加天气查询端点**

```java
@Autowired
private GaodeMapService gaodeMapService;

@GetMapping("/plan/{id}/weather")
public Result<Map<String, Object>> getWeather(
        @PathVariable Long id,
        @RequestParam(required = false, defaultValue = "all") String extensions) {
    log.info("获取天气信息: recordId={}, extensions={}", id, extensions);
    
    // 获取目的地
    PlanRecord record = planRecordService.getById(id);
    if (record == null) {
        return Result.error("行程记录不存在");
    }
    
    String destination = record.getDestination();
    try {
        Map<String, Object> weather = gaodeMapService.queryWeather(destination, extensions);
        return Result.success(weather);
    } catch (Exception e) {
        log.error("查询天气失败", e);
        return Result.error("天气查询失败");
    }
}
```

添加必要的导入：

```java
import cn.sdh.travel.entity.domain.PlanRecord;
import cn.sdh.travel.service.GaodeMapService;
import java.util.Map;
```

- [ ] **Step 2: 验证编译**

```bash
cd backend && mvn compile -q
```

- [ ] **Step 3: 提交**

```bash
git add backend/src/main/java/cn/sdh/travel/controller/TravelPlanController.java
git commit -m "feat(api): add weather query endpoint for plan"
```

---

## Phase 2: 前端工具函数

### Task 8: 创建数据格式化工具

**Files:**
- Create: `fronted/src/utils/planFormatters.js`

- [ ] **Step 1: 创建格式化工具函数**

```javascript
/**
 * 行程数据格式化工具
 */

// 活动类型配置
export const ACTIVITY_TYPES = {
  ATTRACTION: { icon: '🏛️', color: '#8b5cf6', label: '景点' },
  HOTEL: { icon: '🏨', color: '#ec4899', label: '住宿' },
  RESTAURANT: { icon: '🍽️', color: '#f59e0b', label: '餐厅' },
  TRANSPORT: { icon: '🚶', color: '#3b82f6', label: '交通' }
}

// 交通方式配置
export const TRANSPORT_MODES = {
  WALK: { icon: '🚶', label: '步行', lineStyle: 'dashed', color: '#3b82f6' },
  BUS: { icon: '🚌', label: '公交', lineStyle: 'solid', color: '#10b981' },
  SUBWAY: { icon: '🚇', label: '地铁', lineStyle: 'solid', color: '#10b981' },
  TAXI: { icon: '🚕', label: '打车', lineStyle: 'solid', color: '#8b5cf6' },
  DRIVE: { icon: '🚗', label: '自驾', lineStyle: 'solid', color: '#8b5cf6' }
}

// 天配色方案
export const DAY_COLORS = [
  '#8b5cf6', // Day1 紫色
  '#10b981', // Day2 绿色
  '#3b82f6', // Day3 蓝色
  '#f59e0b', // Day4 橙色
  '#ec4899', // Day5 粉色
]

/**
 * 获取活动类型配置
 */
export function getActivityTypeConfig(type) {
  return ACTIVITY_TYPES[type] || ACTIVITY_TYPES.ATTRACTION
}

/**
 * 获取交通方式配置
 */
export function getTransportModeConfig(mode) {
  return TRANSPORT_MODES[mode] || TRANSPORT_MODES.WALK
}

/**
 * 获取天颜色
 */
export function getDayColor(day) {
  return DAY_COLORS[(day - 1) % DAY_COLORS.length]
}

/**
 * 格式化时间（分钟转小时分钟）
 */
export function formatDuration(minutes) {
  if (!minutes) return ''
  if (minutes < 60) return `${minutes}分钟`
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return mins > 0 ? `${hours}小时${mins}分钟` : `${hours}小时`
}

/**
 * 格式化距离（米转公里）
 */
export function formatDistance(meters) {
  if (!meters) return ''
  if (meters < 1000) return `${meters}米`
  return `${(meters / 1000).toFixed(1)}公里`
}

/**
 * 格式化费用
 */
export function formatCost(cost, currency = 'CNY') {
  if (!cost && cost !== 0) return '未知'
  return `¥${cost}`
}

/**
 * 格式化日期
 */
export function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const weekDays = ['日', '一', '二', '三', '四', '五', '六']
  return `${date.getMonth() + 1}月${date.getDate()}日 周${weekDays[date.getDay()]}`
}

/**
 * 格式化时间段
 */
export function formatTimeRange(startTime, endTime) {
  if (!startTime && !endTime) return ''
  if (!startTime) return `至 ${endTime}`
  if (!endTime) return `${startTime} 起`
  return `${startTime} - ${endTime}`
}

/**
 * 从行程中提取所有标记点
 */
export function extractMarkers(itinerary) {
  if (!itinerary) return []
  
  const markers = []
  itinerary.forEach(day => {
    day.activities?.forEach(activity => {
      if (activity.location && activity.type !== 'TRANSPORT') {
        markers.push({
          id: activity.id,
          day: day.day,
          ...activity
        })
      }
    })
  })
  return markers
}

/**
 * 从行程中提取所有路线
 */
export function extractRoutes(itinerary) {
  if (!itinerary) return []
  
  const routes = []
  itinerary.forEach(day => {
    day.activities?.forEach(activity => {
      if (activity.type === 'TRANSPORT' && activity.from && activity.to) {
        routes.push({
          id: activity.id,
          day: day.day,
          ...activity
        })
      }
    })
  })
  return routes
}

/**
 * 计算行程边界
 */
export function calculateBounds(markers) {
  if (!markers || markers.length === 0) return null
  
  let minLng = Infinity, maxLng = -Infinity
  let minLat = Infinity, maxLat = -Infinity
  
  markers.forEach(marker => {
    if (marker.location) {
      minLng = Math.min(minLng, marker.location.lng)
      maxLng = Math.max(maxLng, marker.location.lng)
      minLat = Math.min(minLat, marker.location.lat)
      maxLat = Math.max(maxLat, marker.location.lat)
    }
  })
  
  return {
    southwest: [minLng, minLat],
    northeast: [maxLng, maxLat]
  }
}
```

- [ ] **Step 2: 提交**

```bash
git add fronted/src/utils/planFormatters.js
git commit -m "feat(utils): add plan formatting utilities"
```

---

### Task 9: 创建 useMap composable

**Files:**
- Create: `fronted/src/composables/useMap.js`

- [ ] **Step 1: 创建地图逻辑 composable**

```javascript
import { ref, onMounted, onUnmounted, watch } from 'vue'
import {
  extractMarkers,
  extractRoutes,
  calculateBounds,
  getActivityTypeConfig,
  getTransportModeConfig,
  getDayColor
} from '@/utils/planFormatters'

// 高德地图 API Key
const AMAP_KEY = '6853e704612bd320c0b452a5be5c1efc'

/**
 * 地图逻辑 composable
 */
export function useMap(containerRef, itinerary) {
  const map = ref(null)
  const markers = ref([])
  const polylines = ref([])
  const infoWindow = ref(null)
  const selectedMarker = ref(null)
  const currentDay = ref(null) // null 表示全部

  /**
   * 初始化地图
   */
  const initMap = () => {
    if (!containerRef.value) return

    // 动态加载高德地图 SDK
    if (!window.AMap) {
      const script = document.createElement('script')
      script.src = `https://webapi.amap.com/maps?v=2.0&key=${AMAP_KEY}`
      script.onload = () => {
        createMap()
      }
      document.head.appendChild(script)
    } else {
      createMap()
    }
  }

  /**
   * 创建地图实例
   */
  const createMap = () => {
    map.value = new window.AMap.Map(containerRef.value, {
      zoom: 12,
      center: [116.397, 39.909],
      mapStyle: 'amap://styles/dark',
      features: ['bg', 'road', 'building'],
      viewMode: '2D'
    })

    // 创建信息窗体
    infoWindow.value = new window.AMap.InfoWindow({
      isCustom: true,
      autoMove: true,
      offset: new window.AMap.Pixel(0, -40)
    })
  }

  /**
   * 清除所有标记和路线
   */
  const clearOverlays = () => {
    markers.value.forEach(m => m.setMap(null))
    polylines.value.forEach(p => p.setMap(null))
    markers.value = []
    polylines.value = []
  }

  /**
   * 添加标记点
   */
  const addMarkers = (markerData) => {
    if (!map.value || !markerData.length) return

    markerData.forEach(data => {
      const config = getActivityTypeConfig(data.type)
      const dayColor = getDayColor(data.day)

      // 创建自定义标记
      const marker = new window.AMap.Marker({
        position: [data.location.lng, data.location.lat],
        title: data.name,
        extData: data
      })

      // 设置自定义内容
      marker.setContent(`
        <div class="custom-marker" style="
          width: 32px;
          height: 32px;
          background: ${currentDay.value === null || currentDay.value === data.day ? dayColor : 'rgba(255,255,255,0.3)'};
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 16px;
          box-shadow: 0 2px 8px rgba(0,0,0,0.3);
          cursor: pointer;
        ">
          ${config.icon}
        </div>
      `)

      // 点击事件
      marker.on('click', () => {
        selectedMarker.value = data
        showInfoWindow(marker, data)
      })

      marker.setMap(map.value)
      markers.value.push(marker)
    })
  }

  /**
   * 添加路线
   */
  const addRoutes = (routeData) => {
    if (!map.value || !routeData.length) return

    routeData.forEach(data => {
      const config = getTransportModeConfig(data.transportMode)
      const dayColor = getDayColor(data.day)

      // 创建路线
      const polyline = new window.AMap.Polyline({
        path: [
          [data.from.lng, data.from.lat],
          [data.to.lng, data.to.lat]
        ],
        strokeColor: currentDay.value === null || currentDay.value === data.day ? dayColor : 'rgba(255,255,255,0.2)',
        strokeWeight: 4,
        strokeStyle: config.lineStyle === 'dashed' ? 'dashed' : 'solid',
        strokeDasharray: config.lineStyle === 'dashed' ? [10, 5] : null,
        extData: data
      })

      polyline.setMap(map.value)
      polylines.value.push(polyline)
    })
  }

  /**
   * 显示信息窗体
   */
  const showInfoWindow = (marker, data) => {
    const config = getActivityTypeConfig(data.type)
    
    const content = `
      <div class="info-popup" style="
        background: rgba(20, 20, 30, 0.95);
        border: 1px solid rgba(255,255,255,0.1);
        border-radius: 12px;
        padding: 16px;
        min-width: 200px;
        color: #fff;
      ">
        <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px;">
          <span style="font-size: 20px;">${config.icon}</span>
          <span style="font-weight: 600; font-size: 14px;">${data.name}</span>
        </div>
        ${data.address ? `<div style="font-size: 12px; color: rgba(255,255,255,0.6); margin-bottom: 8px;">📍 ${data.address}</div>` : ''}
        ${data.metadata?.rating ? `<div style="font-size: 12px; color: rgba(255,255,255,0.6);">⭐ ${data.metadata.rating} 分</div>` : ''}
        ${data.cost ? `<div style="font-size: 12px; color: rgba(255,255,255,0.6);">💰 ¥${data.cost}</div>` : ''}
        <div style="margin-top: 12px; display: flex; gap: 8px;">
          <button onclick="window.openNavigation('${data.location.lng},${data.location.lat}')" style="
            padding: 6px 12px;
            background: linear-gradient(135deg, #6366f1, #8b5cf6);
            border: none;
            border-radius: 6px;
            color: #fff;
            font-size: 12px;
            cursor: pointer;
          ">导航前往</button>
        </div>
      </div>
    `

    infoWindow.value.setContent(content)
    infoWindow.value.open(map.value, marker.getPosition())
  }

  /**
   * 关闭信息窗体
   */
  const closeInfoWindow = () => {
    if (infoWindow.value) {
      infoWindow.value.close()
    }
    selectedMarker.value = null
  }

  /**
   * 自适应视野
   */
  const fitBounds = (markerData) => {
    if (!map.value || !markerData.length) return

    const bounds = calculateBounds(markerData)
    if (bounds) {
      map.value.setBounds(new window.AMap.Bounds(
        bounds.southwest,
        bounds.northeast
      ))
    }
  }

  /**
   * 设置当前天
   */
  const setCurrentDay = (day) => {
    currentDay.value = day
    clearOverlays()
    renderOverlays()
  }

  /**
   * 渲染覆盖物
   */
  const renderOverlays = () => {
    if (!itinerary.value) return

    const allMarkers = extractMarkers(itinerary.value)
    const allRoutes = extractRoutes(itinerary.value)

    const filteredMarkers = currentDay.value === null
      ? allMarkers
      : allMarkers.filter(m => m.day === currentDay.value)

    const filteredRoutes = currentDay.value === null
      ? allRoutes
      : allRoutes.filter(r => r.day === currentDay.value)

    addMarkers(filteredMarkers)
    addRoutes(filteredRoutes)
    fitBounds(filteredMarkers)
  }

  /**
   * 定位到指定活动
   */
  const focusActivity = (activityId) => {
    const marker = markers.value.find(m => m.getExtData().id === activityId)
    if (marker && map.value) {
      map.value.setCenter(marker.getPosition())
      map.value.setZoom(15)
      showInfoWindow(marker, marker.getExtData())
    }
  }

  // 监听 itinerary 变化
  watch(itinerary, () => {
    if (map.value) {
      clearOverlays()
      renderOverlays()
    }
  }, { deep: true })

  // 生命周期
  onMounted(() => {
    initMap()
  })

  onUnmounted(() => {
    if (map.value) {
      map.value.destroy()
    }
  })

  return {
    map,
    markers,
    polylines,
    selectedMarker,
    currentDay,
    setCurrentDay,
    clearOverlays,
    fitBounds,
    focusActivity,
    closeInfoWindow
  }
}

// 全局导航函数
window.openNavigation = function(location) {
  const [lng, lat] = location.split(',')
  window.open(`https://uri.amap.com/marker?position=${lng},${lat}&coordinate=gaode`, '_blank')
}
```

- [ ] **Step 2: 创建 composables 目录（如果不存在）**

```bash
mkdir -p fronted/src/composables
```

- [ ] **Step 3: 提交**

```bash
git add fronted/src/composables/useMap.js
git commit -m "feat(composable): add useMap for map logic"
```

---

## Phase 3: 地图组件

### Task 10: 创建 DayFilter 组件

**Files:**
- Create: `fronted/src/components/planning/DayFilter.vue`

- [ ] **Step 1: 创建天筛选器组件**

```vue
<template>
  <div class="day-filter">
    <button
      class="filter-btn"
      :class="{ active: modelValue === null }"
      @click="$emit('update:modelValue', null)"
    >
      全部
    </button>
    <button
      v-for="day in totalDays"
      :key="day"
      class="filter-btn"
      :class="{ active: modelValue === day }"
      :style="modelValue === day ? { background: getDayColor(day) } : {}"
      @click="$emit('update:modelValue', day)"
    >
      Day{{ day }}
    </button>
  </div>
</template>

<script setup>
import { getDayColor } from '@/utils/planFormatters'

defineProps({
  modelValue: {
    type: Number,
    default: null
  },
  totalDays: {
    type: Number,
    default: 1
  }
})

defineEmits(['update:modelValue'])
</script>

<style scoped>
.day-filter {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
}

.filter-btn {
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.filter-btn.active {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-color: transparent;
  color: #fff;
}
</style>
```

- [ ] **Step 2: 提交**

```bash
git add fronted/src/components/planning/DayFilter.vue
git commit -m "feat(component): add DayFilter component"
```

---

### Task 11: 创建 ActivityCard 组件

**Files:**
- Create: `fronted/src/components/planning/ActivityCard.vue`

- [ ] **Step 1: 创建活动卡片组件**

```vue
<template>
  <div
    class="activity-card"
    :class="[activity.type.toLowerCase(), { highlighted: isHighlighted }]"
    @click="$emit('click', activity)"
  >
    <!-- 类型图标和时间 -->
    <div class="activity-header">
      <div class="activity-icon" :style="{ background: typeConfig.color }">
        {{ typeConfig.icon }}
      </div>
      <div class="activity-info">
        <h4 class="activity-name">{{ activity.name }}</h4>
        <span class="activity-time">
          {{ formatTimeRange(activity.startTime, activity.endTime) }}
          <span v-if="activity.duration" class="activity-duration">
            · {{ formatDuration(activity.duration) }}
          </span>
        </span>
      </div>
    </div>

    <!-- 详情（展开时显示） -->
    <div class="activity-details" v-if="expanded">
      <div class="detail-item" v-if="activity.address">
        <span class="detail-icon">📍</span>
        <span>{{ activity.address }}</span>
      </div>
      <div class="detail-item" v-if="activity.cost">
        <span class="detail-icon">💰</span>
        <span>{{ formatCost(activity.cost) }}</span>
      </div>
      <div class="detail-item" v-if="activity.metadata?.rating">
        <span class="detail-icon">⭐</span>
        <span>{{ activity.metadata.rating }} 分</span>
      </div>
      <div class="detail-item" v-if="activity.tips">
        <span class="detail-icon">💡</span>
        <span>{{ activity.tips }}</span>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="activity-actions" v-if="expanded">
      <button class="action-btn" @click.stop="$emit('navigate', activity)">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/>
          <path d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/>
        </svg>
        导航
      </button>
      <button class="action-btn expand-btn" @click.stop="expanded = !expanded">
        {{ expanded ? '收起' : '详情' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import {
  getActivityTypeConfig,
  formatDuration,
  formatCost,
  formatTimeRange
} from '@/utils/planFormatters'

const props = defineProps({
  activity: {
    type: Object,
    required: true
  },
  isHighlighted: {
    type: Boolean,
    default: false
  }
})

defineEmits(['click', 'navigate'])

const expanded = ref(false)

const typeConfig = computed(() => {
  return getActivityTypeConfig(props.activity.type)
})
</script>

<style scoped>
.activity-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 14px 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.activity-card:hover {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.15);
}

.activity-card.highlighted {
  border-color: #6366f1;
  box-shadow: 0 0 20px rgba(99, 102, 241, 0.2);
}

.activity-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.activity-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.activity-info {
  flex: 1;
  min-width: 0;
}

.activity-name {
  font-size: 0.95rem;
  font-weight: 600;
  color: #fff;
  margin: 0 0 4px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.activity-time {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.5);
}

.activity-duration {
  color: rgba(255, 255, 255, 0.4);
}

.activity-details {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.detail-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 8px;
}

.detail-icon {
  flex-shrink: 0;
}

.activity-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.action-btn svg {
  width: 14px;
  height: 14px;
}

.expand-btn {
  margin-left: auto;
}
</style>
```

- [ ] **Step 2: 提交**

```bash
git add fronted/src/components/planning/ActivityCard.vue
git commit -m "feat(component): add ActivityCard component"
```

---

### Task 12: 创建 TransportSegment 组件

**Files:**
- Create: `fronted/src/components/planning/TransportSegment.vue`

- [ ] **Step 1: 创建交通段组件**

```vue
<template>
  <div class="transport-segment">
    <div class="transport-line">
      <div class="transport-icon">
        {{ modeConfig.icon }}
      </div>
      <div class="transport-arrow">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M5 12h14M12 5l7 7-7 7"/>
        </svg>
      </div>
    </div>
    <div class="transport-info">
      <span class="transport-mode">{{ modeConfig.label }}</span>
      <span class="transport-duration" v-if="activity.duration">
        {{ formatDuration(activity.duration) }}
      </span>
      <span class="transport-distance" v-if="activity.distance">
        · {{ formatDistance(activity.distance) }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { getTransportModeConfig, formatDuration, formatDistance } from '@/utils/planFormatters'

const props = defineProps({
  activity: {
    type: Object,
    required: true
  }
})

const modeConfig = computed(() => {
  return getTransportModeConfig(props.activity.transportMode)
})
</script>

<style scoped>
.transport-segment {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  margin-left: 18px;
}

.transport-line {
  display: flex;
  align-items: center;
  gap: 4px;
}

.transport-icon {
  font-size: 14px;
}

.transport-arrow {
  width: 16px;
  height: 16px;
  color: rgba(255, 255, 255, 0.3);
}

.transport-arrow svg {
  width: 100%;
  height: 100%;
}

.transport-info {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.5);
}

.transport-mode {
  color: rgba(255, 255, 255, 0.6);
}

.transport-duration,
.transport-distance {
  color: rgba(255, 255, 255, 0.4);
}
</style>
```

- [ ] **Step 2: 提交**

```bash
git add fronted/src/components/planning/TransportSegment.vue
git commit -m "feat(component): add TransportSegment component"
```

---

### Task 13: 创建 MapView 组件

**Files:**
- Create: `fronted/src/components/planning/MapView.vue`

- [ ] **Step 1: 创建地图视图组件**

```vue
<template>
  <div class="map-view">
    <!-- 天筛选器 -->
    <div class="map-controls">
      <DayFilter v-model="selectedDay" :total-days="totalDays" />
    </div>

    <!-- 地图容器 -->
    <div ref="mapContainer" class="map-container"></div>

    <!-- 图例 -->
    <div class="map-legend">
      <div class="legend-item">
        <span class="legend-marker attraction"></span>
        <span>景点</span>
      </div>
      <div class="legend-item">
        <span class="legend-marker hotel"></span>
        <span>酒店</span>
      </div>
      <div class="legend-item">
        <span class="legend-marker restaurant"></span>
        <span>餐厅</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import DayFilter from './DayFilter.vue'
import { useMap } from '@/composables/useMap'

const props = defineProps({
  itinerary: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['activity-click'])

const mapContainer = ref(null)
const selectedDay = ref(null)

// 计算总天数
const totalDays = computed(() => {
  return props.itinerary?.length || 1
})

// 使用地图 composable
const { setCurrentDay, focusActivity, map } = useMap(mapContainer, computed(() => props.itinerary))

// 监听天选择变化
watch(selectedDay, (day) => {
  setCurrentDay(day)
})

// 暴露方法给父组件
defineExpose({
  focusActivity
})
</script>

<style scoped>
.map-view {
  position: relative;
  height: 100%;
  min-height: 400px;
  background: rgba(255, 255, 255, 0.02);
  border-radius: 16px;
  overflow: hidden;
}

.map-controls {
  position: absolute;
  top: 16px;
  left: 16px;
  z-index: 10;
}

.map-container {
  width: 100%;
  height: 100%;
  min-height: 400px;
}

.map-legend {
  position: absolute;
  bottom: 16px;
  left: 16px;
  display: flex;
  gap: 16px;
  padding: 12px 16px;
  background: rgba(10, 10, 15, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  z-index: 10;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.7);
}

.legend-marker {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.legend-marker.attraction {
  background: #8b5cf6;
}

.legend-marker.hotel {
  background: #ec4899;
}

.legend-marker.restaurant {
  background: #f59e0b;
}
</style>
```

- [ ] **Step 2: 提交**

```bash
git add fronted/src/components/planning/MapView.vue
git commit -m "feat(component): add MapView component"
```

---

### Task 14: 创建 ItineraryTimeline 组件

**Files:**
- Create: `fronted/src/components/planning/ItineraryTimeline.vue`

- [ ] **Step 1: 创建时间轴组件**

```vue
<template>
  <div class="itinerary-timeline">
    <!-- 天切换 -->
    <div class="day-tabs">
      <button
        v-for="day in itinerary"
        :key="day.day"
        class="day-tab"
        :class="{ active: currentDay === day.day }"
        @click="currentDay = day.day"
      >
        <span class="day-number">Day{{ day.day }}</span>
        <span class="day-summary">{{ day.summary }}</span>
      </button>
    </div>

    <!-- 当前天内容 -->
    <div class="day-content" v-if="currentDayData">
      <!-- 日期头部 -->
      <div class="day-header">
        <div class="day-date">
          <span class="day-label">第{{ currentDayData.day }}天</span>
          <span class="day-date-text" v-if="currentDayData.date">
            {{ formatDate(currentDayData.date) }}
          </span>
        </div>
        <!-- 天气 -->
        <div class="day-weather" v-if="currentDayData.weather">
          <span class="weather-icon">🌤️</span>
          <span class="weather-temp">
            {{ currentDayData.weather.tempMin }}°~{{ currentDayData.weather.tempMax }}°
          </span>
          <span class="weather-condition">{{ currentDayData.weather.condition }}</span>
        </div>
      </div>

      <!-- 活动列表 -->
      <div class="activities-list">
        <template v-for="(activity, index) in currentDayData.activities" :key="activity.id">
          <!-- 交通段 -->
          <TransportSegment
            v-if="activity.type === 'TRANSPORT'"
            :activity="activity"
          />
          <!-- 活动卡片 -->
          <ActivityCard
            v-else
            :activity="activity"
            :is-highlighted="highlightedActivityId === activity.id"
            @click="$emit('activity-click', activity)"
            @navigate="$emit('navigate', activity)"
          />
        </template>
      </div>

      <!-- 当日汇总 -->
      <div class="day-summary-bar">
        <span class="summary-label">今日预算</span>
        <span class="summary-value">¥{{ dayTotalCost }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import ActivityCard from './ActivityCard.vue'
import TransportSegment from './TransportSegment.vue'
import { formatDate } from '@/utils/planFormatters'

const props = defineProps({
  itinerary: {
    type: Array,
    default: () => []
  },
  highlightedActivityId: {
    type: String,
    default: null
  }
})

const emit = defineEmits(['activity-click', 'navigate'])

const currentDay = ref(1)

// 当前天数据
const currentDayData = computed(() => {
  return props.itinerary?.find(d => d.day === currentDay.value)
})

// 当日总费用
const dayTotalCost = computed(() => {
  if (!currentDayData.value?.activities) return 0
  return currentDayData.value.activities.reduce((sum, a) => sum + (a.cost || 0), 0)
})

// 监听 itinerary 变化，默认选择第一天
const initCurrentDay = () => {
  if (props.itinerary?.length > 0) {
    currentDay.value = props.itinerary[0].day
  }
}

// 暴露方法
defineExpose({
  setCurrentDay: (day) => { currentDay.value = day }
})
</script>

<style scoped>
.itinerary-timeline {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.02);
  border-radius: 16px;
  overflow: hidden;
}

.day-tabs {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.03);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  overflow-x: auto;
}

.day-tab {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
  padding: 10px 16px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.day-tab:hover {
  background: rgba(255, 255, 255, 0.06);
}

.day-tab.active {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.2), rgba(139, 92, 246, 0.2));
  border-color: rgba(99, 102, 241, 0.3);
}

.day-number {
  font-size: 0.9rem;
  font-weight: 600;
  color: #fff;
}

.day-summary {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.5);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.day-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.day-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.day-label {
  font-size: 1.1rem;
  font-weight: 600;
  color: #fff;
  margin-right: 8px;
}

.day-date-text {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.5);
}

.day-weather {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 8px;
}

.weather-icon {
  font-size: 1rem;
}

.weather-temp {
  font-size: 0.85rem;
  color: #fff;
}

.weather-condition {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.5);
}

.activities-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.day-summary-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 10px;
}

.summary-label {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.6);
}

.summary-value {
  font-size: 1rem;
  font-weight: 600;
  color: #fbbf24;
}
</style>
```

- [ ] **Step 2: 提交**

```bash
git add fronted/src/components/planning/ItineraryTimeline.vue
git commit -m "feat(component): add ItineraryTimeline component"
```

---

## Phase 4: 整合与改造

### Task 15: 改造 Plan.vue 整合新组件

**Files:**
- Modify: `fronted/src/views/Plan.vue`

- [ ] **Step 1: 添加新组件导入和状态**

在 `<script setup>` 部分添加：

```javascript
import MapView from '@/components/planning/MapView.vue'
import ItineraryTimeline from '@/components/planning/ItineraryTimeline.vue'

// 结构化行程数据
const planDataResponse = ref(null)
const highlightedActivityId = ref(null)

// 获取结构化行程数据
const fetchPlanData = async (recordId) => {
  try {
    const response = await fetch(`/api/travel/plan/${recordId}/data`, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    })
    const data = await response.json()
    if (data.code === 200) {
      planDataResponse.value = data.data
    }
  } catch (err) {
    console.warn('获取结构化数据失败:', err)
  }
}

// 处理活动点击
const handleActivityClick = (activity) => {
  highlightedActivityId.value = activity.id
  // 通知地图聚焦
  if (mapViewRef.value) {
    mapViewRef.value.focusActivity(activity.id)
  }
}

// 地图组件引用
const mapViewRef = ref(null)
```

- [ ] **Step 2: 修改 handleComplete 函数**

在 `handleComplete` 函数中添加获取结构化数据的逻辑：

```javascript
const handleComplete = () => {
  isPlanning.value = false
  Object.keys(agentStatus).forEach(key => {
    if (agentStatus[key] === 'loading') {
      agentStatus[key] = 'complete'
    }
  })
  
  // 获取结构化数据（如果有 recordId）
  if (lastRecordId.value) {
    fetchPlanData(lastRecordId.value)
  }
}
```

- [ ] **Step 3: 添加 recordId 追踪**

在 `startPlanning` 函数中添加：

```javascript
const lastRecordId = ref(null)

// 在 handleMessage 中添加
if (data.recordId) {
  lastRecordId.value = data.recordId
}
```

- [ ] **Step 4: 添加可视化布局模板**

在 `supervisor-section` 之后添加可视化区域：

```vue
<!-- 可视化行程展示 -->
<div class="visual-plan-section" v-if="planDataResponse?.itinerary">
  <div class="visual-header">
    <h3>行程可视化</h3>
    <p>点击景点在地图上定位</p>
  </div>
  <div class="visual-content">
    <div class="map-wrapper">
      <MapView
        ref="mapViewRef"
        :itinerary="planDataResponse.itinerary"
        @activity-click="handleActivityClick"
      />
    </div>
    <div class="timeline-wrapper">
      <ItineraryTimeline
        :itinerary="planDataResponse.itinerary"
        :highlighted-activity-id="highlightedActivityId"
        @activity-click="handleActivityClick"
      />
    </div>
  </div>
</div>
```

- [ ] **Step 5: 添加样式**

在 `<style scoped>` 部分添加：

```css
/* Visual Plan Section */
.visual-plan-section {
  margin-top: 32px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  overflow: hidden;
}

.visual-header {
  padding: 20px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.visual-header h3 {
  font-size: 1.2rem;
  font-weight: 600;
  margin-bottom: 4px;
}

.visual-header p {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.5);
}

.visual-content {
  display: grid;
  grid-template-columns: 60% 40%;
  height: 500px;
}

.map-wrapper {
  border-right: 1px solid rgba(255, 255, 255, 0.06);
}

.timeline-wrapper {
  overflow: hidden;
}

@media (max-width: 1024px) {
  .visual-content {
    grid-template-columns: 1fr;
    grid-template-rows: 300px 1fr;
    height: auto;
  }

  .map-wrapper {
    border-right: none;
    border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  }
}
```

- [ ] **Step 6: 验证编译**

```bash
cd fronted && npm run build 2>&1 | head -20
```

- [ ] **Step 7: 提交**

```bash
git add fronted/src/views/Plan.vue
git commit -m "feat(plan): integrate MapView and ItineraryTimeline components"
```

---

### Task 16: 添加前端 API 方法

**Files:**
- Modify: `fronted/src/services/api.js`

- [ ] **Step 1: 添加旅游规划 API**

在 `api.js` 末尾添加：

```javascript
// 旅游规划相关API
export const travelApi = {
  // 获取结构化行程数据
  getPlanData(recordId) {
    return request(`/api/travel/plan/${recordId}/data`)
  },

  // 获取天气信息
  getWeather(recordId, extensions = 'all') {
    return request(`/api/travel/plan/${recordId}/weather?extensions=${extensions}`)
  }
}
```

- [ ] **Step 2: 提交**

```bash
git add fronted/src/services/api.js
git commit -m "feat(api): add travel plan API methods"
```

---

## Phase 5: 测试与验收

### Task 17: 后端单元测试

**Files:**
- Create: `backend/src/test/java/cn/sdh/travel/service/PlanRecordServiceTest.java`

- [ ] **Step 1: 创建测试类**

```java
package cn.sdh.travel.service;

import cn.sdh.travel.entity.domain.PlanRecord;
import cn.sdh.travel.mapper.PlanRecordMapper;
import cn.sdh.travel.service.impl.PlanRecordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlanRecordServiceTest {

    @Mock
    private PlanRecordMapper planRecordMapper;

    @InjectMocks
    private PlanRecordServiceImpl planRecordService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUpdatePlanData() {
        Long id = 1L;
        String planData = "{\"test\": \"data\"}";

        planRecordService.updatePlanData(id, planData);

        verify(planRecordMapper, times(1)).updatePlanData(id, planData);
    }

    @Test
    void testGetPlanData() {
        Long id = 1L;
        String expectedData = "{\"test\": \"data\"}";

        when(planRecordMapper.getPlanData(id)).thenReturn(expectedData);

        String result = planRecordService.getPlanData(id);

        assertEquals(expectedData, result);
    }
}
```

- [ ] **Step 2: 运行测试**

```bash
cd backend && mvn test -Dtest=PlanRecordServiceTest -q
```

- [ ] **Step 3: 提交**

```bash
git add backend/src/test/java/cn/sdh/travel/service/PlanRecordServiceTest.java
git commit -m "test: add PlanRecordService unit tests"
```

---

### Task 18: 验收测试

- [ ] **Step 1: 启动后端服务**

```bash
cd backend && mvn spring-boot:run
```

- [ ] **Step 2: 启动前端服务**

```bash
cd fronted && npm run dev
```

- [ ] **Step 3: 验收清单**

手动验证以下功能：

1. [ ] 后端启动无报错，数据库迁移成功
2. [ ] 前端启动无报错，页面正常加载
3. [ ] 规划完成后，可视化区域显示
4. [ ] 地图正确渲染
5. [ ] 时间轴正确显示每日活动
6. [ ] 天筛选器切换正常
7. [ ] 点击活动卡片，地图标记高亮
8. [ ] 移动端布局适配正常

---

## 实现优先级与时间估算

| Phase | 任务数 | 预计工时 |
|-------|--------|----------|
| Phase 1: 后端数据结构 | 7 个任务 | 2 天 |
| Phase 2: 前端工具函数 | 2 个任务 | 0.5 天 |
| Phase 3: 地图组件 | 5 个任务 | 2 天 |
| Phase 4: 整合改造 | 2 个任务 | 1 天 |
| Phase 5: 测试验收 | 2 个任务 | 1 天 |

**总计：约 6.5 天**

---

## 验收标准

1. 数据库 `plan_data` 字段正确创建
2. API 端点 `/api/travel/plan/{id}/data` 正常返回结构化数据
3. 地图组件正确加载高德地图 SDK
4. 景点、酒店、餐厅标记按类型显示不同颜色
5. 路线按交通方式显示不同样式
6. 天筛选器切换时地图视野自适应
7. 时间轴按天展示，活动卡片信息完整
8. 点击卡片与地图标记联动
9. 移动端布局正常适配
10. 后端单元测试通过

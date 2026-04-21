package cn.sdh.travel.agent.tool;

import cn.hutool.core.util.StrUtil;
import cn.sdh.travel.service.GaodeMapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 高德地图工具
 * 提供给Agent调用的地图能力，包括：
 * - 地理编码（地址转坐标）
 * - 逆地理编码（坐标转地址）
 * - POI搜索（景点、酒店、餐厅等）
 * - 路线规划（驾车、公交、步行）
 * - 距离计算
 * - 天气查询
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GaodeMapTool {

    private final GaodeMapService gaodeMapService;

    // ==================== 地理编码 ====================

    /**
     * 地理编码：将地址转换为经纬度坐标
     */
    @Tool(description = "将地址转换为经纬度坐标。输入地址，返回坐标位置。例如：address='北京市故宫', city='北京'")
    public String geocode(
            @ToolParam(description = "待解析的结构化地址信息") String address,
            @ToolParam(description = "指定查询的城市，可选") String city
    ) {
        log.info("[地图工具] 地理编码: address={}, city={}", address, city);
        Map<String, Object> result = gaodeMapService.geocode(address, city);
        return formatResult(result);
    }

    /**
     * 逆地理编码：将经纬度坐标转换为地址
     */
    @Tool(description = "将经纬度坐标转换为详细地址。输入格式：经度,纬度。例如：location='116.397428,39.90923'")
    public String reverseGeocode(
            @ToolParam(description = "经纬度坐标，格式：经度,纬度") String location
    ) {
        log.info("[地图工具] 逆地理编码: location={}", location);
        Map<String, Object> result = gaodeMapService.reverseGeocode(location);
        return formatResult(result);
    }

    // ==================== POI搜索 ====================

    /**
     * 关键词POI搜索
     */
    @Tool(description = "根据关键词搜索POI兴趣点，如景点、酒店、餐厅等。例如：keywords='故宫', city='北京', types='110100'")
    public String searchPoi(
            @ToolParam(description = "搜索关键词，如'故宫'、'希尔顿酒店'") String keywords,
            @ToolParam(description = "查询城市") String city,
            @ToolParam(description = "POI类型编码，可选。景点:110100, 酒店:100100, 餐厅:050100") String types,
            @ToolParam(description = "当前页码，从1开始，可选") Integer offset,
            @ToolParam(description = "每页条数，最大25，可选") Integer limit
    ) {
        log.info("[地图工具] POI搜索: keywords={}, city={}, types={}", keywords, city, types);
        Map<String, Object> result = gaodeMapService.searchPoi(keywords, city, types, offset, limit);
        return formatResult(result);
    }

    /**
     * 周边POI搜索
     */
    @Tool(description = "搜索中心点周边的POI兴趣点。例如：location='116.397428,39.90923', keywords='餐厅', radius=1000")
    public String searchPoiAround(
            @ToolParam(description = "中心点坐标，格式：经度,纬度") String location,
            @ToolParam(description = "搜索关键词") String keywords,
            @ToolParam(description = "POI类型编码，可选") String types,
            @ToolParam(description = "搜索半径，单位米，默认3000") Integer radius,
            @ToolParam(description = "当前页码") Integer offset,
            @ToolParam(description = "每页条数") Integer limit
    ) {
        log.info("[地图工具] 周边POI搜索: location={}, keywords={}, radius={}", location, keywords, radius);
        Map<String, Object> result = gaodeMapService.searchPoiAround(location, keywords, types, radius, offset, limit);
        return formatResult(result);
    }

    // ==================== 路线规划 ====================

    /**
     * 驾车路线规划
     */
    @Tool(description = "规划驾车路线。输入起点和终点坐标，返回驾车路线方案。strategy: 0-最快 1-最短 2-避免收费 3-避免拥堵")
    public String drivingRoute(
            @ToolParam(description = "起点坐标，格式：经度,纬度") String origin,
            @ToolParam(description = "终点坐标，格式：经度,纬度") String destination,
            @ToolParam(description = "策略：0-最快 1-最短 2-避免收费 3-避免拥堵，默认0") String strategy
    ) {
        log.info("[地图工具] 驾车路线规划: origin={}, destination={}, strategy={}", origin, destination, strategy);
        Map<String, Object> result = gaodeMapService.drivingRoute(origin, destination, strategy);
        return formatResult(result);
    }

    /**
     * 公交路线规划
     */
    @Tool(description = "规划公交/地铁路线。strategy: 0-最快 1-最经济 2-最少换乘 3-最少步行 5-地铁优先")
    public String transitRoute(
            @ToolParam(description = "起点坐标，格式：经度,纬度") String origin,
            @ToolParam(description = "终点坐标，格式：经度,纬度") String destination,
            @ToolParam(description = "起点城市") String city,
            @ToolParam(description = "终点城市，跨城时需要") String cityd,
            @ToolParam(description = "策略：0-最快 1-最经济 2-最少换乘 3-最少步行 5-地铁优先") String strategy
    ) {
        log.info("[地图工具] 公交路线规划: origin={}, destination={}, city={}", origin, destination, city);
        Map<String, Object> result = gaodeMapService.transitRoute(origin, destination, city, cityd, strategy);
        return formatResult(result);
    }

    /**
     * 步行路线规划
     */
    @Tool(description = "规划步行路线。适用于短距离步行导航。")
    public String walkingRoute(
            @ToolParam(description = "起点坐标，格式：经度,纬度") String origin,
            @ToolParam(description = "终点坐标，格式：经度,纬度") String destination
    ) {
        log.info("[地图工具] 步行路线规划: origin={}, destination={}", origin, destination);
        Map<String, Object> result = gaodeMapService.walkingRoute(origin, destination);
        return formatResult(result);
    }

    // ==================== 距离测量 ====================

    /**
     * 距离测量
     */
    @Tool(description = "测量两点间距离。type: 0-直线距离 1-驾车距离 3-步行距离。支持多个起点到一个终点。")
    public String measureDistance(
            @ToolParam(description = "起点坐标，多个用|分隔，如：116.1,39.1|116.2,39.2") String origins,
            @ToolParam(description = "终点坐标") String destination,
            @ToolParam(description = "类型：0-直线 1-驾车 3-步行，默认1") String type
    ) {
        log.info("[地图工具] 距离测量: origins={}, destination={}, type={}", origins, destination, type);
        Map<String, Object> result = gaodeMapService.measureDistance(origins, destination, type);
        return formatResult(result);
    }

    // ==================== 天气查询 ====================

    /**
     * 天气查询
     */
    @Tool(description = "查询城市天气。extensions: base-实时天气 all-天气预报。")
    public String queryWeather(
            @ToolParam(description = "城市名称或adcode") String city,
            @ToolParam(description = "base-实时天气 all-天气预报，默认base") String extensions
    ) {
        log.info("[地图工具] 天气查询: city={}, extensions={}", city, extensions);
        Map<String, Object> result = gaodeMapService.queryWeather(city, extensions);
        return formatResult(result);
    }

    // ==================== 行政区查询 ====================

    /**
     * 行政区查询
     */
    @Tool(description = "查询行政区划信息。可以查询省、市、区的层级关系。")
    public String queryDistrict(
            @ToolParam(description = "行政区名称，如'北京'、'朝阳区'") String keywords,
            @ToolParam(description = "子级行政区级别：0-不返回 1-返回下一级 2-返回下两级 3-返回下三级") String subdistrict
    ) {
        log.info("[地图工具] 行政区查询: keywords={}, subdistrict={}", keywords, subdistrict);
        Map<String, Object> result = gaodeMapService.queryDistrict(keywords, subdistrict);
        return formatResult(result);
    }

    // ==================== 辅助方法 ====================

    /**
     * 通过地址搜索景点
     */
    @Tool(description = "搜索城市内的景点。返回景点名称、地址、坐标等信息。")
    public String searchAttractions(
            @ToolParam(description = "城市名称") String city,
            @ToolParam(description = "景点名称关键词，可选") String keyword,
            @ToolParam(description = "返回数量，默认10") Integer limit
    ) {
        log.info("[地图工具] 搜索景点: city={}, keyword={}", city, keyword);
        String keywords = StrUtil.isNotBlank(keyword) ? keyword : "景点";
        Map<String, Object> result = gaodeMapService.searchPoi(keywords, city, "110100", 1, limit != null ? limit : 10);
        return formatResult(result);
    }

    /**
     * 搜索酒店
     */
    @Tool(description = "搜索城市内或指定位置周边的酒店。")
    public String searchHotels(
            @ToolParam(description = "城市名称或中心点坐标") String location,
            @ToolParam(description = "是否为坐标（true）或城市名（false）") Boolean isCoordinate,
            @ToolParam(description = "酒店名称关键词，可选") String keyword,
            @ToolParam(description = "搜索半径（仅坐标模式有效），单位米") Integer radius,
            @ToolParam(description = "返回数量，默认10") Integer limit
    ) {
        log.info("[地图工具] 搜索酒店: location={}, isCoordinate={}", location, isCoordinate);
        int limitNum = limit != null ? limit : 10;
        Map<String, Object> result;

        if (Boolean.TRUE.equals(isCoordinate)) {
            result = gaodeMapService.searchPoiAround(location, StrUtil.blankToDefault(keyword, "酒店"), "100100", radius, 1, limitNum);
        } else {
            result = gaodeMapService.searchPoi(StrUtil.blankToDefault(keyword, "酒店"), location, "100100", 1, limitNum);
        }

        return formatResult(result);
    }

    /**
     * 搜索餐厅
     */
    @Tool(description = "搜索城市内或指定位置周边的餐厅。")
    public String searchRestaurants(
            @ToolParam(description = "城市名称或中心点坐标") String location,
            @ToolParam(description = "是否为坐标（true）或城市名（false）") Boolean isCoordinate,
            @ToolParam(description = "餐厅名称或菜系关键词，可选") String keyword,
            @ToolParam(description = "搜索半径（仅坐标模式有效），单位米") Integer radius,
            @ToolParam(description = "返回数量，默认10") Integer limit
    ) {
        log.info("[地图工具] 搜索餐厅: location={}, isCoordinate={}", location, isCoordinate);
        int limitNum = limit != null ? limit : 10;
        Map<String, Object> result;

        if (Boolean.TRUE.equals(isCoordinate)) {
            result = gaodeMapService.searchPoiAround(location, StrUtil.blankToDefault(keyword, "餐厅"), "050100", radius, 1, limitNum);
        } else {
            result = gaodeMapService.searchPoi(StrUtil.blankToDefault(keyword, "餐厅"), location, "050100", 1, limitNum);
        }

        return formatResult(result);
    }

    /**
     * 格式化返回结果
     */
    private String formatResult(Map<String, Object> result) {
        StringBuilder sb = new StringBuilder();

        String status = (String) result.get("status");
        if (!"1".equals(status)) {
            sb.append("调用失败: ").append(result.get("info")).append("\n");
            return sb.toString();
        }

        // 地理编码结果
        if (result.containsKey("geocodes")) {
            sb.append("地理编码结果:\n");
            java.util.List<Map<String, Object>> geocodes = (java.util.List<Map<String, Object>>) result.get("geocodes");
            for (Map<String, Object> geo : geocodes) {
                sb.append("- 地址: ").append(geo.get("formattedAddress")).append("\n");
                sb.append("  坐标: ").append(geo.get("location")).append("\n");
                sb.append("  区域: ").append(geo.get("province")).append(geo.get("city")).append(geo.get("district")).append("\n");
            }
        }

        // POI搜索结果
        if (result.containsKey("pois")) {
            sb.append("共找到 ").append(result.get("count")).append(" 条结果:\n");
            java.util.List<Map<String, Object>> pois = (java.util.List<Map<String, Object>>) result.get("pois");
            int idx = 1;
            for (Map<String, Object> poi : pois) {
                sb.append(idx++).append(". ").append(poi.get("name")).append("\n");
                sb.append("   类型: ").append(poi.get("type")).append("\n");
                sb.append("   地址: ").append(poi.get("address")).append("\n");
                sb.append("   坐标: ").append(poi.get("location")).append("\n");
                if (poi.get("tel") != null) sb.append("   电话: ").append(poi.get("tel")).append("\n");
                if (poi.get("rating") != null) sb.append("   评分: ").append(poi.get("rating")).append("\n");
                if (poi.get("distance") != null) sb.append("   距离: ").append(poi.get("distance")).append("米\n");
            }
        }

        // 驾车路线结果
        if (result.containsKey("paths")) {
            sb.append("驾车路线规划:\n");
            sb.append("总距离: ").append(result.get("distance")).append("米\n");
            sb.append("预计时间: ").append(Integer.parseInt((String) result.get("duration")) / 60).append("分钟\n");
            sb.append("过路费: ").append(result.get("tolls")).append("元\n");

            java.util.List<Map<String, Object>> paths = (java.util.List<Map<String, Object>>) result.get("paths");
            Map<String, Object> path = paths.get(0);
            sb.append("路线指引:\n");
            java.util.List<String> steps = (java.util.List<String>) path.get("steps");
            if (steps != null) {
                for (String step : steps) {
                    sb.append("- ").append(step.replaceAll("<[^>]+>", "")).append("\n");
                }
            }
        }

        // 公交路线结果
        if (result.containsKey("transits")) {
            sb.append("公交/地铁路线规划:\n");
            java.util.List<Map<String, Object>> transits = (java.util.List<Map<String, Object>>) result.get("transits");
            int idx = 1;
            for (Map<String, Object> transit : transits) {
                sb.append("方案").append(idx++).append(": 预计").append(Integer.parseInt((String) transit.get("duration")) / 60).append("分钟\n");
                java.util.List<Map<String, Object>> segments = (java.util.List<Map<String, Object>>) transit.get("segments");
                if (segments != null) {
                    for (Map<String, Object> seg : segments) {
                        if (seg.get("busName") != null) {
                            sb.append("  乘坐 ").append(seg.get("busName")).append("\n");
                            sb.append("    从 ").append(seg.get("departureStop")).append(" 到 ").append(seg.get("arrivalStop")).append("\n");
                        }
                        if (seg.get("walkingDistance") != null && Integer.parseInt((String) seg.get("walkingDistance")) > 0) {
                            sb.append("  步行 ").append(seg.get("walkingDistance")).append("米\n");
                        }
                    }
                }
                sb.append("\n");
            }
        }

        // 步行路线结果
        if (result.containsKey("steps") && !result.containsKey("paths") && !result.containsKey("transits")) {
            sb.append("步行路线:\n");
            sb.append("总距离: ").append(result.get("distance")).append("米\n");
            sb.append("预计时间: ").append(Integer.parseInt((String) result.get("duration")) / 60).append("分钟\n");
            sb.append("路线指引:\n");
            java.util.List<String> steps = (java.util.List<String>) result.get("steps");
            for (String step : steps) {
                sb.append("- ").append(step.replaceAll("<[^>]+>", "")).append("\n");
            }
        }

        // 天气结果
        if (result.containsKey("weather")) {
            sb.append("天气查询结果:\n");
            sb.append("城市: ").append(result.get("city")).append("\n");
            sb.append("天气: ").append(result.get("weather")).append("\n");
            sb.append("温度: ").append(result.get("temperature")).append("°C\n");
            sb.append("风向: ").append(result.get("windDirection")).append("\n");
            sb.append("风力: ").append(result.get("windPower")).append("级\n");
            sb.append("湿度: ").append(result.get("humidity")).append("%\n");
            sb.append("发布时间: ").append(result.get("reportTime")).append("\n");
        }

        // 天气预报
        if (result.containsKey("forecasts")) {
            sb.append("\n天气预报:\n");
            java.util.List<Map<String, Object>> forecasts = (java.util.List<Map<String, Object>>) result.get("forecasts");
            for (Map<String, Object> cast : forecasts) {
                sb.append(cast.get("date")).append(" 周").append(cast.get("week")).append("\n");
                sb.append("  白天: ").append(cast.get("dayWeather")).append(" ").append(cast.get("dayTemp")).append("°C\n");
                sb.append("  夜间: ").append(cast.get("nightWeather")).append(" ").append(cast.get("nightTemp")).append("°C\n");
            }
        }

        // 行政区结果
        if (result.containsKey("districts")) {
            sb.append("行政区划:\n");
            java.util.List<Map<String, Object>> districts = (java.util.List<Map<String, Object>>) result.get("districts");
            for (Map<String, Object> dist : districts) {
                sb.append(dist.get("name")).append(" (").append(dist.get("level")).append(")\n");
                sb.append("  中心点: ").append(dist.get("center")).append("\n");
            }
        }

        // 距离测量结果
        if (result.containsKey("results")) {
            sb.append("距离测量结果:\n");
            java.util.List<Map<String, Object>> results = (java.util.List<Map<String, Object>>) result.get("results");
            for (Map<String, Object> r : results) {
                sb.append("起点").append(r.get("originId")).append(": ");
                sb.append("距离").append(r.get("distance")).append("米");
                if (r.get("duration") != null) {
                    sb.append(", 预计").append(Integer.parseInt((String) r.get("duration")) / 60).append("分钟");
                }
                sb.append("\n");
            }
        }

        // 逆地理编码结果
        if (result.containsKey("formattedAddress")) {
            sb.append("详细地址: ").append(result.get("formattedAddress")).append("\n");
            sb.append("省: ").append(result.get("province")).append("\n");
            sb.append("市: ").append(result.get("city")).append("\n");
            sb.append("区: ").append(result.get("district")).append("\n");
        }

        return sb.toString();
    }
}

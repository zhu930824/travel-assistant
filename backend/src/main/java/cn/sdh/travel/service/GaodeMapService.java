package cn.sdh.travel.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.sdh.travel.config.GaodeMapConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 高德地图服务
 * 封装高德地图Web API，提供地理编码、POI搜索、路线规划、距离计算等能力
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GaodeMapService {

    private final GaodeMapConfig gaodeMapConfig;

    /**
     * 地理编码：地址 -> 经纬度
     */
    public Map<String, Object> geocode(String address, String city) {
        Map<String, Object> params = buildBaseParams();
        params.put("address", address);
        if (StrUtil.isNotBlank(city)) {
            params.put("city", city);
        }

        String result = doGet("/geocode/geo", params);
        JSONObject json = JSONUtil.parseObj(result);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", json.getStr("status"));
        response.put("info", json.getStr("info"));

        JSONArray geocodes = json.getJSONArray("geocodes");
        if (geocodes != null && !geocodes.isEmpty()) {
            List<Map<String, Object>> locations = new ArrayList<>();
            for (int i = 0; i < geocodes.size(); i++) {
                JSONObject geo = geocodes.getJSONObject(i);
                Map<String, Object> location = new LinkedHashMap<>();
                location.put("formattedAddress", geo.getStr("formatted_address"));
                location.put("location", geo.getStr("location"));
                location.put("level", geo.getStr("level"));
                JSONObject adInfo = geo.getJSONObject("ad_info") != null ? geo.getJSONObject("ad_info") : new JSONObject();
                // 从addressComponent提取
                JSONObject addrComp = geo.getJSONObject("addressComponent") != null ? geo.getJSONObject("addressComponent") : new JSONObject();
                location.put("province", addrComp.getStr("province"));
                location.put("city", addrComp.getStr("city"));
                location.put("district", addrComp.getStr("district"));
                locations.add(location);
            }
            response.put("geocodes", locations);
        }

        return response;
    }

    /**
     * 逆地理编码：经纬度 -> 地址
     */
    public Map<String, Object> reverseGeocode(String location) {
        Map<String, Object> params = buildBaseParams();
        params.put("location", location);

        String result = doGet("/geocode/regeo", params);
        JSONObject json = JSONUtil.parseObj(result);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", json.getStr("status"));

        JSONObject regeocode = json.getJSONObject("regeocode");
        if (regeocode != null) {
            response.put("formattedAddress", regeocode.getStr("formatted_address"));
            JSONObject addrComp = regeocode.getJSONObject("addressComponent");
            if (addrComp != null) {
                response.put("province", addrComp.getStr("province"));
                response.put("city", addrComp.getStr("city"));
                response.put("district", addrComp.getStr("district"));
                response.put("township", addrComp.getStr("township"));
                response.put("street", addrComp.getStr("streetNumber") != null
                        ? addrComp.getJSONObject("streetNumber").getStr("street") : "");
            }
        }

        return response;
    }

    /**
     * POI关键词搜索
     */
    public Map<String, Object> searchPoi(String keywords, String city, String types,
                                          Integer offset, Integer limit) {
        Map<String, Object> params = buildBaseParams();
        params.put("keywords", keywords);
        if (StrUtil.isNotBlank(city)) {
            params.put("city", city);
        }
        if (StrUtil.isNotBlank(types)) {
            params.put("types", types);
        }
        params.put("offset", offset != null ? offset : 1);
        params.put("limit", limit != null ? limit : 10);

        String result = doGet("/place/text", params);
        JSONObject json = JSONUtil.parseObj(result);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", json.getStr("status"));
        response.put("count", json.getStr("count"));

        JSONArray pois = json.getJSONArray("pois");
        if (pois != null && !pois.isEmpty()) {
            List<Map<String, Object>> poiList = new ArrayList<>();
            for (int i = 0; i < pois.size(); i++) {
                JSONObject poi = pois.getJSONObject(i);
                Map<String, Object> poiInfo = new LinkedHashMap<>();
                poiInfo.put("name", poi.getStr("name"));
                poiInfo.put("type", poi.getStr("type"));
                poiInfo.put("address", poi.getStr("address"));
                poiInfo.put("location", poi.getStr("location"));
                poiInfo.put("tel", poi.getStr("tel"));

                // 评分和价格
                JSONObject bizExt = poi.getJSONObject("biz_ext");
                if (bizExt != null) {
                    poiInfo.put("rating", bizExt.getStr("rating"));
                    poiInfo.put("cost", bizExt.getStr("cost"));
                }

                // 距离（如果有传入位置）
                if (poi.get("distance") != null) {
                    poiInfo.put("distance", poi.getStr("distance"));
                }

                poiList.add(poiInfo);
            }
            response.put("pois", poiList);
        }

        return response;
    }

    /**
     * POI周边搜索
     */
    public Map<String, Object> searchPoiAround(String location, String keywords,
                                                String types, Integer radius,
                                                Integer offset, Integer limit) {
        Map<String, Object> params = buildBaseParams();
        params.put("location", location);
        if (StrUtil.isNotBlank(keywords)) {
            params.put("keywords", keywords);
        }
        if (StrUtil.isNotBlank(types)) {
            params.put("types", types);
        }
        params.put("radius", radius != null ? radius : 3000);
        params.put("offset", offset != null ? offset : 1);
        params.put("limit", limit != null ? limit : 10);

        String result = doGet("/place/around", params);
        JSONObject json = JSONUtil.parseObj(result);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", json.getStr("status"));
        response.put("count", json.getStr("count"));

        JSONArray pois = json.getJSONArray("pois");
        if (pois != null && !pois.isEmpty()) {
            List<Map<String, Object>> poiList = new ArrayList<>();
            for (int i = 0; i < pois.size(); i++) {
                JSONObject poi = pois.getJSONObject(i);
                Map<String, Object> poiInfo = new LinkedHashMap<>();
                poiInfo.put("name", poi.getStr("name"));
                poiInfo.put("type", poi.getStr("type"));
                poiInfo.put("address", poi.getStr("address"));
                poiInfo.put("location", poi.getStr("location"));
                poiInfo.put("tel", poi.getStr("tel"));
                poiInfo.put("distance", poi.getStr("distance"));

                JSONObject bizExt = poi.getJSONObject("biz_ext");
                if (bizExt != null) {
                    poiInfo.put("rating", bizExt.getStr("rating"));
                    poiInfo.put("cost", bizExt.getStr("cost"));
                }

                poiList.add(poiInfo);
            }
            response.put("pois", poiList);
        }

        return response;
    }

    /**
     * 路线规划（驾车）
     */
    public Map<String, Object> drivingRoute(String origin, String destination,
                                             String strategy) {
        Map<String, Object> params = buildBaseParams();
        params.put("origin", origin);
        params.put("destination", destination);
        // strategy: 0-最快 1-最短 2-避免收费 3-避免拥堵
        params.put("strategy", StrUtil.isNotBlank(strategy) ? strategy : "0");

        String result = doGet("/direction/driving", params);
        JSONObject json = JSONUtil.parseObj(result);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", json.getStr("status"));

        JSONObject route = json.getJSONObject("route");
        if (route != null) {
            response.put("distance", route.getStr("distance"));
            response.put("duration", route.getStr("duration"));
            response.put("tolls", route.getStr("tolls"));

            JSONArray paths = route.getJSONArray("paths");
            if (paths != null && !paths.isEmpty()) {
                List<Map<String, Object>> pathList = new ArrayList<>();
                for (int i = 0; i < paths.size(); i++) {
                    JSONObject path = paths.getJSONObject(i);
                    Map<String, Object> pathInfo = new LinkedHashMap<>();
                    pathInfo.put("distance", path.getStr("distance"));
                    pathInfo.put("duration", path.getStr("duration"));
                    pathInfo.put("strategy", path.getStr("strategy"));
                    pathInfo.put("tolls", path.getStr("tolls"));

                    JSONArray steps = path.getJSONArray("steps");
                    if (steps != null && !steps.isEmpty()) {
                        List<String> instructions = new ArrayList<>();
                        for (int j = 0; j < steps.size(); j++) {
                            JSONObject step = steps.getJSONObject(j);
                            instructions.add(step.getStr("instruction"));
                        }
                        pathInfo.put("steps", instructions);
                    }

                    pathList.add(pathInfo);
                }
                response.put("paths", pathList);
            }
        }

        return response;
    }

    /**
     * 路线规划（公交/步行）
     */
    public Map<String, Object> transitRoute(String origin, String destination,
                                             String city, String cityd,
                                             String strategy) {
        Map<String, Object> params = buildBaseParams();
        params.put("origin", origin);
        params.put("destination", destination);
        params.put("city", city);
        if (StrUtil.isNotBlank(cityd)) {
            params.put("cityd", cityd);
        }
        // strategy: 0-最快 1-最经济 2-最少换乘 3-最少步行 5-地铁优先
        params.put("strategy", StrUtil.isNotBlank(strategy) ? strategy : "0");

        String result = doGet("/direction/transit/integrated", params);
        JSONObject json = JSONUtil.parseObj(result);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", json.getStr("status"));

        JSONObject route = json.getJSONObject("route");
        if (route != null) {
            response.put("distance", route.getStr("distance"));

            JSONArray transits = route.getJSONArray("transits");
            if (transits != null && !transits.isEmpty()) {
                List<Map<String, Object>> transitList = new ArrayList<>();
                int limit = Math.min(transits.size(), 3);
                for (int i = 0; i < limit; i++) {
                    JSONObject transit = transits.getJSONObject(i);
                    Map<String, Object> transitInfo = new LinkedHashMap<>();
                    transitInfo.put("duration", transit.getStr("duration"));
                    transitInfo.put("walkingDistance", transit.getStr("walking_distance"));
                    transitInfo.put("cost", transit.getStr("cost"));

                    JSONArray segments = transit.getJSONArray("segments");
                    if (segments != null && !segments.isEmpty()) {
                        List<Map<String, Object>> segList = new ArrayList<>();
                        for (int j = 0; j < segments.size(); j++) {
                            JSONObject seg = segments.getJSONObject(j);
                            Map<String, Object> segInfo = new LinkedHashMap<>();

                            JSONObject bus = seg.getJSONObject("bus");
                            if (bus != null) {
                                JSONArray busLines = bus.getJSONArray("buslines");
                                if (busLines != null && !busLines.isEmpty()) {
                                    JSONObject busLine = busLines.getJSONObject(0);
                                    segInfo.put("busName", busLine.getStr("name"));
                                    segInfo.put("departureStop", busLine.getJSONObject("departure_stop") != null
                                            ? busLine.getJSONObject("departure_stop").getStr("name") : "");
                                    segInfo.put("arrivalStop", busLine.getJSONObject("arrival_stop") != null
                                            ? busLine.getJSONObject("arrival_stop").getStr("name") : "");
                                    segInfo.put("duration", busLine.getStr("duration"));
                                }
                            }

                            JSONObject walking = seg.getJSONObject("walking");
                            if (walking != null) {
                                segInfo.put("walkingDistance", walking.getStr("distance"));
                                segInfo.put("walkingDuration", walking.getStr("duration"));
                            }

                            segList.add(segInfo);
                        }
                        transitInfo.put("segments", segList);
                    }

                    transitList.add(transitInfo);
                }
                response.put("transits", transitList);
            }
        }

        return response;
    }

    /**
     * 步行路线规划
     */
    public Map<String, Object> walkingRoute(String origin, String destination) {
        Map<String, Object> params = buildBaseParams();
        params.put("origin", origin);
        params.put("destination", destination);

        String result = doGet("/direction/walking", params);
        JSONObject json = JSONUtil.parseObj(result);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", json.getStr("status"));

        JSONObject route = json.getJSONObject("route");
        if (route != null) {
            response.put("distance", route.getStr("distance"));
            response.put("duration", route.getStr("duration"));

            JSONArray paths = route.getJSONArray("paths");
            if (paths != null && !paths.isEmpty()) {
                JSONObject path = paths.getJSONObject(0);
                JSONArray steps = path.getJSONArray("steps");
                if (steps != null) {
                    List<String> instructions = new ArrayList<>();
                    for (int i = 0; i < steps.size(); i++) {
                        instructions.add(steps.getJSONObject(i).getStr("instruction"));
                    }
                    response.put("steps", instructions);
                }
            }
        }

        return response;
    }

    /**
     * 距离测量
     */
    public Map<String, Object> measureDistance(String origins, String destination, String type) {
        Map<String, Object> params = buildBaseParams();
        params.put("origins", origins);
        params.put("destination", destination);
        // type: 0-直线 1-驾车 3-步行
        params.put("type", StrUtil.isNotBlank(type) ? type : "1");

        String result = doGet("/distance", params);
        JSONObject json = JSONUtil.parseObj(result);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", json.getStr("status"));

        JSONArray results = json.getJSONArray("results");
        if (results != null) {
            List<Map<String, Object>> distList = new ArrayList<>();
            for (int i = 0; i < results.size(); i++) {
                JSONObject item = results.getJSONObject(i);
                Map<String, Object> distInfo = new LinkedHashMap<>();
                distInfo.put("originId", item.getStr("origin_id"));
                distInfo.put("distance", item.getStr("distance"));
                distInfo.put("duration", item.getStr("duration"));
                distList.add(distInfo);
            }
            response.put("results", distList);
        }

        return response;
    }

    /**
     * 行政区查询
     */
    public Map<String, Object> queryDistrict(String keywords, String subdistrict) {
        Map<String, Object> params = buildBaseParams();
        params.put("keywords", keywords);
        params.put("subdistrict", StrUtil.isNotBlank(subdistrict) ? subdistrict : "1");

        String result = doGet("/config/district", params);
        JSONObject json = JSONUtil.parseObj(result);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", json.getStr("status"));

        JSONArray districts = json.getJSONArray("districts");
        if (districts != null && !districts.isEmpty()) {
            List<Map<String, Object>> districtList = new ArrayList<>();
            for (int i = 0; i < districts.size(); i++) {
                JSONObject dist = districts.getJSONObject(i);
                Map<String, Object> distInfo = new LinkedHashMap<>();
                distInfo.put("name", dist.getStr("name"));
                distInfo.put("level", dist.getStr("level"));
                distInfo.put("center", dist.getStr("center"));

                JSONArray children = dist.getJSONArray("districts");
                if (children != null && !children.isEmpty()) {
                    List<String> childNames = new ArrayList<>();
                    for (int j = 0; j < children.size(); j++) {
                        childNames.add(children.getJSONObject(j).getStr("name"));
                    }
                    distInfo.put("children", childNames);
                }

                districtList.add(distInfo);
            }
            response.put("districts", districtList);
        }

        return response;
    }

    /**
     * 天气查询
     */
    public Map<String, Object> queryWeather(String city, String extensions) {
        Map<String, Object> params = buildBaseParams();
        params.put("city", city);
        // extensions: base-实况 all-预报
        params.put("extensions", StrUtil.isNotBlank(extensions) ? extensions : "base");

        String result = doGet("/weather/weatherInfo", params);
        JSONObject json = JSONUtil.parseObj(result);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", json.getStr("status"));

        JSONArray lives = json.getJSONArray("lives");
        if (lives != null && !lives.isEmpty()) {
            JSONObject live = lives.getJSONObject(0);
            response.put("province", live.getStr("province"));
            response.put("city", live.getStr("city"));
            response.put("weather", live.getStr("weather"));
            response.put("temperature", live.getStr("temperature"));
            response.put("windDirection", live.getStr("winddirection"));
            response.put("windPower", live.getStr("windpower"));
            response.put("humidity", live.getStr("humidity"));
            response.put("reportTime", live.getStr("reporttime"));
        }

        JSONArray forecasts = json.getJSONArray("forecasts");
        if (forecasts != null && !forecasts.isEmpty()) {
            JSONObject forecast = forecasts.getJSONObject(0);
            JSONArray casts = forecast.getJSONArray("casts");
            if (casts != null) {
                List<Map<String, Object>> castList = new ArrayList<>();
                for (int i = 0; i < casts.size(); i++) {
                    JSONObject cast = casts.getJSONObject(i);
                    Map<String, Object> castInfo = new LinkedHashMap<>();
                    castInfo.put("date", cast.getStr("date"));
                    castInfo.put("week", cast.getStr("week"));
                    castInfo.put("dayWeather", cast.getStr("dayweather"));
                    castInfo.put("nightWeather", cast.getStr("nightweather"));
                    castInfo.put("dayTemp", cast.getStr("daytemp"));
                    castInfo.put("nightTemp", cast.getStr("nighttemp"));
                    castInfo.put("dayWind", cast.getStr("daywind"));
                    castInfo.put("nightWind", cast.getStr("nightwind"));
                    castInfo.put("dayPower", cast.getStr("daypower"));
                    castInfo.put("nightPower", cast.getStr("nightpower"));
                    castList.add(castInfo);
                }
                response.put("forecasts", castList);
            }
        }

        return response;
    }

    // ===== 工具方法 =====

    private Map<String, Object> buildBaseParams() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("key", gaodeMapConfig.getApiKey());
        params.put("output", "JSON");
        return params;
    }

    private String doGet(String path, Map<String, Object> params) {
        if (!gaodeMapConfig.isEnabled()) {
            log.warn("高德地图服务未启用");
            return "{\"status\":\"0\",\"info\":\"服务未启用\"}";
        }

        String url = gaodeMapConfig.getBaseUrl() + path;
        log.debug("高德地图API请求: {} params={}", path, params);

        try(HttpResponse response = HttpRequest.get(url)
                .form(params)
                .timeout(gaodeMapConfig.getConnectTimeout())
                .execute()) {
            String result = response.body();
            log.debug("高德地图API响应: {}", result.length() > 500 ? result.substring(0, 500) + "..." : result);
            return result;
        } catch (Exception e) {
            log.error("高德地图API调用失败: path={}, error={}", path, e.getMessage());
            return "{\"status\":\"0\",\"info\":\"API调用失败: " + e.getMessage() + "\"}";
        }
    }
}

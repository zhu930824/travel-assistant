package cn.sdh.travel.controller;

import cn.sdh.travel.common.context.UserContext;
import cn.sdh.travel.common.result.Result;
import cn.sdh.travel.entity.dto.request.CreateGuideRequest;
import cn.sdh.travel.entity.dto.response.GuideResponse;
import cn.sdh.travel.service.TravelGuideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/guide")
@RequiredArgsConstructor
public class TravelGuideController {

    private final TravelGuideService guideService;

    @PostMapping
    public Result<GuideResponse> createGuide(@Valid @RequestBody CreateGuideRequest request) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized();
        }
        log.info("创建攻略: userId={}, title={}", userId, request.getTitle());
        GuideResponse response = guideService.createGuide(userId, request);
        return Result.success("创建成功", response);
    }

    @GetMapping("/{id}")
    public Result<GuideResponse> getGuideDetail(@PathVariable Long id) {
        Long currentUserId = UserContext.getCurrentUserId();
        GuideResponse response = guideService.getGuideDetail(id, currentUserId);
        if (response == null) {
            return Result.notFound("攻略不存在");
        }
        return Result.success(response);
    }

    @GetMapping("/recommend")
    public Result<Map<String, Object>> getRecommendGuides(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = UserContext.getCurrentUserId();
        List<GuideResponse> guides = guideService.getRecommendGuides(userId, page, size);

        Map<String, Object> result = new HashMap<>();
        result.put("list", guides);
        result.put("page", page);
        result.put("size", size);
        return Result.success(result);
    }

    @GetMapping("/my")
    public Result<Map<String, Object>> getMyGuides(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized();
        }
        List<GuideResponse> guides = guideService.getUserGuides(userId, page, size);

        Map<String, Object> result = new HashMap<>();
        result.put("list", guides);
        result.put("page", page);
        result.put("size", size);
        return Result.success(result);
    }

    @PostMapping("/{id}/like")
    public Result<Map<String, Object>> toggleLike(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized();
        }
        boolean liked = guideService.toggleLike(id, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("message", liked ? "点赞成功" : "取消点赞");
        return Result.success(result);
    }

    @PostMapping("/{id}/favorite")
    public Result<Map<String, Object>> toggleFavorite(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized();
        }
        boolean favorited = guideService.toggleFavorite(id, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("favorited", favorited);
        result.put("message", favorited ? "收藏成功" : "取消收藏");
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteGuide(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized();
        }
        guideService.deleteGuide(id, userId);
        return Result.success("删除成功", null);
    }
}

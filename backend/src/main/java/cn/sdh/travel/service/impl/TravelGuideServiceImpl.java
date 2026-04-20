package cn.sdh.travel.service.impl;

import cn.sdh.travel.entity.domain.GuideFavorite;
import cn.sdh.travel.entity.domain.GuideLike;
import cn.sdh.travel.entity.domain.TravelGuide;
import cn.sdh.travel.entity.domain.User;
import cn.sdh.travel.entity.dto.request.CreateGuideRequest;
import cn.sdh.travel.entity.dto.response.GuideResponse;
import cn.sdh.travel.mapper.GuideFavoriteMapper;
import cn.sdh.travel.mapper.GuideLikeMapper;
import cn.sdh.travel.mapper.TravelGuideMapper;
import cn.sdh.travel.mapper.UserMapper;
import cn.sdh.travel.service.TravelGuideService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TravelGuideServiceImpl implements TravelGuideService {

    private final TravelGuideMapper guideMapper;
    private final GuideLikeMapper likeMapper;
    private final GuideFavoriteMapper favoriteMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // 推荐算法权重
    private static final double HOT_WEIGHT = 0.6;
    private static final double PERSONAL_WEIGHT = 0.4;

    @Override
    @Transactional
    public GuideResponse createGuide(Long userId, CreateGuideRequest request) {
        TravelGuide guide = new TravelGuide();
        guide.setUserId(userId);
        guide.setTitle(request.getTitle());
        guide.setDestination(request.getDestination());
        guide.setContent(request.getContent());
        guide.setCoverImage(request.getCoverImage());
        guide.setViewCount(0);
        guide.setLikeCount(0);
        guide.setFavoriteCount(0);
        guide.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        guide.setCreateTime(LocalDateTime.now());

        try {
            if (request.getImages() != null && !request.getImages().isEmpty()) {
                guide.setImages(objectMapper.writeValueAsString(request.getImages()));
            }
            if (request.getTags() != null && !request.getTags().isEmpty()) {
                guide.setTags(objectMapper.writeValueAsString(request.getTags()));
            }
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
        }

        guideMapper.insert(guide);
        log.info("创建攻略成功: guideId={}, userId={}", guide.getId(), userId);

        return convertToResponse(guide, userId, false, false);
    }

    @Override
    public GuideResponse getGuideDetail(Long guideId, Long currentUserId) {
        TravelGuide guide = guideMapper.selectById(guideId);
        if (guide == null || guide.getStatus() != 1) {
            return null;
        }

        guideMapper.incrementViewCount(guideId);

        boolean isLiked = false;
        boolean isFavorited = false;
        if (currentUserId != null) {
            isLiked = checkLiked(guideId, currentUserId);
            isFavorited = checkFavorited(guideId, currentUserId);
        }

        GuideResponse response = convertToResponse(guide, currentUserId, isLiked, isFavorited);
        response.setViewCount(guide.getViewCount() + 1);
        return response;
    }

    @Override
    public List<GuideResponse> getRecommendGuides(Long userId, int page, int size) {
        List<TravelGuide> allGuides = guideMapper.selectList(
            new LambdaQueryWrapper<TravelGuide>()
                .eq(TravelGuide::getStatus, 1)
                .orderByDesc(TravelGuide::getCreateTime)
                .last("LIMIT 100")
        );

        if (allGuides.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Integer> destinationPrefs = new HashMap<>();
        if (userId != null) {
            List<Map<String, Object>> prefs = guideMapper.getUserDestinationPreferences(userId, 5);
            for (Map<String, Object> pref : prefs) {
                destinationPrefs.put((String) pref.get("destination"), ((Number) pref.get("cnt")).intValue());
            }
        }

        // 计算推荐分数并排序
        List<TravelGuide> sortedGuides = allGuides.stream()
            .sorted((g1, g2) -> {
                double score1 = calculateRecommendScore(g1, destinationPrefs);
                double score2 = calculateRecommendScore(g2, destinationPrefs);
                return Double.compare(score2, score1);
            })
            .skip((long) page * size)
            .limit(size)
            .collect(Collectors.toList());

        return sortedGuides.stream()
            .map(guide -> {
                boolean isLiked = userId != null && checkLiked(guide.getId(), userId);
                boolean isFavorited = userId != null && checkFavorited(guide.getId(), userId);
                return convertToResponse(guide, userId, isLiked, isFavorited);
            })
            .collect(Collectors.toList());
    }

    private double calculateRecommendScore(TravelGuide guide, Map<String, Integer> destinationPrefs) {
        // 热度分 = viewCount * 0.4 + likeCount * 10 * 0.6
        double hotScore = (guide.getViewCount() * 0.4 + guide.getLikeCount() * 10 * 0.6);

        // 时间衰减因子：发布越久衰减越大
        long daysSinceCreation = Duration.between(guide.getCreateTime(), LocalDateTime.now()).toDays();
        double timeDecay = Math.exp(-daysSinceCreation / 30.0); // 30天半衰期
        hotScore *= timeDecay;

        // 个性化分
        double personalScore = 0;
        if (destinationPrefs.containsKey(guide.getDestination())) {
            personalScore = destinationPrefs.get(guide.getDestination()) * 10;
        }

        // 混合推荐分数
        return HOT_WEIGHT * hotScore + PERSONAL_WEIGHT * personalScore;
    }

    @Override
    public List<GuideResponse> getUserGuides(Long userId, int page, int size) {
        List<TravelGuide> guides = guideMapper.selectList(
            new LambdaQueryWrapper<TravelGuide>()
                .eq(TravelGuide::getUserId, userId)
                .orderByDesc(TravelGuide::getCreateTime)
                .last("LIMIT " + (page * size) + ", " + size)
        );

        return guides.stream()
            .map(guide -> convertToResponse(guide, userId, false, false))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean toggleLike(Long guideId, Long userId) {
        TravelGuide guide = guideMapper.selectById(guideId);
        if (guide == null) {
            return false;
        }

        LambdaQueryWrapper<GuideLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GuideLike::getGuideId, guideId).eq(GuideLike::getUserId, userId);
        GuideLike existing = likeMapper.selectOne(wrapper);

        if (existing != null) {
            likeMapper.deleteById(existing.getId());
            guideMapper.updateLikeCount(guideId, -1);
            return false;
        } else {
            GuideLike like = new GuideLike();
            like.setGuideId(guideId);
            like.setUserId(userId);
            like.setCreateTime(LocalDateTime.now());
            likeMapper.insert(like);
            guideMapper.updateLikeCount(guideId, 1);
            return true;
        }
    }

    @Override
    @Transactional
    public boolean toggleFavorite(Long guideId, Long userId) {
        TravelGuide guide = guideMapper.selectById(guideId);
        if (guide == null) {
            return false;
        }

        LambdaQueryWrapper<GuideFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GuideFavorite::getGuideId, guideId).eq(GuideFavorite::getUserId, userId);
        GuideFavorite existing = favoriteMapper.selectOne(wrapper);

        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            guideMapper.updateFavoriteCount(guideId, -1);
            return false;
        } else {
            GuideFavorite favorite = new GuideFavorite();
            favorite.setGuideId(guideId);
            favorite.setUserId(userId);
            favorite.setCreateTime(LocalDateTime.now());
            favoriteMapper.insert(favorite);
            guideMapper.updateFavoriteCount(guideId, 1);
            return true;
        }
    }

    @Override
    @Transactional
    public void deleteGuide(Long guideId, Long userId) {
        TravelGuide guide = guideMapper.selectById(guideId);
        if (guide != null && guide.getUserId().equals(userId)) {
            guide.setStatus(2);
            guideMapper.updateById(guide);
            log.info("删除攻略: guideId={}, userId={}", guideId, userId);
        }
    }

    private boolean checkLiked(Long guideId, Long userId) {
        LambdaQueryWrapper<GuideLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GuideLike::getGuideId, guideId).eq(GuideLike::getUserId, userId);
        return likeMapper.selectCount(wrapper) > 0;
    }

    private boolean checkFavorited(Long guideId, Long userId) {
        LambdaQueryWrapper<GuideFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GuideFavorite::getGuideId, guideId).eq(GuideFavorite::getUserId, userId);
        return favoriteMapper.selectCount(wrapper) > 0;
    }

    private GuideResponse convertToResponse(TravelGuide guide, Long currentUserId, boolean isLiked, boolean isFavorited) {
        User user = userMapper.selectById(guide.getUserId());

        List<String> images = parseJsonList(guide.getImages());
        List<String> tags = parseJsonList(guide.getTags());

        return GuideResponse.builder()
            .id(guide.getId())
            .userId(guide.getUserId())
            .username(user != null ? user.getNickname() != null ? user.getNickname() : user.getUsername() : "未知用户")
            .userAvatar(user != null ? user.getAvatar() : null)
            .title(guide.getTitle())
            .destination(guide.getDestination())
            .content(guide.getContent())
            .coverImage(guide.getCoverImage())
            .images(images)
            .tags(tags)
            .viewCount(guide.getViewCount())
            .likeCount(guide.getLikeCount())
            .favoriteCount(guide.getFavoriteCount())
            .isLiked(isLiked)
            .isFavorited(isFavorited)
            .createTime(guide.getCreateTime())
            .createTimeStr(guide.getCreateTime() != null ? guide.getCreateTime().format(DATE_FORMATTER) : "")
            .build();
    }

    private List<String> parseJsonList(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }
}

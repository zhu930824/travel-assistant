package cn.sdh.travel.service;

import cn.sdh.travel.entity.dto.request.CreateGuideRequest;
import cn.sdh.travel.entity.dto.response.GuideResponse;

import java.util.List;

public interface TravelGuideService {

    GuideResponse createGuide(Long userId, CreateGuideRequest request);

    GuideResponse getGuideDetail(Long guideId, Long currentUserId);

    List<GuideResponse> getRecommendGuides(Long userId, int page, int size);

    List<GuideResponse> getUserGuides(Long userId, int page, int size);

    boolean toggleLike(Long guideId, Long userId);

    boolean toggleFavorite(Long guideId, Long userId);

    void deleteGuide(Long guideId, Long userId);
}

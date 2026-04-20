package cn.sdh.travel.entity.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class GuideResponse {

    private Long id;

    private Long userId;

    private String username;

    private String userAvatar;

    private String title;

    private String destination;

    private String content;

    private String coverImage;

    private List<String> images;

    private List<String> tags;

    private Integer viewCount;

    private Integer likeCount;

    private Integer favoriteCount;

    private Boolean isLiked;

    private Boolean isFavorited;

    private LocalDateTime createTime;

    private String createTimeStr;
}

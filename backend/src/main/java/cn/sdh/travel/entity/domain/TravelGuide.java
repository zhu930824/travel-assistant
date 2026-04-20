package cn.sdh.travel.entity.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("travel_guide")
public class TravelGuide {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String destination;

    private String content;

    private String coverImage;

    private String images;

    private String tags;

    private Integer viewCount;

    private Integer likeCount;

    private Integer favoriteCount;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

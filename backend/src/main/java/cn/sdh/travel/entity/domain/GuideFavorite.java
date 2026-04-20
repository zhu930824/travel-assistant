package cn.sdh.travel.entity.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("guide_favorite")
public class GuideFavorite {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long guideId;

    private Long userId;

    private LocalDateTime createTime;
}

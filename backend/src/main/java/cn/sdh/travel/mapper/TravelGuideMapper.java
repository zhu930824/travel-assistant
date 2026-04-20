package cn.sdh.travel.mapper;

import cn.sdh.travel.entity.domain.TravelGuide;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TravelGuideMapper extends BaseMapper<TravelGuide> {

    @Update("UPDATE travel_guide SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(Long id);

    @Update("UPDATE travel_guide SET like_count = like_count + #{delta} WHERE id = #{id}")
    int updateLikeCount(Long id, int delta);

    @Update("UPDATE travel_guide SET favorite_count = favorite_count + #{delta} WHERE id = #{id}")
    int updateFavoriteCount(Long id, int delta);

    @Select("SELECT destination, COUNT(*) AS cnt FROM travel_guide WHERE user_id = #{userId} AND status = 1 GROUP BY destination ORDER BY cnt DESC LIMIT #{limit}")
    java.util.List<java.util.Map<String, Object>> getUserDestinationPreferences(Long userId, int limit);
}

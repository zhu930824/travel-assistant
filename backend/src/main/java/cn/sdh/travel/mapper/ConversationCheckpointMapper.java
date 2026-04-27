package cn.sdh.travel.mapper;

import cn.sdh.travel.entity.domain.ConversationCheckpoint;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 对话Checkpoint Mapper接口
 */
@Mapper
public interface ConversationCheckpointMapper extends BaseMapper<ConversationCheckpoint> {

    /**
     * 根据sessionId查询checkpoint
     */
    @Select("SELECT * FROM conversation_checkpoint WHERE session_id = #{sessionId}")
    ConversationCheckpoint findBySessionId(@Param("sessionId") String sessionId);

    /**
     * 根据用户ID查询活跃的checkpoint列表
     */
    @Select("SELECT * FROM conversation_checkpoint WHERE user_id = #{userId} AND status IN ('active', 'paused') ORDER BY update_time DESC")
    List<ConversationCheckpoint> findActiveByUserId(@Param("userId") Long userId);

    /**
     * 更新状态
     */
    @Update("UPDATE conversation_checkpoint SET status = #{status}, update_time = NOW() WHERE session_id = #{sessionId}")
    int updateStatus(@Param("sessionId") String sessionId, @Param("status") String status);

    /**
     * 更新消息列表和状态
     */
    @Update("UPDATE conversation_checkpoint SET messages = #{messages}, current_round = #{currentRound}, last_speaker = #{lastSpeaker}, terminated = #{terminated}, termination_reason = #{terminationReason}, status = #{status}, update_time = NOW() WHERE session_id = #{sessionId}")
    int updateCheckpoint(@Param("sessionId") String sessionId,
                         @Param("messages") String messages,
                         @Param("currentRound") Integer currentRound,
                         @Param("lastSpeaker") String lastSpeaker,
                         @Param("terminated") Boolean terminated,
                         @Param("terminationReason") String terminationReason,
                         @Param("status") String status);
}

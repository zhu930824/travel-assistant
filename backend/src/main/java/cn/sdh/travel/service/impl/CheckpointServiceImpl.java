package cn.sdh.travel.service.impl;

import cn.sdh.travel.agent.autogen.ConversationState;
import cn.sdh.travel.entity.domain.ConversationCheckpoint;
import cn.sdh.travel.mapper.ConversationCheckpointMapper;
import cn.sdh.travel.service.CheckpointService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 对话Checkpoint服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckpointServiceImpl implements CheckpointService {

    private final ConversationCheckpointMapper checkpointMapper;
    private final ObjectMapper objectMapper;

    public CheckpointServiceImpl(ConversationCheckpointMapper checkpointMapper) {
        this.checkpointMapper = checkpointMapper;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    @Transactional
    public String createCheckpoint(Long userId, String destination, int days,
                                   String budget, String preferences, String humanInputMode) {
        String sessionId = generateSessionId();

        ConversationCheckpoint checkpoint = new ConversationCheckpoint();
        checkpoint.setSessionId(sessionId);
        checkpoint.setUserId(userId);
        checkpoint.setDestination(destination);
        checkpoint.setDays(days);
        checkpoint.setBudget(budget);
        checkpoint.setPreferences(preferences);
        checkpoint.setHumanInputMode(humanInputMode != null ? humanInputMode : "TERMINATE");
        checkpoint.setCurrentRound(0);
        checkpoint.setTerminated(false);
        checkpoint.setStatus("active");
        checkpoint.setCreateTime(LocalDateTime.now());
        checkpoint.setUpdateTime(LocalDateTime.now());

        checkpointMapper.insert(checkpoint);
        log.info("创建Checkpoint: sessionId={}, userId={}, destination={}", sessionId, userId, destination);

        return sessionId;
    }

    @Override
    @Transactional
    public void saveState(String sessionId, ConversationState state) {
        ConversationCheckpoint checkpoint = checkpointMapper.findBySessionId(sessionId);
        if (checkpoint == null) {
            log.warn("Checkpoint不存在: sessionId={}", sessionId);
            return;
        }

        String messagesJson = serializeMessages(state);
        String status = state.isTerminated() ? "completed" : "active";

        checkpointMapper.updateCheckpoint(
            sessionId,
            messagesJson,
            state.getCurrentRound(),
            state.getLastSpeaker(),
            state.isTerminated(),
            state.getTerminationReason(),
            status
        );

        log.debug("保存Checkpoint状态: sessionId={}, round={}, status={}", sessionId, state.getCurrentRound(), status);
    }

    @Override
    public ConversationState loadState(String sessionId) {
        ConversationCheckpoint checkpoint = checkpointMapper.findBySessionId(sessionId);
        if (checkpoint == null) {
            log.warn("Checkpoint不存在: sessionId={}", sessionId);
            return null;
        }

        ConversationState state = new ConversationState();
        if (checkpoint.getMessages() != null && !checkpoint.getMessages().isEmpty()) {
            state = ConversationState.fromJson(checkpoint.getMessages());
        }

        log.info("加载Checkpoint状态: sessionId={}, round={}, messages={}",
            sessionId, state.getCurrentRound(), state.getMessages().size());

        return state;
    }

    @Override
    @Transactional
    public void pauseSession(String sessionId) {
        checkpointMapper.updateStatus(sessionId, "paused");
        log.info("暂停对话: sessionId={}", sessionId);
    }

    @Override
    @Transactional
    public void resumeSession(String sessionId) {
        checkpointMapper.updateStatus(sessionId, "active");
        log.info("恢复对话: sessionId={}", sessionId);
    }

    @Override
    @Transactional
    public void completeSession(String sessionId, String terminationReason) {
        ConversationCheckpoint checkpoint = checkpointMapper.findBySessionId(sessionId);
        if (checkpoint == null) {
            log.warn("Checkpoint不存在: sessionId={}", sessionId);
            return;
        }

        checkpointMapper.updateCheckpoint(
            sessionId,
            checkpoint.getMessages(),
            checkpoint.getCurrentRound(),
            checkpoint.getLastSpeaker(),
            true,
            terminationReason,
            "completed"
        );

        log.info("完成对话: sessionId={}, reason={}", sessionId, terminationReason);
    }

    @Override
    public ConversationCheckpoint getCheckpoint(String sessionId) {
        return checkpointMapper.findBySessionId(sessionId);
    }

    @Override
    public List<ConversationCheckpoint> getUserActiveCheckpoints(Long userId) {
        return checkpointMapper.findActiveByUserId(userId);
    }

    @Override
    @Transactional
    public boolean deleteCheckpoint(String sessionId) {
        ConversationCheckpoint checkpoint = checkpointMapper.findBySessionId(sessionId);
        if (checkpoint == null) {
            return false;
        }

        int rows = checkpointMapper.deleteById(checkpoint.getId());
        log.info("删除Checkpoint: sessionId={}, result={}", sessionId, rows > 0);
        return rows > 0;
    }

    private String generateSessionId() {
        return "cp_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private String serializeMessages(ConversationState state) {
        try {
            return objectMapper.writeValueAsString(state.getMessages());
        } catch (JsonProcessingException e) {
            log.error("序列化消息失败", e);
            return "[]";
        }
    }
}

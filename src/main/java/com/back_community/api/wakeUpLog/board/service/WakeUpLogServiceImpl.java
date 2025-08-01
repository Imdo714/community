package com.back_community.api.wakeUpLog.board.service;

import com.back_community.api.common.embedded.board.Board;
import com.back_community.api.common.page.PageInfo;
import com.back_community.api.user.domain.entity.User;
import com.back_community.api.wakeUpLog.board.domain.dto.request.*;
import com.back_community.api.wakeUpLog.board.domain.dto.response.CreateWakeUpResponse;
import com.back_community.api.wakeUpLog.board.domain.dto.response.WakeUpLogDetailResponse;
import com.back_community.api.wakeUpLog.board.domain.dto.response.WakeUpLogListResponse;
import com.back_community.api.wakeUpLog.board.domain.entity.WakeUpLog;
import com.back_community.api.wakeUpLog.dao.WakeUpLogDao;
import com.back_community.global.exception.handleException.MismatchException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WakeUpLogServiceImpl implements WakeUpLogService {

    private final WakeUpLogDao wakeUpLogDao;

    @Override
    @Transactional
    public CreateWakeUpResponse createWakeUpLog(CreateWakeUpLogDto createWakeUpLogDto, Long userId) {
        // 아침에 기상 인증을 다른 사람들과 동시에 할 가능성이 있다고 봄
        // wakeUpStreak 가 레이스 컨디션이 발생하지 않고 정확히 변경 되어야 함
        User user = wakeUpLogDao.getUserLock(userId);
        wakeUpLogDao.validateNotWakeUpLogToday(userId);

        Board board = Board.builderBoard(createWakeUpLogDto.getTitle(), createWakeUpLogDto.getContent());
        WakeUpLog wakeUpLog = WakeUpLog.builderWakeUpLog(user, board);

        WakeUpLog saved = wakeUpLogDao.saveWakeUpLog(wakeUpLog);

        // 기상 게시물을 삭제하고 재업로드 반복하면 연속인증 수가 계속 증가 함
        // 게시물을 올려야 wake_up_streak 을 계산하기 때문에 기상 인증 안 한 날이있어도 기존 wake_up_streak 값이 그대로 유지 됨
        yesterdayWakeUpLogStreak(userId, user);
        return CreateWakeUpResponse.createWakeUpSuccess(saved.getWakeUpId(), user.getWakeUpStreak());
    }

    @Override
    public WakeUpLogListResponse getWakeUpLogList(int page, int size) {
        Page<WakeUpListDto> wakeUpLogList2 = wakeUpLogDao.getWakeUpLogList(page, size);

        List<WakeUpListDto> list = wakeUpLogList2.getContent();
        PageInfo pageInfo = PageInfo.pageBuilder(wakeUpLogList2);

        return WakeUpLogListResponse.builder()
                .wakeUpLists(list)
                .pageable(pageInfo)
                .build();
    }

    @Override
    public WakeUpLogDetailResponse wakeUpLogDetail(Long logId) {
        WakeUpLog wakeUpLogDetail = wakeUpLogDao.getWakeUpLog(logId);
        int countWakeUpLogLikes = wakeUpLogDao.getCountWakeUpLogLikes(logId);

        return WakeUpLogDetailResponse.of(wakeUpLogDetail, countWakeUpLogLikes);
    }

    @Override
    @Transactional
    public WakeUpLogDetailResponse wakeUpLogUpdate(Long logId, Long userId, UpdateWakeUpLogDto updateWakeUpLogDto) {
        WakeUpLog wakeUpLog = validateWakeUpUserIsOwner(userId, logId);
        int countWakeUpLogLikes = wakeUpLogDao.getCountWakeUpLogLikes(logId);

        wakeUpLog.getBoard().updateBoard(updateWakeUpLogDto);
        return WakeUpLogDetailResponse.of(wakeUpLog, countWakeUpLogLikes);
    }

    @Override
    @Transactional
    public void wakeUpLogDelete(Long logId, Long userId) {
        WakeUpLog wakeUpLog = validateWakeUpUserIsOwner(userId, logId);
        wakeUpLogDao.deleteWakeUpLog(wakeUpLog.getWakeUpId());
    }

    private void yesterdayWakeUpLogStreak(Long userId, User user) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime startOfYesterday = yesterday.atStartOfDay();
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        user.wakeUpStreakUpdate(wakeUpLogDao.yesterdayByUserIdAndDate(userId, startOfYesterday, startOfToday));
    }

    private WakeUpLog validateWakeUpUserIsOwner(Long userId, Long logId) {
        WakeUpLog wakeUpLog = wakeUpLogDao.getWakeUpLog(logId);

        if (!userId.equals(wakeUpLog.getUser().getUserId())) {
            throw new MismatchException("작성자가 다릅니다!");
        }

        return wakeUpLog;
    }
}

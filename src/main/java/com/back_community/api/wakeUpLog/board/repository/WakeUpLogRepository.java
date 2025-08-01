package com.back_community.api.wakeUpLog.board.repository;

import com.back_community.api.wakeUpLog.board.domain.dto.request.WakeUpListDto;
import com.back_community.api.wakeUpLog.board.domain.entity.WakeUpLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface WakeUpLogRepository extends JpaRepository<WakeUpLog, Long> {

    @Query("SELECT COUNT(w) > 0 FROM WakeUpLog w WHERE w.user.userId = :userId AND DATE(w.board.createDate) = CURRENT_DATE")
    Boolean countTodayLogsByUserId(Long userId);

    @Query("SELECT COUNT(w) > 0 FROM WakeUpLog w WHERE w.user.userId = :userId AND w.board.createDate >= :startOfYesterday AND w.board.createDate < :startOfToday")
    Boolean yesterdayByUserIdAndDate(Long userId, LocalDateTime startOfYesterday, LocalDateTime startOfToday);

    @Query("SELECT new com.back_community.api.wakeUpLog.board.domain.dto.request.WakeUpListDto(w.wakeUpId, w.user.name, w.user.imageUrl, w.board.title, w.board.createDate, COUNT(DISTINCT wl), COUNT(DISTINCT wc)) " +
            "FROM WakeUpLog w JOIN w.user u " +
            "LEFT JOIN WakeUpLike wl ON w.wakeUpId = wl.wakeUpLog.wakeUpId " +
            "LEFT JOIN WakeUpComment wc ON w.wakeUpId = wc.wakeUpLog.wakeUpId " +
            "GROUP BY w.wakeUpId, u.name, u.imageUrl, w.board.title, w.board.createDate " +
            "ORDER BY w.wakeUpId DESC")
    Page<WakeUpListDto> findWakeUpLogs(Pageable pageable);

}

package com.back_community.api.wakeUpLog.comment.domain.dto.response;

import com.back_community.api.common.page.PageInfo;
import com.back_community.api.wakeUpLog.comment.domain.entity.WakeUpComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Builder
@AllArgsConstructor
public class CommentListResponse {

    private List<CommentList> wakeUpCommentLists;
    private PageInfo pageable;

    @Getter
    @Builder
    public static class CommentList {
        private Long commentId;
        private String imageUrl;
        private String content;
        private LocalDateTime createDate;
    }

    public static CommentListResponse commentListBuilder(Page<WakeUpComment> comments){
        List<CommentListResponse.CommentList> list = comments.getContent().stream()
                .map(res -> CommentList.builder()
                        .commentId(res.getCommentId())
                        .imageUrl(res.getUser().getImageUrl())
                        .content(res.getComment().getContent())
                        .createDate(res.getComment().getCreateDate())
                        .build())
                .toList();

        PageInfo pageInfo = PageInfo.pageBuilder(comments);

        return CommentListResponse.builder()
                .wakeUpCommentLists(list)
                .pageable(pageInfo)
                .build();
    }

}

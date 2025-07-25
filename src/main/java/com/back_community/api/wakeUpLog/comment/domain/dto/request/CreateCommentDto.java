package com.back_community.api.wakeUpLog.comment.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CreateCommentDto {

    @NotBlank(message = "댓글은 필수입니다.")
    private String content;
}

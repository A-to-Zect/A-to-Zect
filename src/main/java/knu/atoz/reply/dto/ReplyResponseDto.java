package knu.atoz.reply.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReplyResponseDto {
    private Long id;
    private Long memberId;
    private String content;
    private LocalDateTime modifiedAt;
    private String name;
}

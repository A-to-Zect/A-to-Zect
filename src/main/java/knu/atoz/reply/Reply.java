package knu.atoz.reply;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Reply {
    private Long id;
    private Long postId;
    private Long memberId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public Reply(Long id, Long postId, Long memberId, String content, LocalDateTime createdAt){
        this.id = id;
        this.postId = postId;
        this.memberId = memberId;
        this.content = content;
        this.createdAt = createdAt;
        this.modifiedAt = LocalDateTime.now();
    }

    public Reply(Long postId, Long memberId, String content) {
        this.id = null;
        this.postId = postId;
        this.memberId = memberId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
    }
}

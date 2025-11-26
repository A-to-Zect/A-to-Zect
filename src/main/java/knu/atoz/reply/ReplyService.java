package knu.atoz.reply;

import knu.atoz.reply.dto.ReplyRequestDto;
import knu.atoz.reply.dto.ReplyResponseDto;
import knu.atoz.reply.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyRepository replyRepository;

    // 댓글 목록 조회 (작성자 이름 포함)
    public List<ReplyResponseDto> getReplyList(Long postId) {
        return replyRepository.findAllByPostId(postId);
    }

    // 댓글 단건 조회 (수정/삭제 권한 체크용)
    public Reply getReply(Long replyId) {
        Reply reply = replyRepository.findById(replyId);
        if (reply == null) throw new ReplyNotFoundException();
        return reply;
    }

    // 댓글 작성
    public void createReply(Long postId, Long memberId, ReplyRequestDto requestDto) {
        Reply reply = new Reply(postId, memberId, requestDto.getContent());
        replyRepository.save(reply);
    }

    // 댓글 수정
    public void updateReply(Long replyId, Long memberId, ReplyRequestDto requestDto) {
        Reply original = getReply(replyId); // 존재 확인

        if (!original.getMemberId().equals(memberId)) {
            throw new UnauthorizedReplyAccessException();
        }

        Reply updateReply = new Reply(
                original.getId(),
                original.getPostId(),
                memberId,
                requestDto.getContent(),
                original.getCreatedAt(),
                LocalDateTime.now() // 수정 시간 갱신
        );
        replyRepository.update(updateReply);
    }

    // 댓글 삭제
    public void deleteReply(Long replyId, Long memberId) {
        Reply original = getReply(replyId);
        if (!original.getMemberId().equals(memberId)) {
            throw new UnauthorizedReplyAccessException();
        }
        replyRepository.delete(replyId);
    }
}
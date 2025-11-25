package knu.atoz.reply;

import jakarta.servlet.http.HttpSession;
import knu.atoz.member.Member;
import knu.atoz.reply.dto.ReplyRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/projects/{projectId}/posts/{postId}/replies")
@RequiredArgsConstructor
public class ReplyController {

    private final ReplyService replyService;

    // 1. 댓글 작성 (POST)
    @PostMapping
    public String createReply(@PathVariable Long projectId,
                              @PathVariable Long postId,
                              @ModelAttribute ReplyRequestDto dto,
                              HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        try {
            replyService.createReply(postId, loginMember.getId(), dto);
            return "redirect:/projects/" + projectId + "/posts/" + postId; // 게시글 상세로 복귀
        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "/posts/" + postId + "?error=" + encode(e.getMessage());
        }
    }

    // 2. 댓글 삭제 (POST)
    @PostMapping("/{replyId}/delete")
    public String deleteReply(@PathVariable Long projectId,
                              @PathVariable Long postId,
                              @PathVariable Long replyId,
                              HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        try {
            replyService.deleteReply(replyId, loginMember.getId());
            return "redirect:/projects/" + projectId + "/posts/" + postId;
        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "/posts/" + postId + "?error=" + encode(e.getMessage());
        }
    }

    // (댓글 수정은 모달이나 인라인 폼이 필요하지만, 간단히 구현하려면 별도 페이지보다는 삭제 후 다시 쓰는 게 나을 수도 있음.
    // 여기서는 일단 생략하거나, 필요시 별도 수정 로직 추가 가능)

    private String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}
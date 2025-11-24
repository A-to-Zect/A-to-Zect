package knu.atoz.techspec.member;

import jakarta.servlet.http.HttpSession;
import knu.atoz.member.Member;
import knu.atoz.techspec.Techspec;
import knu.atoz.techspec.exception.TechspecException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/members/techspecs")
@RequiredArgsConstructor
public class MemberTechspecController {

    private final MemberTechspecService memberTechspecService;

    // 1. 내 기술 스택 관리 페이지 (목록 + 추가 폼)
    @GetMapping
    public String showTechspecManagePage(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        // 내 스택 목록 조회
        List<Techspec> myTechspecs = memberTechspecService.getMyTechspecs(loginMember.getId());
        model.addAttribute("techspecs", myTechspecs);

        return "member/techspec"; // 뷰 파일 이름
    }

    // 2. 스택 추가 (POST)
    @PostMapping("/add")
    public String addTechspec(@RequestParam String techName, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        try {
            memberTechspecService.addTechspec(loginMember.getId(), techName.trim());
            return "redirect:/members/techspecs";
        } catch (TechspecException e) {
            return "redirect:/members/techspecs?error=" + encode(e.getMessage());
        }
    }

    // 3. 스택 삭제 (POST)
    @PostMapping("/{techspecId}/delete")
    public String removeTechspec(@PathVariable Long techspecId, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        try {
            memberTechspecService.removeTechspec(loginMember.getId(), techspecId);
            return "redirect:/members/techspecs";
        } catch (Exception e) {
            return "redirect:/members/techspecs?error=" + encode(e.getMessage());
        }
    }

    private String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}
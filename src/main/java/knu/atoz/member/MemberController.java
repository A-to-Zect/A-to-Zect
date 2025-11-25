package knu.atoz.member;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import knu.atoz.member.dto.*;
import knu.atoz.project.Project;
import knu.atoz.project.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final ProjectService projectService;

    // 1. 회원가입
    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("signupDto", new SignupRequestDto());
        return "member/signup";
    }

    @PostMapping("/signup")
    public String processSignup(@Valid @ModelAttribute("signupDto") SignupRequestDto dto,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            return "member/signup";
        }
        try {
            memberService.signUp(dto);
            return "redirect:/members/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "member/signup";
        }
    }

    // 2. 로그인
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginDto", new LoginRequestDto());
        return "member/login";
    }

    @PostMapping("/login")
    // [수정] "loginDto" 이름 명시
    public String processLogin(@Valid @ModelAttribute("loginDto") LoginRequestDto dto,
                               BindingResult bindingResult,
                               HttpSession session,
                               Model model) {
        if (bindingResult.hasErrors()) {
            return "member/login";
        }
        try {
            Member member = memberService.login(dto.getEmail(), dto.getPassword());
            session.setAttribute("loginMember", member);
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
            return "member/login";
        }
    }

    // 3. 로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // 4. 마이페이지
    @GetMapping("/mypage")
    public String showMyInfo(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        MemberInfoResponseDto infoDto = memberService.getAllInfo(loginMember.getId());
        List<Project> myProjects = projectService.getMyProjectList(loginMember.getId());
        infoDto.setProjects(myProjects);

        model.addAttribute("info", infoDto);
        return "member/mypage";
    }

    // 5. 정보 수정
    @GetMapping("/edit")
    public String showEditForm(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        MemberUpdateRequestDto dto = new MemberUpdateRequestDto(
                loginMember.getEmail(),
                loginMember.getName(),
                loginMember.getBirthDate()
        );
        // GET에서 "updateDto"라는 이름으로 보냈으므로...
        model.addAttribute("updateDto", dto);
        return "member/edit";
    }

    @PostMapping("/edit")
    // [중요 수정] 1. "updateDto" 이름 명시  2. BindingResult 위치 변경 (DTO 바로 뒤)
    public String updateMember(@Valid @ModelAttribute("updateDto") MemberUpdateRequestDto dto,
                               BindingResult bindingResult,
                               HttpSession session,
                               Model model) {
        if (bindingResult.hasErrors()) {
            return "member/edit";
        }

        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        try {
            Member updatedMember = memberService.updateMember(loginMember.getId(), dto);
            session.setAttribute("loginMember", updatedMember);
            return "redirect:/members/mypage";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "member/edit";
        }
    }

    // 6. 비밀번호 변경
    @GetMapping("/password")
    public String showPasswordChangeForm(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        // GET에서 "passwordDto"로 보냄
        model.addAttribute("passwordDto", new PasswordUpdateRequestDto());
        return "member/edit-password";
    }

    @PostMapping("/password")
    // [수정] "passwordDto" 이름 명시
    public String processPasswordChange(@Valid @ModelAttribute("passwordDto") PasswordUpdateRequestDto dto,
                                        BindingResult bindingResult,
                                        HttpSession session,
                                        Model model) {
        if (bindingResult.hasErrors()) {
            return "member/edit-password"; // 템플릿 파일명 확인 필요 (password.html인지 edit-password.html인지)
        }

        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        try {
            memberService.editPassword(loginMember.getId(), dto);
            return "redirect:/members/mypage";
        } catch (Exception e) {
            model.addAttribute("error", "비밀번호 변경 실패: " + e.getMessage());
            return "member/edit-password";
        }
    }

    // 7. 탈퇴
    @PostMapping("/withdraw")
    public String withdraw(HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        try {
            memberService.delete(loginMember.getId());
            session.invalidate();
            return "redirect:/";
        } catch (Exception e) {
            return "redirect:/members/mypage?error=" + URLEncoder.encode("탈퇴 처리에 실패했습니다.", StandardCharsets.UTF_8);
        }
    }
}
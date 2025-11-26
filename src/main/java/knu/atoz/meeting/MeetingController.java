package knu.atoz.meeting;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import knu.atoz.meeting.dto.MeetingRequestDto;
import knu.atoz.member.Member;
import knu.atoz.participant.ParticipantService;
import knu.atoz.project.Project;
import knu.atoz.project.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/projects/{projectId}/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;
    private final ProjectService projectService;
    private final ParticipantService participantService;

    @GetMapping
    public String listMeetings(@PathVariable Long projectId,
                               HttpSession session,
                               Model model) {
        Member loginMember = getLoginMember(session);
        if (loginMember == null) return "redirect:/members/login";

        if (!isTeamMember(projectId, loginMember.getId())) {
            return "redirect:/projects/" + projectId + "?error=" + encode("접근 권한이 없습니다.");
        }

        try {
            Project project = projectService.getProject(projectId);
            List<Meeting> meetings = meetingService.getMeetingsByProject(projectId);

            model.addAttribute("project", project);
            model.addAttribute("meetings", meetings);

            return "meeting/list";

        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "?error=" + encode(e.getMessage());
        }
    }

    @GetMapping("/new")
    public String showCreateForm(@PathVariable Long projectId,
                                 HttpSession session,
                                 Model model) {
        Member loginMember = getLoginMember(session);
        if (loginMember == null) return "redirect:/members/login";

        if (!isTeamMember(projectId, loginMember.getId())) {
            return "redirect:/projects/" + projectId + "/meetings?error=" + encode("회의록 작성 권한이 없습니다.");
        }

        Project project = projectService.getProject(projectId);

        model.addAttribute("project", project);
        model.addAttribute("meetingDto", new MeetingRequestDto());
        model.addAttribute("isNew", true);

        return "meeting/form";
    }

    @PostMapping("/new")
    public String createMeeting(@PathVariable Long projectId,
                                @Valid @ModelAttribute("meetingDto") MeetingRequestDto dto,
                                BindingResult bindingResult,
                                HttpSession session,
                                Model model) {
        Member loginMember = getLoginMember(session);
        if (loginMember == null) return "redirect:/members/login";

        if (bindingResult.hasErrors()) {
            model.addAttribute("project", projectService.getProject(projectId));
            model.addAttribute("isNew", true);
            return "meeting/form";
        }

        try {
            meetingService.createMeeting(projectId, dto);
            return "redirect:/projects/" + projectId + "/meetings";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("project", projectService.getProject(projectId));
            model.addAttribute("isNew", true);
            return "meeting/form";
        }
    }

    // 4. 수정 폼
    @GetMapping("/{meetingId}/edit")
    public String showEditForm(@PathVariable Long projectId,
                               @PathVariable Long meetingId,
                               HttpSession session,
                               Model model) {
        Member loginMember = getLoginMember(session);
        if (loginMember == null) return "redirect:/members/login";

        if (!isTeamMember(projectId, loginMember.getId())) {
            return "redirect:/projects/" + projectId + "/meetings?error=" + encode("수정 권한이 없습니다.");
        }

        try {
            Project project = projectService.getProject(projectId);
            Meeting meeting = meetingService.getMeeting(meetingId);

            MeetingRequestDto dto = new MeetingRequestDto(
                    meeting.getTitle(),
                    meeting.getDescription(),
                    meeting.getStartTime(),
                    meeting.getEndTime()
            );

            model.addAttribute("project", project);
            model.addAttribute("meeting", meeting);
            model.addAttribute("meetingDto", dto);
            model.addAttribute("isNew", false);

            return "meeting/form";

        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "/meetings?error=" + encode(e.getMessage());
        }
    }

    @PostMapping("/{meetingId}/edit")
    public String updateMeeting(@PathVariable Long projectId,
                                @PathVariable Long meetingId,
                                @Valid @ModelAttribute("meetingDto") MeetingRequestDto dto,
                                BindingResult bindingResult,
                                HttpSession session,
                                Model model) {
        Member loginMember = getLoginMember(session);
        if (loginMember == null) return "redirect:/members/login";

        if (bindingResult.hasErrors()) {
            model.addAttribute("project", projectService.getProject(projectId));
            model.addAttribute("isNew", false);
            try {
                model.addAttribute("meeting", meetingService.getMeeting(meetingId));
            } catch (Exception ignored) {}

            return "meeting/form";
        }

        try {
            meetingService.updateMeeting(meetingId, projectId, dto);
            return "redirect:/projects/" + projectId + "/meetings";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("project", projectService.getProject(projectId));
            model.addAttribute("isNew", false);

            try {
                model.addAttribute("meeting", meetingService.getMeeting(meetingId));
            } catch (Exception ignored) {}

            return "meeting/form";
        }
    }

    @PostMapping("/{meetingId}/delete")
    public String deleteMeeting(@PathVariable Long projectId,
                                @PathVariable Long meetingId,
                                HttpSession session) {
        Member loginMember = getLoginMember(session);
        if (loginMember == null) return "redirect:/members/login";

        if (!isTeamMember(projectId, loginMember.getId())) {
            return "redirect:/projects/" + projectId + "/meetings?error=" + encode("삭제 권한이 없습니다.");
        }

        try {
            meetingService.deleteMeeting(meetingId, projectId);
        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "/meetings?error=" + encode(e.getMessage());
        }
        return "redirect:/projects/" + projectId + "/meetings";
    }

    private Member getLoginMember(HttpSession session) {
        return (Member) session.getAttribute("loginMember");
    }

    private boolean isTeamMember(Long projectId, Long memberId) {
        String role = participantService.getMyRole(projectId, memberId);
        return "LEADER".equals(role) || "MEMBER".equals(role);
    }

    private String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}
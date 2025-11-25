package knu.atoz.document;

import jakarta.servlet.http.HttpSession;
import knu.atoz.document.dto.DocumentRequestDto;
import knu.atoz.member.Member;
import knu.atoz.participant.ParticipantService;
import knu.atoz.project.Project;
import knu.atoz.project.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/projects/{projectId}/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final ProjectService projectService;
    private final ParticipantService participantService;

    // 1. 문서 목록 조회
    @GetMapping
    public String listDocuments(@PathVariable Long projectId,
                                HttpSession session,
                                Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        // 권한 체크: 팀원(LEADER, MEMBER)만 접근 가능
        String myRole = participantService.getMyRole(projectId, loginMember.getId());
        if (myRole == null || "PENDING".equals(myRole)) {
            return "redirect:/projects/" + projectId + "?error=" + encode("접근 권한이 없습니다.");
        }

        try {
            Project project = projectService.getProject(projectId);
            List<Document> documents = documentService.getDocumentsByProject(projectId);

            model.addAttribute("project", project);
            model.addAttribute("documents", documents);

            return "document/list"; // templates/document/list.html

        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "?error=" + encode(e.getMessage());
        }
    }

    // 2. 문서 등록 폼 (GET)
    @GetMapping("/new")
    public String showCreateForm(@PathVariable Long projectId,
                                 HttpSession session,
                                 Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        Project project = projectService.getProject(projectId);

        model.addAttribute("project", project);
        model.addAttribute("documentDto", new DocumentRequestDto());
        model.addAttribute("isNew", true); // 화면 타이틀 제어용

        return "document/form"; // templates/document/form.html
    }

    // 3. 문서 등록 처리 (POST)
    @PostMapping("/new")
    public String createDocument(@PathVariable Long projectId,
                                 @ModelAttribute DocumentRequestDto dto,
                                 HttpSession session,
                                 Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        try {
            documentService.createDocument(projectId, dto);
            return "redirect:/projects/" + projectId + "/documents";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("project", projectService.getProject(projectId));
            model.addAttribute("documentDto", dto);
            model.addAttribute("isNew", true);
            return "document/form";
        }
    }

    // 4. 문서 수정 폼 (GET)
    @GetMapping("/{docId}/edit")
    public String showEditForm(@PathVariable Long projectId,
                               @PathVariable Long docId,
                               HttpSession session,
                               Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        try {
            Project project = projectService.getProject(projectId);
            Document document = documentService.getDocument(docId);

            // 기존 값 채우기
            DocumentRequestDto dto = new DocumentRequestDto(document.getTitle(), document.getLocation());

            model.addAttribute("project", project);
            model.addAttribute("document", document); // ID 등 참조용
            model.addAttribute("documentDto", dto);
            model.addAttribute("isNew", false);

            return "document/form";

        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "/documents?error=" + encode(e.getMessage());
        }
    }

    // 5. 문서 수정 처리 (POST)
    @PostMapping("/{docId}/edit")
    public String updateDocument(@PathVariable Long projectId,
                                 @PathVariable Long docId,
                                 @ModelAttribute DocumentRequestDto dto,
                                 HttpSession session,
                                 Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        try {
            documentService.updateDocument(docId, projectId, dto);
            return "redirect:/projects/" + projectId + "/documents";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("project", projectService.getProject(projectId));
            // 에러 발생 시 기존 document 정보 다시 조회해서 넣어줘야 함 (ID 유지를 위해)
            try {
                model.addAttribute("document", documentService.getDocument(docId));
            } catch (Exception ignored) {}

            model.addAttribute("documentDto", dto);
            model.addAttribute("isNew", false);
            return "document/form";
        }
    }

    // 6. 문서 삭제 (POST)
    @PostMapping("/{docId}/delete")
    public String deleteDocument(@PathVariable Long projectId,
                                 @PathVariable Long docId,
                                 HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        try {
            documentService.deleteDocument(docId, projectId);
        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "/documents?error=" + encode(e.getMessage());
        }
        return "redirect:/projects/" + projectId + "/documents";
    }

    private String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}
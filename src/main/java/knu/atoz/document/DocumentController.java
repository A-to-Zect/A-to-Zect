package knu.atoz.document;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import knu.atoz.document.dto.DocumentRequestDto;
import knu.atoz.member.Member;
import knu.atoz.participant.ParticipantService;
import knu.atoz.project.Project;
import knu.atoz.project.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.io.File;
import java.net.MalformedURLException;
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

    @GetMapping
    public String listDocuments(@PathVariable Long projectId,
                                HttpSession session,
                                Model model) {
        Member loginMember = getLoginMember(session);
        if (loginMember == null) return "redirect:/members/login";

        if (!isTeamMember(projectId, loginMember.getId())) {
            return "redirect:/projects/" + projectId + "?error=" + encode("접근 권한이 없습니다.");
        }

        try {
            Project project = projectService.getProject(projectId);
            List<Document> documents = documentService.getDocumentsByProject(projectId);

            model.addAttribute("project", project);
            model.addAttribute("documents", documents);

            return "document/list";

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
            return "redirect:/projects/" + projectId + "/documents?error=" + encode("문서 등록 권한이 없습니다.");
        }

        Project project = projectService.getProject(projectId);

        model.addAttribute("project", project);
        model.addAttribute("documentDto", new DocumentRequestDto());
        model.addAttribute("isNew", true);

        return "document/form";
    }

    @PostMapping("/new")
    public String createDocument(@PathVariable Long projectId,
                                 @Valid @ModelAttribute("documentDto") DocumentRequestDto dto,
                                 BindingResult bindingResult,
                                 HttpSession session,
                                 Model model) {

        Member loginMember = getLoginMember(session);
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("project", projectService.getProject(projectId));
            model.addAttribute("isNew", true);

            return "document/form";
        }

        try {
            documentService.createDocument(projectId, dto);
            return "redirect:/projects/" + projectId + "/documents";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("project", projectService.getProject(projectId));
            model.addAttribute("isNew", true);
            return "document/form";
        }
    }

    @GetMapping("/{docId}/edit")
    public String showEditForm(@PathVariable Long projectId,
                               @PathVariable Long docId,
                               HttpSession session,
                               Model model) {
        Member loginMember = getLoginMember(session);
        if (loginMember == null) return "redirect:/members/login";

        if (!isTeamMember(projectId, loginMember.getId())) {
            return "redirect:/projects/" + projectId + "/documents?error=" + encode("문서 수정 권한이 없습니다.");
        }

        try {
            Project project = projectService.getProject(projectId);
            Document document = documentService.getDocument(docId);


            DocumentRequestDto dto = new DocumentRequestDto();
            dto.setTitle(document.getTitle());


            model.addAttribute("project", project);
            model.addAttribute("document", document);
            model.addAttribute("documentDto", dto);
            model.addAttribute("isNew", false);

            return "document/form";

        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "/documents?error=" + encode(e.getMessage());
        }
    }

    @PostMapping("/{docId}/edit")
    public String updateDocument(@PathVariable Long projectId,
                                 @PathVariable Long docId,
                                 @Valid @ModelAttribute("documentDto") DocumentRequestDto dto,
                                 BindingResult bindingResult,
                                 HttpSession session,
                                 Model model) {

        Member loginMember = getLoginMember(session);
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("project", projectService.getProject(projectId));
            model.addAttribute("isNew", false);

            try {
                model.addAttribute("document", documentService.getDocument(docId));
            } catch (Exception ignored) {}

            return "document/form";
        }

        try {
            documentService.updateDocument(docId, projectId, dto);
            return "redirect:/projects/" + projectId + "/documents";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("project", projectService.getProject(projectId));
            model.addAttribute("isNew", false);

            try {
                model.addAttribute("document", documentService.getDocument(docId));
            } catch (Exception ignored) {}

            return "document/form";
        }
    }

    @PostMapping("/{docId}/delete")
    public String deleteDocument(@PathVariable Long projectId,
                                 @PathVariable Long docId,
                                 HttpSession session) {
        Member loginMember = getLoginMember(session);
        if (loginMember == null) return "redirect:/members/login";

        if (!isTeamMember(projectId, loginMember.getId())) {
            return "redirect:/projects/" + projectId + "/documents?error=" + encode("문서 삭제 권한이 없습니다.");
        }

        try {
            documentService.deleteDocument(docId, projectId);
        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "/documents?error=" + encode(e.getMessage());
        }
        return "redirect:/projects/" + projectId + "/documents";
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

    @GetMapping("/{docId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long projectId,
                                                 @PathVariable Long docId,
                                                 HttpSession session) throws MalformedURLException {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {

            return ResponseEntity.status(401).build();
        }

        if (!isTeamMember(projectId, loginMember.getId())) {
            return ResponseEntity.status(403).build();
        }

        Document document = documentService.getDocument(docId);
        File file = documentService.getPhysicalFile(docId);

        Resource resource = new UrlResource(file.toURI());


        String originalFileName = document.getLocation();
        String fileName = originalFileName.substring(originalFileName.lastIndexOf("/") + 1);

        String encodedUploadFileName = UriUtils.encode(fileName, StandardCharsets.UTF_8);

        String contentDisposition = "attachment; filename=\"" + encodedUploadFileName + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }
}
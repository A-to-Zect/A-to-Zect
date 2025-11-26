package knu.atoz.document;

import knu.atoz.document.dto.DocumentRequestDto;
import knu.atoz.document.exception.DocumentAccessException;
import knu.atoz.document.exception.DocumentFileNotExistException;
import knu.atoz.document.exception.DocumentNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;

    private final String uploadDir = System.getProperty("user.dir") + "/uploads";

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }


    public void createDocument(Long projectId, DocumentRequestDto dto) {

        try {
            String savedPath = saveFile(dto.getFile());
            if (savedPath == null) {
                throw new DocumentFileNotExistException();
            }

            Document document = new Document(projectId, dto.getTitle(), savedPath);
            documentRepository.save(document);

        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
        }
    }

    public List<Document> getDocumentsByProject(Long projectId) {
        return documentRepository.findByProjectId(projectId);
    }

    public Document getDocument(Long documentId) {
        Document document = documentRepository.findById(documentId);
        if (document == null) {
            throw new DocumentNotFoundException();
        }
        return document;
    }


    public void updateDocument(Long documentId, Long projectId, DocumentRequestDto requestDto) {
        Document targetDocument = documentRepository.findById(documentId);
        if (targetDocument == null) throw new DocumentNotFoundException();

        if (!targetDocument.getProjectId().equals(projectId)) {
            throw new DocumentAccessException("해당 문서는 이 프로젝트에 속하지 않아 수정할 수 없습니다.");
        }

        try {

            String oldPath = targetDocument.getLocation();
            String newPath = oldPath;

            if (requestDto.getFile() != null && !requestDto.getFile().isEmpty()) {

                newPath = saveFile(requestDto.getFile());

                System.out.println("[Update] 기존 파일 삭제 시도: " + oldPath);
                deletePhysicalFile(oldPath);
            }

            Document updateDoc = new Document(documentId, projectId, requestDto.getTitle(), newPath);
            documentRepository.update(updateDoc);

        } catch (IOException e) {
            throw new RuntimeException("파일 수정 중 오류가 발생했습니다.", e);
        }
    }

    public void deleteDocument(Long documentId, Long expectedProjectId) {
        Document targetDocument = documentRepository.findById(documentId);
        if (targetDocument == null) throw new DocumentNotFoundException();

        if (!targetDocument.getProjectId().equals(expectedProjectId)) {
            throw new DocumentAccessException("해당 문서는 이 프로젝트에 속하지 않아 삭제할 수 없습니다.");
        }

        String pathToDelete = targetDocument.getLocation();

        documentRepository.delete(documentId);

        System.out.println("[Delete] 파일 삭제 시도: " + pathToDelete);
        deletePhysicalFile(pathToDelete);
    }

    public File getPhysicalFile(Long documentId) {
        Document document = getDocument(documentId);
        File file = getFileFromPath(document.getLocation());

        if (!file.exists()) {
            throw new DocumentFileNotExistException();
        }
        return file;
    }


    private String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;

        File directory = new File(uploadDir);
        if (!directory.exists()) directory.mkdirs();

        UUID uuid = UUID.randomUUID();
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null) originalFilename = "unknown_file";

        String fileName = uuid + "_" + originalFilename;
        File saveFile = new File(directory, fileName);

        file.transferTo(saveFile);

        return "/uploads/" + fileName;
    }

    private void deletePhysicalFile(String dbPath) {
        if (dbPath == null || dbPath.isEmpty()) return;

        try {
            File file = getFileFromPath(dbPath);

            System.out.println("   -> 삭제 대상 경로: " + file.getAbsolutePath());
            System.out.println("   -> 파일 존재 여부: " + file.exists());

            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    System.out.println("   -> [성공] 파일이 삭제되었습니다.");
                } else {
                    System.err.println("   -> [실패] 파일 삭제에 실패했습니다. (권한 또는 사용 중)");
                }
            } else {
                System.out.println("   -> [무시] 삭제할 파일이 이미 없습니다.");
            }
        } catch (Exception e) {
            System.err.println("   -> [에러] 파일 삭제 중 예외 발생: " + e.getMessage());
        }
    }

    private File getFileFromPath(String dbPath) {


        String cleanPath = dbPath.replace("\\", "/");

        String fileName = cleanPath.substring(cleanPath.lastIndexOf("/") + 1);

        fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);

        return new File(uploadDir, fileName);
    }
}
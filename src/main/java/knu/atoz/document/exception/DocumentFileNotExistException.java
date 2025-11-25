package knu.atoz.document.exception;

public class DocumentFileNotExistException extends DocumentException {
    public DocumentFileNotExistException() {
        super("문서 파일이 존재하지 않습니다.");
    }
}

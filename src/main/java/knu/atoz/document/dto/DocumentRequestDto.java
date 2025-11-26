package knu.atoz.document.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRequestDto {
    @NotEmpty(message = "제목은 필수 입력 값입니다.")
    private String title;

    @NotNull(message = "문서를 첨부해주세요.")
    private MultipartFile file;
}
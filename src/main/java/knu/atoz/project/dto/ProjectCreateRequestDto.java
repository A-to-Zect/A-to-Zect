package knu.atoz.project.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ProjectCreateRequestDto {

    private Long memberId;

    @NotBlank(message = "프로젝트 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "프로젝트 설명은 필수입니다.")
    private String description;

    @NotNull(message = "모집 인원은 필수입니다.")
    @Min(value = 2, message = "최소 2명 이상이어야 합니다.")
    @Max(value = 20, message = "최대 20명까지만 가능합니다.")
    private Integer maxCount;

    private Set<String> techSpecs;

    private Map<Long, String> mbtiMap;
}
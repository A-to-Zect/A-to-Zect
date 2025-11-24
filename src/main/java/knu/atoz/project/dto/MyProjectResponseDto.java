package knu.atoz.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MyProjectResponseDto {
    private Long id;
    private String title;
    private String description;

    private String role;

    private LocalDateTime updatedAt;
}
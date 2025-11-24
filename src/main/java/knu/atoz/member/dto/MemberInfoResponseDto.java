package knu.atoz.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class MemberInfoResponseDto {

    private Long id;
    private String name;
    private String email;
    private LocalDate birthDate;
    private String mbti;
    private String techspecs;
    private String projectAndRole;
}

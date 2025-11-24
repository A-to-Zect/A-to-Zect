package knu.atoz.participant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ParticipantResponseDto {
    private Long memberId;
    private String name;
    private String email;
    private String role;
}
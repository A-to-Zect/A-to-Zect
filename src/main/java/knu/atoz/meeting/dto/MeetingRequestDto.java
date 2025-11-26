package knu.atoz.meeting.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeetingRequestDto {

    @NotBlank(message = "회의 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "회의 내용은 필수입니다.")
    private String description;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endTime;

    @AssertTrue(message = "시간은 둘 다 비워두거나, 둘 다 입력해야 하며 종료 시간은 시작 시간보다 뒤여야 합니다.")
    public boolean isTimeValid() {

        if (startTime == null && endTime == null) {
            return true;
        }

        if (startTime == null || endTime == null) {
            return false;
        }

        return endTime.isAfter(startTime);
    }
}
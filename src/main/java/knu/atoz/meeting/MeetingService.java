package knu.atoz.meeting;

import knu.atoz.meeting.dto.MeetingRequestDto;
import knu.atoz.meeting.exception.MeetingAccessException;
import knu.atoz.meeting.exception.MeetingNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MeetingService {

    private final MeetingRepository meetingRepository;
    public MeetingService(MeetingRepository meetingRepository) {
        this.meetingRepository = meetingRepository;
    }

    public void createMeeting(Long projectId, MeetingRequestDto requestDto) {
        
        Meeting meeting = new Meeting(
            projectId, 
            requestDto.getTitle(), 
            requestDto.getDescription(), 
            requestDto.getStartTime(), 
            requestDto.getEndTime()
        );
        
        meetingRepository.save(meeting);
    }

    public List<Meeting> getMeetingsByProject(Long projectId) {
        return meetingRepository.findByProjectId(projectId);
    }
    
    public Meeting getMeeting(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId);
        if (meeting == null) {
            throw new MeetingNotFoundException();
        }
        return meeting;
    }

    public void updateMeeting(Long meetingId, Long projectId, MeetingRequestDto requestDto) {
        
        Meeting targetMeeting = meetingRepository.findById(meetingId);
        if (targetMeeting == null) {
            throw new MeetingNotFoundException();
        }

        if (!targetMeeting.getProjectId().equals(projectId))
            throw new MeetingAccessException("해당 회의록은 이 프로젝트에 속하지 않아 수정할 수 없습니다.");
        
        Meeting meeting = new Meeting(
            meetingId,
            projectId,
            requestDto.getTitle(),
            requestDto.getDescription(),
            requestDto.getStartTime(),
            requestDto.getEndTime()
        );

        meetingRepository.update(meeting);
    }

    public void deleteMeeting(Long meetingId, Long expectedProjectId) {
        Meeting targetMeeting = meetingRepository.findById(meetingId);
        if (targetMeeting == null) {
            throw new MeetingNotFoundException();
        }

        if (!targetMeeting.getProjectId().equals(expectedProjectId))
            throw new MeetingAccessException("해당 회의록은 이 프로젝트에 속하지 않아 삭제할 수 없습니다.");
        
        meetingRepository.delete(meetingId);
    }
}
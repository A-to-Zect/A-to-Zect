package knu.atoz.meeting.exception;

public abstract class MeetingException extends RuntimeException {
    public MeetingException(String message) {
        super(message);
    }
}
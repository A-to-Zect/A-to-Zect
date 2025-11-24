package knu.atoz.techspec.member;

import knu.atoz.techspec.Techspec;
import knu.atoz.techspec.TechspecRepository;
import knu.atoz.techspec.exception.*;
import knu.atoz.utils.Azconnection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberTechspecService {

    private final TechspecRepository techspecRepository;
    private final MemberTechspecRepository memberTechspecRepository;

    // [변경] Member 객체 대신 ID 사용
    public List<Techspec> getMyTechspecs(Long memberId) {
        return memberTechspecRepository.findTechspecsByMemberId(memberId);
    }

    // [변경] Member 객체 대신 ID 사용, DTO 대신 String 직접 받기 (단일 필드라 더 편함)
    public void addTechspec(Long memberId, String techName) {

        if (techName == null || techName.isBlank()) {
            throw new TechspecInvalidException("스택 이름은 비어 있을 수 없습니다.");
        }

        Connection conn = null;
        try {
            conn = Azconnection.getConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작

            // 1. 기술 스택 존재 확인 및 생성
            Techspec techspec = techspecRepository.findTechspecIdByName(techName);
            Long techspecId;

            if (techspec == null) {
                techspecId = techspecRepository.createTechspec(conn, techName);
            } else {
                techspecId = techspec.getId();
            }

            // 2. 멤버-기술스택 연결
            // (Repository 메서드가 Long id를 반환한다고 가정)
            if (memberTechspecRepository.addMemberTechspec(conn, memberId, techspecId) == null) {
                throw new TechspecAlreadyExistsException("이미 추가된 스택입니다.");
            }

            conn.commit();

        } catch (SQLException e) {
            rollbackQuietly(conn);
            // 오라클 PK/Unique 제약조건 위반 (에러코드 1)
            if (e.getErrorCode() == 1) {
                throw new TechspecAlreadyExistsException("이미 존재하는 스택입니다.");
            }
            throw new RuntimeException("DB 오류: " + e.getMessage());

        } finally {
            closeQuietly(conn);
        }
    }

    // [변경] Member 객체 대신 ID 사용
    public void removeTechspec(Long memberId, Long techspecId) {
        if (techspecId == null || techspecId <= 0) {
            throw new TechspecInvalidException("유효하지 않은 스택 ID입니다.");
        }

        if (!memberTechspecRepository.deleteMemberTechspec(memberId, techspecId)) {
            throw new TechspecNotFoundException("삭제할 스택이 존재하지 않습니다.");
        }
    }

    private void rollbackQuietly(Connection conn) {
        try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
    }

    private void closeQuietly(Connection conn) {
        try {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (SQLException ignored) {}
    }
}
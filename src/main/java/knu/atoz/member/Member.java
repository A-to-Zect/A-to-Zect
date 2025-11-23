package knu.atoz.member;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Member {

    private final Long id;
    private final String email;
    private final String password;
    private final String name;
    private final LocalDate birthDate;
    private final LocalDateTime createdAt;

    public Member(Long id, String email, String password, String name, LocalDate birthDate,  LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.birthDate = birthDate;
        this.createdAt = createdAt;
    }

    public Member(String email, String password, String name, LocalDate birthDate,  LocalDateTime createdAt) {
        this.id = null;
        this.email = email;
        this.password = password;
        this.name = name;
        this.birthDate = birthDate;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

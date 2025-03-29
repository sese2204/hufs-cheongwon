package com.hufs_cheongwon.domain;

import com.hufs_cheongwon.domain.enums.UsersStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class Users extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

//    @Column(nullable = false)
    private String name;

    @Column
    private String studentNumber;

    @Column
    private LocalDateTime inactiveAt;

    @Column
    private LocalDateTime deleteAt;

    @Column
    @Setter
    @Enumerated(EnumType.STRING)
    private UsersStatus usersStatus;

    @OneToMany(mappedBy = "users", cascade = CascadeType.ALL)
    private List<Petition> petitions = new ArrayList<>();

    @OneToMany(mappedBy = "users", cascade = CascadeType.ALL)
    private List<Agreement> agreements = new ArrayList<>();

    @OneToMany(mappedBy = "users", cascade = CascadeType.ALL)
    private List<Report> reports = new ArrayList<>();

    @OneToOne(mappedBy = "users", cascade = CascadeType.ALL)
    private RefreshToken refreshToken;

    @Builder
    public Users(String email, String password, String name, String studentNumber) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.studentNumber = studentNumber;
        this.usersStatus = UsersStatus.ACTIVE;
    }

    /**
     * 연관관계 설정 메소드
     */
    public void addPetition(Petition petition) {
        petitions.add(petition);
    }

    public void addAgreement(Agreement agreement) {
        agreements.add(agreement);
    }

    public void addReport(Report report) {
        reports.add(report);
    }

    public void setEncodedPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return "ROLE_USER";
    }

    /**
     * 비즈니스 메소드
     */
    // 회원 상태 변경
    public void changeUserStatus(UsersStatus status){
        this.usersStatus = status;
    }

    public void setInactiveAt() {
        this.inactiveAt = LocalDateTime.now();
    }

    // 회원 정보 지우기
    public void eraseUserInfo() {
        this.email = "unknown";
        this.password = "DELETED_USER_PASSWORD";
        this.name = null;
        this.studentNumber = null;
        this.deleteAt = LocalDateTime.now();
    }
}

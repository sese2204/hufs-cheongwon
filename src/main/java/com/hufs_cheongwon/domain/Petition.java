package com.hufs_cheongwon.domain;

import com.hufs_cheongwon.domain.enums.Category;
import com.hufs_cheongwon.domain.enums.PetitionStatus;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Petition extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PetitionStatus petitionStatus;

    @Column
    private Integer agreeCount = 0;

    @Column
    private Integer viewCount = 0;

    @Column
    private Integer reportCount = 0;

    @Column
    private Integer bookmarkCount = 0;

    //userId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private Users users; // 작성자

    @OneToOne(mappedBy = "petition", cascade = CascadeType.ALL)
    private Response response;

    @OneToMany(mappedBy = "petition", cascade = CascadeType.ALL)
    private List<Agreement> agreements = new ArrayList<>();

    @OneToMany(mappedBy = "petition", cascade = CascadeType.ALL)
    private List<Report> reports = new ArrayList<>();

    @OneToMany(mappedBy = "petition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Link> links = new ArrayList<>();

    @OneToMany(mappedBy = "petition", cascade = CascadeType.ALL)
    private List<PetitionBookmark> petitionBookmarks = new ArrayList<>();

    @Builder
    public Petition(Users user, String title, Category category, String content, PetitionStatus petitionStatus) {
        this.title = title;
        this.category = category;
        this.content = content;
        this.petitionStatus = petitionStatus;
        this.users = user;
        user.addPetition(this);
    }

    /**
     * 비즈니스 메소드
     */
    public void addAgreeCount(int count) {
        this.agreeCount += count;
    }
    public void addViewCount(int count) {
        this.viewCount += count;
    }
    public void addReportCount(int count) {
        this.reportCount += count;
    }
    public void changePetitionStatus(PetitionStatus newStatus) {
        this.petitionStatus = newStatus;
    }
    public void increaseBookmarkCount() {
        this.bookmarkCount++;
    }

    public void decreaseBookmarkCount() {
        if (this.bookmarkCount > 0) this.bookmarkCount--;
    }

    /**
     * 연관관계 메소드
     */
    public void addReport(Report report) {
        this.reports.add(report);
    }

    public void addAgreement(Agreement agreement) {
        this.agreements.add(agreement);
    }

    public void addLink(Link link) {
        this.links.add(link);
        link.setPetition(this);
    }
    public void removeResponse() {
        this.response = null;
    }
    public void addBookmark(PetitionBookmark petitionBookmark) {
        this.petitionBookmarks.add(petitionBookmark);
    }
}

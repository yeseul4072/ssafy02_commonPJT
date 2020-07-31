package com.ssafy.study.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name="feeds")
public class Feed {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "studyroom_id")
    private Studyroom studyroom;

    @Column(name="studyImage", columnDefinition="BLOB")
    @Lob
    private byte[] studyImage;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "feed")
    private Set<Like> studyLike;

    
    @Column(name="studyContent")
    private String studyContent;

    @Column(name="studyDegree")
    private int studyDegree;

    @Column(name="registTime", columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date registTime;


    protected Set<Like> getStudyLikeInternal(){
        if(this.studyLike==null){
            this.studyLike = new HashSet<>();
        }
        return this.studyLike;
    }
    protected void setStudyLikeInternal(Set<Like> studyLike){
        this.studyLike=studyLike;
    }

    public void addLike(Like studyLike){
        getStudyLikeInternal().add(studyLike);
        studyLike.setFeed(this);
    }

    
    
}
package com.greentech.kmcg.bean;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "score_cg")
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "user_id")
    private Integer userId;
    @Column(name = "score")
    private Integer score;
    @Column(name = "time")
    private Long time;
    @Column(name = "date")
    private Date date;
}

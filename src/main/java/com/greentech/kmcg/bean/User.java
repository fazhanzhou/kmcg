package com.greentech.kmcg.bean;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "user_cg")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String tel;
    @Column(name = "jiedao")
    private String jiedao;
    @Column(name = "address")
    private String address;
    @Column(name = "openid")
    private String openid;
}

package com.taco1.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "token_table")
public class PushTokenEntity {

    @Id
    @Column
    private String token;

    @Column
    private String device;
}

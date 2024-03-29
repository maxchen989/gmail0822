package com.max.gmall0822.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class UserAddress implements Serializable {

    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    String id;

    @Column
    String userAddress;

    @Column
    String userId;

    @Column
    String consignee;

    @Column
    String phoneNum;

    @Column
    String isDefault;

}

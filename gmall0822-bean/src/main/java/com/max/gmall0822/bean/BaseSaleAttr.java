package com.max.gmall0822.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class BaseSaleAttr implements Serializable {

    @Id
    @Column
    private String id;

    @Column
    private String name;
}

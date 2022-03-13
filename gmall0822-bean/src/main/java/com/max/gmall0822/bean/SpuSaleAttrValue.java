package com.max.gmall0822.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class SpuSaleAttrValue implements Serializable {

    @Id
    @Column
    private String id;

    @Column
    private String spuId;

    @Column
    private String saleAttrId;

    @Column
    private String saleAttrValueName;

    @Transient
    private String isChecked;

}

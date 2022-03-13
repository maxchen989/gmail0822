package com.max.gmall0822.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class SkuLsParams implements Serializable {

    String  keyword;

    String catalog3Id;

    String[] valueId; //url傳入多個valueId 認array

    int pageNo=1;

    int pageSize=20;

}

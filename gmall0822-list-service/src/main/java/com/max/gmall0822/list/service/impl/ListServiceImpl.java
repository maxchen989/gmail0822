package com.max.gmall0822.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.max.gmall0822.bean.SkuLsInfo;
import com.max.gmall0822.bean.SkuLsParams;
import com.max.gmall0822.bean.SkuLsResult;
import com.max.gmall0822.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import jdk.nashorn.internal.parser.JSONParser;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListServiceImpl implements ListService {

    @Autowired
    JestClient jestClient; //連接es

    public void saveSkuLSInfo(SkuLsInfo skuLsInfo){

        //Index生成器
        Index.Builder indexBuilder = new Index.Builder(skuLsInfo);
        indexBuilder.index("gmall0822_sku_info").type("_doc").id(skuLsInfo.getId());
        Index index = indexBuilder.build();
        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SkuLsResult getSkuLsInfoList(SkuLsParams skuLsParams) {
        String query ="{\n" +
                "  \"query\": {\n" +
                "    //商品名稱的全文檢索\n" +
                "    \"bool\": { //超過兩個條件的時候要用bool包\n" +
                "      \"must\": [\n" +
                "        {\"match\": {\n" +
                "          \"skuName\": \"iphone 紅\"\n" +
                "        }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"filter\": [\n" +
                "        {\"term\": {\n" +
                "          \"catalog3Id\": \"23\" // 根據3級分類 進行過濾\n" +
                "        }},{\n" +
                "           \"term\": {\n" +
                "          \"skuAttrValueList.valueId\": \"23\" //根據平台屬性值 進行過濾\n" +
                "        }},{\n" +
                "           \"term\": {\n" +
                "          \"skuAttrValueList.valueId\": \"38\" //根據平台屬性值 進行過濾\n" +
                "        }},\n" +
                "        {\n" +
                "          \"range\":{\n" +
                "            \"price\":{\"gte\":100} //根據價格範圍 進行過濾 ; gte 大于等于 great than or equals\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "    }\n" +
                "  //分頁\n" +
                "  //,\"from\":2\n" +
                "  // ,\"size\":0\n" +
                "  ,\"highlight\": {\"fields\": {\"skuName\": {\"pre_tags\": \"<span style='color:red\",\"post_tags\": \"</span>\"}}} //高亮\n" +
                "  ,\"aggs\": {\n" +
                "    \"groupby_valueId\": {\n" +
                "      \"terms\": {\n" +
                "        \"field\": \"skuAttrValueList.valueId\",\n" +
                "        \"size\": 1000\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"sort\": [ //按熱度進行排序\n" +
                "    {\n" +
                "      \"hotScore\": {\n" +
                "        \"order\": \"desc\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "  }";

        //以上可以使用,但是還要自己串,jest提供以下工具,方便使用
        //也是依照dsl下去寫
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //商品名稱查詢搜索
        if(skuLsParams.getKeyword()!=null){
            boolQueryBuilder.must(new MatchQueryBuilder("skuName",skuLsParams.getKeyword()));
            //高亮
            searchSourceBuilder.highlight(new HighlightBuilder().field("skuName").preTags("<span style='color:red'>").postTags("</span>"));
        }

        //三級分類過濾
        if(skuLsParams.getCatalog3Id()!=null){
            boolQueryBuilder.filter(new TermQueryBuilder("catalog3Id",skuLsParams.getCatalog3Id()));
        }

        // new TermsQueryBuilder()
        // terms 類似於 in , 是或的關係, 不要用錯
        //平台屬性過濾
        //skuLsParams.getValueId().length!=0 : 意義 大於0才有for循環的意義
        if(skuLsParams.getValueId()!=null&& skuLsParams.getValueId().length>0){
            String[] valueId = skuLsParams.getValueId();
            for (int i = 0; i < valueId.length; i++) {
                String valueid = valueId[i];
                boolQueryBuilder.filter(new TermQueryBuilder("skuAttrValueList.valueId",valueid));

            }
        }

        //價格過濾
        //boolQueryBuilder.filter(new RangeQueryBuilder("price",).gte("100"));
        searchSourceBuilder.query(boolQueryBuilder);

        //起始行
        searchSourceBuilder.from((skuLsParams.getPageNo()-1)*skuLsParams.getPageSize());//頁碼轉為行
        searchSourceBuilder.size(skuLsParams.getPageSize());

        //聚合
        TermsBuilder aggsBuilder = AggregationBuilders.terms("groupby_valueId").field("skuAttrValueList.valueId").size(1000);
        searchSourceBuilder.aggregation(aggsBuilder);
        //排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);

//        System.out.println(searchSourceBuilder.toString());

        //Search.Builder searchBuilder = new Search.Builder(query);
        Search.Builder searchBuilder = new Search.Builder(searchSourceBuilder.toString());

        // index是哪張表的意思
        Search search = searchBuilder.addIndex("gmall0822_sku_info").addType("_doc").build();

        SkuLsResult skuLsResult = new SkuLsResult();

        try {
            SearchResult searchResult = jestClient.execute(search);
            //商品信息列表
            List<SkuLsInfo> skuLsInfoList=new ArrayList<>();
            List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);//告訴jest要把json給哪個bean
            for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
                SkuLsInfo source = hit.source;
                skuLsInfoList.add(source);
            }
            skuLsResult.setSkuLsInfoList(skuLsInfoList);

            //BUG暫時找不到問題 09/10
            //總數
//            Long total = searchResult.getTotal();
//            skuLsResult.setTotal(total);
//
//            //總頁數 = (總數+每頁行數-1)/每頁行數
//            long totalPage= (total + skuLsParams.getPageSize() -1) / skuLsParams.getPageSize();
//            skuLsResult.setTotalPages(totalPage);

            //聚合 商品涉及的平台屬性
            ArrayList<String> attrValueIdList = new ArrayList<>();
            List<TermsAggregation.Entry> buckets = searchResult.getAggregations().getTermsAggregation("groupby_valueId").getBuckets();
            for (TermsAggregation.Entry bucket : buckets) {
                String key = bucket.getKey();
                attrValueIdList.add(key);
            }
            skuLsResult.setAttrValueIdList(attrValueIdList);


        } catch (IOException e) {
            e.printStackTrace();
        }

        return skuLsResult;
    }

}

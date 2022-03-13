package com.max.gmall0822.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.max.gmall0822.bean.BaseAttrInfo;
import com.max.gmall0822.bean.BaseAttrValue;
import com.max.gmall0822.bean.SkuLsParams;
import com.max.gmall0822.bean.SkuLsResult;
import com.max.gmall0822.service.ListService;
import com.max.gmall0822.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    ListService listService;

    @Reference
    ManageService manageService;

    @GetMapping("list.html")
    // @ResponseBody //返回一個實體結果(還沒有頁面,可做測試)
    // Model 渲染頁面用
    public String list(SkuLsParams skuLsParams, Model model){
        SkuLsResult skuLsResult = listService.getSkuLsInfoList(skuLsParams);
        //新增屬性後,就可添加至html渲染
        model.addAttribute("skuLsResult",skuLsResult);
        //得到平台屬性列表清單
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        List<BaseAttrInfo> attrList = manageService.getAttrList(attrValueIdList);
        model.addAttribute("attrList",attrList);
        //得到歷史當前已有的所有歷史條件 + 當前屬性值的id


        String paramUrl = makeParamUrl(skuLsParams);

        //把所有已經選擇的數值 從屬性+屬性值清單中刪除屬性
        //清單 --> attrList 已選擇的屬性值 skuLsParams.getValueId()
        // 快捷鍵 itco : 迭代器 與iter有什麼優點? 他有iterator,只指向某一列,當我不要那一列,就可以直接remove掉
        // List才可以迭代 (iter,itco) 數組不可迭代 (itar)
        if (skuLsParams.getValueId()!=null&& skuLsParams.getValueId().length>0){
            for (Iterator<BaseAttrInfo> iterator = attrList.iterator(); iterator.hasNext(); ) {
                BaseAttrInfo baseAttrInfo =  iterator.next();
                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                for (BaseAttrValue baseAttrValue : attrValueList) {
                    for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                        String selectedValueId = skuLsParams.getValueId()[i];
                        if(baseAttrValue.getId().equals(selectedValueId)){ //如果清單中的屬性值和已選擇的屬性值相同,那麼刪除對應的屬性
                            iterator.remove();
                        }

                    }
                }

            }
        }

        model.addAttribute("paramUrl",paramUrl);// keyword=小米&valueId=106&valueId=133
        //return JSON.toJSONString(skuLsResult);
        return "list";
    }

    /**
     * 把頁面傳入的參數對象轉skuLsParams換成為ParamUrl
     * @param skuLsParams
     * @return
     */
    public String makeParamUrl(SkuLsParams skuLsParams){
        String paramUrl="";
        if(skuLsParams.getKeyword()!=null){
            paramUrl+="keyword="+skuLsParams.getKeyword();
        }
        else if(skuLsParams.getCatalog3Id()!=null){
            paramUrl+="catalog3Id="+skuLsParams.getCatalog3Id();
        }

        if(skuLsParams.getValueId()!=null&& skuLsParams.getValueId().length>0){
            for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                String valueId = skuLsParams.getValueId()[i];
                if(paramUrl.length()>0){
                    paramUrl+="&";
                }
                paramUrl+="valueId="+valueId;
            }

        }
        return paramUrl;
    }
}

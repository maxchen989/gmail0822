package com.max.gmall0822.manage.serivce.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.max.gmall0822.bean.*;
import com.max.gmall0822.manage.mapper.*;
import com.max.gmall0822.service.ManageService;
import com.max.gmall0822.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator;
import org.jboss.netty.handler.ipfilter.IpSubnetFilterRule;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    BaseAttInfoMapper baseAttInfoMapper;

    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    SpuImageMapper spuImageMapper;

    @Autowired
    SpuInfoMapper spuInfoMapper;

    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    SkuImageMapper skuImageMapper;

    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    public static final String SKUKEY_PREFIX = "sku:";
    public static final String SKUKEY_INFO_SUBFIX = ":info";
    public static final String SKUKEY_LOCK_SUBFIX = ":lock";

    //過期時間
    //    public static final int SKU_EXPIRE=3;

    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id); //取條件Catalog1Id  = catalog1Id的baseCaralog2
        List<BaseCatalog2> baseCatalog2List = baseCatalog2Mapper.select(baseCatalog2);
        return baseCatalog2List;
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        List<BaseCatalog3> baseCatalog3List = baseCatalog3Mapper.select(baseCatalog3);
        return baseCatalog3List;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
//        Example example = new Example(BaseAttrInfo.class);
//        example.createCriteria().andEqualTo("catalog3Id",catalog3Id);
//
//        List<BaseAttrInfo> baseAttrInfoList = baseAttInfoMapper.selectByExample(example);
//        //查詢平台屬性值
//        for (BaseAttrInfo baseAttrInfo : baseAttrInfoList) {
//            BaseAttrValue baseAttrValue = new BaseAttrValue();
//            baseAttrValue.setAttrId(baseAttrInfo.getId());
//            List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.select(baseAttrValue);
//            baseAttrInfo.setAttrValueList(baseAttrValueList);
//        }
        List<BaseAttrInfo> baseAttrInfoList = baseAttInfoMapper.getBaseAttrInfoListByCatalog3Id(catalog3Id);

        return baseAttrInfoList;
    }

    @Override
    public BaseAttrInfo getBaseAttrInfo(String attrId) {
        BaseAttrInfo baseAttrInfo = baseAttInfoMapper.selectByPrimaryKey(attrId);

        BaseAttrValue baseAttrValueQuery = new BaseAttrValue();
        baseAttrValueQuery.setAttrId(attrId);
        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.select(baseAttrValueQuery);

        baseAttrInfo.setAttrValueList(baseAttrValueList);
        return baseAttrInfo;
    }

    @Override
    @Transactional
    public void savaBaseAttrInfo(BaseAttrInfo baseAttrInfo) {

        //baseAttrInfo.getId().length() > 0 只是更嚴謹
        if (baseAttrInfo.getId() != null && baseAttrInfo.getId().length() > 0) {
            baseAttInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
        } else {
            //嚴謹,避免前端弱智
            baseAttrInfo.setId(null);
            baseAttInfoMapper.insertSelective(baseAttrInfo);
        }

        //根據attrId先全部刪除,在統一保存
        Example example = new Example(BaseAttrValue.class);
        example.createCriteria().andEqualTo("attrId", baseAttrInfo.getId());
        baseAttrValueMapper.deleteByExample(example);

        //getAttrValueList : 得到其各個屬性值
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        for (BaseAttrValue baseAttrValue : attrValueList) {
            String id = baseAttrInfo.getId();
            baseAttrValue.setAttrId(id);
            baseAttrValueMapper.insertSelective(baseAttrValue);
        }

    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Override
    public void safeSpuInfo(SpuInfo spuInfo) {
        //spu基本訊息
        spuInfoMapper.insertSelective(spuInfo);

        //圖片訊息
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        for (SpuImage spuImage : spuImageList) {
            spuImage.setSpuId(spuInfo.getId());
            spuImageMapper.insertSelective(spuImage);
        }

        //銷售屬性
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
            spuSaleAttr.setSpuId(spuInfo.getId());
            spuSaleAttrMapper.insertSelective(spuSaleAttr);

            //銷售屬性值
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                spuSaleAttrValue.setSpuId(spuInfo.getId());
                spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
            }

        }

    }

    @Override
    public List<SpuInfo> getSpuList(String catalog3Id) {

        //用於查詢
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);

        return spuInfoMapper.select(spuInfo);
    }

    @Override
    public List<SpuImage> getSpuImageList(String spuId) {
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        return spuImageMapper.select(spuImage);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        return spuSaleAttrMapper.getSpuSaleAttrListBySupId(spuId);
    }

    @Override
    @Transactional
    public void saveSkuInfo(SkuInfo skuInfo) {
        //1,基本信息
        if (skuInfo.getId() == null || skuInfo.getId().length() == 0) {
            skuInfoMapper.insert(skuInfo);
        } else {
            skuInfoMapper.updateByPrimaryKeySelective(skuInfo);
        }

        //2,平台屬性
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuInfo.getId());
        skuAttrValueMapper.delete(skuAttrValue);

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue attrValue : skuAttrValueList) {
            skuAttrValueMapper.insertSelective(attrValue);
        }

        //3,銷售屬性
        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuInfo.getId());
        skuSaleAttrValueMapper.delete(skuSaleAttrValue);

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue saleAttrValue : skuSaleAttrValueList) {
            saleAttrValue.setSkuId(skuInfo.getId());
            skuSaleAttrValueMapper.insertSelective(saleAttrValue);
        }

        //4,圖片
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuInfo.getId());
        skuImageMapper.delete(skuImage);
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage image : skuImageList) {
            image.setSkuId(skuInfo.getId());
            skuImageMapper.insertSelective(image);
        }
    }

    //查詢skuInfo
    public SkuInfo getSkuInfoDB(String skuId) {
        //測試
//        try{
//            Jedis jedis = redisUtil.getJedis();
//            jedis.set("k1","v1");
//            jedis.close();
//        }catch (JedisConnectionException e){
//            e.printStackTrace();
//        }
        //測試用
        System.out.println(Thread.currentThread() + "讀取數據庫!"); //得到線程訊息
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        if (skuInfo == null) {
            return null;
        }

        //圖片
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(skuImageList);

        //銷售屬性
        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuId);
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuSaleAttrValueMapper.select(skuSaleAttrValue);
        skuInfo.setSkuSaleAttrValueList(skuSaleAttrValueList);

        //平台屬性
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValue);
        skuInfo.setSkuAttrValueList(skuAttrValueList);

        return skuInfo;
    }

    public SkuInfo getSkuInfo_redis(String skuId) {
        SkuInfo skuInfoResult = null;
        // 1,先查redis 沒有再查數據庫
        Jedis jedis = redisUtil.getJedis();
        //過期時間,方便調試所以定義在這
        int SKU_EXPIRE = 300;
        // redis結構 : 1,type : String
        // 為什麼不hash,因為過期時間,hash雖然可以拆很多份,但是所有sku相關資料的過期時間都同一個,考慮相關業務需求,所以使用String
        // 2,key : sku:101:info
        // 3,value : skuInfoJson
        String skuKey = SKUKEY_PREFIX + skuId + SKUKEY_INFO_SUBFIX;
        String skuInfoJson = jedis.get(skuKey);//查詢緩存
        if (skuInfoJson != null) {
            if (!"EMPTY".equals(skuInfoJson)) {
                System.out.println(Thread.currentThread() + "命中緩存!"); //得到線程訊息
                skuInfoResult = JSON.parseObject(skuInfoJson, SkuInfo.class);
            }
        } else {

            // setnx =>
            // 鎖為什麼不用這兩步驟代替就好? 1,查鎖 exists 2,搶鎖 set ?
            // 因為分兩步驟仍有多個人拿到鎖的可能, 用setnx一步驟操作就不會有這種情況發生
            //定義鎖的結構 type : String, key : sku:101:lock value : locked (隨便取)
            String lockKey = SKUKEY_PREFIX + skuId + SKUKEY_LOCK_SUBFIX;
            // 但這樣分成兩步驟, 最好設成一步.
            //Long locked = jedis.setnx(lockKey, "locked");//拿鎖成功返回1,失敗返回0
            //jedis.expire(lockKey,10);//設定預期時間
            //但這樣不保證 讓線程只能刪除自己的鎖
            //String locked = jedis.set(lockKey, "locked", "NX", "EX", 10);
            String token = UUID.randomUUID().toString();
            String locked = jedis.set(lockKey, token, "NX", "EX", 10);

            if ("OK".equals(locked)) {
                System.out.println(Thread.currentThread() + "得到鎖!"); //得到線程訊息
                System.out.println(Thread.currentThread() + "未命中!"); //得到線程訊息
                skuInfoResult = getSkuInfoDB(skuId);
                System.out.println(Thread.currentThread() + "寫入緩存!"); //得到線程訊息
                String skuInfoJsonResult = null;

                if (skuInfoResult != null) {
                    skuInfoJsonResult = JSON.toJSONString(skuInfoResult);
                } else {
                    System.out.println(Thread.currentThread() + "empty"); //得到線程訊息
                    skuInfoJsonResult = "EMPTY";
                }
                jedis.setex(skuKey, SKU_EXPIRE, skuInfoJsonResult);//set :寫入緩存 setex:寫入緩存,同時設計過期時間
                System.out.println(Thread.currentThread() + "釋放鎖!" + lockKey); //得到線程訊息
                if (jedis.exists(lockKey) && token.equals(jedis.get(lockKey))) {  //不完美, 可以用lua解決
                    jedis.del(lockKey);//釋放鎖 如果DB卡住?設定過期時間.
                }
            } else {
                System.out.println(Thread.currentThread() + "未得到鎖,開始自旋等待!"); //得到線程訊息
                // 輪詢(每隔一段時間,問一下鎖用完了沒),自旋(調自己)
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getSkuInfo(skuId);
            }
        }

        jedis.close();
        return skuInfoResult;
    }

    @Override
    public SkuInfo getSkuInfo(String skuId) {
        SkuInfo skuInfoResult = null;
        // 1,先查redis 沒有再查數據庫
        Jedis jedis = redisUtil.getJedis();
        //過期時間,方便調試所以定義在這
        int SKU_EXPIRE = 300;
        // redis結構 : 1,type : String
        // 為什麼不hash,因為過期時間,hash雖然可以拆很多份,但是所有sku相關資料的過期時間都同一個,考慮相關業務需求,所以使用String
        // 2,key : sku:101:info
        // 3,value : skuInfoJson
        String skuKey = SKUKEY_PREFIX + skuId + SKUKEY_INFO_SUBFIX;
        String skuInfoJson = jedis.get(skuKey);//查詢緩存
        if (skuInfoJson != null) {
            if (!"EMPTY".equals(skuInfoJson)) {
                System.out.println(Thread.currentThread() + "命中緩存!"); //得到線程訊息
                skuInfoResult = JSON.parseObject(skuInfoJson, SkuInfo.class);
            }
        } else {

            //使用redisson
            Config config = new Config();
            config.useSingleServer().setAddress("redis://redis.gmall.com:6379");

            RedissonClient redissonClient = Redisson.create(config);
            String lockKey = SKUKEY_PREFIX + skuId + SKUKEY_LOCK_SUBFIX;
            RLock lock = redissonClient.getLock(lockKey);
            //lock.lock(10, TimeUnit.SECONDS);//等候時間10秒
            boolean locked = false;
            try {
                locked = lock.tryLock(10, 5, TimeUnit.SECONDS);//鎖有效時間5秒,等候時間10秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (locked) {
                System.out.println(Thread.currentThread() + "得到鎖!"); //得到線程訊息
                //如果得到鎖後能夠在緩存中查詢,那麼直接使用緩存數據,不用再查詢數據庫
                System.out.println(Thread.currentThread() + "再次查詢緩存!"); //得到線程訊息
                String skuInfoJsonResult = jedis.get(skuKey);//查詢緩存

                if (skuInfoJsonResult != null) {
                    if (!"EMPTY".equals(skuInfoJsonResult)) {
                        System.out.println(Thread.currentThread() + "命中緩存!"); //得到線程訊息
                        skuInfoResult = JSON.parseObject(skuInfoJsonResult, SkuInfo.class);
                    }
                } else {
                    skuInfoResult = getSkuInfoDB(skuId);
                    System.out.println(Thread.currentThread() + "寫入緩存!"); //得到線程訊息

                    if (skuInfoResult != null) {
                        skuInfoJsonResult = JSON.toJSONString(skuInfoResult);
                    } else {
                        skuInfoJsonResult = "EMPTY";
                    }
                    jedis.setex(skuKey, SKU_EXPIRE, skuInfoJsonResult);//set :寫入緩存 setex:寫入緩存,同時設計過期時間
                }

                lock.unlock(); //釋放鎖
            }
        }

        jedis.close();
        return skuInfoResult;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListBySupIdCheckSku(String skuId, String spuId) {
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.getSpuSaleAttrListBySupIdCheckSku(skuId, spuId);
        return spuSaleAttrList;
    }

    @Override
    public Map getSkuValueIdsMap(String spuId) {
        List<Map> mapList = skuSaleAttrValueMapper.getSkuSaleAttrValueBySpu(spuId);
        // 為了將數據型態轉為我們想要的型態,原本key是欄位,value是值.
        // 現在想轉成 key是skuId值, value是valueIds值
        Map skuValueIdsMap = new HashMap();
        for (Map map : mapList) {
            String skuId = (Long) map.get("sku_id") + "";
            String valueIds = (String) map.get("value_ids");
            skuValueIdsMap.put(skuId, valueIds);
        }
        return skuValueIdsMap;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(List attrValueIdList) {
        //attrValueIdList --> 13,14,15 : 想把List(13,14,15)轉為字串13,14,15

        String valueIds = StringUtils.join(attrValueIdList.toArray(), ",");

        return baseAttInfoMapper.getBaseAttrInfoListByValueIds(valueIds);
    }
}

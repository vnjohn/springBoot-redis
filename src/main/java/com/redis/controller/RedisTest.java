package com.redis.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.redis.entity.UserInfoEntity;
import com.redis.service.UserInfoService;
import com.redis.utils.JsonUtils;
import com.redis.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Lenovo
 * @date 2020-04-10 23:20
 */
@RestController
public class RedisTest {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UserInfoService userInfoService;

    @GetMapping("/setKey")
    public boolean setKey(@RequestParam String key,
                       @RequestParam String value){
        return redisUtil.set(key,value);
    }

    @GetMapping("/getKey/{key}")
    public String getKey(@PathVariable String key){
        return redisUtil.get(key).toString();
    }

    @GetMapping("/deleteKey/{key}")
    public boolean deleteKey(@PathVariable String key){
        return redisUtil.delKey(key);
    }

    @GetMapping("/findUserInfoAll")
    public Object findUserInfoAll(@RequestParam Integer fraction){
        List<UserInfoEntity> list =null;
        //先从redis中查询该数据是否存在
        String s = redisUtil.get(fraction.toString());
        //如果redis查询为空 再去查一次数据库
        if (s!=null){
            list= JsonUtils.jsonToList(s, UserInfoEntity.class);
        }else{
            //等价SQL: SELECT id,name,age,skill,evaluate,fraction FROM user_info WHERE fraction = #{fraction}
            QueryWrapper<UserInfoEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(UserInfoEntity::getFraction,fraction);
            list = userInfoService.list(queryWrapper);
            /**
             *  查询的key在redis中不存在
             *  对应的fraction在数据库也不存在
             *  此时被非法用户进行攻击,大量的请求会直接打的db上
             *  造成宕机,从而影响整个系统
             *  这种现象称之为缓存穿透
             *  解决方案：把空的数据也缓存起来,比如空字符串,空对象,或空数组、list  并设置一定的时长
             **/
            if (list!=null&&list.size()>0){
                redisUtil.set(fraction.toString(),JsonUtils.objectToJson(list));
            }else{
                redisUtil.set(fraction.toString(),JsonUtils.objectToJson(list),5,TimeUnit.MINUTES);//设置过期时长为5分钟
            }
        }
        return list;
    }
}

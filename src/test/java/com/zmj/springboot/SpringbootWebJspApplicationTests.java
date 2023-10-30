package com.zmj.springboot;

import com.zmj.springboot.entity.User;
import com.zmj.springboot.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.sql.DataSource;

@Slf4j
@SpringBootTest
class SpringbootWebJspApplicationTests {
    @Test
    @DisplayName("异常测试")
    public void exceptionTest() {
        ArithmeticException exception = Assertions.assertThrows(
                ArithmeticException.class, () -> System.out.println(1 % 0));//断定有异常
    }

    @DisplayName("测试组合断言")
    @Test
    void testAllAssertions() {
        Assertions.assertAll("test",()->Assertions.assertTrue(true && true,"结果不为true"),
                () -> Assertions.assertEquals(1,2,"结果不是1"));
        /*结果不是1 ==> expected: <1> but was: <2>
                Comparison Failure:
        Expected :1
        Actual   :2*/
    }

    @DisplayName("测试简单断言")
    @Test
    void testSimpleAssertions() {
        int cal = 2+3;
        Assertions.assertEquals(6,cal,"业务逻辑计算失败");//期望是6，实际是5，报错
        /*org.opentest4j.AssertionFailedError: 业务逻辑计算失败 ==>
        Expected :6
        Actual   :5*/
    }





    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserMapper userMapper;

    @Test
    void contextLoads() {
        log.info("数据源类型：{}",dataSource.getClass());
        //数据源类型：class com.alibaba.druid.pool.DruidDataSource
    }

    @Test
    void testUserMapper() {
        User user = userMapper.selectById(1);
        log.info("用户信息：{}",user);//用户信息：User(id=1, name=Jone, age=18, email=test1@baomidou.com)
    }

    @Test
    void testRedis() {
        ValueOperations operations = redisTemplate.opsForValue();
        operations.set("key1","hello world");
        String key1 = (String) operations.get("key1");
        System.out.println(key1);//hello world

        ValueOperations<String, String> operations2 = stringRedisTemplate.opsForValue();
        operations2.set("key2","你好");
        String key2 = operations2.get("key2");
        System.out.println(key2);//你好
    }



}

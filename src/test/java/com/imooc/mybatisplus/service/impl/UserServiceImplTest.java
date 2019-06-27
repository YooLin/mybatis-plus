package com.imooc.mybatisplus.service.impl;

import com.imooc.mybatisplus.MybatisPlusApplicationTests;
import com.imooc.mybatisplus.entity.User;
import com.imooc.mybatisplus.service.IUserService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author linyicong
 * @since 2019-06-28
 */
public class UserServiceImplTest extends MybatisPlusApplicationTests {
    @Autowired
    private IUserService userService;

    @Test
    public void testSelect() {
        List<User> users = this.userService.lambdaQuery().select(User::getName).like(User::getName, "雨").list();
        Assert.assertTrue(!CollectionUtils.isEmpty(users));
    }

    @Test
    public void testUpdate() {
        boolean updateResult = this.userService.lambdaUpdate().set(User::getName, "刘红美").eq(User::getName, "刘红雨").update();
        Assert.assertTrue(updateResult);
    }

    @Test
    public void testDelete() {
        boolean deleteResult = this.userService.lambdaUpdate().eq(User::getName, "刘红美").remove();
        Assert.assertTrue(deleteResult);
    }
}
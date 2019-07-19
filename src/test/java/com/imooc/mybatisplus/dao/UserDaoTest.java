package com.imooc.mybatisplus.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.additional.query.impl.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.additional.update.impl.LambdaUpdateChainWrapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.imooc.mybatisplus.MybatisPlusApplicationTests;
import com.imooc.mybatisplus.entity.User;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author linyicong
 * @since 2019-06-22
 */
public class UserDaoTest extends MybatisPlusApplicationTests {
    @Resource
    private UserDao userDao;

    @Test
    public void testSelectAll() {
        List<User> users = this.userDao.selectList(Wrappers.emptyWrapper());
        Assert.assertEquals(users.size(), 5);
    }

    @Test
    public void testSelectMap() {
        //  selectByMap方法中参数中Map的Key为表中的列名！
        ImmutableMap<String, Object> columnMap = ImmutableMap.<String, Object>builder()
                .put(User.NAME, "李清照")
                .put(User.AGE, 24)
                .build();
        List<User> users = this.userDao.selectByMap(columnMap);
        Assert.assertEquals(users.size(), 1);
    }

    @Test
    public void testSelectList() {
        // likeRight(User::getName, "王") -> like '王%'
        // like(User::getName, "雨") -> like '%雨%'
        List<User> users = this.userDao.selectList(
                Wrappers.<User>lambdaQuery()
                        .like(User::getName, "雨")
                        .or()
                        .between(User::getAge, 20, 40)
                        .isNotNull(User::getEmail)
                        .orderByDesc(User::getAge)
                        .orderByAsc(User::getId)
        );
        Assert.assertEquals(users.size(), 6);
    }

    @Test(expected = BadSqlGrammarException.class)
    public void testSelectBatchByIds() {
        // selectBatchIds 参数不能为Null或空数组
        this.userDao.selectBatchIds(Collections.emptyList());
    }

    @Test
    public void testSelectSubQuery() {
        List<User> users = this.userDao.selectList(
                Wrappers.<User>lambdaQuery()
                        // date_format函数格式化日期格式，{0} 防止SQL注入
                        .apply("date_format(" + User.CREATE_TIME + ",'%Y-%m-%d') = {0}", "2019-02-14")
                        // 子查询
                        .inSql(User::getManagerId, "select id from user where name like '王%'")
        );
        Assert.assertEquals(users.size(), 1);
    }


    @Test
    public void testSelectAnd() {
        List<User> users = this.userDao.selectList(
                Wrappers.<User>lambdaQuery()
                        .likeRight(User::getName, "王")
                        .and(query -> query.le(User::getAge, 30).or().isNotNull(User::getEmail))
        );
        Assert.assertEquals(users.size(), 1);
    }

    @Test
    public void testSelectNested() {
        List<User> users = this.userDao.selectList(
                Wrappers.<User>lambdaQuery()
                        .nested(query -> query.lt(User::getAge, 40).or().isNotNull(User::getEmail))
                        .likeRight(User::getName, "王")
        );
        Assert.assertEquals(users.size(), 1);
    }

    @Test
    public void testSelectOne() {
        // last("limit 1") 限制只查询一条，注意此处可能存在SQL注入的风险！
        List<User> users = this.userDao.selectList(Wrappers.<User>lambdaQuery().last("limit 1"));
        Assert.assertTrue(CollectionUtils.isEmpty(users));

        // 此处返回多行数据如果使用selectOne方法会报错
        User user = this.userDao.selectOne(Wrappers.emptyWrapper());
        Assert.assertNotNull(user);
    }

    @Test
    public void testSelectName() {
        List<User> users = this.userDao.selectList(Wrappers.<User>lambdaQuery()
                // 指定需要查询的列
                .select(User::getName)
                .like(User::getName, "雨")
                .le(User::getAge, 40));
        Assert.assertFalse(CollectionUtils.isEmpty(users));

        // 使用Predicate函数排除不需要查询的列
        Predicate<TableFieldInfo> exceptColumnPredicate = tableFieldInfo -> !tableFieldInfo.getColumn().equals(User.CREATE_TIME) && !tableFieldInfo.getColumn().equals(User.MANAGER_ID);
        List<User> userList = this.userDao.selectList(Wrappers.<User>lambdaQuery()
                .select(User.class, exceptColumnPredicate)
                .like(User::getName, "雨")
                .le(User::getAge, 40));
        Assert.assertFalse(CollectionUtils.isEmpty(userList));
    }

    @Test
    public void testSelectEntity() {
        User queryConditionEntity = new User()
                .setName("雨")
                .setAge(31);
        User user = this.userDao.selectOne(Wrappers.lambdaQuery(queryConditionEntity));
        Assert.assertNotNull(user);
    }

    @Test
    public void testSelectCondition() {
        String name = "雨";
        boolean condition = !StringUtils.isEmpty(name);
        // 当且仅当condition=true时，查询条件才生效
        List<User> users = this.userDao.selectList(Wrappers.<User>lambdaQuery()
                .like(condition, User::getName, name));
        Assert.assertFalse(CollectionUtils.isEmpty(users));
    }

    @Test
    public void testSelectAllEq() {
        Map<String, Object> paramMap = Maps.newHashMap();
        paramMap.put(User.NAME, "张雨琪");
        paramMap.put(User.AGE, null);
        List<User> users = this.userDao.selectList(Wrappers.<User>query()
                .allEq(true, (key, value) -> key.equals("name"), paramMap, true));
        Assert.assertFalse(CollectionUtils.isEmpty(users));
    }

    @Test
    public void testSelectMaps() {
        // SELECT avg(age) avg_age,min(age) min_avg,max(age) max_age FROM user GROUP BY manager_id HAVING sum(age) < 500
        List<Map<String, Object>> result = this.userDao.selectMaps(Wrappers.<User>query()
                .select("avg(age) avg_age,min(age) min_avg,max(age) max_age")
                .groupBy(User.MANAGER_ID)
                .having("sum(age) < {0}", 500));
        Assert.assertFalse(CollectionUtils.isEmpty(result));
    }

    @Test
    public void testSelectObjs() {
        // selectObjs 只返回第一列数据
        List<Object> result = this.userDao.selectObjs(Wrappers.<User>lambdaQuery()
                .like(User::getName, "雨"));
        Assert.assertFalse(CollectionUtils.isEmpty(result));
    }

    @Test
    public void testLambdaQueryChainWrapper() {
        List<User> users = new LambdaQueryChainWrapper<>(this.userDao)
                .like(User::getName, "雨")
                .ge(User::getAge, 20)
                .list();
        Assert.assertFalse(CollectionUtils.isEmpty(users));
    }

    @Test
    public void testCustomerSelectSql() {
        List<User> users = this.userDao.selectAll(Wrappers.<User>lambdaQuery().like(User::getName, "雨"));
        Assert.assertFalse(CollectionUtils.isEmpty(users));

        List<User> userList = this.userDao.selectByCondition("雨", -1);
        Assert.assertFalse(CollectionUtils.isEmpty(userList));
    }

    @Test
    public void testSelectPage() {
        // Page对象的isSearchCount参数为是否使用count函数查询total
        IPage<User> page = this.userDao.selectPage(new Page<>(1, 10, true), Wrappers.emptyWrapper());
        Assert.assertNotNull(page);
    }

    @Test
    public void testUpdateById() {
        User updateUser = new User().setId(1094590409767661570L).setAge(24);
        int effectRowCount = this.userDao.updateById(updateUser);
        Assert.assertTrue(effectRowCount > 0);
    }

    @Test
    public void testUpdate() {
        User updateUser = new User().setAge(22);
        int effectRowCount = this.userDao.update(updateUser, Wrappers.<User>lambdaQuery()
                .eq(User::getName, "张雨琪")
                .eq(User::getAge, 24));
        Assert.assertTrue(effectRowCount > 0);
    }

    @Test
    public void testLambdaUpdateChainWrapper() {
        boolean updateResult = new LambdaUpdateChainWrapper<>(this.userDao)
                .set(User::getAge, 23)
                .eq(User::getName, "张雨琪")
                .eq(User::getAge, 22)
                .update();
        Assert.assertTrue(updateResult);
    }

    @Test
    public void testDeleteByMap() {
        Map<String, Object> columnMap = ImmutableMap.of(User.NAME, "李清照", User.AGE, 24);
        int effectRowCount = this.userDao.deleteByMap(columnMap);
        Assert.assertTrue(effectRowCount > 0);
    }

    @Test
    public void testDelete() {
        int effectRowCount = this.userDao.delete(Wrappers.<User>lambdaQuery()
                .eq(User::getName, "张雨琪").or().eq(User::getAge, 18));
        Assert.assertEquals(1, effectRowCount);
    }

    @Test
    public void testInsert() {
        User user = new User()
                .setAge(24)
                .setCreateTime(LocalDateTime.now())
                .setEmail("401664157@qq.com")
                .setManagerId(1088248166370832385L)
                .setName("李清照");
        int effectRowCount = this.userDao.insert(user);
        Assert.assertEquals(1, effectRowCount);
    }
}

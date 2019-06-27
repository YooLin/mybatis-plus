package com.imooc.mybatisplus.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.imooc.mybatisplus.constant.ExpandedSqlCondition;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author linyicong
 * @since 2019-06-22
 */
@Data
@Accessors(chain = true)
public class User {
    /**
     * 默认使用雪花算法生成主键ID
     */
    private Long id;
    /**
     * 当使用实体作为查询参数时，条件为 like
     */
    @TableField(condition = SqlCondition.LIKE)
    private String name;
    /**
     * 当使用实体作为查询参数时，条件为 <=
     */
    @TableField(condition = ExpandedSqlCondition.LESS_THAN)
    private Integer age;
    private String email;
    /**
     * 直属上级id
     */
    private Long managerId;
    private LocalDateTime createTime;

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String AGE = "age";
    public static final String EMAIL = "email";
    public static final String MANAGER_ID = "manager_id";
    public static final String CREATE_TIME = "create_time";

}

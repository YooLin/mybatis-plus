package com.imooc.mybatisplus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imooc.mybatisplus.dao.UserDao;
import com.imooc.mybatisplus.entity.User;
import com.imooc.mybatisplus.service.IUserService;
import org.springframework.stereotype.Service;

/**
 * @author linyicong
 * @since 2019-06-28
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserDao, User> implements IUserService {
}

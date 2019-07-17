package com.imooc.mybatisplus.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.imooc.mybatisplus.entity.User;
import com.imooc.mybatisplus.service.IUserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author linyicong
 * @since 2019-07-17
 */
@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {
    private IUserService userService;

    @GetMapping
    public User search(User condition) {
        return this.userService.getOne(Wrappers.query(condition));
    }

    @GetMapping("/list")
    public List<User> list(User condition) {
        return this.userService.list(Wrappers.query(condition));
    }
}

package com.petservices.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petservices.config.wrapResult.DontWrapResult;
import com.petservices.dto.HealthReminderDto;
import com.petservices.entity.ContactMessage;
import com.petservices.entity.ContactMessage;
import com.petservices.entity.HealthReminderMessage;
import com.petservices.entity.User;
import com.petservices.service.ContactMessageService;
import com.petservices.service.ContactMessageService;
import com.petservices.service.HealthReminderMessageService;
import com.petservices.service.HealthReminderPipelineService;
import com.petservices.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;  // 确保导入
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author ${author}
 * @since 2025-02-23
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;
    @Autowired
    HealthReminderPipelineService healthReminderPipelineService;
    @Autowired
    HealthReminderMessageService healthReminderMessageService;
    @Autowired
    ContactMessageService contactMessageService;

    /**
     * 用户登录
     *
     * @param username
     * @param password
     * @return
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/login/{username}/{password}")
    public User login(@PathVariable String username, @PathVariable String password, HttpSession session) {
        System.out.println("=== 登录方法被调用，用户名: " + username + "，密码: " + password);
        User user = userService.login(username, password);
        System.out.println("查询到的用户: " + user);
        if (user != null) {
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("userRole", user.getRole());
            if ("vet".equals(user.getRole())) {
                session.setAttribute("vetId", user.getUserId());
            }
            System.out.println("已将 userId=" + user.getUserId() + " 存入 session");
            System.out.println("session ID: " + session.getId());
        } else {
            System.out.println("用户不存在，未设置 session");
        }
        return user;
    }

    /**
     * 获取所有用户 分页
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    @GetMapping("/getUserList/{currentPage}/{pageSize}")
    public Page<User> getUserList(@PathVariable Integer currentPage,
                                  @PathVariable Integer pageSize) {
        return userService.getUserList(currentPage, pageSize);
    }

    /**
     * 修改用户信息
     *
     * @param user
     */
    @PostMapping("/updateUser")
    public void updateUser(@RequestBody User user) {
        userService.updateUser(user);
    }

    /**
     * 添加用户
     *
     * @param user
     * @return
     */
    @PostMapping("/register")
    public void register(@RequestBody User user) {
        userService.register(user);
    }

    /**
     * 删除用户信息
     *
     * @param userId
     * @return
     */
    @GetMapping("/deleteUser/{userId}")
    public Page<User> deleteUser(@PathVariable Integer userId) {
        return userService.deleteUser(userId);
    }

    /**
     * 查找用户
     *
     * @param user
     * @return
     */
    @PostMapping("/searchUser")
    public List<User> searchUser(@RequestBody User user) {
        return userService.searchUser(user.getName(), user.getPhone());
    }

    /**
     * 用户修改头像
     * @param file
     * @param userId
     * @return
     */
    @DontWrapResult
    @PostMapping("/updateMyAvatar")
    public String updateMyAvatar(@RequestParam("file") MultipartFile file ,
                                 @RequestParam("userId") Integer userId) {
        return userService.updateMyAvatar(file , userId);
    }

    /**
     * 修改个人信息
     * @param user
     */
    @PostMapping ("/updateMyInfo")
    public void updateMyInfo(@RequestBody User user){
        userService.updateMyInfo(user);
    }

    /**
     * 管理员重置密用户码
     * @param userId
     * @return
     */
    @GetMapping("/resetPassword/{userId}")
    public Page<User> resetPassword(@PathVariable Integer userId) {
        return userService.resetPassword(userId);
    }

    /**
     * 查询健康提醒订阅状态
     */
    @GetMapping("/healthReminderSubscribed/{userId}")
    public Integer getHealthReminderSubscribed(@PathVariable Integer userId) {
        return userService.getHealthReminderSubscribed(userId);
    }

    /**
     * 更新健康提醒订阅状态
     */
    @PostMapping("/healthReminderSubscribed/{userId}/{subscribed}")
    public Integer updateHealthReminderSubscribed(@PathVariable Integer userId,
                                                  @PathVariable Integer subscribed) {
        return userService.updateHealthReminderSubscribed(userId, subscribed);
    }

    /**
     * 手动触发一次推送（调试/演示）
     */
    @PostMapping("/healthReminderDispatch/{userId}")
    public Integer triggerHealthReminderDispatch(@PathVariable Integer userId) {
        return healthReminderPipelineService.generateAndDispatch(userId);
    }

    /**
     * 查询推送消息记录
     */
    @GetMapping("/healthReminderMessages/{userId}")
    public List<HealthReminderMessage> getHealthReminderMessages(@PathVariable Integer userId,
                                                                 @RequestParam(value = "status", required = false) Integer status) {
        return healthReminderMessageService.getMessages(userId, status);
    }

    /**
     * 手动重发单条消息
     */
    @PostMapping("/healthReminderMessages/resend/{messageId}")
    public Boolean resendHealthReminderMessage(@PathVariable Integer messageId) {
        return healthReminderMessageService.resendMessage(messageId);
    }

    /**
     * 用户提交客服留言
     */
    @PostMapping("/contactMessage/submit")
    public Boolean submitContactMessage(@RequestBody ContactMessage message) {
        return contactMessageService.submit(message);
    }

    /**
     * 用户查看自己的留言记录
     */
    @GetMapping("/contactMessage/list/{userId}")
    public List<ContactMessage> listMyContactMessages(@PathVariable Integer userId) {
        return contactMessageService.listByUser(userId);
    }

    /**
     * 用户删除自己的留言
     */
    @DeleteMapping("/contactMessage/delete/{messageId}/{userId}")
    public Boolean deleteMyContactMessage(@PathVariable Integer messageId,
                                          @PathVariable Integer userId) {
        return contactMessageService.deleteByUser(messageId, userId);
    }

}


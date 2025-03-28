package edu.ntu.ai.exam.controller;

import edu.ntu.ai.exam.model.User;
import edu.ntu.ai.exam.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    // 添加GET方法处理登录页面请求
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";  // 返回login.html页面
    }

    // POST方法处理登录请求
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        Model model,
                        HttpSession session) {
        User user = userService.login(username, password);
        if (user != null) {
            session.setAttribute("user", user);
            // 根据用户角色决定跳转页面
            if ("TEACHER".equals(user.getRole())) {
                return "redirect:/question/list";  // 教师默认跳转到题库管理
            } else {
                return "redirect:/exam";  // 学生跳转到考试页面
            }
        } else {
            model.addAttribute("error", "用户名或密码错误");
            return "login";
        }
    }

    // 添加GET方法处理注册页面请求
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";  // 返回register.html页面
    }

    // POST方法处理注册请求
    @PostMapping("/register")
    public String register(@ModelAttribute User user, Model model) {
        try {
            userService.register(user);
            return "redirect:/user/login";  // 注册成功跳转到登录页面
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    // 退出登录
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/user/login";
    }
}
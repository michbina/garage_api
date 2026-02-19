package com.garage.controller;

import com.garage.model.User;
import com.garage.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    @Autowired
    private UserService userService;
    
    @GetMapping("/api/user/info")
    @PreAuthorize("isAuthenticated()")
    public String getUserInfo(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName());
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "user-info";
    }
}
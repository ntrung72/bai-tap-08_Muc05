package vn.iotstar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/admin/category/list")
    public String oldCategoryListUrl() {
        return "redirect:/admin/categories";
    }

    @GetMapping("/admin/user/list")
    public String oldUserListUrl() {
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/product/list")
    public String oldProductListUrl() {
        return "redirect:/admin/products";
    }
}

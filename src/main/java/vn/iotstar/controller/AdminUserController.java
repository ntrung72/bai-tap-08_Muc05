package vn.iotstar.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import vn.iotstar.model.User;
import vn.iotstar.service.UserService;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {
    private final UserService service;

    public AdminUserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword, Model model) {
        model.addAttribute("users", service.findAll(keyword));
        model.addAttribute("keyword", keyword.trim());
        model.addAttribute("activeMenu", "user");
        return "admin/user/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("formTitle", "Thêm người dùng");
        model.addAttribute("activeMenu", "user");
        return "admin/user/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes redirect) {
        try {
            User user = service.findById(id);
            user.setPassWord("");
            model.addAttribute("user", user);
            model.addAttribute("formTitle", "Cập nhật người dùng");
            model.addAttribute("activeMenu", "user");
            return "admin/user/form";
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("user") User user, BindingResult result,
            Model model, RedirectAttributes redirect) {
        boolean creating = user.getId() == null;
        String submittedPassword = user.getPassWord() == null ? "" : user.getPassWord();

        if (creating && submittedPassword.isBlank()) {
            result.rejectValue("passWord", "required", "Mật khẩu không được để trống.");
        } else if (!submittedPassword.isBlank()
                && (submittedPassword.length() < 6
                || submittedPassword.length() > 72)) {
            result.rejectValue("passWord", "size", "Mật khẩu phải từ 6 đến 72 ký tự.");
        }
        if (service.userNameExists(user.getUserName(), user.getId())) {
            result.rejectValue("userName", "duplicate", "Tên đăng nhập đã tồn tại.");
        }
        if (service.emailExists(user.getEmail(), user.getId())) {
            result.rejectValue("email", "duplicate", "Email đã tồn tại.");
        }

        User oldUser = null;
        if (!creating) {
            try {
                oldUser = service.findById(user.getId());
            } catch (IllegalArgumentException ex) {
                result.reject("notFound", ex.getMessage());
            }
        }

        if (result.hasErrors()) {
            model.addAttribute("formTitle", creating ? "Thêm người dùng" : "Cập nhật người dùng");
            model.addAttribute("activeMenu", "user");
            return "admin/user/form";
        }

        if (!creating && submittedPassword.isBlank()) {
            user.setPassWord(oldUser.getPassWord());
        }
        service.save(user);
        redirect.addFlashAttribute("success", creating
                ? "Thêm người dùng thành công." : "Cập nhật người dùng thành công.");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            service.deleteById(id);
            redirect.addFlashAttribute("success", "Xóa người dùng thành công.");
        } catch (DataIntegrityViolationException ex) {
            redirect.addFlashAttribute(
                    "error",
                    "Không thể xóa vì người dùng đang có dữ liệu liên quan.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/users";
    }
}

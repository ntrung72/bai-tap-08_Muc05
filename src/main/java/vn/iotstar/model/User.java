package vn.iotstar.model;

import java.io.Serializable;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "[User]", schema = "dbo")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotBlank(message = "Email không được để trống.")
    @Email(message = "Email không đúng định dạng.")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự.")
    @Column(name = "email")
    private String email;

    @NotBlank(message = "Tên đăng nhập không được để trống.")
    @Size(min = 3, max = 50, message = "Tên đăng nhập phải từ 3 đến 50 ký tự.")
    @Pattern(
            regexp = "^[A-Za-z0-9._]+$",
            message = "Tên đăng nhập chỉ gồm chữ cái, số, dấu chấm và dấu gạch dưới.")
    @Column(name = "username")
    private String userName;

    @NotBlank(message = "Họ tên không được để trống.")
    @Size(min = 2, max = 100, message = "Họ tên phải từ 2 đến 100 ký tự.")
    @Column(name = "fullname", columnDefinition = "NVARCHAR(255)")
    private String fullName;

    @Column(name = "password")
    private String passWord;

    @Size(max = 255, message = "Đường dẫn ảnh đại diện không được vượt quá 255 ký tự.")
    @Column(name = "avatar", columnDefinition = "NVARCHAR(255)")
    private String avatar;

    @Min(value = 1, message = "Vai trò không hợp lệ.")
    @Max(value = 3, message = "Vai trò không hợp lệ.")
    @Column(name = "roleid")
    private int roleid = 3;

    @NotBlank(message = "Số điện thoại không được để trống.")
    @Pattern(regexp = "^[0-9]{9,11}$", message = "Số điện thoại phải gồm 9 đến 11 chữ số.")
    @Column(name = "phone", columnDefinition = "NVARCHAR(50)")
    private String phone;

    @Column(name = "createdDate")
    private LocalDate createdDate;

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public String getPassWord() {
        return passWord;
    }
    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }
    public String getAvatar() {
        return avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    public int getRoleid() {
        return roleid;
    }
    public void setRoleid(int roleid) {
        this.roleid = roleid;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public LocalDate getCreatedDate() {
        return createdDate;
    }
    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }
}

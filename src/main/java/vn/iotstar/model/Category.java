package vn.iotstar.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "Category", schema = "dbo")
public class Category implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cate_id")
    private Integer id;

    @NotBlank(message = "Tên danh mục không được để trống.")
    @Size(min = 2, max = 255, message = "Tên danh mục phải từ 2 đến 255 ký tự.")
    @Column(name = "cate_name", columnDefinition = "NVARCHAR(255)")
    private String name;

    @Size(max = 255, message = "Đường dẫn biểu tượng không được vượt quá 255 ký tự.")
    @Column(name = "icons", columnDefinition = "NVARCHAR(255)")
    private String icon;

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }
}

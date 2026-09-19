# Bài tập GraphQL + AJAX + Thymeleaf

## Chức năng đã thực hiện

- Trang chủ `/` hiển thị tất cả sản phẩm theo giá tăng dần.
- Chọn một danh mục trên trang chủ để lấy toàn bộ sản phẩm của danh mục đó.
- CRUD Category qua GraphQL và AJAX.
- Tìm kiếm, phân trang Category qua GraphQL và AJAX.
- CRUD Product qua GraphQL và AJAX.
- Tìm kiếm, phân trang Product qua GraphQL và AJAX.
- Giao diện được render từ template Thymeleaf.
- Giữ chức năng tải ảnh vào thư mục cấu hình bởi `storage.location`.
- Giữ chức năng quản lý User của project ban đầu và chuyển view sang Thymeleaf.

## Đường dẫn chạy

- Trang chủ: `http://localhost:8080/`
- Quản lý danh mục: `http://localhost:8080/admin/categories`
- Quản lý sản phẩm: `http://localhost:8080/admin/products`
- GraphQL endpoint: `http://localhost:8080/graphql`
- GraphiQL: `http://localhost:8080/graphiql`

## Query chính

```graphql
query {
  productsByPriceAsc {
    id
    name
    price
    category {
      id
      name
    }
  }
}
```

```graphql
query {
  productsByCategory(categoryId: 1) {
    id
    name
    price
  }
}
```

```graphql
query {
  productPage(keyword: "trà", page: 0, size: 5) {
    content {
      id
      name
      price
    }
    currentPage
    totalPages
    totalItems
  }
}
```

```graphql
query {
  categoryPage(keyword: "đồ uống", page: 0, size: 5) {
    content {
      id
      name
    }
    currentPage
    totalPages
    totalItems
  }
}
```

## Mutation mẫu

```graphql
mutation {
  createCategory(input: {name: "Trà sữa"}) {
    success
    message
    category {
      id
      name
    }
  }
}
```

```graphql
mutation {
  createProduct(
    input: {
      name: "Trà sữa truyền thống"
      quantity: 20
      price: 25000
      description: "Size M"
      categoryId: 1
    }
  ) {
    success
    message
    product {
      id
      name
    }
  }
}
```

## Chạy project

1. Kiểm tra SQL Server và database `BaiTap`.
2. Kiểm tra tài khoản SQL Server trong `application.properties`.
3. Kiểm tra hoặc đổi `storage.location=E:/upload`.
4. Trong Spring Tool Suite, chọn Maven > Update Project.
5. Chạy `ShopNamTrungApplication` bằng Java 26.

Có thể kiểm thử bằng lệnh:

```bash
./mvnw test
```

Hai test tích hợp trong `ShopGraphqlIntegrationTests` kiểm tra:

- sắp xếp sản phẩm theo giá tăng dần;
- lọc sản phẩm theo danh mục;
- tìm kiếm và phân trang;
- mutation thêm, sửa, xóa Category và Product.

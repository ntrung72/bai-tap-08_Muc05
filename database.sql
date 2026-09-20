USE [master];
GO

IF DB_ID(N'BaiTap') IS NULL
BEGIN
    EXEC(N'CREATE DATABASE [BaiTap] COLLATE Vietnamese_CI_AS');
END;
GO

USE [BaiTap];
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;
GO



IF OBJECT_ID(N'dbo.Category', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Category
    (
        cate_id INT IDENTITY(1, 1) NOT NULL,
        cate_name NVARCHAR(255) NOT NULL,
        icons NVARCHAR(255) NULL,

        CONSTRAINT PK_Category
            PRIMARY KEY (cate_id)
    );
END;
GO



IF OBJECT_ID(N'dbo.Products', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Products
    (
        product_id INT IDENTITY(1, 1) NOT NULL,
        product_name NVARCHAR(255) NOT NULL,

        quantity INT NOT NULL
            CONSTRAINT DF_Products_Quantity DEFAULT (0),

        price DECIMAL(18, 2) NOT NULL
            CONSTRAINT DF_Products_Price DEFAULT (0),

        images NVARCHAR(255) NULL,
        description NVARCHAR(MAX) NULL,
        cate_id INT NOT NULL,

        CONSTRAINT PK_Products
            PRIMARY KEY (product_id),

        CONSTRAINT CK_Products_Quantity
            CHECK (quantity >= 0),

        CONSTRAINT CK_Products_Price
            CHECK (price >= 0),

        CONSTRAINT FK_Products_Category
            FOREIGN KEY (cate_id)
            REFERENCES dbo.Category(cate_id)
    );
END;
GO



IF OBJECT_ID(N'dbo.Products', N'U') IS NOT NULL
    AND OBJECT_ID(N'dbo.Category', N'U') IS NOT NULL
    AND NOT EXISTS
    (
        SELECT 1
        FROM sys.foreign_key_columns AS existing_fk
        WHERE existing_fk.parent_object_id =
              OBJECT_ID(N'dbo.Products')
          AND existing_fk.referenced_object_id =
              OBJECT_ID(N'dbo.Category')
          AND COL_NAME(
                  existing_fk.parent_object_id,
                  existing_fk.parent_column_id
              ) = N'cate_id'
          AND COL_NAME(
                  existing_fk.referenced_object_id,
                  existing_fk.referenced_column_id
              ) = N'cate_id'
    )
    AND NOT EXISTS
    (
        SELECT 1
        FROM dbo.Products AS product
        LEFT JOIN dbo.Category AS category
            ON category.cate_id = product.cate_id
        WHERE category.cate_id IS NULL
    )
BEGIN
    ALTER TABLE dbo.Products WITH CHECK
    ADD CONSTRAINT FK_Products_Category
        FOREIGN KEY (cate_id)
        REFERENCES dbo.Category(cate_id);

    ALTER TABLE dbo.Products
    CHECK CONSTRAINT FK_Products_Category;
END;
GO



IF OBJECT_ID(N'dbo.[User]', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.[User]
    (
        id INT IDENTITY(1, 1) NOT NULL,
        email NVARCHAR(100) NOT NULL,
        username NVARCHAR(50) NOT NULL,
        fullname NVARCHAR(255) NOT NULL,
        [password] NVARCHAR(255) NOT NULL,
        avatar NVARCHAR(255) NULL,

        roleid INT NOT NULL
            CONSTRAINT DF_User_RoleId DEFAULT (3),

        phone NVARCHAR(50) NOT NULL,

        created_date DATE NULL
            CONSTRAINT DF_User_CreatedDate
            DEFAULT (CONVERT(DATE, GETDATE())),

        CONSTRAINT PK_User
            PRIMARY KEY (id),

        CONSTRAINT CK_User_RoleId
            CHECK (roleid BETWEEN 1 AND 3)
    );
END;
GO



IF OBJECT_ID(N'dbo.[User]', N'U') IS NOT NULL
    AND EXISTS
    (
        SELECT 1
        FROM sys.columns
        WHERE object_id = OBJECT_ID(N'dbo.[User]')
          AND name = N'createdDate'
    )
    AND NOT EXISTS
    (
        SELECT 1
        FROM sys.columns
        WHERE object_id = OBJECT_ID(N'dbo.[User]')
          AND name = N'created_date'
    )
BEGIN
    EXEC sys.sp_rename
        N'dbo.[User].createdDate',
        N'created_date',
        N'COLUMN';
END;
GO



IF OBJECT_ID(N'dbo.[User]', N'U') IS NOT NULL
    AND NOT EXISTS
    (
        SELECT 1
        FROM sys.columns
        WHERE object_id = OBJECT_ID(N'dbo.[User]')
          AND name = N'created_date'
    )
BEGIN
    ALTER TABLE dbo.[User]
    ADD created_date DATE NULL
        CONSTRAINT DF_User_CreatedDate_Auto
        DEFAULT (CONVERT(DATE, GETDATE()));
END;
GO


IF NOT EXISTS
(
    SELECT 1
    FROM sys.indexes
    WHERE name = N'IX_Category_Name'
      AND object_id = OBJECT_ID(N'dbo.Category')
)
BEGIN
    CREATE INDEX IX_Category_Name
        ON dbo.Category(cate_name);
END;
GO



IF NOT EXISTS
(
    SELECT 1
    FROM sys.indexes
    WHERE name = N'IX_Products_Name'
      AND object_id = OBJECT_ID(N'dbo.Products')
)
BEGIN
    CREATE INDEX IX_Products_Name
        ON dbo.Products(product_name);
END;
GO

IF NOT EXISTS
(
    SELECT 1
    FROM sys.indexes
    WHERE name = N'IX_Products_Price'
      AND object_id = OBJECT_ID(N'dbo.Products')
)
BEGIN
    CREATE INDEX IX_Products_Price
        ON dbo.Products(price);
END;
GO

IF NOT EXISTS
(
    SELECT 1
    FROM sys.indexes
    WHERE name = N'IX_Products_Category'
      AND object_id = OBJECT_ID(N'dbo.Products')
)
BEGIN
    CREATE INDEX IX_Products_Category
        ON dbo.Products(cate_id);
END;
GO



IF NOT EXISTS
(
    SELECT 1
    FROM sys.indexes
    WHERE name = N'UX_User_Email'
      AND object_id = OBJECT_ID(N'dbo.[User]')
)
AND NOT EXISTS
(
    SELECT email
    FROM dbo.[User]
    GROUP BY email
    HAVING COUNT(*) > 1
)
BEGIN
    CREATE UNIQUE INDEX UX_User_Email
        ON dbo.[User](email);
END;
GO

IF NOT EXISTS
(
    SELECT 1
    FROM sys.indexes
    WHERE name = N'UX_User_Username'
      AND object_id = OBJECT_ID(N'dbo.[User]')
)
AND NOT EXISTS
(
    SELECT username
    FROM dbo.[User]
    GROUP BY username
    HAVING COUNT(*) > 1
)
BEGIN
    CREATE UNIQUE INDEX UX_User_Username
        ON dbo.[User](username);
END;
GO



IF NOT EXISTS (SELECT 1 FROM dbo.Category)
BEGIN
    INSERT INTO dbo.Category
    (
        cate_name,
        icons
    )
    VALUES
        (N'Quần áo nam', NULL),
        (N'Quần áo nữ', NULL),
        (N'Phụ kiện', NULL);
END;
GO


IF NOT EXISTS (SELECT 1 FROM dbo.Products)
BEGIN
    DECLARE @CategoryOne INT;
    DECLARE @CategoryTwo INT;
    DECLARE @CategoryThree INT;

    SELECT @CategoryOne = MIN(cate_id)
    FROM dbo.Category;

    SELECT @CategoryTwo = MIN(cate_id)
    FROM dbo.Category
    WHERE cate_id > @CategoryOne;

    SELECT @CategoryThree = MIN(cate_id)
    FROM dbo.Category
    WHERE cate_id > ISNULL(@CategoryTwo, @CategoryOne);

    SET @CategoryTwo =
        ISNULL(@CategoryTwo, @CategoryOne);

    SET @CategoryThree =
        ISNULL(@CategoryThree, @CategoryOne);

    IF @CategoryOne IS NOT NULL
    BEGIN
        INSERT INTO dbo.Products
        (
            product_name,
            quantity,
            price,
            images,
            description,
            cate_id
        )
        VALUES
            (
                N'Áo thun nam',
                20,
                120000,
                NULL,
                N'Áo thun nam cơ bản.',
                @CategoryOne
            ),
            (
                N'Áo sơ mi',
                15,
                180000,
                NULL,
                N'Áo sơ mi thanh lịch.',
                @CategoryOne
            ),
            (
                N'Váy nữ',
                12,
                220000,
                NULL,
                N'Váy nữ thời trang.',
                @CategoryTwo
            ),
            (
                N'Túi đeo chéo',
                10,
                150000,
                NULL,
                N'Túi đeo chéo tiện dụng.',
                @CategoryThree
            );
    END;
END;
GO




IF NOT EXISTS (SELECT 1 FROM dbo.[User])
BEGIN
    INSERT INTO dbo.[User]
    (
        email,
        username,
        fullname,
        [password],
        avatar,
        roleid,
        phone,
        created_date
    )
    VALUES
        (
            N'admin@gmail.com',
            N'admin',
            N'Quản trị viên',
            N'123456',
            NULL,
            1,
            N'0900000001',
            CONVERT(DATE, GETDATE())
        ),
        (
            N'user@gmail.com',
            N'user',
            N'Người dùng',
            N'123456',
            NULL,
            3,
            N'0900000002',
            CONVERT(DATE, GETDATE())
        );
END;
GO



SELECT DB_NAME() AS current_database;
GO

SELECT
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = N'dbo'
  AND TABLE_NAME IN
  (
      N'Category',
      N'Products',
      N'User'
  )
ORDER BY
    TABLE_NAME,
    ORDINAL_POSITION;
GO

SELECT
    fk.name AS foreign_key_name,
    OBJECT_NAME(fk.parent_object_id) AS child_table,
    COL_NAME(
        fkc.parent_object_id,
        fkc.parent_column_id
    ) AS child_column,
    OBJECT_NAME(
        fk.referenced_object_id
    ) AS parent_table,
    COL_NAME(
        fkc.referenced_object_id,
        fkc.referenced_column_id
    ) AS parent_column
FROM sys.foreign_keys AS fk
JOIN sys.foreign_key_columns AS fkc
    ON fk.object_id = fkc.constraint_object_id
WHERE fk.parent_object_id =
      OBJECT_ID(N'dbo.Products');
GO

SELECT COUNT(*) AS category_count
FROM dbo.Category;

SELECT COUNT(*) AS product_count
FROM dbo.Products;

SELECT COUNT(*) AS user_count
FROM dbo.[User];
GO
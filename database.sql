USE BaiTap;
GO

SELECT DB_NAME() AS current_database;

SELECT
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'dbo'
  AND TABLE_NAME IN ('Category', 'Products', 'User')
ORDER BY TABLE_NAME, ORDINAL_POSITION;

SELECT
    fk.name AS foreign_key_name,
    OBJECT_NAME(fk.parent_object_id) AS child_table,
    COL_NAME(fkc.parent_object_id, fkc.parent_column_id) AS child_column,
    OBJECT_NAME(fk.referenced_object_id) AS parent_table,
    COL_NAME(fkc.referenced_object_id, fkc.referenced_column_id) AS parent_column
FROM sys.foreign_keys fk
JOIN sys.foreign_key_columns fkc
    ON fk.object_id = fkc.constraint_object_id
WHERE OBJECT_NAME(fk.parent_object_id) = 'Products';
GO

document.addEventListener('DOMContentLoaded', () => {
    const tableBody = document.getElementById('product-table-body');
    const pagination = document.getElementById('product-pagination');
    const pageSummary = document.getElementById('product-page-summary');
    const form = document.getElementById('product-form');
    const modal = document.getElementById('product-modal');
    const message = document.getElementById('product-message');
    const keywordInput = document.getElementById('product-keyword');

    const fields = {
        id: document.getElementById('product-id'),
        name: document.getElementById('product-name'),
        categoryId: document.getElementById('product-category'),
        price: document.getElementById('product-price'),
        quantity: document.getElementById('product-quantity'),
        imageFile: document.getElementById('product-image'),
        description: document.getElementById('product-description')
    };

    const state = {
        page: 0,
        size: 5,
        keyword: ''
    };

    function showMessage(text, type) {
        message.textContent = text;
        message.className = `alert ${type}`;
        message.hidden = false;
    }

    function createCell(text) {
        const cell = document.createElement('td');
        cell.textContent = text;
        return cell;
    }

    function createImageCell(product) {
        const cell = document.createElement('td');
        const source = shopApi.imageUrl(product.image, 'product');
        const placeholder = document.createElement('span');
        placeholder.className = 'image-placeholder';
        placeholder.textContent = 'Chưa có ảnh';

        if (!source) {
            cell.appendChild(placeholder);
            return cell;
        }

        const image = document.createElement('img');
        image.className = 'table-image';
        image.src = source;
        image.alt = product.name;
        image.loading = 'lazy';
        image.addEventListener(
                'error',
                () => image.replaceWith(placeholder),
                {once: true});
        cell.appendChild(image);
        return cell;
    }

    function actionButton(label, className, handler) {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = `btn ${className}`;
        button.textContent = label;
        button.addEventListener('click', handler);
        return button;
    }

    function renderRows(products) {
        tableBody.replaceChildren();

        if (products.length === 0) {
            const row = document.createElement('tr');
            const cell = createCell('Không tìm thấy sản phẩm phù hợp.');
            cell.colSpan = 7;
            cell.className = 'empty';
            row.appendChild(cell);
            tableBody.appendChild(row);
            return;
        }

        products.forEach(product => {
            const row = document.createElement('tr');
            row.appendChild(createCell(product.id));
            row.appendChild(createImageCell(product));

            const nameCell = createCell('');
            const strong = document.createElement('strong');
            strong.textContent = product.name;
            nameCell.appendChild(strong);
            row.appendChild(nameCell);

            row.appendChild(createCell(product.category.name));
            row.appendChild(createCell(shopApi.formatCurrency(product.price)));
            row.appendChild(createCell(product.quantity));

            const actionsCell = document.createElement('td');
            const actions = document.createElement('div');
            actions.className = 'action-group';
            actions.appendChild(actionButton(
                    'Sửa',
                    'btn-warning',
                    () => openEdit(product.id)));
            actions.appendChild(actionButton(
                    'Xóa',
                    'btn-danger',
                    () => remove(product.id, product.name)));
            actionsCell.appendChild(actions);
            row.appendChild(actionsCell);
            tableBody.appendChild(row);
        });
    }

    function renderPagination(pageData) {
        pagination.replaceChildren();
        const shownPage = pageData.totalPages === 0
                ? 0
                : pageData.currentPage + 1;
        pageSummary.textContent = `Trang ${shownPage}/${pageData.totalPages} - ${pageData.totalItems} kết quả`;

        const previous = actionButton('Trước', 'btn-light', () => {
            state.page -= 1;
            loadProducts();
        });
        previous.disabled = pageData.first;
        pagination.appendChild(previous);

        for (let index = 0; index < pageData.totalPages; index += 1) {
            const pageButton = actionButton(
                    String(index + 1),
                    index === pageData.currentPage
                            ? 'btn-primary'
                            : 'btn-light',
                    () => {
                        state.page = index;
                        loadProducts();
                    });
            pagination.appendChild(pageButton);
        }

        const next = actionButton('Sau', 'btn-light', () => {
            state.page += 1;
            loadProducts();
        });
        next.disabled = pageData.last;
        pagination.appendChild(next);
    }

    async function loadProducts() {
        try {
            const data = await shopApi.graphQL(`
                query ProductPage($keyword: String!, $page: Int!, $size: Int!) {
                    productPage(keyword: $keyword, page: $page, size: $size) {
                        content {
                            id
                            name
                            quantity
                            price
                            image
                            description
                            category {
                                id
                                name
                            }
                        }
                        currentPage
                        totalPages
                        totalItems
                        pageSize
                        first
                        last
                    }
                }
            `, state);

            if (data.productPage.totalPages > 0
                    && state.page >= data.productPage.totalPages) {
                state.page = data.productPage.totalPages - 1;
                await loadProducts();
                return;
            }

            renderRows(data.productPage.content);
            renderPagination(data.productPage);
        } catch (error) {
            renderRows([]);
            showMessage(error.message, 'error');
        }
    }

    async function loadCategories(selectedId) {
        const data = await shopApi.graphQL(`
            query CategoriesForProduct {
                categories {
                    id
                    name
                }
            }
        `);

        fields.categoryId.replaceChildren();
        const placeholder = document.createElement('option');
        placeholder.value = '';
        placeholder.textContent = '-- Chọn danh mục --';
        fields.categoryId.appendChild(placeholder);

        data.categories.forEach(category => {
            const option = document.createElement('option');
            option.value = category.id;
            option.textContent = category.name;
            option.selected = Number(selectedId) === Number(category.id);
            fields.categoryId.appendChild(option);
        });
    }

    async function openCreate() {
        form.reset();
        fields.id.value = '';
        document.getElementById('product-modal-title').textContent =
                'Thêm sản phẩm';

        try {
            await loadCategories();
            modal.hidden = false;
            fields.name.focus();
        } catch (error) {
            showMessage(error.message, 'error');
        }
    }

    async function openEdit(id) {
        try {
            const data = await shopApi.graphQL(`
                query ProductById($id: ID!) {
                    productById(id: $id) {
                        id
                        name
                        quantity
                        price
                        image
                        description
                        category {
                            id
                            name
                        }
                    }
                }
            `, {id: id});

            const product = data.productById;
            form.reset();
            await loadCategories(product.category.id);

            fields.id.value = product.id;
            fields.name.value = product.name;
            fields.price.value = product.price;
            fields.quantity.value = product.quantity;
            fields.description.value = product.description || '';
            document.getElementById('product-modal-title').textContent =
                    'Cập nhật sản phẩm';
            modal.hidden = false;
            fields.name.focus();
        } catch (error) {
            showMessage(error.message, 'error');
        }
    }

    function closeModal() {
        modal.hidden = true;
    }

    async function save(event) {
        event.preventDefault();
        const id = fields.id.value;

        try {
            const image = await shopApi.uploadImage(
                    fields.imageFile.files[0],
                    'product');
            const input = {
                name: fields.name.value.trim(),
                categoryId: fields.categoryId.value,
                price: Number(fields.price.value),
                quantity: Number(fields.quantity.value),
                image: image,
                description: fields.description.value.trim()
            };

            const data = id
                    ? await shopApi.graphQL(`
                        mutation UpdateProduct($id: ID!, $input: ProductInput!) {
                            updateProduct(id: $id, input: $input) {
                                success
                                message
                                product {
                                    id
                                }
                            }
                        }
                    `, {id: id, input: input})
                    : await shopApi.graphQL(`
                        mutation CreateProduct($input: ProductInput!) {
                            createProduct(input: $input) {
                                success
                                message
                                product {
                                    id
                                }
                            }
                        }
                    `, {input: input});

            const payload = id
                    ? data.updateProduct
                    : data.createProduct;
            if (!payload.success) {
                throw new Error(payload.message);
            }

            closeModal();
            state.page = 0;
            showMessage(payload.message, 'success');
            await loadProducts();
        } catch (error) {
            showMessage(error.message, 'error');
        }
    }

    async function remove(id, name) {
        const confirmed = window.confirm(
                `Bạn có chắc muốn xóa sản phẩm "${name}"?`);
        if (!confirmed) {
            return;
        }

        try {
            const data = await shopApi.graphQL(`
                mutation DeleteProduct($id: ID!) {
                    deleteProduct(id: $id) {
                        success
                        message
                        deletedId
                    }
                }
            `, {id: id});

            if (!data.deleteProduct.success) {
                throw new Error(data.deleteProduct.message);
            }

            showMessage(data.deleteProduct.message, 'success');
            await loadProducts();
        } catch (error) {
            showMessage(error.message, 'error');
        }
    }

    document.getElementById('add-product-button')
            .addEventListener('click', openCreate);
    document.getElementById('product-modal-close')
            .addEventListener('click', closeModal);
    document.getElementById('product-cancel-button')
            .addEventListener('click', closeModal);
    document.getElementById('product-search-form')
            .addEventListener('submit', event => {
                event.preventDefault();
                state.keyword = keywordInput.value.trim();
                state.page = 0;
                loadProducts();
            });
    document.getElementById('product-clear-button')
            .addEventListener('click', () => {
                keywordInput.value = '';
                state.keyword = '';
                state.page = 0;
                loadProducts();
            });

    modal.addEventListener('click', event => {
        if (event.target === modal) {
            closeModal();
        }
    });
    form.addEventListener('submit', save);
    loadProducts();
});

document.addEventListener('DOMContentLoaded', () => {
    const tableBody = document.getElementById('category-table-body');
    const pagination = document.getElementById('category-pagination');
    const pageSummary = document.getElementById('category-page-summary');
    const form = document.getElementById('category-form');
    const modal = document.getElementById('category-modal');
    const message = document.getElementById('category-message');
    const idInput = document.getElementById('category-id');
    const nameInput = document.getElementById('category-name');
    const iconInput = document.getElementById('category-icon');
    const keywordInput = document.getElementById('category-keyword');

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

    function createCell(text, className) {
        const cell = document.createElement('td');
        cell.textContent = text;
        if (className) {
            cell.className = className;
        }
        return cell;
    }

    function createImageCell(category) {
        const cell = document.createElement('td');
        const source = shopApi.imageUrl(category.icon, 'category');
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
        image.alt = category.name;
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

    function renderRows(categories) {
        tableBody.replaceChildren();

        if (categories.length === 0) {
            const row = document.createElement('tr');
            const cell = createCell(
                    'Không tìm thấy danh mục phù hợp.',
                    'empty');
            cell.colSpan = 4;
            row.appendChild(cell);
            tableBody.appendChild(row);
            return;
        }

        categories.forEach(category => {
            const row = document.createElement('tr');
            row.appendChild(createCell(category.id));
            row.appendChild(createImageCell(category));

            const nameCell = createCell('');
            const strong = document.createElement('strong');
            strong.textContent = category.name;
            nameCell.appendChild(strong);
            row.appendChild(nameCell);

            const actionsCell = document.createElement('td');
            const actions = document.createElement('div');
            actions.className = 'action-group';
            actions.appendChild(actionButton(
                    'Sửa',
                    'btn-warning',
                    () => openEdit(category.id)));
            actions.appendChild(actionButton(
                    'Xóa',
                    'btn-danger',
                    () => remove(category.id, category.name)));
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
            loadCategories();
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
                        loadCategories();
                    });
            pagination.appendChild(pageButton);
        }

        const next = actionButton('Sau', 'btn-light', () => {
            state.page += 1;
            loadCategories();
        });
        next.disabled = pageData.last;
        pagination.appendChild(next);
    }

    async function loadCategories() {
        try {
            const data = await shopApi.graphQL(`
                query CategoryPage($keyword: String!, $page: Int!, $size: Int!) {
                    categoryPage(keyword: $keyword, page: $page, size: $size) {
                        content {
                            id
                            name
                            icon
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

            if (data.categoryPage.totalPages > 0
                    && state.page >= data.categoryPage.totalPages) {
                state.page = data.categoryPage.totalPages - 1;
                await loadCategories();
                return;
            }

            renderRows(data.categoryPage.content);
            renderPagination(data.categoryPage);
        } catch (error) {
            renderRows([]);
            showMessage(error.message, 'error');
        }
    }

    function openCreate() {
        form.reset();
        idInput.value = '';
        document.getElementById('category-modal-title').textContent =
                'Thêm danh mục';
        modal.hidden = false;
        nameInput.focus();
    }

    async function openEdit(id) {
        try {
            const data = await shopApi.graphQL(`
                query CategoryById($id: ID!) {
                    categoryById(id: $id) {
                        id
                        name
                        icon
                    }
                }
            `, {id: id});

            form.reset();
            idInput.value = data.categoryById.id;
            nameInput.value = data.categoryById.name;
            document.getElementById('category-modal-title').textContent =
                    'Cập nhật danh mục';
            modal.hidden = false;
            nameInput.focus();
        } catch (error) {
            showMessage(error.message, 'error');
        }
    }

    function closeModal() {
        modal.hidden = true;
    }

    async function save(event) {
        event.preventDefault();
        const id = idInput.value;

        try {
            const icon = await shopApi.uploadImage(
                    iconInput.files[0],
                    'category');
            const input = {
                name: nameInput.value.trim(),
                icon: icon
            };

            const data = id
                    ? await shopApi.graphQL(`
                        mutation UpdateCategory($id: ID!, $input: CategoryInput!) {
                            updateCategory(id: $id, input: $input) {
                                success
                                message
                                category {
                                    id
                                }
                            }
                        }
                    `, {id: id, input: input})
                    : await shopApi.graphQL(`
                        mutation CreateCategory($input: CategoryInput!) {
                            createCategory(input: $input) {
                                success
                                message
                                category {
                                    id
                                }
                            }
                        }
                    `, {input: input});

            const payload = id
                    ? data.updateCategory
                    : data.createCategory;
            if (!payload.success) {
                throw new Error(payload.message);
            }

            closeModal();
            state.page = 0;
            showMessage(payload.message, 'success');
            await loadCategories();
        } catch (error) {
            showMessage(error.message, 'error');
        }
    }

    async function remove(id, name) {
        const confirmed = window.confirm(
                `Bạn có chắc muốn xóa danh mục "${name}"?`);
        if (!confirmed) {
            return;
        }

        try {
            const data = await shopApi.graphQL(`
                mutation DeleteCategory($id: ID!) {
                    deleteCategory(id: $id) {
                        success
                        message
                        deletedId
                    }
                }
            `, {id: id});

            if (!data.deleteCategory.success) {
                throw new Error(data.deleteCategory.message);
            }

            showMessage(data.deleteCategory.message, 'success');
            await loadCategories();
        } catch (error) {
            showMessage(error.message, 'error');
        }
    }

    document.getElementById('add-category-button')
            .addEventListener('click', openCreate);
    document.getElementById('category-modal-close')
            .addEventListener('click', closeModal);
    document.getElementById('category-cancel-button')
            .addEventListener('click', closeModal);
    document.getElementById('category-search-form')
            .addEventListener('submit', event => {
                event.preventDefault();
                state.keyword = keywordInput.value.trim();
                state.page = 0;
                loadCategories();
            });
    document.getElementById('category-clear-button')
            .addEventListener('click', () => {
                keywordInput.value = '';
                state.keyword = '';
                state.page = 0;
                loadCategories();
            });

    modal.addEventListener('click', event => {
        if (event.target === modal) {
            closeModal();
        }
    });
    form.addEventListener('submit', save);
    loadCategories();
});

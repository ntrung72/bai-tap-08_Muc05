document.addEventListener('DOMContentLoaded', () => {
    const categorySelect = document.getElementById('home-category');
    const productGrid = document.getElementById('home-product-grid');
    const summary = document.getElementById('home-result-summary');
    const message = document.getElementById('home-message');

    function showError(text) {
        message.textContent = text;
        message.className = 'alert error';
        message.hidden = false;
    }

    function createProductCard(product) {
        const card = document.createElement('article');
        card.className = 'product-card';

        const imageWrap = document.createElement('div');
        imageWrap.className = 'product-card-image';
        const source = shopApi.imageUrl(product.image, 'product');

        if (source) {
            const image = document.createElement('img');
            image.src = source;
            image.alt = product.name;
            image.loading = 'lazy';
            image.addEventListener('error', () => {
                imageWrap.textContent = 'Chưa có ảnh';
            }, {once: true});
            imageWrap.appendChild(image);
        } else {
            imageWrap.textContent = 'Chưa có ảnh';
        }

        const body = document.createElement('div');
        body.className = 'product-card-body';

        const category = document.createElement('span');
        category.className = 'product-category';
        category.textContent = product.category.name;

        const name = document.createElement('h2');
        name.textContent = product.name;

        const description = document.createElement('p');
        description.textContent = product.description || 'Chưa có mô tả.';

        const footer = document.createElement('div');
        footer.className = 'product-card-footer';

        const price = document.createElement('strong');
        price.textContent = shopApi.formatCurrency(product.price);

        const quantity = document.createElement('span');
        quantity.textContent = `Còn ${product.quantity}`;

        footer.append(price, quantity);
        body.append(category, name, description, footer);
        card.append(imageWrap, body);
        return card;
    }

    function renderProducts(products) {
        productGrid.replaceChildren();
        summary.textContent = `${products.length} sản phẩm - giá từ thấp đến cao`;

        if (products.length === 0) {
            const empty = document.createElement('div');
            empty.className = 'card empty product-empty';
            empty.textContent = 'Danh mục này chưa có sản phẩm.';
            productGrid.appendChild(empty);
            return;
        }

        products.forEach(product => {
            productGrid.appendChild(createProductCard(product));
        });
    }

    function renderCategories(categories) {
        const selectedValue = categorySelect.value;
        categorySelect.replaceChildren();

        const allOption = document.createElement('option');
        allOption.value = '';
        allOption.textContent = 'Tất cả danh mục';
        categorySelect.appendChild(allOption);

        categories.forEach(category => {
            const option = document.createElement('option');
            option.value = category.id;
            option.textContent = category.name;
            option.selected = String(category.id) === selectedValue;
            categorySelect.appendChild(option);
        });
    }

    async function loadProducts() {
        message.hidden = true;
        const categoryId = categorySelect.value;

        try {
            if (categoryId) {
                const data = await shopApi.graphQL(`
                    query ProductsByCategory($categoryId: ID!) {
                        categories {
                            id
                            name
                        }
                        products: productsByCategory(categoryId: $categoryId) {
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
                `, {categoryId: categoryId});
                renderCategories(data.categories);
                renderProducts(data.products);
                return;
            }

            const data = await shopApi.graphQL(`
                query HomeProducts {
                    categories {
                        id
                        name
                    }
                    products: productsByPriceAsc {
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
            `);
            renderCategories(data.categories);
            renderProducts(data.products);
        } catch (error) {
            renderProducts([]);
            showError(error.message);
        }
    }

    categorySelect.addEventListener('change', loadProducts);
    loadProducts();
});

(function () {
    const root = document.body.dataset.contextPath || '/';
    const contextPath = root.endsWith('/')
            ? root.slice(0, -1)
            : root;

    async function graphQL(query, variables = {}) {
        const response = await fetch(`${contextPath}/graphql`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({
                query: query,
                variables: variables
            })
        });

        const payload = await response.json();
        if (!response.ok || payload.errors) {
            const message = payload.errors?.[0]?.message
                    || 'Không thể thực hiện yêu cầu GraphQL.';
            throw new Error(message);
        }

        return payload.data;
    }

    async function uploadImage(file, folder) {
        if (!file) {
            return null;
        }

        const formData = new FormData();
        formData.append('file', file);

        const response = await fetch(
                `${contextPath}/api/uploads/${folder}`,
                {
                    method: 'POST',
                    body: formData
                });
        const payload = await response.json();

        if (!response.ok || !payload.success) {
            throw new Error(payload.message || 'Không thể tải ảnh lên.');
        }

        return payload.path;
    }

    function imageUrl(path, defaultFolder) {
        if (!path || !path.trim()) {
            return null;
        }

        const value = path.trim().replace(/\\/g, '/');
        if (/^https?:\/\//i.test(value)) {
            return value;
        }

        let relativePath = value.replace(/^\/+/, '');
        if (relativePath.startsWith('uploads/')) {
            relativePath = relativePath.substring(8);
        }
        if (relativePath.startsWith('images/')) {
            relativePath = relativePath.substring(7);
        }
        if (!relativePath.includes('/')) {
            relativePath = `${defaultFolder}/${relativePath}`;
        }
        if (relativePath.split('/').includes('..')) {
            return null;
        }

        const encodedPath = relativePath
                .split('/')
                .map(encodeURIComponent)
                .join('/');
        return `${contextPath}/${encodedPath}`;
    }

    function formatCurrency(value) {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(Number(value));
    }

    window.shopApi = {
        graphQL: graphQL,
        uploadImage: uploadImage,
        imageUrl: imageUrl,
        formatCurrency: formatCurrency
    };
})();

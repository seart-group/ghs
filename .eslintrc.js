module.exports = {
    root: true,
    env: {
        node: true,
        es2021: true,
    },
    extends: 'eslint:recommended',
    globals: {
        _: 'readonly',
        H: 'readonly',
        URL: 'readonly',
        self: 'readonly',
        base: 'readonly',
        Blob: 'readonly',
        chroma: 'readonly',
        crypto: 'readonly',
        window: 'readonly',
        jQuery: 'readonly',
        document: 'readonly',
        navigator: 'readonly',
        bootstrap: 'readonly',
        localStorage: 'readonly',
        Handlebars: 'readonly',
        Chart: 'readonly',
    },
    rules: {
        'no-unused-vars': [
            'error',
            {
                'argsIgnorePattern': '^_',
                'varsIgnorePattern': '^_',
                'caughtErrorsIgnorePattern': '^_'
            }
        ],
    },
};

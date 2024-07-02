import js from "@eslint/js";
import globals from "globals";
import recommendedConfig from "eslint-plugin-prettier/recommended";
import eslintConfigPrettier from "eslint-config-prettier";

const { rules: prettierRules } = eslintConfigPrettier;
const prettierConfigsRecommended = recommendedConfig;
const {
    configs: { recommended: jsConfigsRecommended },
} = js;

export default [
    jsConfigsRecommended,
    prettierConfigsRecommended,
    {
        files: ["**/*.js"],
        languageOptions: {
            sourceType: "module",
            ecmaVersion: 2024,
            globals: {
                ...globals.node,
                ...globals.es2021,
                ...globals.browser,
                ...globals.jquery,
                _: "readonly",
                Chart: "readonly",
                H: "readonly",
                Handlebars: "readonly",
                base: "readonly",
                bootstrap: "readonly",
                chroma: "readonly",
            },
        },
        rules: {
            ...prettierRules,
            "no-unused-vars": [
                "error",
                {
                    argsIgnorePattern: "^_",
                    varsIgnorePattern: "^_",
                    caughtErrorsIgnorePattern: "^_",
                },
            ],
        },
    },
    {
        ignores: ["dist/*", "node_modules/*", "target/*"],
    },
];

import js from "@eslint/js";
import globals from "globals";

export default [
    js.configs.recommended,
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
                "_": "readonly",
                "Chart": "readonly",
                "H": "readonly",
                "Handlebars": "readonly",
                "base": "readonly",
                "bootstrap": "readonly",
                "chroma": "readonly",
            }
        },
        rules: {
            "semi": ["error", "always"],
            "indent": ["error", 4],
            "quotes": ["error", "double", {
                "allowTemplateLiterals": true,
            }],
            "linebreak-style": ["error", "unix"],
            "no-unused-vars": ["error" , {
                "argsIgnorePattern": "^_",
                "varsIgnorePattern": "^_",
                "caughtErrorsIgnorePattern": "^_",
            }],
        }
    },
];

# Contributing to GitHub Search

Thank you for your interest in contributing to GitHub Search!
We appreciate your efforts to make this project better.

Before you start, please take a moment to read and understand the following guidelines.

## Getting Started

1. Fork the repository and clone your fork locally.
2. Set the project up, as outlined in the [README](README.md).
3. Create a new branch for your contribution.
   The branch name should follow the format of: `label/short-name`.
   For more information on valid labels, see the [full list](https://github.com/seart-group/ghs/labels).
4. Test your changes thoroughly.
5. Push your changes to your fork: `git push origin label/short-name`.
6. Open a pull request (PR) against the `master` branch of this repository.

## Pull Request Process

Ensure your PR description explains the purpose of your changes and provides context.
Reference related issues through [linking keywords](https://docs.github.com/en/issues/tracking-your-work-with-issues/linking-a-pull-request-to-an-issue).
Your PR should include only relevant changes.
Large changes should be broken into smaller, manageable PRs.
Be prepared to make adjustments based on feedback received by the automated actions and reviewers.

## Style Guide

We use automated GitHub actions to enforce the project code style.
The list of checks includes:

- [CheckStyle](.github/workflows/checkstyle.yml)
- [ESLint](.github/workflows/eslint.yml)
- [hadolint](.github/workflows/hadolint.yml)
- [StyleLint](.github/workflows/stylelint.yml)
- [HTMLHint](.github/workflows/htmlhint.yml)
- [markdownlint](.github/workflows/markdownlint.yml)

## License

By contributing to this project, you agree that your contributions will be licensed under the [project license](LICENSE).

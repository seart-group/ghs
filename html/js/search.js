(function (base, $, _, Handlebars, Modal) {
    const $back = $(".btn-back");
    const $search = $("#search");
    const $spinner = $("#spinner");
    const $results = $("#results");
    const $results_count = $("#results-count");
    const $results_list = $("#results-list");
    const $results_pagination = $(".pagination");
    const $results_jump = $(".pagination-jump");
    const $results_jump_input = $(".pagination-jump-number");
    const $results_empty = $("#template-results-none").html();
    const $results_template = $("#template-results-repo").html();
    const $results_content = Handlebars.compile($results_template);
    const [
        _element,
        download_modal = new Modal(_element)
    ] = $("#download-modal").get();
    const formats = [ "csv", "xml", "json" ];
    const paginationOptions = {
        visiblePages: 5,
        hideOnlyOnePage: false,
        initiateStartPageClick: false,
        first: `<i class="bi bi-chevron-double-left"></i>`,
        prev: `<i class="bi bi-chevron-left"></i>`,
        next: `<i class="bi bi-chevron-right"></i>`,
        last: `<i class="bi bi-chevron-double-right"></i>`
    };

    const setParameters = function (url) {
        return $search.serializeArray()
            .filter((entry) => !!entry.value)
            .reduce((_url, entry) => {
                _url.searchParams.append(entry.name, entry.value);
                return _url;
            }, url);
    }

    const query = function () {
        return setParameters(new URL(`${base}/r/search`));
    }

    const download = function (format) {
        if (!formats.includes(format)) {
            throw new Error(`The ${format} is not supported!`);
        }
        return setParameters(new URL(`${base}/r/download/${format}`));
    };

    const search = async function (page = 0) {
        const url = query();
        url.searchParams.append("page", `${page}`);
        return fetch(url);
    };

    const $toggleSearch = function () {
        $search.toggleClass("d-none");
    };

    const $toggleResults = function () {
        $results.toggleClass("d-none");
    };

    const $toggleSpinner = function () {
        $spinner.toggleClass("d-none");
    };

    const $resetJumpForms = function () {
        $results_jump
            .trigger("reset")
            .removeClass("disabled")
            .children()
            .prop("disabled", false);
        $results_jump_input
            .removeAttr("min")
            .removeAttr("max");
    }

    const $resetResults = function () {
        $results_count.children().remove();
        $results_list.children().remove();
    }

    const $setResultsCount = function (count = 0) {
        $results_count.html(`<h1 class="my-3">Results: ${count.toLocaleString()}</h1>`);
    };

    const $destroyPagination = function () {
        $results_pagination.twbsPagination("destroy");
    };

    const $createPagination = function (current = 1, total = 1) {
        $results_pagination.twbsPagination({
            ...paginationOptions,
            startPage: current || 1,
            totalPages: total || 1
        });
        $(".disabled > .page-link").attr("tabindex", -1);
    }

    const $setDownloadLinks = function (results = 0) {
        const $group = $(".btn-download-group")
        if (!results) {
            const $buttons = $(".btn-download");
            $group.addClass("disabled");
            $buttons.prop("disabled", true)
                .off("click", "**");
        } else {
            $group.removeClass("disabled");
            formats.forEach((format) => {
                const url = download(format);
                const $buttons = $(`.btn-download-${format}`);
                $buttons.prop("disabled", false)
                    .on("click", function () {
                        download_modal.show();
                        window.setTimeout(() => window.location.href = url.href, 500);
                    });
            });
        }
    };

    const $setPageJump = function (pages = 1) {
        if (pages > 1) {
            $results_jump_input.attr({
                min: 1,
                max: pages
            });
        } else {
            $results_jump
                .addClass("disabled")
                .children()
                .prop("disabled", true);
        }
    };

    const $no_results = function () {
        $results_list.append($results_empty);
    };

    const $renderItem = function (
        {
            id,
            name,
            mainLanguage,
            isFork,
            isArchived,
            hasWiki,
            homepage,
            defaultBranch,
            lastCommitSHA,
            license,
            commits,
            watchers,
            stargazers,
            forks,
            branches,
            contributors,
            metrics,
            totalLines,
            totalCodeLines,
            totalCommentLines,
            totalIssues,
            totalPullRequests,
            openIssues,
            openPullRequests,
            releases,
            size,
            createdAt,
            pushedAt,
            updatedAt,
            lastCommit,
            labels,
            languages,
        }) {
        const total = _.sum(Object.values(languages));
        const normalized = _.mapValues(languages, (value) => value / total * 100);

        const languageFilter = $search.serializeArray().find((entry) => entry.name === "language");
        let languageMetrics = metrics.find((metric) => metric.language === languageFilter?.value);
        languageMetrics = {
            language: languageMetrics?.language,
            totalLines: languageMetrics?.totalLines,
            codeLines: languageMetrics?.codeLines,
            commentLines: languageMetrics?.commentLines,
        }

        const context = {
            id: id,
            name: name,
            language: mainLanguage,
            homepage: homepage,
            branch: defaultBranch,
            sha: lastCommitSHA,
            license: license,
            properties: {
                fork: isFork,
                archived: isArchived,
                wiki: hasWiki
            },
            statistics: {
                commits: commits,
                watchers: watchers,
                stars: stargazers,
                forks: forks,
                totalIssues: totalIssues,
                totalPullReqs: totalPullRequests,
                branches: branches,
                contributors: contributors,
                openIssues: openIssues,
                openPullReqs: openPullRequests,
                releases: releases,
                size: size,
                created: createdAt,
                updated: updatedAt,
                lastPush: pushedAt,
                lastCommit: lastCommit,
                totalLines: totalLines,
                codeLines: totalCodeLines,
                commentLines: totalCommentLines,
            },
            languageMetrics: languageMetrics,
            labels: labels,
            languages: normalized
        };
        const html = $results_content(context);
        $results_list.append(html);
    };

    const $renderItems = function (items = []) {
        if (items.length) {
            items.forEach($renderItem);
        } else {
            $no_results();
        }
    };

    $search.on("submit", async function (event) {
        event.preventDefault();
        $toggleSearch();
        $toggleSpinner();
        const response = await search();
        const { totalItems, totalPages, items } = await response.json();
        $setResultsCount(totalItems);
        $setDownloadLinks(totalItems);
        $renderItems(items);
        $createPagination(1, totalPages);
        $setPageJump(totalPages);
        $toggleSpinner();
        $toggleResults();
    });

    const replacePage = async function (page = 1) {
        $toggleResults();
        $toggleSpinner();
        $destroyPagination();
        $resetResults();
        $resetJumpForms();
        const response = await search(page - 1);
        const { totalItems, totalPages, items } = await response.json();
        $setResultsCount(totalItems);
        $renderItems(items);
        $createPagination(page, totalPages);
        $setPageJump(totalPages);
        $toggleSpinner();
        $toggleResults();
    }

    paginationOptions.onPageClick = async function (_, page) {
        await replacePage(page);
    };

    $results_jump.on("submit", async function (event) {
        event.preventDefault();
        const [ { value, page = Number(value) } ] = $(this).serializeArray();
        await replacePage(page);
    });

    $back.on("click", function () {
        $destroyPagination();
        $resetJumpForms();
        $resetResults();
        $toggleResults();
        $toggleSearch();
    });
})(base, jQuery, _, Handlebars, bootstrap.Modal);

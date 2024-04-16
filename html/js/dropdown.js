(function (base, $) {
    const chevron = `<i class="bi bi-chevron-down"></i>`;

    const $search_name_dropdown_toggle = $("#search-name-dropdown-toggle");
    const $search_name_dropdown_items = $("#search-name-dropdown-items > * > .dropdown-item");
    const $search_name_equals = $("#search-name-equals");

    const $search_sort_column_dropdown_toggle = $("#search-sort-column-dropdown-toggle");
    const $search_sort_column_dropdown_items = $("#search-sort-column-dropdown-items > * > .dropdown-item");
    const $search_sort_direction_dropdown_toggle = $("#search-sort-direction-dropdown-toggle");
    const $search_sort_direction_dropdown_items = $("#search-sort-direction-dropdown-items > * > .dropdown-item");
    const $search_sort = $("#search-sort");

    $search_name_dropdown_items.on("click", function () {
        const target = $(this);
        const html = target.html();
        const value = target.val();
        $search_name_dropdown_toggle.html(`${html} ${chevron}`);
        $search_name_equals.attr("value", value);
    });

    $search_sort_column_dropdown_items.on("click", function () {
        const target = $(this);
        const html = target.html();
        const column = target.val();
        const [ _, direction ] = $search_sort.val().split(",");
        $search_sort_column_dropdown_toggle.html(`${html} ${chevron}`);
        $search_sort.attr("value", `${column},${direction}`);
    });

    $search_sort_direction_dropdown_items.on("click", function () {
        const target = $(this);
        const html = target.html();
        const direction = target.val();
        const [ column, _ ] = $search_sort.val().split(",");
        $search_sort_direction_dropdown_toggle.html(`${html} ${chevron}`);
        $search_sort.attr("value", `${column},${direction}`);
    });
})(base, jQuery);

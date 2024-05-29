(function (base, $) {
    const $search_name_dropdown_toggle = $("#search-name-dropdown-toggle");
    const $search_name_dropdown_items = $("#search-name-dropdown-items > * > .dropdown-item");
    const $search_name_equals = $("#search-name-equals");

    const $search_sort_column_dropdown_toggle = $("#search-sort-column-dropdown-toggle");
    const $search_sort_column_dropdown_items = $("#search-sort-column-dropdown-items > * > .dropdown-item");
    const $search_sort_direction_dropdown_toggle = $("#search-sort-direction-dropdown-toggle");
    const $search_sort_direction_dropdown_items = $("#search-sort-direction-dropdown-items > * > .dropdown-item");
    const $search_sort = $("#search-sort");

    $search_name_dropdown_items.on("click", function () {
        const $item = $(this);
        const html = $item.html();
        const value = $item.val();
        $search_name_dropdown_toggle.find("span").html(html);
        $search_name_equals.attr("value", value);
    });

    $search_sort_column_dropdown_items.on("click", function () {
        const $item = $(this);
        const html = $item.html();
        const column = $item.val();
        const [ _, direction ] = $search_sort.val().split(",");
        $search_sort_column_dropdown_toggle.find("span").html(html);
        $search_sort.attr("value", `${column},${direction}`);
    });

    $search_sort_direction_dropdown_items.on("click", function () {
        const $item = $(this);
        const html = $item.html();
        const direction = $item.val();
        const [ column, _ ] = $search_sort.val().split(",");
        $search_sort_direction_dropdown_toggle.find("span").html(html);
        $search_sort.attr("value", `${column},${direction}`);
    });

    const set_dropdown_width = function () {
        $(".dropdown-menu").each(function () {
            const $menu = $(this);
            const $trigger = $menu.prevAll("[data-bs-toggle]");
            const width = $trigger.outerWidth();
            if (width) $menu.css("width", width);
        });
    };

    $(window).on("load", set_dropdown_width);
    $(window).on("resize", set_dropdown_width);
})(base, jQuery);

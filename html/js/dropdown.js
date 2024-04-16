(function (base, $) {
    const chevron = `<i class="bi bi-chevron-down"></i>`;

    const $search_name_dropdown_toggle = $("#search-name-dropdown-toggle");
    const $search_name_dropdown_items = $("#search-name-dropdown-items > * > .dropdown-item");
    const $search_name_equals = $("#search-name-equals");

    $search_name_dropdown_items.on("click", function () {
        const target = $(this);
        const html = target.html();
        const value = target.val();
        $search_name_dropdown_toggle.html(`${html} ${chevron}`);
        $search_name_equals.val(value);
    });
})(base, jQuery);

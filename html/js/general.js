(function ($, storage, clipboard, Tooltip) {
    const $search_name_dropdown_toggle = $("#search-name-dropdown-toggle");
    const $search_name_dropdown_items = $("#search-name-dropdown-items > * > .dropdown-item");
    const $search_name_equals = $("#search-name-equals");
    const $search_only_forks = $("#search-only-forks");
    const $search_exclude_forks = $("#search-exclude-forks");

    const $citation_copy_btn = $("#citation-copy-btn");
    const $citation_copy_target = $("#citation-copy-target");

    $(document).ready(function () {
        const key = "alert.advertisement.last-shown";
        const url = "https://seart-dl4se.si.usi.ch/";
        const lastShown = new Date(storage.getItem(key));
        const today = new Date(new Date().setUTCHours(0, 0, 0, 0));
        const days = Math.ceil((today - lastShown) / (1000 * 60 * 60 * 24));
        if (days > 30) {
            $("header").twbsAlert({
                body: `Need source code instead? Check out our <a href="${url}" target="_blank" class="alert-link">other platform</a>.`
            });
            storage.setItem(key, today.toISOString());
        }

        $("body").tooltip({
            selector: "[data-bs-toggle='tooltip']"
        });
    });

    const [ today ] = new Date().toISOString().split("T");
    $("#search input[type='date']").attr({
        min: "2008-01-01",
        max: today
    });

    $("#search input[type='number']").attr({
        min: 0,
        max: Number.MAX_SAFE_INTEGER,
    });

    $search_name_dropdown_items.on("click", function () {
        const target = $(this);
        const html = target.html();
        const value = target.val();
        $search_name_dropdown_toggle.html(`${html} <i class="bi bi-chevron-down"></i>`);
        $search_name_equals.val(value);
    });

    $search_only_forks.on("change", function () {
        if ($(this).prop("checked")) {
            $search_exclude_forks.prop("checked", false);
        }
    });

    $search_exclude_forks.on("change", function () {
        if ($(this).prop("checked")) {
            $search_only_forks.prop("checked", false);
        }
    });

    $citation_copy_btn.on("click", function () {
        const target = $(this);
        const icon = target.html();
        const [ element ] = target.get();
        const data = $citation_copy_target.html();
        clipboard.writeText(data)
            .then(() => {
                target.attr("data-bs-original-title", "Copied!");
                target.html(`<i class="bi bi-check-lg"></i>`);
                const tooltip = Tooltip.getInstance(element);
                tooltip.show();
                target.attr("data-bs-original-title", "Copy to clipboard...");
                setTimeout(() => {
                    tooltip.hide();
                    target.html(icon);
                }, 2500);
            });
    });
}(jQuery, localStorage, navigator.clipboard, bootstrap.Tooltip));

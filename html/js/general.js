(function ($, storage, clipboard, Tooltip) {
    const $search_only_forks = $("#search-only-forks");
    const $search_exclude_forks = $("#search-exclude-forks");

    const $publication_copy_btn = $("#publication-copy-btn");
    const $publication_copy_target = $("#publication-copy-target");

    const today = new Date(new Date().setUTCHours(0, 0, 0, 0));

    $(document).ready(function () {
        const key = "alert.advertisement.last-shown";
        const url = "https://seart-dl4se.si.usi.ch/";
        const lastShown = new Date(storage.getItem(key));
        const days = Math.ceil((today - lastShown) / (1000 * 60 * 60 * 24));
        if (days > 30) {
            $("header").twbsAlert({
                body: `If you're interested not only in sampling projects, but also in downloading their code, check out our <a href="${url}" target="_blank" class="alert-link">other platform</a>.`
            });
            storage.setItem(key, today.toISOString());
        }

        $("body").removeAttr("inert").tooltip({
            selector: "[data-bs-toggle='tooltip']"
        });
    });

    $("#search input[type='date']").attr({
        min: "2008-01-01",
        max: today.toISOString().slice(0, 10),
    });

    $("#search input[type='number']").attr({
        min: 0,
        max: Number.MAX_SAFE_INTEGER,
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

    $publication_copy_btn.on("click", function () {
        const target = $(this);
        const icon = target.html();
        const [ element ] = target.get();
        const data = $publication_copy_target.html();
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

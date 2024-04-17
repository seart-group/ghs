import Autocomplete from "https://cdn.jsdelivr.net/npm/bootstrap5-autocomplete@1.1.26/autocomplete.min.js";

(function (base, $, Autocomplete) {
    const default_config = {
        notFoundMessage: "No suggestions available...",
        highlightClass: "bg-transparent text-current text-decoration-underline p-0",
        activeClasses: [ "bg-secondary", "text-white" ],
        suggestionsThreshold: 0,
        maximumItems: 10,
        autoselectFirst: false,
        highlightTyped: true,
        fullWidth: true,
    };

    const get_response_items = response => response.json().then(({ items }) => items);
    const toggle_spinner = instance => {
        const input = instance.getInput();
        $(`#${input.id}-spinner`).toggleClass("d-none");
    };

    const server_config = {
        liveServer: true,
        noCache: false,
        debounceTime: 250,
        queryParam: "name",
        serverParams: {
            page: 0,
            size: 10,
        },
        onServerResponse: get_response_items,
        onServerError: (err, _signal, instance) => {
            const $input = $(instance.getInput());
            const width = $input.outerWidth();
            const $dropdown = $input.next(".dropdown-menu");
            if (width) $dropdown.css("width", width);
            const span = document.createElement("span");
            const $span = $(span).addClass("dropdown-item")
                                .addClass("disabled")
                                .text("Suggestions not available...");
            const li = document.createElement("li");
            const $li = $(li).attr("role", "presentation").append($span);
            $dropdown.empty().append($li).toggleClass("show");
        },
        onBeforeFetch: toggle_spinner,
        onAfterFetch: toggle_spinner,
    };

    $(".autocomplete").each((index, element) => {
        const endpoint = element.dataset.endpoint;
        new Autocomplete(element, {
            server: base + endpoint,
            ...default_config,
            ...server_config
        });
    });
})(base, jQuery, Autocomplete);

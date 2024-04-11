import Autocomplete from "https://cdn.jsdelivr.net/npm/bootstrap5-autocomplete@1.1.25/autocomplete.min.js";

(function (base, $, Autocomplete) {
    const $toast_container = $(".toast-container");
    const $search_language = $("#search-language");
    const $search_license = $("#search-license");
    const $search_language_list = $("#search-language-list");
    const $search_license_list = $("#search-license-list");
    const config = {
        notFoundMessage: "No suggestions available...",
        highlightClass: "bg-transparent text-current p-0",
        activeClasses: [ "bg-secondary", "text-white" ],
        suggestionsThreshold: 0,
        maximumItems: 10,
        debounceTime: 250,
        autoselectFirst: false,
        highlightTyped: true,
        fullWidth: true,
    };

    const create_option = (value) => `<option value="${value}">${value}</option>`;

    fetch(`${base}/l`)
        .then(response => response.json())
        .then(data => {
            const html = data.map(create_option).join("");
            $search_language_list.append(html);
        })
        .then(() => {
            const [ element ] = $search_language.get();
            new Autocomplete(element, config);
        })
        .catch(() => $toast_container.twbsToast(({
            id: "language-toast",
            body: "Could not retrieve language suggestions!",
        })));

    fetch(`${base}/r/licenses?${ new URLSearchParams({ size: 100 }) }`)
        .then(response => response.json())
        .then(data => {
            const html = data.map(create_option).join("");
            $search_license_list.append(html);
        })
        .then(() => {
            const [ element ] = $search_license.get();
            new Autocomplete(element, config);
        })
        .catch(() => $toast_container.twbsToast(({
            id: "license-toast",
            body: "Could not retrieve license suggestions!",
        })));
})(base, jQuery, Autocomplete);

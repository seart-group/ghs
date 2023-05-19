(function (base, $) {
    const typeaheadOptions = {
        items: 10,
        delay: 125,
        scrollHeight: 2,
        autoSelect: false,
        fitToElement: true,
        showHintOnFocus: true
    };

    const $search_label = $("#search-label");
    const $search_license = $("#search-license");
    const $search_topics = $("#search-topics");
    const $search_language = $("#search-language");

    fetch(`${base}/r/labels`)
        .then(response => response.json())
        .then(data => $search_label.typeahead({ ...typeaheadOptions, source: data }));

    fetch(`${base}/r/licenses`)
        .then(response => response.json())
        .then(data => $search_license.typeahead({ ...typeaheadOptions, source: data }));

    fetch(`${base}/r/topics`)
        .then(response => response.json())
        .then(data => $search_topics.typeahead({ ...typeaheadOptions, source: data }));

    fetch(`${base}/l`)
        .then(response => response.json())
        .then(data => $search_language.typeahead({ ...typeaheadOptions, source: data }));
}(base, jQuery));

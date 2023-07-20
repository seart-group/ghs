(function (base, $) {
    const typeaheadOptions = {
        items: 10,
        delay: 125,
        scrollHeight: 2,
        autoSelect: false,
        fitToElement: true,
        showHintOnFocus: true
    };

    const $toast_container = $(".toast-container");
    const $search_label = $("#search-label");
    const $search_license = $("#search-license");
    const $search_topic = $("#search-topic");
    const $search_language = $("#search-language");

    const options = (name) => ({ id: `${name}-toast`, body: `Could not retrieve ${name} suggestions!` });

    fetch(`${base}/r/labels`)
        .then(response => response.json())
        .then(data => $search_label.typeahead({ ...typeaheadOptions, source: data }))
        .catch(() => $toast_container.twbsToast(options("label")));

    fetch(`${base}/r/licenses`)
        .then(response => response.json())
        .then(data => $search_license.typeahead({ ...typeaheadOptions, source: data }))
        .catch(() => $toast_container.twbsToast(options("license")));

    fetch(`${base}/r/topics`)
        .then(response => response.json())
        .then(data => $search_topic.typeahead({ ...typeaheadOptions, source: data }))
        .catch(() => $toast_container.twbsToast(options("topic")));

    fetch(`${base}/l`)
        .then(response => response.json())
        .then(data => $search_language.typeahead({ ...typeaheadOptions, source: data }))
        .catch(() => $toast_container.twbsToast(options("language")));
}(base, jQuery));

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
    const $search_topic = $("#search-topic");

    const options = (name) => ({ id: `${name}-toast`, body: `Could not retrieve ${name} suggestions!` });

    fetch(`${base}/r/labels`)
        .then(response => response.json())
        .then(data => $search_label.typeahead({ ...typeaheadOptions, source: data }))
        .catch(() => $toast_container.twbsToast(options("label")));

    fetch(`${base}/r/topics`)
        .then(response => response.json())
        .then(data => $search_topic.typeahead({ ...typeaheadOptions, source: data }))
        .catch(() => $toast_container.twbsToast(options("topic")));
}(base, jQuery));

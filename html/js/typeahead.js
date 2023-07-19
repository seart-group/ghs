(function (base, $, Toast) {
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
    const $search_topic = $("#search-topic");
    const $search_language = $("#search-language");

    const [ language_toast ] = $("#language-toast").get();
    const [ license_toast ] = $("#license-toast").get();
    const [ topic_toast ] = $("#topic-toast").get();
    const [ label_toast ] = $("#label-toast").get();

    fetch(`${base}/r/labels`)
        .then(response => response.json())
        .then(data => $search_label.typeahead({ ...typeaheadOptions, source: data }))
        .catch(() => new Toast(label_toast).show());

    fetch(`${base}/r/licenses`)
        .then(response => response.json())
        .then(data => $search_license.typeahead({ ...typeaheadOptions, source: data }))
        .catch(() => new Toast(license_toast).show());

    fetch(`${base}/r/topics`)
        .then(response => response.json())
        .then(data => $search_topic.typeahead({ ...typeaheadOptions, source: data }))
        .catch(() => new Toast(topic_toast).show());

    fetch(`${base}/l`)
        .then(response => response.json())
        .then(data => $search_language.typeahead({ ...typeaheadOptions, source: data }))
        .catch(() => new Toast(language_toast).show());
}(base, jQuery, bootstrap.Toast));

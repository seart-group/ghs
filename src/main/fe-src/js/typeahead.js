function generateOptions(dataset) {
    return { showHintOnFocus: true, source: dataset, items: 10, minLength: 2 }
}

fetch("http://localhost:8080/api/r/labels").then(response => {
    return response.json();
}).then(data => {
    let labels = data.items;
    $label.typeahead(generateOptions(labels));
}).catch(_ => {});

fetch("http://localhost:8080/api/l").then(response => {
    return response.json();
}).then(data => {
    let languages = data.items;
    $language.typeahead(generateOptions(languages));
}).catch(_ => {});

fetch("http://localhost:8080/api/r/licenses").then(response => {
    return response.json();
}).then(data => {
    let licenses = data.items;
    $license.typeahead(generateOptions(licenses));
}).catch(_ => {});
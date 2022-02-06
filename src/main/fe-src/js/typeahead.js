function generateOptions(dataset) {
    return { showHintOnFocus: true, source: dataset, items: 15, minLength: 2 }
}

function fetchTypeaheadData(url, jqElement) {
    fetch(url).then(response => {
        return response.json();
    }).then(data => {
        jqElement.typeahead(generateOptions(data));
    }).catch(_ => {});
}

fetchTypeaheadData("http://localhost:8080/api/r/labels", $label)
fetchTypeaheadData("http://localhost:8080/api/l", $language)
fetchTypeaheadData("http://localhost:8080/api/r/licenses", $license)
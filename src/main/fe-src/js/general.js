String.prototype.isEmpty = function() {
    return (this.length === 0 || !this.trim());
};

function toggleNameEquality(bool_val) {
    let input  = document.getElementById("name");
    let button = document.getElementById("match");
    button.value = bool_val;
    if (bool_val){
        input.setAttribute("placeholder","Search by full repository name");
        // input.setAttribute("pattern","[A-Za-z0-9-_.]{1,39}\/[A-Za-z0-9-_.]{1,100}");
        input.setAttribute("pattern","[^\\s'\"]{1,39}\/[^\\s'\"]{1,100}");
        input.setAttribute("title","Must follow format of: username/reponame, max lengths 39 and 100 respectively");
        button.innerHTML = "Equals <i class=\"fa fa-angle-down fa-lg\"></i>";
    } else {
        input.setAttribute("placeholder","Search by keyword in name");
        // input.setAttribute("pattern","[A-Za-z0-9-/_.]{0,140}");
        // input.setAttribute("title","Max length: 140, with allowed characters: A-Z a-z 0-9 - / _ .");
        input.setAttribute("pattern","[^\\s'\"]{0,140}");
        input.setAttribute("title","Max length: 140, with no whitespace, ', \"");
        button.innerHTML = "Contains <i class=\"fa fa-angle-down fa-lg\"></i>";
    }
}

function increment(elementId) {
    let value = parseInt(document.getElementById(elementId).value);
    value = isNaN(value) ? 0 : value;
    if (value < Number.MAX_SAFE_INTEGER){
        value++;
    }
    document.getElementById(elementId).value = value;
}

function decrement(elementId) {
    let value = parseInt(document.getElementById(elementId).value);
    value = isNaN(value) ? 0 : value;
    if (value > 0){
        value--;
    }
    document.getElementById(elementId).value = value;
}

function checkOnlyForked() {
    let excludeForks = document.getElementById("exclude-forks");
    let onlyForks = document.getElementById("only-forks");
    if (onlyForks.checked && excludeForks.checked){
        excludeForks.checked = false;
    }
}

function checkExcludeForked() {
    let excludeForks = document.getElementById("exclude-forks");
    let onlyForks = document.getElementById("only-forks");
    if (excludeForks.checked && onlyForks.checked){
        onlyForks.checked = false;
    }
}

function toggleButtonText(buttonId) {
    let button = document.getElementById(buttonId);
    if (button.innerHTML === "Show Details") {
        button.innerHTML = "Hide Details";
    } else {
        button.innerHTML = "Show Details";
    }
}

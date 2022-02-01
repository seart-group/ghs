let supported_languages = [];
let num_of_repos = [];
let colors = [];

function dynamicColors(i, total) {
    let c = 100 + i * 155/total;
    return "rgb(" + c + "," + c + "," + c + ")";
}

fetch("http://localhost:8080/api/r/stats").then(response => {
    return response.json();
}).then(data => {
    let i = 0;
    let total_repos = 0;

    let entries = Object.entries(data);

    for (const [language, repos] of entries) {
        supported_languages.push(language);
        num_of_repos.push(repos);
        total_repos += repos;
        colors.push(dynamicColors(i, entries.length));
        i++;
    }

    document.getElementById("total_num_of_repos").innerText = total_repos.toLocaleString('en', {useGrouping: true});
}).catch(error => {
    console.error('Error (/r/stats):', error);
});

Chart.defaults.global.defaultFontFamily = "Trebuchet MS";

let ctx = document.getElementById('myChart').getContext('2d');

let config = {
    type: 'bar',
    data: {
        labels: supported_languages,
        datasets: [{
            label: '# of repos',
            data: num_of_repos,
            backgroundColor: colors,
            hoverBackgroundColor: colors
        }]
    },
    options: {
        legend: {
            display: false
        },
        tooltips: {
            yAlign: 'bottom'
        },
        layout: {
            padding: {
                left: 15,
                right: 15,
                top: 45,
                bottom: 15
            }
        },
        scales: {
            yAxes: [{
                ticks: {
                    beginAtZero: true
                }
            }]
        }
    }
};

new Chart(ctx,config);

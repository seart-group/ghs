let supported_languages = [];
let num_of_repos = [];
let colors = [];

function dynamicColors(i,total) {
    let c = 100 + i * 155/total;
    return "rgb(" + c + "," + c + "," + c + ")";
}

fetch("http://localhost:8080/r/stats").then(response => {
    return response.json();
}).then(data => {
    let i = 0;
    let items = data.items;
    items.forEach(obj => {
        supported_languages.push(obj.name);
        num_of_repos.push(obj.value);
        colors.push(dynamicColors(i,items.length));
        i++;
    });
}).catch( (error) => {
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

(function (base, $, Chart) {
    const [ canvas ] = $("#statistics-chart").get();
    const $statistics_count = $("#statistics-count");

    // https://nagix.github.io/chartjs-plugin-colorschemes/colorchart.html#:~:text=tableau.Brown20%3A-,tableau.Gray20,-%3A
    const colors = [
        "#49525e", "#4f5864", "#555f6a", "#5b6570",
        "#616c77", "#67737c", "#6e7a81", "#758087",
        "#7c878d", "#848e93", "#8b9598", "#939c9e",
        "#9ca3a4", "#a4a9ab", "#acb0b1", "#b4b7b7",
        "#bcbfbe", "#c5c7c6", "#cdcecd", "#d5d5d5"
    ];

    Chart.defaults.font.family = "Trebuchet MS";

    const options = {
        animation: {
            duration: 0
        },
        layout: {
            padding: {
                top: 30
            }
        },
        responsive: true,
        scales: {
            y: {
                beginAtZero: true,
                ticks: {
                    callback: function (label, _idx, _labels) {
                        return `${label / 1000}k`;
                    }
                },
                title: {
                    display: true,
                    text: "# of projects"
                }
            }
        },
        plugins: {
            tooltip: {
                yAlign: "bottom",
                titleAlign: "center",
                cornerRadius: 0
            },
            legend: {
                display: false
            }
        }
    };

    fetch(`${base}/r/stats`)
        .then(response => response.json())
        .then(json => ({
            _count: Object.values(json).reduce((_sum, _i) => _sum + _i, 0),
            labels: Object.keys(json),
            datasets: [{
                label: "Projects",
                data: Object.values(json),
                borderWidth: 2,
                borderColor: colors,
                backgroundColor: colors.map(color => `${color}bf`)
            }]
        }))
        .then(data => {
            $statistics_count.replaceWith(`<span id="statistics-count">${data._count.toLocaleString()}</span>`);
            new Chart(canvas, {
                type: "bar",
                data: data,
                options: options
            });
        });
}(base, jQuery, Chart));

(function (base, $, _, Chart) {
    const [ canvas ] = $("#statistics-chart").get();
    const $toast_container = $(".toast-container");
    const $statistics_mined = $("#statistics-mined");
    const $statistics_analyzed = $("#statistics-analyzed");

    // https://github.com/nagix/chartjs-plugin-colorschemes/blob/master/src/colorschemes/colorschemes.tableau.js#L45
    const gray20 = [
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
        responsive: true,
        layout: {
            padding: {
                right: 10,
            },
        },
        scales: {
            x: {
                ticks: {
                    display: false,
                },
                title: {
                    display: true,
                    text: "Language",
                    font: {
                        size: 16,
                    },
                },
            },
            y: {
                beginAtZero: true,
                ticks: {
                    callback: function (label, _idx, _labels) {
                        return `${label / 1000}k`;
                    }
                },
                title: {
                    display: true,
                    text: "# of projects",
                    font: {
                        size: 16,
                    },
                },
            },
        },
        elements: {
            bar: {
                borderWidth: 1
            }
        },
        plugins: {
            legend: {
                display: false,
            },
            tooltip: {
                backgroundColor: "rgba(0, 0, 0, 1)",
                displayColors: false,
                yAlign: "bottom",
                titleAlign: "center",
                cornerRadius: 0,
                callbacks: {
                    afterLabel: function (ctx) {
                        const { mined, analyzed } = ctx.raw;
                        return `Analyzed: ${(analyzed / mined * 100).toFixed(2)}%`;
                    },
                },
            },
        }
    };

    fetch(`${base}/r/stats`)
        .then(response => response.json())
        .then(json => {
            const total = Object.values(json).reduce((acc, { mined, analyzed }) => {
                acc.mined += mined;
                acc.analyzed += analyzed;
                return acc;
            }, { mined: 0, analyzed: 0 });
            const coverage = `${(total.analyzed / total.mined * 100).toFixed(2)}%`;
            $statistics_mined.replaceWith(`<span id="statistics-mined">${total.mined.toLocaleString()}</span>`);
            $statistics_analyzed.replaceWith(`<span id="statistics-analyzed">${coverage}</span>`);
            return Object.entries(json).map(([key, value]) => ({ x: key, ...value }));
        })
        .then(data => ({
            datasets: [
                {
                    data,
                    label: "Mined",
                    borderColor: gray20,
                    backgroundColor: gray20.map(color => `${color}bf`),
                    barPercentage: 1,
                    parsing: {
                        yAxisKey: "mined",
                    },
                },
            ]
        }))
        .then(data => new Chart(canvas, { type: "bar", options, data }))
        .catch(() => $toast_container.twbsToast({
            id: "statistics-toast",
            body: "Could not retrieve repository statistics!"
        }));
}(base, jQuery, _, Chart));

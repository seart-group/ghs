(function (base, $, _, Chart) {
    const [ canvas ] = $("#statistics-chart").get();
    const $toast_container = $(".toast-container");
    const $statistics_mined = $("#statistics-mined");
    const $statistics_analyzed = $("#statistics-analyzed");

    // https://github.com/nagix/chartjs-plugin-colorschemes/blob/d96a01846626881aa4bec56828c333af81050906/src/colorschemes/colorschemes.tableau.js#L39
    const blue20 = [
        "#2a5783", "#305d8a", "#376491", "#3d6a98",
        "#437a9f", "#4878a6", "#4e7fac", "#5485b2",
        "#5b8cb8", "#6394be", "#6a9bc3", "#72a3c9",
        "#79aacf", "#80b0d5", "#89b8da", "#92c0df",
        "#9bc7e4", "#a5cfe9", "#afd6ed", "#b9ddf1"
    ];

    // https://github.com/nagix/chartjs-plugin-colorschemes/blob/d96a01846626881aa4bec56828c333af81050906/src/colorschemes/colorschemes.tableau.js#L42
    const red20 = [
        "#ae123a", "#b8163a", "#c11a3b", "#ca223c",
        "#d3293d", "#da323f", "#e13b42", "#e74545",
        "#ec5049", "#f05c4d", "#f36754", "#f5715d",
        "#f77b66", "#f9856e", "#fa8f79", "#fb9984",
        "#fca290", "#fdab9b", "#feb4a6", "#ffbeb2"
    ];

    Chart.defaults.font.family = "Trebuchet MS";

    const options = {
        animation: {
            duration: 0
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
        elements: {
            bar: {
                borderWidth: 1
            }
        },
        plugins: {
            tooltip: {
                backgroundColor: "rgba(0, 0, 0, 1)",
                yAlign: "bottom",
                titleAlign: "center",
                cornerRadius: 0,
            },
        }
    };

    fetch(`${base}/r/stats`)
        .then(response => response.json())
        .then(json => {
            const languages = Object.keys(json);
            const values = Object.values(json);
            const [mined, analyzed] = values.reduce((acc, { mined: left, analyzed: right }) => {
                acc[0].push(left);
                acc[1].push(right);
                return acc;
            }, [[], []]);

            $statistics_mined.replaceWith(`<span id="statistics-mined">${_.sum(mined).toLocaleString()}</span>`);
            $statistics_analyzed.replaceWith(`<span id="statistics-analyzed">${_.sum(analyzed).toLocaleString()}</span>`);

            return {
                labels: languages,
                datasets: [
                    {
                        label: "Mined",
                        data: mined,
                        borderColor: blue20,
                        backgroundColor: blue20.map(color => `${color}bf`),
                        barPercentage: 1
                    },
                    {
                        label: "Analyzed",
                        data: analyzed,
                        borderColor: red20,
                        backgroundColor: red20.map(color => `${color}bf`),
                        barPercentage: 1
                    }
                ]
            };
        })
        .then(data => new Chart(canvas, { type: "bar", options, data }))
        .catch(() => $toast_container.twbsToast({
            id: "statistics-toast",
            body: "Could not retrieve repository statistics!"
        }));
}(base, jQuery, _, Chart));

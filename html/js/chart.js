(function (base, $, _, Handlebars, Chart, chroma) {
    const $statistics_chart = $("#statistics-chart");
    const $statistics_chart_spinner = $("#statistics-chart-spinner");
    const $toast_container = $(".toast-container");
    const $statistics_mined = $("#statistics-mined");
    const $statistics_analyzed = $("#statistics-analyzed");
    const $statistics_csv_btn = $("#statistics-csv-btn");
    const $statistics_png_btn = $("#statistics-png-btn");
    const $table_body = $("#statistics-table > tbody");
    const $table_rows_template = $("#template-table-rows").html();
    const $table_rows_content = Handlebars.compile($table_rows_template);
    const [ canvas ] = $statistics_chart.get();

    const percentage = (numerator, denominator) => (numerator / denominator * 100).toFixed(2);

    Chart.defaults.font.family = "Trebuchet MS";

    const options = {
        animation: {
            duration: 0,
        },
        responsive: true,
        layout: {
            padding: {
                top: 20,
                left: 10,
                bottom: 10,
                right: 20,
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
                    },
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
                borderWidth: 1,
            },
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
                        return `Analyzed: ${percentage(analyzed, mined)}%`;
                    },
                },
            },
        },
    };

    fetch(`${base}/r/stats`)
        .then(response => response.json())
        .then(json => {
            const total = Object.values(json).reduce((acc, { mined, analyzed }) => {
                acc.mined += mined;
                acc.analyzed += analyzed;
                return acc;
            }, { mined: 0, analyzed: 0 });
            const coverage = `${percentage(total.analyzed, total.mined)}%`;
            $statistics_mined.replaceWith(`<span id="statistics-mined">${total.mined.toLocaleString()}</span>`);
            $statistics_analyzed.replaceWith(`<span id="statistics-analyzed">${coverage}</span>`);
            return Object.entries(json)
                .map(([key, value]) => ({ x: key, ...value }))
                .filter(({ mined }) => mined > 0);
        })
        .then(data => {
            const transformed = data.map(({x: language, mined, analyzed}) => ({language, mined, analyzed}));
            $table_body.html($table_rows_content(transformed));
            return data;
        })
        .then(data => {
            $statistics_csv_btn.removeClass("d-none")
                .prop("disabled", false)
                .on("click", () => {
                    const header = "\"language\",\"mined\",\"analyzed\",\"coverage\"";
                    const body = data.map(({ x: language, mined, analyzed }) => {
                        const percentage = mined ? analyzed / mined : 0;
                        return `"${language}","${mined}","${analyzed}","${percentage.toFixed(4)}"`;
                    }).join("\n");
                    const content = `${header}\n${body}\n`;
                    const blob = new Blob([ content ], { type: "text/csv;charset=utf-8," });
                    const url = URL.createObjectURL(blob);
                    const anchor = document.createElement("a");
                    anchor.setAttribute("href", url);
                    anchor.setAttribute("download", "statistics.csv");
                    anchor.click();
                    anchor.remove();
                });
            return data;
        })
        .then(data => {
            const palette = chroma.scale(["#0f0f0f", "#f2f2f2"]).mode("lch").colors(data.length);
            return {
                datasets: [
                    {
                        data,
                        label: "Mined",
                        borderColor: palette,
                        backgroundColor: palette.map(color => `${color}bf`),
                        barPercentage: 1,
                        parsing: {
                            yAxisKey: "mined",
                        },
                    },
                ]
            };
        })
        .then(data => {
            $statistics_chart_spinner.addClass("d-none");
            $statistics_chart.removeClass("d-none");
            return new Chart(canvas, { type: "bar", options, data });
        })
        .then(chart => {
            $statistics_png_btn.removeClass("d-none")
                .prop("disabled", false)
                .on("click", () => {
                    const url = chart.toBase64Image();
                    const anchor = document.createElement("a");
                    anchor.setAttribute("href", url);
                    anchor.setAttribute("download", "statistics.png");
                    anchor.click();
                    anchor.remove();
                });
        })
        .catch(() => $toast_container.twbsToast({
            id: "statistics-toast",
            body: "Could not retrieve repository statistics!"
        }));
}(base, jQuery, _, Handlebars, Chart, chroma));

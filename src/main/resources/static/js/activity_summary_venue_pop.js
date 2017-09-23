requirejs(["moment", "Chart", "lodash", "dashohelper", "chroma", "distinct-colors"],
    function (moment, Chart, _, Helper, chroma, distinctColors) {

    function rebuild() {
            let promise = Helper.get("/api/meta/latestdate");
            promise.then(function (latestDate) {
                let dateRange = Helper.createDateRangeObj(latestDate, 7);
                const getParams = "start=" + dateRange.start.toISOString() + "&end=" + dateRange.end.toISOString();
                return Helper.get(encodeURI("/api/sales/txsummary?" + getParams));
            }).then(function(graphData) {
                drawVenuePopGraph(graphData);
            }).catch(function (error) {
                console.log(error);
            });
        }

        function drawVenuePopGraph(graphData){
            const rawData = JSON.parse(graphData);
            let hlabels = parseTotalSalesDataSetLabels(rawData);
            console.log(hlabels);
            drawChart("venue-popularity-canvas", "Number of Transactions by Venue", hlabels, dataset)
        }

        function parseTotalSalesDataSetLabels(raw) {
            let outlets = [];
            _.each(raw, function (v, k) {
                _.each(v, function (entry) {
                    if (!_.includes(outlets, entry.outletname)) { outlets.push(entry.outletname); }
                })
            });
            return outlets;
        }

        function drawChart(selector, title, hLabels, dataset) {
            let chartConfig = {
                type: 'horizontalBar',
                data: {
                    labels: hLabels,
                    datasets: [{
                        label: 'Total Sales - £',
                        data: dataset,
                        backgroundColor: [
                            'rgb(255, 99, 132)',
                            'rgb(54, 162, 235)',
                            'rgb(255, 206, 86)',
                            'rgb(75, 192, 192)',
                            'rgb(153, 102, 255)'
                        ],
                        borderColor: [
                            'rgba(255,99,132,1)',
                            'rgba(54, 162, 235, 1)',
                            'rgba(255, 206, 86, 1)',
                            'rgba(75, 192, 192, 1)',
                            'rgba(153, 102, 255, 1)'
                        ],
                        borderWidth: 1
                    }]
                },
                options: {
                    scales: {
                        xAxes: [{}],
                        yAxes: [{
                            ticks: {
                                beginAtZero: true
                            }
                        }],
                        title:{
                            display:true,
                            text: title
                        }
                    }
                }
            };

            console.debug("CHART CFG:");
            console.debug(chartConfig);

            let ctx = document.getElementById(selector);
            new Chart(ctx, chartConfig);
        }

window.onload += rebuild();
// END requirejs
});
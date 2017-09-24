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
            let dataSet = parseTotalSalesDataSet(rawData);
            drawChart("venue-popularity-canvas", "Revenue by Venue Over the Last 7 Days", hlabels, dataSet);
        }

        function parseTotalSalesDataSetLabels(raw) {
            let outlets = [];
            _.each(raw, function (v) {
                    outlets.push(v.outletname);
            });
            return outlets;
        }

        function parseTotalSalesDataSet(raw) {
            _.each(raw, function (v) {
                delete v.cashspent;
                delete v.discountamount;
                v.x = v.outletname;
                v.y = v.totalamount;
                delete  v.outletname;
                delete  v.totalamount;
            });
            return raw;
        }

        function drawChart(selector, title, hLabels, dataset) {
            console.log(hLabels)
            let chartConfig = {
                type: 'bar',
                data: {
                    labels: hLabels,
                    datasets: [{
                        label: "BarchartData",
                        data: dataset,
                        backgroundColor: [
                            'rgb(255, 99, 132)',
                            'rgb(54, 162, 235)',
                            'rgb(255, 206, 86)',
                            'rgb(75, 192, 192)',
                            'rgb(153, 102, 255)',
                            'rgb(76, 99, 132)',
                            'rgb(34, 162, 235)',
                            'rgb(255, 123, 86)',
                            'rgb(75, 17, 192)',
                            'rgb(153, 188, 255)'
                        ],
                        borderColor: [
                            'rgba(255,99,132,1)',
                            'rgba(54, 162, 235, 1)',
                            'rgba(255, 206, 86, 1)',
                            'rgba(75, 192, 192, 1)',
                            'rgba(153, 102, 255, 1)',
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
                    responsive: true,
                    maintainAspectRatio: true,
                    title:{
                        display: true,
                        text: title
                    },
                    legend: {
                        display: false
                    },
                    scales: {
                        xAxes: [{ticks: {
                            autoSkip: false
                        }}],
                        yAxes: [{
                            ticks: {
                                beginAtZero: true
                            },
                            scaleLabel:{
                                display: true,
                                labelString: "£"
                            }
                        }]
                    },
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
requirejs(["moment", "Chart", "lodash", "dashohelper", "chroma", "distinct-colors"], function (moment, Chart, _, Helper, chroma, distinctColors) {    var ctx = document.getElementById("busiestTimes");

    function rebuild() {
        let promise = Helper.get("/api/meta/latestdate");
        promise.then(function (latestDate) {
            let dateRange = Helper.createDateRangeObj(latestDate, 7);
            const getParams = "start=" + dateRange.start.toISOString() + "&end=" + dateRange.end.toISOString();
            return Helper.get(encodeURI("/api/summary/busybot/venues?" + getParams));
        }).then(function(graphData) {
            drawVenuePopGraph(graphData);
        }).catch(function (error) {
            console.log(error);
        });
    }

    function drawVenuePopGraph(graphData){
        const rawData = JSON.parse(graphData);
        console.log(rawData);
        let busiestTimeData = parseRawDataSet(rawData);
        // let vlabels = parseTotalSalesDataSetLabels(topFiveData);
        // let colourPallete = getColorPallete(vlabels.length);
        // drawChart("busiestTimes", "Revenue by Venue Over the Last 7 Days", vlabels, colourPallete, topFiveData);
    }

    function parseRawDataSet(raw) {
        let times = [];
        _.map(raw, function (v) {
            console.log(v);
            _.reduce(v, function(){

            })
        });
        return times;
    }

    function drawChart(selector, title, hlabels, colourPallete, dataset){
        let chartConfig = {
            type: 'bar',
            data: {
                labels: hlabels,
                datasets: [{
                    label: '# of Transactions',
                    data: dataset,
                    backgroundColor: colourPallete,
                    borderColor: colourPallete,
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                legend:{
                    display: false
                },
                scales: {
                    yAxes: [{
                        ticks: {
                            beginAtZero: true
                        },
                        scaleLabel:{
                            display: true,
                            labelString: "# of Transactions"
                        }
                    }],
                    xAxes: [{
                        ticks: {
                            autoSkip: false
                        }
                    }]
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
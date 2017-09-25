requirejs(["moment", "Chart", "lodash", "dashohelper", "chroma", "distinct-colors"], function (moment, Chart, _, Helper, chroma, distinctColors) {    var ctx = document.getElementById("busiestTimes");

    function rebuild() {
        let promise = Helper.get("/api/meta/latestdate");
        promise.then(function (latestDate) {
            let dateRange = Helper.createDateRangeObj(latestDate, 7);
            const getParams = "start=" + dateRange.start.toISOString() + "&end=" + dateRange.end.toISOString();
            return Helper.get(encodeURI("/api/summary/busybot?" + getParams));
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
        let hLabels = generateLabels(busiestTimeData);
        let colourPallete = getColorPallete(hLabels.length);
         drawChart("busiestTimes", "Average number of transactions per hour of the day", hLabels, colourPallete, busiestTimeData);
    }

    function getColorPallete(numOfColours){
        let chartPallete = distinctColors({count: numOfColours, lightMin: 50, chromaMin: 50});
        let flattenedPallete = [];
        _.each(chartPallete, function(value){
            let rgbCode = value.rgba();
            let palleteEntry = "rgba("+rgbCode+")";
            flattenedPallete.push(palleteEntry);
        });
        return flattenedPallete;
    }

    function generateLabels(busiestTimeGraphData){
        let hLabels = [];
        _.each(busiestTimeGraphData, function (dataPoint) {
            if(dataPoint.y >= 1){
                hLabels.push(dataPoint.x);
            }
        });
        console.log(hLabels);
        return hLabels;
    }

    function dataPoint(x, y) {
        this.x = x;
        this.y = y;
    }

    function parseRawDataSet(raw) {
        let times = [];
        _.each(raw, function (value, key) {
            if(value >= 1){
                times.push(new dataPoint(key, value.toPrecision(1)));
            }
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
                            labelString: "Average # of Transactions"
                        }
                    }],
                    xAxes: [{
                        scaleLabel:{
                          display: true,
                          labelString: "Time of day (24hr)"
                        },
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
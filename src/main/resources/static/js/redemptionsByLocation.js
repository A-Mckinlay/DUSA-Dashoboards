requirejs(["moment", "Chart", "lodash", "dashohelper", "chroma", "distinct-colors"], function (moment, Chart, _, Helper, chroma, distinctColors) {    var ctx = document.getElementById("busiestTimes");
    function rebuild() {
        let promise = Helper.get("/api/meta/latestdate");
        promise.then(function (latestDate) {
            let dateRange = Helper.createDateRangeObj(latestDate, 7);
            const getParams = "start=" + dateRange.start.toISOString() + "&end=" + dateRange.end.toISOString();
            return Helper.get(encodeURI("/api/loyalty/txtypes/venues?" + getParams));
        }).then(function(graphData) {
            drawRedsByLocGraph(graphData);
        }).catch(function (error) {
            console.log(error);
        });
    }

    function drawRedsByLocGraph(graphData){
        const rawData = JSON.parse(graphData);
        let redsByLocData = parseRawDataSet(rawData);
        let hLabels = getHLabels(redsByLocData);
        let colourPallete = getColourPallete(hLabels.length);
        drawChart("redemptions-by-location", "Number of Redemptions by Location", hLabels, colourPallete, redsByLocData);
    }

    function getColourPallete(numOfColours){
        let chartPallete = distinctColors({count: numOfColours, lightMin: 50, chromaMin: 50});
        let flattenedPallete = [];
        _.each(chartPallete, function(value){
            let rgbCode = value.rgba();
            let palleteEntry = "rgba("+rgbCode+")";
            flattenedPallete.push(palleteEntry);
        });
        return flattenedPallete;
    }

    function getHLabels(dataSet){
        let hLables = [];
        _.each(dataSet, function(value, key){
            hLables.push(value.x);
        });
        return hLables;
    }

    function parseRawDataSet(raw) {
        let dataSet = _.map(raw, function(value, key){
            return{
                x: key,
                y: (value.Redemption |0)
           }
        });
        return dataSet;
    }

    function drawChart(selector, title, hlabels, colourPallete, dataset){
        let chartConfig = {
            type: 'bar',
            data: {
                labels: hlabels,
                datasets: [{
                    label: '# of Redemptions',
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
                            labelString: "Number of Redemptions"
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
requirejs(["moment", "Chart", "lodash", "dashohelper", "chroma", "distinct-colors"], function (moment, Chart, _, Helper, chroma, distinctColors) {    var ctx = document.getElementById("busiestTimes");

    function rebuild() {
        let promise = Helper.get("/api/meta/latestdate");
        promise.then(function (latestDate) {
            let dateRange = Helper.createDateRangeObj(latestDate, 7);
            const getParams = "start=" + dateRange.start.toISOString() + "&end=" + dateRange.end.toISOString();
            return Helper.get(encodeURI("/api/summary/busybot?" + getParams));
        }).then(function(graphData) {
            drawGraph(graphData);
        }).catch(function (error) {
            console.log(error);
        });
    }

    function drawGraph(graphData){
        const rawData = JSON.parse(graphData);
        let herdTransactionData = parseRawDataSet(rawData);
        console.log(herdTransactionData);
        let hLabels = generateLabels(herdTransactionData);
        console.log(hLabels);
        drawChart("9to3graph", "Average number of transactions per hour of the day", hLabels, herdTransactionData);
    }

    function generateLabels(herdTransactionData){
        let hLabels = [];
        let arrayOne = [];
        let arrayTwo = [];
        _.each(herdTransactionData, function (dataPoint) {
            if(dataPoint.x >=21 && dataPoint.x<24 ){
                arrayOne.push(dataPoint.x);
            }
            if(dataPoint.x >=0 && dataPoint.x<4 ){
                arrayTwo.push(dataPoint.x);
            }



        });
        hLabels = [].concat(arrayOne,arrayTwo);
        return hLabels;
    }

    function dataPoint(x, y) {
        this.x = x;
        this.y = y;
    }

    function parseRawDataSet(raw) {
        let times = [];
        let arrayOne = [];
        let arrayTwo = [];
        _.each(raw, function (value, key) {
            if(key >=21 && key<24) {
                arrayOne.push(new dataPoint(key, value.toPrecision(1)));
            }
            if(key >=0 && key<4 ){
                arrayTwo.push(new dataPoint(key, value.toPrecision(1)));
            }
        });
        times = [].concat(arrayOne,arrayTwo);
        return times;
    }

    function drawChart(selector, title, hlabels, dataset){
        let chartConfig = {
            type: 'line',
            data: {
                labels: hlabels,
                datasets: [{
                    label: '# of Transactions',
                    data: dataset,
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                title: {
                    display: false,
                    text: title
                },
                scales: {
                    xAxes: [{
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: 'Day (9pm - 3am)'
                        }
                    }],
                    yAxes: [{
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: 'Average number of transactions'
                        },
                        ticks: {
                            beginAtZero: false
                        },
                    }]
                },
            }
        };

        let ctx = document.getElementById(selector);
        new Chart(ctx, chartConfig);
    }
    window.onload += rebuild();
// END requirejs
});
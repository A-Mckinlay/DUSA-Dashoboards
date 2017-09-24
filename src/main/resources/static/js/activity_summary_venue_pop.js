requirejs(["moment", "Chart", "lodash", "dashohelper", "chroma", "distinct-colors"], function (moment, Chart, _, Helper, chroma, distinctColors) {

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
        let colourPallete = getColorPallete(hlabels.length);
        let dataSet = parseTotalSalesDataSet(rawData);
        drawChart("venue-popularity-canvas", "Revenue by Venue Over the Last 7 Days", hlabels, colourPallete, dataSet);
    }

    function parseTotalSalesDataSetLabels(raw) {
        let outlets = [];
        _.each(raw, function (v) {
                outlets.push(v.outletname);
        });
        return outlets;
    }

    function parseTotalSalesDataSet(raw) {//Note: creating the property x and y here so that the object can be interpreted by Charts.js lib.
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

    function getColorPallete(numOfColours){
        let chartPallete = distinctColors({count: numOfColours, lightMin: 50, chromaMin: 50});
        let flattenedPallete = [];
        _.each(chartPallete, function(value){
            let rgbCode = value._rgb.toString();
            let palleteEntry = "rgba("+rgbCode+")";
            flattenedPallete.push(palleteEntry);
        });
        return flattenedPallete;
    }

    function drawChart(selector, title, hLabels, colourPallete, dataset) {
        let chartConfig = {
            type: 'bar',
            data: {
                labels: hLabels,
                datasets: [{
                    label: "BarchartData",
                    data: dataset,
                    backgroundColor: colourPallete,
                    borderColor: colourPallete,
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
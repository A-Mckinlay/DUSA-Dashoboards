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
        let topFiveData = parseTotalSalesDataSet(rawData);
        let vlabels = parseTotalSalesDataSetLabels(topFiveData);
        let colourPallete = getColorPallete(vlabels.length);
        drawChart("top5BarGraph", "Revenue by Venue Over the Last 7 Days", vlabels, colourPallete, topFiveData);
    }

    function parseTotalSalesDataSetLabels(topFiveData) {
        let outlets = [];
        _.each(topFiveData, function (v) {
            outlets.push(v.y);
        });
        return outlets;
    }

    function parseTotalSalesDataSet(raw) {
        _.each(raw, function (v) { //Note: creating the property x and y here so that the object can be interpreted by Charts.js lib.
            delete v.cashspent;
            delete v.discountamount;
            if(v.outletname == "DUSA The Union - Marketplace") {
                v.y = "Marketplace";
            }
            else{
                v.y = v.outletname;
            }
            v.x = v.totalamount;
            delete  v.outletname;
            delete  v.totalamount;
        });
        raw = _.sortBy(raw, function (entry) {//sort to ascending
            return entry.x;
        });
        raw.reverse();//change sort to descending
        raw.length = 5;
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

    function drawChart(selector, title, vlabels, colourPallete, dataset){
        let chartConfig = {
            type: 'horizontalBar',
            data: {
                labels: vlabels,
                datasets: [{
                    label: 'Total Sales - £',
                    data: dataset,
                    backgroundColor: colourPallete,
                    borderColor: colourPallete,
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                scales: {
                    xAxes: [{scaleLabel:{
                        display: true,
                        labelString: "Total Sales - £"
                    }}],
                    yAxes: [{
                        ticks: {
                            beginAtZero: true
                        }
                    }]
                },
                legend:{
                    display: false
                }
            }
        };
        let ctx = document.getElementById(selector);
        new Chart(ctx, chartConfig);
    }

    window.onload += rebuild();
// END requirejs
});
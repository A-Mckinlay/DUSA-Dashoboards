requirejs(["moment", "Chart", "lodash", "dashohelper", "chroma", "distinct-colors"], function (moment, Chart, _, Helper, chroma, distinctColors) {

    function rebuild() {
        let promise = Helper.get("/api/meta/latestdate");
        promise.then(function (latestDate) {
            let dateRange = Helper.createDateRangeObj(latestDate, 7);
            const getParams = "start=" + dateRange.start.toISOString() + "&end=" + dateRange.end.toISOString();
            return Helper.get(encodeURI("/api/summary/busybot/venues?" + getParams));
        }).then(function (graphData) {
            drawVenuePopGraph(graphData);
        }).catch(function (error) {
            console.log(error);
        });
    }

    function drawVenuePopGraph(graphData) {
        console.log("GraphData: " + graphData);
        const rawData = JSON.parse(graphData);
        let dataSets = parseTotalSalesDataSet(rawData);
        console.log("rawData" + rawData["Library"]["0"]);
        drawChart("nightVsDay");
    }

    function parseTimeDataSetLabels(rawData) {
        let times = [];

    }

    function parseTotalSalesDataSet(rawData) {
        let locationData = [];
        _.each(rawData, function(value, key){
            let locationDataObject = {};
            locationDataObject[key] = value;
            locationData.push(locationDataObject);
        });
        console.log(locationData);

        let valueArray = [];

        console.log(valueArray);

    }

    function objToArray(obj){
        let array = [];
        _.each(obj, function (value, key) {
            array.push(value)
        })
        return array;
    }


    function drawChart(selector) {
        let chartConfig =
            {
                type: 'radar',
                data: {
                    labels: null,
                    datasets: [{
                        label: '# of Transactions',
                        data: null,
                        backgroundColor: null,
                        borderColor: null,
                        borderWidth: 1
                    }]
                },

                options: {
                    maintainAspectRatio: true,
                    scale: {
                        display: true

                    }
                }
            };
        let ctx = document.getElementById(selector);
        new Chart(ctx, chartConfig);
    }

    window.onload += rebuild();
});
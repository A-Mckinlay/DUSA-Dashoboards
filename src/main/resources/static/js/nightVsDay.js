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
        const rawData = JSON.parse(graphData);
        let dataSets = parseRawData(rawData);
        let hlabels = getHLabels();
        drawChart("nightVsDay", "# of transactions by venue by hour", hlabels, dataSets);
    }


    function getHLabels(){
        let hLables = new Array(24);
        for(let i=0; i<hLables.length; i++){
            hLables[i] = i;
        }
        return hLables;
    }

    function parseRawData(rawData){
        let locationData = [];
        _.each(rawData, function(value, key){
            let locationDataObject = {};
            locationDataObject[key] = value;
            locationData.push(locationDataObject);
        });

        let colours = distinctColors({
            count: locationData.length,
            lightMin: 50,
            lightMax: 90
        });

        let marshalled = _.map(locationData, function(value) {
            let obj = _.toPairs(value);
            obj = _.first(obj);
            let vals = _.map(obj[1], function(v) { return v; });
            return {
                label: obj[0],
                data: vals,
                backgroundColor: colours.pop().alpha(0.5).css()
            }
        });
        return marshalled;
    }

    function drawChart(selector, title, hlabels, dataSets) {
        let chartConfig =
            {
                type: 'radar',
                data: {
                    labels: hlabels,
                    datasets: dataSets
                },

                options: {
                    title:{
                        display: true,
                        text: title
                    },
                    responsive: true,
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
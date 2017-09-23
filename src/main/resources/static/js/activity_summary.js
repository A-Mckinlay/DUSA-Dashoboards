requirejs(["moment", "Chart", "lodash", "dashohelper", "chroma", "distinct-colors"],
    function (moment, Chart, _, Helper, chroma, distinctColors) {

    function getDataDrawGraph() {
        let promise = Helper.get("/api/meta/latestdate");
        promise.then(function (latestDate) {
            let dateRange = Helper.createDateRangeObj(latestDate, 28);
            const getParams = "start=" + dateRange.start.toISOString() + "&end=" + dateRange.end.toISOString();
            let url = "/api/sales/dailytx?" + getParams;
            url = encodeURI(url);
            return Helper.get(url);
        }).then(function (graphData) {
            drawGraph(graphData);
        }).catch(function (error) {
            console.log(error);
        });
    }

    function parseDateLabels(raw) {
        let timestamps = [];
        _.each(raw, function (key, value) {
                timestamps.push(moment(value));
        });
        timestamps = _.sortBy(timestamps, function (x) { return x.unix(); });
        return _.map(timestamps, function (it) {
            return it.format("DD/MM/YY");
        });
    }

    function parseDataSetLabels(raw) {
        let outlets = [];
        _.each(raw, function (v, k) {
            _.each(v, function (entry) {
                if (!_.includes(outlets, entry.outletname)) { outlets.push(entry.outletname); }
            })
        });
        return outlets;
    }

    function parseDatasets(raw, outlets) {
        let remapped = _.map(raw, function(v, k) {
            return {
                date: k,
                values: v
            };
        });
        remapped = _.sortBy(remapped, function (x) {
            return moment(x.date).unix();
        });

        let values = _.map(remapped, function (x) {
            return x.values;
        });
        return _.map(values, function (x) {
            //console.log(x);
            let ret = new Array(_.size(outlets));
            ret.fill(0);
            _.forEach(x, function (y) {
                let idx = _.indexOf(outlets, y.outletname);
                ret[idx] = y.totalamount;
            });
            return ret;
        })
    }

    function drawGraph(graphData) {
        const rawData = JSON.parse(graphData);
        let outlets = parseDataSetLabels(rawData);
        let dateLabels = parseDateLabels(rawData);
        let datasets = parseDatasets(rawData, outlets);
        console.debug("Outlets/dates:");
        console.debug(outlets);
        console.debug(dateLabels);
        let chartConfig = {
            type: 'line',
            data: {
                datasets: [],
                labels: dateLabels
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                scales: {
                    yAxes: [{
                        ticks: {
                            beginAtZero: true
                        },
                        stacked: true
                    }]
                }
            }
        };

        // Cheat and use a matrix transpose to rotate the 2D array! From https://stackoverflow.com/a/31001358
        let dsTranspose = _.zip(...datasets);
        console.debug("Transposed values:");
        console.debug(dsTranspose);
        let colors = distinctColors({
            count: _.size(outlets),
            lightMin: 50,
            lightMax: 90
        });
        for (let i = 0; i < _.size(outlets); i++) {
            let ds = {
                label: outlets[i],
                data: dsTranspose[i],
                backgroundColor: colors[i].hex(),
                borderColor: colors[i].darken().hex(),
            };
            //console.log(ds);
            chartConfig.data.datasets.push(ds);
        }
        console.debug("CHART CFG:");
        console.debug(chartConfig);

        let ctx = document.getElementById("myChart");
        //console.log(ctx);
        let myChart = new Chart(ctx, chartConfig);
        //console.log(myChart);
    }
    getDataDrawGraph();
// END requirejs
});
requirejs(["moment", "Chart", "lodash", "dashohelper", "chroma", "distinct-colors"],
    function (moment, Chart, _, Helper, chroma, distinctColors) {

    function rebuildAll() {
        let promise = Helper.get("/api/meta/latestdate");
        promise.then(function (latestDate) {
            let dateRange = Helper.createDateRangeObj(latestDate, 28);
            const getParams = "start=" + dateRange.start.toISOString() + "&end=" + dateRange.end.toISOString();
            return Helper.get(encodeURI("/api/sales/dailytx?" + getParams));
        }).then(function(graphData) {
            drawTotalSalesGraph(graphData);
            drawTxSummaryGraph(graphData);
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

    function parseTotalSalesDataSetLabels(raw) {
        let outlets = [];
        _.each(raw, function (v, k) {
            _.each(v, function (entry) {
                if (!_.includes(outlets, entry.outletname)) { outlets.push(entry.outletname); }
            })
        });
        return outlets;
    }

    function parseTotalSalesDatasets(raw, outlets) {
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

    function drawChart(selector, hLabels, datasets) {
        let chartConfig = {
            type: 'line',
            data: {
                datasets: datasets,
                labels: hLabels
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                title:{
                    display:true,
                    text:'Revenue by Venue Over the Last 28 Days'
                },
                scales: {
                    xAxes: [{
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: 'Day'
                        }
                    }],
                    yAxes: [{
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: '£'
                        },
                        ticks: {
                            beginAtZero: true
                        },
                        stacked: true,
                    }]
                },
            }
        };

        console.debug("CHART CFG:");
        console.debug(chartConfig);

        let ctx = document.getElementById(selector);
        new Chart(ctx, chartConfig);
    }

    function drawTotalSalesGraph(graphData) {
        console.debug("===== START TOTAL SALES =====");
        const rawData = JSON.parse(graphData);
        let outlets = parseTotalSalesDataSetLabels(rawData);
        let dateLabels = parseDateLabels(rawData);
        let datasets = parseTotalSalesDatasets(rawData, outlets);
        console.debug("Outlets/dates:");
        console.debug(outlets);
        console.debug(dateLabels);

        // Cheat and use a matrix transpose to rotate the 2D array! From https://stackoverflow.com/a/31001358
        let dsTranspose = _.zip(...datasets);
        console.debug("Transposed values:");
        console.debug(dsTranspose);
        let colors = distinctColors({
            count: _.size(outlets),
            lightMin: 50,
            lightMax: 90
        });
        let chartDatasets = [];
        for (let i = 0; i < _.size(outlets); i++) {
            let ds = {
                label: outlets[i],
                data: dsTranspose[i],
                backgroundColor: colors[i].hex(),
                borderColor: colors[i].darken().hex(),
            };
            chartDatasets.push(ds);
        }

        drawChart("total-sales-canvas", dateLabels, chartDatasets);
    }

    function drawTxSummaryGraph(graphData) {
        console.debug("===== START TX SUMMARY =====");
        const rawData = JSON.parse(graphData);
        console.debug(rawData);
    }

    window.onload += rebuildAll();
// END requirejs
});
requirejs(["moment", "Chart", "lodash", "dashohelper", "dateranger", "chroma", "distinct-colors"],
    function (moment, Chart, _, Helper, Dateranger, chroma, distinctColors) {

        const dateranger = Dateranger();
        dateranger.setWarningThreshold(60);

        let totalSalesChart = null;
        let txSummaryChart = null;
        let venuePopChart = null;

        function rebuildAll() {
            const getParams = "start=" + dateranger.getStart().toISOString() + "&end=" + dateranger.getEnd().toISOString();

            // Clear the deck
            if (totalSalesChart !== null) totalSalesChart.destroy();
            if (txSummaryChart !== null) txSummaryChart.destroy();
            if (venuePopChart !== null) venuePopChart.destroy();

            // Redraw
            getAndDrawTotalSales(getParams);
            getAndDrawTxSummary(getParams);
            getAndDrawVenuePop(getParams);
        }

        function getAndDrawTotalSales(getParams) {
            Helper.get(encodeURI("/api/sales/dailytx?" + getParams))
                .then(function (graphData) {
                    drawTotalSalesGraph(graphData);
                }).catch(function (error) {
                console.log(error);
            });
        }

        function getAndDrawTxSummary(getParams) {
            Helper.get(encodeURI("/api/summary/tx/daily?" + getParams))
                .then(function (graphData) {
                    drawTxSummaryGraph(graphData);
                }).catch(function (error) {
                console.log(error);
            });
        }

        function getAndDrawVenuePop(getParams) {
            Helper.get(encodeURI("/api/sales/txsummary?" + getParams))
                .then(function (graphData) {
                    drawVenuePopGraph(graphData)
                }).catch(function (error) {
                console.log(error)
            });
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

        function parseDateLabels(raw) {
            let timestamps = [];
            _.each(raw, function (key, value) {
                timestamps.push(moment(value));
            });
            timestamps = _.sortBy(timestamps, function (x) {
                return x.unix();
            });
            return _.map(timestamps, function (it) {
                return it.format("DD/MM/YY");
            });
        }

        function parseTotalSalesDataSetLabels(raw) {
            let outlets = [];
            _.each(raw, function (v, k) {
                _.each(v, function (entry) {
                    if (!_.includes(outlets, entry.outletname)) {
                        outlets.push(entry.outletname);
                    }
                })
            });
            return outlets;
        }

        function parseTotalSalesDatasets(raw, outlets) {
            let remapped = _.map(raw, function (v, k) {
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
                let ret = new Array(_.size(outlets));
                ret.fill(0);
                _.forEach(x, function (y) {
                    let idx = _.indexOf(outlets, y.outletname);
                    ret[idx] = y.totalamount;
                });
                return ret;
            })
        }

        function parseVenuePopDatasets(raw) {//Note: creating the property x and y here so that the object can be interpreted by Charts.js lib.
            _.each(raw, function (v) {
                delete v.cashspent;
                delete v.discountamount;
                v.x = v.outletname;
                v.y = v.totalamount;
                delete v.outletname;
                delete v.totalamount;
            });
            return raw;
        }

        function parseVenuePopDataSetLabels(raw) {
            let outlets = [];
            _.each(raw, function (v) {
                outlets.push(v.outletname);
            });
            return outlets;
        }

        function drawLineChart(selector, title, hLabels, datasets) {
            let chartConfig = {
                type: 'line',
                data: {
                    datasets: datasets,
                    labels: hLabels
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: true,
                    title: {
                        display: true,
                        text: title
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
            return new Chart(ctx, chartConfig);
        }

        function drawBarChart(selector, title, hLabels, colourPallete, dataset) {
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
                    title: {
                        display: true,
                        text: title
                    },
                    legend: {
                        display: false
                    },
                    scales: {
                        xAxes: [{
                            ticks: {
                                autoSkip: false
                            }
                        }],
                        yAxes: [{
                            ticks: {
                                beginAtZero: true
                            },
                            scaleLabel: {
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
            return new Chart(ctx, chartConfig);
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

            totalSalesChart = drawLineChart("total-sales-canvas", "Revenue by Venue", dateLabels, chartDatasets);
        }

        function drawTxSummaryGraph(graphData) {
            console.debug("===== START TX SUMMARY =====");
            const rawData = JSON.parse(graphData);
            console.debug("Raw data:");
            console.debug(rawData);

            const dsLabels = [
                "Payments",
                "Redemptions",
                "Reversals"
            ];
            const dateLabels = parseDateLabels(rawData);
            let remapped = _.map(rawData, function (v, k) {
                return {
                    date: moment(k),
                    payments: v.payments,
                    redemptions: v.redemptions,
                    reversals: v.reversals
                }
            });
            remapped = _.sortBy(remapped, function (x) {
                return x.date.unix();
            });
            let rawDsValues = _.map(remapped, function (x) {
                return [x.payments, x.redemptions, x.reversals];
            });
            rawDsValues = _.zip(...rawDsValues);

            const datasets = [
                {
                    label: "Payments",
                    data: rawDsValues[0],
                    backgroundColor: chroma('green').brighten().hex(),
                    borderColor: chroma('green').darker().hex()
                },
                {
                    label: "Redemptions",
                    data: rawDsValues[1],
                    backgroundColor: chroma('yellow').brighten().hex(),
                    borderColor: chroma('yellow').darker().hex()
                },
                {
                    label: "Reversals",
                    data: rawDsValues[2],
                    backgroundColor: chroma('red').brighten().hex(),
                    borderColor: chroma('red').darker().hex()
                }
            ];

            txSummaryChart = drawLineChart("transaction-types-canvas", "Transactions", dateLabels, datasets);
        }

        function drawVenuePopGraph(graphData){
            const rawData = JSON.parse(graphData);
            let hlabels = parseVenuePopDataSetLabels(rawData);
            let colourPallete = getColorPallete(hlabels.length);
            let dataSet = parseVenuePopDatasets(rawData);
            venuePopChart = drawBarChart("venue-popularity-canvas", "Revenue by Venue", hlabels, colourPallete, dataSet);
        }

        dateranger.addOnChangeHandler(rebuildAll)

// END requirejs
    });
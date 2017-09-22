requirejs(["moment", "Chart", "lodash", "dashohelper"], function (moment, Chart, _, Helper) {

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

    function parseDateLabels(graphData){
        const data = JSON.parse(graphData);
        let unixTimeStamps = [];
        _.each(data, function (key, value) {
            if (key.length > 0) {
                unixTimeStamps.push(moment(value).unix());
            }
        });
        unixTimeStamps.sort(function(a, b){return a-b});//Sort in ascending order
        let labels = [];
        for(let i=0; i<unixTimeStamps.length; i++){
            labels[i] = moment.unix(unixTimeStamps[i]).format("DD/MM/YY");
        }
        return labels;
    }

    function parseDataSetLabels(graphData){
        const data = JSON.parse(graphData);
        console.log(data);
        let possibleLabels = [];
        _.each(data, function (value, key) {
            if (key.length > 0) {
                _.each(value, function(value, key){
                   possibleLabels.push(value.outletname);
                });
            }
        });
        let labelSet = new Set(possibleLabels);
        let labels = Array.from(labelSet);

        let DataPoint = function(date, value){
            this.date = date;
            this.value = value;
        }

        let DataSet = function(outletName, dataPoints){
            this.outletName = outletName;
            this.dataPoints = dataPoints;
        };

        let dataSets = [];
        for(let i=0; i<labels.length; i++){
            let dataSet = new DataSet(labels[i], []);
            dataSets.push(dataSet);
        }

        let mappedArray = _.map(data, function(value, key){
            return {
                date: key,
                entries: value
            }
        });

        let dataArray = _.sortBy(mappedArray, function(value){
            return moment(value.date).unix();
        });
        console.log(dataArray);
        for(let i=0; i<dataArray.length; i++)
        {
            for(let j=0; j<dataSets.length; j++)
            {
                if(dataArray[i].entries.length === 0)
                {
                    dataSets[j].dataPoints.push(new DataPoint(dataArray[i].date, 0))
                }
                if(dataArray[i].entries.length > 0){
                    for(let k=0; k<dataArray[i].entries.length; k++){
                        for(let z=0; z<dataSets[j].dataPoints.length; z++) {
                            if (dataSets[j].outletName === dataArray[i].entries[k].outletname) {
                                if (dataSets[j].dataPoints[z].date === dataArray[i].date){
                                    dataSets[j].dataPoints[z].value += dataArray[i].entries[k].totalamount;
                                }else{
                                    dataSets[j].dataPoints.push(new DataPoint(dataArray[i].date, dataArray[i].entries[k].totalamount));
                                }
                            }
                        }
                    }
                }
            }
        }
        // console.log(dataSets);
    }

    function drawGraph(graphData) {
        let labels = parseDateLabels(graphData);
        let dataSetLabels = parseDataSetLabels(graphData);
        let ctx = document.getElementById("myChart");
        let myChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: dataSetLabels,
                        data: [2, 23, 45, 2, 34, 5, 6, 43],
                        backgroundColor: [
                            'rgba(255, 99, 132, 0.2)',
                            // 'rgba(54, 162, 235, 0.2)',
                            // 'rgba(255, 206, 86, 0.2)',
                            // 'rgba(75, 192, 192, 0.2)',
                            // 'rgba(153, 102, 255, 0.2)',
                            // 'rgba(255, 159, 64, 0.2)'
                        ],
                        borderColor: [
                            'rgba(255,99,132,1)',
                            // 'rgba(54, 162, 235, 1)',
                            // 'rgba(255, 206, 86, 1)',
                            // 'rgba(75, 192, 192, 1)',
                            // 'rgba(153, 102, 255, 1)',
                            // 'rgba(255, 159, 64, 1)'
                    ],
                    borderWidth: 1
                },
                    {

                    }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    yAxes: [{
                        ticks: {
                            beginAtZero: true
                        }
                    }]
                }
            }
        });
    }
    getDataDrawGraph();
// END requirejs
});
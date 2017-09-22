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

        let dataSet = {
            outletName: "",
            dataPoints: []
        };

        for(let i=0; i<labels.length; i++){
            _.each(data, function (value, key) {
                console.log(key);
                _.each(value, function(value, key){
                    console.log("data point: " + value.outletname + " " + value.totalamount);
                });
            });
        }
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
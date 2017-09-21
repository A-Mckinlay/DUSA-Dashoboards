requirejs(["moment", "Chart"], function (moment, Chart) {

    function createDateRangeObj(latestDate, numberOfDays) {
        let endDate = new Date(latestDate);
        let originDate = new Date(latestDate);
        originDate.setDate(originDate.getDate() - numberOfDays);//TODO: Decide if 28 should be 30/31/somethingelse
        let dateRange = {
            start: originDate,
            end: endDate
        }
        console.log(dateRange);
        return dateRange;
    }

    function getDataDrawGraph() {

        function get(url) {
            return new Promise(function (resolve, reject) {
                let xhttp = new XMLHttpRequest();
                xhttp.open("GET", url, true);
                xhttp.onload = function () {
                    if (xhttp.status == 200) {
                        resolve(xhttp.response);
                    } else {
                        reject(xhttp.statusText);
                    }
                };
                xhttp.onerror = function () {
                    reject(xhttp.statusText);
                };
                xhttp.send();
            });
        }

        let promise = get("/api/meta/latestdate");
        promise.then(function (latestDate) {
            let dateRange = createDateRangeObj(latestDate, 28);
            const getParams = "start=" + dateRange.start.toISOString() + "&end=" + dateRange.end.toISOString()
            let url = "/api/sales/dailytx?" + getParams;
            url = encodeURI(url);
            return get(url);
        }).then(function(graphData){
            drawGraph(graphData);
        }).catch(function (error) {
            console.log(error);
        });
    }
    function drawGraph(graphData) {
        let ctx = document.getElementById("myChart");
        console.log(graphData);
        let myChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: ["Red", "Blue", "Yellow", "Green", "Purple", "Orange"],
                datasets: [{
                    label: '# of Votes',
                    data: [2, 23, 45, 2, 34, 5, 6, 43],
                    backgroundColor: [
                        'rgba(255, 99, 132, 0.2)',
                    ],
                    borderColor: [
                        'rgba(255,99,132,1)',
                    ],
                    borderWidth: 1
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
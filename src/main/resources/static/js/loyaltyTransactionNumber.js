requirejs(["moment"], function (moment) {

    function createDateRangeObj(latestDate, endNumberOfDays, originNumberOfDays) {
        let endDate = new Date(latestDate);
        let originDate = new Date(latestDate);
        originDate.setDate(originDate.getDate() - originNumberOfDays);
        endDate.setDate(endDate.getDate() - endNumberOfDays);
        let dateRange = {
            start: originDate,
            end: endDate
        }
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
            let dateRange = createDateRangeObj(latestDate,0, 7);
            const getParams = "start=" + dateRange.start.toISOString() + "&end=" + dateRange.end.toISOString()
            let url = "/api/summary/tx?" + getParams;
            url = encodeURI(url);
            return get(url);
        }).then(function(graphData) {
            drawGraph(graphData);
            return get("/api/meta/latestdate");
        }).then(function (latestDate) {
            let dateRange = createDateRangeObj(latestDate, 7, 14);
            const getParams = "start=" + dateRange.start.toISOString() + "&end=" + dateRange.end.toISOString()
            let url = "/api/summary/tx?" + getParams;
            url = encodeURI(url);
            return get(url);
        }).then(function(trendData) {
            drawTrend(trendData);
        }).catch(function (error) {
            console.log(error);
        });

    }

    function drawGraph(graphData) {
        let ctx = document.getElementById("numTransactions");
        var parsedData = JSON.parse(graphData);
        console.log(graphData);
        ctx.innerHTML = parsedData["Redemption"];

    }

    function drawTrend(trendData) {
        let ctx = document.getElementById("previousNumTransactions");
        var parsedData = JSON.parse(trendData);
        let previousWeekValue = parsedData["Redemption"];
        ctx.innerHTML = previousWeekValue;

        let currentWeekValue = document.getElementById("numTransactions").innerHTML;

        if( parseFloat(currentWeekValue) > parseFloat(previousWeekValue ))
        {
            let greenTri = document.getElementById("numTransactionTrend");
            greenTri.src = "http://www.publicdomainpictures.net/pictures/40000/velka/basic-triangle-shape.jpg";
        }
        else if(parseFloat(currentWeekValue) < parseFloat(previousWeekValue))
        {
            let greenTri = document.getElementById("numTransactionTrend");
            greenTri.src = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c0/Red_Triangle.svg/1200px-Red_Triangle.svg.png";

        }
        else if(parseFloat(currentWeekValue) === parseFloat(previousWeekValue))
        {
            let greenTri = document.getElementById("numTransactionsTrend");
            greenTri.src = "http://www.charbase.com/images/glyph/9644";
        }
    }

    getDataDrawGraph();
// END requirejs
});
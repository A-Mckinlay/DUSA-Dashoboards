requirejs(["moment"], function (moment) {

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
            let dateRange = createDateRangeObj(latestDate, 7);
            const getParams = "start=" + dateRange.start.toISOString() + "&end=" + dateRange.end.toISOString()
            let url = "/api/users/avgspend?" + getParams;
            url = encodeURI(url);
            return get(url);
        }).then(function(graphData){
            drawGraph(graphData);
        }).catch(function (error) {
            console.log(error);
        });
    }
    function drawGraph(graphData) {
        let ctx = document.getElementById("averageSpend");
        var parsedData = JSON.parse(graphData);
        console.log(parsedData["cashspent"]);
        ctx.innerHTML = "£" + parsedData["cashspent"];

    }
    getDataDrawGraph();
// END requirejs
});
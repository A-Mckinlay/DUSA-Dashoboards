requirejs(["moment", "Chart"], function (moment, Chart) {

function createDateRangeObj(latestDate, numberOf, units) {
    const originDate = moment(latestDate).subtract(numberOf, units)
    const dateRange = {
        start: originDate.format("YYYY, MM, DD, hh:mm:ss, z"),
        end: latestDate
    }
    console.log(dateRange);
    return dateRange;
}

function main(){
    getData();
}

function getData() {

    function get(url, obj) {
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
            xhttp.send(obj);
        });
    }

    let promise = get("http://localhost:4567/api/meta/latestdate");//TODO: See Robert about this url
    promise.then(function (latestDate) {
        let dateRange = createDateRangeObj(latestDate, 1, 'month');
        return get("http://localhost:4567/api/sales/monthlytx", dateRange);
    }).then(function(graphData){
        console.log(graphData);
    }).catch(function (error) {
        console.log(error);
    });
}

// function getData(dateRange){
//     var xhttp;
//     xhttp = new XMLHttpRequest();
//     xhttp.onreadystatechange = function(){
//         if(this.readyState == 4 && this.status == 200) {
//
//         }
//     };
//     xhttp.open("GET", "/api/sales/monthlytx", true);
//     var json = JSON.stringify(dateRange)
//     console.log(json);
//     xhttp.send(json);
// }

var ctx =  document.getElementById("myChart");
main();
var myChart = new Chart(ctx, {
    type: 'line',
    data: {
        labels: ["Red", "Blue", "Yellow", "Green", "Purple", "Orange"],
        datasets: [{
            label: '# of Votes',
            data: [2,23,45,2,34,5,6,43],
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
                    beginAtZero:true
                }
            }]
        }
    }
});

// END requirejs
});
import moment from "/js/lib/moment.js"


function createDateRangeObj(latestDate) {
    const originDate = moment(latestDate).subtract(1, 'month')
    const dateRange = {
        start: originDate,
        end: latestDate
    }
    return dateRange;
}

function main(){
    let dateRange = getDateRange("http://localhost:4567/api/meta/latestdate")
    console.log(dateRange);
}

function getDateRange(url) {
    let latestDate =  new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest();
        xhr.open("GET", url);
        xhr.onload = () => resolve(xhr.responseText);
        xhr.onerror = () => reject(xhr.statusText);
        xhr.send();
    });

    latestDate.then(
        function createDateRangeObj(latestDate) {
            const originDate = moment(latestDate).subtract(1, 'month')
            const dateRange = {
                start: originDate,
                end: latestDate
            }
            return dateRange;
        }
    ).catch(
        (reason) => {
            console.log('Handle rejected promise ('+reason+') here.');
        });
};

getLatestDate.then()


function getData(dateRange){
    var xhttp;
    xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function(){
        if(this.readyState == 4 && this.status == 200) {

        }
    };
    xhttp.open("GET", "/api/sales/monthlytx", true);
    var json = JSON.stringify(dateRange)
    console.log(json);
    xhttp.send(json);
}

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
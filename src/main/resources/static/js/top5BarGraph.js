requirejs(["moment", "Chart"], function (moment, Chart) {
    var ctx = document.getElementById("top5BarGraph").getContext('2d');
    var top5BarGraph = new Chart(ctx, {
        type: 'horizontalBar',
        data: {
            labels: ["Mono", "Library", "Air Bar", "Entertainment", "Food on Four"],
            datasets: [{
                label: 'Total Sales - £',
                data: [12, 19, 3, 5, 2],
                backgroundColor: [
                    'rgb(255, 99, 132)',
                    'rgb(54, 162, 235)',
                    'rgb(255, 206, 86)',
                    'rgb(75, 192, 192)',
                    'rgb(153, 102, 255)'
                ],
                borderColor: [
                    'rgba(255,99,132,1)',
                    'rgba(54, 162, 235, 1)',
                    'rgba(255, 206, 86, 1)',
                    'rgba(75, 192, 192, 1)',
                    'rgba(153, 102, 255, 1)'
                ],
                borderWidth: 1
            }]
        },
        options: {
            scales: {
                xAxes: [{}],
                yAxes: [{
                    ticks: {
                        beginAtZero: true
                    }
                }]
            }
        }
    });

});
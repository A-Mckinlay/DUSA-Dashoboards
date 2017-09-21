requirejs(["moment", "Chart"], function (moment, Chart) {
    var ctx = document.getElementById("busiestTimes");
    var busiestTimes = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ["7am-9am", "10am-12pm", "1pm-3pm", "4pm-6pm", "7pm-9pm", "10pm-12pm", "1am - 3am"],
            datasets: [{
                label: '# of Transactions',
                data: [2, 23, 45, 2, 34, 4, 12],
                backgroundColor: [
                    'rgba(255, 99, 132, 0.2)',
                    'rgba(255, 99, 132, 0.2)',
                    'rgba(54, 162, 235, 0.2)',
                    'rgba(255, 206, 86, 0.2)',
                    'rgba(75, 192, 192, 0.2)',
                    'rgba(153, 102, 255, 0.2)',
                    'rgba(255, 99, 132, 0.2)'
                ],
                borderColor: [
                    'rgba(255,99,132,1)',
                    'rgba(255, 99, 132, 1)',
                    'rgba(54, 162, 235, 1)',
                    'rgba(255, 206, 86, 1)',
                    'rgba(75, 192, 192, 1)',
                    'rgba(153, 102, 255, 1)',
                    'rgba(255, 99, 132, 1)'
                ],
                borderWidth: 1
            }]
        },
        options: {
            scales: {
                yAxes: [{
                    ticks: {
                        beginAtZero: true
                    }
                }]
            }
        }
    });
});
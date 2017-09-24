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
                    'rgb(255, 99, 132)',
                    'rgb(255, 99, 132)',
                    'rgb(54, 162, 23)',
                    'rgb(255, 206, 86)',
                    'rgb(75, 192, 192)',
                    'rgb(153, 102, 255)',
                    'rgb(255, 99, 132)'
                ],
                borderColor: [
                    'rgb(255,99,132)',
                    'rgb(255, 99, 132)',
                    'rgb(54, 162, 235)',
                    'rgb(255, 206, 86)',
                    'rgb(75, 192, 192)',
                    'rgb(153, 102, 255)',
                    'rgb(255, 99, 132)'
                ],
                borderWidth: 1
            }]
        },
        options: {
            legend:{
                display: false
            },
            scales: {
                yAxes: [{
                    ticks: {
                        beginAtZero: true
                    },
                    scaleLabel:{
                        display: true,
                        labelString: "# of Transactions"
                    }
                }],
                xAxes: [{
                    ticks: {
                        autoSkip: false
                    }
                }]
            }
        }
    });
});
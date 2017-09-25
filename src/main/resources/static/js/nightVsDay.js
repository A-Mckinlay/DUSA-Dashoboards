requirejs(["moment", "Chart", "lodash", "dashohelper", "chroma", "distinct-colors"], function (moment, Chart, _, Helper, chroma, distinctColors) {

    var ctx = document.getElementById("nightVsDay");
    var nightVsDay = new Chart(ctx, {
        type: 'radar',
        data: {
            labels: ['12am', '1am', '2am', '3am', '4am', '5am', '6am', '7am','9am', '10am', '11am', '12pm','1pm', '2pm', '3pm', '4pm','5pm', '6pm', '7pm', '8pm', '9pm', '10pm','11pm'],
            datasets: [{
                label:"Mono",
                backgroundColor: "rgba(200,0,0,0.2)",
                data: [20, 10, 4, 2,5,12,4,23,16,13,16,19,3,14,2,5,4,2,8,10,20,24,20]
            }, {
                label: "Liar",
                backgroundColor: "rgba(0,0,200,0.2)",
                data: [2, 1, 24, 20, 25, 10, 30, 3, 6, 3, 6, 19, 13, 23, 20, 15, 14, 12, 18, 15, 24, 24, 2]
            }

        ]
        },

        options: {
            maintainAspectRatio: true,
            scale: {
                    display: true

            }
        }


    });
});
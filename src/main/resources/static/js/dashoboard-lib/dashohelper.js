// Dashoboards helper functions.
define(["moment"], function(moment) {

    const Helpers = {};

    Helpers.createDateRangeObj = function(latestDate, numberOfDays) {
        let endDate = new Date(latestDate);
        let originDate = new Date(latestDate);
        originDate.setDate(originDate.getDate() - numberOfDays); //TODO: Decide if 28 should be 30/31/something else
        return {
            start: originDate,
            end: endDate
        };
    };

    Helpers.get = function(url) {
        return new Promise(function (resolve, reject) {
            let xhttp = new XMLHttpRequest();
            xhttp.open("GET", url, true);
            xhttp.onload = function () {
                if (xhttp.status === 200) {
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
    };

    return Helpers;

}); // END define
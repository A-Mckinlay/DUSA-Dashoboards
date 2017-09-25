define(["moment", "lodash", "dashohelper"], function (moment, _, Helper) {

    class Dateranger {

        constructor() {
            this.dates = [];
            this.latest = new Date();
            this.onChangeHandlers = [];
            this.setupDone = false;

            this.start = moment();
            this.end = moment();

            // We need this because JS is shit about handling 'this'; see https://stackoverflow.com/a/4591483
            const thisObj = this;

            $(document).ready(function() {
                Helper.get("/api/meta/latestdate").then(function(raw) { thisObj.setup(raw); });
                $( "#dr-date-rangeslider" ).slider({
                    range: true,
                    min: 0,
                    max: 364,
                    step: 7,
                    values: [ 336, 364 ],
                    slide: _.debounce(function(evt, ui) { thisObj.onSliderChange(evt, ui); }, 100)
                });
                $( "#dr-custom-apply-btn" ).click(function() { thisObj.onCustomApply(); });
            });
        }

        setup(latestDateRaw) {
            this.latest = moment(latestDateRaw).endOf('day');
            this.dates = new Array(365);
            for (let i = 0; i < 365; i++) {
                let j = 364 - i;
                let newDay = this.latest.clone();
                newDay.subtract(j, 'days');
                this.dates[i] = newDay;
            }
            this.setupDone = true;
            this.applyRange(this.latest.clone().subtract(28, 'days'), this.latest.clone());
        }

        onSliderChange(_evt, ui) {
            if (!this.setupDone) {
                console.log("Dateranger setup not finished! Retrying onSliderChange in 250ms.");
                let thisObj = this;
                window.setTimeout(function() { thisObj.onSliderChange(_evt, ui); }, 250);
                return;
            }

            let min = this.dates[ui.values[0]];
            let max = this.dates[ui.values[1]];
            this.applyRange(min, max);
        }

        addOnChangeHandler(fn) {
            this.onChangeHandlers.push(fn);
        }

        onCustomApply() {
            const start = moment($("#dr-start-date-selector").val());
            const end = moment($("#dr-end-date-selector").val());
            const alert = $( "#dr-input-alert" );
            if (start.isValid() && end.isValid()) {
                if (start.isBefore(end)) {
                    alert.hide();
                    this.applyRange(start, end);
                } else {
                    alert.html("This range starts after the end date! Swap the values if this is really the range you want.");
                    alert.show();
                }
            } else {
                alert.html("One or both of your dates are invalid. Check your input and try again.");
                alert.show();
            }
        }

        applyRange(min, max) {
            if (!this.setupDone) {
                console.log("Dateranger setup not finished! Retrying applyRange in 250ms.");
                let thisObj = this;
                window.setTimeout(function() { thisObj.applyRange(min, max); }, 250);
                return;
            }

            let minIdx = _.findIndex(this.dates, function(d) { return min.isSame(d, "day"); });
            let maxIdx = _.findIndex(this.dates, function(d) { return max.isSame(d, "day"); });

            if (min.isBefore(_.first(this.dates))) minIdx = 0;
            if (min.isAfter(_.last(this.dates))) minIdx = _.size(this.dates) - 1;
            if (max.isBefore(_.first(this.dates))) maxIdx = 0;
            if (max.isAfter(_.last(this.dates))) maxIdx = _.size(this.dates) - 1;

            this.start = min.clone().endOf('day');
            this.end = max.clone().endOf('day');

            $( "#dr-date-rangeslider" ).slider("values", [minIdx, maxIdx]);
            $( "#dr-start-date").html(this.start.format("DD/MM/YYYY"));
            $( "#dr-end-date").html(this.end.format("DD/MM/YYYY"));

            const thisObj = this;

            _.forEach(this.onChangeHandlers, function(fn) { fn(thisObj.getStart(), thisObj.getEnd()); });
        }

        getStart() {
            return this.start.clone();
        }

        getEnd() {
            return this.end.clone();
        }
    }

    return function() {
        return new Dateranger();
    };

}); // END define
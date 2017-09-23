requirejs(["lodash", "d3", "chroma", "mustache", "dashohelper"], function (_, d3, chroma, Mustache, Helper) {

    const SPECTROGRAPH_TEMPLATE = "<div class='spectrograph-div' id='div-{{id}}'>" +
        "<label for='spectrogram-{{id}}'><b>{{outletname}}: </b></label>" +
        "<svg height='50px' id='spectrogram-{{id}}'></svg>" +
        "</div>";

    const scale = chroma.scale('Spectral').domain([1,0]);

    function rebuild() {
        // Clear the deck
        $('#spectrograph-host').empty();

        let promise = Helper.get("/api/meta/latestdate");
        promise.then(function (latestDate) {
            let dateRange = Helper.createDateRangeObj(latestDate, 28);
            const getParams = "start=" + dateRange.start.toISOString() + "&end=" + dateRange.end.toISOString();
            let url = "/api/summary/busybot/venues?" + getParams;
            url = encodeURI(url);
            return Helper.get(url);
        }).then(function (graphData) {
            data = JSON.parse(graphData);
            console.log(data);
            handleData(data);
        }).catch(function (error) {
            console.log(error);
        });
    }

    function renderLegend() {
        let svg = d3.select("#spectrogram-legend");

        let g = svg.append("defs")
            .append("linearGradient")
            .attr("id", "legend-gradient")
            .attr("spreadMethod", "pad");

        for (let i = 0; i <= 4; i++) {
            g.append("stop")
                .attr("offset", i/4.0 * 100 + "%")
                .attr("stop-color", scale(i/4.0).hex())
                .attr("stop-opacity", 1);
        }

        svg.append("rect")
            .attr("width", "100%")
            .attr("height", "100%")
            .style("fill", "url(#legend-gradient)")
    }

    function handleData(raw) {
        console.log(raw);
        _.forEach(raw, function (values, outlet) {
            let id = _.snakeCase(outlet);
            console.log(id);
            renderSpectrogramHTML(outlet, id);

            let max_value = 0;
            _.forEach(values, function (v, k) {
                if (v > max_value) max_value = v;
            });
            console.log("MAX: " + max_value);

            let selector = "#spectrogram-" + id;
            let svg = d3.select(selector);

            let defs = svg.append("defs");

            let gradient = defs
                .append("linearGradient")
                .attr("id", "gradient-" + id)
                .attr("spreadMethod", "pad");

            for (let i = 0; i < 24; i++) {
                let x = values[i]/max_value;
                let scalePoint = (i/24.0 * 100);
                console.log(i + " // " + x);
                let g = gradient
                    .append("stop")
                    .attr("offset", (i/24 * 100) + "%")
                    .attr("stop-color", scale(x).hex())
                    .attr("stop-opacity", 1);
                //console.log(g);
            }

            let group = svg.selectAll("text")
                .data(d3.range(24))
                .enter()
                .append("text")
                .attr("y", "90%")
                .attr("x", function(d) { return d/24 * 100 + "%"; })
                .attr("dy", ".2em")
                .attr("class", "dt-label")
                //.attr("transform", "rotate(270)")
                .text(function (d) {
                    return d + ":00"
                });

            svg.append("rect")
                .attr("width", "100%")
                .attr("height", "75%")
                .style("fill", "url(#gradient-" + id + ")");
        })
    }

    function renderSpectrogramHTML(outletname, id) {
        const html = Mustache.render(SPECTROGRAPH_TEMPLATE, {id: id, outletname: outletname});
        $('#spectrograph-host').append(html);
    }

    window.onload += renderLegend();
    window.onload += rebuild();

}); // END requirejs()
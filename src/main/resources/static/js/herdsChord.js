requirejs(["d3", "lodash", "color-hash", "dashohelper"], function (d3, _, ColorHash, Helper) {

    getAndDrawChord();

    function getAndDrawChord() {
        let promise = Helper.get("/api/meta/latestdate");
        promise.then(function (latestDate) {
            let dateRange = Helper.createDateRangeObj(latestDate, 28);
            const getParams = "start=" + dateRange.start.toISOString() + "&end=" + dateRange.end.toISOString();
            let url = "/api/herds/chords?" + getParams;
            url = encodeURI(url);
            return Helper.get(url);
        }).then(function(graphData){
            console.log(graphData);
            let datas = JSON.parse(graphData);
            let hasher = new ColorHash();
            datas.outlets = _.map(datas.outlets, function (outlet) {
                return {
                    name: outlet,
                    data: hasher.hex(outlet)
                }
            });
            drawChords(datas.matrix, datas.outlets);
        }).catch(function (error) {
            console.log(error);
        });
    }

    // Based heavily on https://bl.ocks.org/mbostock/4062006
    function drawChords(matrix, map) {
        let svg = d3.select("svg"),
            width = +svg.attr("width"),
            height = +svg.attr("height"),
            outerRadius = Math.min(width, height) * 0.4 - 40,
            innerRadius = outerRadius - 30;

        let chord = d3.chord()
            .padAngle(0.05)
            .sortGroups(d3.descending)
            .sortSubgroups(d3.descending);

        let arc = d3.arc()
            .innerRadius(innerRadius)
            .outerRadius(outerRadius);

        let ribbon = d3.ribbon()
            .radius(innerRadius);

        let color = d3.scaleOrdinal()
            .domain(d3.range(_.size(map)))
            .range(_.map(map, function (ent) {
                return ent.data;
            }));

        let g = svg.append("g")
            .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")")
            .datum(chord(matrix));

        let group = g.append("g")
            .attr("class", "groups")
            .selectAll("g")
            .data(function(chords) { return chords.groups; })
            .enter().append("g");

        group.append("path")
            .style("fill", function(d) { return color(d.index); })
            .style("stroke", function(d) { return d3.rgb(color(d.index)).darker(); })
            .attr("d", arc);

        let groupNames = group.selectAll(".group-name")
            .data(function(d) { return [halfPoint(d)]; })
            .enter().append("g")
            .attr("class", "group-name")
            .attr("transform", function(d) { return "rotate(" + (d.angle * 180 / Math.PI - 90) + ") translate(" + outerRadius + ",0)"; });

        groupNames.append("line")
            .attr("x2", 6);

        groupNames
            .append("text")
            .attr("x", 8)
            .attr("dy", ".35em")
            .attr("transform", function(d) { return d.angle > Math.PI ? "rotate(180) translate(-16)" : null; })
            .style("text-anchor", function(d) { return d.angle > Math.PI ? "end" : null; })
            .text(function(d) {
                //console.log(d);
                return map[d.index].name;
            });

        g.append("g")
            .attr("class", "ribbons")
            .selectAll("path")
            .data(function(chords) { return chords; })
            .enter().append("path")
            .attr("d", ribbon)
            .style("fill", function(d) { return color(d.target.index); })
            .style("stroke", function(d) { return d3.rgb(color(d.target.index)).darker(); });

        function halfPoint(d) {
            console.log(d);
            let k = (d.endAngle - d.startAngle) / 2;
            return {value: d.value, angle: k + d.startAngle, index: d.index};
        }
    }

});
requirejs(["d3", "lodash", "dashohelper", "chroma", "distinct-colors"],
    function (d3, _, Helper, Chroma, distinctColors) {

    let locked = false;
    let chordsData = null;
    let flowsData = null;

        function rebuild() {
            if (locked) return;
            locked = true;

            if (chordsData === null) {
                getChordData();
                return;
            }
            if (flowsData === null) {
                getFlowsData();
                return;
            }

            $("#herds-chords").empty();
            $("#herds-flows").empty();

            redrawFlows();
            redrawChords();
            locked = false;
        }

        function getChordData() {
            let promise = Helper.get("/api/meta/latestdate");
            return promise.then(function (latestDate) {
                let dateRange = Helper.createDateRangeObj(latestDate, 28);
                const getParams = "start=" + dateRange.start.toISOString() + "&end=" + dateRange.end.toISOString();
                let url = "/api/herds/chords?" + getParams;
                url = encodeURI(url);
                return Helper.get(url);
            }).then(function (graphData) {
                chordsData = processRaws(JSON.parse(graphData));
                locked = false;
                rebuild();
            }).catch(function (error) {
                console.log(error);
            });
        }

        function getFlowsData() {
            let promise = Helper.get("/api/meta/latestdate");
            return promise.then(function (latestDate) {
                let dateRange = Helper.createDateRangeObj(latestDate, 28);
                const getParams = "start=" + dateRange.start.toISOString() + "&end=" + dateRange.end.toISOString();
                let url = "/api/herds/flows?" + getParams;
                url = encodeURI(url);
                return Helper.get(url);
            }).then(function (graphData) {
                flowsData = processRaws(JSON.parse(graphData));
                locked = false;
                rebuild();
            }).catch(function (error) {
                console.log(error);
            });
        }

        function redrawChords() {
            render(chordsData, "#herds-chords", false);
        }

        function redrawFlows() {
            render(flowsData, "#herds-flows", true);
        }

        function processRaws(datas) {
            let step = 0;
            const steps = _.size(datas.outlets);
            let colors = distinctColors({
                count: steps,
                lightMin: 25,
                lightMax: 75
            });
            datas.outlets = _.map(datas.outlets, function (outlet) {
                let v = {
                    name: outlet,
                    data: colors[step].hex()
                };
                step += 1;
                return v;
            });
            return datas;
        }

        function render(datas, selector, renderTicks) {
            drawChords(datas.matrix, datas.outlets, selector, renderTicks);
        }

        function getWidth() {
            const w1 = $("#herds-chords-div").width() * 1.0;
            const w2 = $("#herds-flows-div").width() * 1.0;
            return Math.round(Math.max(w1, w2));
        }

        // Based heavily on https://bl.ocks.org/mbostock/4062006 and https://stackoverflow.com/a/42493333
        function drawChords(matrix, map, elementSel, renderTicks) {
            let svg = d3.select(elementSel),
                width = getWidth(),
                height = Math.round($(window).height()),
                outerRadius = Math.min(width, height) * 0.35 - 40,
                innerRadius = outerRadius - 30;

            $(elementSel).width(width + "px");
            $(elementSel).height(height + "px");

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
                .data(function (chords) {
                    return chords.groups;
                })
                .enter().append("g");

            group.append("path")
                .style("fill", function (d) {
                    return color(d.index);
                })
                .style("stroke", function (d) {
                    return d3.rgb(color(d.index)).darker();
                })
                .attr("d", arc)
                .on("mouseover", fade(.1))
                .on("mouseout", fade(1));

            //Add labels to each group
            group.append("text")
                .attr("dy", ".35em") // width
                .attr("class", "group-label")
                .attr("text-anchor", function (d) {
                    d.angle = (d.startAngle + d.endAngle) / 2;
                    return d.angle > Math.PI ? "end" : "inherit";
                })
                .attr("transform", function (d, i) { // angle
                    d.angle = (d.startAngle + d.endAngle) / 2;
                    d.name = map[i].value;
                    //console.log(d.angle);
                    return "rotate(" + (d.angle * 180 / Math.PI) + ")" +
                        "translate(0," + -1.1 * (outerRadius + 10) + ")" +
                        "rotate(270)" + (d.angle > Math.PI ? "rotate(180)" : "");
                }) //to spin when the angle between 135 to 225 degrees
                .text(function (d) {
                    return map[d.index].name;
                });

            if (renderTicks) {
                let groupTick = group.selectAll(".group-tick")
                    .data(function (d) {
                        return groupTicks(d, 10);
                    })
                    .enter().append("g")
                    .attr("class", "group-tick")
                    .attr("transform", function (d) {
                        return "rotate(" + (d.angle * 180 / Math.PI - 90) + ") translate(" + outerRadius + ",0)";
                    });

                groupTick.append("line")
                    .attr("x2", 6);

                groupTick
                    .filter(function (d) {
                        return d.value % 50 === 0;
                    })
                    .append("text")
                    .attr("x", 8)
                    .attr("dy", ".35em")
                    .attr("transform", function (d) {
                        return d.angle > Math.PI ? "rotate(180) translate(-16)" : null;
                    })
                    .style("text-anchor", function (d) {
                        return d.angle > Math.PI ? "end" : null;
                    })
                    .text(function (d) {
                        return d.value;
                    });

                // Returns an array of tick angles and values for a given group and step.
                function groupTicks(d, step) {
                    var k = (d.endAngle - d.startAngle) / d.value;
                    return d3.range(0, d.value, step).map(function (value) {
                        return {value: value, angle: value * k + d.startAngle};
                    });
                }
            }

            let ribbons = g.append("g")
                .attr("class", "ribbons")
                .selectAll("path")
                .data(function (chords) {
                    return chords;
                })
                .enter().append("path")
                .attr("d", ribbon)
                .style("fill", function (d) {
                    return color(d.target.index);
                })
                .style("stroke", function (d) {
                    return d3.rgb(color(d.target.index)).darker();
                });

            function fade(opacity) {
                return function (d, i) {
                    ribbons
                        .filter(function (d) {
                            return d.source.index !== i && d.target.index !== i;
                        })
                        .transition()
                        .style("opacity", opacity);
                };
            }
        }

        $(document).ready(rebuild);
        $(window).resize(rebuild);

    });
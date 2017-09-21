requirejs(["d3", "lodash", "color-hash"], function (d3, _, ColorHash) {

    getAndDrawChord();

    function createDateRangeObj(latestDate, numberOfDays) {
        let endDate = new Date(latestDate);
        let originDate = new Date(latestDate);
        originDate.setDate(originDate.getDate() - numberOfDays);
        let dateRange = {
            start: originDate,
            end: endDate
        };
        console.log(dateRange);
        return dateRange;
    }

    function getAndDrawChord() {
        function get(url) {
            return new Promise(function (resolve, reject) {
                let xhttp = new XMLHttpRequest();
                xhttp.open("GET", url, true);
                xhttp.onload = function () {
                    if (xhttp.status == 200) {
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
        }

        let promise = get("/api/meta/latestdate");
        promise.then(function (latestDate) {
            let dateRange = createDateRangeObj(latestDate, 28);
            const getParams = "start=" + dateRange.start.toISOString() + "&end=" + dateRange.end.toISOString()
            let url = "/api/herds/chords?" + getParams;
            url = encodeURI(url);
            return get(url);
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

    function drawChords(matrix, map) {
        var svg = d3.select("svg"),
            width = +svg.attr("width"),
            height = +svg.attr("height"),
            outerRadius = Math.min(width, height) * 0.4 - 40,
            innerRadius = outerRadius - 30;

        var chord = d3.chord()
            .padAngle(0.05)
            .sortGroups(d3.descending)
            .sortSubgroups(d3.descending);

        var arc = d3.arc()
            .innerRadius(innerRadius)
            .outerRadius(outerRadius);

        var ribbon = d3.ribbon()
            .radius(innerRadius);

        var color = d3.scaleOrdinal()
            .domain(d3.range(_.size(map)))
            .range(_.map(map, function (ent) {
                return ent.data;
            }));

        var g = svg.append("g")
            .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")")
            .datum(chord(matrix));

        var group = g.append("g")
            .attr("class", "groups")
            .selectAll("g")
            .data(function(chords) { return chords.groups; })
            .enter().append("g");

        group.append("path")
            .style("fill", function(d) { return color(d.index); })
            .style("stroke", function(d) { return d3.rgb(color(d.index)).darker(); })
            .attr("d", arc);

        var groupNames = group.selectAll(".group-name")
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

        /*var groupTick = group.selectAll(".group-tick")
            .data(function(d) { return groupTicks(d, 10); })
            .enter().append("g")
            .attr("class", "group-tick")
            .attr("transform", function(d) { return "rotate(" + (d.angle * 180 / Math.PI - 90) + ") translate(" + outerRadius + ",0)"; });

        groupTick.append("line")
            .attr("x2", 6);*/

        /*groupTick
            .filter(function(d) { return d.value % 50 === 0; })
            .append("text")
            .attr("x", 8)
            .attr("dy", ".35em")
            .attr("transform", function(d) { return d.angle > Math.PI ? "rotate(180) translate(-16)" : null; })
            .style("text-anchor", function(d) { return d.angle > Math.PI ? "end" : null; })
            .text(function(d) { return d.value; });*/

        g.append("g")
            .attr("class", "ribbons")
            .selectAll("path")
            .data(function(chords) { return chords; })
            .enter().append("path")
            .attr("d", ribbon)
            .style("fill", function(d) { return color(d.target.index); })
            .style("stroke", function(d) { return d3.rgb(color(d.target.index)).darker(); });

// Returns an array of tick angles and values for a given group and step.
        function groupTicks(d, step) {
            var k = (d.endAngle - d.startAngle) / d.value;
            return d3.range(0, d.value, step).map(function(value) {
                return {value: value, angle: value * k + d.startAngle};
            });
        }

        function halfPoint(d) {
            console.log(d);
            let k = (d.endAngle - d.startAngle) / 2;
            return {value: d.value, angle: k + d.startAngle, index: d.index};
        }
    }


    // From https://github.com/sghall/d3-chord-diagrams/blob/master/uber.html
    /*function drawChords (matrix, mmap) {
        var w = 980, h = 800, r1 = h / 2, r0 = r1 - 110;
        var chord = d3.chord(matrix)
            //.padding(.02)
            .sortSubgroups(d3.descending)
            .sortChords(d3.descending);
        var arc = d3.arc()
            .innerRadius(r0)
            .outerRadius(r0 + 20);
        var svg = d3.select("body").append("svg:svg")
            .attr("width", w)
            .attr("height", h)
            .append("svg:g")
            .attr("id", "circle")
            .attr("transform", "translate(" + w / 2 + "," + h / 2 + ")");
        svg.append("circle")
            .attr("r", r0 + 20);
        var rdr = chordRdr(matrix, mmap);
        var g = svg.selectAll("g.group")
            .data(chord.groups)
            //.enter()
            //.append("svg:g")
            .attr("class", "group")
            .on("mouseover", mouseover)
            .on("mouseout", function (d) { d3.select("#tooltip").style("visibility", "hidden") });
        g.append("svg:path")
            .style("stroke", "grey")
            .style("fill", function(d) { return rdr(d).gdata; })
            .attr("d", arc);
        g.append("svg:text")
            .each(function(d) { d.angle = (d.startAngle + d.endAngle) / 2; })
            .attr("dy", ".35em")
            .style("font-family", "helvetica, arial, sans-serif")
            .style("font-size", "9px")
            .attr("text-anchor", function(d) { return d.angle > Math.PI ? "end" : null; })
            .attr("transform", function(d) {
                return "rotate(" + (d.angle * 180 / Math.PI - 90) + ")"
                    + "translate(" + (r0 + 26) + ")"
                    + (d.angle > Math.PI ? "rotate(180)" : "");
            })
            .text(function(d) { return rdr(d).gname; });
        var chordPaths = svg.selectAll("path.chord")
            .data(chord.chords())
            .enter().append("svg:path")
            .attr("class", "chord")
            .style("stroke", "grey")
            .style("fill", function(d) { return _.where(mmap, {id: d.source.index })[0].data;; })
            .attr("d", d3.svg.chord().radius(r0))
            .on("mouseover", function (d) {
                d3.select("#tooltip")
                    .style("visibility", "visible")
                    .html(chordTip(rdr(d)))
                    .style("top", function () { return (d3.event.pageY - 100)+"px"})
                    .style("left", function () { return (d3.event.pageX - 100)+"px";})
            })
            .on("mouseout", function (d) { d3.select("#tooltip").style("visibility", "hidden") });
        function chordTip (d) {
            var p = d3.format(".1%"), q = d3.format(",.2r")
            return "Chord Info:<br/>"
                +  d.sname + " → " + d.tname
                + ": " + p(d.svalue) + "<br/>"
                + d.tname + " → " + d.sname
                + ": " + p(d.tvalue) + "<br/>";
        }
        function groupTip (d) {
            var p = d3.format(".1%"), q = d3.format(",.2r")
            return "Group Info:<br/>"
                + d.gname + " : " + p(d.gvalue) + "<br/>";
        }
        function mouseover(d, i) {
            d3.select("#tooltip")
                .style("visibility", "visible")
                .html(groupTip(rdr(d)))
                .style("top", function () { return (d3.event.pageY - 80)+"px"})
                .style("left", function () { return (d3.event.pageX - 130)+"px";})
            chordPaths.classed("fade", function(p) {
                return p.source.index != i
                    && p.target.index != i;
            });
        }
    }

    function chordMpr (data) {
        var mpr = {}, mmap = {}, n = 0,
            matrix = [], filter, accessor;

        mpr.setFilter = function (fun) {
            filter = fun;
            return this;
        },
            mpr.setAccessor = function (fun) {
                accessor = fun;
                return this;
            },
            mpr.getMatrix = function () {
                matrix = [];
                _.each(mmap, function (a) {
                    if (!matrix[a.id]) matrix[a.id] = [];
                    _.each(mmap, function (b) {
                        var recs = _.filter(data, function (row) {
                            return filter(row, a, b);
                        })
                        matrix[a.id][b.id] = accessor(recs, a, b);
                    });
                });
                return matrix;
            },
            mpr.getMap = function () {
                return mmap;
            },
            mpr.printMatrix = function () {
                _.each(matrix, function (elem) {
                    console.log(elem);
                })
            },
            mpr.addToMap = function (value, info) {
                if (!mmap[value]) {
                    mmap[value] = { name: value, id: n++, data: info }
                }
            },
            mpr.addValuesToMap = function (varName, info) {
                var values = _.uniq(_.pluck(data, varName));
                _.map(values, function (v) {
                    if (!mmap[v]) {
                        mmap[v] = { name: v, id: n++, data: info }
                    }
                });
                return this;
            }
        return mpr;
    }
//*******************************************************************
//  CHORD READER
//*******************************************************************
    function chordRdr (matrix, mmap) {
        return function (d) {
            var i,j,s,t,g,m = {};
            if (d.source) {
                i = d.source.index; j = d.target.index;
                s = _.where(mmap, {id: i });
                t = _.where(mmap, {id: j });
                m.sname = s[0].name;
                m.sdata = d.source.value;
                m.svalue = +d.source.value;
                m.stotal = _.reduce(matrix[i], function (k, n) { return k + n }, 0);
                m.tname = t[0].name;
                m.tdata = d.target.value;
                m.tvalue = +d.target.value;
                m.ttotal = _.reduce(matrix[j], function (k, n) { return k + n }, 0);
            } else {
                g = _.where(mmap, {id: d.index });
                m.gname = g[0].name;
                m.gdata = g[0].data;
                m.gvalue = d.value;
            }
            m.mtotal = _.reduce(matrix, function (m1, n1) {
                return m1 + _.reduce(n1, function (m2, n2) { return m2 + n2}, 0);
            }, 0);
            return m;
        }
    }*/

});
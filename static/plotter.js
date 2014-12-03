function Plotter() {
}

(function(){
    var margin = {top: 20, right: 20, bottom: 30, left: 40},
        width = Math.ceil(window.screen.width * 0.8) - margin.left - margin.right,
        height = Math.ceil(window.screen.width * 0.3) - margin.top - margin.bottom;

    var x = d3.scale.ordinal()
        .rangeRoundBands([0, width], 0.2);

    var y = d3.scale.linear()
        .range([height,0]);

    var xAxis = d3.svg.axis()
        .scale(x)
        .orient("bottom");

    var yAxis = d3.svg.axis()
        .scale(y)
        .orient("left");

    this.plot = function(selector, stats, histogram, metaData) {
        var domainValues = prepareDataset(histogram);
        d3.selectAll(selector+" svg").remove();
        var svg = createSVGContainer(selector);
        setXYScales(domainValues, stats);
        drawAxises(svg, metaData);
        plotValues(svg, domainValues);
        drawStats(svg, stats, metaData);
        return {"selector":selector, "data":stats};
    };

    this.graph = function(selector, graphData) {
        return new Grapher().plot(selector, graphData);
    };

    function prepareDataset(histogram) {
        return joinDataSets(histogram._1, histogram._2);
    }

    function joinDataSets(histogramX, histogramY) {
        var domainValues = [];
        _.each(histogramX, function(v,index) { if(index < histogramX.length -1){
            domainValues.push({"range":v + "-" + histogramX[index+1], "freq":histogramY[index]});
        }});
        return domainValues;
    }

    function createSVGContainer(selector) {
        return d3.select(selector).append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
    }

    function setXYScales(domainValues, stats) {
        x.domain(_.map(domainValues, function(d){return d.range;}));
        y.domain([0, d3.max(domainValues, function(d){return d.freq;})]);
    }

    function drawAxises(svg, metaData) {
        svg.append("g")
            .attr("class", "x axis")
            .attr("transform", "translate(0," + height + ")")
            .call(xAxis)
            .append("text")
            .attr("y", 19)
            .attr("dy", ".71em")
            .attr("x", Math.ceil(width * 0.8))
            .attr("class","axis-label")
            .text("count( $1 )".format(metaData.tableName));

        svg.append("g")
            .attr("class", "y axis")
            .call(yAxis)
            .append("text")
            .attr("transform", "rotate(-90)")
            .attr("y", 6)
            .attr("dy", ".71em")
            .attr("class","axis-label")
            .text("count( $1 )".format(metaData.columnName));
    }


    function plotValues(svg, domainValues) {
        svg.selectAll(".bar")
            .data(domainValues)
            .enter().append("rect")
            .attr("class", "bar")
            .attr("x", function(d) { return x(d.range); })
            .attr("width", x.rangeBand())
            .attr("y", function(d) { return y(d.freq); })
            .attr("height", function(d) { return Math.abs(height - y(d.freq)); });
    }

    function drawStats(svg, stats, metaData) {
        svg.append("text").attr("y", 20).attr("x", 200).text("$1 $2 with $3 to $4 $5, average $6 and std deviation of $7".format(stats.count, metaData.columnName, stats.min, stats.max, metaData.tableName, niceFormat(stats.mean), niceFormat(stats.stdev)));
    }

    function niceFormat(value) {
        return parseFloat(value).toFixed(2);
    }
}).call(Plotter.prototype);

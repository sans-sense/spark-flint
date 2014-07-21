function Plotter() {
}

(function(){
    var margin = {top: 20, right: 20, bottom: 30, left: 40},
        width = Math.ceil(window.screen.width * 0.8) - margin.left - margin.right,
        height = Math.ceil(window.screen.width * 0.3) - margin.top - margin.bottom;

    var x = d3.scale.ordinal()
        .rangeRoundBands([0, width], .2);

    var y = d3.scale.linear()
        .range([height,0]);

    var xAxis = d3.svg.axis()
        .scale(x)
        .orient("bottom");

    var yAxis = d3.svg.axis()
        .scale(y)
        .orient("left");

    this.plot = function(selector, stats, histogram) {
        var domainValues = prepareDataset(histogram)
        d3.selectAll(selector+" svg").remove();
        var svg = createSVGContainer(selector);
        setXYScales(domainValues, stats);
        drawAxises(svg)
        plotValues(svg, domainValues);
    }

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
        x.domain(_.map(domainValues, function(d){return d.range}));
        y.domain([stats.min, stats.max]);
    }

    function drawAxises(svg) {
        svg.append("g")
            .attr("class", "x axis")
            .attr("transform", "translate(0," + height + ")")
            .call(xAxis);

        svg.append("g")
            .attr("class", "y axis")
            .call(yAxis)
            .append("text")
            .attr("transform", "rotate(-90)")
            .attr("y", 6)
            .attr("dy", ".71em")
            .attr("class","axis-label")
            .text("Frequency");
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
}).call(Plotter.prototype)

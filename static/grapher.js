function Grapher() {
}

(function(){

    var force, svg, d3Data, loadingMsg, margin, width, height, selector;

    margin = {top: 20, right: 20, bottom: 30, left: 40};
    width = Math.ceil(window.screen.width * 0.8) - margin.left - margin.right;
    height = Math.ceil(window.screen.width * 0.3) - margin.top - margin.bottom;


    this.plot = function(elSelector, graphData) {
        var graphSelector = elSelector+" svg";
        d3.selectAll(graphSelector).remove();
        selector = elSelector;
        svg = createSVGContainer(elSelector);
        force = createLayout(width, height);
        d3Data = adaptToD3Format(graphData);
        loadingMsg = getLoadingMessage(svg);
        setTimeout(render, 10);
        return new Graph(graphSelector, force, d3Data);
    };


    function createLayout(width, height) {
        var force = d3.layout.force()
            .charge(-120)
            .linkDistance(30)
            .size([width, height]);
        return force;
    }

    function adaptToD3Format(graphData) {
        var nodeLookup = {};
        var nodes = [];
        var nameExtractor = /^\S*\/(\S*) (\S*) \(.*/;
        _.forEach(
            graphData.nodes, 
            function(item, index){ 
                var name = item._2;
                var splits = /^\S*\/(\S*) (\S*) \(.*/.exec(name);
                if (splits.length === 3) {
                    name = splits[1] + "." + splits[2];
                }
                nodes.push({"name": name, "fName":item._2});
                nodeLookup[item._1] = index; });
        var links = _.map(
            graphData.links, 
            function(item) { 
                return {"source":nodeLookup[item[0]], "target":nodeLookup[item[1]]};});
        return {"nodes": nodes, "links":links};
    }

    function getLoadingMessage(svg) {
        return svg.append("text")
            .attr("x", width / 2)
            .attr("y", height / 2)
            .attr("dy", ".35em")
            .style("text-anchor", "middle")
            .text("Creating Graph. A few moments please...");

    }

    function renderLinks() {
        var link = svg.append("g")
            .attr("class","line-container")
            .selectAll(".link")
            .data(d3Data.links)
            .enter().append("line")
            .attr("class", "link")
            .style("stroke-width", "1")
            .attr("x1", function(d) { return d.source.x; })
            .attr("y1", function(d) { return d.source.y; })
            .attr("x2", function(d) { return d.target.x; })
            .attr("y2", function(d) { return d.target.y; });

        var linkHeads = svg.append("g")
            .attr("class","dest-container")
            .selectAll(".arrow-head")
            .data(d3Data.links)
            .enter().append("circle")
            .attr("class","arrow-head")
            .attr("r",2)
            .attr("cx", function(d) { return d.target.x - 4; })
            .attr("cy", function(d) { return d.target.y - 4; })
            .attr("fill","blue");
    }

    function renderNodes() {
        var node = svg.append("g")
            .attr("class","node-container")
            .selectAll(".node")
            .data(d3Data.nodes)
            .enter().append("circle")
            .attr("class", "node")
            .attr("r", 4)
            .attr("cx", function(d) { return d.x; })
            .attr("cy", function(d) { return d.y; })
            .call(force.drag);

        node.append("title")
            .text(function(d) { return d.name; });

    }

    function setScaleAndClass(selector) {
        d3.select(selector).attr("class","graph-container");
        setPositions(margin.left,margin.top);
    }

    function addDragAndZoom() {
        var drag = d3.behavior.drag()
            .on("dragstart", dragstarted)
            .on("drag", dragmove);
        d3.select(selector+" svg").call(drag);
    }

    function dragmove(d) { 
        var evt = d3.event;
        var currSvg = d3.select(d3.event.sourceEvent.target).select("g.top-container");
        setPositions(d3.event.dx, d3.event.dy, 0, currSvg);
    }

    function dragstarted(d) {
        d3.event.sourceEvent.stopPropagation();
    }

    function render() {
        force.nodes(d3Data.nodes).links(d3Data.links).start();
        for (var i = 100; i > 0; --i) force.tick();
        force.stop();
        renderNodes();
        renderLinks();
        loadingMsg.remove();
        setScaleAndClass(selector);
        addDragAndZoom();
    }

    function createSVGContainer(selector) {
        return d3.select(selector).append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g").attr("class", "top-container");
    }

    function setPositions(x, y, zoom, currSvg) {
        var svgContainer = currSvg || svg;
        if (_.isArray(svgContainer) && (svgContainer.size() < 1)) {
            return;
        }
        var state = JSON.parse(svgContainer.attr("render-state") || '{"x":0,"y":0, "zoom":0.25}');
        state.x += x;
        state.y += y;
        state.zoom += zoom || 0;
        if (state.zoom < 0.05) {
            console.log("lowest value reached");
            state.zoom = 0.05;
        }
        svgContainer.attr("transform", "translate("+ state.x +"," + state.y + "),scale("+state.zoom+")");
        svgContainer.attr("render-state", JSON.stringify(state));
    }


    function Graph(elSelector, force, d3Data) {
        this.selector = elSelector;
        this.layout = force;
        this.data = d3Data;
    }

    (function(){
        this.zoom = function(zoomIn) {
            setPositions(0, 0, ((zoomIn)? 1:-1) * 0.05, d3.select(this.selector + " g.top-container"));
        };

        this.search = function(searchParam) {
        };
    }).call(Graph.prototype);

}).call(Grapher.prototype);

var prettyPrinters = {};

$(function(){
    var plotter = new Plotter();

    prettyPrinters.query = function(data, containerId) {
        var resultsStr = "";
        if (data.length > 0) {
            resultsStr +="<table class='table table-bordered table-results'>";
            $.each(data, function(){
				var trStr = "<tr>";
				$.each(this, function(k, v){
					trStr += "<td>"+v+"</td>";
				});
				resultsStr += trStr;
			}); //end of each block
            resultsStr += "</table>";
        } else {
            resultsStr = "<br>No rows returned for this query";
        }
        
        setCmdResult(containerId,resultsStr);
    };


    prettyPrinters.desc = function(data, containerId) {
       return  prettyPrinters.query.call(this, _.map(data, function(val){ return (val._1)?  [val._2, val._1] : [val];}), containerId);
    };

    prettyPrinters.histogram = function(data, containerId) {
        plotter.plotHistogram("#"+containerId, data.stats, data.histogram, data.metadata);
    };

    prettyPrinters.scala = function(data, containerId) {
        setCmdResult(containerId,data);
    };

    prettyPrinters.graph = function(data, containerId) {
        var selector, graph;
        selector = "#"+containerId;
        $(selector).append('<div class="search-graph-ops"><button type="button" class="btn btn-small zoom-in" title="Zoom-In"><span class="icon icon-zoom-in zoom-in" /></button> <button type="button" class="btn btn-small" title="Zoom-Out"><span class="icon icon-zoom-out" /></button> <button type="button" class="btn btn-small" title="Rectangle-select"><span class="icon icon-pencil" /></button> <input type="text" placeholder="Search Vertices"/></div>');
        graph = plotter.graph(selector, data);
        addSearch($(selector +" .search-graph-ops input"), graph);
        addZoom($(selector +" .search-graph-ops button:not(:last)"), graph);
        addStats($(selector +" .search-graph-ops"), graph)
    };

    prettyPrinters.error = function(data, containerId) {
        setCmdResult(containerId, data);
    }

    function setCmdResult(containerId, contents) {
        $("#"+containerId).html(contents);
    }


    function addSearch(elSelector, graph) {
        elSelector.keypress(function(event) {
            var keycode = (event.keyCode ? event.keyCode : event.which);
	        if(keycode == '13'){
		        graph.search(elSelector.val());
	        }
        });
    }

    function addZoom(elSelector, graph) {
        elSelector.click(function(event) {
            var zoomIn = $(event.target).hasClass("zoom-in");
            graph.zoom(zoomIn);
        });
    }

    function addStats(elSelector, graph) {
        var graphData = graph.data;
        $(elSelector).append("<div>vertices : "+graphData.nodes.length+", edges:"+graphData.links.length+"</div>");
    }

});

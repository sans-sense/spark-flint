// yes, I am in a time bubble and don't know any better libraries that jquery
$(function() {
    var shellInputSel = '#shellInput';
    var resultsSel = "#results";
    var serverUrl = "/plugins/command.json";
    var shellPrompt = ">> ";
    var localCommands = {
       "clear": function() {
           $(resultsSel).html("");
       },
       "help": function() {
           var helpStr = ["","help: shows this output", 
                          "clear: clears the console", 
                          "select: run sql commands",
                          "analyze &lt;tableName&gt; &lt;columnName&gt;: analyzes the dist and top 10 values in this column of this table",
                         "desc &lt;tableName&gt;: gets the column names and type for this table"].join("<br>")
           $(resultsSel).append(helpStr);
       }
    };
    var prettyPrinters = {};
    var commandStack = [];
    var currHistoryCursor = 0;
    var commandNumber = 0;
    var commandResults = [];
    var plotter = new Plotter();

    $(shellInputSel).keyup(function(event){
        var command = $(shellInputSel).val();
	    var keycode = (event.keyCode ? event.keyCode : event.which);
	    if(keycode == '13'){
            $(resultsSel).append("<br>" + shellPrompt + command);
            $(shellInputSel).hide();
            runCommand(command)
            currHistoryCursor = commandStack.length;
	    } else if (keycode == '40' && event.ctrlKey) {
            setInputCmdAs(getNextCommand());//down
        } else if (keycode == '38' && event.ctrlKey) {
            setInputCmdAs(getPrevCommand());//up
        }
    });

    function runCommand(command) {
        commandStack.push(command);
        if (localCommands[command]) {
            setInputCmdAs()
            localCommands[command].call();
        } else {
            runServerCommand(command);
        }
    }

    function runServerCommand(commandStr) {
		var resultsStr = "";
        var commandHolder = { id:commandNumber};
        var cmdParser = /(desc|analyze)\s+(.*)/;
        var parsedResults;
        var cmdType, cmdArgs;
        cmdType = "query";
        cmdArgs = commandStr;

        if ((parsedResults = cmdParser.exec(commandStr)) && (parsedResults.length > 2)) {
            cmdType = parsedResults[1];
            cmdArgs = parsedResults[2];
        }

        $.ajax({
            url: serverUrl,
            type: "POST",
            data : "payload="+JSON.stringify({"command":cmdType, "args": cmdArgs})
        }).done(function(data) {
			resultsStr = "<div class='cmdResultContainer' id='resultContainer"+commandNumber+"'></div>";
			$(resultsSel).append(resultsStr);
                
            var payload = data.success;
            if (payload == false) {
			    resultsStr += "<br>Could not execute query";
            } else {
                commandHolder.result = data;
                prettyPrinters[cmdType].call(this,data, "#resultContainer"+commandNumber);
            }
            commandResults.push(commandHolder);
            commandNumber++;
            setInputCmdAs();
        });
    }

    function getNextCommand() {
        if (currHistoryCursor < commandStack.length - 1) {
            currHistoryCursor++;
            return commandStack[currHistoryCursor];
        } else {
            return "";
        }
    }

    function getPrevCommand() {
        if (currHistoryCursor > 0) {
            currHistoryCursor--;
            return commandStack[currHistoryCursor];
        } else {
            return "";
        }
    }

    function setInputCmdAs(initialCommand) {
        var initialCommand = initialCommand || "";
        $(shellInputSel).val(initialCommand);
        $(shellInputSel).show();
        $(shellInputSel).focus();
    }

    prettyPrinters["query"] = function(data, parentSelector) {
        var resultsStr = "";
        if (data.length > 0) {
            resultsStr +="<table class='table table-bordered table-results'>";
            $.each(data, function(){
				var trStr = "<tr>"
				$.each(this, function(k, v){
					trStr += "<td>"+v+"</td>"
				});
				resultsStr += trStr
			}); //end of each block
            resultsStr += "</table>";
        } else {
            resultsStr = "<br>No rows returned for this query";
        }
        
        $(parentSelector).html(resultsStr);
    }

    prettyPrinters["desc"] = function(data, parentSelector) {
       return  prettyPrinters["query"].call(this, _.map(data, function(val){ return [val._1, val._2]}), parentSelector)
    }

    prettyPrinters["analyze"] = function(data, parentSelector) {
        plotter.plot(parentSelector, data.stats, data.histogram);
    }

    // support alias top10 = "select $2, count(*) as fieldCount from $1 group by $2 order by fieldCount desc limit 10 "
});

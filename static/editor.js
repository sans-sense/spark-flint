$(function() {
    var shellInputContainerSel = '#shellInputContainer';
    var shellInputSel = '#shellInput';
    var resultsSel = "#results";
    var serverUrl = "/workbench/command.json";
    var shellPrompt = ">> ";
    var localCommands = {
       "clear": function() {
           $(resultsSel).html("");
       },
       "help": function() {
           var helpStr = ["","help: shows this output", 
                          "clear: clears the console", 
                          "select: run sql commands",
                          "histogram &lt;tableName&gt;, &lt;columnName&gt;: Plots the histogram",
                         "desc [tableName]: if tableName is given gets the column names and type for this table or gets all available tables",
                         "aliases: lists all aliases, to use an alias type cmdName parameters e.g to run top10, use top10 commits,author",
                         "alias command=sql: aliases the sql with a command can be parameterized, run aliases for samples",
                         "graph dataset: plots a force directed graph (flowery thingy) for a dataset, the dataset should have been registered before via the spark-shell",
                         "scala command: run scala code on the driver"].join("<br>");
           $(resultsSel).append(helpStr);
       },
       "aliases": function() {
           var aliasListStr = "";
           _.each(aliases, function(value, key) {aliasListStr+= "<br> $1 : $2".format(key, value);});
           $(resultsSel).append(aliasListStr);
       },
       "alias":function(command) {
           var aliasRegex = /alias (.+)=(.+)/;
           var splits = aliasRegex.exec(command);
           addAlias(splits[1], splits[2]);
       }
    };
    var commandStack = [];
    var currHistoryCursor = 0;
    var commandNumber = 0;
    var commandResults = [];
    var plotter = new Plotter();
    var aliases = {};
    var cmdRegex = /\s*(\S+)\s+(.+)/;
    var cellMagician = new CellMagician();

    if (typeof String.prototype.format !== 'function') {
        String.prototype.format = function() {
            var formatted = this, i, vals;
            if ($.isArray(arguments[0])) {
                vals = arguments[0];
            } else {
                vals = arguments;
            }
            for (i = 0; i < vals.length; i++) {
                formatted = formatted.replace(new RegExp("\\$" + (i + 1),"g"), vals[i]);
            }
            return formatted;
        };
    }

    $(shellInputSel).keyup(function(event){
        var command = $(shellInputSel).val();
	    var keycode = (event.keyCode ? event.keyCode : event.which);
	    if(keycode == '13' && event.ctrlKey){
            $(resultsSel).append("<br>" + shellPrompt + command);
            $(shellInputContainerSel).hide();
            runCommand(command);
            currHistoryCursor = commandStack.length;
	    } else if (keycode == '40' && event.ctrlKey) {
            setInputCmdAs(getNextCommand());//down
        } else if (keycode == '38' && event.ctrlKey) {
            setInputCmdAs(getPrevCommand());//up
        }
    });

    function runCommand(command) {
        commandStack.push(command);
        if (command.indexOf("%%") === 0) {
            cellMagician.runCommand(command);
        } else {
            var cmdSplits = cmdRegex.exec(command);
            if (cmdSplits === null) {
                cmdSplits = ["",command];
            }
            if (localCommands[cmdSplits[1]]) {
                localCommands[cmdSplits[1]].call(this, command);
                $( "body" ).trigger("showInputCmd")
            } else {
                if (aliases[cmdSplits[1]]) {
                    command = aliases[cmdSplits[1]].format((cmdSplits[2]||"").split(","));
                }
                runServerCommand(command);
            }
        }
    }

    function runServerCommand(commandStr) {
		var resultsStr = "";
        var commandHolder = { id:commandNumber};
        var cmdParser = /^(desc|histogram|graph|scala)\s*([\s\S]*)/;
        var parsedResults;
        var cmdType, cmdArgs;
        cmdType = "query";
        cmdArgs = commandStr;
        var resultContainerId = "resultContainer"+commandNumber;

        if ((parsedResults = cmdParser.exec(commandStr)) && (parsedResults.length > 2)) {
            cmdType = parsedResults[1];
            cmdArgs = parsedResults[2];
        }
		resultsStr = "<div class='cmdResultContainer' id='"+resultContainerId+"'></div>";
		$(resultsSel).append(resultsStr);

        $.ajax({
            url: serverUrl,
            type: "POST",
            data : "payload="+JSON.stringify({"command":cmdType, "args": cmdArgs})
        }).done(function(data) {
            var payload = data.success;
            if (payload === false) {
			    $("#"+resultContainerId).html("<br>Could not execute query");
            } else {
                commandHolder.result = data;
                prettyPrinters[cmdType].call(this, data, resultContainerId);
            }
            commandResults.push(commandHolder);
            commandNumber++;
            $( "body" ).trigger("showInputCmd")
        }).error(function(data) {
-            prettyPrinters.error("<br>Could not execute query, check the syntax of the query, remove semi comlons if used at end of query", resultContainerId);
            $( "body" ).trigger("showInputCmd")
        });
    }

    $("body").on("showInputCmd", function() {
        setInputCmdAs();
    });

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

    function setInputCmdAs(command) {
        var initialCommand = command || "";
        $(shellInputSel).val(initialCommand);
        $(shellInputContainerSel).show();
        $(shellInputSel).trigger('autosize.resize');
        $(shellInputSel).focus();
    }

    function addAlias(commandName, commandValue) {
        aliases[commandName] = commandValue;
    }

    addAlias("top10","select $2, count(*) as fieldCount from $1 group by $2 order by fieldCount desc limit 10");

    addAlias("top20","select $2, count(*) as fieldCount from $1 group by $2 order by fieldCount desc limit 20");

    addAlias("count","select count(*) from $1");

    addAlias("few","select * from $1 limit 10");

    $(shellInputSel).autosize();
    $(shellInputSel).focus();

});

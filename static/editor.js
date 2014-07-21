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
                          "analyze &lt;tableName&gt; &lt;columnName&gt;: analyzes the dist and top 10 values in this column of the table"].join("<br>")
           $(resultsSel).append(helpStr);
       }
    };
    var commandStack = [];
    var currHistoryCursor = 0;
    var commandNumber = 0;
    var commandResults = [];

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

    function runServerCommand(commandName) {
		var resultsStr = "";
        var commandHolder = { id:commandNumber};
        $.ajax({
            url: serverUrl,
            type: "POST",
            data : "payload="+JSON.stringify({"command":"query", "args": commandName})
        }).done(function(data) {
			var tableStr = "<div class='cmdResultContainer' id='resultContainer"+commandNumber+"'><br><table class='table table-bordered' style='width:80%'>";
            var payload = data.success;
            if (payload == false) {
			    resultsStr += "<br>Could not execute query";
            } else {
                if (data.length > 0) {
                    $.each(data, function(){
				        var trStr = "<tr>"
				        $.each(this, function(k, v){
					        trStr += "<td>"+v+"</td>"
				        });
				        tableStr += trStr
			        });
			        resultsStr = tableStr +"</table></div>"
                    commandHolder.result = data;
                } else {
                    resultsStr = "<br>No rows returned for this query";
                }
            }
			$(resultsSel).append(resultsStr);
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
});

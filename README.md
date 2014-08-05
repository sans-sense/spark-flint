spark-flint
===========

Adds a simple editor to webUI for spark at http://localhost:4040/plugins. A hack for developers to get a better feel of data exploration. Is not for production, just local data exploration.

#### Starting it  

* :load flint.init  
Assuming that we launched spark-shell from this folder or use the relative path, use this command on the spark repl  

#### Sample  
For code-analysis load the code-analysis component using

:load components/code-analysis/codeAnalysis.init

Analyze the git log for finding top committers, code churn (most changed files), distribution of commit and churn. The commands to run for this.  
* Generate thg git log for some project with, git log --numstat > detailed.commit.log  
* Register the file as a Table in spark with, registerCommitsAsTable("./detailed.commit.log", "commits")  
* Query this table from the ui (http://localhost:4040/plugins) with,select author, count(*) as commitCount from commits group by author order by commitCount desc limit 10") or use the alias top10 as top10 commits,author  
* On UI view distribution and top values with "analyze commits,author"  

In short the commit log is now mapped as an table (SchemaRDD) which can be queried like any other table. The UI supports other commands like analyze tablename, fieldname.  For a complete list run help. You can of course create any other table in repl and run sql on it from the web UI.


### Misc  
If we add [asm-all-5.0.3.jar](http://repo1.maven.org/maven2/org/ow2/asm/asm-all/5.0.3/asm-all-5.0.3.jar) to classpath using add_jars, we can also view the source of spark classes from repl.

* :load components/shell-enhancements/shellEnhance.init
* SourceUtil.indexFolder("../../spark")
* SourceUtil.source(res6.head._2) where res6 is the output of
sqlContext.sql("select * from commits limit 1").queryExecution.analyzed.output.map {attr => (attr.name, attr.dataType)}

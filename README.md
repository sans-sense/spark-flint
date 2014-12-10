spark-flint
===========

Adds a simple editor to webUI for spark at http://localhost:4040/workbench. A hack for developers to get a better feel of data exploration. Is not for production, just local data exploration.

#### Starting it  
Launch spark-shell from this folder, use this command on the spark repl  
* :load flint.init  

#### Sample  
For code-analysis load the code-analysis component using

:load components/code-analysis/codeAnalysis.init

Analyze the git log for finding top committers, code churn (most changed files), distribution of commit and churn. The commands to run for this.  
* Generate thg git log for some project with, git log --numstat > detailed.commit.log  
* Register the file as a Table in spark with, registerCommitsAsTable("./detailed.commit.log", "commits")  
* Query this table from the ui (http://localhost:4040/workbench) with,select author, count(*) as commitCount from commits group by author order by commitCount desc limit 10" or use the alias top10 as top10 commits,author  
* On UI view distribution and top values with "histogram commits,author"  

In short the commit log is now mapped as an table (SchemaRDD) which can be queried like any other table. For a complete list run help. You can of course create any other table in repl and run sql on it from the web UI.


### Misc
For creating a sample graph of all methods and the ones they invoke in the spark jar or to look at the source add [asm-all-5.0.3.jar](http://repo1.maven.org/maven2/org/ow2/asm/asm-all/5.0.3/asm-all-5.0.3.jar) to classpath.

* :load components/code-analysis/graphAnalysis.init for creating graph
* :load components/shell-enhancements/shellEnhance.init
* SourceUtil.indexFolder("../../spark")
* SourceUtil.source(res6.head._2) where res6 is the output of
sqlContext.sql("select * from commits limit 1").queryExecution.analyzed.output.map {attr => (attr.name, attr.dataType)}


### Step Summary__
Summary of all steps  
git clone https://github.com/sans-sense/spark-flint.git  
cd spark-flint  
wget http://repo1.maven.org/maven2/org/ow2/asm/asm-all/5.0.3/asm-all-5.0.3.jar  
SPARK_MEM=2048m $SPARK_HOME/bin/spark-shell --jars ./asm-all-5.0.3.jar --total-executor-cores 4 --master local  
once spark-shell starts and it gives the scala> prompt  
:load flint.init  
:load components/code-analysis/graphAnalysis.init
This will run createGraph which exports a graph of all method invocations in spark lib as a graph, this does take a bit of time (1 minute or so)

SPARK_MEM=2048m ../../spark/bin/spark-shell --jars ~/.m2/repository/org/ow2/asm/asm-all/5.0.3/asm-all-5.0.3.jar --total-executor-cores 4 --master local

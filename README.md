=======
spark-flint
===========

Adds a simple editor to webUI for spark at http://localhost:4040/plugins. A hack for developers to get a better feel of data exploration. Is not for production, just local data exploration.

#### Starting it  

:load user.init  
Assuming that we launched spark-shell from this folder or use the relative path, use this command on the spark repl  

#### Sample  

Analyze the git log for finding top committers, code churn (most changed files), distribution of commit and churn. The commands to run for this.  
1. Generate thg git log for some project with, git log --numstat > detailed.commit.log  
2. Register the file as a Table in spark with, registerCommitsAsTable("./detailed.commit.log", "commits")  
3. Query this table from the ui (http://localhost:4040/plugins) with,select author, count(*) as commitCount from commits group by author order by commitCount desc limit 20")
4. On UI view distribution and top values with, analyze commits,author

In short the commit log is now mapped as an table (SchemaRDD) which can be queried like any other table. The UI supports other commands like analyze tablename, fieldname.  For a complete list run help. You can of course create any other table in repl and run sql on it from the web UI.


#### Handy things  
Some useful commands to read a json file, register it as table, print the schema, query some field, plot the histogram using JFreeChart  
val commits = sqlContext.jsonFile("./github/data.json")  
commits.registerAsTable("commits")  
commits.cache  
commits.printSchema()  
val dCommits = sqlContext.sql("select actor, count(*) as commitCount from commits group by actor").collect.map {row=> row(1).asInstanceOf[Long].toDouble}  
import org.apache.spark.rdd._  
val stater = new DoubleRDDFunctions(sc.parallelize(dCommits))  
val commitHistogram = stater.histogram(5)  
val distArray = commitHistogram._1.take(commitHistogram._1.length - 1).zipWithIndex.map{case(y,i)=> (((y + commitHistogram._1(i+1))/2).toInt,commitHistogram._2(i).toInt)}  
new Grapher("demo").redraw(distArray)  

<<<<<<< HEAD
### Shell Extension  
Adds a simple editor to webUI for spark at http://localhost:4040/plugins. A hack for developers to get a better feel of data exploration. Is not for production, just local data exploration.

#### Starting it  

:load user.init


#### Sample  

Analyze the git log for finding top committers, code churn (most changed files), distribution of commit and churn. The commands to run for this.  
1. git log --numstat > detailed.commit.log
2. registerCommitsAsTable("./detailed.commit.log", "commits")
3. On the ui (http://localhost:4040/plugins) run "select author, count(*) as commitCount from commits group by author order by commitCount desc limit 20")

In short the commit log is now mapped as an table (SchemaRDD) which can be queried like any other table. The UI supports other commands like analyze <tablename>, <fieldname>, for a complete list run help.


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

=======
spark-flint
===========

Small utility library for apache spark
>>>>>>> f1182fb826b60ef8644b659becceb95c9649dd23

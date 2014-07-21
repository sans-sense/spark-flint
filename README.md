var rawData = sc.textFile("./github/data.json")
def extractAuthor(line:String) = {JSON.parseFull(line).get.asInstanceOf[Map[String,Any]].get("actor").get.asInstanceOf[String]}
val data = rawData.map(line => try{extractAuthor(line)}catch{case e:Exception => "none"})
val authorVsCount = data.map(author=> (author,1)).reduceByKey(_ + _)
authorVsCount.sortBy(r => -1 * r._2).take(10) 
case class CommitHistory(name:String, commits: Int)
val chRdd = authorVsCount.map(p=> CommitHistory(p._1, p._2))
val sqlContext = new org.apache.spark.sql.SQLContext(sc)
import sqlContext.createSchemaRDD
chRdd.registerAsTable("commitHistory")
def runQuery(sql:String) = {sqlContext.sql(sql).collect.foreach(println)}
runQuery("select name, commits from commitHistory where commits > 10 order by commits desc")

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

Tutorial
1. Spark Shell
2. RDD collection
3. Distribute


Issues and Features
1. Reserved keywords in schemaRDD for example
case class CommitHistory(name:String, count: Int)
runQuery("select name, count from commitHistory")


Why Spark
1.Scala makes it so good, sortBy, map, reduceByKey. Functions as first class citizens. "_", tuples.
2. implicit


Questions
1. How do I see the table def


Handy Commands
def gis(s: String) = new GZIPInputStream(new BufferedInputStream(new FileInpuuthtStream(s)))
def contents = Source.fromInputStream(gis("github/data/2013-04-11-9.json.gz"))

local-cluster[1,1,512]

Anti Thoughts
"One example is that it is dependent on available memory; any large dataset that exceeds that will hit a huge performance wall." [http://www.informationweek.com/big-data/big-data-analytics/will-spark-google-dataflow-steal-hadoops-thunder/a/d-id/1278959?page_number=2]


ADD_JARS="/data/work/projects/DataEngineering/work/lib/jcommon-1.0.22.jar,/data/work/projects/DataEngineering/work/lib/jfreechart-1.0.18.jar"

java.util.Date is not handled


Git log handling
:load GitLogParser.scala
val sqlContext = new org.apache.spark.sql.SQLContext(sc)
import sqlContext.createSchemaRDD
val commitIterator = StatLogProcessor.iterate(new BufferedReader(new InputStreamReader(new FileInputStream("./spark.git.log"))))
val commitArray = commitIterator.toArray
val commitRDD = sc.parallelize(commitArray)
commitRDD.registerAsTable("commits")
commitRDD.cache
def runQuery(sql:String) = {sqlContext.sql(sql).collect.foreach(println)}
runQuery("select author, count(*) as commitCount from commits group by author order by commitCount desc limit 20")

"select count(*)  from commits where locs['RoutingTablePartition.scala'] is not null"
scala
import org.objectweb.asm._
import java.io._

Commit.getClass.getClassLoader.asInstanceOf[scala.tools.nsc.interpreter.IMain$TranslatingClassLoader]
res0.classBytes(Commit.getClass.getName)
val cReader = new ClassReader(new ByteArrayInputStream(res1))
:val cw = new ClassVisitor(Opcodes.ASM5){
            val mv = new MethodVisitor() {
                override def visitLineNumber(line: Int, start: Label) {
                System.out.println(line + " " +label);
                super.visitLineNumber(line, start);
                }
            }

            override def  visitAttribute(attr:Attribute) {
                System.out.println("Class Attribute: "+attr.`type`);
                super.visitAttribute(attr);
            }

            override def visitSource(source: String, debug: String) {
                     System.out.println(source + "  " + debug);
                     super.visitSource(source, debug)
            }

            override def visitMethod(access: Int, name:String, desc:String, signature:String, exceptions: Array[String]) : MethodVisitor {
             System.out.println(name);
             return mv
            }
}
val cw = new ClassVisitor(Opcodes.ASM5){
            val mv = new MethodVisitor(Opcodes.ASM5) {
                override def visitLineNumber(line: Int, start: Label) {
                System.out.println(line + " " +start);
                super.visitLineNumber(line, start);
                }
            }

            override def  visitAttribute(attr:Attribute) {
                System.out.println("Class Attribute: "+attr.`type`);
                super.visitAttribute(attr);
            }

            override def visitSource(source: String, debug: String) {
                     System.out.println(source + "  " + debug);
                     super.visitSource(source, debug)
            }

            override def visitMethod(access: Int, name:String, desc:String, signature:String, exceptions: Array[String]) : MethodVisitor = {
             System.out.println(name);
             return mv
            }
}

cReader.accept(cw, 0)
val fos = new FileOutputStream("/data/work/projects/DataEngineering/work/captured.tmp")
fos.write(res1)
fos.close



Ugly Hacks
./sql/catalyst/src/main/scala/org/apache/spark/sql/catalyst/SqlParser.scala

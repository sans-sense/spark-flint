import scala.util.parsing.combinator.RegexParsers
import java.text.SimpleDateFormat
import java.util.Date
import scala.util.parsing.input.Reader
import java.io.{InputStreamReader, BufferedReader, FileInputStream}

case class Commit( hash: String, author: String, timestamp: java.sql.Timestamp, message: String, locs: Map[ String, Int ] )

object StatLogProcessor {

  private object StatLogParser extends RegexParsers with Serializable{
    val eol = "\n" | "\\z".r
    override val skipWhitespace = false

    val partialHash = "[0-9a-f]{7}".r
    val fullHash = "[0-9a-f]{40}".r
    val header = "commit" ~ rep( whiteSpace ) ~> fullHash <~ eol
    val author = "Author:" ~ rep( whiteSpace ) ~> ".+".r <~ eol
    val merge = "Merge:" ~ rep( whiteSpace ) ~ partialHash ~ rep( whiteSpace ) ~ partialHash <~ eol
    val date = "Date:" ~ rep( whiteSpace ) ~> ".+".r <~ eol
    val commentLine = "    " ~> ".*".r <~ eol
    val comment = rep( commentLine ) <~ eol ^^ { case l: List[_] => l.mkString( "\n" ) }
    val metric = ( "[0-9]+".r | "-" ) <~ rep( whiteSpace )
    val metricLine = ( metric ~ metric ~ ".+".r ) <~ eol
    val metrics = rep( metricLine )

    //Mon Jun 6 01:15:59 2011 +0000
    val sdf = new SimpleDateFormat( "EEE MMM d HH:mm:ss yyyy Z" )
    val parsedDate = date ^^ { case d => new java.sql.Timestamp(sdf.parse( d ).getTime) }

    val summarizedMetrics = metrics ^^ {
      case l: List[_] =>
        val grouped = l.groupBy { case ( ( _ ~ _ ) ~ path ) => path.split( "/" ).last  }
        val counted = grouped mapValues { _.flatMap {
          case ( ( "-" ~ "-" ) ~ _ ) => None
          case ( ( "-" ~ del ) ~ _ ) => Some( -del.toInt )
          case ( ( add ~ "-" ) ~ _ ) => Some(  add.toInt )
          case ( ( add ~ del ) ~ _ ) => Some(  add.toInt - del.toInt )
        } }
        val summarized = counted mapValues { _.sum }
        summarized
    }

    val commit =
      header ~
      opt( merge ) ~
      author ~
      ( parsedDate <~ eol ) ~
      comment ~
      opt( summarizedMetrics <~ eol )

    val extractedCommit = commit ^^ {
        case ( ( ( ( ( _hash ~ _ ) ~ _author ) ~ _date ) ~ _comment ) ~ _metrics ) =>
          Commit( _hash, _author, _date, _comment, _metrics getOrElse Map.empty )
      }
  }

  def parse( in: Reader[ Char ] ): Option[( Commit, Reader[ Char ] )] = {
    val result = StatLogParser.parse( StatLogParser.extractedCommit, in )
    if ( result.successful ) Some( result.get, result.next ) else None
  }
  def iterate( in: java.io.Reader ): Iterable[ Commit ] = {
    val initsr = scala.util.parsing.input.StreamReader( in )
    val str = Stream.iterate( parse( initsr ) ) { pr => parse( pr.get._2 ) }
    str.takeWhile { _.isDefined }.map { _.get._1 }
  }

  class IdMap {
    private var m: Map[ String, Int ] = Map.empty
    def apply( key: String ) =
      m.getOrElse( key, { val id = m.size + 1; m += key -> id; id } )
  }

  def main( args: Array[ String ] ) {
    val fis = new FileInputStream( "/Users/tomer/dev/git-analytics/log.stats" )
    val br = new BufferedReader( new InputStreamReader( fis ) )

    var authors = new IdMap
    var exts = Seq( "java", "scala" )

    val sdf = new SimpleDateFormat( "yyyy-MM-dd" )
    iterate( br ) foreach { commit =>
      val aid = authors( commit.author )
      val extColumns = exts.map { ext => commit.locs.getOrElse( ext, 0 ) }.mkString( "\t" )
      val time = sdf.format( commit.timestamp )
      println( s"${commit.hash}\t$aid\t$time\t$extColumns" )
    }
  }
}
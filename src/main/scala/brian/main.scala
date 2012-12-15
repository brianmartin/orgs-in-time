package brian

import java.sql.Timestamp
import java.io.PrintWriter
import java.io.File
import com.codahale.logula.Logging
import java.sql.ResultSet
import org.apache.log4j.Level
import java.io.File

object RunWorker extends Logging {

  var r = new util.Random

  def main(args: Array[String]): Unit = {

    LoggingConfig.configure(args(0))

    log.debug("Running on timestamp: " + args(0))

    val startTime = args(0).toLong
    val endTime = {
      if (args.size > 1)
        args(1).toLong
      else
        startTime + (15 * 60 * 1000) // 0-15m
    }

    log.info("" + SQL.query("describe documents_CURRENT;", (r: ResultSet) => r.getString("Field")))
    val docs = SQL.query("""
          select * from documents_CURRENT
            where lang_code='en'
              and TYPE='MAINSTREAM_NEWS'
              and document_date >= '""" + new Timestamp(startTime).toString() + """'
              and document_date <  '""" + new Timestamp(endTime).toString()   + """'
        ;""",
        (r: ResultSet) => 
          new Document(
              id = r.getString("document_id"),
              title = r.getString("title"),
              time = r.getTimestamp("document_date"),
              content = r.getString("content")
            )
        )

    for ((doc, i) <- docs.zipWithIndex) {
      if (i % 100 == 0)
        log.debug("" + doc.time)
      DetectOrganizationsPipeline(doc)
      val (orgs, orgCombos) = doc.allOrgsAndCombinationsOfOrgs()
      for ((org1, org2) <- orgCombos)
        ComboCount(org1, org2)
      for (org <- orgs)
        IndividualCount(org)
    }

    { val pw = new PrintWriter(new File("out/" + startTime + "-0-15-combo-total.txt")); pw.println(ComboCount.numProcessed); pw.close() }
    { val pw = new PrintWriter(new File("out/" + startTime + "-0-15-individual-total.txt")); pw.println(IndividualCount.numProcessed); pw.close() }
    ComboCount.writeToFile(new File("out/" + startTime + "-0-15-combo.cms"))
    IndividualCount.writeToFile(new File("out/" + startTime + "-0-15-individual.cms"))
  }

}

object GenerateTimestamps {

  def main(args: Array[String]): Unit = {
    val pw  = new PrintWriter(new File("timestamps.txt"))
    val pwm = new PrintWriter(new File("timestamps_millis.txt"))

    var t = new Timestamp(112,11,10, 23,0,0,0) // 112 corresponds to 2012
    var i = 24 * 30 * 12 // a year or so worth of hours
    while (i > 0) {
      pwm.println(t.getTime())
      pw.println(t.toString())
      t = new Timestamp(t.getTime() - 3600000)
      i -= 1
    }
    pw.flush()
    pw.close()
    pwm.flush()
    pwm.close()
  }

}

object AggregateBy {

  var allInFiles = Seq.empty[String]

  def setAllInFiles(inPath: String): Unit =
    new File(inPath).listFiles.map(f => f.getName.take(13)) // get only the timestamp portion

  def main(args: Array[String]): Unit = {

    val timeGranularity = args(0)
    val inPath = args(1)
    val outPath = args(2)

    setAllInFiles(inPath)

    timeGranularity match {
      case "day"   => filesSeqsByDay(inPath, outPath)
      //case "week"  => filesSeqsByWeek(args(1))
      //case "month" => filesSeqsByMonth(args(1))
    }

  }


}

object RunServer

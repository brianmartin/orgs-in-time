package brian

import java.sql.Timestamp
import com.codahale.logula.Logging
import java.sql.ResultSet
import org.apache.log4j.Level
import java.io.File

object Run extends Logging {

  var r = new util.Random

  def main(args: Array[String]): Unit = {

    LoggingConfig.configure()

    val startTime = args(0).toLong
    val endTime = {
      if (args.size > 1)
        args(1).toLong
      else
        startTime + (60 * 60 * 1000) // an hour
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

    log.debug("#docs : " + docs.size)
    log.debug("doc: " + DetectOrganizationsPipeline(docs.head))

    for ((doc, i) <- docs.zipWithIndex) {
      log.debug("" + doc.time)
      DetectOrganizationsPipeline(doc)
      for ((org1, org2) <- doc.allCombinationsOfOrganizations()) {
        Count(org1, org2)
      }
    }

    log.debug("US and White House freq: " + Count.frequency("US", "White House"))

    Count.writeToFile(new File("cms.obj"))

  }

}

object GenerateTimestamps {

  import java.io.PrintWriter
  import java.io.File

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

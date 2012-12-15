package brian

import com.codahale.logula.Logging
import java.sql.ResultSet
import org.apache.log4j.Level
import java.io.File

object Run extends Logging {

  var r = new util.Random

  def main(args: Array[String]): Unit = {

    LoggingConfig.configure()

    log.info("" + SQL.query("describe documents_CURRENT;", (r: ResultSet) => r.getString("Field")))
    val docs = SQL.query("""
          select * from documents_CURRENT
            where lang_code='en'
              and TYPE='MAINSTREAM_NEWS'
            limit 3000
        ;""",
        (r: ResultSet) => 
          new Document(
              id = r.getString("document_id"),
              title = r.getString("title"),
              content = r.getString("content")
            )
        )

    log.debug("doc: " + DetectOrganizationsPipeline(docs.head))

    for ((doc, i) <- docs.zipWithIndex) {
      DetectOrganizationsPipeline(doc)
      for ((org1, org2) <- doc.allCombinationsOfOrganizations()) {
        if (r.nextFloat < 0.01)
          log.debug("" + org1 + " | " + org2)
        Count(org1, org2)
      }
    }

    log.debug("US and White House freq: " + Count.frequency("US", "White House"))

    Count.writeToFile(new File("cms.obj"))

  }

}

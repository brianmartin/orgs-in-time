package brian

import com.codahale.logula.Logging
import java.sql.ResultSet
import org.apache.log4j.Level

object Run extends Logging {

  def main(args: Array[String]): Unit = {

    LoggingConfig.configure()

    log.info("" + SQL.query("describe documents_CURRENT;", (r: ResultSet) => r.getString("Field")))
    val docs = SQL.query("""
          select * from documents_CURRENT
            where lang_code='en'
              and TYPE='MAINSTREAM_NEWS'
            limit 1
        ;""",
        (r: ResultSet) => 
          new Document(
              id = r.getString("document_id"),
              title = r.getString("title"),
              content = r.getString("content")
            )
        )

    log.debug("doc: " + DetectOrganizationsPipeline(docs.head))
    log.debug("doc: " + docs.head)

  }

}

package brian

import com.codahale.logula.Logging
import java.sql.ResultSet
import org.apache.log4j.Level

object Run extends Logging {

  def main(args: Array[String]): Unit = {

    LoggingConfig.configure()

    log.info("" + SQL.query("describe documents_CURRENT;", (r: ResultSet) => r.getString("Field")))
    log.debug("" + SQL.query("describe documents_CURRENT;", (r: ResultSet) => r.getString("Field")))

  }

}

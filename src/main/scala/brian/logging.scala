package brian

import com.codahale.logula.Logging
import org.apache.log4j.Level

object LoggingConfig {

  def configure(timestamp: String = "default") = {
    Logging.configure { log =>
      log.registerWithJMX = true
      log.level = Level.DEBUG
      log.console.enabled = true
      log.console.threshold = Level.DEBUG

      log.file.enabled = true
      log.file.filename = "/home/martin/canvas/bd/log/0-15/" + timestamp + ".log"
      log.file.maxSize = 10 * 1024 // KB
      //log.file.retainedFiles = 5 // keep five old logs around
    }
  }

}

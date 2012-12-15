package brian

import org.squeryl.Schema
import org.squeryl.SessionFactory
import org.squeryl.adapters.H2Adapter
import org.squeryl.adapters.MySQLAdapter
import org.squeryl.Session
import org.squeryl.PrimitiveTypeMode._
import java.sql.ResultSet
import collection.mutable.ArrayBuffer

class SQL {

  val host = "localhost"
  val port = 3306

  // set the session factory and create the table
  def init() = {
    Class.forName("com.mysql.jdbc.Driver")
    SessionFactory.concreteFactory = Some(() => Session.create(
      java.sql.DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/sept3", "root", ""),
      new MySQLAdapter)
    )
  }
  init()

  def query[A](sql: String, extractResult: (ResultSet) => A): Seq[A] = {
    val session: Session = (SessionFactory.concreteFactory.getOrElse { throw new Error("Before querying must first init a PersistSQL instance.") }).apply()
    val conn = session.connection
    val stmt = conn.createStatement()
    val rs   = stmt.executeQuery(sql)
    val res = new ArrayBuffer[A]
    while (rs.next()) {
      res += extractResult(rs)
    }
    rs.close()
    stmt.close()
    conn.close()
    res
  }

}

object SQL extends SQL

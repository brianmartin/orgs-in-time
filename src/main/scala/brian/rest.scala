package brian

import java.net.InetSocketAddress
import java.util.{NoSuchElementException => NoSuchElement}
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.jboss.netty.util.CharsetUtil.UTF_8
import org.jboss.netty.handler.codec.http.HttpMethod
import org.jboss.netty.handler.codec.http.HttpMethod.GET
import org.jboss.netty.handler.codec.http.HttpMethod.POST
import org.jboss.netty.handler.codec.http.HttpMethod.PUT
import com.twitter.util.Future
import com.twitter.finagle.http.{Http, RichHttp, Request, Response}
import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.Version.Http11
import com.twitter.finagle.http.path._
import com.twitter.finagle.http.Method
import com.twitter.finagle.http.ParamMap
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.finagle.builder.{Server, ServerBuilder}
import com.codahale.logula.Logging
import com.codahale.jerkson.Json

import java.sql.Timestamp
import com.twitter.algebird.CMS

object QueryServer extends Logging {

  def respondWithStatic(path: String): Response = {
    val response = Response()
    response.content = copiedBuffer(io.Source.fromFile("static/" + path).getLines.mkString("\n"), UTF_8)
    response
  }

  def constructJSONResponse(json: String): Response = {
    val response = Response()
    response.setContentTypeJson
    response.content = copiedBuffer(json, UTF_8)
    response.addHeader("Access-Control-Allow-Origin", "*")
    response
  }

  def constructJSONErrorResponse(json: String): Response = {
    val response = Response(Http11, InternalServerError)
    response.mediaType = "text/plain"
    response.content = copiedBuffer(json, UTF_8)
    response.addHeader("Access-Control-Allow-Origin", "*")
    response
  }

  class Respond extends Service[Request, Response] with Logging {
    def apply(request: Request) = {
      try {
        log.debug("received request: " + request + " path: " + request.path + " method: " + request.method + " params: " + request.params)
        routes(request.method -> Path(request.path))(request)
      } catch {
        case e: MatchError => Future value Response(Http11, NotFound)
        case e: NoSuchElement => Future value Response(Http11, NotFound)
        case e: Exception => Future.value {
          val message = Option(e.getMessage) getOrElse "Something went wrong."
          log.error("\nMessage: %s\nStack trace:\n%s".format(message, e.getStackTraceString))
          log.error("" + e)
          constructJSONErrorResponse(message)
        }
      }
    }
  }

  def serveQuery(query: (String) => String): (Request) => Future[Response] =
    (req: Request) => { Future value { constructJSONResponse(query(req.contentString)) }}

  def serveStatic(path: String): (Request) => Future[Response] =
    (req: Request) => { Future value { respondWithStatic(path) }}


  var individualCounts = null.asInstanceOf[Map[Timestamp, CMS]]
  var comboCounts = null.asInstanceOf[Map[Timestamp, CMS]]

  def frequencies(org: String): Seq[Seq[String]] = {
    val m = comboCounts.toSeq.sortBy(_._1.getTime()).map { case (time, cms) => (time.toString().take(10), cms.frequency(Counter.hashToLong(org)).estimate.toString())}
    Seq(m.map(_._1).toSeq, m.map(_._2).toSeq)
  }

  def frequencies(org1: String, org2: String): Seq[Seq[String]] = {
    val m = comboCounts.toSeq.sortBy(_._1.getTime()).map { case (time, cms) => (time.toString().take(10), cms.frequency(Counter.hashToLong(org1, org2)).estimate.toString())}
    Seq(m.map(_._1).toSeq, m.map(_._2).toSeq)
  }

  object s { def unapply(str: String): Option[String] = { if (str.nonEmpty) Some(str) else None } }

  val routes: PartialFunction[(HttpMethod, Path), (Request) => Future[Response]] = {
    case GET -> Root /"query"/s(org)           => serveQuery((_) => Json.generate(frequencies(org)))
    case GET -> Root /"query"/s(org1)/s(org2)  => serveQuery((_) => Json.generate(frequencies(org1, org2)))
    case GET -> Root / s(staticPath)           => serveStatic(staticPath)
    case GET -> Root / "js"  / s(staticPath)   => serveStatic("js/" + staticPath)
    case GET -> Root / "css" / s(staticPath)   => serveStatic("css/" + staticPath)
    case GET -> Root / "ico" / s(staticPath)   => serveStatic("ico/" + staticPath)
  }

  def main(args: Array[String]) {

    LoggingConfig.configure(logToFile = false)

    val sketchDir = args(0)
    val port = 8888

    log.info("Loading individual counts...")
    individualCounts = LoadSketchesAggregatedByDay(sketchDir, postFix = "-0-15-individual.cms", limit = 300, drop = 15)
    log.info("Loading combo counts")
    comboCounts      = LoadSketchesAggregatedByDay(sketchDir, postFix = "-0-15-combo.cms", limit = 300, drop = 15)

    val service = new Respond
    val server  = ServerBuilder()
      .codec(RichHttp[Request](Http()))
      .bindTo(new InetSocketAddress(port))
      .name("QueryServer")
      .build(service)

    this.log.info("Server started on port: %s" format port)
  }

}

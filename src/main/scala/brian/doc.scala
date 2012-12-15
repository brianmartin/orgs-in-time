package brian

import opennlp.tools.util.Span
import collection.mutable.StringBuilder
import collection.mutable.ArrayBuffer
import com.codahale.logula.Logging
import java.sql.Timestamp

class Document(
  val id: String,
  val title: String,
  val time: Timestamp,
  val content: String
) extends Logging {

  var sentences: Array[Array[String]] = null
  var organizations: Array[Array[Span]] = null

  def allCombinationsOfOrganizations(): Seq[Tuple2[String, String]] = {
    val orgStrings = new ArrayBuffer[String]
    for ((orgs, sentenceId) <- organizations.zipWithIndex if orgs.size > 0) {
      for ((org, i) <- orgs.zipWithIndex)
        orgStrings.append(sentences(sentenceId).drop(org.getStart()).take(org.getEnd() - org.getStart()).mkString(" "))
    }

    orgStrings.combinations(2).filter(orgs => !orgs.head.equals(orgs.last)).map(orgs => orgs.head -> orgs.last).toSeq
  }

  override def toString(): String = {
    val sb = new StringBuilder

    sb.append("\n")

    sb.append("\n\nSentences:\n")
    sb.append("=================\n\n")
    for ((sent,i) <- sentences.zipWithIndex)
      sb.append("sent " + i + ": " + sent.mkString(" | ") + "\n")

    sb.append("\n\nOrganizations:\n")
    sb.append("=================\n\n")
    for ((orgs, sentenceId) <- organizations.zipWithIndex if orgs.size > 0) {
      for ((org, i) <- orgs.zipWithIndex)
        sb.append("orgs of sid: " + sentenceId + " : " + i + " : " + sentences(sentenceId).drop(org.getStart()).take(org.getEnd() - org.getStart()).mkString(" | ") + " \n")
    }

    sb.toString
  }

}


package brian

import opennlp.tools.util.Span
import collection.mutable.StringBuilder

class Document(
  val id: String,
  val title: String,
  val content: String
) {

  var sentences: Array[Array[String]] = null
  var organizations: Array[Array[Span]] = null

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


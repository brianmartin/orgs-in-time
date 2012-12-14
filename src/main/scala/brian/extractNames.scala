package brian

import com.codahale.logula.Logging
import opennlp.tools.util.Span

object DetectSentences extends Logging {

  import opennlp.tools.sentdetect._

  private var sentenceDetector: SentenceDetector = null

  def init(): Unit = {
    val modelIn = getClass().getResourceAsStream("/en-sent.bin")
    val sentenceModel = new SentenceModel(modelIn)
    modelIn.close()
    sentenceDetector = new SentenceDetectorME(sentenceModel)
  }
  init()

  def apply(str: String): Array[String] = {
    sentenceDetector.sentDetect(str)
  }

}

object DetectTokens extends Logging {

  import opennlp.tools.tokenize._

  private var tokenizer: Tokenizer = null

  def init(): Unit = {
    val modelIn = getClass().getResourceAsStream("/en-token.bin")
    val tokenModel = new TokenizerModel(modelIn)
    modelIn.close()
    tokenizer = new TokenizerME(tokenModel)
  }
  init()

  def apply(sentence: String): Array[String] = {
    tokenizer.tokenize(sentence)
  }
}

object DetectOrganizations extends Logging {

  import opennlp.tools.namefind._

  private var nameFinder: NameFinderME = null

  def init(): Unit = {
    val modelIn = getClass().getResourceAsStream("/en-ner-organization.bin")
    val orgModel = new TokenNameFinderModel(modelIn)
    modelIn.close()
    nameFinder = new NameFinderME(orgModel)
  }
  init()

  // TODO: what's the result type here?
  def apply(tokens: Array[String]): Array[Span] = {
    nameFinder.find(tokens)
  }

}

object DetectOrganizationsPipeline extends Logging {

  def apply(doc: Document): Document = {
    doc.sentences = DetectSentences(doc.content).map(s => DetectTokens(s))
    doc.organizations = doc.sentences.map(s => DetectOrganizations(s))
    doc
  }

}

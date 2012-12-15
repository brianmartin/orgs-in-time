package brian

import com.twitter.algebird.CMS
import com.twitter.algebird.CMSInstance
import collection.mutable.ArrayBuffer
import util.MurmurHash
import com.codahale.logula.Logging

import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.io.FileOutputStream
import java.io.FileInputStream
import java.io.ObjectOutputStream
import java.io.ObjectInputStream

class Counter extends Logging {

  val DELTA = 1E-10
  val EPS = 0.001
  val SEED = 1
  var cms: CMS = CMSInstance(CMS.monoid(EPS, DELTA, SEED).hashes, EPS, DELTA)

  private def hashToLong(x: String, y: String): Long = MurmurHash.arrayHash(Array(x, y)).toLong
  private def hashToLong(x: String): Long = MurmurHash.stringHash(x).toLong

  var numProcessed = 0L

  def apply(org1: String, org2: String): Unit = {
    numProcessed += 1
    cms = cms.asInstanceOf[CMSInstance] + hashToLong(org1, org2)
  }

  def apply(org: String): Unit = {
    numProcessed += 1
    cms = cms.asInstanceOf[CMSInstance] + hashToLong(org)
  }


  def frequency(org1: String, org2: String): Long =
    cms.frequency(hashToLong(org1, org2)).estimate

  def writeToFile(out: File): Unit = {
    val oos = new ObjectOutputStream(new FileOutputStream(out))
    oos.writeObject(cms)
    oos.close()
  }

  def readFromFile(in: File): Unit = {
    val ois = new ObjectInputStream(new FileInputStream(in))
    cms = ois.readObject().asInstanceOf[CMS]
    ois.close()
  }

//  def testCMSketch(): Unit = {
//    val data = Seq(
//        "A" -> "B",
//        "A" -> "B",
//        "B" -> "C",
//        "C" -> "A"
//      )
//
//    for ((x, y) <- data)
//      apply(x, y)
//
//    log.debug("A B freq: " + frequency("A", "B"))
//
//    log.debug("Serializing...")
//    val f = new File("cms.tmp")
//    writeToFile(f)
//    cms = null
//    log.debug("Deserializing...")
//    readFromFile(f)
//
//    val cms2 = Counter.mergeAllSerialized(Seq(f, f, f, f))
//    log.debug("A B freq: " + frequency("A", "B"))
//    log.debug("sketch total: " + cms.totalCount)
//    log.debug("sketch 2 total: " + cms2.totalCount)
//  }
//
//  def main(args: Array[String]): Unit = {
//    LoggingConfig.configure(logToFile = false)
//    testCMSketch()
//  }

}

object Counter {

  def mergeSerialized(cms1: CMS, in2: File): CMS = {
    val ois2 = new ObjectInputStream(new FileInputStream(in2))
    val cms2 = ois2.readObject().asInstanceOf[CMS]
    ois2.close()
    cms1.asInstanceOf[CMSInstance] ++ cms2
  }

  def mergeAllSerialized(ins: Seq[File]): CMS = {
    val ois = new ObjectInputStream(new FileInputStream(ins.head))
    val firstCms = ois.readObject().asInstanceOf[CMS]
    ois.close()
    ins.drop(1).foldLeft(firstCms) { case (cms, file) => mergeSerialized(cms, file) }
  }

}

object ComboCount extends Counter
object IndividualCount extends Counter

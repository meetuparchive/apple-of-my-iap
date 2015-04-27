package com.meetup.iap

import com.meetup.iap.receipt.Subscription
import com.meetup.util.Logging

import java.io.File
import scala.io.Source

import org.json4s.DefaultFormats
import org.json4s.native.Serialization.{read, writePretty}
import org.apache.commons.io.FileUtils

/**
 * Save the existing biller data to a temp file to be cached.
 */
object BillerCache extends Logging {
  implicit val formats = DefaultFormats

  private val ProjectName = "iap-service"
  private val inProject = new File(".").getCanonicalPath.endsWith(ProjectName)

  private val Folder = {
    val base = if(inProject) "" else "iap-service/"
    new File(s"${base}tmp/")
  }
  if(!Folder.exists) {
    Folder.mkdirs
  }

  private val TempFile = new File(Folder, "subscriptions.json")
  if(!TempFile.exists) {
    TempFile.createNewFile
  }

  def readFromCache(): Map[String, Subscription] = {
    log.info("Reading from file: " + TempFile.getAbsolutePath)
    val raw = Source.fromFile(TempFile).mkString.trim

    if(raw.nonEmpty) {
        Map(read[Map[String, Subscription]](raw).toSeq: _*)
    } else Map.empty
  }

  def writeToCache(subs: Map[String, Subscription]) {
      val json = writePretty(subs)
      FileUtils.writeStringToFile(TempFile, json, "UTF-8")
  }
}

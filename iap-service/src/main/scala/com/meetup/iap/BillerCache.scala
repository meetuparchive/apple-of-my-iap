package com.meetup.iap

import com.meetup.iap.receipt.Subscription
import org.slf4j.LoggerFactory

import java.io.File
import scala.io.Source

import org.json4s.DefaultFormats
import org.json4s.native.Serialization.{read, writePretty}
import org.apache.commons.io.FileUtils

/**
 * Save the existing biller data to a temp file to be cached.
 */
object BillerCache {
  val log = LoggerFactory.getLogger(BillerCache.getClass)

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

  private val PlansFile = new File(Folder, "plans.json")
  if (!PlansFile.exists) {
    PlansFile.createNewFile
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

  def readPlansFromFile(): List[Plan] = {
    log.info(s"Reading from plans file: ${PlansFile.getAbsolutePath}")
    val raw = Source.fromFile(PlansFile).mkString.trim

    if(raw.nonEmpty) {
      log.info("Found some plans")
      List(read[List[Plan]](raw).toSeq: _*)
    } else List.empty
  }
}

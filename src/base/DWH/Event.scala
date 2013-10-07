package base.DWH

import scala.slick.session.Database.threadLocalSession
import scala.slick.driver.PostgresDriver.simple._
import java.sql.Timestamp
import base.Wrapper.DBWrapper

// Case class to make pattern matching for the Event table
case class Event(id: Int, event_id: Int, trace_id: Int, name: String, resource: String, role: String, group: String, transition: String, timestamp: Timestamp) extends AEventProperty {

  // Writes a single row to DWH
  def writeToDb {
    DBWrapper.db.withSession{Event.autoInc.insert(event_id,trace_id,name,resource,role,group,transition,timestamp)}
  }

  // Override tostring to fancy printlines
  override def toString:String = "This is Event. Id = "+id+"; event_id = "+event_id+
    "; trace_id = "+trace_id+"; name = "+name+"; resource = "+resource+"; role ="+role+
    "; group = "+group+"; transition = "+transition+"; timestamp = "+timestamp+";"
}

// Companion object used by SLICK framework
object Event extends Table[(Int, Int, Int, String, String, String, String, String, Timestamp)]("EVENT"){
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def event_id = column[Int]("EVENT_ID")
  def trace_id = column[Int]("TRACE_ID")
  def name = column[String]("NAME")
  def resource = column[String]("RESOURCE")
  def role = column[String]("ROLE")
  def group = column[String]("GROUP")
  def transition = column[String]("TRANSITION")
  def timestamp = column[Timestamp]("TIMESTAMP")
  def * = id ~ event_id ~ trace_id ~ name ~ resource ~ role ~ group ~ transition ~ timestamp
  def autoInc = event_id ~ trace_id ~ name ~ resource ~ role ~ group ~ transition ~ timestamp returning id
}


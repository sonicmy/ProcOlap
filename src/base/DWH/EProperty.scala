package base.DWH

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.session.Database.threadLocalSession
import base.Wrapper.DBWrapper

// Case class to make pattern matching for the EProperty table
case class EProperty(id: Int, event_id: Int, key: String, value: String, datatype: String) extends AEventProperty {

  // Writes a single row to DWH
  def writeToDb {
    DBWrapper.db.withSession{EProperty.autoInc.insert(event_id,key,value,datatype)}
  }

  // Override tostring to fancy printlines
  override def toString:String = "This is Event Property. Id = "+id+"; event_id = "+event_id+"; key = "+key+
  "; value = "+value+"; datatype = "+datatype+";"
}

// Companion object used by SLICK framework
object EProperty extends Table[(Int, Int, String, String, String)]("EPROPERTY"){
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def event_id = column[Int]("EVENT_ID")
  def key = column[String]("KEY")
  def value = column[String]("VALUE")
  def datatype = column[String]("DATATYPE")
  def * = id ~ event_id ~ key ~ value ~ datatype
  def autoInc = event_id ~ key ~ value ~ datatype returning id
}

case class EPropertySelector(key: String, value: String, datatype: String) {
  override def toString:String = "This is TPropertySelector. key = "+key+"; value = "+value+"; Type = "+datatype
}
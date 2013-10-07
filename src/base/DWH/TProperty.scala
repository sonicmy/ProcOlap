package base.DWH

import scala.slick.session.Database.threadLocalSession
import scala.slick.driver.PostgresDriver.simple._
import base.Wrapper.DBWrapper

// Case class to make pattern matching for the TProperty table
case class TProperty(id: Int,  trace_id: Int ,key: String, value: String, datatype: String) extends ATraceProperty {

  // Writes a single row to DWH
  def writeToDb {
    DBWrapper.db.withSession{TProperty.autoInc.insert(trace_id,key,value,datatype)}
  }

  // Override tostring to fancy printlines
  override def toString:String = "This is Trace Property. Id = "+id+"; trace_id = "+trace_id+"; key = "+key+
    "; value = "+value+"; datatype = "+datatype+";"
}

// Companion object used by SLICK framework
object TProperty extends Table[(Int, Int, String, String, String)]("TPROPERTY"){
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def trace_id = column[Int]("TRACE_ID")
  def key = column[String]("KEY")
  def value = column[String]("VALUE")
  def datatype = column[String]("DATATYPE")
  def * = id ~ trace_id ~ key ~ value ~ datatype
  def autoInc = trace_id ~ key ~ value ~ datatype returning id
}

case class TPropertySelector(key: String, value: String, datatype:String) {
  override def toString:String = "This is TPropertySelector. key = "+key+"; value = "+value+"; Type = "+datatype
}
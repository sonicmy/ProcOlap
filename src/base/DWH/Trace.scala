package base.DWH

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.session.Database.threadLocalSession
import base.Wrapper.DBWrapper

// Case class to make pattern matching for the Trace table
case class Trace(id: Int, trace_id: Int, name: String, modelReference: String) extends ATraceProperty {

  // Writes a single row to DWH
  def writeToDb {
    DBWrapper.db.withSession{Trace.autoInc.insert(trace_id,name,modelReference)}
  }

  // Override tostring to fancy printlines
  override def toString:String = "This is Trace. Id = "+ id+"; trace_id = "+trace_id+"; name = "+name+"; modelReferance = "+modelReference+";"
}

// Companion object used by SLICK framework
object Trace extends Table[(Int, Int, String, String)]("TRACE"){
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def trace_id = column[Int]("TRACE_ID")
  def name = column[String]("NAME")
  def modelReference = column[String]("MODELREFERENCE")
  def * = id ~ trace_id ~ name ~ modelReference
  def autoInc = trace_id ~ name ~ modelReference returning id
}
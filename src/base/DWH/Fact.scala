package base.DWH

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.session.Database.threadLocalSession
import base.Wrapper.DBWrapper
import scala.slick.jdbc.GetResult


// Case class to the Fact table
case class Fact(trace_id: Int, process: String) {
  // Writes a line to FACT table with SLICK framework
  def writeToDB{
    DBWrapper.db.withSession{Fact.insert(trace_id,process)}
  }
  override def toString:String = "Trace_id = "+trace_id+"; Process was "+process

}

// Companion object used by SLICK framework
object Fact extends Table[(Int, String)]("FACT"){
  def trace_id = column[Int]("TRACE_ID")
  def process = column[String]("PROCESS")
  def * = trace_id ~ process
  implicit val getFactResult = GetResult(r =>Fact(r.<<, r.<<))
}
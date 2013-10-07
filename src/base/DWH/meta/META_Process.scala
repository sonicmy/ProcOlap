package base.DWH.meta

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.session.Database.threadLocalSession
import base.Wrapper.DBWrapper

case class META_Process(id: Int, name: String) {
  override def toString:String = "META id = "+id+"; Name = "+name

  def writeToDb {
    DBWrapper.db.withSession{META_Process.insert(id,name)}
  }
}

object META_Process extends Table[(Int,String)]("META_PROCESS"){
  def id = column[Int]("ID")
  def name = column[String]("NAME")
  def * = id ~ name
}



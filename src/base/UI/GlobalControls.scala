package base.UI

import javafx.scene.text.{FontWeight, Font}
import scala.Predef._

object GlobalControls{
  val boxStyle = "-fx-border-color: gray; -fx-border-width: 0.5; -fx-background-color: #E8E8E8;"
  val splashBoxStyle = "-fx-border-color: gray; -fx-border-width: 0.5; -fx-border-radius: 10 10 10 10; -fx-background-radius: 10 10 10 10; -fx-background-color: #E8E8E8;"
  val outputStyle = "-fx-border-color: gray; -fx-border-width: 0.5; -fx-background-color: #E8E8E8; -fx-font-size: 10;"
  val splashLabelStyle = "-fx-background-color: #E8E8E8; -fx-font-size: 10;"
  val controlFont = Font.font(null, FontWeight.BOLD, 13)
  val headerFont = Font.font(null, 14)
  val menuLabelFont = Font.font(null, FontWeight.THIN, 10)
  val menuFileStyle = "-fx-background-color: #BFBFBF; -fx-font-size: 12;"
  val progressBackground =  "-fx-background-color: grey;"
  val notificationText = Font.font(null, FontWeight.BOLD, 20)

  var processingFile = ""
  var workspace = ""
  var connection = ("","","","")
  var hideShortSQLDefault = false

  def graphFontSize:String = SideMenu.getGraphFontSize
}

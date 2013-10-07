package base.UI

import javafx.scene.control.TextArea
import java.util.Calendar

object Output extends TextArea{
  this.setStyle(GlobalControls.outputStyle)
  this.setPrefSize(200, 120)
  this.setWrapText(true)

  def addText(line: String) = {
      this.appendText("\n"+Calendar.getInstance().getTime+"\n")
      this.appendText(line)
  }
}

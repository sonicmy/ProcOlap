package base.UI

import javafx.scene.control.{Button, ScrollPane}
import javafx.scene.layout.{HBox, Priority, VBox}
import javafx.scene.shape.Rectangle
import javafx.scene.paint.Color


class QueryConstructorHelper extends VBox {
  val scroll = new ScrollPane
  val queryConstuctorHelperBox = new VBox
  val queryConstuctorHelper = new VBox
  //this.setFitToWidth(true)
  queryConstuctorHelperBox.getChildren.addAll(queryConstuctorHelper)

  queryConstuctorHelper.setSpacing(3)
  queryConstuctorHelper.getChildren.add(new Rectangle(15,15,Color.GRAY))

  VBox.setVgrow(scroll, Priority.ALWAYS)
  scroll.setContent(queryConstuctorHelperBox)

  this.getChildren.addAll(scroll)

  def refreshHelper(graph: List[List[QueryConstructorTrait]]){
    queryConstuctorHelper.getChildren.clear()
    graph.foreach(
      row => {
        val box = new HBox
        box.setSpacing(3)
        queryConstuctorHelper.getChildren.add(box)
        row.foreach(
          col => {
            box.getChildren.add(col.getHelperItem)
          }
        )
      }
    )
  }

  def setExecuteBtn(button: Button){ this.getChildren.add(button)}
}

object QueryConstructorHelper{
  def apply = new QueryConstructorHelper
}

package base.UI

import javafx.scene.layout.{Priority, HBox, VBox}
import javafx.scene.control.{ComboBox, Label}
import javafx.geometry.Pos
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color

class QueryChooser(val constructor: QueryConstructor) extends VBox{
  private var pages: List[List[Label]] = Nil
  private var page: Int = 0
  private var max: Int = 0
  val boxStyle = "-fx-border-color: gray; -fx-border-width: 0.5; -fx-background-color: #E8E8E8"

  val header = new Label("Chooser Box")
  val operationDropDown = new ComboBox[String]
  val valuesAndSwitcherBox = new VBox()
  val valuesBox = new VBox
  val switcherBox = new HBox()
  val left = new Label("<")
  val middle = new Label("0 -> 0")
  val right = new Label(">")
  val itemsPerPage = 8

  valuesBox.setStyle(boxStyle)

  right.setOnMouseClicked(new EventHandler[MouseEvent] {
    def handle(p1: MouseEvent) {
      if(!pages.isEmpty && page < max){
        page += 1
        switchPage(page,max, pages(page -1))
      }
    }
  })
  right.setOnMouseEntered(new EventHandler[MouseEvent] {
    def handle(p1: MouseEvent) {
      if(!pages.isEmpty && page < max) right.setUnderline(true)
    }
  })
  right.setOnMouseExited(new EventHandler[MouseEvent] {
    def handle(p1: MouseEvent) {
      right.setUnderline(false)
    }
  })
  left.setOnMouseClicked(new EventHandler[MouseEvent] {
    def handle(p1: MouseEvent) {
      if(!pages.isEmpty && page > 1){
        page -=1
        switchPage(page, max, pages(page -1))
      }
    }
  })
  left.setOnMouseEntered(new EventHandler[MouseEvent] {
    def handle(p1: MouseEvent) {
      if(!pages.isEmpty && page > 1) left.setUnderline(true)
    }
  })
  left.setOnMouseExited(new EventHandler[MouseEvent] {
    def handle(p1: MouseEvent) {
      left.setUnderline(false)
    }
  })

  left.setTextFill(Color.BLUE)
  right.setTextFill(Color.BLUE)

  header.setStyle(boxStyle)
  middle.setPrefWidth(200)
  middle.setAlignment(Pos.BASELINE_CENTER)
  switcherBox.getChildren.addAll(left, middle, right)
  switcherBox.setStyle(boxStyle)
  valuesBox.setSpacing(1)
  valuesAndSwitcherBox.getChildren.addAll(valuesBox, switcherBox)
  this.setStyle(boxStyle)
  this.setPrefSize(200, 200)
  operationDropDown.getItems.addAll("=", "<>")
  operationDropDown.getSelectionModel.selectFirst()
  operationDropDown.setPrefWidth(200)
  header.setPrefWidth(200)
  header.setAlignment(Pos.BASELINE_CENTER)

  this.getChildren.addAll(header, valuesAndSwitcherBox, operationDropDown)
  VBox.setVgrow(valuesAndSwitcherBox, Priority.ALWAYS)
  VBox.setVgrow(valuesBox, Priority.ALWAYS)

  def setLabels(labels:List[Label], name: Label, datatype: String) {
    header.setText(datatype+ ": "+name.getText)
    val i = if(labels.size == 1) 1 else labels.size -1
    max = i / itemsPerPage + 1
    page = 1

    pages = labels.sortBy(_.getText).grouped(itemsPerPage).toList
    switchPage(page, max, pages(0))
  }

  def switchPage(page: Int, max: Int, label: List[Label]) {
    valuesBox.getChildren.clear()
    label.foreach(i => valuesBox.getChildren.add(i))
    middle.setText(page + " -> " + max)
  }

  def setSql(text: String){
    val textCorrected = text.replaceAll("\\#oper#",operationDropDown.getSelectionModel.getSelectedItem)
    constructor.setSql(textCorrected)
  }
}

object QueryChooser extends VBox{
  def apply(constructor: QueryConstructor) = new QueryChooser(constructor)
}

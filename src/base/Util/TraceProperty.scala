package base.Util

import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import base.UI.QueryChooser

case class TraceProperty (name: String, datatype: String, values: List[String], isExt: Boolean) {
  val label = new Label(name)
  val box = new VBox()
  val pages = if(values.size / 12 == 0) 1 else values.size / 12
  private var chooser:QueryChooser = null

  val labels = values.map{
    i =>
      val label = new Label(i)
      label.setOnMouseReleased(new EventHandler[MouseEvent] {
        def handle(p1: MouseEvent) {
          if(isExt) setSQL("AND \"TRACE\".\""+name.toUpperCase+"\" #oper# '"+i+"' ")
          else setSQL("AND \"TPROPERTY\".\"KEY\" = '"+name+"' AND \"TPROPERTY\".\"VALUE\" #oper# '"+i+"' ")
        }
      })

      label.setOnMouseEntered(new EventHandler[MouseEvent] {
        def handle(p1: MouseEvent) {
          label.setTextFill(Color.BLUE)
          label.setUnderline(true)
        }
      })

      label.setOnMouseExited(new EventHandler[MouseEvent] {
        def handle(p1: MouseEvent) {
          label.setTextFill(Color.BLACK)
          label.setUnderline(false)
        }
      })
    label
  }

  label.setOnMouseEntered(new EventHandler[MouseEvent] {
    def handle(p1: MouseEvent) {
      label.setTextFill(Color.BLUE)
      label.setUnderline(true)
    }
  })

  label.setOnMouseExited(new EventHandler[MouseEvent] {
    def handle(p1: MouseEvent) {
      label.setTextFill(Color.BLACK)
      label.setUnderline(false)
    }
  })

  label.setOnMouseReleased(new EventHandler[MouseEvent] {
    def handle(p1: MouseEvent) {
      setBox(pages, labels)
    }
  })

  def getGraphics:Label = label

  def setBox(maxPages: Int, labels: List[Label] ) {
    chooser.setLabels(labels, label, datatype)
  }

  def setSQL(text :String){
    chooser.setSql(text)
  }

  def setChooser(chooser: QueryChooser){
    this.chooser = chooser
  }
}

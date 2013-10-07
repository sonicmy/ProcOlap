package base.UI

import javafx.scene.layout.StackPane
import javafx.beans.property.{SimpleDoubleProperty, DoubleProperty}
import javafx.beans.value.{ObservableValue, ChangeListener}
import javafx.scene.control.{Label, ProgressIndicator, ProgressBar}
import javafx.geometry.{Insets, Pos}
import javafx.scene.paint.Color
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent

object ProgressSplash extends StackPane{

  val back = new StackPane
  val progressValue:DoubleProperty = new SimpleDoubleProperty
  val progressBar = new ProgressBar()
  val progressIndicator = new ProgressIndicator()
  val text = new Label("Processing..\nWatch log closely for details")
  val tutorialBtn = new Label("Watch tutorial")
  text.setFont(GlobalControls.notificationText)
  text.setTextFill(Color.LIGHTBLUE)
  tutorialBtn.setTextFill(Color.BLUE)


  StackPane.setAlignment(progressBar, Pos.CENTER)
  StackPane.setAlignment(progressIndicator, Pos.CENTER)
  StackPane.setAlignment(text,Pos.TOP_CENTER)
  StackPane.setAlignment(tutorialBtn, Pos.TOP_RIGHT)
  StackPane.setMargin(text, new Insets(40,0,0,0) )
  StackPane.setMargin(tutorialBtn, new Insets(5,20,0,0) )

  progressIndicator.setMaxSize(100,100)
  progressIndicator.setVisible(false)
  back.setStyle("-fx-background-color: black;")
  back.setOpacity(.5)

  progressBar.setPrefWidth(400)
  progressBar.setProgress(0)

  this.getChildren.addAll(back, progressBar, progressIndicator, text, tutorialBtn)

  progressValue.addListener(new ChangeListener[Number] {
    def changed(p1: ObservableValue[_ <: Number], p2: Number, p3: Number) {
      progressBar.setProgress(p3.doubleValue())
      if(p3.doubleValue() == 100.0){
        progressBar.setVisible(false)
        progressIndicator.setVisible(true)
      }
    }
  })

  tutorialBtn.setOnMouseEntered(new EventHandler[MouseEvent] {
    def handle(p1: MouseEvent) {
      tutorialBtn.setUnderline(true)
    }
  })

  tutorialBtn.setOnMouseExited(new EventHandler[MouseEvent] {
    def handle(p1: MouseEvent) {
      tutorialBtn.setUnderline(false)
    }
  })

  tutorialBtn.setOnMouseClicked(new EventHandler[MouseEvent] {
    def handle(p1: MouseEvent) {
      UI.addTutorial
    }
  })

}

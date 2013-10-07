package base.UI

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout._
import javafx.stage.Stage
import base.Wrapper.{XESParser, DBWrapper}
import javafx.concurrent.{WorkerStateEvent, Task}
import javafx.scene.media.AudioClip
import java.io.File
import javafx.event.EventHandler

class UI extends Application{
  def start(primaryStage: Stage) {
    primaryStage.setTitle("Process OLAP");
    UI.primaryStage = primaryStage
    val root = UI.root
    val scene = new Scene(root, 300, 470)
    primaryStage.setMaxHeight(470)
    primaryStage.setMaxWidth(300)
    UI.primaryScene = scene
    root.getChildren.addAll(BaseLayout,SplashScreen)
    primaryStage.setScene(scene)
    primaryStage.show()
  }
}

object UI {
  def main(args: Array[String]) {
    Application.launch(classOf[UI], args: _*)
  }

  def removeSplash = {
    val connection = GlobalControls.connection
    DBWrapper.setConnection(connection._1, connection._2, connection._3, connection._4)
    SideMenu.workingDirectory.setText(GlobalControls.workspace)
    SideMenu.processingFile.setText(GlobalControls.processingFile)
    root.getChildren.remove(SplashScreen)

    analyzeXes

    primaryStage.setMaxHeight(Int.MaxValue)
    primaryStage.setMaxWidth(Int.MaxValue)
    primaryStage.setMinHeight(600)
    primaryStage.setMinWidth(800)
    primaryStage.setResizable(true)
  }

  def analyzeXes {
    val task = new Task[Void]() {
      override def call:Void = {
        XESParser.processXESLog(GlobalControls.processingFile)
        Output.addText("Processing complete.")
        if(SideMenu.getSoundEnabled){
          val ding = new AudioClip(new File("sound/DING.WAV").toURI.toURL.toExternalForm)
          ding.play()
        }
        null
      }
    }

    task.setOnSucceeded(new EventHandler[WorkerStateEvent] {
      def handle(p1: WorkerStateEvent) {
        root.getChildren.remove(ProgressSplash)
        val(trace, event) = XESParser.analyzeExistingDWH
        BaseLayout.addTrace(trace)
        BaseLayout.addEvent(event)
      }
    })

    new Thread(task).start()

    root.getChildren.add(ProgressSplash)
  }

  def addTutorial = {
    root.getChildren.add(Tutorial)
    primaryStage.setMaxHeight(600)
    primaryStage.setMaxWidth(800)
    root.setPrefWidth(800)
    root.setPrefHeight(600)
    primaryStage.setResizable(true)
  }

  def removeTutorial = {
    root.getChildren.remove(Tutorial)
    primaryStage.setMaxHeight(Int.MaxValue)
    primaryStage.setMaxWidth(Int.MaxValue)
    primaryStage.setResizable(true)
  }

  var primaryScene:Scene = null
  var primaryStage:Stage = null
  val root = new StackPane
}
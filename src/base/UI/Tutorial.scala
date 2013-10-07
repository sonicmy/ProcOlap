package base.UI

import javafx.scene.layout.StackPane
import javafx.scene.image.{ImageView, Image}
import java.io.File
import javafx.scene.control.Label
import javafx.geometry.{Insets, Pos}
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color

object Tutorial extends StackPane {
  val slide1 = new Image(new File("pres/intro.png").toURI.toURL.toExternalForm)
  val slide2 = new Image(new File("pres/output.png").toURI.toURL.toExternalForm)
  val slide3 = new Image(new File("pres/accordion.png").toURI.toURL.toExternalForm)
  val slide4 = new Image(new File("pres/constructor1.png").toURI.toURL.toExternalForm)
  val slide5 = new Image(new File("pres/constructor2.png").toURI.toURL.toExternalForm)
  val slide6 = new Image(new File("pres/variance.png").toURI.toURL.toExternalForm)
  val slide7 = new Image(new File("pres/rest.png").toURI.toURL.toExternalForm)
  val slide8 = new Image(new File("pres/final.png").toURI.toURL.toExternalForm)

  val slides = List(slide1,slide2,slide3,slide4,slide5,slide6,slide7,slide8)

  val viewer = new ImageView
  val frontPane = new StackPane
  val skip = new Label("Skip tutorial")

  var page = 1

  skip.setFont(GlobalControls.headerFont)
  skip.setTextFill(Color.BLUE)

  skip.setOnMouseEntered(new EventHandler[MouseEvent] {
    def handle(p1: MouseEvent) {
      skip.setUnderline(true)
    }
  })

  skip.setOnMouseExited(new EventHandler[MouseEvent] {
    def handle(p1: MouseEvent) {
      skip.setUnderline(false)
    }
  })

  skip.setOnMouseClicked(new EventHandler[MouseEvent] {
    def handle(p1: MouseEvent) {
      UI.removeTutorial
    }
  })

  StackPane.setAlignment(skip, Pos.TOP_RIGHT)
  StackPane.setMargin(skip, new Insets(20,80,0,0))

  viewer.setImage(slides.head)
  frontPane.getChildren.add(skip)
  this.getChildren.addAll(viewer, frontPane)

  frontPane.setOnMouseClicked(new EventHandler[MouseEvent] {
    def handle(p1: MouseEvent) {
      if(page < 8) {
        page += 1
        viewer.setImage(slides(page -1))
      } else{ UI.removeTutorial; page = 1}
    }
  })
}

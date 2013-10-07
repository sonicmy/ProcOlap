package base.UI

import javafx.scene.layout.{Priority, HBox, VBox}
import javafx.stage.{FileChooser, DirectoryChooser}
import javafx.scene.control._
import javafx.scene.text.TextAlignment
import javafx.event.{ActionEvent, EventHandler}
import javafx.scene.input.MouseEvent
import javafx.geometry.Pos
import java.io.File
import javafx.stage.FileChooser.ExtensionFilter
import javafx.scene.effect.BlendMode
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle

object SideMenu extends HBox{
  val box = this
  val mainBox = new VBox
  val headerBox = new HBox
  val header = new Label("Properties")
  val directoryLabel = new Label("Working directory")
  val directoryMainBox = new VBox
  val directoryBox = new HBox
  val direcrotyChooser = new DirectoryChooser
  val workingDirectory  = new Label
  val directoryChooserBtn = new Button("...")
  val processingLabel = new Label("Processing XES file")
  val processingMainBox = new VBox
  val processingBox = new HBox
  val processingFile = new Label
  val processingChooser = new FileChooser()
  val processingChooserBtn = new Button("...")
  val graphFontMainBox = new VBox
  val graphFontBox = new HBox
  val graphFontLabel = new Label("Choose Graph font size")
  val graphLessButton = new Label("-")
  val graphValue = new Label("6")
  val graphMoreButton = new Label("+")
  val colorSchemeMainBox = new VBox
  val colorSchemeBox = new HBox
  val colorSchemeLabel = new Label("Choose color scheme")
  val colorScheme = new ComboBox[String]
  val formater = new Rectangle(15,0,Color.BLACK)
  val leftBox = new VBox
  val sideMenuClosedButton = new Label("<")
  val soundBox = new HBox
  val enableSound = new CheckBox
  val enableSoundLabel = new Label("Enable sound")

  enableSound.selectedProperty().setValue(true)

  leftBox.setSpacing(0)

  formater.setOpacity(0)
  graphLessButton.setTextFill(Color.BLUE)
  graphMoreButton.setTextFill(Color.BLUE)
  sideMenuClosedButton.setTextFill(Color.BLUE)
  colorScheme.getItems.addAll("Light","Dark")
  colorScheme.getSelectionModel.selectFirst()
  colorScheme.setOnAction(new EventHandler[ActionEvent] {
    def handle(p1: ActionEvent) {
      colorScheme.getSelectionModel.getSelectedItem match {
        case "Light" => UI.root.setBlendMode(BlendMode.SRC_ATOP)
        case "Dark"  => UI.root.setBlendMode(BlendMode.DIFFERENCE)
      }
    }
  })

  processingChooser.getExtensionFilters.add(new ExtensionFilter("XES document (*.xes)", "*.xes"))

  sideMenuClosedButton.setOnMouseEntered(new EventHandler[MouseEvent] {
    def handle(p1: MouseEvent) {
      sideMenuClosedButton.setUnderline(true)
    }
  })
  sideMenuClosedButton.setOnMouseExited(new EventHandler[MouseEvent] {
    def handle(p1: MouseEvent) {
      sideMenuClosedButton.setUnderline(false)
    }
  })
  sideMenuClosedButton.setOnMouseClicked(new EventHandler[MouseEvent] {
    def handle(p1: MouseEvent) {
      leftBox.getChildren.remove(sideMenuClosedButton)
      box.getChildren.add(mainBox)
    }
  })


  graphLessButton.setOnMouseEntered(new EventHandler[MouseEvent] {
    def handle(p1: MouseEvent) {
      if(graphValue.getText.toInt > 0) graphLessButton.setUnderline(true)
    }
  })
  graphLessButton.setOnMouseExited(new EventHandler[MouseEvent] {
    def handle(p1: MouseEvent) {
      graphLessButton.setUnderline(false)
    }
  })
  graphLessButton.setOnMouseClicked(new EventHandler[MouseEvent] {
    def handle(p1: MouseEvent) {
      val value = graphValue.getText.toInt
      if(value > 0) graphValue.setText(value - 1 toString)
    }
  })

  graphMoreButton.setOnMouseEntered(new EventHandler[MouseEvent] {
    def handle(p1: MouseEvent) {
      graphMoreButton.setUnderline(true)
    }
  })
  graphMoreButton.setOnMouseExited(new EventHandler[MouseEvent] {
    def handle(p1: MouseEvent) {
      graphMoreButton.setUnderline(false)
    }
  })
  graphMoreButton.setOnMouseClicked(new EventHandler[MouseEvent] {
    def handle(p1: MouseEvent) {
      val value = graphValue.getText.toInt
      if(value > 0) graphValue.setText(value + 1 toString)
    }
  })

  directoryChooserBtn.setOnAction(new EventHandler[ActionEvent] {
    def handle(p1: ActionEvent) {
      val selectedDirectory = direcrotyChooser.showDialog(UI.primaryStage)
      if(selectedDirectory != null){
        workingDirectory.setText(selectedDirectory.getAbsolutePath)
        GlobalControls.workspace = selectedDirectory.getAbsolutePath+"/"
      }
    }
  })

  processingChooserBtn.setOnAction(new EventHandler[ActionEvent] {
    def handle(p1: ActionEvent) {
      val selectedFile = processingChooser.showOpenDialog(UI.primaryStage)
      if(selectedFile != null) {
        processingFile.setText(selectedFile.getAbsolutePath)
        GlobalControls.processingFile = selectedFile.getAbsolutePath
        UI.analyzeXes
      }
    }
  })

  header.setFont(GlobalControls.headerFont)

  this.setSpacing(4)
  mainBox.setSpacing(8)
  this.setStyle(GlobalControls.boxStyle)
  //headerBox.setStyle(GlobalControls.boxStyle)

  header.setPrefWidth(250)
  workingDirectory.setPrefWidth(220)
  processingFile.setPrefWidth(220)
  workingDirectory.setStyle(GlobalControls.menuFileStyle)
  processingFile.setStyle(GlobalControls.menuFileStyle)
  workingDirectory.setWrapText(true)
  processingFile.setWrapText(true)
  processingLabel.setAlignment(Pos.BOTTOM_CENTER)
  colorSchemeLabel.setAlignment(Pos.BOTTOM_CENTER)
  directoryLabel.setAlignment(Pos.BOTTOM_CENTER)
  graphFontLabel.setAlignment(Pos.BOTTOM_CENTER)
  enableSoundLabel.setAlignment(Pos.BOTTOM_CENTER)
  processingLabel.setUnderline(true)
  directoryLabel.setUnderline(true)
  colorSchemeLabel.setUnderline(true)
  graphFontLabel.setUnderline(true)
  enableSoundLabel.setUnderline(true)
  processingBox.setSpacing(10)
  directoryBox.setSpacing(10)
  graphFontBox.setSpacing(30)
  soundBox.setSpacing(30)


  processingLabel.setFont(GlobalControls.menuLabelFont)
  directoryLabel.setFont(GlobalControls.menuLabelFont)
  colorSchemeLabel.setFont(GlobalControls.menuLabelFont)
  graphFontLabel.setFont(GlobalControls.menuLabelFont)
  enableSoundLabel.setFont(GlobalControls.menuLabelFont)

  processingMainBox.setSpacing(2)
  directoryMainBox.setSpacing(2)
  colorSchemeMainBox.setSpacing(2)

  graphFontMainBox.getChildren.addAll(graphFontLabel, graphFontBox)
  processingMainBox.getChildren.addAll(processingLabel, processingBox)
  directoryMainBox.getChildren.addAll(directoryLabel, directoryBox)
  colorSchemeMainBox.getChildren.addAll(colorSchemeLabel, colorSchemeBox)

  header.setTextAlignment(TextAlignment.CENTER)
  header.setAlignment(Pos.BASELINE_CENTER)
  headerBox.getChildren.addAll(header)
  colorSchemeBox.getChildren.add(colorScheme)

  directoryBox.getChildren.addAll(workingDirectory, directoryChooserBtn)
  graphFontBox.getChildren.addAll(graphLessButton, graphValue, graphMoreButton)
  processingBox.getChildren.addAll(processingFile,processingChooserBtn)
  leftBox.getChildren.add(formater)

  soundBox.getChildren.addAll(enableSound, enableSoundLabel)

  HBox.setHgrow(workingDirectory, Priority.ALWAYS)
  HBox.setHgrow(header, Priority.ALWAYS)
  HBox.setHgrow(processingFile, Priority.ALWAYS)


  mainBox.getChildren.addAll(headerBox, directoryMainBox, processingMainBox, graphFontMainBox, colorSchemeMainBox, soundBox)
  this.getChildren.addAll(leftBox)
  leftBox.getChildren.add(sideMenuClosedButton)

  def setRemoveButton(remove: Label) = {
    headerBox.getChildren.add(remove)
  }

  def removeMainBox = {
    this.getChildren.remove(mainBox)
    VBox.setVgrow(sideMenuClosedButton, Priority.ALWAYS)
    leftBox.getChildren.add(sideMenuClosedButton)
  }

  def setWorkingDirectory(directory: File){
    GlobalControls.workspace = directory.getAbsolutePath+"/"
    workingDirectory.setText(directory.getAbsolutePath+"/")
  }

  def setProcessingFile(file: String) = GlobalControls.processingFile = getXESFile
  def getWorkingDirectory: String = workingDirectory.getText

  def getXESFile: String = processingFile.getText
  def getGraphFontSize: String = graphValue.getText
  def getSoundEnabled: Boolean = enableSound.selectedProperty().getValue

}

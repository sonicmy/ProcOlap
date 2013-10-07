package base.UI

import javafx.scene.layout.{Priority, VBox, HBox, StackPane}
import javafx.scene.control.{PasswordField, Button, TextField, Label}
import javafx.scene.shape.Rectangle
import javafx.scene.paint.Color
import javafx.geometry.Pos
import javafx.event.{ActionEvent, EventHandler}
import base.Wrapper.DBWrapper
import javafx.stage.{FileChooser, DirectoryChooser}
import javafx.stage.FileChooser.ExtensionFilter

object SplashScreen extends StackPane{
  val mainBox = new HBox
  val leftBox = new VBox
  val rightBox = new VBox

  UI.primaryStage.setResizable(false)

  def getDatabaseProperties:(String, String, String, String) =
    (databaseBox.databaseAddressField.getText+"/",
      databaseBox.databaseDBNameField.getText,
      databaseBox.databaseUserField.getText,
      databaseBox.databasePasswordField.getText
    )

  def getWorkspace: String = workingDirectoryBox.directoryChooserField.getText+"/"

  def getProcessingFile: String = processingXESBox.fileChooserField.getText

  object databaseBox extends VBox {

  val databaseBoxLabel = new Label("Fill database connection details")

  val databaseBoxUserBox = new HBox
  val databaseUserField = new TextField
  val databaseUserLabel = new Label("user: ")

  val databaseBoxPasswordBox = new HBox
  val databasePasswordField = new PasswordField
  val databasePasswordLabel = new Label("pass: ")

  val databaseBoxAddressBox = new HBox
  val databaseAddressField = new TextField
  val databaseAddressLabel = new Label("address: ")

  val databaseBoxDBNameBox = new HBox
  val databaseDBNameField = new TextField
  val databaseDBNameLabel = new Label("name: ")

  val databaseTestConnectionBox = new HBox
  val databaseTestConnectionBtn = new Button("Test connection")
  val databaseTestConnectionLabel = new Label("")


  this.setStyle(GlobalControls.splashBoxStyle)

  databaseAddressField.setText("localhost")
  databaseDBNameField.setText("dipdb")
  databaseUserField.setText("postgres")

  databasePasswordField.setMinWidth(200)
  databaseAddressField.setMinWidth(200)
  databaseDBNameField.setMinWidth(200)
  databaseUserField.setMinWidth(200)

  databaseTestConnectionBox.setRotate(180)
  databaseTestConnectionBtn.setRotate(180)
  databaseTestConnectionLabel.setRotate(180)

  databaseTestConnectionBox.getChildren.addAll(databaseTestConnectionBtn,databaseTestConnectionLabel)

  databaseDBNameLabel.setStyle(GlobalControls.splashLabelStyle)
  databaseAddressLabel.setStyle(GlobalControls.splashLabelStyle)
  databasePasswordLabel.setStyle(GlobalControls.splashLabelStyle)
  databaseUserLabel.setStyle(GlobalControls.splashLabelStyle)

  databaseBoxUserBox.setSpacing(7)
  databaseBoxPasswordBox.setSpacing(7)
  databaseBoxAddressBox.setSpacing(7)
  databaseBoxDBNameBox.setSpacing(7)

  databaseUserLabel.setPrefWidth(50)
  databasePasswordLabel.setPrefWidth(50)
  databaseAddressLabel.setPrefWidth(50)
  databaseDBNameLabel.setPrefWidth(50)
  databaseTestConnectionLabel.setPrefWidth(150)

  databaseBoxLabel.setFont(GlobalControls.controlFont)

  databaseBoxUserBox.getChildren.addAll(databaseUserLabel, databaseUserField)
  databaseBoxPasswordBox.getChildren.addAll(databasePasswordLabel, databasePasswordField)
  databaseBoxAddressBox.getChildren.addAll(databaseAddressLabel, databaseAddressField)
  databaseBoxDBNameBox.getChildren.addAll(databaseDBNameLabel, databaseDBNameField)

  this.getChildren.addAll(databaseBoxLabel, databaseBoxAddressBox, databaseBoxDBNameBox, databaseBoxUserBox, databaseBoxPasswordBox, databaseTestConnectionBox, new Rectangle(0,5, Color.GRAY))
  this.setSpacing(20)
  this.setAlignment(Pos.CENTER)

  HBox.setHgrow(databaseAddressField, Priority.ALWAYS)
  HBox.setHgrow(databaseDBNameField, Priority.ALWAYS)
  HBox.setHgrow(databasePasswordField, Priority.ALWAYS)
  HBox.setHgrow(databaseUserField, Priority.ALWAYS)
  HBox.setHgrow(leftBox, Priority.ALWAYS)

  databaseTestConnectionBtn.setOnAction(new EventHandler[ActionEvent] {
      def handle(p1: ActionEvent) {
        if(DBWrapper.tryNewConnection(databaseAddressField.getText+"/", databaseDBNameField.getText, databaseUserField.getText, databasePasswordField.getText)) {
          databaseTestConnectionLabel.setText("Succeeded")
          databaseTestConnectionLabel.setTextFill(Color.GREEN)
        }
        else {
          databaseTestConnectionLabel.setText("Failed")
          databaseTestConnectionLabel.setTextFill(Color.RED)
        }
      }
    })
  }

  object workingDirectoryBox extends VBox {
    val workingDirectoryLabel = new Label("Set workspace folder")
    val directoryChooserBox = new HBox
    val directoryChooserField = new Label("/home/sonicmy")
    val directoryChooserBtn = new Button("...")
    val directoryChooser = new DirectoryChooser

    workingDirectoryLabel.setFont(GlobalControls.controlFont)

    directoryChooserField.setPrefWidth(200)
    directoryChooserField.setWrapText(true)
    directoryChooserField.setStyle(GlobalControls.menuFileStyle)

    directoryChooserBox.setRotate(180)
    directoryChooserBtn.setRotate(180)
    directoryChooserField.setRotate(180)

    directoryChooserBox.getChildren.addAll(directoryChooserBtn,directoryChooserField)

    directoryChooserBox.setSpacing(10)
    this.setSpacing(7)
    this.setStyle(GlobalControls.splashBoxStyle)
    this.getChildren.addAll(workingDirectoryLabel,directoryChooserBox,new Rectangle(0,5, Color.GRAY))

    directoryChooserBtn.setOnAction(new EventHandler[ActionEvent] {
      def handle(p1: ActionEvent) {
        val directory = directoryChooser.showDialog(UI.primaryStage)
        if(directory != null) directoryChooserField.setText(directory.getAbsolutePath)
      }
    })
  }

  object processingXESBox extends VBox {
    val processingXESLabel = new Label("Set XES log file")
    val fileChooserBox = new HBox
    val fileChooserField = new Label("")
    val fileChooserBtn = new Button("...")
    val fileChooser = new FileChooser

    processingXESLabel.setFont(GlobalControls.controlFont)
    fileChooser.getExtensionFilters.add(new ExtensionFilter("XES document (*.xes)", "*.xes"))

    fileChooserField .setPrefWidth(200)
    fileChooserField .setWrapText(true)
    fileChooserField .setStyle(GlobalControls.menuFileStyle)

    fileChooserBox.setRotate(180)
    fileChooserBtn.setRotate(180)
    fileChooserField.setRotate(180)

    fileChooserBox.getChildren.addAll(fileChooserBtn,fileChooserField)

    fileChooserBox.setSpacing(10)
    this.setSpacing(7)
    this.setStyle(GlobalControls.splashBoxStyle)
    this.getChildren.addAll(processingXESLabel,fileChooserBox,new Rectangle(0,5, Color.GRAY))

    fileChooserBtn.setOnAction(new EventHandler[ActionEvent] {
      def handle(p1: ActionEvent) {
        val file = fileChooser.showOpenDialog(UI.primaryStage)
        if(file != null) fileChooserField.setText(file.getAbsolutePath)
      }
    })
  }

  object proceedBox extends VBox {
    val box = new HBox
    val next = new Button("Proceed")

    next.setOnAction(new EventHandler[ActionEvent] {
      def handle(p1: ActionEvent) {
        GlobalControls.connection = getDatabaseProperties
        GlobalControls.workspace = getWorkspace
        GlobalControls.processingFile = getProcessingFile
        UI.removeSplash
      }
    })

    next.setRotate(180)
    this.setRotate(180)
    box.getChildren.add(next)
    this.getChildren.addAll(new Rectangle(0,10, Color.GRAY),box)
    VBox.setVgrow(this, Priority.ALWAYS)
  }

  rightBox.setSpacing(10)
  rightBox.getChildren.addAll(new Rectangle(0,10,Color.GRAY), databaseBox, workingDirectoryBox, processingXESBox, proceedBox)
  mainBox.getChildren.addAll(leftBox, rightBox, new Rectangle(10,0,Color.GRAY))
  this.getChildren.add(mainBox)
  this.setStyle(GlobalControls.boxStyle)
}

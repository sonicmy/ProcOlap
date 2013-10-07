package base.UI

import javafx.scene.layout.{StackPane, Priority, HBox, VBox}
import javafx.scene.control.{Slider, Label}
import javafx.geometry.{Orientation, Pos}
import javafx.scene.image.{ImageView, Image}
import java.io.{File, FileWriter}
import javafx.scene.paint.Color
import base.Wrapper.DBWrapper
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.beans.value.{ObservableValue, ChangeListener}
import scala.io.Source

class SliceBox(sliderBox: VBox) extends VBox{
  val box = this
  this.setSpacing(1)
  val slider = new Slider
  val sliderLabel = new Label()


  HBox.setHgrow(this, Priority.ALWAYS)

  sliderBox.setSpacing(1)
  sliderBox.setStyle(GlobalControls.boxStyle)
  slider.setMin(50); slider.setMax(99.99)
  slider.setOrientation(Orientation.VERTICAL)
  slider.setMajorTickUnit(90)
  slider.setMinorTickCount(50)
  slider.setShowTickLabels(true)
  slider.setShowTickMarks(true)
  slider.valueProperty().addListener(new ChangeListener[Number]{
    def changed(p1: ObservableValue[_ <: Number], p2: Number, p3: Number) {
      sliderLabel.setText(String.format("%.2f",p3))
      box.getChildren.toArray.foreach(i => i match { case e: SliceItem => e.needRefresh(p3.doubleValue())})
    }
  })
  sliderLabel.setText(slider.getMin.toString)
  sliderBox.getChildren.addAll(sliderLabel,slider)
  VBox.setVgrow(slider, Priority.ALWAYS)

  class SliceItem(val name: String, val image: ImageView, weightedProcess: List[(Double,String)], var last: (Double, String), val shortQuery: String, var hideShortSql :Boolean ) extends HBox{
    val label = new Label(shortQuery)
    val deleteBtn = new Label("X")
    val graph = new StackPane
    val labelBox = new VBox
    val sqlBox = new VBox
    val hideShortBtn = new Label

    deleteBtn.setOnMouseEntered(new EventHandler[MouseEvent] {
      def handle(p1: MouseEvent) {
        deleteBtn.setUnderline(true)
      }
    })
    deleteBtn.setOnMouseExited(new EventHandler[MouseEvent] {
      def handle(p1: MouseEvent) {
        deleteBtn.setUnderline(false)
      }
    })
    deleteBtn.setOnMouseClicked(new EventHandler[MouseEvent] {
      def handle(p1: MouseEvent) {
        remove
      }
    })

    hideShortBtn.setOnMouseEntered(new EventHandler[MouseEvent] {
      def handle(p1: MouseEvent) {
        hideShortBtn.setUnderline(true)
      }
    })
    hideShortBtn.setOnMouseExited(new EventHandler[MouseEvent] {
      def handle(p1: MouseEvent) {
        hideShortBtn.setUnderline(false)
      }
    })
    hideShortBtn.setOnMouseClicked(new EventHandler[MouseEvent] {
      def handle(p1: MouseEvent) {
        if(hideShortBtn.getText == ">"){
          sqlBox.getChildren.add(label)
          hideShortBtn.setText("<")
        }
        else{
          sqlBox.getChildren.remove(label)
          hideShortBtn.setText(">")
        }
        hideShortSql = !hideShortSql
      }
    })

    deleteBtn.setTextFill(Color.BLUE)
    deleteBtn.setFont(GlobalControls.controlFont)
    //val content = new Rectangle(645, 125, Color.RED)

    this.setStyle(GlobalControls.boxStyle)
    /*
    label.setAlignment(Pos.BOTTOM_RIGHT)
    label.setRotate(90)
    label.setPrefWidth(150)
    */
    graph.getChildren.addAll(image)
    labelBox.getChildren.addAll(deleteBtn,sqlBox)
    this.getChildren.addAll(labelBox,graph)

    VBox.setVgrow(this, Priority.ALWAYS)
    HBox.setHgrow(graph, Priority.ALWAYS)

    label.setWrapText(true)
    label.setPrefWidth(150)
    label.setMinWidth(50)
    label.setFont(GlobalControls.menuLabelFont)
    label.setRotate(180)
    sqlBox.setRotate(180)
    sqlBox.getChildren.add(hideShortBtn)
    hideShortBtn.setRotate(180)
    hideShortBtn.setTextFill(Color.BLUE)
    hideShortBtn.setAlignment(Pos.CENTER)
    if(!hideShortSql) {
      sqlBox.getChildren.add(label)
      hideShortBtn.setText("<")
    }
    else hideShortBtn.setText(">")

    def remove = removeItem(this)

    def needRefresh(value: Double){
      val wproc = weightedProcess.filter(_._1 < value)
      if(!wproc.isEmpty && wproc.last != last){
        val (content, last) = SliceUtil.countWeights(weightedProcess,value)
        if(addGraph(content,GlobalControls.workspace,name,name, weightedProcess, last, shortQuery, hideShortSql)) this.remove
      }
    }
  }

  def removeItem(item: SliceItem) = this.getChildren.removeAll(item)

  def addItem(name: String, image: ImageView, weightedProcess: List[(Double,String)], last: (Double, String), shortQuery: String, hideShortSql: Boolean) = this.getChildren.add(new SliceItem(name, image, weightedProcess, last, shortQuery, GlobalControls.hideShortSQLDefault))

  def addGraph(content: List[String], filepath: String, inName: String, outName: String , weightedProcess:List[(Double,String)], last: (Double, String), shortQuery: String, hideShortSql: Boolean):Boolean = {
    def tryWriteFile(content: List[String], filepath: String, inName: String, outName: String): Boolean = {
      try{

        val header = """digraph G {
                       size="""+GlobalControls.graphFontSize+""";
                       bgcolor=transparent;
                       rankdir="LR"; """+"\n"
        val footer = "\n}"

        val finalFile = header + content.mkString("\n") + footer
        val fileLocation = filepath+inName+".dot"

        //val lines = scala.io.Source.fromFile(fileLocation).mkString
        //println(lines)

        val file = new File(fileLocation)
        if(file.exists && Source.fromFile(fileLocation).getLines.mkString("\n") == finalFile) false
        else {
          file.createNewFile()

          val fw = new FileWriter(fileLocation)
          fw.write(finalFile)
          fw.close()
          //println(fileLocation)
          true
        }
      }
      catch {case ex:Exception => Output.addText("\n"+ex.getMessage+"\nError writing file "+filepath+inName+".dot"); false}
    }

    def tryCallDot(filepath: String, inName: String, outName: String): Boolean = {
      try{
        val rt = Runtime.getRuntime()
        val command = "dot -Tpng "+filepath+inName+".dot -o "+filepath+outName+".png"
        val file = new File(filepath+outName)
        if(file.exists()) file.delete()
        rt.exec(command)
        true
      }
      catch {case _ => Output.addText("\nUnable to call dot. Check your graphwiz"); false}
    }

    def tryReadFile(cycle: Int, filepath: String, filename: String): Boolean = {
      try {
        Thread.sleep(100)
        val imagePath = filepath+filename+".png"
        val image = new Image(new File(imagePath).toURI.toURL.toExternalForm)
        if(image.getWidth == 0) throw new Exception("Not yet loaded")

        val imageView = new ImageView(image)

        addItem(filename, imageView, weightedProcess, last, shortQuery, hideShortSql)
        true
      }
      catch {
        case _ if (cycle < 0) => Output.addText("\nUnable to load file "+filepath+filename+".png"); false
        case ex:Exception => {println(ex.getMessage) ;Thread.sleep(500); tryReadFile(cycle -1, filepath, filename) }
        }
    }

    val v1 = tryWriteFile(content,filepath,inName,outName)
    val v2 = if(v1) tryCallDot(filepath,inName,outName) else false
    val v3 = if(v2) tryReadFile(10,filepath,outName) else false
    if(v3) Output.addText("\nGenerating graph") else Output.addText("\nProcessing failed")
    v3
  }
}


object SliceUtil{
  def countWeights(facts :List[(Double, String)], percent: Double):(List[String],(Double, String)) = {

    def makeTuples(facts :List[String]):List[(Char,Char)] = facts match {
      case Nil => Nil
      case head :: tail => {
        val line = head.split("->").toList.map(c => c.charAt(0))
        processLine(line) ::: makeTuples(tail)
      }
    }

    def processLine(line: List[Char]): List[(Char,Char)] = line match {
      case e1 :: Nil => Nil
      case e1 :: e2 :: tail => (e1 -> e2) :: processLine(e2 :: tail)
    }

    def makeOutput(tuples: List[(Char,Char)]): List[(Char,List[Char])] = tuples match {
      case Nil => Nil
      case (e1, e2) :: rest => {
        val (head, tail) = rest.partition(elem => e1 == elem._1)
        ((e1 -> e2) :: head).distinct.groupBy(_._1).map(e => e._1 -> e._2.map(_._2).filter(_ != e1)).toList ::: makeOutput(tail)
      }
    }

    def makeInput(tuples: List[(Char,Char)]): List[(List[Char],Char)] = tuples match {
      case Nil => Nil
      case (e1, e2) :: rest => {
       val (head, tail) = rest.partition(elem => e2 == elem._2)
        ((e1 -> e2) :: head).distinct.groupBy(_._2).map(e => e._2.map(_._1).filter(_ != e2) -> e2).toList ::: makeInput(tail)
      }
    }

    def defineHeadAndTail(tuples: List[String], output: List[(Char,List[Char])]):(List[Char],List[Char]) = {
      val heads = tuples.map(_.head).distinct
      val tails = tuples.map(_.last).distinct
      heads -> tails.filterNot(c => output.map(_._1) contains c)
    }
    if(facts.isEmpty) Nil -> (-1.0, "")
    else{
      val (processes0, higher) = facts.partition(_._1 < percent)
      val processes = if(processes0.isEmpty) higher.last :: Nil else processes0
      val tuples = makeTuples(processes.map(_._2))
      //val output = makeOutput(tuples)
      //val input = makeInput(tuples)
      //val (head,tail) = defineHeadAndTail(processes, output)
      val procNames = DBWrapper.getMetaProcess
      tuples.distinct.map(i => "\""+procNames.filter(p => p._1.toString == i._1.toString).head._2+"\""  + " -> " +"\""+ procNames.filter(p => p._1.toString == i._2.toString).head._2 + "\";") -> processes.last
    }
  }
}

object SliceBox{
  def apply(slider: VBox) = new SliceBox(slider)
}

package base.UI

import javafx.scene.layout.{Priority, HBox, VBox}
import javafx.scene.control._
import javafx.scene.paint.Color
import javafx.event.{ActionEvent, EventHandler}
import javafx.scene.input.MouseEvent
import javafx.scene.Node
import javafx.scene.shape.Rectangle
import base.Wrapper.{FactUtil, DBWrapper}
import scala._
import scala.Predef._
import scala.util.Random


trait QueryConstructorTrait{
  def getLevel:Int
  def recalculateLevel
  def removeLevel
  def addLevel
  def getHelperItem: Node
  def returnBaseQuery: String
  def returnOperation: String
  def returnQuery:String
}

class QueryConstructor(query: String, helper: QueryConstructorHelper, sliceBox: SliceBox) extends VBox with QueryConstructorTrait{
  val baseQuery = new Label(query)
  val queryItemField = new VBox
  val execute = new Button("Execute query")

  execute.setOnAction(new EventHandler[ActionEvent] {
    def handle(p1: ActionEvent) {
      println("")

      def processHeadGraph(row: List[QueryConstructorTrait]): String = row match {
        case Nil          => ")"
        case head :: tail => head.returnOperation +"\n"+head.returnBaseQuery + "\n" + processHeadGraph(tail)
      }

      def processTailGraph(graph: List[List[QueryConstructorTrait]]):String = graph match{
        case Nil => ""
        case head :: tail => head match {
          case row => row.head.returnOperation +"\n("+row.head.returnBaseQuery +"\n"+processRow(row.tail) +"\n"+ processTailGraph(tail) +")"
        }
      }

      def processRow(row: List[QueryConstructorTrait]): String = row match {
        case Nil          => ""
        case head :: tail => head.returnOperation +"\n"+head.returnBaseQuery + "\n" + processRow(tail)
      }

      def processQuerySmallText(graph: List[List[QueryConstructorTrait]]):String = {
        def processHeadGraph(row: List[QueryConstructorTrait]): String = row match {
          case Nil          => ")"
          case head :: tail => head.returnQuery + " " + processHeadGraph(tail)
        }

        def processTailGraph(graph: List[List[QueryConstructorTrait]]):String = graph match{
          case Nil => " "
          case head :: tail => head match {
            case row => row.head.returnOperation +" ("+row.head.returnQuery +" "+processRow(row.tail) +" "+ processTailGraph(tail) +")"
          }
        }

        def processRow(row: List[QueryConstructorTrait]): String = row match {
          case Nil          => ""
          case head :: tail => head.returnOperation +" "+head.returnQuery + " " + processRow(tail)
        }
        "(" +processHeadGraph(graph.head) + " " + processTailGraph(graph.tail)
      }

      val query = "("+ processHeadGraph(baseGraph.head) + "\n" + processTailGraph(baseGraph.tail)
      val res = DBWrapper.customQuery(query)
      val weightedProcess = FactUtil.queryProcessOccurrences(res)
      Output.addText("\nData loaded")
      val (content, last) = SliceUtil.countWeights(weightedProcess, sliceBox.slider.getValue + 1)
      if(content.isEmpty) Output.addText("\nResult query is Empty")
      else {
        val rnd = new Random
        val names = rnd.alphanumeric.take(8).mkString
        val shortQuery = processQuerySmallText(baseGraph)
        sliceBox.addGraph(content,GlobalControls.workspace+"/",names,names, weightedProcess, last, shortQuery, GlobalControls.hideShortSQLDefault)
      }
    }
  })

  var baseGraph:List[List[QueryConstructorTrait]] = addGraphItem(Nil,this)

  helper.setExecuteBtn(execute)
  def setBaseGraphValue(graph: List[List[QueryConstructorTrait]]) {helper.refreshHelper(graph); baseGraph = graph}

  VBox.setVgrow(queryItemField, Priority.ALWAYS)

  this.getChildren.addAll(baseQuery, queryItemField)

  class queryItem(query: String) extends HBox with QueryConstructorTrait{
    var levelled = false
    val oper = new ComboBox[String]
    val queryField = new Label(query)
    val fieldBox = new VBox
    val controlBox = new VBox
    val levelBtn = new Label("->")
    val deleteBtn = new Label(" X")
    val leveller = new Rectangle(10,10)
    val marker = new Rectangle(10,10)
    marker.setOpacity(0)
    leveller.setOpacity(0)

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
    levelBtn.setOnMouseEntered(new EventHandler[MouseEvent] {
      def handle(p1: MouseEvent) {
        levelBtn.setUnderline(true)
      }
    })
    levelBtn.setOnMouseExited(new EventHandler[MouseEvent] {
      def handle(p1: MouseEvent) {
        levelBtn.setUnderline(false)
      }
    })
    levelBtn.setOnMouseClicked(new EventHandler[MouseEvent] {
      def handle(p1: MouseEvent) {
        if(!levelled) {
          addLevel
          levelBtn.setText(" <-")
        }
        else{
          removeLevel
          levelBtn.setText(" ->")
        }
        levelled = !levelled
        recalculateAllLevels(getLevel)
      }
    })

    levelBtn.setTextFill(Color.BLUE)
    levelBtn.setFont(GlobalControls.controlFont)
    deleteBtn.setTextFill(Color.BLUE)
    deleteBtn.setFont(GlobalControls.controlFont)

    controlBox.getChildren.addAll(levelBtn, deleteBtn)
    controlBox.setSpacing(3)
    fieldBox.getChildren.addAll(oper,queryField)

    oper.getItems.addAll("INTERSECT","UNION")
    oper.getSelectionModel.selectFirst()

    this.getChildren.addAll(fieldBox, controlBox)
    this.setSpacing(6)

    this.setRotate(180)
    fieldBox.setRotate(180)
    controlBox.setRotate(180)

    HBox.setHgrow(fieldBox,Priority.ALWAYS)
    VBox.setVgrow(levelBtn, Priority.ALWAYS)
    VBox.setVgrow(deleteBtn, Priority.ALWAYS)
    VBox.setVgrow(this, Priority.ALWAYS)

    def remove = deleteItem(this)
    def getLevel: Int = getElementLevel(baseGraph, this)

    def addLevel {
      this.getChildren.add(marker)
      addElementLevel(baseGraph, this)
    }
    def removeLevel {
      this.getChildren.removeAll(marker)
      removeElementLevel(baseGraph, this)
    }

    def recalculateLevel {
      val level = getLevel
      this.getChildren.removeAll(leveller)
      leveller.setWidth(10 * level)
      this.getChildren.add(leveller)
    }

    def getHelperItem: Node = {
      val rec = new Rectangle(15,15,Color.GRAY)
      rec.setOnMouseEntered(new EventHandler[MouseEvent] {
        def handle(p1: MouseEvent) {
          val top = new Tooltip(oper.getSelectionModel.getSelectedItem+"\n"+query)
          Tooltip.install(rec,top)
        }
      })
      rec
    }

    def returnBaseQuery: String = DBWrapper.returnBaseQuery(query)

    def returnOperation: String = oper.getSelectionModel.getSelectedItem

    def returnQuery: String = query
  }

  def recalculateAllLevels(from: Int) {
    baseGraph.zipWithIndex.filter(i => i._2 >= from).foreach(_._1.foreach(j => j.recalculateLevel))
  }

  def addElementLevel(graph: List[List[QueryConstructorTrait]], elem: QueryConstructorTrait) {
    val level = getElementLevel(graph,elem)
    val (head:List[QueryConstructorTrait], tail:List[QueryConstructorTrait]) = graph(level).splitAt(graph(level).indexOf(elem))
    val (resHead: List[List[QueryConstructorTrait]], resTail: List[List[QueryConstructorTrait]]) = graph.splitAt(level)
    setBaseGraphValue(clearBaseGraph((resHead ::: (head :: Nil)) ++ (tail :: resTail.tail)))
  }

  def removeElementLevel(graph: List[List[QueryConstructorTrait]], elem: QueryConstructorTrait) {
    val level = getElementLevel(graph, elem)
    val element: List[QueryConstructorTrait] = graph(level)
    val (resHead: List[List[QueryConstructorTrait]], resTail: List[List[QueryConstructorTrait]]) = graph.splitAt(level)
    setBaseGraphValue(clearBaseGraph(resHead.init ++ ((resHead.last ++ element) :: Nil) ++ resTail.tail))
  }

  def getElementLevel(graph : List[List[QueryConstructorTrait]], elem: QueryConstructorTrait): Int = {
    graph.zipWithIndex.map( i => if(i._1 contains elem) i._2 else -1).filter( _ != -1).head
  }

  def addItem(query: String) = {
    val qi = new queryItem(query)
    setBaseGraphValue(addGraphItem(baseGraph, qi))
    queryItemField.getChildren.add(qi)
    qi.recalculateLevel
  }

  def deleteItem(item: queryItem) = {
    val level = item.getLevel
    val isLevelled = item.levelled
    queryItemField.getChildren.removeAll(item)
    setBaseGraphValue(clearBaseGraph(baseGraph.map(i => i.filter( p => p != item))))
    val next= if(baseGraph.size > 1) baseGraph(level).head match {case i: queryItem => i.levelled case _ => true} else true
    if(isLevelled && !next) joinElements(baseGraph(level))
    recalculateAllLevels(level)

    def joinElements(row: List[QueryConstructorTrait]) {
      val (resHead, resTail) = baseGraph.splitAt(baseGraph.indexOf(row))
      setBaseGraphValue(clearBaseGraph(resHead.init ++ ((resHead.last ++ row) :: Nil) ++ resTail.tail))
    }
  }

  def clearBaseGraph(raw : List[List[QueryConstructorTrait]]): List[List[QueryConstructorTrait]] = raw match {
    case Nil => Nil
    case head :: tail => if(head.isEmpty) clearBaseGraph(tail) else head :: clearBaseGraph(tail)
  }

  def isLastLevelled:Boolean = {
    val size = queryItemField.getChildren.size()-1
    if(size < 0) false
      else {
      queryItemField.getChildren.get(size) match {
        case c:queryItem => c.levelled
      }
    }
  }

  def addGraphItem(graph: List[List[QueryConstructorTrait]], node: QueryConstructorTrait):List[List[QueryConstructorTrait]] = {
      val last: List[QueryConstructorTrait] = if(!graph.isEmpty) graph.last ::: node :: Nil else node :: Nil
      val init: List[List[QueryConstructorTrait]] = if(!graph.isEmpty) graph.init else Nil
      init ::: (last :: Nil)
  }

  def getGraph:List[List[QueryConstructorTrait]] = baseGraph

  def getLevel: Int = 0

  def recalculateLevel {}

  def removeLevel {}

  def addLevel {}

  def getHelperItem: Node = {
    val rec = new Rectangle(15,15,Color.GRAY)
    val tip = new Tooltip("Base Query")
    Tooltip.install(rec, tip)
    rec
  }

  def setSql(text: String){
    addItem(text)
  }

  def returnBaseQuery: String = DBWrapper.returnBaseQuery("")

  def returnOperation: String = ""

  def returnQuery: String = "Everything"
}

object QueryConstructor{
  def apply(query: String, helper: QueryConstructorHelper, slice: SliceBox) = new QueryConstructor(query, helper, slice)
}

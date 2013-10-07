package base.UI

import javafx.scene.layout.{Priority, HBox, VBox}
import javafx.scene.control._
import javafx.event.{ActionEvent, EventHandler}
import base.Wrapper.XESParser
import base.Util.{EventProperty, TraceProperty}
import javafx.scene.paint.Color
import javafx.scene.input.MouseEvent

object BaseLayout extends VBox{
  /*
     HBox = item1 | item2

     Vbox = item1
            _____
            item2
    */
  val topMainContentBox = new HBox
  val accordionAndOutputBox = new VBox
  val graphAndQueryBox = new VBox
  val output = Output
  val accordion = new Accordion
  val menu = new ToolBar
  val traceBox = new VBox
  val eventBox = new VBox
  val graphAndControlsBox = new HBox
  val queryChooserAndConstructorBox = new HBox
  val sliderBox = new VBox
  val slicesBox = new SliceBox(sliderBox)
  val sliceScroll = new ScrollPane
  val queryConstructorScroll = new ScrollPane
  val queryConstructorHelper = QueryConstructorHelper.apply
  val queryConstructor  = QueryConstructor("Base Query", queryConstructorHelper, slicesBox  )
  val queryChooser = QueryChooser(queryConstructor)
  val sideMenu = SideMenu
  val sideMenuCloseBtn = new Label("X")

  sideMenuCloseBtn.setTextFill(Color.BLUE)


  sideMenuCloseBtn.setFont(GlobalControls.controlFont)

  sliceScroll.setStyle(GlobalControls.boxStyle)
  accordion.setStyle(GlobalControls.boxStyle)
  sliderBox.setStyle(GlobalControls.boxStyle)

  topMainContentBox.setSpacing(1)
  topMainContentBox.setStyle(GlobalControls.boxStyle )
  accordionAndOutputBox.setSpacing(1)
  accordionAndOutputBox.setStyle(GlobalControls.boxStyle )
  queryChooserAndConstructorBox.setSpacing(1)
  queryChooserAndConstructorBox.setStyle(GlobalControls.boxStyle)
  graphAndQueryBox.setSpacing(1)
  graphAndQueryBox.setStyle(GlobalControls.boxStyle )

  sideMenu.setRemoveButton(sideMenuCloseBtn)

  sideMenuCloseBtn.setOnMouseEntered(new EventHandler[MouseEvent] {
    def handle(p1: MouseEvent) {
      sideMenuCloseBtn.setUnderline(true)
    }
  })
  sideMenuCloseBtn.setOnMouseExited(new EventHandler[MouseEvent] {
    def handle(p1: MouseEvent) {
      sideMenuCloseBtn.setUnderline(false)
    }
  })
  sideMenuCloseBtn.setOnMouseClicked(new EventHandler[MouseEvent] {
    def handle(p1: MouseEvent) {
      sideMenu.removeMainBox
    }
  })

  queryConstructorScroll.setContent(queryConstructor)
  //queryConstructorScroll.setFitToWidth(true)
  queryChooserAndConstructorBox.getChildren.addAll(queryChooser, queryConstructorScroll, queryConstructorHelper)
  sliceScroll.setContent(slicesBox)
  sliceScroll.setFitToWidth(true)
  accordion.getPanes.addAll(new TitledPane("Trace", traceBox), new TitledPane("Event", eventBox))
  graphAndControlsBox.getChildren.addAll(sliceScroll,sliderBox,sideMenu)
  graphAndQueryBox.getChildren.addAll(graphAndControlsBox, queryChooserAndConstructorBox)
  accordionAndOutputBox.getChildren.addAll(accordion, output,SmallBox)
  topMainContentBox.getChildren.addAll(accordionAndOutputBox,graphAndQueryBox)
  this.getChildren.addAll(menu,topMainContentBox)

  VBox.setVgrow(topMainContentBox, Priority.ALWAYS)
  VBox.setVgrow(accordion, Priority.ALWAYS)
  VBox.setVgrow(graphAndControlsBox, Priority.ALWAYS)

  HBox.setHgrow(queryConstructorScroll, Priority.ALWAYS)
  HBox.setHgrow(graphAndQueryBox, Priority.ALWAYS)
  HBox.setHgrow(sliceScroll, Priority.ALWAYS)

  def addTrace(traces: List[TraceProperty]) = {
    traceBox.getChildren.clear()
    traces.foreach(_.setChooser(queryChooser))
    traces.foreach( t => traceBox.getChildren.add(t.label))
  }
  def addEvent(events: List[EventProperty]) = {
    eventBox.getChildren.clear()
    events.foreach(_.setChooser(queryChooser))
    events.foreach( e => eventBox.getChildren.add(e.label))
  }
}
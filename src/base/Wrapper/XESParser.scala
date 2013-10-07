package base.Wrapper

import base.DWH._
import scala.xml.NodeSeq
import scala.slick.session.Database.threadLocalSession
import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.{StaticQuery => Q}
import java.text.SimpleDateFormat
import java.sql.Timestamp
import base.DWH.meta.META_Process
import base.UI._
import base.Util.EventProperty
import base.Util.TraceProperty
import base.DWH.EPropertySelector
import base.DWH.TPropertySelector


object XESParser {

  def tryConnection: Boolean = DBWrapper.tryConnection

  def tryMetaTables: Boolean = DBWrapper.tryMetaTables

  //def analyzeHeader: String = {}

  def analyzeExistingDWH:(List[TraceProperty], List[EventProperty]) = {
    Output.addText("Analyzing DWH...")
    if(tryConnection) {
      val tPInfo = DBWrapper.getTPropertyInfo
      val tNonExt = tPInfo.groupBy(_.key).map(f => (f._1,f._2.head.datatype,f._2.map(i => i.value))).filter(i => i._1 != "None").map(i => TraceProperty(i._1,i._2,i._3, false)).toList

      val (tnames, tmodelreferences) = DBWrapper.getTraceInfo
      val tname = TraceProperty("Name","string",tnames, true)
      val tmodel = TraceProperty("Model Reference","string",tmodelreferences , true)

      val ePInfo = DBWrapper.getEPropertyInfo
      val eNonExt = ePInfo.groupBy(_.key).map(f => (f._1,f._2.head.datatype,f._2.map(i => i.value))).filter(i => i._1 != "None").map(i => EventProperty(i._1,i._2,i._3, false)).toList

      val (enames, eresources, eroles, egroups, etransitions, etimestamps) = DBWrapper.getEventInfo
      val ename = EventProperty("Name","string",enames, true)
      val eresource = EventProperty("Resource","string",eresources, true)
      val erole = EventProperty("Role", "string", eroles, true)
      val egroup = EventProperty("Group", "string", egroups, true)
      val etransition = EventProperty("Transition","string", etransitions, true)
      val etimestamp = EventProperty("Timestamp","timestamp",etimestamps, true)

      Output.addText("Analyzing completed")
      ((tname :: tmodel :: Nil) ::: tNonExt) -> ((ename :: eresource :: erole :: egroup :: etransition :: etimestamp :: Nil) ::: eNonExt)
    }
    else{Output.addText("Analyzing failed") ;Nil -> Nil}
  }

  def processXESLog(path: String):Boolean = {
    if(tryConnection) {

      // Drop and Create tables in future DWH
      // TODO Add support for reusing old data if consistent (Add new data)

      // TODO Check is connection done
      //DBWrapper.tryConnection

      // TODO Check if there was previous connection with past data
      // Just a bunch of variables used for formatting dates
      // DAMN YOU ISO8601
      // and for initial log analysis
      try {
        if(path.isEmpty) throw new Error("No file selected")
        DBWrapper.dropAndCreateTables
        val log = scala.xml.XML.loadFile(path)
        if(log.isEmpty) throw new Error("Corrupted log file")
        val typeList = List ("string", "date", "int", "float", "boolean", "id")
        Output.addText("Analyzing XES file")

        val eventSize = (log \\ "event") size
        var counter = 0.0

        val startTime: Long = System.currentTimeMillis()
        val extensions = log \ "extension" map(x => ( x \ "@prefix" toString))
        val date = new java.util.Date()
        val sysdate = new Timestamp(date.getTime)
        val dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val traces = log \ "trace"
        var s_event: Map[String, Int] = Map()

        // Trace inspecting function is
        // searching for trace properties that are either included in extension list or not
        // Important! Pay attention that this it tail recursion function
        def inspectTrace(trace: NodeSeq, index: Int, eList: List[AEventProperty], eventIndex: Int): List[ATraceProperty] = {
          val id = index+1

          // These variables are used to fill in extension traces
          // Non extension traces are filled on the fly
          var name = "None"
          var modelReference = "None"

          // Variable for the FACT table - that maps the event sequence for each trace
          var process = ""

          // Basic recursive call stopper. If we have analyzed all records in given XML,
          // we return Nil and writing all gathered Event records in
          // tables, according to their types
          if(trace.isEmpty){ DBWrapper.writeEvents(eList) ; Nil}
          else {

            // Gather all trace related records and Event related records from XML
            val traceRecords = trace.head.nonEmptyChildren.filter(x => (x \\ "event" isEmpty) && !x.isAtom)
            val events = trace.head \ "event"

            // Inspect events, gathered from current trace
            lazy val props = inspectEvent(events,id, eventIndex)

            // Concatenate events acquired and past results, in order
            // to have full event collection in the end
            lazy val eventList = eList ::: props
            lazy val eventI = eventList.size


            // Function that inspects events.
            // It gathers events properties from the trace.
            def inspectEvent(events: NodeSeq, trace_id: Int, index: Int): List[AEventProperty] = {
              val event_id = index+1

              // These variables are used to fill in extension events
              // Non extension events are filled on the fly
              var name = "None"
              var resource = "None"
              var role = "None"
              var group = "None"
              var transition = "None"
              var timestamp = sysdate

              // Because this function also uses tail recursion, we use current statement as main recursion stopper
              // also when the event processing is finished, we have a record to make in FACE table with
              // all data for the Process column gathered
              if(events.isEmpty){ Fact(trace_id, process.substring(0, process.length -2)).writeToDB; Nil}
              else {

                // Take individual event records from the given event (first in the list)
                val eventRecords = events.head.nonEmptyChildren.filter(!_.isAtom)

                // Flag to indicate, weather there was a single event, that did not have
                // any non-extension properties. If there was not, we have do add "None" records to
                // non extension properties table (Or left join in SQL will return Nothing for such records)
                var hasProp = false

                // Nested recursive function to analyze each record in event
                def inspectEventRecords(record: NodeSeq): List[EProperty] = {
                  // Main stopper of tail recursion.
                  // It there is nothing left to analyze and no non-extension events were gathered
                  // we create an empty event, and return it with an empty list
                  if(record.isEmpty && !hasProp) EProperty(-1, event_id, "None", "None", "None") :: Nil

                  // Second main stopper
                  // If there was at least one non-extension event, we just return an empty list
                  else if (record.isEmpty) Nil

                  // Otherwise, there are records to analyze, and we check weather the given record
                  // is listed in extensions or not.
                  // If yes, then we cant return a valid Event object to write to database
                  // until we gather them all, so we pattern match them, to know, which particular
                  // extension is used.
                  else if(extensions.exists(x => (record.head \ "@key").toString().indexOf(x) != -1 )){

                    val key = record.head \ "@key" toString() split(":")
                    key(1) match {
                      case "name" => {
                        name = record.head \ "@value" toString()

                        // Populate process variable
                        // If the event is not in the list yet
                        // add a new event to the list and
                        // add the order number of this event into process with separator '->'.
                        if(!s_event.contains(name))
                          s_event += name -> s_event.size
                        process += s_event.get(name).get+"->"
                      }
                      case "resource" => resource = record.head \ "@value" toString()
                      case "role" => role = record.head \ "@value" toString()
                      case "group" => group = record.head \ "@value" toString()
                      case "transition" => transition = record.head \ "@value" toString()
                      case "timestamp" => timestamp =  new Timestamp(dateFormat.parse(record.head \ "@value" toString()).getTime)
                      case _ => println("Unexpected property: "+key(1))
                    }
                    // Because we have nothing to write yet, but the row is already analyzed,
                    // we just make next loop in our cycle
                    inspectEventRecords(record.tail)
                  }
                  // Else our row was not listed in extensions, and we add an element of event non-extensional properties.
                  // Also we pull the trigger hasProp - so that no 'None' values will be inserted.
                  // Finally, we use tail recursion, to process given records till very end.
                  else{hasProp = true; EProperty(-1,event_id,(record.head \ "@key").toString(), (record.head \ "@value").toString(), record.head.label) :: inspectEventRecords(record.tail)}
                }

                //Basic counter
                counter +=1
                if(counter == 74){
                  Output.addText(events.tail.head.toString())
                }
                SmallBox.setText(counter.toString)
                //ProgressSplash.progressValue.setValue( (counter/ eventSize) * 100 )

                // Each iteration of inspectEventRecords function analyzes single event in a single trace.
                // Because one event has only one record in Event table but can have more than one record in EProperties table we:
                // 1. Make a call, to inspect next event records in XML file
                // 2. After call is done we have a formed Event object, so we concatenate it to the result
                // 3. Finally we have to process next Event in trace, so we recursively call function with 'tail' as an argument
                inspectEventRecords(eventRecords) ::: Event(-1,event_id,trace_id,name,resource,role, group,transition, timestamp) :: inspectEvent(events.tail,trace_id,event_id)
              }
            }

            // Function inspects each record in given ONLY trace related XML records
            def inspectTraceRecords(record: NodeSeq): List[TProperty] = {

              // This function is recursive, so once again we need to have stopper
              // so if we have nothing to analyze, we return nothing
              if(record.isEmpty) TProperty(-1, id, "None", "None", "None") :: Nil

              // If we have something to analyze, we find weather this is an extensional object or a non-extensional property
              else if(extensions.exists(x => (record.head \ "@key").toString().indexOf(x) != -1)) {

                // We match keys for known extensions, and fill - in valued to make Trace object
                val key = record.head \ "@key" toString() split(":")
                key(1) match {
                  case "name" => name = record.head \ "@value" toString()
                  case "modelReference" => modelReference = record.head \ "@value" toString()
                  case _ => println("Unexpected property: "+key(1))
                }
                // Because we have nothing to return yet, we just iterate once again through recursive call
                inspectTraceRecords(record.tail)
              }
              // Else, we have a non-extension object to return, which we concatenate with recursive a call with the tail
              else TProperty(-1,id,(record.head \ "@key").toString(), (record.head \ "@value").toString(), "string") :: inspectTraceRecords(record.tail)
            }
            // Because after we have looped through inspectTraceRecords we have a finished Trace object
            // we can concatenate it with the result of inspectTraceRecords function and launch
            // inspectTrace tail recursion once more
            inspectTraceRecords(traceRecords) ::: Trace(-1,id,name,modelReference) :: inspectTrace(trace.tail,id, eventList, eventI)
          }
        }

        // Analyze log, and insert records in DWH
        DBWrapper.writeTrace(inspectTrace(traces, 0, Nil, 0))
        val endTime: Long = System.currentTimeMillis()
        Output.addText(((endTime - startTime) / 1000+" seconds taken"))
        //Initialize variable for retrieving events in FACT table
        s_event.map(_.swap).foreach(i => DBWrapper.db.withSession{META_Process.insert(i._1,i._2)})
        true
      } catch {
        case ex:Error => {
          Output.addText(ex.getMessage)
          false
        }
      }

    } else{ Output.addText("Log processing failed") ;false}
  }
}

// Supportive object to communicate with database
object DBWrapper {

  // Basic constant to call database
  var db:Database = null

  def setConnection(address: String, dbName: String, user: String, password: String) = db = Database.forURL("jdbc:postgresql://"+address+dbName, driver = "org.postgresql.Driver", user = user, password = password)

  def tryNewConnection(address: String, dbName: String, user: String, password: String):Boolean = {
    val newdb = Database.forURL("jdbc:postgresql://"+address+dbName, driver = "org.postgresql.Driver", user = user, password = password)
    try{
      newdb.withSession{
        val q = Q.queryNA[String]("""select 'Hello World' """)
        q.list.foreach(println)
      }
      true
    }
    catch {
      case e:Error =>{Output.addText(e.getMessage()); false}
      case _ => false
    }
  }

  // Drops and created tables in DWH
  def dropAndCreateTables{
    db.withSession {
      Q.updateNA("""DROP TABLE IF EXISTS "FACT" """).execute
      Q.updateNA("""DROP TABLE IF EXISTS "TRACE" """).execute
      Q.updateNA("""DROP TABLE IF EXISTS "TPROPERTY" """).execute
      Q.updateNA("""DROP TABLE IF EXISTS "EVENT" """).execute
      Q.updateNA("""DROP TABLE IF EXISTS "EPROPERTY" """).execute
      Q.updateNA("""DROP TABLE IF EXISTS "META_PROCESS" """).execute

      (Fact.ddl ++ Trace.ddl ++ TProperty.ddl ++ Event.ddl ++ EProperty.ddl ++ META_Process.ddl).create
      Output.addText("Tables created")
    }
  }

  // Does pattern matching and, using parallel calls inserts records to Trace and TProperty tables
  def writeTrace(traces: List[ATraceProperty]) {
    traces.par.foreach(x => db.withSession{
      x match {
        case t:Trace => Trace.autoInc.insert(t.trace_id,t.name,t.modelReference)
        case tp:TProperty => TProperty.autoInc.insert(tp.trace_id,tp.key,tp.value,tp.datatype)
      }
    })
  }

  // Does pattern matching and, using parallel calls inserts records to Event and EProperty tables
  def writeEvents(events: List[AEventProperty]) {
    val size = events.size

    if(size > 0){

      Output.addText("Writing to database")
      val startTime: Long = System.currentTimeMillis()
      db.withSession{
        events.head match{
          case e:Event => Event.autoInc.insert(e.event_id,e.trace_id,e.name,e.resource,e.role,e.group,e.transition,e.timestamp)
          case ep:EProperty => EProperty.autoInc.insert(ep.event_id, ep.key, ep.value, ep.datatype)
        }
      }
      val endTime: Long = System.currentTimeMillis()
      val tot = size * (endTime - startTime) / (500 * Runtime.getRuntime.availableProcessors())
      val text = if(tot < 60) tot + " seconds" else tot/60 + " minutes"

      Output.addText("For "+size+" records it will take about " +text)

      if(!events.tail.isEmpty) events.tail.par.foreach(x => db.withSession{
        x match {
          case e:Event => Event.autoInc.insert(e.event_id,e.trace_id,e.name,e.resource,e.role,e.group,e.transition,e.timestamp)
          case ep:EProperty => EProperty.autoInc.insert(ep.event_id, ep.key, ep.value, ep.datatype)
        }
      })
    }
  }

  def tryConnection:Boolean = {
    try{
      db.withSession{
        val q = Q.queryNA[String]("""select 'Hello World' """)
        q.list.foreach(println)
      }
      true
    }
    catch {
      case e:Exception => Output.addText(e.getMessage()); false
    }
  }

  /*SELECT "FACT"."TRACE_ID","FACT"."PROCESS" FROM "FACT", "EPROPERTY", "EVENT", "TRACE", "TPROPERTY"
    WHERE "EPROPERTY"."EVENT_ID" = "EVENT"."EVENT_ID"
    AND "TRACE"."TRACE_ID" = "EVENT"."TRACE_ID"
    AND "TRACE"."TRACE_ID" = "TPROPERTY"."TRACE_ID"
    AND "FACT"."TRACE_ID" = "TRACE"."TRACE_ID"
    AND "EPROPERTY"."KEY" = 'phoneType' AND "EPROPERTY"."VALUE" = 'T2'
    AND "EPROPERTY"."KEY" = "phoneType" AND "EPROPERTY"."VALUE" = 'T2'
    GROUP BY "FACT"."TRACE_ID", "FACT"."PROCESS";
  */

  def getTPropertyInfo:List[TPropertySelector] = {
    db.withSession{
     val q = Q.queryNA[(String,String,String)](
       """ select "TPROPERTY"."KEY", "TPROPERTY"."VALUE", "TPROPERTY"."DATATYPE"
            FROM "TPROPERTY" group by "TPROPERTY"."KEY", "TPROPERTY"."VALUE",
           "TPROPERTY"."DATATYPE" order by "TPROPERTY"."KEY", "TPROPERTY"."VALUE",
           "TPROPERTY"."DATATYPE" """)
      q.list.map(i => TPropertySelector(i._1,i._2,i._3))
    }
  }

  def getTraceInfo:(List[String],List[String]) = {
    db.withSession{
      val q1 = Q.queryNA[String](
        """ SELECT DISTINCT "TRACE"."NAME"
            FROM "TRACE"
        """)

      val q2 = Q.queryNA[String](
        """ SELECT DISTINCT "TRACE"."MODELREFERENCE"
            FROM "TRACE"
        """)

      q1.list -> q2.list
    }
  }

  def getEPropertyInfo:List[EPropertySelector] = {
    db.withSession{
     val q = Q.queryNA[(String,String,String)](
       """ select "EPROPERTY"."KEY", "EPROPERTY"."VALUE", "EPROPERTY"."DATATYPE"
         FROM "EPROPERTY" group by "EPROPERTY"."KEY", "EPROPERTY"."VALUE",
         "EPROPERTY"."DATATYPE" order by "EPROPERTY"."KEY", "EPROPERTY"."VALUE",
         "EPROPERTY"."DATATYPE"
        """)
     q.list.map(i => EPropertySelector(i._1,i._2,i._3))
    }
  }

  def getEventInfo:(List[String],List[String],List[String],List[String],List[String],List[String]) = {
    db.withSession{
      val q1 = Q.queryNA[String](
        """ SELECT DISTINCT "EVENT"."NAME"
            FROM "EVENT"
        """)
      val q2 = Q.queryNA[String](
        """ SELECT DISTINCT "EVENT"."RESOURCE"
            FROM "EVENT"
        """)
      val q3 = Q.queryNA[String](
        """ SELECT DISTINCT "EVENT"."ROLE"
            FROM "EVENT"
        """)
      val q4 = Q.queryNA[String](
        """ SELECT DISTINCT "EVENT"."GROUP"
            FROM "EVENT"
        """)
      val q5 = Q.queryNA[String](
        """ SELECT DISTINCT "EVENT"."TRANSITION"
            FROM "EVENT"
        """)
      val q6 = Q.queryNA[String](
        """ SELECT DISTINCT "EVENT"."TIMESTAMP"
            FROM "EVENT"
        """)
      (q1.list, q2.list, q3.list, q4.list, q5.list, q6.list)
    }
  }

  def returnBaseQuery(qpart:String):String = {
    val baseQuery =
      """SELECT "FACT"."TRACE_ID","FACT"."PROCESS"
        FROM "FACT", "EPROPERTY", "EVENT", "TRACE", "TPROPERTY"
        WHERE "EPROPERTY"."EVENT_ID" = "EVENT"."EVENT_ID"
        AND "TRACE"."TRACE_ID" = "EVENT"."TRACE_ID"
        AND "TRACE"."TRACE_ID" = "TPROPERTY"."TRACE_ID"
        AND "FACT"."TRACE_ID" = "TRACE"."TRACE_ID"
        ?
        GROUP BY "FACT"."TRACE_ID", "FACT"."PROCESS" """

    baseQuery.replaceAll("\\?",qpart)
  }

  def customQuery(sqlText: String):List[Fact] = {
    if(tryConnection){
      db.withSession{
        try {
          val q = Q.queryNA[Fact](sqlText)
          q.list
        }
        catch {
          case _ => List(Fact(-1,"Invalid query given"))
        }
      }
    } else {
      Output.addText("Failed to connect to Database")
      List(Fact(-1,"Failed to connect"))
    }
  }
  def returnBaseCase(qpart: String):List[Fact] = {
    /*
      SELECT * FROM "EVENT", "EPROPERTY"
      WHERE "EVENT"."EVENT_ID" = "EPROPERTY"."EVENT_ID"
      AND "EPROPERTY"."KEY" = 'defectFixed'
      AND "EPROPERTY"."VALUE" = 'false'
      AND "EVENT"."TRACE_ID" IN
      (SELECT "EVENT"."TRACE_ID" FROM "EVENT", "EPROPERTY"
      WHERE "EVENT"."EVENT_ID" = "EPROPERTY"."EVENT_ID"
      AND "EPROPERTY"."KEY" = 'phoneType'
      AND "EPROPERTY"."VALUE" = 'T2');

      SELECT "FACT"."TRACE_ID","FACT"."PROCESS" FROM "FACT", "EPROPERTY", "EVENT", "TRACE", "TPROPERTY"
        WHERE "EPROPERTY"."EVENT_ID" = "EVENT"."EVENT_ID"
        AND "TRACE"."TRACE_ID" = "EVENT"."TRACE_ID"
        AND "TRACE"."TRACE_ID" = "TPROPERTY"."TRACE_ID"
        AND "FACT"."TRACE_ID" = "TRACE"."TRACE_ID"
        AND "EPROPERTY"."KEY" = 'defectFixed'
        AND "EPROPERTY"."VALUE" = 'false'
        AND "EVENT"."TRACE_ID" IN
        (SELECT "EVENT"."TRACE_ID" FROM "EVENT", "EPROPERTY"
        WHERE "EVENT"."EVENT_ID" = "EPROPERTY"."EVENT_ID"
        AND "EPROPERTY"."KEY" = 'phoneType'
        AND "EPROPERTY"."VALUE" = 'T2')
        GROUP BY "FACT"."TRACE_ID", "FACT"."PROCESS";
    */
    if(tryConnection){
      db.withSession{
        //val qpart = "AND "EPROPERTY"."KEY" = 'phoneType' AND "EPROPERTY"."VALUE" = 'T2'"
        val baseQuery =
      """SELECT "FACT"."TRACE_ID","FACT"."PROCESS" FROM
        "FACT", "EPROPERTY", "EVENT", "TRACE", "TPROPERTY"
        WHERE "EPROPERTY"."EVENT_ID" = "EVENT"."EVENT_ID"
        AND "TRACE"."TRACE_ID" = "EVENT"."TRACE_ID"
        AND "TRACE"."TRACE_ID" = "TPROPERTY"."TRACE_ID"
        AND "FACT"."TRACE_ID" = "TRACE"."TRACE_ID"
        ?
        GROUP BY "FACT"."TRACE_ID", "FACT"."PROCESS" """

        val baseCorrectedQuery = baseQuery.replaceAll("\\?",qpart)

        try {
          val q = Q.queryNA[Fact](baseCorrectedQuery)
          q.list
        }
        catch{ case _ => List(Fact(-1,"Invalid query given"))}
      /*
       val q =for {
          fact <- Fact
          trace <- Trace if fact.trace_id === trace.trace_id
          tProperty <- TProperty if trace.trace_id === tProperty.trace_id
          event <- Event if trace.trace_id === event.trace_id
          eProperty <- EProperty if event.event_id === eProperty.event_id
        } yield (fact.* , trace.*, tProperty.*, event.*, eProperty.*)
        q.list
        */
      }
    } else {println("Failed to connect"); Nil}
  }

  def getMetaProcess:List[(Int,String)] = {
    if(tryConnection){
      db.withSession{
        val q = Q.queryNA[(Int, String)](
          """SELECT "META_PROCESS"."ID", "META_PROCESS"."NAME" FROM "META_PROCESS"
          """)
        q.list
      }
    } else Nil
  }

  def getBase(qpart: String): List[(Double,String)] = {
    FactUtil.countVerticalOccurrencesPercent(FactUtil.countOccurrencesPercent(FactUtil.countOccurrences(returnBaseCase(qpart))))
  }

  def tryMetaTables:Boolean = {
    try{
      db.withSession{


      }
      true
    }
    catch {case _ => false}
  }
}

object FactUtil {

  //def countProcessOccurrences(facts: List[Fact])

  def countOccurrences(facts: List[Fact]):List[(String,Int)] =  facts.groupBy(_.process).mapValues(_.size).toList.sortBy(_._2)

  def countOccurrencesPercent(occs: List[(String,Int)]): List[(Double, String)] = {
    val total:Double = occs.map(_._2).sum
    occs.map( i => (i._1, (i._2 / total) * 100.0)).map(i => i._2 ->  i._1)
  }

  def countVerticalOccurrencesPercent(occs: List[(Double,String)]): List[(Double,String)] = {
    def countVerticalOccurrencesPercent0(occs: List[(Double,String)], lastOcc: Double): List[(Double,String)] = occs match {
      case Nil => Nil
      case head :: tail => {
        val grow = head._1 + lastOcc
        (grow -> head._2) :: countVerticalOccurrencesPercent0(tail,grow)
      }
    }
    countVerticalOccurrencesPercent0(occs.reverse,0)
  }

  def queryFact(qpart:String): List[(Double,String)] = base.Wrapper.DBWrapper.getBase(qpart)

  def queryProcessOccurrences(facts: List[Fact]):List[(Double,String)] = FactUtil.countVerticalOccurrencesPercent(FactUtil.countOccurrencesPercent(FactUtil.countOccurrences(facts)))
}
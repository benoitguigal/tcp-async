package handler

import db.DB
import akka.util.ByteString
import com.github.mauricio.async.db.RowData
import java.util.Date
import akka.io.Tcp.Write
import akka.actor.{ Props, ActorRef }

object DbHandlerProps extends HandlerProps {
  def props(connection: ActorRef) = Props(classOf[DbHandler], connection)
}

class DbHandler(connection: ActorRef) extends Handler(connection) with DB {

  def system = context.system

  override implicit def dispatcher = context.dispatcher

  /**
   * Writes incoming message to database and returns all data in db to user
   * @return
   */
  def received(data: String) = {
    execute("INSERT INTO demo VALUES (?)", data + "--" + new Date).foreach(_ => printAll())
  }

  /**
   * Prints all data in db to user
   */
  def printAll() {
    respond("values in db are:")
    for {
      queryResult <- fetch("SELECT * FROM demo")
      resultSet <- queryResult
      rowData <- resultSet
      result = getData(rowData)
    } respond(result)
  }

  /**
   * Convert given data and send it to user
   * @param response: String
   */
  def respond(response: String) {
    connection ! Write(ByteString(response + "\n"))
  }

  def getData(rowData: RowData) = {
    rowData("data").asInstanceOf[String]
  }
}

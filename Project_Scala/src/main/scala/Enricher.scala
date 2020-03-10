import scala.io.Source
import java.io.{BufferedWriter, FileWriter}

object Enricher extends App {

  val trip: List[Trip] =
    Source.fromFile("/Users/mohammadyousefi/Downloads/gtfs_stm/trips.txt").getLines().toList.tail.map(_.split(",", -1))
    .map(p => Trip(p(0).toInt, p(1), p(2), p(3), p(4).toInt, p(5).toInt, p(6).toInt,
      if (p(7).isEmpty) None else Some(p(7)),
      if (p(8).isEmpty) None else Some(p(8))))

  val route: List[Route] =
    Source.fromFile("/Users/mohammadyousefi/Downloads/gtfs_stm/routes.txt").getLines().toList.tail.map(_.split(",", -1))
    .map(p => Route(p(0).toInt, p(1), p(2), p(3), p(4), p(5), p(6),p(7)))

  val calendar: List[Calendar] =
    Source.fromFile("/Users/mohammadyousefi/Downloads/gtfs_stm/calendar.txt").getLines().toList.tail.map(_.split(",", -1))
    .map(p => Calendar(p(0), p(1).toInt, p(2).toInt, p(3).toInt, p(4).toInt, p(5).toInt, p(6).toInt, p(7).toInt, p(8), p(9))
  )

  // Trip and Route Join by route_id
  val tripRoute: List[JoinOutput] =
    new MapJoin[Trip, Route]((i) => i.route_id.toString)((j) => j.route_id.toString).join(trip, route)

  // RouteTrip and Calendar Join by service_id
  val enrichedTrip =
    new LoopJoin[Calendar, JoinOutput]((i, j) => i.service_id == j.left.asInstanceOf[Trip].service_id).join(calendar, tripRoute)

  val output = enrichedTrip
    .map(joinOutput => {
      val t = Trip.tripSchema(joinOutput.right.getOrElse(" ").asInstanceOf[JoinOutput].left.asInstanceOf[Trip])
      val r = Route.routeSchema(joinOutput.right.getOrElse(" ").asInstanceOf[JoinOutput].right.getOrElse(" ").asInstanceOf[Route])
      val c = Calendar.calendarSchema(joinOutput.left.asInstanceOf[Calendar])
      t + "," + r + "," + c
    })
  // Print the result to  to CSV
  val Result = new BufferedWriter(new FileWriter("/Users/mohammadyousefi/Desktop/Result.csv"))
  for (i <- output) {
    Result.newLine()
    Result.write(i)
  }
}
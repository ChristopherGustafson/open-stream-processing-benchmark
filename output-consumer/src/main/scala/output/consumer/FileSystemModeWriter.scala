package output.consumer

import java.net.InetSocketAddress
import java.util.Properties
import java.util.concurrent.TimeUnit

import com.codahale.metrics.graphite.{Graphite, GraphiteReporter}
import com.codahale.metrics.jmx.JmxReporter
import com.codahale.metrics.{MetricFilter, MetricRegistry, Slf4jReporter}
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import play.api.libs.json.Json
import java.text.{DateFormat, SimpleDateFormat}

import scala.collection.JavaConverters._

/**
 * For local development!
 * - Reads data from Kafka continuously
 * - Updates latency histogram as data comes in
 * - Sends histograms to Graphite and console logs
 */
object FileSystemModeWriter {
  def run: Unit = {
    val logger = LoggerFactory.getLogger(this.getClass)
    val localConfigUtils = new LocalConfigUtils

        //properties of metric consumer
    val props = new Properties()
    props.put("bootstrap.servers", localConfigUtils.kafkaBootstrapServers)
    props.put("group.id", "output-consumer")
    props.put("enable.auto.commit", "true")
    props.put("auto.commit.interval.ms", "1000")
    props.put("session.timeout.ms", "30000")
    props.put("auto.offset.reset", "latest")
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")

    //metric consumer
    val consumer = new KafkaConsumer[String, String](props)

    //subscribe to metrics topic
    consumer.subscribe(java.util.Collections.singletonList("add-confirm"))

    while (true) {
      val metrics = consumer.poll(10000)
      for (metric <- metrics.asScala) {
        val kafkaPublishTimestampResult = metric.timestamp()
        val metricValue = Json.parse(metric.value())
        val publishTimestampInput = (metricValue \ "timestamp").as[String].toLong
        // val jobProfile = (metricValue \ "jobProfile").as[String]
        val durationPublishToPublish = kafkaPublishTimestampResult - publishTimestampInput
        println("Got value: " + metricValue)
        println("Time Duration: " + durationPublishToPublish + "\n")
        // registry.histogram("jobProfile").update(durationPublishToPublish)
      } 
    }
  }

  def getTime(timeString: String): Long = {
    // Date format of data
    val dateFormat: DateFormat = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss")
    dateFormat.parse(timeString).getTime
  }
}

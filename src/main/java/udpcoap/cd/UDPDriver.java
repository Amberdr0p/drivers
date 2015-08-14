package udpcoap.cd;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.io.IOUtils;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import semiot.library_driver.IDriver;
import semiot.library_driver.WAMPClient;

public class UDPDriver implements IDriver {

	private String driverName;
	private InetAddress addr;
	private int port;
	private String ip;
	private ScheduledFuture handle = null;
	private ScheduledExecutorService scheduler;
	private ScheduledSend scheduledSend;
	private static final Logger logger = LoggerFactory
			.getLogger(UDPDriver.class);
	private CoapClient coapClient;
	private static final ServiceConfig config = ConfigFactory
			.create(ServiceConfig.class);
	private String tempMess;

	private String template;

	private WAMPClient wampClient;

	public UDPDriver(String param) {

	}

	public UDPDriver() {

	}

	public static void main(String argv[]) {
		// UDPDriver driver = new UDPDriver();
		// driver.initialize();
		// driver.run();
	}

	public boolean initialize(WAMPClient wampClient) {
		this.wampClient = wampClient;
		// с конфиг файла приходит
		driverName = "UDPDriver";
		port = 5683;
		ip = "94.19.230.213";
		this.scheduler = Executors.newScheduledThreadPool(1);
		this.scheduledSend = new ScheduledSend();
		coapClient = new CoapClient(
				"coap://94.19.230.213:5683/dht22/temperature");
		try {
			this.template = IOUtils.toString(UDPDriver.class
					.getResourceAsStream( // переписать
					"/udpcoap/cd/templates/temperature.ttl"));

			tempMess = IOUtils.toString(UDPDriver.class.getResourceAsStream( // переписать
					"/udpcoap/cd/templates/tempermeter.ttl"));
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
		return true;
	}

	public void run() {
		/*
		 * System.out.println("-----"); System.out.println("---run---");
		 * System.out.println("-----");
		 */

		wampClient.publish(config.topicsNewAndObserving(),
				tempMess.replace("${HOST}", ip) // config.hostname())
						.replace("${PORT}", String.valueOf(port)));// String.valueOf(port));;

		start();

		/*
		 * System.out.println("-----"); Response response = cr.advanced();
		 * System.out.println(response.getPayloadString());
		 */
	}
	public void start() {
		if (this.handle != null)
			stop();

		int nDelay = 5;
		this.handle = this.scheduler.scheduleAtFixedRate(this.scheduledSend, 0,
				nDelay, SECONDS);
		logger.info("UScheduled started. Repeat will do every "
				+ String.valueOf(nDelay) + " seconds");
	}

	public void stop() {
		if (handle == null)
			return;

		handle.cancel(true);
		handle = null;
		logger.info("UScheduled stoped");
	}

	public String getDriverName() {
		return driverName;
	}

	private class ScheduledSend implements Runnable {
		public void run() {
			logger.info("ScheduledSend start");

			CoapResponse cr = coapClient.get();
			if (cr != null) {
				// topic из конфига
				String topic = "94.19.230.213.5683.meter.temperature.obs";
				wampClient.publish(
						topic,
						toTurtle(cr.getResponseText(),
								System.currentTimeMillis()));
			}
			logger.info("ScheduledSend complete");
		}
	}

	private String toTurtle(String respText, long timestamp) {
		final String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
				.format(new Date(timestamp));
		return template.replace("${HOST}", ip)
				.replace("${PORT}", String.valueOf(port))
				.replace("${TIMESTAMP}", String.valueOf(timestamp))
				.replace("${DATETIME}", date).replace("${VALUE}", respText);
	}

}

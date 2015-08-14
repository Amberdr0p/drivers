package udpcoap.cd;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.DefaultValue;
import org.aeonbits.owner.Config.Key;

@Config.LoadPolicy(Config.LoadType.FIRST)
@Config.Sources({ "file:/semiot-platform/device-proxy-service/config.properties" })
public interface ServiceConfig extends Config {

	@DefaultValue("3131")
	@Key("services.deviceproxy.port")
	int port();

	@DefaultValue("ws://demo-dev.semiot.ru/ws")
	@Key("services.wamp.uri")
	String wampUri();

	@DefaultValue("realm1")
	@Key("services.wamp.realm")
	String wampRealm();

	@DefaultValue("15")
	// seconds
	@Key("services.wamp.reconnect")
	int wampReconnectInterval();

	@DefaultValue("TURTLE")
	@Key("services.wamp.message.format")
	String wampMessageFormat();
	
	@DefaultValue("ru.semiot.devices.newandobserving")
	@Key("services.topics.newandobserving")
	String topicsNewAndObserving();
}

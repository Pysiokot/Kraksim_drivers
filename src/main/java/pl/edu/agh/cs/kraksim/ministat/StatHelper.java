package pl.edu.agh.cs.kraksim.ministat;

import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.core.Gateway;
import pl.edu.agh.cs.kraksim.main.drivers.Driver;

import java.util.HashMap;
import java.util.Map;

final class StatHelper {
	private static final Logger LOGGER = Logger.getLogger(StatHelper.class);
	private final Map<Object, TravelDetails> tdMap;

	private int cityCarCount;
	private int emergencyVehiclesCount;
	private int normalCarsCount;
	private int cityTravelCount;
	private float cityTravelLength;
	private float cityTravelDuration;
	private float emergencyVehiclesTravelLength;
	private float emergencyVehiclesTravelDuration;
	private float normalCarsTravelLength;
	private float normalCarsTravelDuration;

	private float cityAvgCarSpeed;

	StatHelper() {
		tdMap = new HashMap<>();
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("StatHelper init<> ");
		}
	}

	void clear() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(" ");
		}

		tdMap.clear();
		cityCarCount = 0;
		emergencyVehiclesCount=0;
		normalCarsCount = 0;
		cityTravelCount = 0;
		cityTravelLength = 0.0f;
		cityTravelDuration = 0.0f;
		emergencyVehiclesTravelLength = 0.0f;
		emergencyVehiclesTravelDuration = 0.0f;
		normalCarsTravelLength = 0.0f;
		normalCarsTravelDuration = 0.0f;
		cityAvgCarSpeed = 0.0f;
	}

	void beginTravel(Object driver, GatewayMiniStatExt entranceGateway, int turn) {
		LOGGER.trace("Trip: " + driver + ", start time=" + turn);
		Driver d = (Driver) driver;
		boolean emergency = d.isEmergency();
		cityCarCount++;
		if (emergency) {
			emergencyVehiclesCount++;
		} else {
			normalCarsCount++;
		}
		tdMap.put(driver, new TravelDetails(entranceGateway, turn));
	}

	void incTravelLength(Object driver, int delta) {
		LOGGER.trace("difference=" + delta);
		tdMap.get(driver).length += delta;
	}

	TravelDetails endTravel(Object driver, Gateway exitGateway, int turn) {
		TravelDetails td = tdMap.remove(driver);
		Driver d = (Driver) driver;
		boolean emergency = d.isEmergency();
		cityCarCount--;
		if (emergency) {
			emergencyVehiclesCount--;
		} else {
			normalCarsCount--;
		}

		int duration = turn - td.entranceTurn;

		td.entranceGateway.noteTravel(exitGateway, td.length, duration);

		cityTravelCount++;
		cityTravelLength += td.length;
		cityTravelDuration += duration;
		if (emergency) {
			emergencyVehiclesTravelLength += td.length;
			emergencyVehiclesTravelDuration += duration;
		} else {
			normalCarsTravelLength += td.length;
			normalCarsTravelDuration += duration;
		}

		LOGGER.trace("Trip: " + driver + ", len=" + td.length + ", dur=" + duration);

		cityAvgCarSpeed += td.length / duration;
		if (cityAvgCarSpeed != 0) {
			cityAvgCarSpeed /= 2;
		}

		return td;
	}

	float getCityAvgCarSpeed() {
		return cityAvgCarSpeed;
	}

	int getCityCarCount() {
		return cityCarCount;
	}

	int getEmergencyVehiclesCount() {
		return emergencyVehiclesCount;
	}

	int getNormalCarsCount() {
		return normalCarsCount;
	}

	int getCityTravelCount() {
		return cityTravelCount;
	}

	float getCityAvgVelocity() {
		return cityTravelDuration > 0.0f ? cityTravelLength / cityTravelDuration : 0.0f;
	}

	float getAvgEmergencyVehiclesVelocity() {
		return emergencyVehiclesTravelDuration > 0.0f ? emergencyVehiclesTravelLength / emergencyVehiclesTravelDuration : 0.0f;
	}

	float getAvgNormalCarsVelocity() {
		return normalCarsTravelDuration > 0.0f ? normalCarsTravelLength / normalCarsTravelDuration : 0.0f;
	}

	float getCityTravelLength() {
		return cityTravelLength;
	}

	float getEmergencyVehiclesTravelLength() {
		return emergencyVehiclesTravelLength;
	}

	float getNormalCarsTravelLength() {
		return normalCarsTravelLength;
	}

	float getCityTravelDuration() {
		return cityTravelDuration;
	}

	float getEmergencyVehiclesTravelDuration() {
		return emergencyVehiclesTravelDuration;
	}

	float getNormalCarsTravelDuration() {
		return normalCarsTravelDuration;
	}

	static final class TravelDetails {
		private final GatewayMiniStatExt entranceGateway;
		private final int entranceTurn;
		private int length;

		TravelDetails(GatewayMiniStatExt entranceGateway, int entranceTurn) {
			this.entranceGateway = entranceGateway;
			this.entranceTurn = entranceTurn;
		}
	}
}

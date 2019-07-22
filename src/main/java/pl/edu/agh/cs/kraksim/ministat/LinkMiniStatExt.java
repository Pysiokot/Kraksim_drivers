package pl.edu.agh.cs.kraksim.ministat;

import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.iface.Clock;
import pl.edu.agh.cs.kraksim.iface.mon.CarDriveHandler;
import pl.edu.agh.cs.kraksim.iface.mon.LinkMonIface;
import pl.edu.agh.cs.kraksim.iface.mon.MonIView;
import pl.edu.agh.cs.kraksim.main.drivers.Driver;

import java.util.HashMap;
import java.util.Map;

public class LinkMiniStatExt {
	private static final Logger LOGGER = Logger.getLogger(LinkMiniStatExt.class);
	private final Map<Object, Integer> entranceTurnMap;
	private final LastPeriodAvgDuration lastPeriodAvgDuration;
	private final LastPeriodAvgVelocity lastPeriodAvgVelocity;
	private final LastPeriodCarCount lastPeriodCarOutCount;
	private final LastPeriodCarCount lastPeriodCarInCount;
	private final Link link;
	private int carCount;
	private int emergencyVehiclesCount;
	private int normalCarsCount;
	private int driveCount;
	private long carOnRedLight;
	private double totalDriveLength;
	private double totalDriveDuration;
	private float s;
	private double lastLastPeriodAvgDuration;
	/**
	 * prędkość samochodów nie licząc stania na czerwonym świetle
	 */
	private double avarageRidingVelocity = 0.0;
	/**
	 * prędkość samochodów licząc stanie na czerwonym
	 */
	private double avarageVelocity = 0.0;

	LinkMiniStatExt(final Link link, MonIView monView, final Clock clock, final StatHelper helper) {
		LOGGER.trace("LinkMiniStatExt init() " + link);
		entranceTurnMap = new HashMap<>();
		this.link = link;

		lastPeriodAvgDuration = new LastPeriodAvgDuration();
		lastPeriodAvgVelocity = new LastPeriodAvgVelocity(link);
		lastPeriodCarOutCount = new LastPeriodCarCount();
		lastPeriodCarInCount = new LastPeriodCarCount();

		LinkMonIface l = monView.ext(link);
		l.installInductionLoops(0, new CarDriveHandler() {
			public void handleCarDrive(int velocity, Object driver) {
				Driver d = (Driver) driver;
				boolean emergency = d.isEmergency();
				carCount++;
				if (emergency) {
					emergencyVehiclesCount++;
				} else {
					normalCarsCount++;
				}
				lastPeriodCarInCount.update();
				entranceTurnMap.put(driver, clock.getTurn());
				System.out.println("IND:: LinkMiniStatExt ++ " + link.getId());
			}
		});

		l.installInductionLoops(link.getLength(), new CarDriveHandler() {
			public void handleCarDrive(int velocity, Object driver) {
				Driver d = (Driver) driver;
				boolean emergency = d.isEmergency();
				carCount--;
				if (emergency) {
					emergencyVehiclesCount--;
				} else {
					normalCarsCount--;
				}
				int length = link.getLength();
				Integer tmp = entranceTurnMap.remove(driver);
				int duration = clock.getTurn() - ((tmp == null) ? (0) : (tmp));

				helper.incTravelLength(driver, length);

				driveCount++;
				totalDriveLength += length;
				totalDriveDuration += duration;
				lastPeriodAvgDuration.update(duration);
				lastPeriodAvgVelocity.update(duration);
				lastPeriodCarOutCount.update();

				s += duration * duration;
				System.out.println("IND:: LinkMiniStatExt ++ " + link.getId());
			}
		});
		//link.calculateWeight(carCount);
	}

	void clear() {
		LOGGER.trace("LinkMiniStatExt clear() ");
		entranceTurnMap.clear();
		carCount = 0;
		emergencyVehiclesCount = 0;
		normalCarsCount = 0;
		driveCount = 0;
		totalDriveLength = 0.0f;
		totalDriveDuration = 0.0f;
		s = 0.0f;
	}

	public int getCarCount() {
		return carCount;
	}

	public int getEmergencyVehiclesCount() {
		return emergencyVehiclesCount;
	}

	public int getNormalCarsCount() {
		return normalCarsCount;
	}

	public int getDriveCount() {
		return driveCount;
	}

	public float getAvgVelocity() {
		return (float) avarageVelocity;
	}

	public float getAvgDuration() {
		return driveCount > 0 ? (float) (totalDriveDuration / driveCount) : 0.0f;
	}

	public float getLastPeriodAvgDuration() {
		float avgDuration = lastPeriodAvgDuration.getLastPeriodAvgDuration();
		lastLastPeriodAvgDuration = avgDuration;
		return avgDuration;
	}

	/**
	 * Zwraca średnią prędkość za ostatni okres czasu lub -1.0f jeśli
	 * żaden pojazd nie wyjechał
	 *
	 * @return
	 */
	public double getLastLastPeriodAvgVelocity() {
		if (lastLastPeriodAvgDuration == 0.0) {
			return -1.0f;
		}
		return link.getLength() / lastLastPeriodAvgDuration;
	}

	public float getLastPeriodAvgVelocity() {
		return lastPeriodAvgVelocity.getLastPeriodAvgVelocity();
	}

	public int getLastPeriodCarOutCount() {
		return lastPeriodCarOutCount.getLastPeriodCarCount();
	}

	public int getLastPeriodCarInCount() {
		return lastPeriodCarInCount.getLastPeriodCarCount();
	}

	public float getStdDevDuration() {
		if (driveCount > 1) {
			return (float) Math.sqrt((s - totalDriveDuration / driveCount * totalDriveDuration) / (driveCount - 1));
		} else {
			return 0.0f;
		}
	}

	/**
	 * zwraca liczbę samochodów stojących na czerwonym świetle
	 * liczba ta jest obliczana w module @see pl.edu.agh.cs.kraksim.main.StatsUtil
	 *
	 * @return
	 */
	public long getCarCountOnRedLigth() {
		return carOnRedLight;
	}

	public void setCarCountOnRedLigth(long carCountOnRedLight) {
		carOnRedLight = carCountOnRedLight;
	}

	/**
	 * Prędkość średnia nie licząc samochodów na czerwonym świetle
	 *
	 * @return
	 */
	public double getAvarageRidingVelocity() {
		return avarageRidingVelocity;
	}

	public void setAvarageRidingVelocity(Double avarageRidingVelocity) {
		this.avarageRidingVelocity = avarageRidingVelocity;
	}

	public void setAvarageVolocity(Double avaraVelocity) {
		avarageVelocity = avaraVelocity;
	}
}

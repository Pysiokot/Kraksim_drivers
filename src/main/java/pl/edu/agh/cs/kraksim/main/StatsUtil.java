package pl.edu.agh.cs.kraksim.main;

import pl.edu.agh.cs.kraksim.core.City;
import pl.edu.agh.cs.kraksim.core.Gateway;
import pl.edu.agh.cs.kraksim.core.Lane;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.iface.block.BlockIView;
import pl.edu.agh.cs.kraksim.iface.block.LaneBlockIface;
import pl.edu.agh.cs.kraksim.iface.carinfo.CarInfoCursor;
import pl.edu.agh.cs.kraksim.iface.carinfo.CarInfoIView;
import pl.edu.agh.cs.kraksim.iface.carinfo.LaneCarInfoIface;
import pl.edu.agh.cs.kraksim.main.drivers.Driver;
import pl.edu.agh.cs.kraksim.ministat.*;
import pl.edu.agh.cs.kraksim.real.RealSimulationParams;

import java.io.PrintWriter;
import java.util.*;

public class StatsUtil {
	static final int dumpStatsInterval = 60;
	static final int dumpLinkStatsInterval = 300;

	/**
	 * Używana do liczenia średnich prędkości z pominięciem pojazdów
	 * czekających w kolejce na czerwonym świetle
	 *
	 * @author Krzysztof Kot
	 */
	public static class LinkStat {
		private final Map<Link, Long> currentLinkDriveLength;
		private final Map<Link, Long> currentLinkMovementCount;
		private final Map<Link, List<Double>> linksAvgVelocity;
		private final Map<Link, List<Integer>> linksCarCount;
		private final Map<Link, Integer> currentLinkCarCount;

		public LinkStat() {
			currentLinkDriveLength = new HashMap<>();
			currentLinkMovementCount = new HashMap<>();
			linksAvgVelocity = new HashMap<>();
			linksCarCount = new HashMap<>();
			currentLinkCarCount = new HashMap<>();
		}

		public void increaseLinkDriveLength(Link link, long length) {
			long currentLength = getLinkDriveLength(link);
			currentLinkDriveLength.put(link, currentLength + length);
		}

		public void increaseLinkMovementCount(Link link, long count) {
			long carCount = getLinkMovementCount(link);
			currentLinkMovementCount.put(link, carCount + count);
		}

		public double getAvgVelocity(Link link) {
			long movementCount = getLinkMovementCount(link);
			long currentLength = getLinkDriveLength(link);
			return ((double) currentLength) / movementCount;
		}

		public void addAvgVelocityToList(Link link, Double avgVelocity) {
			List<Double> linkAvgVelocity = linksAvgVelocity.get(link);
			if (linkAvgVelocity == null) {
				linkAvgVelocity = new LinkedList<>();
			}
			linkAvgVelocity.add(avgVelocity);
			linksAvgVelocity.put(link, linkAvgVelocity);
		}

		public void setCurrentCarCount(Link link, Integer carCount) {
			currentLinkCarCount.put(link, carCount);
		}

		public int getCurrentCarCount(Link link) {
			return (currentLinkCarCount.get(link) != null) ? currentLinkCarCount.get(link) : 0;
		}

		public void addCarCountToList(Link link, Integer carCount) {
			List<Integer> linkCarCountList = linksCarCount.get(link);
			if (linkCarCountList == null) {
				linkCarCountList = new ArrayList<>();
			}
			linkCarCountList.add(carCount);
			linksCarCount.put(link, linkCarCountList);
			link.calculateWeight((double) carCount);
		}

		public List<Double> getAvgVelocitiesList(Link link) {
			return linksAvgVelocity.get(link);
		}

		public List<Integer> getCarCountList(Link link) {
			return linksCarCount.get(link);
		}

		public long getLinkDriveLength(Link link) {
			return (currentLinkDriveLength.get(link) != null) ? currentLinkDriveLength.get(link) : 0;
		}

		public long getLinkMovementCount(Link link) {
			return (currentLinkMovementCount.get(link) != null) ? currentLinkMovementCount.get(link) : 0;
		}

		public void clearLink(Link link) {
			currentLinkDriveLength.remove(link);
			currentLinkMovementCount.remove(link);
		}
	}

	public static void statHeader(final City city, final PrintWriter statWriter) {
		// statWriter.print( "turn " );
		for (Iterator<Link> i = city.linkIterator(); i.hasNext(); ) {
			Link link = i.next();
			statWriter.print(link.getId() + ' ');
		}

		statWriter.print("#of_travels #of_cars avg_velocity");
		statWriter.println(",");
	}

	public static void dumpCarStats(final City city, final MiniStatEView statView, int turn, final PrintWriter statWriter) {
		// statWriter.print( turn );
		if ((turn % dumpStatsInterval) == 0) {
			for (Iterator<Link> i = city.linkIterator(); i.hasNext(); ) {
				Link link = i.next();
				LinkMiniStatExt linkExt = statView.ext(link);

				statWriter.print(linkExt.getDriveCount() + " ");
			}

			statWriter.print(statView.ext(city).getTravelCount() + " " + statView.ext(city).getCarCount() + ' ' + statView.ext(city).getAvgVelocity());
			statWriter.println("");
		}
	}

	public static void dumpLinkStats(final City city, final PrintWriter statWriter, LinkStat linkStat, LinkStat linkRidingStat) {


		for (Iterator<Link> i = city.linkIterator(); i.hasNext(); ) {
			Link link = i.next();

			statWriter.print(String.format("<link from='%s' to='%s'>\n", link.getBeginning().getId(), link.getEnd().getId()));
			List<Double> avgVelocities = linkStat.getAvgVelocitiesList(link);
			List<Double> avgRidingVelocities = linkRidingStat.getAvgVelocitiesList(link);
			List<Integer> carCounts = linkStat.getCarCountList(link);
			assert avgVelocities.size() == avgRidingVelocities.size();

			int period = 0;
			for (int j = 0; j < avgVelocities.size(); j++) {
				double avgVelocity = avgVelocities.get(j);
				double avgRidingVelocity = avgRidingVelocities.get(j);
				int carCount = carCounts.get(j);
				int begin = period * dumpLinkStatsInterval;
				int end = (period + 1) * dumpLinkStatsInterval;
				statWriter.print(String.format("\t<period begin='%d' end='%d' avg_velocity='%.4f' avg_riding_velocity='%.4f' carCount='%d'>\n", begin, end, avgVelocity, avgRidingVelocity, carCount));
				period++;
			}
			statWriter.print("</link>\n");
		}
	}

	/**
	 * @param city
	 * @param carInfoView
	 * @param turn
	 * @param linkStat       średnia predkosc dla wszystkich pojazdów
	 * @param linkRidingStat średnia prędkość statystyki dla wszystkich pojazdów
	 *                       oprócz tych stojących na czerwnoym świetle
	 */
	public static void collectLinkStats(final City city, final CarInfoIView carInfoView, final BlockIView blockView, final MiniStatEView statView, int turn, LinkStat linkStat, LinkStat linkRidingStat) {
		CityMiniStatExt cityMiniStat = statView.ext(city);
		long allCarsOnRedLigth = 0;
		long emergencyVehiclesOnRedLight = 0;
		long normalCarsOnRedLight = 0;
		for (Iterator<Link> i = city.linkIterator(); i.hasNext(); ) {
			Link link = i.next();
			LinkMiniStatExt linkMiniStatExt = statView.ext(link);

			// carStat
			if ((turn % dumpLinkStatsInterval) == 0) {
				int oldCarCount = linkStat.getCurrentCarCount(link);
				int newCarCount = statView.ext(link).getDriveCount();
				linkStat.addCarCountToList(link, newCarCount - oldCarCount);
				linkStat.setCurrentCarCount(link, newCarCount);
			}

			// cos
			long linkDriveLength = 0;
			long linkMovementCount = 0;

			long linkRidingDriveLength = 0;
			long linkRidingMovementCount = 0;
			long emergencyVehiclesLinkRidingMovementCount = 0;
			long normalCarsLinkRidingMovementCount = 0;

			for (int lineNum = 0; lineNum < link.laneCount(); lineNum++) {
				Lane lane = link.getLaneAbs(lineNum);
				LaneCarInfoIface laneCarInfo = carInfoView.ext(lane);
				LaneBlockIface laneBlock = blockView.ext(lane);

				// Liczenie zwykłej średniej prędkości.
				CarInfoCursor infoForwardCursor = laneCarInfo.carInfoForwardCursor();
				while (infoForwardCursor != null && infoForwardCursor.isValid()) {
					try {
						linkDriveLength += infoForwardCursor.currentVelocity();
						linkMovementCount++;
					} catch (NoSuchElementException e) {
						e.printStackTrace();
					}
					infoForwardCursor.next();
				}

				// Żeby policzyć średnią prędkość bez stania na światłach.
				CarInfoCursor infoBackwardCursor = laneCarInfo.carInfoBackwardCursor();
				int laneLength = lane.getLength();

				boolean lastInLane = true;
				boolean skipingMode = true;
				int lastPos = -1;

				while (infoBackwardCursor != null && infoBackwardCursor.isValid()) {
					try {
						int currentPos = infoBackwardCursor.currentPos();
						int currentVelocity = infoBackwardCursor.currentVelocity();

						// jak jest zielone to liczymy prędkość dla wszystkich
						if (!laneBlock.isBlocked()) {
							lastInLane = false;
							skipingMode = false;
						}

						// wywoływane tylko dla ostatniego pojazdu
						if (lastInLane) {
							if (currentPos != (laneLength - 1)) {
								skipingMode = false;
							}
							lastInLane = false;
						}
						// sprawdzam czy ten pojazd też mogę ignorować
						else if (skipingMode && (currentPos != (lastPos - 1) || currentVelocity > 0)) {
							skipingMode = false;
						}

						if (!skipingMode) {
							Driver d = (Driver) infoBackwardCursor.currentDriver();
							boolean emergency = d.isEmergency();
							linkRidingDriveLength += infoBackwardCursor.currentVelocity();
							linkRidingMovementCount++;
							if (emergency) {
								emergencyVehiclesLinkRidingMovementCount++;
							} else {
								normalCarsLinkRidingMovementCount++;
							}
						}
						lastPos = currentPos;
					} catch (NoSuchElementException e) {
						e.printStackTrace();
					}
					infoBackwardCursor.next();
				}
			}
			linkStat.increaseLinkDriveLength(link, linkDriveLength);
			linkStat.increaseLinkMovementCount(link, linkMovementCount);

			linkRidingStat.increaseLinkDriveLength(link, linkRidingDriveLength);
			linkRidingStat.increaseLinkMovementCount(link, linkRidingMovementCount);

			if ((turn % dumpLinkStatsInterval) == 0) {

				Double linkAvgVelocity = linkStat.getAvgVelocity(link);
				linkStat.addAvgVelocityToList(link, linkAvgVelocity);
				linkStat.clearLink(link);

				Double linkRidingAvgVelocity = linkRidingStat.getAvgVelocity(link);

				linkRidingStat.addAvgVelocityToList(link, linkRidingAvgVelocity);
				linkRidingStat.clearLink(link);

				assert (linkRidingAvgVelocity < linkAvgVelocity);
			}

			Double avarageVolocity = linkStat.getAvgVelocity(link);
			linkMiniStatExt.setAvarageVolocity(avarageVolocity == null ? Double.valueOf(0) : avarageVolocity);
			Double linkRidingAvgVelocity = linkRidingStat.getAvgVelocity(link);
			linkMiniStatExt.setAvarageRidingVelocity(linkRidingAvgVelocity == null ? Double.valueOf(0) : linkRidingAvgVelocity);

			long carOnRedLightInLink = linkMiniStatExt.getCarCount() - linkRidingMovementCount;
			long emergencyVehiclesOrRedLightInLink = linkMiniStatExt.getEmergencyVehiclesCount() - emergencyVehiclesLinkRidingMovementCount;
			long normalCarsOrRedLightInLink = linkMiniStatExt.getNormalCarsCount() - normalCarsLinkRidingMovementCount;
			linkMiniStatExt.setCarCountOnRedLigth(carOnRedLightInLink);
			allCarsOnRedLigth += carOnRedLightInLink;
			emergencyVehiclesOnRedLight += emergencyVehiclesOrRedLightInLink;
			normalCarsOnRedLight += normalCarsOrRedLightInLink;
		}
		
		cityMiniStat.setAllCarsOnRedLight(allCarsOnRedLigth);
		cityMiniStat.setEmergencyVehiclesOnRedLight(emergencyVehiclesOnRedLight);
		cityMiniStat.setNormalCarsOnRedLight(normalCarsOnRedLight);
		
	}

	public static void dumpStats(final City city, final MiniStatEView statView, final int turn, final PrintWriter writer) {
		writer.println("CITY STATS");
		writer.println("==========");
		writer.printf("%15s %15s\n", "sim. duration", "avg. velocity");
		writer.printf("%15d %15.2f\n", turn, statView.ext(city).getAvgVelocity());

		writer.println();

		writer.println("ROUTE STATS");
		writer.println("===========");
		writer.printf("%6s %6s %6s %15s %15s %15s %15s\n", "from", "to", "count", "avg. duration", "<-std dev.", "avg. velocity", "<-[kph]");
		for (Iterator<Gateway> i1 = city.gatewayIterator(); i1.hasNext(); ) {
			Gateway g1 = i1.next();
			GatewayMiniStatExt g1ext = statView.ext(g1);
			for (Iterator<Gateway> i2 = city.gatewayIterator(); i2.hasNext(); ) {
				Gateway g2 = i2.next();
				RouteStat rs = g1ext.getRouteStat(g2);
				if (rs != null && rs.getTravelCount() > 0) {
					float v = rs.getAvgVelocity();
					float vkph = RealSimulationParams.convertToKPH(v);
					writer.printf("%6s %6s %6d %15.1f %15.1f %15.2f %15.1f\n", g1.getId(), g2.getId(), rs.getTravelCount(), rs.getAvgDuration(), rs.getStdDevDuration(), v, vkph);
				}
			}
		}
		
		writer.println();

		writer.println("LINK STATS");
		writer.println("==========");
		writer.printf("%6s %6s %6s %15s %15s %15s %15s\n", "from", "to", "count", "avg. duration", "<-std dev.", "avg. velocity", "<-[kph]");
		for (Iterator<Link> i = city.linkIterator(); i.hasNext(); ) {
			Link link = i.next();
			LinkMiniStatExt linkExt = statView.ext(link);
			if (linkExt.getDriveCount() > 0) {
				float v = linkExt.getAvgVelocity();
				float vkph = RealSimulationParams.convertToKPH(v);
				writer.printf("%6s %6s %6d %15.1f %15.1f % 15.2f %15.1f\n", link.getBeginning().getId(), link.getEnd().getId(), linkExt.getDriveCount(), linkExt.getAvgDuration(), linkExt.getStdDevDuration(), v, vkph);
			}
		}
	}
}

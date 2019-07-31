package pl.edu.agh.cs.kraksim.stat;

import com.google.common.collect.Maps;
import pl.edu.agh.cs.kraksim.core.City;
import pl.edu.agh.cs.kraksim.real_extended.RealSimulationParams;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class StatCollector {
	private int currentTurn = 0;
	private static StatCollector statCollector = null;

	private final Map<String, Map<String, LinkStat>> roadStats = new HashMap<>();
	// mapa zawiera podsumowanie statystyk dla drog,
	private Map<String, LinkStatSummary> linksSummary;
	// zawiera podsumowanie statystyk dla kierowcow - pojazdow
	private Map<String, DriverSummary> driversSummary;

	private City city;

	public int getCurrentTurn() {
		return currentTurn;
	}

	public void setCurrentTurn(int currentTurn) {
		this.currentTurn = currentTurn;
	}

	public void turn() {
		currentTurn++;
	}

	private StatCollector() {
	}

	// TODO: when extending simulation to multithreaded, synchronize this
	public static StatCollector getInstance() {
		if (statCollector == null) {
			statCollector = new StatCollector();
		}

		return statCollector;
	}

	// TODO: when extending simulation to multithreaded, synchronize this
	public static StatCollector getInstance(City city) {
		if (statCollector == null) {
			statCollector = new StatCollector();
		}
		// WARNING: this singleton holds reference to city
		// remember to destroy this singleton, when not used
		statCollector.city = city;

		return statCollector;
	}

	public void addLinkEnterStatistic(String linkId, String driverName, int velocity) {
		Map<String, LinkStat> stats = roadStats.get(linkId);
		if (stats == null) {
			stats = new HashMap<>();
		}
		LinkStat linkstat = stats.get(driverName);
		if (linkstat == null) {
			linkstat = new LinkStat();
		}
		// TODO: conflicts
		linkstat.setEnterSpeed(velocity);
		linkstat.setEnterTime(currentTurn);

		stats.put(driverName, linkstat);
		roadStats.put(linkId, stats);
	}

	public void addLinkLeaveStatistic(String linkId, String driverName, int velocity) {
		Map<String, LinkStat> stats = roadStats.get(linkId);
		if (stats == null) {
			stats = Maps.newHashMap();
		}
		LinkStat linkstat = stats.get(driverName);
		if (linkstat == null) {
			linkstat = new LinkStat();
		}
		linkstat.setLeaveSpeed(velocity);
		linkstat.setLeaveTime(currentTurn);

		stats.put(driverName, linkstat);
		roadStats.put(linkId, stats);
	}

	public void printStats(Writer out) throws IOException {
		// TODO: move to external classes, and use as decorators, or visitors
		computeSummary();
	/*
	 * out.write(new DriverSummary().getFormat()); out.write("\n"); for
     * (Entry<String, DriverSummary> entry : driversSummary.entrySet()) {
     * out.write(entry.getValue().toString()); out.write("\n"); }
     */
		out.write(new LinkStatSummary().getFormat());
		out.write("\n");
		for (Entry<String, LinkStatSummary> entry : linksSummary.entrySet()) {
			out.write(entry.getValue().toString());
			out.write("\n");
		}
	}

	public void computeSummary() {
		// TODO: move to external classes, and use as decorators, or visitors
		// mapa zawiera podsumowanie statystyk dla drog,
		linksSummary = Maps.newTreeMap();
		// zawiera podsumowanie statystyk dla kierowcow - pojazdow
		driversSummary = Maps.newTreeMap();

		for (Entry<String, Map<String, LinkStat>> entry : roadStats.entrySet()) {
			String linkId = entry.getKey();
			Map<String, LinkStat> linkValues = entry.getValue();

			// update SUMMARY per LINK
			LinkStatSummary singleLinkSummary = new LinkStatSummary();
			singleLinkSummary.setLinkLength(city.findLink(linkId).getLength());
			singleLinkSummary.setId(linkId);

			for (Entry<String, LinkStat> linkValueEntry : linkValues.entrySet()) {
				String driverID = linkValueEntry.getKey();
				LinkStat singleStat = linkValueEntry.getValue();

				singleLinkSummary.increaseCarConuter();
				singleLinkSummary.addTotalDriveTime(singleStat.getLinkDriveTime());

				// update SUMMARY per DRIVER
				DriverSummary singleDriverSum = driversSummary.get(driverID);
				if (singleDriverSum == null) {
					singleDriverSum = new DriverSummary();
				}
				singleDriverSum.setDriverName(driverID);
				singleDriverSum.addTotalTripLenght(singleLinkSummary.getLinkLength());
				singleDriverSum.addTotalTripTime(singleStat.getLinkDriveTime());
				driversSummary.put(singleDriverSum.getDriverName(), singleDriverSum);
			}
			linksSummary.put(singleLinkSummary.getId(), singleLinkSummary);
		}
	}

	public Map<String, DriverSummary> getDriversSummary() {
		if (driversSummary == null) {
			computeSummary();
		}
		return driversSummary;
	}

	public Map<String, LinkStatSummary> getLinksSummary() {
		if (linksSummary == null) {
			computeSummary();
		}
		return linksSummary;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Entry<String, Map<String, LinkStat>> linkEntry : roadStats.entrySet()) {
			sb.append("----\nROAD = ").append(linkEntry.getKey()).append('\n');
			Map<String, LinkStat> linkValues = linkEntry.getValue();

			for (Entry<String, LinkStat> linkValuesEntry : linkValues.entrySet()) {
				sb.append("DRIVER = ").append(linkValuesEntry.getKey()).append(": ");
				sb.append(linkValuesEntry.getValue()).append('\n');
			}
		}
		return sb.toString();
	}

	private static class LinkStat {
		private int enterTime = 0;
		private int leaveTime = 0;
		private int enterSpeed = 0;
		private int leaveSpeed = 0;

		public LinkStat() {
		}

		public int getEnterSpeed() {
			return enterSpeed;
		}

		public void setEnterSpeed(int enterSpeed) {
			this.enterSpeed = enterSpeed;
		}

		public int getEnterTime() {
			return enterTime;
		}

		public void setEnterTime(int enterTime) {
			this.enterTime = enterTime;
		}

		public int getLeaveSpeed() {
			return leaveSpeed;
		}

		public void setLeaveSpeed(int leaveSpeed) {
			this.leaveSpeed = leaveSpeed;
		}

		public int getLeaveTime() {
			return leaveTime;
		}

		public void setLeaveTime(int leaveTime) {
			this.leaveTime = leaveTime;
		}

		public int getLinkDriveTime() {
			// TODO: rethink this
			if ((leaveTime - enterTime) < 0) {
				return 0;// System.out.println(this);
			}
			return (leaveTime - enterTime);
		}

		@Override
		public String toString() {
			return "[ et " + enterTime + ", es " + enterSpeed + ", lt " + leaveTime + ", ls " + leaveSpeed + ']';
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + enterSpeed;
			result = prime * result + enterTime;
			result = prime * result + leaveSpeed;
			result = prime * result + leaveTime;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}

			LinkStat other = (LinkStat) obj;
			return !(enterSpeed != other.enterSpeed || enterTime != other.enterTime || leaveSpeed != other.leaveSpeed) && leaveTime == other.leaveTime;
		}
	}

	private static class LinkStatSummary {
		private String id;
		private int linkLength = 0;
		private int totalDriveTime = 0;
		private double totalDriveTimeSqr = 0;
		private int cars = 0;

		void increaseCarConuter() {
			cars++;
		}

		int getCars() {
			return cars;
		}

		void addTotalDriveTime(int delta) {
			totalDriveTime += delta;
			totalDriveTimeSqr += (float) delta * (float) delta;
		}

		int getTotalDriveTime() {
			return totalDriveTime;
		}

		public String getFormat() {
			return "linkID, linkLength, totalDriveTime, cars, averageDrivingTime, stddev(averageDrivingTime), averageSpeed";
		}

		// FORMAT: linkID, linkLength, totalDriveTime, cars
		@Override
		public String toString() {
			//StringBuilder sb = new StringBuilder();
			// sb.append("time ")
			// .append(totalDriveTime)
			// .append("; cars ")
			// .append(cars)
			// .append("; LENGTH ")
			// .append(linkLength);
			double avgDrivingTime = totalDriveTime / (double) cars;
			double devAvgDrivingTime = Math.sqrt((totalDriveTimeSqr - (double) totalDriveTime * avgDrivingTime) / (float) (cars - 1));
			double unitPerSec = linkLength / avgDrivingTime;
			double kphPerSec = RealSimulationParams.convertToKPH((float) unitPerSec);
			return String.format("%6s %4d %6d %6d %6.2f %6.2f %6.2f %6.2f", id, linkLength, totalDriveTime, cars, avgDrivingTime, devAvgDrivingTime, unitPerSec, kphPerSec);
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + cars;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			result = prime * result + linkLength;
			result = prime * result + totalDriveTime;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}

			LinkStatSummary other = (LinkStatSummary) obj;
			if (cars != other.cars) {
				return false;
			}
			if (id == null && other.id != null) {
				return false;
			} else if (!id.equals(other.id)) {
				return false;
			}

			return linkLength == other.linkLength && totalDriveTime == other.totalDriveTime;
		}

		void setLinkLength(int linkLength) {
			this.linkLength = linkLength;
		}

		int getLinkLength() {
			return linkLength;
		}

		void setId(String id) {
			this.id = id;
		}

		String getId() {
			return id;
		}
	}

	private static class DriverSummary {
		private String driverName;
		private int totalTripTime = 0;
		private int totalTripLenght = 0;

		public String getFormat() {
			return "driverName, totalTripTime, totalTripLenght";
		}

		// format: driverName, totalTripTime, totalTripLenght
		@Override
		public String toString() {
			// sb.append("time ").append(totalTripTime).append("; length
			// ").append(totalTripLenght);
			return driverName + ", " + totalTripTime + ", " + totalTripLenght;
		}

		void setTotalTripTime(int totalTripTime) {
			this.totalTripTime = totalTripTime;
		}

		void addTotalTripTime(int delta) {
			totalTripTime += delta;
		}

		int getTotalTripTime() {
			return totalTripTime;
		}

		void setTotalTripLenght(int totalTripLenght) {
			this.totalTripLenght = totalTripLenght;
		}

		void addTotalTripLenght(int delta) {
			totalTripLenght += delta;
		}

		int getTotalTripLenght() {
			return totalTripLenght;
		}

		void setDriverName(String driverName) {
			this.driverName = driverName;
		}

		String getDriverName() {
			return driverName;
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + ((driverName == null) ? 0 : driverName.hashCode());
			result = prime * result + totalTripLenght;
			result = prime * result + totalTripTime;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}

			DriverSummary other = (DriverSummary) obj;
			if (driverName == null && other.driverName != null) {
					return false;
			} else if (!driverName.equals(other.driverName)) {
				return false;
			}

			return totalTripLenght == other.totalTripLenght && totalTripTime == other.totalTripTime;
		}
	}
}

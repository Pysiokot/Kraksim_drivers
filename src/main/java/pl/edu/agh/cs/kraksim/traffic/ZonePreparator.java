package pl.edu.agh.cs.kraksim.traffic;

import com.google.common.collect.*;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.main.drivers.DriverZones;

import java.util.*;

public class ZonePreparator {
	private static final Logger LOGGER = Logger.getLogger(ZonePreparator.class);

	private final Iterator<DriverZones> iterator;

	public ZonePreparator(int count, Collection<Pair<String, Double>> zones) {
		iterator = makeIterator(count, zones);
	}

	public Iterator<DriverZones> getIterator() {
		return iterator;
	}

	private static Iterator<DriverZones> makeIterator(int count, Collection<Pair<String, Double>> zones) {
		Map<String, Double> weight = calculateWeight(zones);
		List<DriverZones> baseDrivers = prepareBaseDrivers(weight);
		List<DriverZones> drivers = Lists.newArrayListWithCapacity(count);

		while(drivers.size() < count){
			drivers.addAll(baseDrivers);
			Collections.shuffle(baseDrivers);
		}

		return drivers.iterator();
	}

	private static List<DriverZones> prepareBaseDrivers(Map<String, Double> weight) {
		Queue<Integer> indexes = makeIndexes();
		DriverZones[]drivers = new DriverZones[1000];
		for (Map.Entry<String, Double> elem : weight.entrySet()) {
			DriverZones zones = new DriverZones(elem.getKey());
			for (int i = 0; i < elem.getValue() * 1000; i++){
				drivers[indexes.poll()] = zones;
			}
		}

		return Lists.newArrayList(drivers);
	}

	private static Queue<Integer> makeIndexes() {
		LinkedList<Integer> indexes = Lists.newLinkedList();
		for(int i=0;i<1000;i++){
			indexes.add(i);
		}
		Collections.shuffle(indexes);
		return indexes;
	}

	private static Map<String, Double> calculateWeight(Collection<Pair<String, Double>> zones) {
		Map<String, Double> weights = new HashMap<>();
		for (Pair<String, Double> zone : zones) {
			Double weightSoFar = weights.get(zone.getLeft());
			if (weightSoFar != null) {
				LOGGER.warn(String.format("Already defined zone %s - percentage will be recomputed", zone.getLeft()));
			} else {
				weightSoFar = 0.0;
			}
			weights.put(zone.getLeft(), weightSoFar + zone.getRight());
		}

		double sum = getValuesSum(weights.values());

		for (String key : weights.keySet()) {
			weights.put(key, weights.get(key) / sum);
		}

		return weights;
	}

	private static double getValuesSum(Collection<Double> values) {
		double sum = 0.0;

		for (double value : values) {
			sum += value;
		}

		return sum;
	}
}

package pl.edu.agh.cs.kraksim.routing.prediction;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.core.City;
import pl.edu.agh.cs.kraksim.core.Link;

import java.util.*;

public class DefaultTrafficPredictor implements ITrafficPredictor {
	private static final Logger LOGGER = Logger.getLogger(DefaultTrafficPredictor.class);

	private ITrafficPredictionSetup setup = null;
	private WorldState mostRecentWorldState;

	private Map<Integer, Set<Integer>> neighborsArray;    // set of neighbours for each link
	private double[] defaultValuesForLinks;        // value of each link referred to as 100%

	// a map containing statistics for each result of each of values in each link
	// yes, it does have few dimensions - actually, this map reduces 4 dimensions
	// into one, using String obtained from PredictionContainerPath.toString() 
	private final Map<String, TrafficStatisticsForResult> resultsMap;
	private final Queue<WorldState> history;            // few last history entries

	// Singleton pattern
	private static ITrafficPredictor theInstance;

	public static ITrafficPredictor getInstance() {
		if (theInstance == null) {
			theInstance = new DefaultTrafficPredictor();
		}
		return theInstance;
	}

	private DefaultTrafficPredictor() {
		history = new LinkedList<>();
		defaultValuesForLinks = null;
		neighborsArray = null;
		resultsMap = new HashMap<>();
	}

	//@Override
	public void setup(ITrafficPredictionSetup setup) {
		this.setup = setup;
		int linkCount = setup.getCity().linkCount();
		defaultValuesForLinks = new double[linkCount];
		// separated in case of inheriting with something more clever
		setupDefaultValuesForLink();
		this.setup.getDiscretiser().setDefaultsForColumns(defaultValuesForLinks);

		neighborsArray = new HashMap<>();
		// just the same as above...
		setupNeighboursArray();
	}

	/**
	 * This method needs commenting. It searches for all links that are
	 * no more than x hops from the given link - and it does it for each
	 * of the links in the city. The recursion is done by 2 queues - one
	 * of them keeping the links within current number of hops (not yet
	 * processed), the other one one keeps those that can be reached form
	 * the current ones. Then number of hops left is decremented and the
	 * queues replaced.
	 */
	private void setupNeighboursArray() {
		City city = setup.getCity();
		Iterator<Link> it = city.linkIterator();
		// for each link in the city...
		while (it.hasNext()) {
			Link link = it.next();
			// prepare set of reachable links
			Set<Integer> set = Sets.newHashSet();

			// get ready for searching
			int hopsCount = setup.getNumberOfInfluencedLinks();
			Queue<Link> currentHops = new LinkedList<>();
			Queue<Link> nextHops = new LinkedList<>();

			currentHops.add(link);
			// now, for all hops...
			while (hopsCount > 0) {
				// and for each link at this distance
				Link currentLink = currentHops.poll();
				while (currentLink != null) {
					// and add all links reachable from it to the next level queue
					for (Iterator<Link> iter = currentLink.reachableLinkIterator(); iter.hasNext(); ) {
						// add it to reachable links
						Link temp = iter.next();
						set.add(temp.getLinkNumber());
						nextHops.add(temp);
					}
					// and pop the next link
					currentLink = currentHops.poll();
				}
				// we are done for this distance, let's swap the queues
				currentHops = nextHops;
				nextHops = new LinkedList<>();
				// decrement the number of hops left;
				hopsCount--;
			}
			// we have the last level of distance in currentHops queue
			// let's add it to the set
			for (Link currentLink : currentHops) {
				set.add(currentLink.getLinkNumber());
			}
			currentHops.clear();
			// now, as we created the set, let's map it 
			neighborsArray.put(link.getLinkNumber(), set);
		}
	}

	private void setupDefaultValuesForLink() {
		City city = setup.getCity();
		Iterator<Link> it = city.linkIterator();
		while (it.hasNext()) {
			Link link = it.next();
			// divide link length by max speed to get the quickest time to get through
			double avgDuration = link.getLength() / link.getSpeedLimit();
			defaultValuesForLinks[link.getLinkNumber()] = avgDuration;
		}
	}

	public void adjustCurrentWeightsOfLink(double[] weightsOfLinks) {
		// if there are no levels then it means that the prediction is off...
		if (setup.getDiscretiser().getNumberOfLevels() < 1) {
			return;
		}

		// otherwise, starting the adjustment
		int linksNo = defaultValuesForLinks.length;
		double[] influences = new double[linksNo];
		// initializing the array of changes
		for (int i = 0; i < linksNo; i++) {
			influences[i] = 1;
		}
		TrafficPredictionContainerPath path = new TrafficPredictionContainerPath();

		// for each of the links
		for (int i = 0; i < linksNo; i++) {
			path.setLinkNumber(i);
			double linkState = mostRecentWorldState.getStateAt(i);
			if (linkState < 0) {
				continue;
			}
			// obtain its level
			TrafficLevel srcLvl = null;
			try {
				srcLvl = setup.getDiscretiser().getLevelForValueInColumn(i, linkState);
			} catch (TrafficPredictionException e) {
				LOGGER.error(e);
				continue;
			}

			// and set it in path
			path.setLevel(srcLvl);
			Set<Integer> neighbors = neighborsArray.get(i);
			// then, for each timestep...
			for (int t = 0; t < setup.getNumberOfInfluencedTimesteps(); t++) {
				path.setTimestepNumber(t + 1);
				// and for each of neighbors
				for (Integer ngb : neighbors) {
					// get the most often result of it
					path.setDestinationLinkNumber(ngb);
					TrafficStatisticsForResult stat = resultsMap.get(path.toString());
					if (stat == null) {
						continue;
					}
					TrafficLevel winner = null;
					try {
						winner = setup.getDiscretiser().getLevelByName(stat.getNameOfMostFrequentLevel());
					} catch (TrafficPredictionException e) {
						LOGGER.error(e);
						continue;
					}
					// is it often enough?
					if (stat.getProbabilityForLevel(winner) > setup.getCutOutProbability()) {
						// cut out repetitions with too little counter
						if (stat.getCounterForLevel(winner) > setup.getCutOutMinimumCounter()) {
							// Aaa, so we've got a pattern. It's time to use it!

							LOGGER.debug("Pattern: " + path + " results in " + winner.getDescription() + " (prop=" + stat.getProbabilityForLevel(winner) + ')');
							// scaling influence
							double influence = computeInfluence(winner.getMaxInfluence(), stat.getProbabilityForLevel(winner));

							// and applying the influence on the neighbor
							influences[ngb] += influence;
						}
					}
				}
			}

			// ageing process
			for (TrafficStatisticsForResult stat : resultsMap.values()) {
				stat.ageResults(setup.getAgeingRate());
			}
		}

		// influences are computed. Now, we shall use them on the real weights 
		for (int i = 0; i < weightsOfLinks.length; i++) {
			// if 1 - no point in multiplying
			if (influences[i] != 1.) {
				weightsOfLinks[i] *= influences[i];
			}
		}
	}

	/**
	 * Method used to scale the influence. Max influence is the amount of
	 * influence the pattern can have. Probability informs about the reliability
	 * of the pattern, minimal reliability is kept in setup field of this class
	 *
	 * @param maxInfluence maximum influence the pattern can have
	 * @param probability  reliability of the pattern
	 * @return the influence that should be used
	 */
	private double computeInfluence(double maxInfluence, double probability) {

		// scaling range of (minProp, 1> onto (0, 1>
		double minProp = setup.getCutOutProbability();
		double dProp = probability - minProp;
		double propScale = dProp / (1 - minProp);
		// propScale contains information about how strong is the pattern among
		// believable patterns. Example:
		// minProp = 85%
		// probability = 90%
		// propScale = 33% - because it is 33% above the minimal probability

		// the scaling of influence is easy - it is just multiplying propScale
		// by maxInfluence
		return maxInfluence * propScale;
	}

	///@Override
	public synchronized void appendWorldState(WorldState state) {
		history.add(state);
		LOGGER.debug("ad" + setup.getNumberOfInfluencedTimesteps() + setup.getNumberOfInfluencedLinks());
		mostRecentWorldState = state;
		if (history.size() > setup.getNumberOfInfluencedTimesteps() + 1) {
			history.poll();
		}
		try {
			analyzeHistory();
		} catch (Exception e) {
			LOGGER.error(e);
		}
	}

	private synchronized void analyzeHistory() throws TrafficPredictionException {
		int steps = history.size() - 1;
		WorldState source = history.poll();
		history.add(source);

		TrafficPredictionContainerPath path = new TrafficPredictionContainerPath();
		WorldState state = null;
		// we shall increment counters for each of appearing history 
		// entries in the whole space
		for (int i = 0; i < steps; i++) {
			// pick the states, from the oldest ones
			state = history.poll();
			history.add(state);

			path.setTimestepNumber(i + 1);
			// for each of source links
			for (int l = 0; l < neighborsArray.size(); l++) {
				path.setLinkNumber(l);
				// we're counting the value it has (and discretise it)
				double currentValue = source.getStateAt(l);

				// negative values imply there were no cars on the link 
				// yet, so we omit this value
				if (currentValue < 0) {
					continue;
				}
				// discretising 
				TrafficLevel sourceLevel = setup.getDiscretiser().getLevelForValueInColumn(l, currentValue);
				// and setting it in the path
				path.setLevel(sourceLevel);

				// and now, for each of the neighbours...
				for (Integer dest : neighborsArray.get(l)) {
					// ... we set the path, ...
					path.setDestinationLinkNumber(dest);
					// .. obtain the state, ...
					currentValue = state.getStateAt(dest);
					// (if negative-we skip it)
					if (currentValue < 0) {
						continue;
					}

					// Discretise the state...
					TrafficLevel level = setup.getDiscretiser().getLevelForValueInColumn(dest, currentValue);
					// finally, we reached it! Now, we increment the counter
					String strPath = path.toString();
					TrafficStatisticsForResult stats = resultsMap.get(strPath);
					if (stats == null) {
						stats = new TrafficStatisticsForResult();
					}
					stats.incrementCounterForLevel(level);
					resultsMap.put(strPath, stats);
				} // of for neighbours
			}//of for each link
		}// of for each history entry
	}

	public Map<Integer, Set<Integer>> getNeighborsArray() {
		return neighborsArray;
	}

	public Queue<WorldState> getHistory() {
		return history;
	}
}

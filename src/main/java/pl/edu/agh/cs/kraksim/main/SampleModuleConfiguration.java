package pl.edu.agh.cs.kraksim.main;

import edu.uci.ics.jung.graph.Graph;
import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.KraksimConfigurator;
import pl.edu.agh.cs.kraksim.core.*;
import pl.edu.agh.cs.kraksim.core.exceptions.InvalidClassSetDefException;
import pl.edu.agh.cs.kraksim.core.exceptions.ModuleCreationException;
import pl.edu.agh.cs.kraksim.dsyncdecision.DsyncDecisionModuleCreator;
import pl.edu.agh.cs.kraksim.iface.Clock;
import pl.edu.agh.cs.kraksim.iface.block.BlockIView;
import pl.edu.agh.cs.kraksim.iface.carinfo.CarInfoIView;
import pl.edu.agh.cs.kraksim.iface.decision.DecisionIView;
import pl.edu.agh.cs.kraksim.iface.eval.EvalIView;
import pl.edu.agh.cs.kraksim.iface.mon.MonIView;
import pl.edu.agh.cs.kraksim.iface.sim.SimIView;
import pl.edu.agh.cs.kraksim.main.gui.GUISimulationVisualizer;
import pl.edu.agh.cs.kraksim.main.gui.SimulationVisualizer;
import pl.edu.agh.cs.kraksim.ministat.MiniStatEView;
import pl.edu.agh.cs.kraksim.ministat.MiniStatModuleCreator;
import pl.edu.agh.cs.kraksim.real_extended.DriverEnv;
import pl.edu.agh.cs.kraksim.real_extended.QLearner;
import pl.edu.agh.cs.kraksim.real_extended.RealSimulationParams;
import pl.edu.agh.cs.kraksim.routing.*;
import pl.edu.agh.cs.kraksim.simpledecision.SimpleDecisionEView;
import pl.edu.agh.cs.kraksim.simpledecision.SimpleDecisionModuleCreator;
import pl.edu.agh.cs.kraksim.sna.centrality.CentralityCalculator;
import pl.edu.agh.cs.kraksim.sna.centrality.MeasureType;
import pl.edu.agh.cs.kraksim.weka.WekaPredictionModule;
import pl.edu.agh.cs.kraksim.weka.WekaPredictionModuleHandler;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SampleModuleConfiguration {
	private static final Logger LOGGER = Logger.getLogger(SampleModuleConfiguration.class);
	private final WekaPredictionModuleHandler wekaPredictionHandler = new WekaPredictionModuleHandler();
	private City city;
	private Router router;
	private TimeBasedRouter dynamicRouter;
	private StaticRouter staticRouter;
	private SimIView simView;
	private MiniStatEView statView;
	private EvalIView evalView;
	private DecisionIView decisionView;
	private CarInfoIView carInfoView;
	private BlockIView blockView;
	private boolean qlearning = KraksimConfigurator.getProperty("qlearning").equals("true");

	private List<QLearner> QLearners;

	private Graph<Node, Link> graph;

	/**
	 * This is the place where all the binding between modules is done.
	 *
	 * @param core
	 * @param evalProvider
	 */
	public SimulationVisualizer setUpModules(final Core core, final EvalModuleProvider evalProvider, ModuleCreator physModuleCreator, Clock clock, StartupParameters params) {
		SimulationVisualizer visualizator = null;
		city = core.getCity();

		graph = CentralityCalculator.cityToGraph(city);

		try {
			// this is Nagel-Schreckenberg Simulation Module
			Module physModule = core.newModule("phys", physModuleCreator);
			simView = new SimIView(physModule);
			carInfoView = new CarInfoIView(physModule);
			MonIView monView = new MonIView(physModule);
			blockView = new BlockIView(physModule);

			//TMP
			CentralityCalculator.carInfoView = carInfoView;
			Iterator<Link> it = city.linkIterator();
			while (it.hasNext()) {
				it.next().calculateWeight(0);
			}
			CentralityCalculator.calculateCentrality(graph, MeasureType.PageRank, 3);
			//tylko do wypisywania
			//CentralityCalculator.calculateCentrality(city, MeasureType.PageRank);
			//END TMP

			Module statModule = core.newModule("stat", new MiniStatModuleCreator(monView, clock));
			statView = new MiniStatEView(statModule);

			ITimeTable timeTable = null;
			if (params.isRerouting()) {
				if (params.getPredictionModule().equals("weka") && params.isEnablePrediction()) {
					WekaPredictionModule predictionModule = new WekaPredictionModule(city, statView, carInfoView, clock);
					wekaPredictionHandler.setPredictionModule(predictionModule);
					timeTable = new TimeTableRules(city, clock, predictionModule);
				} else {
					timeTable = new TimeTable(city, statView, clock, params.getGlobalInforUpdateInterval());
				}
				dynamicRouter = new TimeBasedRouter(city, timeTable);
				router = dynamicRouter;
				staticRouter = new StaticRouter(city);
			} else {
				router = new StaticRouter(city);
				if (params.getPredictionModule().equals("weka") && params.isEnablePrediction()) {
					WekaPredictionModule predictionModule = new WekaPredictionModule(city, statView, carInfoView, clock);
					wekaPredictionHandler.setPredictionModule(predictionModule);
					timeTable = new TimeTableRules(city, clock, predictionModule);
					LOGGER.info("Prediction configured");
				}
			}

			Module evalModule = evalProvider.provideNew("eval", core, carInfoView, monView, blockView, 2, RealSimulationParams.DEFAULT_MAX_VELOCITY);
			if (evalModule != null) {
				evalView = new EvalIView(evalModule);

				ITimeTable timeTableToPass = null;
				if (params.isMinimalSpeedUsingPrediction()) {
					timeTableToPass = timeTable;
				}

				Module decisionModule = core.newModule("decision", new SimpleDecisionModuleCreator(evalView, blockView, carInfoView, statView, timeTableToPass, wekaPredictionHandler, clock, params.getTransitionDuration()));
				decisionView = new DecisionIView(decisionModule);
				SimpleDecisionEView simpleDecisionView = new SimpleDecisionEView(decisionModule);
				wekaPredictionHandler.setEvalView(evalView);
				wekaPredictionHandler.setSimpleDecisionView(simpleDecisionView);
			} else {
				Module newDecisionModule = core.newModule("newDecision", new DsyncDecisionModuleCreator(statView, blockView, clock, params.getTransitionDuration(), evalProvider.getAlgorithmCode().equals("sync")));
				decisionView = new DecisionIView(newDecisionModule);
			}

			if (qlearning){
				List<QLearner> tmpRlearners = new LinkedList<>();

				for (Iterator<Link> linkIt = city.linkIterator(); linkIt.hasNext();) {
					tmpRlearners.add(new QLearner(new DriverEnv(linkIt.next(), city,  statView, carInfoView, clock, params.getStatFileName())));
				}

				QLearners = tmpRlearners;
			} else {
				QLearners = Collections.emptyList();
			}

			if (params.isVisualization()) {
				visualizator = new GUISimulationVisualizer(city, carInfoView, blockView, statView);
			} else {
				visualizator = new ConsoleSimulationVisualizer(city, statView);
			}
		} catch (InvalidClassSetDefException e) {
			error("Internal error", e);
		} catch (ModuleCreationException e) {
			error("Error while creating module", e);
		}

		return visualizator;
	}

	private static void error(final String text, final Throwable error) {
		LOGGER.error(text + "\n  Details: " + error.getMessage());
		System.exit(1);
	}

	public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
	}

	public Router getRouter() {
		return router;
	}

	public TimeBasedRouter getDynamicRouter() {
		return dynamicRouter;
	}

	public StaticRouter getStaticRouter() {
		return staticRouter;
	}

	public SimIView getSimView() {
		return simView;
	}

	public MiniStatEView getStatView() {
		return statView;
	}

	/*public void setStatView(MiniStatEView statView) {
		this.statView = statView;
	}*/

	public EvalIView getEvalView() {
		return evalView;
	}

	public DecisionIView getDecisionView() {
		return decisionView;
	}

	public CarInfoIView getCarInfoView() {
		return carInfoView;
	}

	public BlockIView getBlockView() {
		return blockView;
	}

	public WekaPredictionModuleHandler getWekaPrediction() {
		return wekaPredictionHandler;
	}

	public Graph<Node, Link> getGraph() {
		return graph;
	}

	public List<QLearner> getRLearners() {
		return QLearners;
	}
}

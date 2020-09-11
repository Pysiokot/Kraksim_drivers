package pl.edu.agh.cs.kraksim.main;

// on 7/15/07 3:41 PM

import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.core.Core;
import pl.edu.agh.cs.kraksim.core.Gateway;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.core.ModuleCreator;
import pl.edu.agh.cs.kraksim.iface.Clock;
import pl.edu.agh.cs.kraksim.iface.eval.EvalIView;
import pl.edu.agh.cs.kraksim.iface.sim.Route;
import pl.edu.agh.cs.kraksim.iface.sim.TravelEndHandler;
import pl.edu.agh.cs.kraksim.main.drivers.DecisionHelper;
import pl.edu.agh.cs.kraksim.main.drivers.Driver;
import pl.edu.agh.cs.kraksim.main.gui.Controllable;
import pl.edu.agh.cs.kraksim.main.gui.OptionsPanel;
import pl.edu.agh.cs.kraksim.main.gui.SimulationVisualizer;
import pl.edu.agh.cs.kraksim.parser.ModelParser;
import pl.edu.agh.cs.kraksim.parser.ParsingException;
import pl.edu.agh.cs.kraksim.parser.TrafficSchemeParser;
import pl.edu.agh.cs.kraksim.real_extended.DriverType;
import pl.edu.agh.cs.kraksim.qlearning.QLearner;
import pl.edu.agh.cs.kraksim.real_extended.RealSimulationParams;
import pl.edu.agh.cs.kraksim.routing.NoRouteException;
import pl.edu.agh.cs.kraksim.routing.prediction.TrafficPredictionFactory;
import pl.edu.agh.cs.kraksim.sna.GraphVisualizer;
import pl.edu.agh.cs.kraksim.sna.SnaConfigurator;
import pl.edu.agh.cs.kraksim.traffic.TravellingScheme;
import pl.edu.agh.cs.kraksim.visual.infolayer.InfoProvider;

import java.io.*;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;

public class Simulation implements Clock, TravelEndHandler, Controllable {
	private static final Logger logger = Logger.getLogger(Simulation.class);
	
	// run arguments
	private final StartupParameters params = new StartupParameters();
	private final SampleModuleConfiguration modules = new SampleModuleConfiguration();
	private PrintWriter statWriter;
	private PrintWriter summaryStatWriter;
	private PrintWriter linkStatWriter;
	private PrintWriter qTablesWriter;
	private PrintWriter qSummaryWriter;
	private SimulationVisualizer visualizer;
	private StatsUtil.LinkStat linkStat;
	private StatsUtil.LinkStat linkRidingStat;
	
	//do grafu
	private GraphVisualizer graphVisualizer;
	
	
	private Collection<TravellingScheme> trafficScheme;

	private int turn;
	public static Integer turnNumber = 0; // the only way to get sim turn in other class 
	private int activeDriverCount;
	private PriorityQueue<Driver> departureQueue;
	private DecisionHelper isDriverRoutingHelper;

	private volatile boolean continousMode = false;
	private boolean stepMode = false;

	private OptionsPanel controler;

	private final PrintWriter console = new PrintWriter(System.out);

	private long startTime;

	private void error(final String text, final Throwable error) {
		logger.error(text + "\n  Details: " + error.getMessage());
		System.exit(1);
	}

	private void error(final String text) {
		logger.error(text);
		System.exit(1);
	}

	public static void main(final String[] args) {
		new Simulation(args).run();
	}

	public Simulation(String[] args) {
		// logger.info( "STARTING SIMULATION with params: " + Arrays.toString(
		// args ) );
		departureQueue = new PriorityQueue<>();

		try {
			params.parseOptions(args, console);
		} catch (ParseException e) {
			System.err.println("Exception!");
			e.printStackTrace();
		}
		final EvalModuleProvider evalProvider = getEvaluationProvider();
        InfoProvider.getInstance().setEvalProvider(evalProvider);
		final Core core = createCore(params.getModelFile(), params
				.getTrafficSchemeFile());
		setUpStatictics();
		
		// create new phys module
		ModuleCreator physModuleCreator;
		        switch (params.getPhysModule()) {
		            case "realExtended": {
						RealSimulationParams simulationParams = new pl.edu.agh.cs.kraksim.real_extended.RealSimulationParams(params.getModelRg(), params.getCarMoveModel());
		                simulationParams.setSwitchTime(params.getSwitchTime());
		                simulationParams.setMinSafeDistance(params.getMinSafeDistance());

		                physModuleCreator = new pl.edu.agh.cs.kraksim.real_extended.RealModuleCreator(simulationParams);
		                break;
		            } default : {
		                physModuleCreator = new pl.edu.agh.cs.kraksim.real_extended.RealModuleCreator
		                        (new pl.edu.agh.cs.kraksim.real_extended.RealSimulationParams(params.getModelRg(), params.getCarMoveModel()));
		                break;
		            }
		        }
		
		visualizer = modules.setUpModules(core, evalProvider, physModuleCreator, this, params);
		console.println("");

		isDriverRoutingHelper = new DecisionHelper(params.getDriverRoutingRg(),
				params.getDriverRoutingTh());

		if (params.isCommandLineMode()) {
			doRun();
		}
	}

	private void setUpStatictics() {
		if (params.getStatFileName() != null) {
			File file = new File(params.getStatFileName());
			boolean dirCreated = file.mkdirs();
			boolean success = file.delete();
			try {
				statWriter = new PrintWriter(new BufferedOutputStream(
						new FileOutputStream(params.getStatFileName())));
				summaryStatWriter = new PrintWriter(
						new BufferedOutputStream(new FileOutputStream(params
								.getStatFileName()
								+ ".sum")));
        linkStatWriter = new PrintWriter(
            new BufferedOutputStream(new FileOutputStream(params
                .getStatFileName()
                + ".link")));

        qTablesWriter = new PrintWriter(new BufferedOutputStream(
				new FileOutputStream(params.getStatFileName()+".ql")));


		qSummaryWriter = new PrintWriter(new BufferedOutputStream(
				new FileOutputStream(params.getStatFileName()+".qla")));

        
			} catch (FileNotFoundException e) {
				error("Error: statistics file cannot be created -- "
						+ params.getStatFileName());
			}
		} else {
			statWriter = new PrintWriter(System.out);
			linkStatWriter = new PrintWriter(System.out);
		}
		linkStat = new StatsUtil.LinkStat();
		linkRidingStat = new StatsUtil.LinkStat();
	}

	private EvalModuleProvider getEvaluationProvider() {
		EvalModuleProvider evalProvider = null;
		try {
			evalProvider = configureAlgorithm(params.getAlgorithmName());
			console.print(", alg=" + params.getAlgorithmName());
		} catch (AlgorithmConfigurationException e) {
			error("Error: ", e);
		}
		
		return evalProvider;
	}

	private Core createCore(final String modelFile,
			final String trafficSchemeFile) {
		Core core = null;
		String file = null;
		try {
			file = modelFile;
			core = ModelParser.parse(file);
			console.print(", model=" + file);

			file = trafficSchemeFile;
			trafficScheme = TrafficSchemeParser.parse(file, core.getCity(), params);
			console.print(", scheme=" + file);
		} catch (FileNotFoundException e) {
			error("Error: cannot open file: " + file, e);
		} catch (IOException e) {
			error("Error: An I/O error occured while parsing file: " + file, e);
		} catch (ParsingException e) {
			e.printStackTrace();
			error("Error: Data error while parsing file: " + file, e);
		}

		return core;
	}

	private static EvalModuleProvider configureAlgorithm(final String algConf)
			throws AlgorithmConfigurationException {
		// System.out.println( algConf );
		EvalModuleProvider[] providers = StartupParameters.getEvalProviders();

		int colonIndex = algConf.indexOf(':');
		String algCode = null;
		if (colonIndex == -1) {
			algCode = algConf;
		} else {
			algCode = algConf.substring(0, colonIndex);
		}

		EvalModuleProvider modProvider = null;
		for (EvalModuleProvider provider : providers) {
			if (provider.getAlgorithmCode().equals(algCode)) {
				modProvider = provider;
				break;
			}
		}

		if (modProvider == null) {
			throw new AlgorithmConfigurationException("algorithm " + algCode
					+ " not found");
		}

		if (colonIndex != -1) {
			String algParams = algConf.substring(colonIndex + 1);
			String[] params = algParams.split(",");
			for (String parameter : params) {
				int y = parameter.indexOf('=');
				if (y == -1) {
					throw new AlgorithmConfigurationException(
							"algorithm configuration syntax error");
				}
				modProvider.setParam(parameter.substring(0, y), parameter
						.substring(y + 1));
			}
		}

		return modProvider;
	}

	public void run() {
		modules.getSimView().ext(modules.getCity()).setCommonTravelEndHandler(
				this);

		for (int i = 0; i < params.getLearnPhaseCount(); i++) {
			visualizer.startLearningPhase(i);
			runPhase();
			//visualizer.endPhase();
		}
		startTime = System.currentTimeMillis();
		visualizer.startTestingPhase();
		// ===================================================================================
		// --- INITIALIZE TEST RUN
		// ===================================================================================
		turn = 0;
		turnNumber = turn;
		modules.getStatView().ext(modules.getCity()).clear();

		generateDrivers();
		modules.getDecisionView().ext(modules.getCity()).initialize();
		// ===================================================================================
		// START TEST RUN
		// ===================================================================================
		StatsUtil.statHeader(modules.getCity(), statWriter);
		boolean isRunning = true;
		while (isRunning) {

			if (continousMode) {
				stepMode = true;
			}

			if (stepMode) {

//				 if ( turn > 5000 ) {	//stop over 5000 
//				 isRunning = false;
//				 }
//				 else
				if (activeDriverCount > 0) {
					step();
				} else {
					isRunning = false;
				}
				stepMode = false;
			} else {
				try {
					// that way we are not wasting CPU time, when in PAUSE
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// I don't know when and how can this thread be interrupted,
					// so this Exception is IGNORED,
					// but I think this should be fixed for application safety
					// and stability
					logger.error(e);
				}
			}
		}
		// ===================================================================================
		// FINILIZE TEST RUN
		// ===================================================================================
		long elapsed = System.currentTimeMillis() - startTime;
		visualizer.endPhase();
		visualizer.end(elapsed);

		if (qTablesWriter != null && qSummaryWriter != null) {
			modules.getRLearners().forEach((n)->qTablesWriter.print(n.QTableToString()));
			printQAnalysis();
		}


		if (summaryStatWriter != null) {
			StatsUtil.dumpStats(modules.getCity(), modules.getStatView(), turn,
					summaryStatWriter);
		}
		if (linkStatWriter != null) {
		    StatsUtil.dumpLinkStats(modules.getCity(),
		            linkStatWriter, linkStat, linkRidingStat);
		}
		cleanUp(summaryStatWriter);
		cleanUp(statWriter);
		cleanUp(linkStatWriter);
		cleanUp(qTablesWriter);
		cleanUp(qSummaryWriter);
		//cleanUp(console);
		modules.getRLearners().forEach(QLearner::cleanUp);

		if (controler != null) {
			controler.end();
		}
	}

	private void printQAnalysis() {
		List<QLearner> rLearners = modules.getRLearners();
		if(rLearners.isEmpty())
			return;
		int[][] bests = new int[7][27];
		int[][] sumVisits = new int[7][27];
		for (QLearner qlearner : rLearners){
			Object table = qlearner.getPolicy().getQTable();
			int[][] visits = qlearner.getPolicy().getVisits();
			for (int i=0;i<7;i++){
				double value=0;
				int max=0;
				for (int j=0;j<27;j++){
					sumVisits[i][j]+=visits[i][j];
					if (value < (double) Array.get(Array.get(table, i),j)){
						value = (double) Array.get(Array.get(table, i),j);
						max=j;
					}

				}

				if(value>0.0)
					bests[i][max]+=1;
			}
		}
		for (int i=0;i<7;i++) {
			String line = "state "+i+"("+Arrays.stream(bests[i]).sum()+")"+"("+Arrays.stream(sumVisits[i]).sum()+")\n";
			for (int j = 0; j < 27; j++) {
				line += bests[i][j] + "("+sumVisits[i][j]+") ";
			}
			line+="\n";
			System.out.println(line);
			qSummaryWriter.print(line);
		}
	}

	private void cleanUp(final Writer writer) {
		if (writer != null) {
			try {
				writer.close();
			} catch (IOException e) {
				logger.warn("Exception while closing ", e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.edu.agh.cs.kraksim.main.Controllable#doStep()
	 */
	public synchronized void doStep() {
		TrafficPredictionFactory.setCityForPredictionSetup(modules.getCity());
		continousMode = false;
		stepMode = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.edu.agh.cs.kraksim.main.Controllable#doRun()
	 */
	public final synchronized void doRun() {
		TrafficPredictionFactory.setCityForPredictionSetup(modules.getCity());
		continousMode = true;
		stepMode = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.edu.agh.cs.kraksim.main.Controllable#doPause()
	 */
	public synchronized void doPause() {
		continousMode = false;
		stepMode = false;
	}

	/*
	 * ONE simulation step, one turn.
	 */
	private void step() {
		modules.getRLearners().forEach(QLearner::runPreEpoch);

		try {
			doDepartures();
		} catch (NoRouteException e) {
			error("Error: There is no route for a travelling scheme", e);
		}

		if (logger.isTraceEnabled()) {
			logger.trace("======== Simulation Module - TURN: " + turn
					+ ". ========");
		}
		modules.getSimView().ext(modules.getCity()).simulateTurn();

		if (logger.isTraceEnabled()) {
			logger
					.trace("======== TURN ENDED - Notifying Evaluation Module.========");
		}
		EvalIView eV = modules.getEvalView();
        InfoProvider.getInstance().setEvalIView(eV);
		if (eV != null) {
			eV.ext(modules.getCity()).turnEnded();
		}

		if (logger.isTraceEnabled()) {
			logger
					.trace("======== TURN ENDED - Notifying Decision Module.========");
		}
		modules.getDecisionView().ext(modules.getCity()).turnEnded();

		modules.getWekaPrediction().turnEnded();
		turn++;
		turnNumber = turn;
		
		//do grafu
		if(turn % SnaConfigurator.getSnaRefreshInterval() == 0 && params.isVisualization()){
			refreshGraph();
		}

		modules.getRLearners().forEach(QLearner::runPostEpoch);

		visualizer.update(turn);
		StatsUtil.dumpCarStats(modules.getCity(), modules.getStatView(), turn, statWriter);
		
		
		StatsUtil.collectLinkStats(modules.getCity(), modules.getCarInfoView(), modules.getBlockView(), modules.getStatView(), turn, linkStat, linkRidingStat);
	}

	private void runPhase() {
		turn = 0;
		turnNumber = turn;
		modules.getStatView().ext(modules.getCity()).clear();

		generateDrivers();
		modules.getDecisionView().ext(modules.getCity()).initialize();

		while (activeDriverCount > 0) {
			step();
		}

	}

	private void generateDrivers() {
		activeDriverCount = 0;

		for (TravellingScheme travelScheme : trafficScheme) {
			for (int i = 0; i < travelScheme.getCount(); i++) {
				boolean emergency = false;
				boolean isDriverReRoutingDecision = true;//isDriverRoutingHelper.decide(50);
				DriverType driverType = DriverType.generateDriverType();
				if (isDriverReRoutingDecision) {
					Driver driver = travelScheme.generateDriver(activeDriverCount++, emergency, driverType,
							travelScheme, modules.getDynamicRouter(),
							new DecisionHelper(params.getDecisionRg(), params
									.getRouteDecisionTh()));
					driver.setDepartureTurn(params.getGenRg());
					departureQueue.add(driver);
				} else {
					Driver driver = travelScheme.generateDriver(activeDriverCount++, emergency, driverType,
							travelScheme, null, null);
					driver.setDepartureTurn(params.getGenRg());
					departureQueue.add(driver);
				}

			}
			for (int j = 0; j < travelScheme.getEmergencyVehicles(); j++) {
				boolean emergency = true;
				boolean isDriverReRoutingDecision = isDriverRoutingHelper
						.decide(50);
				DriverType driverType = DriverType.generateDriverType();
				if (isDriverReRoutingDecision) {
					Driver driver = travelScheme.generateDriver(activeDriverCount++, emergency, driverType,
							travelScheme, modules.getDynamicRouter(),
							new DecisionHelper(params.getDecisionRg(), params
									.getRouteDecisionTh()));
					driver.setDepartureTurn(params.getGenRg());
					departureQueue.add(driver);
				} else {
					Driver driver = travelScheme.generateDriver(activeDriverCount++, emergency, driverType,
							travelScheme, null, null);
					driver.setDepartureTurn(params.getGenRg());
					departureQueue.add(driver);
				}
			}
		}
	}

	private void doDepartures() throws NoRouteException {

		while (true) {
			Driver Driver = departureQueue.peek();
			if (Driver == null || Driver.getDepartureTurn() > turn) {
				break;
			}

			departureQueue.poll();
			Gateway ggg = Driver.srcGateway();
			Link l234 = ggg.getOutboundLink();
			Gateway g234 = Driver.destGateway();
			Route route = modules.getRouter().getRoute(
					l234,
					g234);
//			Route route = modules.getRouter().getRoute(
//					Driver.srcGateway().getOutboundLink(),
//					Driver.destGateway());

			Route route2 = modules.getStaticRouter().getRoute(l234,g234);

			modules.getSimView().ext(modules.getCity()).insertTravel(
					Driver, route, route2, params.isRerouting());
		}
	}

	public void handleTravelEnd(final Object driver) {
		Driver Driver = (Driver) driver;
		if (Driver.nextTravel()) {
			Driver.setDepartureTurn(params.getGenRg());
			departureQueue.add(Driver);
		} else {
			activeDriverCount--;
		}

	}

	public int getTurn() {
		return turn;
	}

	public SimulationVisualizer getVisualizer() {
		return visualizer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.edu.agh.cs.kraksim.main.Controllable#setController(pl.edu.agh.cs.kraksim.main.OptionsPanel)
	 */
	public void setController(final OptionsPanel panel) {
		controler = panel;
	}

	public void setGraphVisualizer(GraphVisualizer graphVisualizer) {
		this.graphVisualizer = graphVisualizer;
	}
	
	private void refreshGraph(){
		this.graphVisualizer.refreshGraph();
	}
	
	public SampleModuleConfiguration getModules() {
		return modules;
	}
	
	public StatsUtil.LinkStat getLinkStat(){
		return linkStat;
	}
	
	public StatsUtil.LinkStat getLinkRidingStat(){
		return linkRidingStat;
	}
	
}

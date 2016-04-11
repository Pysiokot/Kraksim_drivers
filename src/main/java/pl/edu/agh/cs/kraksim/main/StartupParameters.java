package pl.edu.agh.cs.kraksim.main;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Random;

public class StartupParameters {
	private static final Logger LOGGER = Logger.getLogger(StartupParameters.class);

	private static final int DEFAULT_TRANSITION_DURATION = 8;
	private static final int DEFAULT_LEARN_REP_COUNT = 0;
	public static final String PROG_NAME = "kraksim";
	private final Options options = new Options();

	private String statFileName = null;
	private boolean visualization = false;
	private int transitionDuration;
	private String algorithmName = null;
	private String modelFile = null;
	private String trafficSchemeFile = null;
	private int learnPhaseCount;
	private CarMoveModel carMoveModel;

	private boolean enablePrediction = false;
	private String predictionModule = "false";
	private boolean rerouting = false;
	private boolean commandLineMode = false;
	private boolean minimalSpeedUsingPrediction = false;
	private boolean isZoneAwareness = false;

	private long modelSeed;
	private long genSeed;
	private final Random modelRg;
	private final Random genRg;

	private int routeDecisionTh = 0;
	private final Random decisionRg;
	private final Random isDriverRoutingRg;
	private int isDriverRoutingTh = 0;

	private long globalUpdateInterval = 300;
	private String physModule = "real";

    private int switchTime = 0;
    private int minSafeDistance = 0;

	public StartupParameters() {
		prepareOptions();
		decisionRg = new Random(909090);
		isDriverRoutingRg = new Random(919191);
		transitionDuration = DEFAULT_TRANSITION_DURATION;
		// modelSeed = System.currentTimeMillis();
		modelSeed = 121212;
		genSeed = 31 * modelSeed;

		modelRg = new Random(modelSeed);
		genRg = new Random(genSeed);
		learnPhaseCount = DEFAULT_LEARN_REP_COUNT;
	}

	// =================================================================`==================
	// --- SET UP METHODS
	// ===================================================================================

	public void parseOptions(String[] args, PrintWriter console) throws ParseException {
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(options, args);

		if (cmd.hasOption('h')) {
			printUsage(console);
			System.exit(0);
		}

		commandLineMode = cmd.hasOption('g');
		isZoneAwareness = cmd.hasOption('z');
		visualization = cmd.hasOption('v');

		if (cmd.hasOption('r')) {
			rerouting = Boolean.parseBoolean(cmd.getOptionValue('r'));
		}
		if (cmd.hasOption('e')) {
			enablePrediction = Boolean.parseBoolean(cmd.getOptionValue('e'));
		}
		if (cmd.hasOption('X')) {
			setCarMoveModel(cmd.getOptionValue('X'));
			LOGGER.info("carMoveModel=" + carMoveModel);
		}
		minimalSpeedUsingPrediction = cmd.hasOption('m');
		if (cmd.hasOption('t')) {
			try {
				transitionDuration = Integer.parseInt(cmd.getOptionValue('t'));
				if (transitionDuration < 0) {
					throw new NumberFormatException();
				}
				console.print("tr=" + transitionDuration);
			} catch (NumberFormatException e) {
				error("Error: invalid transition duration - must be a positive number", e);
			}
		}
        if (cmd.hasOption('Q')) {
            physModule = cmd.getOptionValue('Q');
        }
        if (cmd.hasOption('Z')){
            switchTime = Integer.parseInt(cmd.getOptionValue('Z'));
        }
        if (cmd.hasOption('Y')) {
            minSafeDistance = Integer.parseInt(cmd.getOptionValue('Y'));
        }
		if (cmd.hasOption('d')) {
			try {
				routeDecisionTh = Integer.parseInt(cmd.getOptionValue('d'));
				if (routeDecisionTh < 0) {
					throw new NumberFormatException();
				}
				console.print(" dec=" + routeDecisionTh);
			} catch (NumberFormatException e) {
				error("Error: invalid routeDecisionTh - must be a nonnegative number", e);
			}
		}
		if (cmd.hasOption('k')) {
			try {
				isDriverRoutingTh = Integer.parseInt(cmd.getOptionValue('k'));
				if (isDriverRoutingTh < 0) {
					throw new NumberFormatException();
				}
				console.print(", drv=" + isDriverRoutingTh);
			} catch (NumberFormatException e) {
				error("Error: invalid isDriverRoutingTh - must be a nonnegative number", e);
			}
		}
		if (cmd.hasOption('u')) {
			try {
				globalUpdateInterval = Integer.parseInt(cmd.getOptionValue('u'));
				if (globalUpdateInterval < 0) {
                    throw new NumberFormatException();
				}
				console.print(", gui=" + globalUpdateInterval);
			} catch (NumberFormatException e) {
				error("Error: invalid isDriverRoutingTh - must be a nonnegative number", e);
            }
        }
        if (cmd.hasOption('s')) {
			try {
				setModelSeed(Long.parseLong(cmd.getOptionValue('s')));
			} catch (NumberFormatException e) {
				error("Error: invalid model seed - must be a number", e);
			}
		}
		if (cmd.hasOption('S')) {
			try {
				setGenSeed(Long.parseLong(cmd.getOptionValue('S')));
			} catch (NumberFormatException e) {
				error("Error: invalid traffic generation seed - must be a number", e);
			}
		}
		if (cmd.hasOption('l')) {
			try {
				learnPhaseCount = Integer.parseInt(cmd.getOptionValue('l'));
				if (learnPhaseCount < 0) {
					throw new NumberFormatException();
				}
			} catch (NumberFormatException e) {
				error("Error: invalid learning phase count - must be a nonnegative number", e);
			}
		}
		if (cmd.hasOption('o')) {
			statFileName = cmd.getOptionValue('o');
		}

		if (cmd.hasOption('a')) {
			predictionModule = cmd.getOptionValue('a');
		}



		String[] leftArgs = cmd.getArgs();
		if (leftArgs.length != 3) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			printUsage(pw);
			error("Invalid number of arguments " + sw);
		}
		algorithmName = leftArgs[0];
		modelFile = leftArgs[1];
		trafficSchemeFile = leftArgs[2];
	}

	private void printUsage(PrintWriter writer) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(PROG_NAME, options);
		writer.println("algorithms:");

		//TODO rewrite it - options has changed
		//TODO: Pair from java.lang3 is now used
//		final EvalModuleProvider[] providers = getEvalProviders();
//		for (int i = 0; i < providers.length; i++) {
//			EvalModuleProvider provider = providers[i];
//			writer.println("\t" + provider.getAlgorithmCode() + ": " + provider.getAlgorithmName());
//			writer.println("\t parameters:");
//
//			Iterator<KeyValPair> iter = provider.getParamsDescription();
//			while (iter.hasNext()) {
//				KeyValPair pair = iter.next();
//				writer.println("\t\t" + pair.getKey() + ": " + pair.getVal());
//			}
//
//		}
	}

	public static EvalModuleProvider[] getEvalProviders() {
		return new EvalModuleProvider[]{new SOTLModuleProvider(), new RLModuleProvider(), new RLCDModuleProvider(), new EmptyModuleProvider("sync"), new EmptyModuleProvider("static")};
	}

	// ===================================================================================
	// --- ACCESSOR METHODS - GET/SET
	// ===================================================================================

	public String getStatFileName() {
		return statFileName;
	}

	public void setStatFileName(String statFileName) {
		this.statFileName = statFileName;
	}

	public boolean isRerouting() {
		return rerouting;
	}

	public void setRerouting(boolean rerouting) {
		this.rerouting = rerouting;
	}

	public boolean isCommandLineMode() {
		return commandLineMode;
	}

	public void setCommandLineMode(boolean commandLineMode) {
		this.commandLineMode = commandLineMode;
	}

	public boolean isVisualization() {
		return visualization;
	}

	public void setVisualization(boolean visualization) {
		this.visualization = visualization;
	}

	public boolean isMinimalSpeedUsingPrediction() {
		return minimalSpeedUsingPrediction;
	}

	public void setMinimalSpeedUsingPrediction(boolean minimalSpeedUsingPrediction) {
		this.minimalSpeedUsingPrediction = minimalSpeedUsingPrediction;
	}

	public int getTransitionDuration() {
		return transitionDuration;
	}

	public void setTransitionDuration(int transitionDuration) {
		this.transitionDuration = transitionDuration;
	}

	public String getAlgorithmName() {
		return algorithmName;
	}

	public void setAlgorithmName(String algorithmName) {
		this.algorithmName = algorithmName;
	}

	public String getModelFile() {
		return modelFile;
	}

	public void setModelFile(String modelFile) {
		this.modelFile = modelFile;
	}

	public String getTrafficSchemeFile() {
		return trafficSchemeFile;
	}

	public void setTrafficSchemeFile(String trafficSchemeFile) {
		this.trafficSchemeFile = trafficSchemeFile;
	}

	public long getModelSeed() {
		return modelSeed;
	}

	public void setModelSeed(long modelSeed) {
		this.modelSeed = modelSeed;
		modelRg.setSeed(modelSeed);
	}

	public long getGenSeed() {
		return genSeed;
	}

	public void setGenSeed(long genSeed) {
		this.genSeed = genSeed;
		genRg.setSeed(genSeed);
	}

	public Random getModelRg() {
		return modelRg;
	}

	public Random getGenRg() {
		return genRg;
	}

	public CarMoveModel getCarMoveModel() {
		return carMoveModel;
	}

	public void setCarMoveModel(String data) {
		carMoveModel = new CarMoveModel(data);
	}

	public int getLearnPhaseCount() {
		return learnPhaseCount;
	}

	public void setLearnPhaseCount(int learnPhaseCount) {
		this.learnPhaseCount = learnPhaseCount;
	}

	// ===================================================================================
	// --- HELPER ERROR HANDLING
	// ===================================================================================

	private static void error(String text, Throwable error) {
		LOGGER.error(text + "\n  Details: " + error.getMessage());

		System.exit(1);
	}

	private static void error(String text) {
		LOGGER.error(text);
		System.exit(1);
	}

	public int getRouteDecisionTh() {
		return routeDecisionTh;
	}

	public Random getDecisionRg() {
		return decisionRg;
	}

	public int getDriverRoutingTh() {
		return isDriverRoutingTh;
	}

	public Random getDriverRoutingRg() {
		return isDriverRoutingRg;
	}

	public long getGlobalInforUpdateInterval() {
		return globalUpdateInterval;
	}

	public boolean isEnablePrediction() {
		return enablePrediction;
	}

	public String getPredictionModule() {
		return predictionModule;
	}

	public boolean isZoneInfoIncluded() {
		return isZoneAwareness;
	}

    public String getPhysModule() {
        return physModule;
    }

    public int getSwitchTime() {
        return switchTime;
    }

    public int getMinSafeDistance() {
        return minSafeDistance;
    }

	private void prepareOptions() {
		options.addOption("v", "visualization", false, "turns on visulization (default: off)");
		options.addOption("z", "zoneAware", false, "turns on zone awareness (default: off)");
		options.addOption("m", "minimalSpeed", false, "minimal speed when using prediction (default: off)");
		options.addOption("p", "opanel", false, "opanel (default: off)");
		options.addOption("g", "commandLine", false, "is command line (default: off)");
		options.addOption("h", "help", false, "shows help");
        options.addOption(OptionBuilder.withLongOpt("realModule").hasArg().withType(String.class).withDescription("real module config").create('Q'));
		options.addOption(OptionBuilder.withLongOpt("switchTime").hasArg().withType(Integer.class).withDescription("time to switch lanes").create('Z'));
		options.addOption(OptionBuilder.withLongOpt("minSafeDistance").hasArg().withType(Integer.class).withDescription("minimum distance to switch lanes").create('Y'));
		options.addOption(OptionBuilder.withLongOpt("rerouting").hasArg().withType(String.class).withDescription("turns on rerouting (default: off)").create('r'));
		options.addOption(OptionBuilder.withLongOpt("prediction").hasArg().withType(String.class).withDescription("turns on prediction (default: off)").create('e'));
		options.addOption(OptionBuilder.withLongOpt("transitionDuration").hasArg().withType(Integer.class).withDescription("sets the druation of traffic lights' transitional state (default: 8)").create('t'));
		options.addOption(OptionBuilder.withLongOpt("modelSeed").hasArg().withType(Long.class).withDescription("sets the seed of the traffic simulator RNG (default: based on the system clock)").create('s'));
		options.addOption(OptionBuilder.withLongOpt("genSeed").hasArg().withType(Long.class).withDescription("sets the seed of the traffic generator RNG (default: based on the system clock)").create('S'));
		options.addOption(OptionBuilder.withLongOpt("learnPhaseCount").hasArg().withType(Long.class).withDescription("number of learning phases (default: 0)").create('l'));
		options.addOption(OptionBuilder.withLongOpt("statFile").hasArg().withType(String.class).withDescription("statistics file name (default: statistics are generated)").create('o'));
		options.addOption(OptionBuilder.withLongOpt("carMoveModel").hasArg().withType(String.class).withDescription("car move model").create('X'));
		options.addOption(OptionBuilder.withLongOpt("routeDecisionTh").hasArg().withType(Integer.class).withDescription("parameter th for route decition (default: 0)").create('d'));
		options.addOption(OptionBuilder.withLongOpt("driverRoutingTh").hasArg().withType(Integer.class).withDescription("paramater th for driver routing (default: 0)").create('k'));
		options.addOption(OptionBuilder.withLongOpt("globalUpdateInterval").hasArg().withType(Long.class).withDescription("global update interval (default: 300)").create('u'));
		options.addOption(OptionBuilder.withLongOpt("predictionModule").hasArg().withType(String.class).withDescription("prediction module name").create('a'));
	}
}

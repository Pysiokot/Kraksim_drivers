package pl.edu.agh.cs.kraksim;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import pl.edu.agh.cs.kraksim.main.Simulation;
import pl.edu.agh.cs.kraksim.main.gui.MainVisualisationPanel;

import javax.swing.*;
import java.util.Properties;

public class KraksimRunner {
	public static final Logger LOGGER = Logger.getLogger(KraksimRunner.class);
	
	/**
	 * Main
	 *
	 * @param args may contain config file path
	 */
	public static void main(String[] args) {
		if (args.length > 0){
            KraksimConfigurator.setConfigPath(args[0]);
        }

		final Properties props = KraksimConfigurator.getPropertiesFromFile();

		// we assume that if there is no word about visualisation in config,
		// then it is necessary...
		boolean visualise = true;
		// but if there is...
		if (props.containsKey("visualization") && props.getProperty("visualization").equals("false")) {
			visualise = false;
		}

		// set up Logger
		PropertyConfigurator.configure("src\\main\\resources\\log4j.properties");


		// set up the prediction
		String predictionConfig = props.getProperty("enablePrediction");
		String predictionFileConfig = props.getProperty("predictionFile");
		if (!"true".equals(predictionConfig)) {
			KraksimConfigurator.disablePrediction();
			LOGGER.info("Prediction disabled");
		} else {
			KraksimConfigurator.configurePrediction(predictionFileConfig);
			LOGGER.info("Prediction configured");
		}

		// start simulation - with or without visualisation
		if (visualise) {
			String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
			try {
				UIManager.setLookAndFeel(lookAndFeel);
			} catch (Exception e) {
				e.printStackTrace();
			}

			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JFrame frame = new JFrame("Kraksim Visualiser");
					frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

					frame.getContentPane().add(new MainVisualisationPanel(props));
					frame.setSize(370, 270);
					frame.setVisible(true);
				}
			});
		} else {
			Thread simThread = new Thread(new Simulation(KraksimConfigurator.prepareInputParametersForSimulation(props)));

			simThread.start();
			try {
				simThread.join();
			} catch (InterruptedException e) {
				LOGGER.error("InterruptedException", e);
			}
		}
	}
}

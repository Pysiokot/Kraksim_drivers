package pl.edu.agh.cs.kraksim;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import pl.edu.agh.cs.kraksim.main.Simulation;
import pl.edu.agh.cs.kraksim.main.StartupParameters;
import pl.edu.agh.cs.kraksim.main.gui.MainVisualisationPanel;
import pl.edu.agh.cs.kraksim.main.gui.SimulationVisualizer;
import pl.edu.agh.cs.kraksim.ministat.GatewayMiniStatExt;
import pl.edu.agh.cs.kraksim.sna.centrality.KmeansClustering;
import weka.core.PropertyPath;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public class KraksimRunner {
	public static final Logger LOGGER = Logger.getLogger(KraksimRunner.class);
	public static final Logger LOGGER2 = Logger.getLogger(GatewayMiniStatExt.class);
	/**
	 * Main
	 *
	 * @param args may contain config file path
	 */
	public static void main(String[] args) {
		if (args.length > 0){
            KraksimConfigurator.setConfigPath(args[0]);
        }

		Properties props = KraksimConfigurator.getPropertiesFromFile();

		// we assume that if there is no word about visualisation in config,
		// then it is necessary...
		// but if there is...
		boolean visualise = !(props.containsKey("visualization") && props.getProperty("visualization").equals("false"));

		// set up Logger
		PropertyConfigurator.configure("src\\main\\resources\\log4j.properties");



		// set up the prediction
		String predictionEnabled = props.getProperty("enablePrediction");
		String predictionFileConfig = props.getProperty("predictionFile");
		if (!"true".equals(predictionEnabled)) {
			KraksimConfigurator.disablePrediction();
			LOGGER.info("Prediction disabled");
		} else {
			KraksimConfigurator.configurePrediction(predictionFileConfig);
			LOGGER.info("Prediction configured with file: " + predictionFileConfig);
		}

		// start simulation - with or without visualisation
		if (visualise) {
			String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
			try {
				UIManager.setLookAndFeel(lookAndFeel);
			} catch (Exception e) {
				e.printStackTrace();
			}

			Properties finalProps = props;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JFrame frame = new JFrame("Kraksim Visualiser");
					frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

					frame.getContentPane().add(new MainVisualisationPanel(finalProps));
					frame.setSize(800, 600);
                    frame.setVisible(true);
                    Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
                    int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
                    int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
                    frame.setLocation(x, y);
				}
			});
		} else {
			int s = 0;
			int c = 0;
			int j=0,i;

			int counter = 0;
			long startTime = System.currentTimeMillis();
			for(i=s;i<c+1;i++) {
				KraksimConfigurator.setConfigPath(String.format("configuration/kraksim.properties"));
				props = KraksimConfigurator.getPropertiesFromFile();
				int n = Integer.parseInt(KraksimConfigurator.getProperty("repeats"));
				KmeansClustering.setProperties(props);
				long confTime = System.currentTimeMillis();
				LOGGER.info("Running configuration nr: "+(i)+" (" +(i-s+1)+"/"+(c-s+1)+")");
////				LOGGER.info("Simulation nr "+((i-s)*n+1)+"/"+((c+1-s)*n)+" started...");
////				Simulation sim = new Simulation(KraksimConfigurator.prepareInputParametersForSimulation(props, 0));
////				Thread simThread = new Thread(sim);
////				simThread.start();
//				try {
//					simThread.join();
//				} catch (InterruptedException e) {
//					LOGGER.error("InterruptedException", e);
//				}
				//System.out.println("\n");
				LOGGER.info("Simulation nr "+counter+"/"+(c-s+1)*n+" started...");
				for (j = 0; j < n; j++) {
					FileAppender appender=null;
					try {
						String dest_path=KraksimConfigurator.getProperty("out_dic_name")+"\\"+KraksimConfigurator.getProperty("map_name")+"\\"+KraksimConfigurator.getProperty("density")+"\\"+KraksimConfigurator.getProperty("gen_name")+"\\"+"logs"+KraksimConfigurator.getProperty("nlvl");
						if(KraksimConfigurator.getProperty("qlearning").equals("true"))
							dest_path+="qlearning";
						appender = new FileAppender(new PatternLayout(),dest_path+"\\travellogs"+j+".log");
						LOGGER2.addAppender(appender);
					} catch (IOException e) {
						e.printStackTrace();
					}
					//LOGGER.info("Simulation nr "+((i-s)*n+j+1)+"/"+((c+1-s)*n)+" started...");
					Thread runner = new Thread(new Simulation(KraksimConfigurator.prepareInputParametersForSimulation(props, j)));
					runner.start();
					try {
						runner.join();
					} catch (InterruptedException e) {
						LOGGER.error("InterruptedException", e);
					}
					counter += 1;
					if(j%1==0) {
						long elapsed = System.currentTimeMillis() - startTime;
						long confElapsed = System.currentTimeMillis() - confTime;
						printStats(elapsed, confElapsed, counter, ((c-s+1)*n), j+1, i);
//						double avgTime = (elapsed/((i-s)*n+j+1) *(((c+1-s)*n)-((i-s)*n+j+1)))/1000.0;
//						LOGGER.info("Time passed: "+ elapsed/1000.0 + " s");
//						LOGGER.info("Estimated time left: " + avgTime + " s");
					}
					if (appender != null) {
						LOGGER2.removeAppender(appender);
						appender.close();
					}
					//LOGGER.info("Simulation nr " + j + " ended.");
					//System.out.println("\n");

//
//					String dest_path="D:\\studia\\sem9\\magisterka\\kraksim_final\\kraksim\\"+"\\"+KraksimConfigurator.getProperty("out_dic_name")+"\\"+KraksimConfigurator.getProperty("map_name")+"\\"+KraksimConfigurator.getProperty("density")+"\\"+KraksimConfigurator.getProperty("gen_name")+"\\"+"logs";
//
//					if(KraksimConfigurator.getProperty("qlearning")=="true")
//						dest_path+="q";
//					dest_path+="\\"+((c-s+1)*n);
//					try {
//						File f = new File(dest_path);
//						f.mkdirs();
//						Files.move(Paths.get("D:\\studia\\sem9\\magisterka\\kraksim_final\\kraksim\\output\\logs"), Paths.get(dest_path), StandardCopyOption.REPLACE_EXISTING);
//					} catch (IOException e) {
//						e.printStackTrace();
//						//"D:\\studia\\sem9\\magisterka\\kraksim_final\\kraksim\\output\\logs"
//					}

					System.gc();
				}

			}
			long elapsed = System.currentTimeMillis() - startTime;
			LOGGER.info("All simulations have ended in time: "+(elapsed / 1000.0)+" s");
		}
	}

	public static void printStats(long elapsed, long confElapsed, int nr, int n, int confs, int i){
		double avgTimePerSim = elapsed/nr/1000.0;
		double avgTimeInConf = confElapsed/confs/1000.0;
		double simLeft = n-nr;
		double avgTime = (avgTimePerSim * simLeft);
		double avgTimeConf = (avgTimeInConf* simLeft);
		System.out.println("Time passed: "+ elapsed/1000.0 + " s, average sim duration: "+avgTimePerSim+" s, average in conf: "+avgTimeInConf+" s");
		System.out.println("Estimated time left: " + (int)avgTime/60+" m "+ (int)avgTime%60 + " s, based on conf: " + (int)avgTimeConf/60+" m "+ (int)avgTimeConf%60 + " s");
		LOGGER.info("Simulation nr "+nr+"/"+n+" ("+i+") started...");
		//System.out.println(confElapsed+"   "+(nr-confs));
		//System.out.println(elapsed+"   "+(nr-1));

	}
}

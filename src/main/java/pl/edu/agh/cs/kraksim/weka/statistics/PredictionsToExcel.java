package pl.edu.agh.cs.kraksim.weka.statistics;

import jxl.Workbook;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.weka.PredictionSetup;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class PredictionsToExcel {
	private static final Logger LOGGER = Logger.getLogger(PredictionsToExcel.class);
	private final String trafficFileName;

	public PredictionsToExcel(PredictionSetup setup) {
		trafficFileName = setup.getOutputMainFolder();
	}

	public void writeToExcel(int actualTurn, Archive<Double> classData, Archive<Double> classDataPrediction) {
		String folderPath = "excel";
		String filePath = folderPath + File.separator + trafficFileName + actualTurn + "_prediction.xls";
		LOGGER.debug("Create folder");
		File folder = new File(folderPath);
		if (!folder.isDirectory()) {
			folder.mkdirs();
		}
		WritableWorkbook workbook;
		try {
			workbook = Workbook.createWorkbook(new File(filePath));
			LOGGER.debug("Write fields");
			writeFields(workbook, actualTurn, classData, classDataPrediction);

			workbook.write();
			workbook.close();
			LOGGER.debug("Workbook closed");
		} catch (IOException | WriteException e) {
			e.printStackTrace();
		}
	}

	private static void writeFields(WritableWorkbook workBook, int actualTurn, Archive<Double> classData, Archive<Double> classDataPrediction) throws WriteException {
		SortedSet<Integer> turns = new TreeSet<>(classDataPrediction.getTurns());
		List<Double> c = classData.getCongestionListByTurn(turns.first());
		WritableSheet[] sheetTable = new WritableSheet[c.size()];
		for (int i = 0; i < c.size(); i++) {
			sheetTable[i] = workBook.createSheet(String.valueOf(i), i);
		}
		int j = 0;
		for (Integer turn : turns) {
			if (turn < actualTurn) {
				List<Double> congestionList = classData.getCongestionListByTurn(turn);
				List<Double> predictionList = classDataPrediction.getCongestionListByTurn(turn);
				for (int i = 0; i < congestionList.size(); i++) {
					Double realValue = congestionList.get(i);
					Double predictedValue = predictionList.get(i);

					WritableSheet sheet = sheetTable[i];
					Number number = new Number(0, j, turn);
					sheet.addCell(number);
					Number number2 = new Number(1, j, realValue);
					sheet.addCell(number2);
					Number number3 = new Number(2, j, predictedValue);
					sheet.addCell(number3);
				}
			}
			j++;
		}
	}
}

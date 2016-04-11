package pl.edu.agh.cs.kraksim.statistics.charts;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import pl.edu.agh.cs.kraksim.ministat.CityMiniStatExt;

import javax.swing.*;
import java.awt.*;

public abstract class NumberChart extends JPanel {

	private static final long serialVersionUID = 7011369834305087337L;
	private long milis = 0;

	private TimeSeries series;
	private JFreeChart chart;
	protected CityMiniStatExt cityStat;

	public NumberChart(String title, CityMiniStatExt cityStat) {
		this.cityStat = cityStat;
		init(title);
	}

	private void init(String title) {
		setLayout(new BorderLayout());
		BoxLayout flow = new BoxLayout(this, BoxLayout.PAGE_AXIS);

		setLayout(flow);

		series = new TimeSeries(title, Millisecond.class);

		XYDataset dataset = createDataset();
		chart = createChart(dataset, title);
		ChartPanel chartPanel = new ChartPanel(chart);
		setPreferredSize(new java.awt.Dimension(500, 270));
		add(chartPanel);
	}

	private XYDataset createDataset() {
		return new TimeSeriesCollection(series);
	}

	/**
	 * Creates a chart.
	 *
	 * @param dataset the data for the chart.
	 * @return a chart.
	 */
	private JFreeChart createChart(XYDataset dataset, String title) {

		// create the chart...
		 JFreeChart chart = ChartFactory.createTimeSeriesChart(title, // title
				"X", "Y", dataset, // axes + data
				true, // legend
				true, // tooltips
				false // urls
		);

		// final JFreeChart chart = ChartFactory.createXYLineChart("Car count",
		// // chart
		// // title
		// "X", // x axis label
		// "Y", // y axis label
		// dataset, // data
		// PlotOrientation.VERTICAL, true, // include legend
		// true, // tooltips
		// false // urls
		// );

		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
		chart.setBackgroundPaint(Color.white);

		// final StandardLegend legend = (StandardLegend) chart.getLegend();
		// legend.setDisplaySeriesShapes(true);

		// get a reference to the plot for further customisation...
		 XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.lightGray);
		// plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);

		// final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		// renderer.setSeriesLinesVisible(0, false);
		// renderer.setSeriesShapesVisible(1, false);
		// plot.setRenderer(renderer);

		// change the auto tick unit selection to integer units only...
		// final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		// rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		ValueAxis axis = plot.getDomainAxis();
		// axis.setRange(0, 60000);
		axis.setAutoRange(true);
		axis.setFixedAutoRange(60000.0); // 60 seconds
		axis = plot.getRangeAxis();
		axis.setRange(0.0, 500);
		// OPTIONAL CUSTOMISATION COMPLETED.

		return chart;
	}

	public void setRange(double low, double high) {
		ValueAxis axis = chart.getXYPlot().getRangeAxis();
		axis.setRange(low, high);
	}

	public void addData(double data) {
		series.addOrUpdate(new FixedMillisecond(milis), data);
		milis += 100;
	}

	public abstract void refresh();
}

package autograder.metrics;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

import autograder.student.AutograderSubmissionMap;
import autograder.student.GradeCalculator;

public class GraphingTool {
	
	private GradeCalculator calculator;

	@Inject
	public GraphingTool(GradeCalculator calculator) {
		this.calculator = calculator;
	}
	
	public void writeScoreHistorgram(AutograderSubmissionMap submissions, String title, OutputStream output) throws IOException {
		List<Double> scores = new ArrayList<>();
		
		submissions.listStudents()
			.forEach(submission -> scores.add(calculator.calculateGrade(submission)));
		
		JFreeChart chart = buildHistogram(title, scores, 11);
		ChartUtilities.writeChartAsPNG(output, chart, 600, 400);
	}
	
	public JFreeChart buildHistogram(String title, List<? extends Number> data, int bins) {
		HistogramDataset dataset = new HistogramDataset();
		dataset.addSeries("Histogram", convertData(data), bins);
		
		dataset.setType(HistogramType.FREQUENCY);
		
		JFreeChart chart = ChartFactory.createHistogram(title, "Scores (%)", "Num of Students", dataset, PlotOrientation.VERTICAL, false, false, false);
		return chart;
	}
	
	private double[] convertData(List<? extends Number> data) {
		double[] returnValues = new double[data.size()];
		for(int dataIdx = 0; dataIdx < data.size(); dataIdx++) {
			returnValues[dataIdx] = data.get(dataIdx).doubleValue();
		}
		return returnValues;
	}
}

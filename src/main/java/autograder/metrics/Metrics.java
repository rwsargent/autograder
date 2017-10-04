package autograder.metrics;

import java.util.Arrays;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

/**
 * POJO used for holding data.
 * 
 * @author ryans
 */
public class Metrics {

	public double stdv;
	public double max;
	public double mean;
	public double min;
	public double[] mode;
	public double[] normal;
	public double median;

	/**
	 * The metrics returned include max, min, mean, mode, median, and standard deviation.
     * The format returned is: 
     * max      99.9
     * min      99.9
     * mean     99.9
     * mode     [1, 2]
     * median   99.9
     * std  dev 99.9
	 * @param data
	 * @return
	 */
	public static Metrics calculate(double[] data) {
		Metrics metric = new Metrics();
		
		Arrays.sort(data);
		
		metric.max = data[data.length -1];
		metric.mean = StatUtils.mean(data);
		metric.min = data[0];
		metric.mode = StatUtils.mode(data);
		metric.median = data[data.length / 2];
		metric.stdv = new StandardDeviation().evaluate(data, metric.mean);
		
		metric.normal = StatUtils.normalize(data);
		
		return metric;
	}
	
	public String printBasicReport() {
		StringBuilder sb = new StringBuilder();
		// -7, for the length of "median"
		sb.append(String.format("%-8s %02.2f\n", "max", max));
		sb.append(String.format("%-8s %02.2f", "min", min));
		sb.append(String.format("%-8s %02.2f", "mean", mean));
		sb.append(String.format("%-8s %2s", "mode", Arrays.toString(mode)));
		sb.append(String.format("%-8s %02.2f", "median", median));
		sb.append(String.format("%-8s %02.2f", "std dev", stdv));
		
		return sb.toString();
	}
}

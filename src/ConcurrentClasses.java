/*
 * A collection of Callable and Runnables used for
 * concurrent data processing. Each callable corresponds
 * to a data processing method in DataPoint
 */
import java.util.List;
import java.util.concurrent.Callable;


class MAECallable implements Callable<Double> {
	private double[] original;
	private double[] interpolated;
	
	public MAECallable(double[] original, double[] interpolated) {
		this.original= original;
		this.interpolated = interpolated;
	}
	
	@Override
	public Double call() throws Exception {
		return DataPoint.MAE(original, interpolated);
	}
}

class MSECallable implements Callable<Double> {
	private double[] original;
	private double[] interpolated;
	
	public MSECallable(double[] original, double[] interpolated) {
		this.original= original;
		this.interpolated = interpolated;
	}
	
	@Override
	public Double call() throws Exception {
		return DataPoint.MSE(original, interpolated);
	}
}

class RMSECallable implements Callable<Double> {
	private double[] original;
	private double[] interpolated;
	
	public RMSECallable(double[] original, double[] interpolated) {
		this.original= original;
		this.interpolated = interpolated;
	}
	
	@Override
	public Double call() throws Exception {
		return DataPoint.RMSE(original, interpolated);
	}
}

class MARECallable implements Callable<Double> {
	private double[] original;
	private double[] interpolated;
	
	public MARECallable(double[] original, double[] interpolated) {
		this.original= original;
		this.interpolated = interpolated;
	}
	
	@Override
	public Double call() throws Exception {
		return DataPoint.MARE(original, interpolated);
	}
}

class InitNeighborRunnable implements Runnable {
	private DataPoint dataPoint;
	private List<DataPoint> pointList;
	public InitNeighborRunnable(DataPoint dataPoint, List<DataPoint> pointList) {
		this.dataPoint = dataPoint;
		this.pointList = pointList;
	}
	@Override
	public void run() {
		dataPoint.init(pointList);
	}
}

class LoocvCallable implements Callable<Double> {
	public DataPoint target;
	private int N;
	private double p;
	public LoocvCallable(DataPoint target, int N, double p) {
		this.target = target;
		this.N = N;
		this.p = p;
	}

	@Override
	public Double call() throws Exception {
		return new Double(target.loocv(N, p));
	}
}

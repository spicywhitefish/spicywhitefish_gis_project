/*
 * Class to hold individual representations of PM25 data,
 * and nearest neighbors
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

public class DataPoint {
	private static final int MAX_NEIGHBORS = 5;
	double x;
	double y;
	int time;
	double measurement;
	private DataPoint[] neighbors;
	private boolean isInitialized;

	public DataPoint(double x, double y, int time, double measurement) {
		this.x=x;
		this.y=y;
		this.time=time;
		this.measurement=measurement;
		this.neighbors = new DataPoint[MAX_NEIGHBORS];
		isInitialized = false;
	}
	
	//Overloaded constructor for parseInputFile
	//TODO: remove unneccessary duplicate constructor
	public DataPoint(double[] args) {
		this.x=args[0];
		this.y=args[1];
		this.time=(int)(args[2]);
		this.measurement=args[3];
		this.neighbors = new DataPoint[MAX_NEIGHBORS];
		isInitialized = false;
	}
	
	//Initialize neighbors if not already performed
	public void init(List<DataPoint> pointList) {
		if (!isInitialized) {
			this.initNeighbors(pointList);
		}
	}
	//Intialize closest neighbors. This runs in O(N) so
	//we try to only do this once per data point.
	private void initNeighbors(List<DataPoint> pointList) {
		neighbors = new DataPoint[MAX_NEIGHBORS];
		double[] distances = new double[MAX_NEIGHBORS];
		for (DataPoint element : pointList) {
			//Use dist^2 for a slight performance bump by avoiding sqrt
			double distance = this.getDistanceSquaredTo(element);
			//Am I trying to add myself as one of my neighbors?
			if (element == this || distance == 0)
				//Then don't consider this DataPoint a neighbor
				continue;
			//Have we filled up our neighbor list?
			boolean hasNull = hasNull(neighbors);
			//DataPoint to
			DataPoint currentPoint = element;
			for (int i=0; i<neighbors.length; i++) {
				//Is one of our neighbor slots empty?
				if (neighbors[i] == null) {
					//Then add this DataPoint, regardless of how far away it is
					neighbors[i] = element;
					distances[i] = distance;
					//Exit loop to avoid adding this point to neighbors again
					break;
				}
				//Is this point closer than the current neighbor?
				if (distance < distances[i] && !hasNull) {
					//Then swap out the points. Save the point that
					// wasn't quite as close and try to insert it in 
					//neighbors somewhere else. 
					DataPoint swapPoint = neighbors[i];
					double swapDist = distances[i];
					neighbors[i] = element;
					distances[i] = distance;
					//Use the point we kicked out as our candidate
					//DataPoint for insertion
					element = swapPoint;
					distance = swapDist;
					//Restart the neighbors loop to search all of the neighbors
					//for a place for the point that we kicked out.
					i=0;
				}
			}
		}
		//Sort neighbors so getting neighbors < 5 is simply
		// neighbors[0] -> neighbors[n-1]
		this.sortNeighbors();
		this.isInitialized = true;
	}
	private static boolean hasNull(DataPoint[] points) {
		for(int i=0; i<points.length; i++) {
			if (points[i]==null) {
				return true;
			}
		}
		return false;
	}
	
	//Selection sort neighbors into increasing order by distance.
	private void sortNeighbors() {
		for(int i=0; i<neighbors.length; i++) {
			double iDist = this.getDistanceSquaredTo(neighbors[i]);
			for(int j=i+1; j<neighbors.length; j++) {
				double jDist = this.getDistanceSquaredTo(neighbors[j]);
				if (jDist < iDist) {
					DataPoint swap = neighbors[i];
					neighbors[i]=neighbors[j];
					neighbors[j] = swap;
				}
			}
		}
	}
	/*
	 * Initialize a list of DataPoints in parallel
	 */
	public static void initPoints(List<DataPoint> points) {
		//Create a thread pool for task execution
		ExecutorService executor = Executors.newCachedThreadPool();
		for (DataPoint element : points) {
			//submit a task to initialize this data point
			executor.submit(new InitNeighborRunnable(element, points));
		}
		//Signal thread pool that we have finished submitting tasks
		executor.shutdown();
		//Wait for the thread pool to finish
		while(!executor.isTerminated()) {
			try {
				executor.awaitTermination(1, TimeUnit.MINUTES);
			} catch (InterruptedException e) {
				JOptionPane.showMessageDialog(Application.app.frmDirectedStudyFinal, "Initialization Interrupted!", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	//Facade for interpolateValue
	public static double interpolateValue(double x, double y, int t,  List<DataPoint> points) {
		return interpolateValue(x, y, t, 3, 1, points);
	}
	//Return the result of loocv interpolation on a point
	public static double interpolateValue(double x, double y, int t, int N, double p, List<DataPoint> points) {
		DataPoint output = new DataPoint(x, y, t, 0);
		output.init(points);
		for(DataPoint neighbor : output.neighbors) {
			neighbor.init(points);
		}
		double sum = 0;
		for(int i=0; i<N; i++) {
			sum+= output.getLambda(output.neighbors, output.neighbors[i], p) * output.neighbors[i].measurement;
		}
		return sum;
	}
	//Return lambda value of loocv equation
	double getLambda(DataPoint[] neighbors, DataPoint selectedPoint, double p) {
		double di = this.getDistanceTo(selectedPoint);
		double numerator = Math.pow(1/di, p);
		double denominator = 0;
		for(DataPoint pointElement : neighbors) {
			double dk = this.getDistanceTo(pointElement);
			denominator += Math.pow(1/dk, p);
		}
		assert denominator != 0;
		return numerator / denominator;
	}
	//A slightly stripped down interpolate value to be run in parallel
	//on points that are known to be initialized.
	//TODO: consolidate with interpolateValue
	public double loocv(int N, double p) {
		double sum = 0;
		for(int i=0; i<N; i++) {
			sum+= this.getLambda(this.neighbors, this.neighbors[i], p) * neighbors[i].measurement;
		}
		return sum;
	}
	//Used in loocv equation
	public double getDistanceTo(DataPoint other) {
		return Math.sqrt((x-other.x)*(x-other.x)+(y-other.y)*(y-other.y)+(time-other.time)*(time-other.time));
	}
	//Performance optimization for finding closest neighbors
	public double getDistanceSquaredTo(DataPoint other) {
		return (x-other.x)*(x-other.x)+(y-other.y)*(y-other.y)+(time-other.time)*(time-other.time);
	}
	//Parse input file into a list of DataPoint
	public static List<DataPoint> parseFile(File f) throws FileNotFoundException {
		List<DataPoint> output = new ArrayList<DataPoint>();
		//File-reading scanner
		Scanner sc = null;
		try {
			sc = new Scanner(f);
			int index = 0;
			//Treat every 4 doubles as a DataPoint
			double[] dPArgs = new double[4];
			while (sc.hasNextDouble()) {
				double val = sc.nextDouble();
				dPArgs[index%4] = val;
				//Have we filled dataPointArgs?
				if (index % 4 == 3) {
					output.add(new DataPoint(dPArgs));
					dPArgs = new double[4];
				}
				index++;
			}
		} catch (FileNotFoundException e) {
			f = null;
			throw e;
		} finally {
			sc.close();
		}
		return output;
	}
	/*
	 * Perform loocv in parallel for n = (3,4,5) and p = (1,2,3)
	 *  and write results to the specified file
	 * TODO: split into two functions
	 */
	public static void generateLOOCV(File outFile) throws Exception {
		//Create a new thread pool for parallel execution
		ExecutorService executor = Executors.newCachedThreadPool();
		/*
		 * Easily the ugliest use of generics I have ever committed
		 * against mankind
		 * Outer list: loocv results lists (rows in output file)
		 * Inner List: loocv results per data point (a single row in output file)
		 * Future: result of loocv returned by the Callable instance.
		 * Double: The type of the result of the loocv Callable 
		 */
		List<List<Future<Double>>> futures = new ArrayList<List<Future<Double>>>();
		for(DataPoint element : Application.app.dataPoints) {
			List<Future<Double>> rowFutureList = new ArrayList<Future<Double>>(10);
			for(int N=3; N<=5; N++) {
				for(int p=1; p<=3; p++) {
					Callable<Double> worker = new LoocvCallable(element, N, p);
					Future<Double> loocvResult = executor.submit(worker);
					rowFutureList.add(loocvResult);
				}
			}
			futures.add(rowFutureList);
		}
		//Signal thread pool that we have finished submitting tasks
		executor.shutdown();
		FileWriter fw = new FileWriter(outFile, false);
		BufferedWriter bw = new BufferedWriter(fw);
		//Write results to the specified file
		for(int i=0; i< futures.size(); i++) {
			List<Future<Double>> row = futures.get(i);
			//A single row in the output text file
			StringBuilder sb = new StringBuilder();
			//Append the original Data Point value
			//TODO: make this not use a global variable
			sb.append(Application.app.dataPoints.get(i).measurement);
			sb.append(" ");
			//Wait for each loocv Callable to complete,
			// then append its result to the output row
			for(Future<Double> future : row) {
				sb.append(future.get().toString());
				sb.append(" ");
			}
			sb.append("\n");
			//Write the output row to the file
			bw.append(sb.toString());
		}
		bw.close();
	}
	
	//Compute the error values 
	public static double MAE(double[] original, double[] interpolated){
		assert original.length == interpolated.length;
		double sum=0;
		for (int i=0;i<original.length;i++){
			sum += Math.abs(interpolated[i]-original[i]);
		}
		return sum/original.length;
	}
	public static double MSE(double[] original, double[] interpolated){
		assert original.length == interpolated.length;
			double sum=0;
			for (int i=0;i<original.length;i++){
				sum += Math.pow(interpolated[i]-original[i],2);
			}
			return sum/original.length;
	}
	public static double RMSE(double[] original, double[] interpolated){
		assert original.length == interpolated.length;
		double sum=0;
		for (int i=0;i<original.length;i++){
			sum += Math.pow(interpolated[i]-original[i],2);
		}
		return Math.sqrt(sum/original.length);
	}
	public static double MARE(double[] original, double[] interpolated){
		assert original.length == interpolated.length;
		double sum=0;
		for (int i=0;i<original.length;i++){
			sum += (Math.abs(interpolated[i]-original[i]))/original[i];
		}
		return sum/original.length;
	}
	//Are we in the same position and from the same time?
	public boolean equals(Object o) {
		if (o instanceof DataPoint) {
			DataPoint dp = (DataPoint)(o);
			return this.x == dp.x && y == dp.y && time == dp.time; 
		} else
			return false;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(x);
		sb.append(" ");
		sb.append(y);
		sb.append(" ");
		sb.append(time);
		sb.append(" ");
		sb.append(measurement);
		return sb.toString();
	}
}
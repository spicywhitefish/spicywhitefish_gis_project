import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/*
 * Main entry-point class for the application.
 * Also contains UI elements and acts as a
 * singleton to any hold global data.
 * Created using Window Builder plugin for Eclipse
 */
public class Application {
	
	//Only application top-level window
	public JFrame frmDirectedStudyFinal;
	//Application singleton object
	public static Application app;
	//List of data points imported from a file
	public List<DataPoint> dataPoints;
	public File file;
	
	//UI elements
	public JMenuItem mntmInterpolateValue;
	public JMenuItem mntmImport; 
	public JMenuItem mntmParseFile;
	public JMenuItem mntmLoocv;
	public JLabel lblInterpolated;
	public JTextField txtInterpolated;
	public JTextField txtOpenedFile;
	private JLabel lblX;
	private JLabel lblY;
	private JLabel lblTime;
	private JLabel lblN;
	private JLabel lblP;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Application window = new Application();
					window.initialize();
					//Provide a static reference to our application so
					//we can reference it anywhere in this package.
					Application.app = window;
					window.frmDirectedStudyFinal.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
		
	/**
	 * Create the application.
	 */
	public Application() {
		/*
		* TODO: Search code for random access on dataPoints
		* and profile the ArrayList implementation against
		* a LinkedList implementation to determine whether
		* switching to LinkedList is worthwhile
		*/
		dataPoints = new ArrayList<DataPoint>();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmDirectedStudyFinal = new JFrame();
		frmDirectedStudyFinal.setTitle("Directed Study Final");
		frmDirectedStudyFinal.setBounds(100, 100, 800, 600);
		frmDirectedStudyFinal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmDirectedStudyFinal.getContentPane().setLayout(null);
		
		JLabel lblOpenedFile = new JLabel("File Opened:");
		lblOpenedFile.setBounds(12, 12, 98, 15);
		frmDirectedStudyFinal.getContentPane().add(lblOpenedFile);
		
		txtOpenedFile = new JTextField();
		txtOpenedFile.setEditable(false);
		txtOpenedFile.setText("No file opened.");
		txtOpenedFile.setBounds(12, 31, 318, 19);
		frmDirectedStudyFinal.getContentPane().add(txtOpenedFile);
		txtOpenedFile.setColumns(10);
		
		lblInterpolated = new JLabel("Interpolated Value:");
		lblInterpolated.setBounds(12, 159, 149, 15);
		frmDirectedStudyFinal.getContentPane().add(lblInterpolated);
		
		txtInterpolated = new JTextField();
		txtInterpolated.setBounds(157, 157, 165, 19);
		frmDirectedStudyFinal.getContentPane().add(txtInterpolated);
		txtInterpolated.setColumns(10);
		
		lblX = new JLabel("X:");
		lblX.setBounds(12, 78, 98, 15);
		frmDirectedStudyFinal.getContentPane().add(lblX);
		
		lblY = new JLabel("Y:");
		lblY.setBounds(122, 78, 98, 15);
		frmDirectedStudyFinal.getContentPane().add(lblY);
		
		lblTime = new JLabel("Time:");
		lblTime.setBounds(232, 78, 98, 15);
		frmDirectedStudyFinal.getContentPane().add(lblTime);
		
		lblN = new JLabel("Number of Neighbors:");
		lblN.setBounds(12, 105, 250, 15);
		frmDirectedStudyFinal.getContentPane().add(lblN);
		
		lblP = new JLabel("P Value: ");
		lblP.setBounds(12, 132, 98, 15);
		frmDirectedStudyFinal.getContentPane().add(lblP);
		
		JMenuBar menuBar = new JMenuBar();
		frmDirectedStudyFinal.setJMenuBar(menuBar);
		
		mntmImport = new JMenuItem("Import");
		mntmImport.setMaximumSize(new Dimension(80, 32767));
		mntmImport.setHorizontalAlignment(SwingConstants.LEFT);
		//Import menu item action listener
		mntmImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser();
				int returnVal = chooser.showOpenDialog(Application.app.frmDirectedStudyFinal);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					Application.app.file = chooser.getSelectedFile(); 
					txtOpenedFile.setText(chooser.getSelectedFile().getAbsolutePath());
					Application.app.hasFile(true);
				}
			}
		});
		menuBar.add(mntmImport);
		
		mntmParseFile = new JMenuItem("Parse File");
		mntmParseFile.setMaximumSize(new Dimension(100, 32767));
		mntmParseFile.setHorizontalAlignment(SwingConstants.LEFT);
		//Parse file menu item action listener
		mntmParseFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
				Application app = Application.app;
				app.parseFile();
				JOptionPane.showMessageDialog(frmDirectedStudyFinal, "File successfully parsed.", "Success", JOptionPane.PLAIN_MESSAGE);
				} catch (FileNotFoundException e) {
					JOptionPane.showMessageDialog(frmDirectedStudyFinal, "File not found!", "Error!", JOptionPane.ERROR_MESSAGE);
					Application.app.hasFile(false);
					txtOpenedFile.setText("No file opened.");					
				}
			}
		});
		mntmParseFile.setEnabled(false);
		menuBar.add(mntmParseFile);
		
		mntmInterpolateValue = new JMenuItem("Interpolate Value");
		mntmInterpolateValue.setMaximumSize(new Dimension(210, 32767));
		mntmInterpolateValue.setHorizontalAlignment(SwingConstants.LEFT);
		//Interpolate menu item action listener
		mntmInterpolateValue.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				double x = Double.parseDouble(JOptionPane.showInputDialog(Application.app.frmDirectedStudyFinal, "X?"));
				double y = Double.parseDouble(JOptionPane.showInputDialog(Application.app.frmDirectedStudyFinal, "Y?"));
				int time = Integer.parseInt(JOptionPane.showInputDialog(Application.app.frmDirectedStudyFinal, "Time?"));
				int n = Integer.parseInt(JOptionPane.showInputDialog(Application.app.frmDirectedStudyFinal, "Number of Neighbors?"));
				int p = Integer.parseInt(JOptionPane.showInputDialog(Application.app.frmDirectedStudyFinal, "P Value?"));
				lblX.setText("X: "+x);
				lblY.setText("Y: "+y);
				lblTime.setText("Time: "+time);
				lblN.setText("Number of Neighbors: "+n);
				lblP.setText("P Value: "+p);
				double value = DataPoint.interpolateValue(x, y, time, n, p, app.dataPoints);
				txtInterpolated.setText(value+"");
			}
		});
		mntmInterpolateValue.setEnabled(false);
		menuBar.add(mntmInterpolateValue);
		
		mntmLoocv = new JMenuItem("LOOCV");
		//Leave one out cross validation menu item action listener
		mntmLoocv.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//Ask the user to specify an input file of PM25 measurements
				JFileChooser chooser = new JFileChooser();
				int returnVal = chooser.showOpenDialog(Application.app.frmDirectedStudyFinal);
				//Did the user select a file?
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					//Get the selected file
					Application.app.file = chooser.getSelectedFile(); 
					try {
						//Processing 146k records can take upwards of 2 minutes,
						//this is a lazy method of warning the user
						//TODO: delegate this to a separate thread to save the Swing
						// AWT/Events thread from hanging. Add a loading animation.
						JOptionPane.showMessageDialog(frmDirectedStudyFinal, "Warning: LOOCV may take a few minutes to complete.", "Warning", JOptionPane.INFORMATION_MESSAGE);
						// Initialize points, that is set nearest neighbors on each data point
						DataPoint.initPoints(Application.app.dataPoints);
						//Write the loocv output file
						DataPoint.generateLOOCV(chooser.getSelectedFile());
					//Was the operation unsuccessful?
					} catch (Exception e) {
						//Alert the user what went wrong
						JOptionPane.showMessageDialog(Application.app.frmDirectedStudyFinal, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
					//The operation was successful, so notify the user.
					JOptionPane.showMessageDialog(Application.app.frmDirectedStudyFinal, "Results successfully generated!");
				}
				
			}
		});
		mntmLoocv.setEnabled(false);
		menuBar.add(mntmLoocv);
		
		//Error summary menu item action listener
		JMenuItem mntmErrorSummary = new JMenuItem("Error Summary");
		mntmErrorSummary.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File input=null, output=null;
				//Ask user to Select Input LOOCV
				JFileChooser chooser = new JFileChooser();
				chooser.setDialogTitle("Select Input LOOCV");
				int returnVal = chooser.showOpenDialog(Application.app.frmDirectedStudyFinal);
				//Did the user select OK?
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					//Get the file they selected
					input = chooser.getSelectedFile();
					//Create a new file chooser to select the output file with
					chooser = new JFileChooser();
					chooser.setDialogTitle("Select Output File");
					returnVal = chooser.showOpenDialog(Application.app.frmDirectedStudyFinal);
					//Did the user select an output file?
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						//Get the output file the user selecetd
						output = chooser.getSelectedFile();
					}
					//Write the error measures to the specified output file
					Application.writeErrorFile(Application.getErrorResults(Application.parseLoocvFile(input)), output);
					//Alert the user that the operation succeeded
					JOptionPane.showMessageDialog(Application.app.frmDirectedStudyFinal, "Successfully generated error measures file");
				}
			}
		});
		mntmErrorSummary.setMaximumSize(new Dimension(250, 32767));
		mntmErrorSummary.setHorizontalTextPosition(SwingConstants.LEFT);
		mntmErrorSummary.setHorizontalAlignment(SwingConstants.LEFT);
		menuBar.add(mntmErrorSummary);
	}
	//Abstraction to separate UI logic from File parsing for initial import
	private void parseFile() throws FileNotFoundException {
		this.dataPoints = DataPoint.parseFile(this.file);
	}

	/*
	 * Abstraction to separate UI logic from File parsing for loocv.
	 * 
	 */
	public static double[][] parseLoocvFile(File file){
		/*
		 * Magic number 10 is original points + 
		 * (the 3 n values for loocv ) * (the 3 p values for loocv).
		 * lists is an array of parallel lists of doubles.
		 * At the end of this method, each column in the
		 * input file will be read into its own list  
		 */
		//TODO: Remove magic number
		List<Double>[] lists = new LinkedList[10];
		for (int i=0; i<10; i++) {
			/*
			 * Each list may read 146,000 lines in final output,
			 * so use linked list to avoid arraylist re-copy
			 * overhead
			 */ 
			lists[i] = new LinkedList<Double>();
		}
		//Scanner to read the file
		Scanner sc = null;
		try {
			sc = new Scanner(file);
			int index = 0;
			//Is there another double in the file?
			while (sc.hasNextDouble()) {
				//Then add it to the appropriate list in our array of parallel lists 
				lists[index%10].add(new Double(sc.nextDouble()));
				index++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			//Make sure to close the file-reading scanner
			sc.close();
		}
		//Cast our list into a double matrix
		double[][] results = new double[10][];
		//Each column represents an loocv interpolation method,
		//or method for short
		for(int method=0; method<results.length; method++) {
			results[method] = new double[lists[method].size()];
			int columnIndex=0;
			for(Double d : lists[method]) {
				results[method][columnIndex] = d.doubleValue();
				columnIndex++;
			}
		}
		return results;
	}
	/*
	 * Parallel function to do error summary data processing.
	 * Operates on number of loocv types (9) multiplied by
	 * number of error measure functions (4). Each of these
	 * computations works on 146,000 doubles, so we compute
	 * in parallel to save time
	 */
	private static double[][] getErrorResults(double[][] lists) {
		//Create cached thread pool for parallel processing of
		// error measurements
		ExecutorService executor = Executors.newCachedThreadPool();
		/*
		 * Easily the ugliest use of generics I have ever committed
		 * against mankind. I'm sorry. Here's a guide:
		 * Outer list: loocv results (columns in the text file)
		 * Inner List: error measuring functions
		 * Future: result of computation returned by the error 
		 *  measuring Callable instance.
		 * Double: The type of the result of the error measure Callable 
		 */
		List<List<Future<Double>>> futures = new ArrayList<List<Future<Double>>>();
		for(int i=1; i<lists.length; i++) {
			//Create Callables for each error measuring function
			Callable<Double> mae = new MAECallable(lists[0], lists[i]);
			Callable<Double> mse = new MSECallable(lists[0], lists[i]);
			Callable<Double> rmse = new RMSECallable(lists[0], lists[i]);
			Callable<Double> mare = new MARECallable(lists[0], lists[i]);
			//Group the error measuring Callables' future results 
			// into a list, per list of loocv data
			ArrayList<Future<Double>> column = new ArrayList<Future<Double>>();
			column.add(executor.submit(mae));
			column.add(executor.submit(mse));
			column.add(executor.submit(rmse));
			column.add(executor.submit(mare));
			//Add our new list of futures to our list of all futures
			futures.add(column);
		}
		//Signal the thread pool that we have finished submitting tasks
		executor.shutdown();
		//Matrix of double to return to the caller
		//Magic number 9 = # of loocv lists
		//TODO: Remove magic number
		double[][] output = new double[9][];
		for(int i=0; i<output.length; i++) {
			//Magic number 4 = number of error measurements
			//TODO: remove magic number
			output[i] = new double[4];
		}
		//Fill output with the results of parallel execution,
		// making this thread wait for each result in order
		int loocvIndex=0, methodIndex=0;
		for(List<Future<Double>> idwMethod : futures) {
			for(Future<Double> errorValueFuture : idwMethod) {
				try {
					//Set output data to result of erroValueFuture,
					//waiting for its Callable to finish execution
					output[loocvIndex][methodIndex] = errorValueFuture.get().doubleValue();
				} catch (Exception e) {
					e.printStackTrace();
				}
				methodIndex++;
			}
			methodIndex=0;
			loocvIndex++;
		}
		return output;
	}
	/*
	 * Write a double matrix to a file using the correct syntax
	 * according to the project description
	 */
	public static void writeErrorFile(double[][] results, File output) {
		//Used to write to the ouput file
		BufferedWriter bw = null;
		try{
			FileWriter fw = new FileWriter(output.getAbsoluteFile());
			//Initialize the file writer to write to the output file
			bw = new BufferedWriter(fw);
			//Iterate over each error method
			//Magic number 4 = number of error methods
			//TODO: remove magic number
			for(int errorMethodIndex=0; errorMethodIndex<4; errorMethodIndex++) {
				//Used to construct ourput line
				StringBuilder sb = new StringBuilder();
				for(int loocvColumnIndex=0; loocvColumnIndex<9; loocvColumnIndex++) {
					//Append the appropriate error method name
					switch(errorMethodIndex) {
					case 0:
						sb.append("MAE");
						break;
					case 1:
						sb.append("MSE");
						break;
					case 2:
						sb.append("RMSE");
						break;
					case 3:
						sb.append("MARE");
					default:
						break;
					}
					sb.append(" for IDW with ");
					//Append the n-value of the loocv data to the output line
					sb.append(loocvColumnIndex/3+3);
					sb.append(" neighbors and exponent ");
					//Append the p-value of the loocv data to the output line
					sb.append(loocvColumnIndex%3+1);
					sb.append(": ");
					//Append the result of the error measure to the output line
					sb.append(results[loocvColumnIndex][errorMethodIndex]);
					sb.append("\n");
				}
				sb.append("\n");
				//Write the output line to the file
				bw.append(sb.toString());
			}
			//Close the file writer
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//Convenience method for enabling / disabling actions that
	//require an input file
	private void hasFile(boolean hasFile) {
		mntmParseFile.setEnabled(hasFile);
		mntmInterpolateValue.setEnabled(hasFile);
		mntmLoocv.setEnabled(hasFile);
	}
}
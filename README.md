spicywhitefish_gis_project
==========================

The final resting place of my group's directed study project.
Initially created with Subversion on my EC2 server,
moved to GitHub to showcase some code that I've written for the curious.

I plan on going back and documenting the code better.
This was a rush job- the entire goes from scratch to
submitted in <48 hours. Also, I was the primary
developer for the project. For these two reasons, the
code was not documented nearly well enough.

/docs has our final write-up, it's a good source of information on the project. THe full project description follows.

Project Requirements:
Final Project Description
Design a graphical user interface (GUI) that implements the following tasks, using a software tool 
that you feel comfortable with, such as Java.
1. The GUI should have the capability to import a sample data set of PM2.5measurements, 
pm25_2009_measured.txt, is available to download
in folio. It contains 146,125
spatiotemporal PM2.5 measurements(x, y, time, measurement), with the time domain as 
day: 1 for 01/01/2009, 2 for 01/02/2009, ……, and 365 for 12/31/2009.

//Not implemented
2. (Optional) Visualize all the measurement locations in a U.S. state-level map. You can 
use a symbol such as a circle or a star to illustrate a location in the map, similar as the 
Figure below. Since measurements are from 365 days, the set of measurement locations 
to visualize is the union of all the locations from 365 days. The boundary information of 
each state is stored in files st99_d00.dat and st99_d00a.dat, which are available to 
download in folio.

3. Implement the IDW based spatiotemporal extension interpolation methods. Read 
Review Paper 1.pdf, available in folio, for the detailed description of the method: 
especially Section 4.2, under Formula 23, page 21. The user should be prompted to 
input the values for the number of the nearest neighbors (integer type) and the exponent 
(floating-point type). 

4. Implement a query in GUI that allows the user to input (x,y,t) and display the interpolated 
PM2.5 value using IDW.
5. Evaluate the interpolation methods using leave-one-out cross validation. Leave-one-out 
cross validation (LOOCV) removes one of the n observation points and uses the 
remaining n − 1 points to estimate its value; and this process is repeated at each 
observation point. Read Review Paper 2.pdf and Section 3.2 of Review Paper 3.pdf, 
available in folio, for the description of the leave-one-out-cross validation.
Please evaluate the following IDW methods:
Ø IDW with 3 neighbors and exponents 1, 2 and 3.
Ø IDW with 4 neighbors and exponents 1, 2 and 3.
Ø IDW with 5 neighbors and exponents 1, 2 and 3.
Save the interpolation results in a new file. Use PM2.5 data for testing. The new text file 
should have the format of (original, n3e1, n3e2, n3e3, …, n5e1, n5e2, n5e3). The original 
column is the last column from pm25_2009_measured.txt. The rest of the columns store 
interpolated values for all the 146,125 spatiotemporal points from the input data set of 
pm25_2009_measured.txt. Since there are 3 possible numbers of neighbors and 3
possible exponent values, there should be 3×3=9 columns after the original column. Use 
the following naming convention for the file: 
loocv_idw.txt
6. Compute the error measurements MAE, MSE, RMSE and MARE for each of 9 IDW 
methodsfrom Task 5, based on the original and interpolated columns in the text file
resulted from Task 5. Save the error measurement results in a new text file using the 
PM2.5 data. Read Section III.B of Review Paper 4.pdf, available in folio, for the details 
of the error measurements MAE, MSE, RMSE and MARE. Name your error statistics 
files as 
error_statistics_idw.txt
This file should contain the error statistics of MAE, MSE, RMSE and MARE for each of 
the 9 IDW methods.
//Not implemented
7. (Optional) Feel free to add any additional features to your graphic user interface, such as 
visualizing the daily PM2.5 values in the extent of the conterminous U.S. in the year of 
2009, using animation. You will need to decide (or ask the user to decide) the raster cell 
size for visualization and assume the interpolated PM2.5 value at the centroid of each cell 
is the value for the whole cell. Design and implement a color rendering scheme to 
visualize different PM2.5 values by different colors, similar as the visualization results 
given below.

GitHub clone of SVN repo svn://timothyelam.com/svnrepos/directed (cloned by http://svn2github.com/)

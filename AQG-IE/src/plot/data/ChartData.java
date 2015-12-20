package plot.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChartData {

	private static final String[] colors = {"\"blue\"","\"brown\"","\"cadetblue\"","\"chartreuse\"","\"darkgoldenrod1\"","\"darkviolet\"",
		"\"forestgreen\"","\"hotpink\"","\"deepskyblue\"","\"chocolate3\"","\"olivedrab\""};
	
	private static final int[] pch = {0,1,2,3,4,5,6,15,16,17};
	
	private static final int[] line = {1,2,3,4,5,6};
	
	private String title;
	private String axisY;
	private String axisX;
	private ArrayList<Series> dataSeries;
	private String outPutName;
	private String folder;

	private boolean normalizeYAxis;

	public ChartData(String outputName, boolean normalizeYAxis){
		dataSeries = null;
		this.outPutName = outputName;
		this.normalizeYAxis = normalizeYAxis;
	}
	
	public void setTitle(String title){
		this.title = title;
	}
	
	public void setAxisYTitle(String axisY){
		this.axisY = axisY;
	}
	
	public void setAxisXTitle(String axisX){
		this.axisX = axisX;
	}
	
	public void addSeries(Series series){
		
		getSeriesData().add(series);
		
	}
	
	private List<Series> getSeriesData() {
		
		if (dataSeries == null){
			dataSeries = new ArrayList<Series>();
		}
		return dataSeries;
	}

	public void plot(String folder, int intervals) throws IOException{
		plot(folder,false,false,intervals);
	}
	
	public void plot(String folder, boolean percentX, boolean percentY, int intervals) throws IOException{
		
		double maxValue = 1.0;
		
		List<Series> sData = getSeriesData();
		
		if (!normalizeYAxis){
			for (int i = 0; i < sData.size(); i++) {
				
				List<Pair> pairs = sData.get(i).getPairs();
				
				for (int j = 0; j < pairs.size(); j++) {
					
					if (maxValue < pairs.get(j).getyValue())
						maxValue = Math.ceil(pairs.get(j).getyValue());
					
				}
				
			}
		}
		
		this.folder = folder;
		
		List<Double> xValues = generateXValues(getSeriesData(),intervals);
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(folder + outPutName + ".R")));
		
		bw.write("# Define 2 vectors\n");
		
		for (Series series : getSeriesData()) {
			bw.write(generateSeriesText(series,xValues) + "\n");
		}

		bw.write(generateFixedLine(xValues,maxValue) + "\n");
		
		bw.write("# Calculate range from 0 to max value of cars and trucks\n");
		
		bw.write(generateRangeLine(getSeriesData()) + "\n");
		
		bw.write("# Start postscript device driver to save output to " + folder + outPutName + ".eps\n");

		bw.write(getPostcriptLine(folder + outPutName) + "\n");
		
		bw.write("# Graph using y axis that ranges from 0 to max\n");
		 
		bw.write("# value in vectors.  Turn off axes and\n");
		 
		bw.write("# annotations (axis labels) so we can specify them ourself\n");
		
		bw.write(generatePlot(getSeriesData()) + "\n");
		
		bw.write("# Make x axis using labels\n");
		
		bw.write(generateAxisX(xValues, percentX) + "\n");
		
		bw.write("# Make y axis with horizontal labels that display ticks at\n");
		 
		bw.write("# every 4 marks. 4*0:g_range[2] is equivalent to c(0,4,8,12).\n");
		
		bw.write(generateAxisY(getSeriesData().get(0), maxValue) + "\n");
		
		bw.write("# Create box around plot\n");
		
		bw.write("box()\n");

		for (int i = 0; i < getSeriesData().size(); i++) {
			
			bw.write("# Graph with solid lines\n");
			
			bw.write(generateLines(getSeriesData().get(i),i) + "\n");
			
		}

		bw.write("# Create a title with a red, bold/italic font\n");
		
		bw.write(generateTitle() + "\n");
		
		bw.write("# Label the x and y axes with dark green text\n");

		bw.write(generateTitleX() + "\n");
		
		bw.write(generateTitleY() + "\n");
		
		bw.write("# Create a legend at (1, g_range[2]) that is slightly smaller\n");

		bw.write("# (cex) and uses the same line colors and points used by\n"); 
		 
		bw.write("# the actual plots\n");
		
		bw.write(generateLegend() + "\n");
		
		bw.write("# Turn off device driver (to flush output to EPS)\n");

		bw.write("dev.off()\n");
		
		bw.close();
		
		
	}

	private List<Double> generateXValues(List<Series> series, int intervals) {
		
		List<Double> xValues = new ArrayList<Double>(); //hopefully all have the same xValues. (see AddedByProcessed to see Normalized working well)
		
		//Can do a Sanity Check to see that all the X values are the same ...
		
		Series list = series.get(0);
		
		for (Series ser2 : series) {
			if (list.size() < ser2.size()){
				list = ser2;
			}
		}
		
		int every = 1;
		
		if (list.getPairs().size() > intervals)
			every = (int)Math.ceil(list.getPairs().size() / intervals);
		
		int index = 1; 
		
		for (Pair pair : list.getPairs()) {
			
			if (index % every == 0){
				xValues.add(pair.getxValue());
			}
			index ++;
		}

		//there must be a last one
		
		if (xValues.get(xValues.size()-1) < list.getPairs().get(list.getPairs().size()-1).getxValue()){
			xValues.add(list.getPairs().get(list.getPairs().size()-1).getxValue()); //XXX see if this is the value to add or the actual increment 10,20,30, etc...
		}
		
		return xValues;
		
	}

	private String generateFixedLine(List<Double> series, double maxValue) {
		
		String ret = "Fixed <- c("+maxValue+"," + maxValue;
		
		for (int i = 1; i < series.size(); i++) {
			
			ret += "," + maxValue;
			
		}
		
		return ret + ")";
	
	}

	private String generateLegend() {
		
		String ret = "legend(1, g_range[2], c(\"" + getSeriesData().get(0).getName().replace("_", " ") + "\"";
	
		for (int i = 1; i < getSeriesData().size(); i++) {
			
			ret += ",\"" + getSeriesData().get(i).getName().replace("_", " ") + "\"";
			
		}
		
		String color = "col=c(" + getColor(0);
		String lty = "lty=c(" + getLty(0);
		String pch = "pch=c(" + getPch(0);
		
		for (int i = 1; i < getSeriesData().size(); i++) {
			
			color += "," + getColor(i);
			lty += "," + getLty(i);
			pch += "," + getPch(i);
						
		}

		color += ")";
		lty += ")";
		pch += ")";
		
		ret += "), cex=0.8,  " + color + ", " + pch + ", " + lty + ", lwd = 2);";
	
		return ret;
	}

	private int getLty(int i) {
		return line[(int)i % line.length];
	}

	private int getPch(int i) {
		return pch[(int)i % pch.length];
	}

	private String getColor(int i) {
		
		//XXX add more colors?
		
		return colors[(int)i % colors.length];
	}

	private String generateTitleY() {
		
		String ret = "title(ylab=\"" + axisY + "\", col.lab=\"black\")";
		
//		title(ylab="Test Y", col.lab=rgb(0,0.5,0))

		return ret;
		
	}

	private String generateTitleX() {
		
		String ret = "title(xlab=\"" + axisX + "\", col.lab=\"black\")";

		return ret;
		
//		title(xlab="Test X", col.lab=rgb(0,0.5,0))
	}

	private String generateTitle() {
		
		String ret = "title(main=\"" + title + "\", col.main=\"black\", font.main=4)";
		
//		title(main="Title", col.main="red", font.main=4)

		return ret;
	}

	private String generateLines(Series series, int i) {
	
		String ret = "lines(" + series.getName() + ", type=\"o\", pch="+getPch(i)+", lty="+getLty(i)+", lwd=2, col=" + getColor(i) + ")";
	
		return ret;
//		lines(Incremental, type="o", pch=22, lty=2, col="red")

	}

	private String generateAxisY(Series series, double maxValue) {
//		return "axis(2, las=1, at=0.1*0:g_range[2])";
		
		double normalized = maxValue / 10.0;
		
		return "axis(2, las=1, at="+normalized+"*0:10)";
	
	}

	private String generateAxisX(List<Double> xValues, boolean percentX) {
		
		String ret = "axis(1, at=1:" + (xValues.size()+1) + ", lab=c(";
		
		ret += generateIntervals(xValues, percentX) + "))";
		
//		axis(1, at=1:8, lab=c("1","3","4","30","35","36","38","50"))

		return ret;
		
	}

	private String generateIntervals(List<Double> xValues, boolean percent) {
				
		DecimalFormat df = new DecimalFormat("#.##");
		
		if (percent){
			df = new DecimalFormat("%#.##");
		}
		
		String ret = "\""+df.format(0.0)+"\"";
			
		for (Double xValue : xValues) {
			
			ret += ",\"" + df.format(xValue) + "\"";
			
		}
		
		if (ret.isEmpty())
			return ret;
		
		return ret;
	
	}

	private String generatePlot(List<Series> list) {
		
		String ret = "plot(Fixed, type=\"n\", col=" + getColor(0) + ", ylim=g_range, lwd = 2, axes=FALSE, ann=FALSE)"; 
		
//		plot(MSC, type="o", col="blue", ylim=g_range, 
//				axes=FALSE, ann=FALSE)
	
		return ret;
	}

	private String getPostcriptLine(String outPutName) {
		
		String ret = "postscript(file=\"" + outPutName + ".eps\")";

		return ret;
		
	}

	private String generateRangeLine(List<Series> seriesData) {
		
		String ret = "g_range <- range(0";
		
		for (Series series : seriesData) {
			ret += ", " + series.getName(); 
		}
		
		return ret + ", Fixed)";
//		g_range <- range(0, MSC, Incremental,QProber)
	}

	private String generateSeriesText(Series series, List<Double> xValues) {
		
		String ret = series.getName() + " <- c(0.0" + getYInterval(series.getPairs(),xValues) + ")";
		
		return ret;
		
//		MSC <- c(1.7, 2.323432, 4.6565, 18.3453, 19.00008)
//		Incremental <- c(2, 3, 4, 5, 12)
//		QProber <- c(12, 3, 14, 5, 12)

	}

	private String getYInterval(List<Pair> pairs, List<Double> xValues) {
		
		String ret = "";
		
		if (pairs.isEmpty()){
			return ret;
		}
		
		int currentIndex = 0;
		
		boolean lastAdded = false;
		
		for (Pair pair : pairs) {
			
			if (pair.getxValue() == xValues.get(currentIndex)){
				ret += "," + pair.getyValue();
				currentIndex++;
				lastAdded = true;
			}else{
				lastAdded = false;
			}
		}
		
		if (!lastAdded)
			ret += "," + pairs.get(pairs.size()-1).getyValue();
		
		return ret;
	}

	public String getOutputFile() {
		return folder + outPutName + ".R";
	}

	public String getOutputOutFile(){
		return folder + outPutName + ".Rout";
	}
}

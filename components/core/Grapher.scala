import java.awt.Color
import javax.swing.JFrame
import org.jfree.chart._
import org.jfree.data.xy._
import org.jfree.ui._
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.chart.axis.NumberAxis

class Grapher(title:String) extends JFrame(title) {
  
  def redraw(data:Array[(Int,Int)]) = {
    val dataset = createDataset(data);
    val chart = createChart(dataset);
    val chartPanel = new ChartPanel(chart);
    
    chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
    setContentPane(chartPanel);
    this.pack();
    RefineryUtilities.centerFrameOnScreen(this);
    this.setVisible(true);
  }
  def createDataset(data:Array[(Int,Int)]):XYDataset = {
    val dSeries = new XYSeries("Data");
    data.foreach{case(x,y) => dSeries.add(x,y)}
    val dataset = new XYSeriesCollection();
    dataset.addSeries(dSeries);
    return dataset;
  }
  def createChart(dataset:XYDataset) : JFreeChart = {
    
    val chart = ChartFactory.createXYLineChart(
      "Line Chart Demo 6",      // chart title
      "X",                      // x axis label
      "Y",                      // y axis label
      dataset,                  // data
      PlotOrientation.VERTICAL,
      true,                     // include legend
      true,                     // tooltips
      false                     // urls
    );
    chart.setBackgroundPaint(Color.white);
    val plot = chart.getXYPlot();
    plot.setBackgroundPaint(Color.lightGray);
    plot.setDomainGridlinePaint(Color.white);
    plot.setRangeGridlinePaint(Color.white);
    
    val renderer = new XYLineAndShapeRenderer();
    renderer.setSeriesLinesVisible(0, false);
    renderer.setSeriesShapesVisible(1, false);
    plot.setRenderer(renderer);
    val rangeAxis = plot.getRangeAxis();
    rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    return chart;
    
  }
}

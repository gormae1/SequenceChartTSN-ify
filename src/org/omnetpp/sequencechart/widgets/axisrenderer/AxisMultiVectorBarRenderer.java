/*--------------------------------------------------------------*
  Copyright (C) 2006-2015 OpenSim Ltd.

  This file is distributed WITHOUT ANY WARRANTY. See the file
  'License' for details on this and other legal matters.
*--------------------------------------------------------------*/

package org.omnetpp.sequencechart.widgets.axisrenderer;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ILog;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.omnetpp.common.color.ColorFactory;
import org.omnetpp.common.util.BigDecimal;
import org.omnetpp.eventlog.IEvent;
import org.omnetpp.eventlog.IEventLog;
import org.omnetpp.eventlog.SequenceChartFacade;
import org.omnetpp.scave.engine.EnumType;
import org.omnetpp.scave.engine.ResultItem;
import org.omnetpp.scave.engine.XYArray;
import org.omnetpp.scave.engine.XYArrayVector;
import org.omnetpp.sequencechart.widgets.SequenceChart;
import org.omnetpp.sequencechart.SequenceChartPlugin;
import org.omnetpp.sequencechart.widgets.axisrenderer.EthIf;
import org.omnetpp.sequencechart.widgets.axisrenderer.PCPInfo;
import org.omnetpp.sequencechart.widgets.axisrenderer.NNXYArray;
import java.util.ArrayList;


/**
 * A special axis representation for sequence charts which displays a horizontal colored bar
 * with the names representing the individual values of a data vector.
 */
public class AxisMultiVectorBarRenderer implements IAxisRenderer {
    private static final Color AXIS_COLOR = ColorFactory.ORANGE;

    private static final Font VALUE_NAME_FONT = new Font(null, "Courier New", 12, 0);

    private static final Color VALUE_NAME_COLOR = ColorFactory.BLACK;

    private static final Color NO_VALUE_COLOR = ColorFactory.WHITE;

    private static final int NUM_GATES = 2;

    private SequenceChart sequenceChart;

    private String vectorFileName;

    private String vectorRunName;
    
    private String vectorModuleFullPath;
    
    @SuppressWarnings("unused")
    private EthIf eIf;

    // NOTE: this must be kept here to avoid the garbage collector delete the underlying C++ object, because it would also delete the XYArray
//    private ArrayList<XYArrayVector> dataVectors;
    
    @SuppressWarnings("unused")
    private NNXYArray data;
    private NNXYArray qData;
    
    private ArrayList<NNXYArray> dataVectors;
    private ArrayList<NNXYArray> qVectors;
    
    private ResultItem.DataType type;
    private ILog log;

    private EnumType enumType;

    private long numPCP;
    
    private int maxQueueLen;
    
//    sequenceChart, vectorFileName, vectorRunName, dataVector);
    public AxisMultiVectorBarRenderer(SequenceChart sequenceChart, String fileName, String runName, EthIf eIf) {
        this.sequenceChart = sequenceChart;
        this.log = SequenceChartPlugin.getDefault().getLog();
        this.dataVectors = new ArrayList<NNXYArray>();
        this.qVectors = new ArrayList<NNXYArray>();
        
        for (int idx = 0; idx < eIf.size(); idx++) {
        	dataVectors.add(eIf.getInfo(idx).getSchedVector());
        	qVectors.add(eIf.getInfo(idx).getqVector());
        }
        
        this.numPCP = dataVectors.size();
    	this.data = dataVectors.get(0);

        this.eIf = eIf;
        this.vectorFileName = fileName;
        this.vectorRunName = runName;
    }

    public String getVectorFileName() {
        return vectorFileName;
    }

    public String getVectorRunName() {
        return vectorRunName;
    }

    public String getVectorModuleFullPath() {
        return vectorModuleFullPath;
    }

    public int getHeight() {
        return 90;
    }

    /**
     * Draws a colored tick bar based on the values in the data vector in the given range.
     */
    public void drawAxis(Graphics graphics, IEvent startEvent, IEvent endEvent)
    {
        Rectangle rect = graphics.getClip(Rectangle.SINGLETON);
        ILog log = SequenceChartPlugin.getDefault().getLog();
        log.info("DA: events given: [start:" + startEvent.getEventNumber() + "]" + startEvent.getSimulationTime() + ", [end:" + endEvent.getEventNumber() + "]" + endEvent.getSimulationTime());
        log.info("DA: clipping rect: " + rect);
        rect.height += 20;
        graphics.setClip(rect);
        
        // draw default color where no value is available
        graphics.setLineCap(SWT.CAP_SQUARE);
        graphics.setLineStyle(SWT.LINE_SOLID);
        graphics.setBackgroundColor(NO_VALUE_COLOR);
        graphics.fillRectangle(rect.x, 0, rect.right() - rect.x, getHeight());

        SequenceChartFacade sequenceChartFacade = sequenceChart.getInput().getSequenceChartFacade();
        IEventLog eventLog = sequenceChart.getInput().getEventLog();
        
        for (int pcpIdx = 0; pcpIdx < (int)numPCP; pcpIdx++) {
        	data = dataVectors.get(pcpIdx);
        	log.info("FOR PCP:"+pcpIdx+", SCHED ARR: " + data.toString());

        	
//        	data = dataVectors.get(pcpIdx).get(0);
        	
        	 int size = getDataLength();

             int startIndex = getIndex(startEvent, true);
             if (startIndex == -1)
                 startIndex = 0;

             int endIndex = getIndex(endEvent, false);
             if (endIndex == -1)
                 endIndex = size;
             long endEventNumber = endEvent.getEventNumber();
             String xS = "";
        	
	        for (int phase = 0; phase < 2; phase++) {
	            for (int i = startIndex; i < endIndex; i++) {
	                long eventNumber = getEventNumber(i);
	                xS += "" + eventNumber + ";";
	                
	                long nextEventNumber = Math.min(endEventNumber, (i == size - 1) ? endEventNumber : getEventNumber(i + 1));
	
	                if (eventNumber == -1 || nextEventNumber == -1 || eventNumber >= nextEventNumber)
	                    continue;
	
	                IEvent event = eventLog.getEventForEventNumber(eventNumber);
	                IEvent nextEvent = eventLog.getEventForEventNumber(nextEventNumber);
	
	                int x1 = Integer.MAX_VALUE;
	                int x2 = Integer.MAX_VALUE;
	
	                // check for events being filtered out
	                if (event != null)
	                    x1 = (int)sequenceChart.getViewportCoordinateForTimelineCoordinate(sequenceChartFacade.getTimelineCoordinateBegin(event));
	                else {
	                    event = sequenceChartFacade.getNonFilteredEventForEventNumber(eventNumber);
	                    if (event != null) {
	                        BigDecimal eventSimulationTime = event.getSimulationTime();
	                        double eventTimelineCoordinate = sequenceChartFacade.getTimelineCoordinateForSimulationTime(eventSimulationTime, false);
	
	                        if (eventTimelineCoordinate == sequenceChartFacade.getTimelineCoordinateForSimulationTime(eventSimulationTime, true))
	                            x1 = (int)sequenceChart.getViewportCoordinateForTimelineCoordinate(eventTimelineCoordinate);
	                    }
	                }
	
	                if (nextEvent != null)
	                    x2 = (int)sequenceChart.getViewportCoordinateForTimelineCoordinate(sequenceChartFacade.getTimelineCoordinateBegin(nextEvent));
	                else {
	                    nextEvent = sequenceChartFacade.getNonFilteredEventForEventNumber(nextEventNumber);
	                    if (nextEvent != null) {
	                        BigDecimal nextEventSimulationTime = nextEvent.getSimulationTime();
	                        double nextEventTimelineCoordinate = sequenceChartFacade.getTimelineCoordinateForSimulationTime(nextEventSimulationTime, false);
	
	                        if (nextEventTimelineCoordinate == sequenceChartFacade.getTimelineCoordinateForSimulationTime(nextEventSimulationTime, true))
	                            x2 = (int)sequenceChart.getViewportCoordinateForTimelineCoordinate(nextEventTimelineCoordinate);
	                    }
	                }
	
	                if (x1 == Integer.MAX_VALUE || x2 == Integer.MAX_VALUE)
	                    continue;
	
//	                int colorIndex = getValueIndex(i);
	                graphics.setBackgroundColor(data.getY(i) == 888 ? ColorFactory.GREEN : (data.getY(i) == 999 ? ColorFactory.RED : ColorFactory.BLUE));

	                if (phase == 0) {
	                    graphics.fillRectangle(x1, getScaledHeight()*pcpIdx, x2 - x1, getScaledHeight());
	                    graphics.setForegroundColor(AXIS_COLOR);
	                    graphics.drawLine(x1, getScaledHeight()*pcpIdx, x1, pcpIdx == 0 ? 0 : getScaledHeight()*(pcpIdx-1));
	                }
	
	                // draw labels starting at each value change and repeat labels based on canvas width
	                if (phase == 1) {
	                    String name = getValueText(i);
	                    if (name != null) {
	                        int labelWidth = (int)(graphics.getFontMetrics().getAverageCharacterWidth() * name.length());
	
	                        if (x2 - x1 > labelWidth + 6) {
	                            graphics.setForegroundColor(VALUE_NAME_COLOR);
	                            graphics.setFont(VALUE_NAME_FONT);
	
	                            int x = x1 + 5;
	                            while (x < rect.right() && x < x2 - labelWidth) {
	                                graphics.drawText("PCP " + (pcpIdx) + "|" + (data.getY(i) == 999 ? "CLOSED" : "OPEN") + "|" + eventNumber + "["+i+"]", x, pcpIdx*getScaledHeight());
	                                x += sequenceChart.getClientArea().width;
	                            }
	                        }
	                    }
	                }
	            }
	        }
	        graphics.setForegroundColor(AXIS_COLOR);
	        graphics.drawLine(rect.x, pcpIdx == 0 ? 0 : getScaledHeight()*(pcpIdx-1), rect.right(), pcpIdx == 0 ? 0 : getScaledHeight()*(pcpIdx-1));
	        graphics.drawLine(rect.x, getScaledHeight()*pcpIdx, rect.right(), getScaledHeight()*pcpIdx);
        }
        
        graphics.drawLine(rect.x, getScaledHeight()*(int)numPCP, rect.right(), getScaledHeight()*(int)numPCP);


        // draw axis as a colored thick line with labels representing values
        // two phases: first draw the background and after that draw the values
        for (int pcpIdx = 0; pcpIdx < (int)numPCP; pcpIdx++) {
        	qData = qVectors.get(pcpIdx);
        	int size = qData.length();
//        	log.info("FOR PCP:"+ pcpIdx +", QUEUE ARR: " + qData.toString());
        	int maxQ = 0;
        	
        	
        	for (int idx2 = 0; idx2 < size; idx2++) {
        		if (qData.getY(idx2) > maxQ) {maxQ = (int)qData.getY(idx2);}
        	}
        	
        	int slicedUp = (int)(getScaledHeight() / maxQ);
			int startIndex = getIndexMine(startEvent, qData, true);
			if (startIndex == -1) { startIndex = 0; }
			
			int endIndex = getIndexMine(endEvent, qData, false);
			if (endIndex == -1) { endIndex = size; }

			long endEventNumber = endEvent.getEventNumber();
        	log.info("Drawing bars for QUEUE. Given ST:" + startEvent.getEventNumber() + "ED:" + endEventNumber +"; PCP:" + pcpIdx + ", ST:" + qData.getEventNumber(startIndex) + ", ED:" + qData.getEventNumber(endIndex));

			for (int i = startIndex; i < endIndex; i++) {
                long eventNumber = getEventNumberMine(i, qData);
                long nextEventNumber = Math.min(endEventNumber, (i == size - 1) ? endEventNumber : getEventNumberMine(i + 1, qData));
            	
                if (eventNumber == -1 || nextEventNumber == -1 || eventNumber >= nextEventNumber)
                    continue;

                IEvent event = eventLog.getEventForEventNumber(eventNumber);
                IEvent nextEvent = eventLog.getEventForEventNumber(nextEventNumber);

                int x1 = Integer.MAX_VALUE;
                int x2 = Integer.MAX_VALUE;

                // check for events being filtered out
                if (event != null)
                    x1 = (int)sequenceChart.getViewportCoordinateForTimelineCoordinate(sequenceChartFacade.getTimelineCoordinateBegin(event));
                else {
                    event = sequenceChartFacade.getNonFilteredEventForEventNumber(eventNumber);
                    if (event != null) {
                        BigDecimal eventSimulationTime = event.getSimulationTime();
                        double eventTimelineCoordinate = sequenceChartFacade.getTimelineCoordinateForSimulationTime(eventSimulationTime, false);

                        if (eventTimelineCoordinate == sequenceChartFacade.getTimelineCoordinateForSimulationTime(eventSimulationTime, true))
                            x1 = (int)sequenceChart.getViewportCoordinateForTimelineCoordinate(eventTimelineCoordinate);
                    }
                }

                if (nextEvent != null)
                    x2 = (int)sequenceChart.getViewportCoordinateForTimelineCoordinate(sequenceChartFacade.getTimelineCoordinateBegin(nextEvent));
                else {
                    nextEvent = sequenceChartFacade.getNonFilteredEventForEventNumber(nextEventNumber);
                    if (nextEvent != null) {
                        BigDecimal nextEventSimulationTime = nextEvent.getSimulationTime();
                        double nextEventTimelineCoordinate = sequenceChartFacade.getTimelineCoordinateForSimulationTime(nextEventSimulationTime, false);

                        if (nextEventTimelineCoordinate == sequenceChartFacade.getTimelineCoordinateForSimulationTime(nextEventSimulationTime, true))
                            x2 = (int)sequenceChart.getViewportCoordinateForTimelineCoordinate(nextEventTimelineCoordinate);
                    }
                }

                if (x1 == Integer.MAX_VALUE || x2 == Integer.MAX_VALUE)
                    continue;

//                int colorIndex = getValueIndex(i);
                // TODO: maybe add color ranges so that big number = red, small = green or something
                graphics.setBackgroundColor(ColorFactory.BLUE);
            	graphics.fillRectangle(x1, (int)(getScaledHeight()*(pcpIdx)+(getScaledHeight() - (int)((qData.getY(i) * slicedUp)))), x2 - x1, (int)((qData.getY(i) * slicedUp)));
//                graphics.setForegroundColor(AXIS_COLOR);
//                graphics.drawLine(x1, getScaledHeight()*pcpIdx, x1, pcpIdx == 0 ? 0 : getScaledHeight()*(pcpIdx-1));
			}
			
        }
        
        data = dataVectors.get(0);
    }
    
    private int getScaledHeight() {
    	return getHeight() / (int)numPCP;
    }

    public int getIndexMine(IEvent event, NNXYArray toFind, boolean before) {
    	long eventNumber = event.getEventNumber();
    	int idx = 0;
    	for (idx = 0; idx < toFind.size(); idx++) {
    		if (toFind.getEventNumber(idx) >= eventNumber) {
    			break;
    		}
    	}
    	if (before) {
    		if (idx != 0) {return idx-1;}
    	}
    	
    	if (idx == toFind.size()) { idx--; }
    	
    	return idx;
    }
    
    /**
     * Returns the element index having less or greater or equal event number in the data array depending on the given flag.
     */
    public int getIndex(IEvent event, boolean before)
    {
        int index = -1;
        int left = 0;
        int right = getDataLength() - 1;
        long eventNumber = event.getEventNumber();

        while (left <= right) {
            int mid = (right + left) / 2;

            if (getEventNumber(mid) == eventNumber) {
                do {
                    if (before)
                        mid--;
                    else
                        mid++;
                }
                while (mid >= 0 && mid < getDataLength() && getEventNumber(mid) == eventNumber);

                index = mid;
                break;
            }
            else if (left == right)
                break;
            else if (eventNumber < getEventNumber(mid))
                right = mid - 1;
            else
                left = mid + 1;
        }

        if (left > right)
            if (before)
                if (eventNumber < getEventNumber(left))
                    index = left - 1;
                else
                    index = left;
            else
                if (eventNumber > getEventNumber(right))
                    index = right + 1;
                else
                    index = right;

        if (index < 0 || index >= getDataLength())
            return -1;
        else {
            Assert.isTrue((before && getEventNumber(index) < eventNumber) ||
                          (!before && getEventNumber(index) > eventNumber));
            return index;
        }
    }

    /**
     * Returns the index having less or greater or equal simulation time in the data array depending on the given flag.
     */
    public int getIndex(BigDecimal simulationTime, boolean before)
    {
        int index = -1;
        int left = 0;
        int right = getDataLength();

        while (left <= right) {
            int mid = (right + left) / 2;

            if (getSimulationTime(mid) == simulationTime) {
                do {
                    if (before)
                        mid--;
                    else
                        mid++;
                }
                while (mid >= 0 && mid < getDataLength() && getSimulationTime(mid) == simulationTime);

                index = mid;
                break;
            }
            else if (simulationTime.less(getSimulationTime(mid)))
                right = mid - 1;
            else
                left = mid + 1;
        }

        if (left > right)
            if (before)
                if (simulationTime.less(getSimulationTime(left)))
                    index = left - 1;
                else
                    index = left;
            else
                if (simulationTime.greater(getSimulationTime(right)))
                    index = right + 1;
                else
                    index = right;

        if (index < 0 || index >= getDataLength())
            return -1;
        else {
            Assert.isTrue((before && getSimulationTime(index).less(simulationTime)) ||
                          (!before && getSimulationTime(index).greater(simulationTime)));
            return index;
        }
    }

    public int getDataLength()
    {
        return data.length();
    }

    public BigDecimal getSimulationTime(int index)
    {
        return new BigDecimal(data.getPreciseX(index));
    }
    
    public long getEventNumberMine(int idx, NNXYArray arr) {
    	return arr.getEventNumber(idx);
    }

    public long getEventNumber(int index)
    {
        if (0 > index || index >= data.length()) {
            ILog log = SequenceChartPlugin.getDefault().getLog();
//            log.info("OOB ERROR!! [" + index + "] v.s. " + data.length());
            return -1;
        }
        data.getX(index);
        data.getY(index);
        data.getPreciseX(index);
        return data.getEventNumber(index);
    }

    public double getValue(int index)
    {
        return data.getY(index);
    }

    private int getValueIndex(int index)
    {
        if (type == ResultItem.DataType.TYPE_ENUM || type == ResultItem.DataType.TYPE_INT)
            return (int)Math.floor(getValue(index));
        else {
            double value = getValue(index);
            if (Math.floor(value) == value)
                return (int)value;
            else
                return index;
        }
    }

    private String getValueText(int index)
    {
        if (type == ResultItem.DataType.TYPE_ENUM)
            return enumType.nameOf((int)Math.floor(getValue(index)));
        else {
            double value = getValue(index);
            if (value == Math.floor(value))
                return String.valueOf((long)value);
            else
                return String.valueOf(value);
        }
    }
}
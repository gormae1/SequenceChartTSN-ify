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
import org.eclipse.swt.graphics.RGBA;
import org.omnetpp.sequencechart.widgets.axisrenderer.EthIf;
import org.omnetpp.sequencechart.widgets.axisrenderer.TrafficClassInfo;
import org.omnetpp.sequencechart.widgets.axisrenderer.NNXYArray;
import java.util.ArrayList;


/**
 * A special axis representation for sequence charts which displays a horizontal colored bar
 * with the names representing the individual values of a data vector.
 */
public class AxisMultiVectorBarRenderer implements IAxisRenderer {
    private static final Color AXIS_COLOR = ColorFactory.BLACK;

    private static final Font VALUE_NAME_FONT = new Font(null, "Courier New", 12, SWT.BOLD);

    private static final Color VALUE_NAME_COLOR = ColorFactory.BLACK;

    private static final Color NO_VALUE_COLOR = ColorFactory.WHITE;
    
    private static final int NUM_GATES = 2;
     
    private static final int GATE_CLOSED = 0;
    
    private static final int GATE_OPEN = 1;
    
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

    private int numGates;
    
    private int maxQueueLen;
    
    public AxisMultiVectorBarRenderer(SequenceChart sequenceChart, String fileName, String runName, EthIf eIf) {
        this.sequenceChart = sequenceChart;
        
        // for printing debug and warning messages to the Eclipse log
        this.log = SequenceChartPlugin.getDefault().getLog();
        
        this.dataVectors = new ArrayList<NNXYArray>();
        this.qVectors = new ArrayList<NNXYArray>();
        
        // populate the queue fill and schedule vectors for each gate
        for (int idx = 0; idx < eIf.size(); idx++) {
        	dataVectors.add(eIf.getInfo(idx).getSchedVector());
        	qVectors.add(eIf.getInfo(idx).getqVector());
        }
        
        this.numGates = dataVectors.size();
        // proceed anyway and see what happen
        if (numGates <= 0 || numGates >= 8) { log.warn("Number of gates is invalid!"); }
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
    	switch(numGates)
    	{
    	case 1:
    		return 60;
    	case 2:
    		return 80;
    	case 3:
    		return 120;
    	case 4:
    		return 160;
    	case 5:
    		return 180;
    	case 6:
    		return 200;
    	case 7:
    		return 220;
    	default:
    		this.log.warn("gate number is not within 0-7! Applying default height.");
    		return 180;
    	}
    }

    /**
     * Draws the TSN schedule along the axis, as well as the queue fill, for each gate number
     */
    public void drawAxis(Graphics graphics, IEvent startEvent, IEvent endEvent)
    {
        Rectangle rect = graphics.getClip(Rectangle.SINGLETON);
//        log.info("DA: events given: [start:" + startEvent.getEventNumber() + "]" + startEvent.getSimulationTime() + ", [end:" + endEvent.getEventNumber() + "]" + endEvent.getSimulationTime());
//        log.info("DA: clipping rect: " + rect);
        rect.height += 20;	// add extra space to the clip rect to allow us to draw more
        graphics.setClip(rect);
        
        // draw default color where no value is available
        graphics.setLineCap(SWT.CAP_SQUARE);
        graphics.setLineStyle(SWT.LINE_SOLID);
        graphics.setBackgroundColor(NO_VALUE_COLOR);
        graphics.fillRectangle(rect.x, 0, rect.right() - rect.x, getHeight());

        SequenceChartFacade sequenceChartFacade = sequenceChart.getInput().getSequenceChartFacade();
        IEventLog eventLog = sequenceChart.getInput().getEventLog();
        
        // first print the schedules
        for (int gateIdx = 0; gateIdx < (int)numGates; gateIdx++) {
        	data = dataVectors.get(gateIdx);

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
	
	                // black to indicate that something's gone wrong, TODO log warnings
	                graphics.setBackgroundColor(data.getY(i) == GATE_OPEN ? ColorFactory.GREEN : (data.getY(i) == GATE_CLOSED ? ColorFactory.RED : ColorFactory.BLACK));

	                if (phase == 0) {
	                    graphics.fillRectangle(x1, getScaledHeight()*gateIdx, x2 - x1, getScaledHeight());
	                    // the below is for drawing vertical bars at each gateState change
	                    // graphics.setForegroundColor(AXIS_COLOR);
	                    // graphics.drawLine(x1, getScaledHeight()*gateIdx, x1, gateIdx == 0 ? 0 : getScaledHeight()*(gateIdx-1));
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
	                                graphics.drawText("GATE " + (gateIdx) + "|" + (data.getY(i) == GATE_CLOSED ? "CLOSED" : "OPEN") + "|E:" + eventNumber + "[I:"+i+"]", x, gateIdx*getScaledHeight());
	                                x += sequenceChart.getClientArea().width;
	                            }
	                        }
	                    }
	                }
	            }
	        }
	        graphics.setForegroundColor(AXIS_COLOR);
	        graphics.drawLine(rect.x, gateIdx == 0 ? 0 : getScaledHeight()*(gateIdx-1), rect.right(), gateIdx == 0 ? 0 : getScaledHeight()*(gateIdx-1));
	        graphics.drawLine(rect.x, getScaledHeight()*gateIdx, rect.right(), getScaledHeight()*gateIdx);
        }
        
        graphics.drawLine(rect.x, getScaledHeight()*(int)numGates, rect.right(), getScaledHeight()*(int)numGates);


        // draw the queue fill lines
        for (int gateIdx = 0; gateIdx < (int)numGates; gateIdx++) {
        	qData = qVectors.get(gateIdx);
        	int size = qData.length();
        	if (qData.length() == 0) { continue; }
        	int maxQ = 0;
        	
        	// find the largest queue fill
        	for (int idx2 = 0; idx2 < size; idx2++) {
        		if (qData.getY(idx2) > maxQ) {maxQ = (int)qData.getY(idx2);}
        	}
        	if (maxQ == 0) {maxQ = 1;}	// set to 1 to avoid div by zero
        	
        	// divide the schedule rectangle into slices based on the max queue fill
//        	int slicedUp = (int)(getScaledHeight() / maxQ);
        	
			int startIndex = getIndexMine(startEvent, qData, true);
			if (startIndex == -1) { startIndex = 0; }
			
			int endIndex = getIndexMine(endEvent, qData, false);
			if (endIndex == -1) { endIndex = size; }

			long endEventNumber = endEvent.getEventNumber();

			for (int i = startIndex; i < endIndex; i++) {
                long eventNumber = qData.getEventNumber(i);
                long nextEventNumber = Math.min(endEventNumber, (i == size - 1) ? endEventNumber : qData.getEventNumber(i + 1));
            	
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

                graphics.setBackgroundColor(ColorFactory.BLUE);
                graphics.setForegroundColor(ColorFactory.BLUE);

            	int y1 = (int)(getScaledHeight()*(gateIdx)+(getScaledHeight() - convertScale(qData.getY(i),maxQ)));
            	int y2 = (int)(getScaledHeight()*(gateIdx)+(getScaledHeight() - convertScale(qData.getY((i == size - 1) ? i : i+1), maxQ)));
                log.info("DA: Y: " + qData.getY(i) + ", maxQ: " + maxQ + ", ConvScale: "+ convertScale(qData.getY(i),maxQ) + " y1: " + y1 + " y2: " + y2);

                graphics.fillRectangle(x1, y1, x2 - x1, (int)(5));

                int prevLW = graphics.getLineWidth();
                graphics.setLineWidth(5);
            	graphics.drawLine(x1 + (x2 - x1),y1+2, x2, y2+2);
            	graphics.setLineWidth(prevLW);
			}
			
        }
        data = dataVectors.get(0);
    }
    
    private int convertScale(double value, int max) {
    	return (int)((double)((double)getScaledHeight() / (double)max) * value);
    }
    
    private int getScaledHeight() {
    	return getHeight() / (int)numGates;
    }

    // modified getIndex() that uses NNXYArray and simpler logic
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
            log.warn("Event number OOB! [" + index + "] v.s. " + data.length());
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

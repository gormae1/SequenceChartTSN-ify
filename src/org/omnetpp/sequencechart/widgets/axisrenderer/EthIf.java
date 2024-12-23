package org.omnetpp.sequencechart.widgets.axisrenderer;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.core.runtime.ILog;
import org.omnetpp.scave.engine.XYArray;
import org.omnetpp.scave.engine.XYArrayVector;
import org.omnetpp.sequencechart.SequenceChartPlugin;
import org.omnetpp.eventlog.IEventLog;
import org.omnetpp.sequencechart.widgets.axisrenderer.NNXYArray;

/**
 * Represents a switch's Ethernet interface
 */
public class EthIf {
    public String moduleNameStart;
    // maps gates to queue fill and schedule vectors
    public TreeMap<Integer, TrafficClassInfo> gateToInfo;	
    
    public EthIf(String modName) {
        this.moduleNameStart = modName;
        this.gateToInfo = new TreeMap<Integer, TrafficClassInfo>();
    }

    /**
     * Add a mapping from gate to queue fill and schedule vectors. Converts 
     * the XYArrays to local NNXArrays.
     * 
     * @param gateVal the gate number to use as a key
     * @param schedVector the schedule (gateState) vector
     * @param qVector the queue fill (queueLength) vector
     */
    public void addInfo(int gateVal, XYArray schedVector, XYArray qVector) {
    	// first convert the XYArrays to NNXYArrays
    	NNXYArray schedNN = new NNXYArray(schedVector);
    	NNXYArray qNN = new NNXYArray(qVector);
    	
    	// add the gate number as a key to the map
        TrafficClassInfo toAdd = new TrafficClassInfo(gateVal, schedNN, qNN);
        if (gateVal < 0 || gateVal > 7) {throw new IllegalArgumentException();}
        
        gateToInfo.put(gateVal, toAdd);
    }

    /**
     * Retrieve the queue fill and schedule vectors given the gate number.
     * 
     * @param gateVal the gate number
     * @return the TrafficClassInfo containing the two related vectors
     */
    public TrafficClassInfo getInfo(int gateVal) {
        if (gateVal < 0 || gateVal > 7) {throw new IllegalArgumentException();}
        return gateToInfo.get(gateVal);
    }
    
    public int size() {
    	return gateToInfo.size();
    }


}

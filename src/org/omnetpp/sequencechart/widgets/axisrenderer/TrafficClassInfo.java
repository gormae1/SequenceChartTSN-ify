package org.omnetpp.sequencechart.widgets.axisrenderer;

import org.omnetpp.scave.engine.XYArray;
import org.omnetpp.scave.engine.XYArrayVector;
import org.omnetpp.sequencechart.widgets.axisrenderer.NNXYArray;

/**
 * Pairs a gate value with the queue fill and schedule vectors for the gate value.
 */
public class TrafficClassInfo {
    public int gateVal;
    @SuppressWarnings("unused")
    public NNXYArray schedVector;	// the schedule vector
    @SuppressWarnings("unused")
    public NNXYArray qVector;	// the queue fill vector

    public TrafficClassInfo(int val, NNXYArray schedVector, NNXYArray qVector) {
        this.gateVal = val;
        this.schedVector =  schedVector;
        this.qVector = qVector;
    };
    
    public NNXYArray getSchedVector() {
    	return schedVector;
    }
    
    public NNXYArray getqVector() {
    	return qVector;
    }   
    
}
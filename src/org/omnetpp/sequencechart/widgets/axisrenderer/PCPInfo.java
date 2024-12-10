package org.omnetpp.sequencechart.widgets.axisrenderer;

import org.omnetpp.scave.engine.XYArray;
import org.omnetpp.scave.engine.XYArrayVector;
import org.omnetpp.sequencechart.widgets.axisrenderer.NNXYArray;

public class PCPInfo {
    public int pcpVal;
    @SuppressWarnings("unused")
    public NNXYArray schedVector;
    @SuppressWarnings("unused")
    public NNXYArray qVector;

    public PCPInfo(int val, NNXYArray schedVector, NNXYArray qVector) {
        this.pcpVal = val;
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
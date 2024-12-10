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

public class EthIf {
    public String moduleNameStart;
    public TreeMap<Integer, PCPInfo> pcpToInfo;

    public EthIf(String modName) {
        this.moduleNameStart = modName;
        this.pcpToInfo = new TreeMap<Integer, PCPInfo>();
//        for (int idx = 0; idx < 8; idx++) {pcpToInfo[idx] = null;}
//        this.head = 0;
    }

    public void addInfo(int pcpVal, XYArray schedVector, XYArray qVector) {
    	NNXYArray schedNN = new NNXYArray(schedVector);
    	NNXYArray qNN = new NNXYArray(qVector);
    	
        PCPInfo toAdd = new PCPInfo(pcpVal, schedNN, qNN);
        if (pcpVal < 0 || pcpVal > 7) {throw new IllegalArgumentException();}
        
        pcpToInfo.put(pcpVal, toAdd);
    }

    public PCPInfo getInfo(int pcpVal) {
        if (pcpVal < 0 || pcpVal > 7) {throw new IllegalArgumentException();}
//        String dataDebug = "";
//        ILog log = SequenceChartPlugin.getDefault().getLog();
//        XYArray data = pcpToInfo[(pcpVal)].getSchedVector();
//        for (int idx = 0; idx < data.length(); idx++) {
//        	dataDebug += "(x:" + data.getX(idx) + "|y:" + data.getY(idx) + "|px:" + data.getPreciseX(idx) + "|e:" + data.getEventNumber(idx) + "),";
//        }
//        log.info("getInfo(" + pcpVal + "):" + dataDebug);
//        return pcpToInfo[(pcpVal)];
        return pcpToInfo.get(pcpVal);
    }
    
    public int size() {
    	return pcpToInfo.size();
//        ILog log = SequenceChartPlugin.getDefault().getLog();
//        int cntr = 0;
//        for (int idx = 0; idx < 8; idx++) {
//        	if (pcpToInfo[idx] != null) {cntr++;}
//        }
//        
//        log.info("PCP SIZE: " + cntr);
//    	return cntr;
    }


}

package org.omnetpp.sequencechart.widgets.axisrenderer;

import org.omnetpp.scave.engine.XYArray;
import org.omnetpp.scave.engine.XYArrayVector;

import java.math.BigDecimal;
import java.util.ArrayList;

public class NNXYArray {
	private ArrayList<Double> xs;
	private ArrayList<Double> ys;
	private ArrayList<BigDecimal> xps;
	private ArrayList<Long> ens;
	
	public NNXYArray(XYArray og) {
		this.xs = new ArrayList<Double>();
		this.ys = new ArrayList<Double>();
		this.xps = new ArrayList<BigDecimal>();
		this.ens = new ArrayList<Long>();
		
		convertOG(og);
	}
	
	public void convertOG(XYArray og) {
		for (int idx = 0; idx < og.length(); idx++) {
			this.xs.add(og.getX(idx));
			this.ys.add(og.getY(idx));
			this.xps.add((BigDecimal)og.getPreciseX(idx).toBigDecimal());
			this.ens.add(og.getEventNumber(idx));
		}
	}
	
	public double getX(int idx) {
		return xs.get(idx);
	}
	
	public double getY(int idx) {
		return ys.get(idx);
	}
	
	public BigDecimal getPreciseX(int idx) {
		return xps.get(idx);
	}
	
	public long getEventNumber(int idx) {
		return ens.get(idx);
	}
	
	public int size() {
		return xs.size();
	}
	
	public int length() {
		return this.size();
	}
	
	public String toString() {
		String repr = "";
		for (int idx = 0; idx < this.size(); idx++) {
			repr += "(X:" + xs.get(idx) + "|Y:" + ys.get(idx) + "|P:" + xps.get(idx) + "|E:" + ens.get(idx);
			if (idx != this.size()-1) { repr += "), "; }
			else { repr += ")."; }
		}
		return repr;
	}


}

/*
* Copyright (c) Joan-Manuel Marques 2013. All rights reserved.
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
*
* This file is part of the practical assignment of Distributed Systems course.
*
* This code is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This code is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this code.  If not, see <http://www.gnu.org/licenses/>.
*/

package recipes_service.tsae.data_structures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

//LSim logging system imports sgeag@2017
import edu.uoc.dpcs.lsim.LSimFactory;
import lsim.worker.LSimWorker;
import edu.uoc.dpcs.lsim.logger.LoggerManager.Level;

/**
 * @author Joan-Manuel Marques, Daniel Lázaro Iglesias
 * December 2012
 *
 */
public class TimestampMatrix implements Serializable{
	// Needed for the logging system sgeag@2017
	private transient LSimWorker lsim = LSimFactory.getWorkerInstance();
	
	private static final long serialVersionUID = 3331148113387926667L;
	ConcurrentHashMap<String, TimestampVector> timestampMatrix = new ConcurrentHashMap<String, TimestampVector>();
	
	public TimestampMatrix() {}
	
	public TimestampMatrix(List<String> participants){
		// create and empty TimestampMatrix
		for (Iterator<String> it = participants.iterator(); it.hasNext(); ){
			timestampMatrix.put(it.next(), new TimestampVector(participants));
		}
	}
	
	/**
	 * Not private for testing purposes.
	 * @param node
	 * @return the timestamp vector of node in this timestamp matrix
	 */
	TimestampVector getTimestampVector(String node){
		return timestampMatrix.get(node);
	}
	
	/**
	 * Merges two timestamp matrix taking the elementwise maximum
	 * @param tsMatrix
	 */
	public synchronized void updateMax(TimestampMatrix tsMatrix){
		Set<String> hosts = tsMatrix.timestampMatrix.keySet();
		for (String host : hosts) {
			TimestampVector tsv = timestampMatrix.get(host);
			TimestampVector tref = tsMatrix.getTimestampVector(host);
			if(tsv != null)
				tsv.updateMax(tref);
		}	

	}
	
	/**
	 * substitutes current timestamp vector of node for tsVector
	 * @param node
	 * @param tsVector
	 */
	public synchronized void update(String node, TimestampVector tsVector){
		timestampMatrix.put(node, tsVector);
	}
	
	/**
	 * 
	 * @return a timestamp vector containing, for each node, 
	 * the timestamp known by all participants
	 */
	public synchronized TimestampVector minTimestampVector(){
		TimestampVector min = null;
		
		for (Iterator<String> it = timestampMatrix.keySet().iterator(); it.hasNext(); ){
			String node = it.next();
			if (min == null) {
				min = timestampMatrix.get(node).clone();
			} else {
				min.mergeMin(timestampMatrix.get(node));

			}
		}
		return minVector;
	}
	public void setTimestampMatrix(ConcurrentHashMap<String, TimestampVector> timestampMatrix) {
		this.timestampMatrix = timestampMatrix;
	}
	
	/**
	 * clone
	 */
	public synchronized TimestampMatrix clone(){
		List<String> participants = new ArrayList<String>(timestampMatrix.keySet());
		TimestampMatrix matrix = new TimestampMatrix(participants);
		for (Iterator<String> it = timestampMatrix.keySet().iterator(); it.hasNext(); ){
			String participant = it.next();
			matrix.update(participant, timestampMatrix.get(participant));
		}

		return copyTMatrix;
	}
	
	/**
	 * equals
	 */
	@Override
	public synchronized boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TimestampMatrix other = (TimestampMatrix) obj;
		for (String node : timestampMatrix.keySet()) {
			TimestampVector summary = this.getTimestampVector(node);
			TimestampVector otherSummary = other.getTimestampVector(node);
			if (summary != otherSummary) {
				return false;
			}
		}
		return true;
	}

	
	/**
	 * toString
	 */
	@Override
	public synchronized String toString() {
		String all="";
		if(timestampMatrix==null){
			return all;
		}
		for(Enumeration<String> en=timestampMatrix.keys(); en.hasMoreElements();){
			String name=en.nextElement();
			if(timestampMatrix.get(name)!=null)
				all+=name+":   "+timestampMatrix.get(name)+"\n";
		}
		return all;
	}
}

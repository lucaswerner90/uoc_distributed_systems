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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

//LSim logging system imports sgeag@2017
import edu.uoc.dpcs.lsim.LSimFactory;
import edu.uoc.dpcs.lsim.logger.LoggerManager.Level;
import lsim.worker.LSimWorker;
import recipes_service.data.Operation;

/**
 * @author Joan-Manuel Marques, Daniel Lázaro Iglesias
 * December 2012
 *
 */
public class Log implements Serializable{
	// Needed for the logging system sgeag@2017
	private transient LSimWorker lsim = LSimFactory.getWorkerInstance();

	private static final long serialVersionUID = -4864990265268259700L;
	/**
	 * This class implements a log, that stores the operations
	 * received  by a client.
	 * They are stored in a ConcurrentHashMap (a hash table),
	 * that stores a list of operations for each member of 
	 * the group.
	 */
	private ConcurrentHashMap<String, List<Operation>> log= new ConcurrentHashMap<String, List<Operation>>();  

	public Log(List<String> participants){
		// create an empty log
		for (Iterator<String> it = participants.iterator(); it.hasNext(); ){
			log.put(it.next(), new Vector<Operation>());
		}
	}

	/**
	 * inserts an operation into the log. Operations are 
	 * inserted in order. If the last operation for 
	 * the user is not the previous operation than the one 
	 * being inserted, the insertion will fail.
	 * 
	 * @param op
	 * @return true if op is inserted, false otherwise.
	 */
	public synchronized boolean add(Operation op){
		lsim.log(Level.TRACE, "Inserting into Log the operation: "+op);
		Timestamp t, other;
		other = op.getTimestamp();
		List<Operation> operations = log.get(other.getHostid());
		if(operations.isEmpty()) {
			operations.add(op);
			return true;
		}
		t = operations.get(operations.size()-1).getTimestamp();
		if(t.compare(other) < 0) {
			operations.add(op);
			return true;
		} else {
			return false;
		}
		
	}
	
	/**
	 * Checks the received summary (sum) and determines the operations
	 * contained in the log that have not been seen by
	 * the proprietary of the summary.
	 * Returns them in an ordered list.
	 * @param sum
	 * @return list of operations
	 */
	public synchronized List<Operation> listNewer(TimestampVector sum){
		
		List<Operation> newer = new Vector<Operation>();
		
		for (String node : log.keySet()) {
			List<Operation> operations = log.get(node);
			Timestamp ts = sum.getLast(node);
			
			for (Operation op : operations) {
				if (op.getTimestamp().compare(ts) > 0) {
					newer.add(op);
				}
			}
		}
		return newer;
	}
	
	/**
	 * Removes from the log the operations that have
	 * been acknowledged by all the members
	 * of the group, according to the provided
	 * ackSummary. 
	 * @param ack: ackSummary.
	 */
	public synchronized void purgeLog(TimestampMatrix ack){
		List<String> participants = new Vector<String>(this.log.keySet());
		TimestampVector min = ack.minTimestampVector();
		for (Iterator<String> it = participants.iterator(); it.hasNext(); ){
			String node = it.next();
			for (Iterator<Operation> opIt = log.get(node).iterator(); opIt.hasNext();) {
				if (min.getLast(node) != null && opIt.next().getTimestamp().compare(min.getLast(node)) <= 0) {
					opIt.remove();
				}
			}
		}
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
		Log other = (Log) obj;
		/*for(Map.Entry<String, List<Operation>> entry : this.log.entrySet()) {
			List <Operation> otherOps = other.log.get(entry.getKey());
			if(!entry.getValue().equals(otherOps))
				System.out.println("LOS LOG NO SON IGUALES");
				return false;
		}*/
		List<Operation> ops = new Vector<Operation>();
		List<Operation> otherOps = new Vector<Operation>();
		List<Operation> loopOps, comparingOps;
		for (String node : log.keySet()) {
			ops = log.get(node);
			otherOps = other.log.get(node);
			if (ops.size() >= otherOps.size()) {
				loopOps = ops;
				comparingOps = otherOps;
			} else {
				loopOps = otherOps;
				comparingOps = ops;
			}
			for (Operation op : loopOps) {
				if (!comparingOps.contains(op)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * toString
	 */
	@Override
	public synchronized String toString() {
		String name="";
		for(Enumeration<List<Operation>> en=log.elements();
		en.hasMoreElements(); ){
		List<Operation> sublog=en.nextElement();
		for(ListIterator<Operation> en2=sublog.listIterator(); en2.hasNext();){
			name+=en2.next().toString()+"\n";
		}
	}
		
		return name;
	}
}
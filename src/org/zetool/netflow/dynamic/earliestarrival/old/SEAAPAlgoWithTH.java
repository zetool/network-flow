/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zetool.netflow.dynamic.earliestarrival.old;

import org.zetool.netflow.dynamic.problems.EarliestArrivalFlowProblem;
import org.zetool.netflow.dynamic.earliestarrival.EarliestArrivalAugmentingPath;
import org.zetool.algorithm.shortestpath.Dijkstra;
import org.zetool.netflow.ds.network.ImplicitTimeExpandedResidualNetwork;
import org.zetool.graph.Node;
import org.zetool.netflow.ds.flow.FlowOverTimeImplicit;
import java.util.Arrays;
import java.util.LinkedList;
import org.zetool.common.algorithm.Algorithm;
import org.zetool.common.algorithm.AlgorithmStatusEvent;
import org.zetool.netflow.dynamic.earliestarrival.EarliestArrivalAugmentingPathAlgorithm;
import org.zetool.netflow.dynamic.earliestarrival.EarliestArrivalAugmentingPathProblem;
/**
 *
 * @author schwengf
 */
public class SEAAPAlgoWithTH extends Algorithm<EarliestArrivalFlowProblem, FlowOverTimeImplicit>{
   
    private int arrivalTime;
    private int[] distances;
    private int flowUnitsSent;
    private EarliestArrivalAugmentingPath path;
    private EarliestArrivalAugmentingPathAlgorithm pathAlgorithm;
    private EarliestArrivalAugmentingPathProblem pathProblem;
    private boolean autoConvert = true;
    private int OriginTime;
    
    public SEAAPAlgoWithTH() {
        pathAlgorithm = new EarliestArrivalAugmentingPathAlgorithm();
    }

    public SEAAPAlgoWithTH(boolean b) {
        pathAlgorithm = new EarliestArrivalAugmentingPathAlgorithm();
        autoConvert = b;
    }

    @Override
    protected FlowOverTimeImplicit runAlgorithm(EarliestArrivalFlowProblem problem) {
        if (problem.getTotalSupplies() == 0) {
            drn = new ImplicitTimeExpandedResidualNetwork(problem);
            paths = new LinkedList<EarliestArrivalAugmentingPath>();
            return new FlowOverTimeImplicit(drn, paths);
        } 
        OriginTime = problem.getTimeHorizon();
        //System.out.println("TimeHorizon: " + OriginTime);
        flowUnitsSent = 0;        
        drn = new ImplicitTimeExpandedResidualNetwork(problem);     
        pathProblem = new EarliestArrivalAugmentingPathProblem(drn, drn.superSource(), problem.getSink(), problem.getTimeHorizon());
        pathAlgorithm.setProblem(pathProblem);
        calculateEarliestArrivalAugmentingPath();
        paths = new LinkedList<EarliestArrivalAugmentingPath>();
        while (!path.isEmpty() && path.getCapacity() > 0) {
            flowUnitsSent += path.getCapacity();
            //System.out.println("Sent flow units: " + flowUnitsSent);
            fireProgressEvent(flowUnitsSent * 1.0 / problem.getTotalSupplies(), String.format("%1$s von %2$s Personen evakuiert.", flowUnitsSent, problem.getTotalSupplies()));
            paths.add(path);
            drn.augmentPath(path);
            calculateEarliestArrivalAugmentingPath();
        }
        if (autoConvert) {
            fireEvent(new AlgorithmStatusEvent(this, "INIT_PATH_DECOMPOSITION"));
            FlowOverTimeImplicit flow = new FlowOverTimeImplicit(drn, paths);
            return flow;
            
        } else {
            return null;
        }
    }
    public LinkedList<EarliestArrivalAugmentingPath> paths;
    public ImplicitTimeExpandedResidualNetwork drn;

    private void calculateEarliestArrivalAugmentingPath() {
        if (flowUnitsSent == getProblem().getTotalSupplies()) {
            path = new EarliestArrivalAugmentingPath();
            return;
        }
        boolean pathFound = false;
        while (!pathFound) {
            pathAlgorithm.run();
            path = pathAlgorithm.getSolution();
            //System.out.println("Path: " + path);
            //System.out.println("arrival Time: " + path.getArrivalTime());
            //System.out.println("Capacity: " + path.getCapacity());
            if (path.isEmpty() || path.getCapacity() == 0) 
            {
                  path = new EarliestArrivalAugmentingPath();
                  //System.out.println("Kein Pfad gefunden");
                  return; 
             }
             
            else 
            {
                 pathFound = true;
            }
        }
        
    }

    private void calculateShortestPathLengths() {
        distances = new int[getProblem().getSources().size()];
        int index = 0;
        Dijkstra dijkstra = new Dijkstra(getProblem().getNetwork(), getProblem().getTransitTimes(), getProblem().getSink(), true);
        dijkstra.run();
        for (Node source : getProblem().getSources()) {
            System.out.println("source: " + source);
            distances[index++] = dijkstra.getDistance(source);
        }
        Arrays.sort(distances);
        for (int i=0; i< distances.length; i++)
        {
            System.out.println("Distanzen: " + distances[i]);
        }
    }

    public int getCurrentArrivalTime() {
        return arrivalTime;
    }

    private int getNextDistance(int currentDistance) {
        int index = Arrays.binarySearch(distances, currentDistance + 1);
        if (index >= 0) {
            return currentDistance + 1;
        } else {
            return distances[-index - 1];
        }
    }
}



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package systemj.signals.SOA;

import java.net.DatagramSocket;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author Atmojo
 */
public class DiscPortAssignment {
    private static Hashtable DiscPortAssignment = new Hashtable();
    private static Object DiscPortAssignmentLock = new Object();
    private static Vector DiscReplyPortReady = new Vector();
    private static Object DiscReplyPortReadyLock = new Object();
    
    private static Vector DiscCDNameTransmittingStat = new Vector();
    private static Object DiscCDNameTransmittingStatLock = new Object();
    
    public static void SetDiscPortAssignment(String CDName, DatagramSocket assignedPort){
        synchronized(DiscPortAssignmentLock){
            DiscPortAssignment.put(CDName, assignedPort);
        }
    }
   
    public static DatagramSocket GetDiscPortAssignment(String CDName){
        synchronized(DiscPortAssignmentLock){
            return (DatagramSocket)DiscPortAssignment.get(CDName);
        }
    }
    
     public static void SetCDNameTransmittingStat(String CDName){
        synchronized(DiscCDNameTransmittingStatLock){
            DiscCDNameTransmittingStat.addElement(CDName);
        }
     }
     
     public static boolean GetCDNameTransmittingStat(String CDName){
        synchronized(DiscCDNameTransmittingStatLock){
             if(DiscCDNameTransmittingStat.contains(CDName)){
                DiscCDNameTransmittingStat.removeElement(CDName);
                return true;
            } else {
                return false;
            }
        }
     }
    
    public static boolean IsDiscPortAssignmentExist(String CDName){
        synchronized(DiscPortAssignmentLock){
            if(DiscPortAssignment.containsKey(CDName)){
                return true;
            } else {
                return false;
            }
        }
    }
    public static void RemoveDiscPortAssignment(String CDName){
        synchronized(DiscPortAssignmentLock){
           DiscPortAssignment.remove(CDName);
        }
    }
     public static void SetDiscReplyPortReady(String CDName){
        synchronized(DiscReplyPortReadyLock){
            DiscReplyPortReady.addElement(CDName);
        }
    }
    public static boolean GetDiscReplyPortReady(String CDName){
        synchronized(DiscReplyPortReadyLock){
            if(DiscReplyPortReady.contains(CDName)){
                DiscReplyPortReady.removeElement(CDName);
                return true;
            } else {
                return false;
            }
        }
    }
    
    public static boolean CheckDiscReplyPortReady(String CDName){
        synchronized(DiscReplyPortReadyLock){
            if(DiscReplyPortReady.contains(CDName)){
                return true;
            } else {
                return false;
            }
        }
    }
}
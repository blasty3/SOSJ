/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package systemj.common;

import java.util.Hashtable;
import systemj.bootstrap.ClockDomain;

public class CDObjectsBuffer {
    private static Hashtable SSCDInstances = new Hashtable();
    private final static Object SSCDInstancesLock = new Object();
    private static Hashtable TempStrongMigCDInst = new Hashtable();
    private final static Object TempStrongMigCDInstLock = new Object();
    private static Hashtable TempWeakMigCDInst = new Hashtable();
    private final static Object TempWeakMigCDInstLock = new Object();
   
    public static void CopyCDInstancesToBuffer(Hashtable CDInstances){
        synchronized (SSCDInstancesLock){
            SSCDInstances = CDInstances;
        }
    }
    public static void AddCDInstancesToBuffer(String cdname,ClockDomain cdins){
        synchronized(SSCDInstancesLock){
            SSCDInstances.put(cdname, cdins);
        }
    }
    public static void RemoveCDInstancesFromBuffer(String cdname){
        synchronized(SSCDInstancesLock){
            SSCDInstances.remove(cdname);
        }
    }
    public static ClockDomain GetCDInstancesFromBuffer(String cdname){
        synchronized(SSCDInstancesLock){
            return (ClockDomain)SSCDInstances.get(cdname);
        }
    }
    public static Hashtable getAllCDInstancesFromBuffer(){
        synchronized (SSCDInstancesLock){
            return SSCDInstances;
        }
    }
    public static boolean CDObjBufferHas(String cdName){
       synchronized(SSCDInstancesLock){
           if(SSCDInstances.containsKey(cdName)){
               return true;
           } else {
               return false;
           }
       }
    }
    public static void AddCDObjToTempStrongMigBuffer(String OrigSSName, String CDName,ClockDomain cdInst){
        synchronized(TempStrongMigCDInstLock){
            Hashtable hash = new Hashtable();
            hash.put("CDInst", cdInst);
            hash.put("OrigSSName", OrigSSName);
            TempStrongMigCDInst.put(CDName, hash);
        }
    }
    public static Hashtable GetAllCDObjToTempStrongMigBuffer(){
        synchronized(TempStrongMigCDInstLock){
           Hashtable hash = TempStrongMigCDInst;
           TempStrongMigCDInst = new Hashtable();
           return hash;
        }
    }
    public static void AddCDObjToTempWeakMigBuffer(String OrigSSName,String CDName,ClockDomain cdInst){
        synchronized(TempWeakMigCDInstLock){
            Hashtable hash = new Hashtable();
            hash.put("CDInst", cdInst);
            hash.put("OrigSSName", OrigSSName);
            TempWeakMigCDInst.put(CDName, hash);
        }
    }
    public static Hashtable GetAllCDObjToTempWeakMigBuffer(){
        synchronized(TempWeakMigCDInstLock){
           Hashtable hash = TempWeakMigCDInst;
           TempWeakMigCDInst = new Hashtable();
           return hash;
        }
    }
    public static void ModifyCDNameOfCDObjTempWeakMigBuffer(String DesiredCDName, String CurrentCDName){
        synchronized(TempWeakMigCDInstLock){
            ClockDomain cd = (ClockDomain)TempWeakMigCDInst.get(CurrentCDName);
            cd.setName(DesiredCDName);
            TempWeakMigCDInst.remove(CurrentCDName);
            TempWeakMigCDInst.put(DesiredCDName, cd);
        }
    }
    public static boolean TempWeakMigBufferHasEntry(String CDEntryName){
        synchronized(TempWeakMigCDInstLock){
            if(TempWeakMigCDInst.containsKey(CDEntryName)){
                return true;
            } else{
                return false;
            }
        }
    }
}
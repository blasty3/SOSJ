/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemj.common.SOAFacility.Support;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.me.JSONException;
import org.json.me.JSONObject;

/**
 * contain all SOA msg buffer for data exchange between SOA thread
 * @author Udayanto
 */
public class SOABuffer {
   
    private static String RegBeaconPeriodTime;
    private static Vector DiscMsgBuffer = new Vector();
    private static Vector ReqAdvMsgBuffer = new Vector();
    
    private static JSONObject AdvVisibList = new JSONObject();
    private static boolean RequestForAdvTransmission = false;
    private static boolean RegNotify = false;
    private static volatile long notifID;
    private static boolean NotifyChangedCDStat = false;
    private static boolean ReceivedNotifyChangedStat = false;
    private static long lastAdvertisementTime=0;
    
    private static final Object RequestForAdvTransmissionLock = new Object();
    private static final Object RegNotifyLock = new Object();
    private static final Object notifIDLock = new Object();
    private static final Object NotifyChangedCDStatLock = new Object();
    private static final Object ReceivedNotifyChangedStatLock = new Object();
    private final static Object advTimeStampLock = new Object();
    private static final Object RegBeaconPeriodTimeLock = new Object();
    private static final Object ReqAdvMsgBufferLock = new Object();
    private static final Object DiscMsgBufferLock = new Object();
    
    private static String EmptySSName = null;
    private static final Object AdvVisibListLock = new Object();
    private static String SubnetMaskAddr;
    private static final Object SubnetMaskAddrLock = new Object();
    private static final Object SOSJRegistryIDLock = new Object();
    private static String SOSJRegistryID;
    private static String GtwyAddr;
    private static final Object GtwyAddrLock = new Object();
    private static final Object SOSJRegistryAddrLock = new Object();
    private static String SOSJRegistryAddr;
    private static final Object EmptySSNameLock = new Object();
    
    private static Vector CurrReqForAdvED = new Vector();
    private static final Object CurrReqForAdvEDLock = new Object();
    
    public static void AddEmptySSName(String SSName){
        synchronized(EmptySSNameLock){
            EmptySSName = SSName;
        }
    }
    
    public static String GetEmptySSName(){
        synchronized(EmptySSNameLock){
            return EmptySSName;
        }
    }
    
    public static void SetRegNotifySS(boolean stat){
        synchronized(RegNotifyLock){
            RegNotify = stat;
        }
    }
    
    public static boolean GetRegNotify(){
        synchronized(RegNotifyLock){
            return RegNotify;
        }
    }
    
    public static void SetNotifyChangedCDStat(boolean stat){
        synchronized(NotifyChangedCDStatLock){
           NotifyChangedCDStat = stat;
        }
    }
    
    public static boolean GetNotifyChangedCDStat(){
        synchronized(NotifyChangedCDStatLock){
            return NotifyChangedCDStat;
        }
    }
    
    public static void SetReceivedNotifyChangedCDStat(boolean stat){
        synchronized(ReceivedNotifyChangedStatLock){
           ReceivedNotifyChangedStat = stat;
        }
    }
    
    public static boolean GetReceivedNotifyChangedCDStat(){
        synchronized(ReceivedNotifyChangedStatLock){
            return ReceivedNotifyChangedStat;
        }
    }
    
    public static void SetNotifID(long notID){
        synchronized(notifIDLock){
            notifID = notID;
        }
    }
    
    public static long getNotif(){
        synchronized(notifIDLock){
            return notifID;
        }
    }
    
    public static void setSOSJRegID(String regID){
        synchronized(SOSJRegistryIDLock){
            SOSJRegistryID = regID;
        }
    }
    
    public static String getSOSJRegID(){
        synchronized(SOSJRegistryIDLock){
            return SOSJRegistryID;
        }
    }
    
    public static void setSOSJRegAddr(String regaddr){
        synchronized(SOSJRegistryAddrLock){
            SOSJRegistryAddr=regaddr;
        }
    }
    
    public static String getSOSJRegAddr(){
        synchronized(SOSJRegistryAddrLock){
            return SOSJRegistryAddr;
        }
    }
    
    public static void setGatewayAddr(String addr){
        synchronized(GtwyAddrLock){
            GtwyAddr=addr;
        }
    }
    
    public static String getGatewayAddr(){
        synchronized(GtwyAddrLock){
            return GtwyAddr;
        }
    }
    
    public static void setSubnetMaskAddr(String addr1){
        synchronized(SubnetMaskAddrLock){
            SubnetMaskAddr=addr1;
        }
        
    }
    
    public static String getSubnetMaskAddr(){
        synchronized(SubnetMaskAddrLock){
            return SubnetMaskAddr;
        }
    }
    
    public static void setSOSJRegBeaconPeriodTime(String addr){
        synchronized(RegBeaconPeriodTimeLock){
            RegBeaconPeriodTime=addr;
        }
        
    }
    
    public static String getSOSJRegBeaconPeriodTime(){
         synchronized(RegBeaconPeriodTimeLock){
             return RegBeaconPeriodTime;
        }
    }
    
    public static void putDiscMsgToDiscBuffer(JSONObject jsMsg){

        synchronized (DiscMsgBufferLock){
            DiscMsgBuffer.addElement(jsMsg);
        }

    }
    
    public static Vector getAllDiscMsgFromBuffer(){
        synchronized (DiscMsgBufferLock){
            Vector vec = DiscMsgBuffer;
            DiscMsgBuffer = new Vector();
            return vec;
        }
    }
    
    public static void removeAdvStatOfCDName(String assocCDName){
        synchronized(AdvVisibListLock){
            if(AdvVisibList.has(assocCDName)){
                AdvVisibList.remove(assocCDName);
            }
        }
    }
    
    public static void setAdvVisib(String assocCDName, String VisibStat){
        synchronized (AdvVisibListLock){
            try {
                AdvVisibList.put(assocCDName,VisibStat);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public static String getAdvVisib(String assocCDName){
       
        synchronized (AdvVisibListLock){
            String res = "";
            try {
                res = AdvVisibList.getString(assocCDName);
            } catch (JSONException ex) {
               ex.printStackTrace();
            }
            return res;
        }
    }
    
    public static void putReqAdvToReqAdvBuffer(JSONObject jsMsg){
        synchronized (ReqAdvMsgBufferLock){
            ReqAdvMsgBuffer.addElement(jsMsg);
        }
    }
    
    public static Vector getAllContentReqAdvBuffer(){
        Vector allReqAdvs = new Vector();
        synchronized (ReqAdvMsgBufferLock){
                allReqAdvs = ReqAdvMsgBuffer;
                ReqAdvMsgBuffer = new Vector();
        }
        return allReqAdvs;
    }
    
    public static void putCurrReqAdvED(String SSName){
        synchronized (CurrReqForAdvEDLock){
            CurrReqForAdvED.addElement(SSName);
        }
    }
    public static boolean IsSSNameReqForAdvED(String SSName){
        synchronized (CurrReqForAdvEDLock){
            if(CurrReqForAdvED.contains(SSName)){
                return true;
            } else {
                return false;
            }
        }
    }
    public static void removeCurrReqAdvED(String SSName){
        synchronized (CurrReqForAdvEDLock){
            if(CurrReqForAdvED.contains(SSName)){
               CurrReqForAdvED.removeElement(SSName);
            } 
        }
    }
   
      public static void SetAdvTransmissionRequest(boolean stat){
          synchronized (RequestForAdvTransmissionLock){
              RequestForAdvTransmission = stat;
          }
      }
      
      public static boolean getAdvTransmissionRequest(){
          synchronized (RequestForAdvTransmissionLock){
              return RequestForAdvTransmission;
          }
      }
      
      public static void RecordAdvertisementTimeStamp(){
        synchronized (advTimeStampLock){
            lastAdvertisementTime = System.currentTimeMillis();
        }
      }
    public static long getRecordedAdvertisementTimeStamp(){
        synchronized (advTimeStampLock){
                return lastAdvertisementTime;
        }
    }
}
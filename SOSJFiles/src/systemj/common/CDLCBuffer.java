/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package systemj.common;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import systemj.bootstrap.ClockDomain;

public class CDLCBuffer {
    private static final Object CDLCMigrationThreadBusyFlagLock = new Object();
    private static boolean CDLCMigrationThreadBusyFlag = false;
    private static JSONObject UpdateTempServiceDesc = new JSONObject();
    private final static Object UpdateTempServiceDescLock = new Object();
    private static JSONObject TempSSSignalChannelMap = new JSONObject();
    private final static Object TempSSSSignalChannelMapLock = new Object();
    private static Vector RequestCreateCDList = new Vector();
    private final static Object RequestCreateCDListLock = new Object();
    private static Vector RequestHibernateCDList = new Vector();
    private final static Object RequestHibernateCDListLock = new Object();
    private static Hashtable RequestMigrateBuffer = new Hashtable();
    private final static Object RequestMigrateBufferLock = new Object();
    private static Hashtable MigrationReqReport = new Hashtable();
    private final static Object MigrationReqReportLock = new Object();
    private static Vector RequestWakeUpCDList = new Vector();
    private final static Object RequestWakeUpCDListLock = new Object();
    private static Vector RequestKillCDList = new Vector();
    private final static Object RequestKillCDListLock = new Object();
    private static JSONObject MigTempSSSignalChannelMap = new JSONObject();
    private final static Object MigTempSSSignalChannelMapLock = new Object();
    private static final Object MigrationReqMsgBufferLock = new Object();
    private static Vector MigrationReqMsgBuffer = new Vector();
    private final static Object MigrationBusyFlagLock = new Object();
    private static boolean MigrationBusyFlag = false;
    private final static Object WeakMigrationDoneLock = new Object();
    private static boolean WeakMigrationDoneFlag = false;
    private final static Object StrongMigrationDoneLock = new Object();
    private static boolean StrongMigrationDoneFlag = false;
    private final static Object LinkCreationPortBusyFlagLock = new Object();
    private static boolean LinkCreationBusyFlag = false;
    private static boolean MigGoAheadModifyPartner = false;
    private static boolean MigModifyPartnerDone = false;
    private static Hashtable MigSSMigTypeCDObjs = new Hashtable();
    private static final Object MigGoAheadModifyPartnerLock  = new Object();
    private static final Object MigModifyPartnerDoneLock = new Object();
    private static final Object MigCDObjsLock = new Object();
    private static Vector OccuringMigDestSS = new Vector();
    private static final Object OccuringMigDestSSLock = new Object();
    
    private static final Object TempDevelCreateCDBufferLock = new Object();
    private static final Object CodeRecThreadDevelBusyLock = new Object();
    private static boolean CodeRecThreadDevelBusy = false;
    private static Vector TempDevelCreateCDBuffer = new Vector();
    private static final Object ChanReconfigureBufferLock = new Object();
    private static Vector ChanReconfigureBuffer = new Vector();
    private static final Object DistChanReconfigureBufferLock = new Object();
    private static Vector DistChanReconfigureBuffer = new Vector();
    private static final Object PartnerChanReconfigureBufferLock = new Object();
    private static Vector PartnerChanReconfigureBuffer = new Vector();
    private static final Object TempMigServDescBufferLock = new Object();
    private static JSONObject TempMigServDescBuffer = new JSONObject();
    private static final Object InvokeServPartnerChanReconfigureBufferLock = new Object();
    private static Vector InvokeServPartnerChanReconfigureBuffer = new Vector();
    private static final Object InvokeServ2ChanReconfigureBufferLock = new Object();
    private static Hashtable InvokeServ2ChanReconfigureBuffer = new Hashtable();
    private static final Object InvStatChanReconfigBufferLock = new Object();
    private static JSONObject InvStatChanReconfigBuffer = new JSONObject();
    private static final Object StatChanReconfigureBufferLock = new Object();
    private static JSONObject StatChanReconfigureBuffer = new JSONObject();
    private static final Object ReconfigInChanConfigIMBufferLock = new Object();
    private static Vector ReconfigInChanConfigIMBuffer = new Vector();
    
    public static void AddReconfigInChanConfigIMBuffer(String InChanCDName, String InChanChanName, String NewPartnerCDName, String NewPartnerChanName, String PartnerSSName){
        synchronized(ReconfigInChanConfigIMBufferLock){
            Hashtable hash = new Hashtable();
            hash.put("InchanName", InChanChanName);
            hash.put("InchanCDName", InChanCDName);
            hash.put("PartnerCDName", NewPartnerCDName);
            hash.put("PartnerChanCDName", NewPartnerChanName);
            hash.put("SSName", PartnerSSName);
            ReconfigInChanConfigIMBuffer.addElement(hash);
        }
    }
    public static boolean IsReconfigInChanConfigIMBufferEmpty(){
        synchronized(ReconfigInChanConfigIMBufferLock){
            if(ReconfigInChanConfigIMBuffer.size()==0){
                return true;
            } else {
                return false;
            }
        }
    }
    public static Vector GetReconfigInChanConfigIMBuffer(){
        synchronized(ReconfigInChanConfigIMBufferLock){
            return ReconfigInChanConfigIMBuffer;
        }
    }
    public static void clearReconfigInChanConfigIMBuffer(){
        synchronized(ReconfigInChanConfigIMBufferLock){
            ReconfigInChanConfigIMBuffer = new Vector();
        }
    }
    
    public static void AddDevelCreateCD(String CDName, JSONObject CDMap, JSONObject CDServDesc){
        synchronized(TempDevelCreateCDBufferLock){
            Hashtable req = new Hashtable();
            
            req.put("CDName",CDName);
            req.put("CDMap", CDMap);
            req.put("CDServDesc", CDServDesc);
            
            TempDevelCreateCDBuffer.addElement(req);
            
        }
    }
    
    public static boolean IsDevelCreateCDEmpty(){
        synchronized(TempDevelCreateCDBufferLock){
            if(TempDevelCreateCDBuffer.size()==0){
                return true;
            } else {
                return false;
            }
        }
    }
    
    public static void ClearDevelCreateCD(){
        synchronized(TempDevelCreateCDBufferLock){
            
            TempDevelCreateCDBuffer = new Vector();
            
        }
    }
    
    public static Vector getDevelCreateCD(){
        synchronized(TempDevelCreateCDBufferLock){
            Vector vec = new Vector();
            
            for(int i=0;i<TempDevelCreateCDBuffer.size();i++){
                
                vec.addElement((Hashtable)TempDevelCreateCDBuffer.get(i));
                
            }
            return vec;
            
        }
    }
    public static void SetDevelThreadBusyFlag(boolean stat){
        synchronized(CodeRecThreadDevelBusyLock){
            CodeRecThreadDevelBusy = stat;
        }
    }
    public static boolean GetDevelThreadBusyFlag(){
        synchronized(CodeRecThreadDevelBusyLock){
            return CodeRecThreadDevelBusy ;
        }
    }
  
    public static boolean AddInvokeServChanReconfig(String chcdname, String cname, String cdirection, String partnercdname, String partnercname){
        synchronized(InvokeServ2ChanReconfigureBufferLock){
                Hashtable hash2 = new Hashtable();
                hash2.put("PartnerCDName", partnercdname);
                hash2.put("PartnerChanName",partnercname);
                hash2.put("CDName",chcdname);
                hash2.put("ChanName",cname);
                hash2.put("ChanDir", cdirection);
                if(!InvokeServ2ChanReconfigureBuffer.containsKey(partnercdname)){
                    InvokeServ2ChanReconfigureBuffer.put(partnercdname,hash2);
                    return true;
                } else {
                    return false;
                }
        }
    }
    public static boolean AddInvokeServChanReconfig(String ssdest, String chcdname, String cname, String cdirection, String partnercdname, String partnercname){
        synchronized(InvokeServ2ChanReconfigureBufferLock){
                Hashtable hash2 = new Hashtable();
                hash2.put("PartnerCDName", partnercdname);
                hash2.put("PartnerChanName",partnercname);
                hash2.put("CDName",chcdname);
                hash2.put("ChanName",cname);
                hash2.put("ChanDir", cdirection);
                hash2.put("DestSS", ssdest);
                if(!InvokeServ2ChanReconfigureBuffer.containsKey(partnercdname)){
                    InvokeServ2ChanReconfigureBuffer.put(partnercdname,hash2);
                    return true;
                } else {
                    return false;
                }
        }
    }
    
    public static boolean IsInvokeServChanReconfigBufferEmpty(){
        synchronized(InvokeServ2ChanReconfigureBufferLock){
            if(InvokeServ2ChanReconfigureBuffer.size()==0){
                return true;
            } else {
                return false;
            }
        }
    }
    
    public static void ClearInvokeServChanReconfigBuffer(){
        synchronized(InvokeServ2ChanReconfigureBufferLock){
            InvokeServ2ChanReconfigureBuffer = new Hashtable();
        }
    }
    
    public static Hashtable GetReconfigInvokeServChanBuffer(){
        synchronized(InvokeServ2ChanReconfigureBufferLock){
            return InvokeServ2ChanReconfigureBuffer;
        }
    }
    public static void AddInvStatChanReconfig(String chcdname, String cname, boolean stat){
        synchronized(InvStatChanReconfigBufferLock){
              
            try {
                
                if(InvStatChanReconfigBuffer.has(chcdname)){
                    
                    JSONObject jsChanName = InvStatChanReconfigBuffer.getJSONObject(chcdname);
                    
                    jsChanName.put(cname, Boolean.toString(stat));
                        
                    InvStatChanReconfigBuffer.put(chcdname, jsChanName);
                     
                } else {
                    
                    JSONObject jsChanName = new JSONObject();
                    
                    jsChanName.put(cname, stat);
                    
                    InvStatChanReconfigBuffer.put(chcdname, jsChanName);
                    
                }
                
                
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
              
                
        }
    }
   
    public static boolean GetInvStatChanBuffer(String chcdname, String cname){
        synchronized(InvStatChanReconfigBufferLock){
            boolean stat = false;
            try {
                JSONObject jsChanName = InvStatChanReconfigBuffer.getJSONObject(chcdname);
                stat = Boolean.parseBoolean(jsChanName.getString(cname));
                jsChanName.remove(cname);
                if(jsChanName.length()==0){
                    InvStatChanReconfigBuffer = new JSONObject();
                } else {
                    InvStatChanReconfigBuffer.put(chcdname, jsChanName);
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            return stat;
        }
    }
    
    
    public static void AddStatChanReconfig(String chcdname, String cname, boolean stat){
        synchronized(StatChanReconfigureBufferLock){
              
            try {
                
                if(StatChanReconfigureBuffer.has(chcdname)){
                    
                    JSONObject jsChanName = StatChanReconfigureBuffer.getJSONObject(chcdname);
                    
                    jsChanName.put(cname, Boolean.toString(stat));
                        
                    StatChanReconfigureBuffer.put(chcdname, jsChanName);
                     
                } else {
                    
                    JSONObject jsChanName = new JSONObject();
                    
                    jsChanName.put(cname, stat);
                    
                    StatChanReconfigureBuffer.put(chcdname, jsChanName);
                    
                }
                
                
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
              
                
        }
    }
    
    public static void AddChanReconfig(String chcdname, String cname, String cdirection, String partnercdname, String partnercname){
        synchronized(ChanReconfigureBufferLock){
           
                Hashtable hash2 = new Hashtable();
                hash2.put("PartnerCDName", partnercdname);
                hash2.put("PartnerChanName",partnercname);
                hash2.put("CDName",chcdname);
                hash2.put("ChanName",cname);
                hash2.put("ChanDir", cdirection);
                ChanReconfigureBuffer.addElement(hash2);
        }
    }
    
    public static boolean IsChanReconfigBufferEmpty(){
        synchronized(ChanReconfigureBufferLock){
            if(ChanReconfigureBuffer.size()==0){
                return true;
            } else {
                return false;
            }
        }
    }
    
    public static void ClearChanReconfigBuffer(){
        synchronized(ChanReconfigureBufferLock){
            ChanReconfigureBuffer = new Vector();
        }
    }
    
    public static Vector GetReconfigChanBuffer(){
        synchronized(ChanReconfigureBufferLock){
            return ChanReconfigureBuffer;
        }
    }
    
     public static void AddDistChanReconfig(String chcdname, String cname, String cdirection, String partnercdname, String partnercname, String partnerSS){
        synchronized(DistChanReconfigureBufferLock){
                Hashtable hash2 = new Hashtable();
                hash2.put("PartnerCDName", partnercdname);
                hash2.put("PartnerChanName",partnercname);
                hash2.put("PartnerSSName", partnerSS);
                hash2.put("CDName",chcdname);
                hash2.put("ChanName",cname);
                hash2.put("ChanDir", cdirection);
                DistChanReconfigureBuffer.addElement(hash2);
        }
    }
    public static boolean IsDistChanReconfigBufferEmpty(){
        synchronized(DistChanReconfigureBufferLock){
            if(DistChanReconfigureBuffer.size()==0){
                return true;
            } else {
                return false;
            }
        }
    }
    public static void ClearDistChanReconfigBuffer(){
        synchronized(DistChanReconfigureBufferLock){
            DistChanReconfigureBuffer = new Vector();
        }
    }    
    public static Vector GetReconfigDistChanBuffer(){
        synchronized(DistChanReconfigureBufferLock){
            return DistChanReconfigureBuffer;
        }
    }
     public static void UpdateDistChanReconfigBuffer(Vector vec){
        synchronized(DistChanReconfigureBufferLock){
            DistChanReconfigureBuffer = vec;
        }
    }
    
    public static boolean IsOldInvokeServPartnerChanReconfigBufferEmpty(){
        synchronized(InvokeServPartnerChanReconfigureBufferLock){
            if(InvokeServPartnerChanReconfigureBuffer.size()==0){
                return true;
            } else {
                return false;
            }
        }
    }
    
    public static void ClearOldInvokeServPartnerChanReconfigBuffer(){
        synchronized(InvokeServPartnerChanReconfigureBufferLock){
            InvokeServPartnerChanReconfigureBuffer = new Vector();
        }
    }
    
    public static Vector GetOldInvokeServPartnerReconfigChanBuffer(){
        synchronized(InvokeServPartnerChanReconfigureBufferLock){
            return InvokeServPartnerChanReconfigureBuffer;
        }
    }
    
    public static void AddOldPartnerChanReconfig(String cdname, String channame,String partnercdirection, String partnercdname, String partnercname, String partnerssname){
        synchronized(PartnerChanReconfigureBufferLock){
            Hashtable hash2 = new Hashtable();
                hash2.put("PartnerCDName", partnercdname);
                hash2.put("PartnerChanName",partnercname);
                hash2.put("CDName", cdname);
                hash2.put("ChanName", channame);
                hash2.put("PartnerChanDir", partnercdirection);
                hash2.put("PartnerSSName", partnerssname);
                PartnerChanReconfigureBuffer.addElement(hash2);
        }
    }
    public static boolean IsOldPartnerChanReconfigBufferEmpty(){
        synchronized(PartnerChanReconfigureBufferLock){
            if(PartnerChanReconfigureBuffer.size()==0){
                return true;
            } else {
                return false;
            }
        }
    }
   
    public static void ClearOldPartnerChanReconfigBuffer(){
        synchronized(PartnerChanReconfigureBufferLock){
            PartnerChanReconfigureBuffer = new Vector();
        }
    }
    public static Vector GetOldPartnerReconfigChanBuffer(){
        synchronized(PartnerChanReconfigureBufferLock){
            return PartnerChanReconfigureBuffer;
        }
    }
    
    public static void AddOccuringMigDestSS(String ssname){
        synchronized(OccuringMigDestSSLock){
            OccuringMigDestSS.addElement(ssname);
        }
    }
    
    public static Vector GetAllOccuringMigDestSS(){
        synchronized(OccuringMigDestSSLock){
            return OccuringMigDestSS;
        }
    }
    public static boolean IsOccuringMigDestSS(String ssname){
        synchronized(OccuringMigDestSSLock){
            if(OccuringMigDestSS.contains(ssname)){
                return true;
            } else {
                return false;
            }
        }
    }
    
    public static void RemoveOccuringMigDestSS(String ssname){
        synchronized(OccuringMigDestSSLock){
            OccuringMigDestSS.removeElement(ssname);
        }
    }
    
    public static void SetCDLCMigrationFlagBusy(){
        synchronized(CDLCMigrationThreadBusyFlagLock){
            CDLCMigrationThreadBusyFlag = true;
        }
    }
    
    public static void SetCDLCMigrationFlagFree(){
        synchronized(CDLCMigrationThreadBusyFlagLock){
            CDLCMigrationThreadBusyFlag = false;
        }
    }
    
    public static boolean ISCDLCMigrationThreadBusy(){
        synchronized(CDLCMigrationThreadBusyFlagLock){
            return CDLCMigrationThreadBusyFlag;
        }
    }
    
    public static void SetMigModPartnerDone(boolean stat){
        synchronized(MigModifyPartnerDoneLock){
            MigModifyPartnerDone = stat;
        }
    }
    
    public static void SetMigGoAheadModPartner(boolean stat){
        synchronized(MigGoAheadModifyPartnerLock){
            MigGoAheadModifyPartner = stat;
        }
    }
    
    public static boolean GetMigGoAheadModPartner(){
        synchronized(MigGoAheadModifyPartnerLock){
            return MigGoAheadModifyPartner ;
        }
    }
    
    public static boolean GetMigModPartnerDone(){
        synchronized(MigModifyPartnerDoneLock){
            return MigModifyPartnerDone;
        }
    }
   
    public static void SetAllMigLocPartReconfBuffer(String DestSS, String MigType, Vector vecAllCDName,JSONObject migratingMap){
        synchronized(MigCDObjsLock){
            Hashtable hash = new Hashtable();
            hash.put("CDNames", vecAllCDName);
            hash.put("MigType", MigType);
            hash.put("MigratingMap", migratingMap);
            MigSSMigTypeCDObjs.put(DestSS,hash);
        }       
    }

    public static Hashtable GetAllMigLocPartReconfBufferEmpty(){
        synchronized(MigCDObjsLock){
            return MigSSMigTypeCDObjs;
        }
    }
    
    public static boolean IsAllMigLocPartReconfBufferEmpty(){
        synchronized(MigCDObjsLock){
            if(MigSSMigTypeCDObjs.size()==0){
                return true;
            } else {
                return false;
            }
        }
    }
    
    public static Hashtable GetMigLocPartReconfBuffer(String destSS){
        synchronized(MigCDObjsLock){
            return (Hashtable)MigSSMigTypeCDObjs.get(destSS);
        }
    }
    
    public static boolean MigLocPartReconfBufferHas(String destSS){
        synchronized(MigCDObjsLock){
            if(MigSSMigTypeCDObjs.containsKey(destSS)){
                return true;
            } else {
                return false;
            }
        }
    }
    
     public static void RemoveMigLocPartReconfBuffer(String destSS){
        synchronized(MigCDObjsLock){
            MigSSMigTypeCDObjs.remove(destSS);
        }
    }
   
    public static void SetStrongMigrationDoneFlag(){
        synchronized(StrongMigrationDoneLock){
            StrongMigrationDoneFlag = true;
        }
    }
    
    public static void ResetStrongMigrationDoneFlag(){
        synchronized(StrongMigrationDoneLock){
            StrongMigrationDoneFlag = false;
        }
    }
    
    public static boolean GetStrongMigrationDoneFlag(){
        synchronized(StrongMigrationDoneLock){
            return StrongMigrationDoneFlag;
        }
    }
    
    public static void SetWeakMigrationDoneFlag(){
        synchronized(WeakMigrationDoneLock){
            WeakMigrationDoneFlag = true;
        }
    }
    
    public static void ResetWeakMigrationDoneFlag(){
        synchronized(WeakMigrationDoneLock){
            WeakMigrationDoneFlag = false;
        }
    }
    
    public static boolean GetWeakMigrationDoneFlag(){
        synchronized(WeakMigrationDoneLock){
            return WeakMigrationDoneFlag;
        }
    }
    
    public static void SetLinkCreationBusyFlag(boolean stat){
        synchronized(LinkCreationPortBusyFlagLock){
            LinkCreationBusyFlag = stat;
        }
    }
    
    public static boolean GetLinkCreationBusyFlag()  {
        synchronized (LinkCreationPortBusyFlagLock){
            return LinkCreationBusyFlag;
        }
    }
    
    public static void updateMigTempSignalChannelMap(String cdname , JSONObject js){
        synchronized(MigTempSSSignalChannelMapLock){
            try {
                MigTempSSSignalChannelMap.put(cdname, js);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public static JSONObject getAllMigTempSignalChannelMap(){
        synchronized(MigTempSSSignalChannelMapLock){
            return MigTempSSSignalChannelMap;
        }
    }
  
    public static void setMigrationBusyFlag(){
        synchronized(MigrationBusyFlagLock){
            MigrationBusyFlag = true;
        }
    }
    
    public static void releaseMigrationBusyFlag(){
        synchronized(MigrationBusyFlagLock){
            MigrationBusyFlag=false;
        }
    }
    
    public static boolean getMigrationBusyFlag(){
        synchronized(MigrationBusyFlagLock){
            return MigrationBusyFlag;
        }
    }
        
    public static Vector getMigrationRequestMsg(){
        
        Vector migReqMsg = new Vector();
        
        synchronized(MigrationReqMsgBufferLock){
            migReqMsg = MigrationReqMsgBuffer;
            MigrationReqMsgBuffer = new Vector();
        }
        
        return migReqMsg;
    }
   
    
    // End CD migration
    
    //Update CD Signal Remap
   
    
    // End Update CD Signal Remap
    
    // Update Service Description
    
    public static void putUpdateServiceDescription(JSONObject js){
        synchronized(UpdateTempServiceDescLock){
            Enumeration keysEn = js.keys();
            while(keysEn.hasMoreElements()){
                String servName = keysEn.nextElement().toString();
                try {
                    UpdateTempServiceDesc.put(servName, js.getJSONObject(servName));
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
   
    public static JSONObject GetTempUpdateServDesc(){
        synchronized(UpdateTempServiceDescLock){
            return UpdateTempServiceDesc;
        }
    }
    
    public static void ClearTempUpdateServDesc(){
        synchronized(UpdateTempServiceDescLock){
            UpdateTempServiceDesc = new JSONObject();
        }
    }
    
    // End Update Service Description
    
    //Create CD
    
    public static void updateTempSignalChannelMap(JSONObject js){
        synchronized(TempSSSSignalChannelMapLock){
            TempSSSignalChannelMap = js;
        }
    }
    
    public static JSONObject getAllTempSignalChannelMap(){
        synchronized(TempSSSSignalChannelMapLock){
            return TempSSSignalChannelMap;
        }
    }
    
   
    public static boolean IsTempSigChanMapEmpty(){
        synchronized(TempSSSSignalChannelMapLock){
            return TempSSSignalChannelMap.isEmpty();
        }
    }
    
    public static JSONObject GetTempSigChanMap(String Cdname){
        synchronized(TempSSSSignalChannelMapLock){
            JSONObject js = new JSONObject();
            try {
                js.put(Cdname,TempSSSignalChannelMap.getJSONObject(Cdname));
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            return js;
        }
    }
    
    public static void AddTempSigChanMapCD(JSONObject jsCDMap){
        synchronized(TempSSSSignalChannelMapLock){
            Enumeration keys = jsCDMap.keys();
            while(keys.hasMoreElements()){
                String key = keys.nextElement().toString();
                try {
                    JSONObject jsCDDet = jsCDMap.getJSONObject(key);
                    TempSSSignalChannelMap.put(key, jsCDDet);
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    public static void RemoveTempSigChanMap(String Cdname){
        synchronized(TempSSSSignalChannelMapLock){
                TempSSSignalChannelMap.remove(Cdname);
        }
    }
    
    public static boolean IsRequestCreateCDEmpty(){
        synchronized(RequestCreateCDListLock){
            if(RequestCreateCDList.size()==0){
                return true;
            } else {
                return false;
            }
        }
    }
    
    public static void clearRequestCreateCD(){
        synchronized(RequestCreateCDListLock){
            RequestCreateCDList = new Vector();
        }
    }
    
    public static Vector getAllRequestCreateCD(){
        synchronized(RequestCreateCDListLock){
            return RequestCreateCDList;
        }
    }
    
    public static String getRequestCreateCD(int i){
        synchronized(RequestCreateCDListLock){
            return RequestCreateCDList.get(i).toString();
        }
    }
    
    public static int getRequestCreateSize(){
        synchronized(RequestCreateCDListLock){
            return RequestCreateCDList.size();
        }
    }
    
    public static void TransferRequestCreateCDToBuffer(String CDName){
        synchronized(RequestCreateCDListLock){
            RequestCreateCDList.addElement(CDName);
        }
    }
    
    // End Create CD
  
     public static void TransferRequestWakeUpCDToBuffer(String cdname){
      synchronized(RequestWakeUpCDListLock){
          //for (int i=0;i<vec.size();i++){
              RequestWakeUpCDList.addElement(cdname);
          //}
          
      }
    }
   
    public static Vector GetAllRequestWakeUpCD(){
        synchronized(RequestWakeUpCDListLock){
            return RequestWakeUpCDList;
        }
    }
    
    public static int GetRequestWakeUpCDSize(){
        synchronized(RequestWakeUpCDListLock){
            return RequestWakeUpCDList.size();
        }
    }
    
    public static void ClearRequestWakeUpCD(){
        synchronized(RequestWakeUpCDListLock){
            RequestWakeUpCDList = new Vector();
        }
    }
    
    public static boolean IsRequestWakeUpCDEmpty(){
        synchronized(RequestWakeUpCDListLock){
            if(RequestWakeUpCDList.size()==0){
                return true;
            } else {
                return false;
            }
        }
    }
    
    //Trigger Hibernate
    
    public static void TransferRequestHibernateCDToBuffer(String cdname){
      synchronized(RequestHibernateCDListLock){
          
          //for (int i=0;i<vec.size();i++){
              RequestHibernateCDList.addElement(cdname);
          //}
          
          //RequestHibernateCDList = vec;
      }
   }
    
    public static int GetRequestHibernateCDSize(){
        synchronized(RequestHibernateCDListLock){
            return RequestHibernateCDList.size();
        }
    }
    
    public static String GetRequestHibernateCD(int index){
        synchronized(RequestHibernateCDListLock){
            return RequestHibernateCDList.get(index).toString();
        }
    }
    
    public static Vector GetAllRequestHibernateCD(){
        synchronized(RequestHibernateCDListLock){
            return RequestHibernateCDList;
        }
    }
    
    public static void ClearRequestHibernateCD(){
        synchronized(RequestHibernateCDListLock){
            RequestHibernateCDList = new Vector();
        }
    }
    
    public static void RemoveRequestHibernateCD(int i){
        synchronized(RequestHibernateCDListLock){
            RequestHibernateCDList.remove(i);
        }
    }
    
    public static boolean IsRequestHibernateCDEmpty(){
        synchronized(RequestHibernateCDListLock){
            if(RequestHibernateCDList.size()==0){
                return true;
            } else {
                return false;
            }
        }
    }
    
    public static void TransferRequestKillCDToBuffer(String CDName){
        synchronized(RequestKillCDListLock){
            
              RequestKillCDList.addElement(CDName);
           
        }
    }
    
    public static String GetRequestKillCD(int index){
        synchronized(RequestKillCDListLock){
            return RequestKillCDList.get(index).toString();
        }
    }
    
    public static void ClearRequestKillCD(){
        synchronized(RequestKillCDListLock){
            RequestKillCDList = new Vector();
        }
    }
    
    public static boolean IsRequestKillCDEmpty(){
        synchronized(RequestKillCDListLock){
            if(RequestKillCDList.size()==0){
                return true;
            } else {
                return false;
            }
        }
    }
    
    public static int GetRequestKillCDSize(){
        synchronized(RequestKillCDListLock){
            return RequestKillCDList.size();
        }
    }
    
    // Migration buffer
    
    public static boolean IsRequestMigrateEmpty(){
        synchronized(RequestMigrateBufferLock){
            if(RequestMigrateBuffer.size()==0){
                return true;
            } else {
                return false;
            }
        }
    }
    
    public static void AddRequestMigrate(String CDName, String DestinationSS, JSONObject jsCDMap, JSONObject jsServDesc, String MigType){
        synchronized(RequestMigrateBufferLock){
            
            try{
             
            //hash.put("CDName",CDName);
            
            if(RequestMigrateBuffer.containsKey(DestinationSS)){
                
                JSONObject hashMigType = (JSONObject) RequestMigrateBuffer.get(DestinationSS);
                
                if(MigType.equalsIgnoreCase("strong")){
                    
                    if(hashMigType.has("strong")){
                        JSONObject AllCD = (JSONObject)hashMigType.get("strong");
                    
                        Vector CDNameList = new Vector();
                    
                        CDNameList = (Vector)AllCD.get("CDNameList");
                    
                    //add new CDMapping
                    
                        JSONObject jsAllCDMap = (JSONObject)AllCD.get("CDMap");
                    
                    Enumeration keysjsCDMap = jsCDMap.keys();
                    
                    while(keysjsCDMap.hasMoreElements()){
                        String CDMapEntry = keysjsCDMap.nextElement().toString();
                        
                        JSONObject CDMapDet = jsCDMap.getJSONObject(CDMapEntry);
                        
                        jsAllCDMap.put(CDMapEntry, CDMapDet);
                        
                    }
                    
                    AllCD.put("CDMap", jsAllCDMap);
                    
                    //end add CD Mapping
                    //add new Serv description
                    
                    JSONObject jsAllCDServDesc = (JSONObject)AllCD.get("CDServDesc");
                    
                    Enumeration keysjsCDServDesc = jsServDesc.keys();
                    
                    while(keysjsCDServDesc.hasMoreElements()){
                        String CDServDescEntry = keysjsCDServDesc.nextElement().toString();
                        
                        JSONObject CDServDescDet = jsServDesc.getJSONObject(CDServDescEntry);
                        
                        jsAllCDServDesc.put(CDServDescEntry, CDServDescDet);
                        
                    }
                    
                    AllCD.put("CDServDesc", jsAllCDServDesc);
                    
                    //end serv desc
                    
                    CDNameList.addElement(CDName);
                    
                    Object objVecAllCD = (Object) CDNameList;
                
                    AllCD.put("CDNameList", objVecAllCD);
                    
                    hashMigType.put("strong", AllCD);
                    
                    RequestMigrateBuffer.put(DestinationSS, hashMigType);
                    
                } else {
                    JSONObject CDNameList = new JSONObject();
                
                    Vector vecAllCD = new Vector();
                
                    vecAllCD.addElement(CDName);
                
                    //add new CDMapping
                    
                    CDNameList.put("CDMap", jsCDMap);
                    
                    //end add CD Mapping
                    //add new Serv description
                    
                    CDNameList.put("CDServDesc", jsServDesc);
                    
                    //end serv desc
                    
                    JSONObject migtypeCD = new JSONObject();
                    
                    Object objVecAllCD = (Object) vecAllCD;
                
                    CDNameList.put("CDNameList",objVecAllCD);
                
                   
                
                    migtypeCD.put(MigType, CDNameList);
                
                    RequestMigrateBuffer.put(DestinationSS, migtypeCD);
                }
                    
                } else if (MigType.equalsIgnoreCase("weak")){
                    
                    if(hashMigType.has("weak")){
                    JSONObject AllCD = (JSONObject)hashMigType.get("weak");
                    
                    Vector CDNameList = new Vector();
                    
                    CDNameList = (Vector)AllCD.get("CDNameList");
                    
                    CDNameList.addElement(CDName);
                    
                    //add new CDMapping
                    
                    JSONObject jsAllCDMap = (JSONObject)AllCD.get("CDMap");
                    
                    Enumeration keysjsCDMap = jsCDMap.keys();
                    
                    while(keysjsCDMap.hasMoreElements()){
                        String CDMapEntry = keysjsCDMap.nextElement().toString();
                        
                        JSONObject CDMapDet = jsCDMap.getJSONObject(CDMapEntry);
                        
                        jsAllCDMap.put(CDMapEntry, CDMapDet);
                        
                    }
                    
                    AllCD.put("CDMap", jsAllCDMap);
                    
                    //end add CD Mapping
                    //add new Serv description
                    
                    JSONObject jsAllCDServDesc = (JSONObject)AllCD.get("CDServDesc");
                    
                    Enumeration keysjsCDServDesc = jsServDesc.keys();
                    
                    while(keysjsCDServDesc.hasMoreElements()){
                        String CDServDescEntry = keysjsCDServDesc.nextElement().toString();
                        
                        JSONObject CDServDescDet = jsServDesc.getJSONObject(CDServDescEntry);
                        
                        jsAllCDServDesc.put(CDServDescEntry, CDServDescDet);
                        
                    }
                    
                    AllCD.put("CDServDesc", jsAllCDServDesc);
                    
                    //end serv desc
                    
                    Object objVecAllCD = (Object) CDNameList;
                
                    
                    
                    AllCD.put("CDNameList", objVecAllCD);
                    
                    hashMigType.put("weak", AllCD);
                    
                    RequestMigrateBuffer.put(DestinationSS, hashMigType);
                } else {
                    
                    JSONObject CDNameList = new JSONObject();
                
                    Vector vecAllCD = new Vector();
                
                    vecAllCD.addElement(CDName);
                
                    //add new CDMapping
                    
                    CDNameList.put("CDMap", jsCDMap);
                    
                    //end add CD Mapping
                    //add new Serv description
                    
                    CDNameList.put("CDServDesc", jsServDesc);
                    
                    //end serv desc
                    
                    JSONObject migtypeCD = new JSONObject();
                    Object objVecAllCD = (Object) vecAllCD;
                
                    CDNameList.put("CDNameList",objVecAllCD);
                
                    migtypeCD.put(MigType, CDNameList);
                
                    RequestMigrateBuffer.put(DestinationSS, migtypeCD);
                
                }
                    
                } else {
                    System.out.println("Migration type is not recognized");
                }
                
            } else {
                
                if(MigType.equalsIgnoreCase("strong") || MigType.equalsIgnoreCase("weak") ){
                    
                    JSONObject CDNameList = new JSONObject();
                
                    Vector vecAllCD = new Vector();
                
                    vecAllCD.addElement(CDName);
                
                    //add new CDMapping
                    
                    CDNameList.put("CDMap", jsCDMap);
                    
                    //end add CD Mapping
                    //add new Serv description
                    
                    CDNameList.put("CDServDesc", jsServDesc);
                    
                    //end serv desc
                    
                    JSONObject migtypeCD = new JSONObject();
                    Object objVecAllCD = (Object) vecAllCD;
                
                    CDNameList.put("CDNameList",objVecAllCD);
                
                    migtypeCD.put(MigType, CDNameList);
                
                    RequestMigrateBuffer.put(DestinationSS, migtypeCD);
                    
                } else {
                    System.out.println("Migration type is not recognized");
                }
                
            }
                
            } catch (Exception ex){
                ex.printStackTrace();
            }
            
        }
    }
    
    public static void AddRequestMigrate(String CDName, String DestinationSS, JSONObject jsCDMap, String MigType){
        synchronized(RequestMigrateBufferLock){
            
            try{
             
            //hash.put("CDName",CDName);
            
            if(RequestMigrateBuffer.containsKey(DestinationSS)){
                
                JSONObject hashMigType = (JSONObject) RequestMigrateBuffer.get(DestinationSS);
                
                if(MigType.equalsIgnoreCase("strong")){
                    
                  if(hashMigType.has("strong")){
                        Hashtable AllCD = (Hashtable)hashMigType.get("strong");
                    
                        Vector CDNameList = new Vector();
                    
                        CDNameList = (Vector)AllCD.get("CDNameList");
                    
                    //add new CDMapping
                    
                        JSONObject jsAllCDMap = (JSONObject)AllCD.get("CDMap");
                    
                    Enumeration keysjsCDMap = jsCDMap.keys();
                    
                    while(keysjsCDMap.hasMoreElements()){
                        String CDMapEntry = keysjsCDMap.nextElement().toString();
                        
                        JSONObject CDMapDet = jsCDMap.getJSONObject(CDMapEntry);
                        
                        jsAllCDMap.put(CDMapEntry, CDMapDet);
                        
                    }
                    
                    AllCD.put("CDMap", jsAllCDMap);
                    
                    //end add CD Mapping
                    //add new Serv description
                   
                    //end serv desc
                    
                    CDNameList.addElement(CDName);
                    
                    AllCD.put("CDNameList", CDNameList);
                    
                    hashMigType.put("strong", AllCD);
                    
                    RequestMigrateBuffer.put(DestinationSS, hashMigType);
                    
                 } else {
                    Hashtable CDNameList = new Hashtable();
                
                    Vector vecAllCD = new Vector();
                
                    vecAllCD.addElement(CDName);
                
                    //add new CDMapping
                    
                    
                    
                    CDNameList.put("CDMap", jsCDMap);
                    
                    //end add CD Mapping
                    //add new Serv description
                    
                    
                    //CDNameList.put("CDServDesc", jsServDesc);
                    
                    //end serv desc
                    
                   // Hashtable migtypeCD = new Hashtable();
                
                    CDNameList.put("CDNameList",vecAllCD);
                
                    hashMigType.put(MigType, CDNameList);
                
                    RequestMigrateBuffer.put(DestinationSS,hashMigType);
                  }
                    
                } else if (MigType.equalsIgnoreCase("weak")){
                    
                    if(hashMigType.has("weak")){
                    Hashtable AllCD = (Hashtable)hashMigType.get("weak");
                    
                    Vector CDNameList = new Vector();
                    
                    CDNameList = (Vector)AllCD.get("CDNameList");
                    
                    CDNameList.addElement(CDName);
                    
                    //add new CDMapping
                    
                    JSONObject jsAllCDMap = (JSONObject)AllCD.get("CDMap");
                    
                    Enumeration keysjsCDMap = jsCDMap.keys();
                    
                    while(keysjsCDMap.hasMoreElements()){
                        String CDMapEntry = keysjsCDMap.nextElement().toString();
                        
                        JSONObject CDMapDet = jsCDMap.getJSONObject(CDMapEntry);
                        
                        jsAllCDMap.put(CDMapEntry, CDMapDet);
                        
                    }
                    
                    AllCD.put("CDMap", jsAllCDMap);
                    
                    //end add CD Mapping
                    //add new Serv description
                    
                    
                    
                    //end serv desc
                    
                    AllCD.put("CDNameList", CDNameList);
                    
                    hashMigType.put("weak", AllCD);
                    
                    RequestMigrateBuffer.put(DestinationSS, hashMigType);
                } else {
                    
                    Hashtable CDNameList = new Hashtable();
                
                    Vector vecAllCD = new Vector();
                
                    vecAllCD.addElement(CDName);
                
                    //add new CDMapping
                    
                    CDNameList.put("CDMap", jsCDMap);
                    
                    //end add CD Mapping
                    //add new Serv description
                    
                    
                    
                    //end serv desc
                    
                    //Hashtable migtypeCD = new Hashtable();
                
                    CDNameList.put("CDNameList",vecAllCD);
                
                    hashMigType.put(MigType, CDNameList);
                
                    RequestMigrateBuffer.put(DestinationSS, hashMigType);
                
                   }
                    
                } else {
                    System.out.println("Migration type is not recognized");
                }
                
            } else {
                
                if(MigType.equalsIgnoreCase("strong") || MigType.equalsIgnoreCase("weak") ){
                    
                    Hashtable CDNameList = new Hashtable();
                
                    Vector vecAllCD = new Vector();
                
                    vecAllCD.addElement(CDName);
                
                    //add new CDMapping
                    
                    CDNameList.put("CDMap", jsCDMap);
                    
                    //end add CD Mapping
                    //add new Serv description
                    
                    
                    
                    //end serv desc
                    
                   
                
                    CDNameList.put("CDNameList",vecAllCD);
                    
                    JSONObject migtypeCD = new JSONObject();
                
                    migtypeCD.put(MigType, CDNameList);
                
                    RequestMigrateBuffer.put(DestinationSS, migtypeCD);
                    
                } else {
                    System.out.println("Migration type is not recognized");
                }
                
            }
                
            } catch (Exception ex){
                ex.printStackTrace();
            }
            
        }
    }
    
    public static Vector GetAllCDNameOfMigTypeAndDestSS(String destSS, String migType){
        synchronized(RequestMigrateBufferLock){
            Vector allCDVec = new Vector();
            JSONObject hashMig = (JSONObject)RequestMigrateBuffer.get(destSS);
            JSONObject AllCD;
            try {
                AllCD = (JSONObject)hashMig.get(migType);
                 allCDVec = (Vector) AllCD.get("CDNameList");
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            
            return allCDVec;
        }
    }
    
    
     //use this when req migrate of type and ss has been deleted
    public static void RecoverReqMigrateAllCD(String destSS, String migType, Vector vecAllCDNames){
        
        synchronized(RequestMigrateBufferLock){
            
           // Hashtable recoveredCDs = new Hashtable();
            
           // recoveredCDs.put(migType, vecAllCDNames);
           // 
           // RequestMigrateBuffer.put(destSS, recoveredCDs);
            
        }
    }
  
    public static void RemoveReqMigrate(String destSS,String migType){
        synchronized(RequestMigrateBufferLock){
            JSONObject hashMig = (JSONObject)RequestMigrateBuffer.get(destSS);
            if(hashMig.length()==1){
                RequestMigrateBuffer.remove(destSS);
            } else {
                hashMig.remove(migType);
                RequestMigrateBuffer.put(destSS,hashMig);
            }
        }
    }
    
    public static Hashtable GetRequestMigrate(){
        synchronized(RequestMigrateBufferLock){
            return RequestMigrateBuffer;
        }
    }
   
      public static void SetMigrationStatus(String DestSS, String Report){
          synchronized(MigrationReqReportLock){
              MigrationReqReport.put(DestSS,Report);
          }
      }
      
      public static String GetMigrationStatus(String DestSS){
          synchronized(MigrationReqReportLock){
              return (String)MigrationReqReport.get(DestSS);
          }
      }
      
      public static void AddTempMigServDesc(String CDName, JSONObject js){
          synchronized(TempMigServDescBufferLock){
              try {
                  TempMigServDescBuffer.put(CDName, js);
              } catch (JSONException ex) {
                 ex.printStackTrace();
              }
          }
      }
      
      public static JSONObject GetTempMigServDesc(String assocCDName){
          synchronized(TempMigServDescBufferLock){
              JSONObject jsResult = new JSONObject();
              Enumeration keysMigServName = TempMigServDescBuffer.keys();
              while(keysMigServName.hasMoreElements()){
                  String keyMigServName = keysMigServName.nextElement().toString();
                  try {
                      JSONObject IndivServDesc = TempMigServDescBuffer.getJSONObject(keyMigServName);
                      String assCDName = IndivServDesc.getString("associatedCDName");
                      if(assCDName.equals(assocCDName)){
                          jsResult.put(keyMigServName,IndivServDesc);
                          break;
                      }
                  } catch (JSONException ex) {
                      ex.printStackTrace();
                  }
              }
              return jsResult;
          }
      }
}
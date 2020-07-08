package sysj;

import java.util.Enumeration;
import java.util.Vector;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import systemj.common.CDLCBuffer;
import systemj.common.RegAllCDStats;
import systemj.common.RegAllSSAddr;
import systemj.common.SOAFacility.*;

public class SJ {
    public static synchronized void CreateCD(String CDName, String filenameCDMapXML, String filenameCDServDescXML){
            JSONObject CDMap = ParseCDMap(CDName, filenameCDMapXML);
            JSONObject CDServDesc = ParseServDesc(filenameCDServDescXML);
            CDLCBuffer.TransferRequestCreateCDToBuffer(CDName);
            CDLCBuffer.AddTempSigChanMapCD(CDMap);
            CDLCBuffer.putUpdateServiceDescription(CDServDesc);
    }
    
    //Update Mapping only
    
    //end mapping only
    
    public static synchronized void KillCD(String CDName){
            CDLCBuffer.TransferRequestKillCDToBuffer(CDName);
    }
    
    
    public static synchronized void MigrateCD(String CDName,String fileNameCDMap, String fileNameCDServDesc, String DestinationSS, String MigType){
        
        //synchronized (MigrateCDLock){
            if(MigType.equals("strong") || MigType.equals("weak")){
           
                JSONObject CDMap = ParseCDMap(fileNameCDMap);
                JSONObject CDServDesc = ParseServDesc(fileNameCDServDesc);
            
                
                CDLCBuffer.AddRequestMigrate(CDName, DestinationSS, CDMap, CDServDesc,MigType);
            
            } else {
            
                throw new RuntimeException("Unknown migration type, choose either 'weak' or 'strong'");
            }
        //}
        
    }
    
    public static synchronized void SuspendCD(String CDName){
            CDLCBuffer.TransferRequestHibernateCDToBuffer(CDName);
    }
    public static synchronized void WakeUpCD(String CDName){
            CDLCBuffer.TransferRequestWakeUpCDToBuffer(CDName);
    }
    
    public static synchronized String GetCDMacroState(String CDName){
        String stat = "NONE";
        if(RegAllCDStats.GetAllIntCDMacroState().has(CDName)){
            stat = RegAllCDStats.GetIntCDMacroState(CDName);
        } else {
            JSONObject allCDStats = RegAllCDStats.getAllCDStats();
            Enumeration keysAllCDs = allCDStats.keys();
            while(keysAllCDs.hasMoreElements()){
                String SSName = keysAllCDs.nextElement().toString();
                try {
                    JSONObject allCDinSS = allCDStats.getJSONObject(SSName);
                    if(allCDinSS.has(CDName)){
                        stat = allCDinSS.getString(CDName);
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return stat;
    }
    
    public static JSONObject ParseCDMap(String filename){
        CDLCMapParser cdpars = new CDLCMapParser();
        JSONObject js = new JSONObject();
        try {
            js = cdpars.parse(filename);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return js;
    }
    
    public static synchronized void ReconfigChan(String CDName, String ChanName, String ChanDir, String PartnerCDName, String PartnerChanName, String PartnerSSName){
       
        CDLCBuffer.AddChanReconfig(CDName, CDName, ChanDir, PartnerCDName, PartnerChanName);
    }
    
    public static synchronized void ReconfigChan(String CDName, String ChanName, String ChanDir, String PartnerCDName, String PartnerChanName){
        
    }
    
    public static JSONObject ParseCDMap(String CDName, String filename){
        CDLCMapParser cdpars = new CDLCMapParser();
        JSONObject js = new JSONObject();
        try {
            js = cdpars.parse(CDName, filename);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return js;
    }
    public static JSONObject ParseCDMap(String CDName, String CDClassName, String filename){
        CDLCMapParser cdpars = new CDLCMapParser();
        JSONObject js = new JSONObject();
        try {
            js = cdpars.parse(CDName, CDClassName,filename);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return js;
    }
    
    public static JSONObject ParseCDMap(String CDName, String CDClassName, String inchanName,String filename){
        CDLCMapParser cdpars = new CDLCMapParser();
        JSONObject js = new JSONObject();
        try {
            js = cdpars.parse(CDName, CDClassName,inchanName,filename);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return js;
    }
    
    public static JSONObject ParseCDMap(String CDName, String CDClassName, String inchanName,String ochanName,String filename){
        CDLCMapParser cdpars = new CDLCMapParser();
        JSONObject js = new JSONObject();
        try {
            js = cdpars.parse(CDName, CDClassName,inchanName,ochanName,filename);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return js;
    }
    public static JSONObject ParseServDesc(String filename){
        ServDescParser cdsdparse = new ServDescParser();
        JSONObject js = new JSONObject();
        try {
            js = cdsdparse.parse(filename);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return js;
    }
    
    public static JSONObject ParseServDesc(String CDName, String ServName,String filename){
        
        ServDescParser cdsdparse = new ServDescParser();
        
        JSONObject js = new JSONObject();
        try {
            js = cdsdparse.parse(CDName,ServName,filename);
            
            //System.out.println("Parsed New SD: " +js.toPrettyPrintedString(2, 0));
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return js;
        
    }
    
   
    
    /*
    public static boolean IsPartnerTerminated(String CDName, String ChannelName, String ChannelDirection){ //in or out
        synchronized(ChannelQueryLock){
            
            boolean res = false;
            
            JSONObject jsAllCDMap = SJSSCDSignalChannelMap.getCurrentSignalChannelMapping();
            
            try {
                JSONObject jsCDTrgtMap = jsAllCDMap.getJSONObject(CDName);
                
                JSONObject jsChansCD = jsCDTrgtMap.getJSONObject("SChannels");
                
                if(ChannelDirection.equalsIgnoreCase("input")){
                     
                    JSONObject jsInChansCD = jsChansCD.getJSONObject("inputs");
                    
                    if(jsInChansCD.has(ChannelName)){
                        
                        Scheduler sc = (Scheduler)SchedulersBuffer.ObtainSchedulers().get(0);
                        
                        if(sc.SchedulerHasCD(CDName)){
                            
                            String cname = ChannelName+"_in";
                            
                            ClockDomain cdins = sc.getClockDomain(CDName);
                            
                            try {
                                Field f = cdins.getClass().getField(cname);
                                
                                input_Channel inchan = (input_Channel)f.get(cdins);
                                
                                res = inchan.IsInputChannelTerminated();
                                
                            } catch (Exception ex) {
                               ex.printStackTrace();
                               System.exit(1);
                            } 
                            
                        } else if(CDObjectsBuffer.CDObjBufferHas(CDName)){
                            
                            String cname = ChannelName+"_in";
                            
                            ClockDomain cdins = CDObjectsBuffer.GetCDInstancesFromBuffer(CDName);
                            
                            try {
                                Field f = cdins.getClass().getField(cname);
                                
                                input_Channel inchan = (input_Channel)f.get(cdins);
                                
                                res = inchan.IsInputChannelTerminated();
                                
                            } catch (Exception ex) {
                               ex.printStackTrace();
                               System.exit(1);
                            } 
                            
                            
                        }
                        
                    } else {
                        System.err.println(ChannelDirection+ " Channel of name "+ChannelName+" in the CD name: " +CDName+ " is non existent!");
                        System.exit(1);
                    }
                    
                } else if(ChannelDirection.equalsIgnoreCase("output")){
                    
                    JSONObject jsOutChansCD = jsChansCD.getJSONObject("outputs");
                    
                    if(jsOutChansCD.has(ChannelName)){
                        
                        Scheduler sc = (Scheduler)SchedulersBuffer.ObtainSchedulers().get(0);
                        
                        if(sc.SchedulerHasCD(CDName)){
                            
                            String cname = ChannelName+"_out";
                            
                            ClockDomain cdins = sc.getClockDomain(CDName);
                            
                            try {
                                Field f = cdins.getClass().getField(cname);
                                
                                output_Channel ochan = (output_Channel)f.get(cdins);
                                
                                res = ochan.IsOutputChannelTerminated();
                                
                            } catch (Exception ex) {
                               ex.printStackTrace();
                               System.exit(1);
                            } 
                            
                        } else if(CDObjectsBuffer.CDObjBufferHas(CDName)){
                            
                            String cname = ChannelName+"_out";
                            
                            ClockDomain cdins = CDObjectsBuffer.GetCDInstancesFromBuffer(CDName);
                            
                            try {
                                Field f = cdins.getClass().getField(cname);
                                
                                output_Channel ochan = (output_Channel)f.get(cdins);
                                
                                res = ochan.IsOutputChannelTerminated();
                                
                            } catch (Exception ex) {
                               ex.printStackTrace();
                               System.exit(1);
                            } 
                            
                            
                        }
                        
                    } else {
                        System.err.println(ChannelDirection+ " Channel of name"+ChannelName+" in the CD name: " +CDName+ " is non existent!");
                        System.exit(1);
                    }
                    
                } else {
                    
                    System.err.println("Wrong direction for IsChannelTerminatedFunction executed in CD: " +CDName+" , choose input or output!");
                    
                    System.exit(1);
                }
                
            } catch (JSONException ex) {
                
                System.err.println("Channel "+ChannelName+" in the CD name: " +CDName+ " is non existent!");
                
                ex.printStackTrace();
               
                
            }
            
            return res;
            
        }
    }
    */
}

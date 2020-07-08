/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemj.common;

import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.json.me.JSONTokener;
import systemj.common.SOAFacility.Support.SOABuffer;

/**
 *
 * @author Udayanto
 */
public class SJSOAMessage{
    
    private JSONObject responseAdvertisementMessage,broadcastDiscoveryMessage,DiscoveryReplyMessage,requestAdvertiseMessage,ReqMigrationMessage;
    
    public String ConstructNoP2PServToRegDiscoveryMessage(String SSOrigin, String regID, int recPort){
        JSONObject jsDiscMsg = new JSONObject();
        try {
            jsDiscMsg = new JSONObject();
            jsDiscMsg.put("sourceAddress",SJSSCDSignalChannelMap.GetLocalSSAddr());
            jsDiscMsg.put("MsgType","Discovery");
            jsDiscMsg.put("associatedSS",SSOrigin);
            jsDiscMsg.put("regID", regID);
            jsDiscMsg.put("respPort", Integer.toString(recPort));
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return jsDiscMsg.toString();
    }
    
     public JSONObject ConstructRespAdvertisementInJSON(String expiryTime, String regID, String SSOrigAddr, boolean notifySSStat, String SSOrigin){
        
         JSONObject AdvMsg = new JSONObject();
         
         try {
            //NoP2PProvToRegRespAdvMsg = new JSONObject();
            AdvMsg.put("MsgType","Advertisement");
            AdvMsg.put("sourceAddress", SJSSCDSignalChannelMap.GetLocalSSAddr());
            AdvMsg.put("associatedSS",SSOrigin);
            AdvMsg.put("regID", regID);
            AdvMsg.put("SSAddr", SSOrigAddr);
            AdvMsg.put("expiryTime", expiryTime);
            AdvMsg.put("CDStats", RegAllCDStats.GetAllIntCDMacroState());
            AdvMsg.put("SSAddrs", RegAllSSAddr.getAllSSAddr());
            
              
            JSONObject jsInclServ = new JSONObject();
            JSONObject jsIntReg = SJServiceRegistry.obtainInternalRegistry();
            
            Enumeration keysJSIntReg = jsIntReg.keys();
            
            while(keysJSIntReg.hasMoreElements()){
                
                String servName = keysJSIntReg.nextElement().toString();
                
                JSONObject jsIndivServ = jsIntReg.getJSONObject(servName);
                
                String assocCDName = jsIndivServ.getString("associatedCDName");
                
                String CDStat = RegAllCDStats.GetIntCDMacroState(assocCDName);
                
                if(CDStat.equalsIgnoreCase("Active")){
                    jsInclServ.put(servName, jsIndivServ);
                }
                
            }
            
            //advertisementMessage.put("portForResponse","7777");
           
            AdvMsg.put("Notify", Boolean.toString(notifySSStat));
            AdvMsg.put("serviceList", jsInclServ);
        } catch (JSONException ex) {
            System.out.println("What happens in ConstructReadvertisementMessage: " +ex.getMessage());
        }
        return AdvMsg;
    }
    
    public String ConstructReqAdvertisementMessage(String regID, String regAddr, String destSS, String port){  //
        JSONObject regRequestAdvertiseMessage = new JSONObject();
        try {
            regRequestAdvertiseMessage.put("MsgType","RequestForAdvertisement");
            regRequestAdvertiseMessage.put("regID", regID);
            regRequestAdvertiseMessage.put("destSS", destSS);
            regRequestAdvertiseMessage.put("regAddr",regAddr);
            regRequestAdvertiseMessage.put("respPort", port);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return regRequestAdvertiseMessage.toString();
    }
    
    public String ConstructRegRequestAdvertisementMessage(String targetRegID, String srcAddr){  //uses port 8888 now
        
        JSONObject regRequestAdvertiseMessage = new JSONObject();
        
        try {
            
            regRequestAdvertiseMessage.put("MsgType","regRequestAdvertise");
            regRequestAdvertiseMessage.put("regID", targetRegID);
            //regRequestAdvertiseMessage.put("regAddr",regAddr);
            regRequestAdvertiseMessage.put("sourceAddress", srcAddr);
            //regRequestAdvertiseMessage.put("destAddr",destAddress);
            //regRequestAdvertiseMessage.put("expServiceType",SJServiceRegistry.getConsumerExpectedServiceType());
            
        } catch (JSONException ex) {
            System.out.println("What happens in ConstructRegReqReadvertisementMessage: " +ex.getMessage());
        }
        return regRequestAdvertiseMessage.toString();
    }
    
    public String ConstructRegNotifyMessage(String regID, String notifyID){
        JSONObject regNotifyMessage = new JSONObject();
        try {
            regNotifyMessage.put("MsgType","Notify");
            regNotifyMessage.put("regID", regID);
            regNotifyMessage.put("notifyID",notifyID);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return regNotifyMessage.toString();
    }
    
    public JSONObject ConstructRegResponseReAdvertisementMessageInJSON(String expiryTime, String regID, String regAddr){
        
        JSONObject regRespAdvMsg = new JSONObject();
        
        try {
           
            regRespAdvMsg.put("MsgType","regRespReqAdvertise");
            regRespAdvMsg.put("regID",regID);
            regRespAdvMsg.put("regAddr", regAddr);
            regRespAdvMsg.put("expiryTime", expiryTime);
            //regRespAdvMsg.put("sourceAddress", jsRegReqAdvMsg.getString("sourceAddress"));
            //SJServiceRegistry.AppendSourceIPAddressToMessage(regRespAdvMsg);
            //regRespAdvMsg.put("associatedSS",SSOrigin);
            //responseAdvertisementMessage.put("advertisementExpiry", expiryTime);   
            //responseAdvertisementMessage.put("portForResponse","7777");
            //responseAdvertisementMessage.put("serviceList", SJServiceRegistry.obtainInternalRegistryProviderOnly());
            //helloMessage.put("serviceList",currentDetailedServiceRegistry.getString("Node0")); //perhaps this isn't needed at all
        } catch (JSONException ex) {
           ex.printStackTrace();
        }
        return regRespAdvMsg;
    }
   
    
    public String ConstructDiscoveryReplyMessage(JSONObject jsMsg,JSONObject jsAllServ)
    {
        JSONObject serviceList = new JSONObject();
        JSONObject processedSJMessage = new JSONObject();
        try {
                Enumeration keysSSName = jsAllServ.keys();
                while (keysSSName.hasMoreElements()){
                    String keySSName = keysSSName.nextElement().toString();
                    JSONObject jsAllSSServ = jsAllServ.getJSONObject(keySSName);
                    serviceList.put(keySSName,jsAllSSServ);
                }
                    processedSJMessage.put("destinationAddress",jsMsg.getString("sourceAddress"));
                    processedSJMessage.put("sourceAddress",SOABuffer.getSOSJRegAddr());
                    processedSJMessage.put("serviceList",serviceList);
                    processedSJMessage.put("CDStats", RegAllCDStats.getAllCDStats());
                    processedSJMessage.put("SSAddrs", RegAllSSAddr.getAllSSAddr());
                    processedSJMessage.put("MsgType","DiscoveryReply");
                    //if(jsMsg.has("associatedSS")){
                        processedSJMessage.put("destSS",jsMsg.getString("associatedSS"));
                    //}
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return processedSJMessage.toString();
    }
    
    public String ConstructRegistryBeaconMessage(String refreshTime, String RegistryID, String RegAddr){
        JSONObject RegAdvMessage = new JSONObject();
        try {
            RegAdvMessage.put("MsgType","Beacon");
            RegAdvMessage.put("regID",RegistryID);
            RegAdvMessage.put("regAddr", RegAddr);
            RegAdvMessage.put("retransmissionTime", refreshTime);
        } catch (JSONException ex) {
           ex.printStackTrace();
        }
        return RegAdvMessage.toString();
    }
    
   
      public JSONObject ConstructResponseReAdvertisementMessageInJSON(String expiryTime, String destAddr, String SSOrigin){
        try {
            responseAdvertisementMessage = new JSONObject();
            responseAdvertisementMessage.put("MsgType","responseReqAdvertise");
            responseAdvertisementMessage.put("destAddr",destAddr);
            responseAdvertisementMessage.put("sourceAddress", SJSSCDSignalChannelMap.GetLocalSSAddr());
            responseAdvertisementMessage.put("associatedSS",SSOrigin);
            responseAdvertisementMessage.put("expiryTime", expiryTime);   
            //responseAdvertisementMessage.put("portForResponse","7777");
            responseAdvertisementMessage.put("serviceList", SJServiceRegistry.obtainInternalRegistry());
            //helloMessage.put("serviceList",currentDetailedServiceRegistry.getString("Node0")); //perhaps this isn't needed at all
        } catch (JSONException ex) {
            System.out.println("What happens in ConstructReadvertisementMessage: " +ex.getMessage());
        }
        return responseAdvertisementMessage;
    }
      
   
     public String ConstructAdvertisementMessage(String expiryTime, boolean notifySSStat, boolean ChangedCDStat, String SSOrigin, String SSOrigAddr, String regID){
        JSONObject advertisementMessage = new JSONObject();
         try {
            JSONObject inclAdv = new JSONObject();
            JSONObject jsIntServ = SJServiceRegistry.obtainInternalRegistry();
            Enumeration keysjsIntServ = jsIntServ.keys();
            while (keysjsIntServ.hasMoreElements()){
                Object keyjsIndivServ = keysjsIntServ.nextElement();
                JSONObject jsIndivServ = jsIntServ.getJSONObject(keyjsIndivServ.toString());
                String assocCDName = jsIndivServ.getString("associatedCDName");
                String CDStat = RegAllCDStats.GetIntCDMacroState(assocCDName);
                String ServVisibStat = SOABuffer.getAdvVisib(assocCDName);
                if(CDStat.equalsIgnoreCase("Active") && ServVisibStat.equalsIgnoreCase("visible")){
                    inclAdv.put(jsIndivServ.getString("serviceName"), jsIndivServ);
                }
            }
            advertisementMessage = new JSONObject();
            advertisementMessage.put("MsgType","Advertisement");
            advertisementMessage.put("sourceAddress", SJSSCDSignalChannelMap.GetLocalSSAddr());
            advertisementMessage.put("associatedSS",SSOrigin);
            advertisementMessage.put("SSAddr", SSOrigAddr);
            advertisementMessage.put("regID", regID);
            advertisementMessage.put("CDStats", RegAllCDStats.GetAllIntCDMacroState());
            advertisementMessage.put("expiryTime", expiryTime);   
            advertisementMessage.put("serviceList", inclAdv);
            advertisementMessage.put("changedCDStat", ChangedCDStat);
            advertisementMessage.put("Notify", Boolean.toString(notifySSStat));
        } catch (JSONException ex) {
           ex.printStackTrace();
        }
        return advertisementMessage.toString();
    }
     
     public String ConstructAdvertisementMessage(String SJMessage)
    {
        String processedSJMessage ="";
        //SJSOAMessage sjdisc = new SJSOAMessage();
        try {
            JSONObject jsMsg = new JSONObject(new JSONTokener(SJMessage));
            if (jsMsg.getString("MsgType").equalsIgnoreCase("RequestForAdvertisement")){String LocalSSName = SJSSCDSignalChannelMap.getLocalSSName();
                String regID = jsMsg.getString("regID");
                String expTime = Long.toString(SJSSCDSignalChannelMap.GetSSExpiryTime());
                boolean notify = SOABuffer.GetRegNotify();
                boolean notifyCDStat = SOABuffer.GetNotifyChangedCDStat();
                String SSName = SJSSCDSignalChannelMap.getLocalSSName();
                String SSAddr = SJSSCDSignalChannelMap.GetLocalSSAddr();
                processedSJMessage = ConstructAdvertisementMessage(expTime, notify,notifyCDStat,SSName,SSAddr,regID);
                //processedSJMessage = ConstructRespAdvertisementInJSON(Long.toString(SJSSCDSignalChannelMap.GetSSExpiryTime()), regID, SJSSCDSignalChannelMap.GetLocalSSAddr(),false, LocalSSName);
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return processedSJMessage;
    }
     
    /*
    public String ConstructReqWeakMigrationMessage(String destinationSubsystem, String SSOrigin){
        try {
            String sourceAddress = SJSSCDSignalChannelMap.GetLocalSSAddr();
            //to get address, must access Serv registry
            JSONObject jsInt = SJServiceRegistry.obtainInternalRegistry();
            
           // Enumeration keysJsInt = jsInt.keys();
            
           
            
            ReqWeakMigrationMessage = new JSONObject();
            
            ReqWeakMigrationMessage.put("MsgType","requestWeakServiceMigration");
            ReqWeakMigrationMessage.put("sourceAddress",sourceAddress);
            ReqWeakMigrationMessage.put("associatedSS",SSOrigin);
            ReqWeakMigrationMessage.put("destinationSubsystem",destinationSubsystem);
            //ReqMigrationMessage.put("destAddress",destAddress);
            
            
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        
        return ReqWeakMigrationMessage.toString();
    }
    */
    
    public String ConstructReqMigrationMessage(String destinationSubsystem, String migType, String recPort, String rcvPort,String SSOrigin){
        try {
            
            String sourceAddress = SJSSCDSignalChannelMap.GetLocalSSAddr();
            //to get address, must access Serv registry
            
            ReqMigrationMessage = new JSONObject();
            ReqMigrationMessage.put("migType", migType);
            ReqMigrationMessage.put("MsgType","requestMigration");
            ReqMigrationMessage.put("sourceAddress",sourceAddress);
            ReqMigrationMessage.put("associatedSS",SSOrigin);
            ReqMigrationMessage.put("destinationSubsystem",destinationSubsystem);
            ReqMigrationMessage.put("recPort", recPort);
            ReqMigrationMessage.put("rcvPort", rcvPort);
           
            //ReqMigrationMessage.put("destAddress",destAddress);
            
            
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        
        return ReqMigrationMessage.toString();
    }
    
    public String ConstructResponseMigrationMessage(String ACK, String SSOrigin, String SSDest){
        JSONObject RespMigrationMessage = new JSONObject();
        try {
            
            String sourceAddress = SJSSCDSignalChannelMap.GetLocalSSAddr();
            
            RespMigrationMessage.put("associatedSS",SSOrigin);
            RespMigrationMessage.put("MsgType", "responseReqServiceMigration");
            RespMigrationMessage.put("destinationSubsystem",SSDest );
            RespMigrationMessage.put("sourceAddress",sourceAddress);
            RespMigrationMessage.put("data", ACK);
            
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        
        return RespMigrationMessage.toString();
    }
    
    public String ConstructResponseMigrationMessage(String ACK, String SSOrigin, String SSDest, String recport){
        JSONObject RespMigrationMessage = new JSONObject();
        try {
            
            String sourceAddress = SJSSCDSignalChannelMap.GetLocalSSAddr();
            //to get address, must access Serv registry
            //JSONObject jsInt = SJServiceRegistry.obtainInternalRegistry();
            
            //Enumeration keysJsInt = jsInt.keys();
            
            
            RespMigrationMessage = new JSONObject();
            RespMigrationMessage.put("associatedSS",SSOrigin);
            RespMigrationMessage.put("MsgType", "responseReqServiceMigration");
            RespMigrationMessage.put("destinationSubsystem",SSDest );
            RespMigrationMessage.put("sourceAddress",sourceAddress);
            RespMigrationMessage.put("data", ACK);
            RespMigrationMessage.put("recPort", recport);
            
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        
        return RespMigrationMessage.toString();
    }
    
    public String CreateLinkCreationReqMsg(String SSOrigin, String SSDest, String destAddr, String sourceAddress, String respPort, String trnsPort){
        JSONObject jsMSg = new JSONObject();
        try{
            jsMSg.put("associatedSS",SSOrigin);
            jsMSg.put("MsgType","reqLinkCreation");
            jsMSg.put("destinationSubsystem",SSDest );
            jsMSg.put("destAddr",destAddr);
            jsMSg.put("sourceAddress", sourceAddress);
            jsMSg.put("respPort", respPort);
            jsMSg.put("trnsPort", trnsPort);
        } catch (JSONException jex){
            jex.printStackTrace();
        }
        return jsMSg.toString();
    }
    public String CreateLinkCreationRespMsg(String SSOrigin, String SSDest, String sourceAddress, String ACK){
        JSONObject jsMSg = new JSONObject();
        try{
            jsMSg.put("associatedSS",SSOrigin);
            jsMSg.put("MsgType", "respLinkCreation");
            jsMSg.put("destinationSubsystem",SSDest );
            jsMSg.put("sourceAddress",sourceAddress);
            jsMSg.put("data",ACK);
        } catch (JSONException jex){
            jex.printStackTrace();
        }
        return jsMSg.toString();
    }
    public String CreateLinkCreationACKMsg(String SSOrigin, String SSDest, String sourceAddress, String ACK){
        JSONObject jsMSg = new JSONObject();
        try{
            jsMSg.put("associatedSS",SSOrigin);
            jsMSg.put("MsgType","respLinkCreationFromReq");
            jsMSg.put("destinationSubsystem",SSDest );
            jsMSg.put("sourceAddress",sourceAddress);
            jsMSg.put("data",ACK);
        } catch (JSONException jex){
            jex.printStackTrace();
        }
        return jsMSg.toString();
    }
    
    public String CreateLinkCreationRespMsg(String SSOrigin, String SSDest, String sourceAddress, String ACK, String portNum){
        
        JSONObject jsMSg = new JSONObject();
        
        try{
           
            jsMSg.put("associatedSS",SSOrigin);
            jsMSg.put("MsgType", "respLinkCreation");
            jsMSg.put("destinationSubsystem",SSDest );
            jsMSg.put("sourceAddress",sourceAddress);
            jsMSg.put("data",ACK);
            jsMSg.put("trnsPort", portNum);
            
        } catch (JSONException jex){
            jex.printStackTrace();
        }
        
        return jsMSg.toString();
        
    }
    
    public String CreateChanQueryMsg(String InChanName,String SSOrigin, String SSDest, String sourceAddress){
        
        JSONObject jsMSg = new JSONObject();
        
        try{
           
            jsMSg.put("associatedSS",SSOrigin);
            jsMSg.put("MsgType","chanQuery");
            jsMSg.put("destinationSubsystem",SSDest );
            jsMSg.put("sourceAddress",sourceAddress);
           
            jsMSg.put("inchanName", InChanName);
            
            
        } catch (JSONException jex){
            jex.printStackTrace();
        }
        
        return jsMSg.toString();
        
    }
    
    public String CreateChanQueryRespMsg(String InChanName,String SSOrigin, String SSDest, String sourceAddress, String inchanStat){
        
        JSONObject jsMSg = new JSONObject();
        
        try{
           
            jsMSg.put("associatedSS",SSOrigin);
            jsMSg.put("MsgType","chanQueryResp");
            jsMSg.put("destinationSubsystem",SSDest );
            jsMSg.put("sourceAddress",sourceAddress);
            
            jsMSg.put("inchanName", InChanName);
            jsMSg.put("inchanStat",inchanStat);
            
        } catch (JSONException jex){
            jex.printStackTrace();
        }
        
        return jsMSg.toString();
        
    }
    
}

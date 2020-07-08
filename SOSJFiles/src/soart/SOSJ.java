/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package soart;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.json.me.JSONTokener;
import systemj.common.CDLCBuffer;
import systemj.common.RegAllCDStats;
import systemj.common.RegAllSSAddr;
import systemj.common.SJChannelRequestMessage;
import systemj.common.SJChannelResponseMessage;
import systemj.common.SJMessageConstants;
import systemj.common.SJResponseMessage;
import systemj.common.SJSSCDSignalChannelMap;
import systemj.common.SJServiceRegistry;
import systemj.common.SOAFacility.Support.SOABuffer;

public class SOSJ {
    public static synchronized String CreateChanInvReqMsg(String respSSName, String respCDName, String respChanName, String actionName,long msgID){
        JSONObject jsReq = new JSONObject();
        try{
            jsReq.put("respSSName",respSSName);
            jsReq.put("respCDName",respCDName);
            jsReq.put("respChanName",respChanName);
            jsReq.put("action", actionName);
            jsReq.put("msgID",Long.toString(msgID));
        } catch (JSONException jex){
            jex.printStackTrace();
        }
        return jsReq.toString();
    }
    public static synchronized String CreateChanInvReqMsg(String respSSName, String respCDName, String respChanName, String actionName, long msgID, String val){
        JSONObject jsReq = new JSONObject();
        try{
            jsReq.put("respSSName",respSSName);
            jsReq.put("respCDName",respCDName);
            jsReq.put("respChanName",respChanName);
            jsReq.put("action", actionName);
            jsReq.put("data", val);
            jsReq.put("msgID",Long.toString(msgID));
        } catch (JSONException jex){
            jex.printStackTrace();
        }
        return jsReq.toString();
    }
    
    public static synchronized String CreateChanInvReqMsg(String respSSName, String respCDName, String respChanName, String actionName){
        JSONObject jsReq = new JSONObject();
        try{
            jsReq.put("respSSName",respSSName);
            jsReq.put("respCDName",respCDName);
            jsReq.put("respChanName",respChanName);
            jsReq.put("action", actionName);
            jsReq.put("msgID",Long.toString(System.currentTimeMillis()));
        } catch (JSONException jex){
            jex.printStackTrace();
        }
        return jsReq.toString();
    }
    public static synchronized String CreateChanInvReqMsg(String respSSName, String respCDName, String respChanName, String actionName, String val){
        JSONObject jsReq = new JSONObject();
        try{
            jsReq.put("respSSName",respSSName);
            jsReq.put("respCDName",respCDName);
            jsReq.put("respChanName",respChanName);
            jsReq.put("action", actionName);
            jsReq.put("data", val);
            jsReq.put("msgID",Long.toString(System.currentTimeMillis()));
        } catch (JSONException jex){
            jex.printStackTrace();
        }
        return jsReq.toString();
    }

    public static synchronized long generateMessageID(){
        return System.currentTimeMillis();
    }
    public static synchronized String CreateSigInvocReqMsg(String actionName, long msgID){
        JSONObject jsReq = new JSONObject();
        try {
            jsReq.put("action", actionName);
            jsReq.put("msgID", Long.toString(msgID));
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return jsReq.toString();
    }
    public static synchronized String CreateSigInvocReqMsg(String actionName, long msgID, String data){
        JSONObject jsReq = new JSONObject();
        try {
            jsReq.put("action", actionName);
            jsReq.put("msgID", Long.toString(msgID));
            jsReq.put("data", data);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return jsReq.toString();
    }
    
    public static synchronized String CreateSigInvocReqMsg(String actionName, long msgID, int data){
        JSONObject jsReq = new JSONObject();
        try {
            jsReq.put("action", actionName);
            jsReq.put("msgID", Long.toString(msgID));
            jsReq.put("data", Integer.toString(data));
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return jsReq.toString();
    }
    
    public static synchronized String CreateSigInvocReqMsg(String actionName){
        JSONObject jsReq = new JSONObject();
        try {
            jsReq.put("action", actionName);
            jsReq.put("msgID", Long.toString(System.currentTimeMillis()));
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return jsReq.toString();
    }
    public static synchronized String CreateSigInvocReqMsg(String actionName, String data){
        JSONObject jsReq = new JSONObject();
        try {
            jsReq.put("action", actionName);
            jsReq.put("msgID", Long.toString(System.currentTimeMillis()));
            jsReq.put("data", data);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return jsReq.toString();
    }
    
    public static synchronized String CreateSigInvocReqMsg(String actionName, int data){
        JSONObject jsReq = new JSONObject();
        try {
            jsReq.put("action", actionName);
            jsReq.put("msgID", Long.toString(System.currentTimeMillis()));
            jsReq.put("data", Integer.toString(data));
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return jsReq.toString();
    }
    
    public static synchronized String CreateSigInvocReqMsg(String CDReqName, String SignalRespName, String DestAddr, String DestPort,String actionName, int data){
        JSONObject jsReq = new JSONObject();
        JSONObject LocSSMap = SJSSCDSignalChannelMap.getCurrentSignalChannelMapping();
        String locSSName = SJSSCDSignalChannelMap.getLocalSSName();
        String RequesterAddr = RegAllSSAddr.getSSAddrOfSSName(locSSName);
        try {
            JSONObject jsReqCDMap = LocSSMap.getJSONObject(CDReqName);
            JSONObject jsSigs = jsReqCDMap.getJSONObject("signals");
            JSONObject jsSigsInputs = jsSigs.getJSONObject("inputs");
            JSONObject SigInputConfigs = jsSigsInputs.getJSONObject(SignalRespName);
            String portNum = SigInputConfigs.getString("Port");
            jsReq.put("action", actionName);
            jsReq.put("msgID", Long.toString(System.currentTimeMillis()));
            jsReq.put("data", data);
            jsReq.put("respAddr", RequesterAddr);
            jsReq.put("respPort", portNum);
            jsReq.put("destAddr", DestAddr);
            jsReq.put("destPort", DestPort);
        }  catch (JSONException ex) {
            ex.printStackTrace();
        }
        return jsReq.toString();
    }
    
    public static synchronized String CreateSigInvocReqMsg(String CDReqName, String SignalRespName, String DestAddr, String DestPort,String actionName, String data){
        JSONObject jsReq = new JSONObject();
        JSONObject LocSSMap = SJSSCDSignalChannelMap.getCurrentSignalChannelMapping();
        String locSSName = SJSSCDSignalChannelMap.getLocalSSName();
        String RequesterAddr = RegAllSSAddr.getSSAddrOfSSName(locSSName);
        try {
            JSONObject jsReqCDMap = LocSSMap.getJSONObject(CDReqName);
            JSONObject jsSigs = jsReqCDMap.getJSONObject("signals");
            JSONObject jsSigsInputs = jsSigs.getJSONObject("inputs");
            JSONObject SigInputConfigs = jsSigsInputs.getJSONObject(SignalRespName);
            String portNum = SigInputConfigs.getString("Port");
            jsReq.put("action", actionName);
            jsReq.put("msgID", Long.toString(System.currentTimeMillis()));
            jsReq.put("data", data);
            jsReq.put("respAddr", RequesterAddr);
            jsReq.put("respPort", portNum);
            jsReq.put("destAddr", DestAddr);
            jsReq.put("destPort", DestPort);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return jsReq.toString();
    }
    
     public static synchronized String CreateSigInvocReqMsg(String CDReqName, String SignalRespName, String DestAddr, String DestPort,String actionName){
        JSONObject jsReq = new JSONObject();
        JSONObject LocSSMap = SJSSCDSignalChannelMap.getCurrentSignalChannelMapping();
        String locSSName = SJSSCDSignalChannelMap.getLocalSSName();
        String RequesterAddr = RegAllSSAddr.getSSAddrOfSSName(locSSName);
        try {
            JSONObject jsReqCDMap = LocSSMap.getJSONObject(CDReqName);
            JSONObject jsSigs = jsReqCDMap.getJSONObject("signals");
            JSONObject jsSigsInputs = jsSigs.getJSONObject("inputs");
            JSONObject SigInputConfigs = jsSigsInputs.getJSONObject(SignalRespName);
            String portNum = SigInputConfigs.getString("Port");
            jsReq.put("action", actionName);
            jsReq.put("msgID", Long.toString(System.currentTimeMillis()));
            jsReq.put("respAddr", RequesterAddr);
            jsReq.put("respPort", portNum);
            jsReq.put("destAddr", DestAddr);
            jsReq.put("destPort", DestPort);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return jsReq.toString();
    }
    
    public static synchronized String CreateSigInvocRespMsg(String ReqMsg, int data){
        JSONObject jsReqMsg = new JSONObject();
        JSONObject jsRespMsg = new JSONObject();
        try {
            jsReqMsg = new JSONObject(new JSONTokener(ReqMsg));
            String msgID = jsReqMsg.getString("msgID");
            if(jsReqMsg.has("respAddr")){
               String respAddr = jsReqMsg.getString("respAddr");
               String respPort = jsReqMsg.getString("respPort");
               jsRespMsg.put("destAddr", respAddr);
               jsRespMsg.put("destPort", respPort);
            }
            
            jsRespMsg.put("msgID", msgID);
            jsRespMsg.put("status", "ACK");
            
            jsRespMsg.put("data", Integer.toString(data));
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return jsRespMsg.toString();
    }
    
    public static synchronized String CreateSigInvocRespMsg(String ReqMsg, String data){
        JSONObject jsReqMsg = new JSONObject();
        JSONObject jsRespMsg = new JSONObject();
        try {
            jsReqMsg = new JSONObject(new JSONTokener(ReqMsg));
            String msgID = jsReqMsg.getString("msgID");
            if(jsReqMsg.has("respAddr")){
                 String respAddr = jsReqMsg.getString("respAddr");
                 String respPort = jsReqMsg.getString("respPort");
                  jsRespMsg.put("destAddr", respAddr);
                  jsRespMsg.put("destPort", respPort);
            }
            jsRespMsg.put("msgID", msgID);
            jsRespMsg.put("status", "ACK");
            jsRespMsg.put("data", data);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return jsRespMsg.toString();
    }
   
    public static synchronized String CreateChanInvRespMsg(String ReqMsg, String val){
        JSONObject jsResp = new JSONObject();
        try {
            JSONObject jsReqMsg = new JSONObject(new JSONTokener(ReqMsg));
            String msgID = jsReqMsg.getString("msgID");
            jsResp.put("data", val);
            jsResp.put("status", "ACK");
            jsResp.put("msgID", msgID);
        } catch (JSONException ex) {
           ex.printStackTrace();
        }
        return jsResp.toString();
    }
    public static synchronized String CreateChanInvRespMsg(String ReqMsg){
        JSONObject jsResp = new JSONObject();
        try {
            JSONObject jsReqMsg = new JSONObject(new JSONTokener(ReqMsg));
            String msgID = jsReqMsg.getString("msgID");
             jsResp.put("status", "ACK");
            jsResp.put("msgID", msgID);
           
        } catch (JSONException ex) {
           ex.printStackTrace();
        }
        return jsResp.toString();
    }
    /*
    public static synchronized String CreateChanInvRespMsg(String ReqMsg){
        String res = "{}";
        try {
            JSONObject jsReqMsg = new JSONObject(new JSONTokener(ReqMsg));
            String respSSName = jsReqMsg.get("respSSName").toString();
            String respChanName = jsReqMsg.get("respChanName").toString();
            String respCDName = jsReqMsg.get("respCDName").toString();
            String srcSSName = jsReqMsg.get("destSSName").toString();
            String srcCDName = jsReqMsg.get("destCDName").toString();
            String srcChanName = jsReqMsg.get("destChanName").toString();
            SJChannelResponseMessage sjrespmsg = new SJChannelResponseMessage();
            sjrespmsg.SetDestCDName(respCDName);
            sjrespmsg.SetDestSSName(respSSName);
            sjrespmsg.SetDestChanName(respChanName);
            sjrespmsg.SetSrcCDName(srcCDName);
            sjrespmsg.SetSrcSSName(srcSSName);
            sjrespmsg.SetSrcChanName(srcChanName);
            return sjrespmsg.createResponseMessage();
        } catch (JSONException ex) {
           ex.printStackTrace();
        }
        return res;
    }
    */
    public static synchronized String GetValRspMsg(String RespMsg){
        String res = "";
        try {
            JSONObject jsRespMsg = new JSONObject(new JSONTokener(RespMsg));
            res = jsRespMsg.getString("payload");
        } catch (JSONException ex) {
           ex.printStackTrace();
        }
        return res;
    }
    public static synchronized long GetNotif(){
        return SOABuffer.getNotif();
    }
    public static synchronized boolean ConfigureInvocChannel(String CDSenderName, String ChanSenderName, String CDReceiverName, String ChanReceiverName){
        boolean stat = CDLCBuffer.AddInvokeServChanReconfig(CDSenderName,ChanSenderName, "output", CDReceiverName,ChanReceiverName);
        return stat;
    }
    public static synchronized boolean ConfigureInvocChannel(String CDSenderName, String ChanSenderName, String RecReqMsg){
        boolean stat = false;
        try {
            JSONObject jsReqMsg = new JSONObject(new JSONTokener(RecReqMsg));
            String CDReceiverName = jsReqMsg.getString("respCDName");
            String ChanReceiverName = jsReqMsg.getString("respChanName");
            String respSSName = jsReqMsg.getString("respSSName");
            
            stat = CDLCBuffer.AddInvokeServChanReconfig(respSSName,CDSenderName,ChanSenderName, "output", CDReceiverName,ChanReceiverName);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return stat;
    }
    public static synchronized boolean GetInvocChannelReconfigStat(String PartChanCDName, String PartChanName){
        //boolean stat = CDLCBuffer.GetReconfigInvokeServStatChanBuffer(PartChanCDName, PartChanName);
        boolean stat = CDLCBuffer.GetInvStatChanBuffer(PartChanCDName, PartChanName);
        return stat;
    }
    public static synchronized String GetData(String jsString){
        String actionData=null;
        try
        {
            JSONObject js = new JSONObject(new JSONTokener(jsString));
            if (js.has("data")){
                actionData = js.getString("data");
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        if (actionData==null){
            return "";
        } else {
            return actionData;
        }
    }
    public static synchronized String GetAction(String jsString){
        String action=null;
        try
        {
            JSONObject js = new JSONObject(new JSONTokener(jsString));
                if (js.has("action")){
                        action = js.getString("action");
                }
        } catch (JSONException ex) {
           ex.printStackTrace();
        }
        if (action==null){
            return "";
        } else {
            return action;
        }   
    }
    public static synchronized void StoreServiceToRegistry(String regJSON){
        try {
            JSONObject serv = new JSONObject(new JSONTokener(regJSON));
            SJServiceRegistry.SaveDiscoveredServicesNoP2P(serv);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }
    public static synchronized void SetCDServVisibility(String CDName,boolean stat){
       if(stat){
           SOABuffer.setAdvVisib(CDName, "visible");
       } else {
           SOABuffer.setAdvVisib(CDName, "invisible");
       }
       SOABuffer.SetRegNotifySS(true);
    }
    public static synchronized String GetLocalRegistry(){
        String reg="{}";
        try {
             reg = SJServiceRegistry.obtainInternalRegistry().toString();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return reg;
    }
    
    public static synchronized String GetStoredServiceDescription(){
        String reg="{}";
        try {
             reg = SJServiceRegistry.obtainCurrentRegistry().toString();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return reg;
    }
    /*
    public static synchronized String CreateResponseWithData(String msg, String data){
        try {
            JSONObject js = new JSONObject(new JSONTokener(msg));
            JSONObject jsPyld = new JSONObject();
                    SJResponseMessage sjResp = new SJResponseMessage(SJMessageConstants.MessageType.ACK);
                    sjResp.setMessageID(js.getInt("msgID"));
                    sjResp.setSourceAddress(js.getString("srcAddr"));
                    sjResp.setDestinationPort(Integer.parseInt(js.getString("respPort")));
                    jsPyld.put("data", data);
                    sjResp.setJSONPayload(jsPyld);
                    msg = sjResp.createResponseMessage();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return msg;
    }
    public static void TriggerRefreshAdv(){
        SOABuffer.SetAdvTransmissionRequest(true);
    }
    */
    public static String GetAllCDMacroStates(){
        String res = "";
        try {
            res = RegAllCDStats.getAllCDStats().toPrettyPrintedString(2, 0);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return res;
    }
    public static synchronized String CreateSigInvocRespACKMsg(String ReqMsg){
        JSONObject jsReqMsg = new JSONObject();
        JSONObject jsRespMsg = new JSONObject();
        try {
            jsReqMsg = new JSONObject(new JSONTokener(ReqMsg));
            String msgID = jsReqMsg.getString("msgID");
            jsRespMsg.put("msgID", msgID);
            jsRespMsg.put("status", "ACK");
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return jsRespMsg.toString();
    }
    public static synchronized String CreateSigInvocRespACKMsg(String ReqMsg, String data){
        JSONObject jsReqMsg = new JSONObject();
        JSONObject jsRespMsg = new JSONObject();
        try {
            jsReqMsg = new JSONObject(new JSONTokener(ReqMsg));
            String msgID = jsReqMsg.getString("msgID");
            jsRespMsg.put("msgID", msgID);
            jsRespMsg.put("status", "ACK");
            jsRespMsg.put("data", data);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return jsRespMsg.toString();
    }
    public static synchronized String CreateSigInvocRespACKMsg(String ReqMsg, int data){
        JSONObject jsReqMsg = new JSONObject();
        JSONObject jsRespMsg = new JSONObject();
        try {
            jsReqMsg = new JSONObject(new JSONTokener(ReqMsg));
            String msgID = jsReqMsg.getString("msgID");
            jsRespMsg.put("msgID", msgID);
            jsRespMsg.put("status", "ACK");
            jsRespMsg.put("data", Integer.toString(data));
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return jsRespMsg.toString();
    }
    public static synchronized long GetSignalInvocMsgID(String InvMsg){
            long msgID=0;
        try {
            JSONObject jsMsg = new JSONObject(new JSONTokener(InvMsg));
            msgID = Long.parseLong(jsMsg.getString("msgID"));
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return msgID;
    }
    public static synchronized String GetRespStatus(String jsString){
        String respStatus="";
        try
        {
            JSONObject js = new JSONObject(new JSONTokener(jsString));
            if (js.has("status")){
                respStatus = js.getString("status");
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return respStatus;
    }
    
    public static synchronized void addService(String servDesc){
        try {
            JSONObject jsServDesc = new JSONObject(new JSONTokener(servDesc));
            SJServiceRegistry.AddInternalRegWithServ(jsServDesc);
            SOABuffer.SetAdvTransmissionRequest(true); //trigger transmission of Adv to update service registry
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }
}
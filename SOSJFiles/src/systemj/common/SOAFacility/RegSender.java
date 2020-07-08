/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemj.common.SOAFacility;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;

import java.io.ObjectOutputStream;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import java.net.MulticastSocket;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.json.me.JSONTokener;

import systemj.common.InflaterDeflater;
import systemj.common.RegAllCDStats;
import systemj.common.RegAllSSAddr;
import systemj.common.SJSOAMessage;
import systemj.common.SJSSCDSignalChannelMap;
import systemj.common.SJServiceRegistry;
import systemj.common.SOAFacility.Support.NetworkConnCheckSimple;
import systemj.common.SOAFacility.Support.SOABuffer;

public class RegSender implements Runnable {
    @Override
    public void run() {
        SJSOAMessage sjdisc = new SJSOAMessage();
        String GtwyAddr = SOABuffer.getGatewayAddr();
        String SubnetMask = SOABuffer.getSubnetMaskAddr();
        String broadcastAddr = getBroadcastAddress(GtwyAddr,SubnetMask);
        NetworkConnCheckSimple netcheck = new NetworkConnCheckSimple();
        long lastReqAdvTransmittedTime = System.currentTimeMillis();
        long currentTime = 0;
         long stTime = System.currentTimeMillis();
                            long endTime = System.currentTimeMillis();
        
        while (true) {
                String connStat = netcheck.CheckNetworkConn(SOABuffer.getGatewayAddr(), 1400);
                    if (connStat.equalsIgnoreCase("Connected")){
                            Vector discMsgs = SOABuffer.getAllDiscMsgFromBuffer();
                            for (int i=0;i<discMsgs.size();i++){
                                JSONObject discMsg = (JSONObject)discMsgs.get(i);
                                try {
                                    JSONObject jsServReg = SJServiceRegistry.obtainCurrentRegistry();
                                    String discReplyMsg = sjdisc.ConstructDiscoveryReplyMessage(discMsg, jsServReg);
                                    int respPort = Integer.parseInt(discMsg.getString("respPort"));
                                    if(!discReplyMsg.equalsIgnoreCase("{}")){
                                        SendDiscReplyMsg(discReplyMsg, respPort);
                                    }
                                } catch (JSONException ex) {
                                   ex.printStackTrace();
                                }
                            }
                            //adv transmission from req adv
                            //send registry adv
                            /*
                            Vector allContentReqAdvBuffer = SOABuffer.getAllContentReqAdvBuffer();
                            if(allContentReqAdvBuffer.size()>0){
                                for (int i=0;i<allContentReqAdvBuffer.size();i++){
                                     JSONObject jsReqAdvMsg = (JSONObject) allContentReqAdvBuffer.get(i);
                                            try {
                                                JSONObject generatedRespAdvMsgJSON = ProcessMessageInJSON(jsReqAdvMsg.toString(), SJServiceRegistry.obtainInternalRegistry().toString()); 
                                                String destAddr = jsReqAdvMsg.getString("sourceAddress");
                                                if(destAddr.equalsIgnoreCase(SOABuffer.getSOSJRegAddr())){
                                                    SendAdvMsg("224.0.0.100", generatedRespAdvMsgJSON.toString());
                                                } else {
                                                    SendAdvMsg(destAddr, generatedRespAdvMsgJSON.toString());
                                                }
                                            } catch (JSONException ex) {
                                                System.out.println("cannot find destination Address in ProcessMessageInJSON");
                                            } 
                                }
                            }
                            */
                                //regular adv
                            boolean startRegAdv = CheckOwnBeaconExp();
                            if (startRegAdv){
                                String beaconPeriod = SOABuffer.getSOSJRegBeaconPeriodTime();
                                String regID = SOABuffer.getSOSJRegID();
                                String regAddr = SOABuffer.getSOSJRegAddr();
                                String AdvMsg = sjdisc.ConstructRegistryBeaconMessage(beaconPeriod, regID, regAddr);
                                SendBeaconMsg(broadcastAddr, AdvMsg);
                                SOABuffer.RecordAdvertisementTimeStamp();
                            }
                            //end regular adv
                            //notify all SS with broadcast
                            boolean notify = SOABuffer.GetRegNotify();
                            if(notify){
                                String regID = SOABuffer.getSOSJRegID();
                                String strTime = Long.toString(System.currentTimeMillis());
                                String NotifyMsg = sjdisc.ConstructRegNotifyMessage(regID,strTime);
                                SendNotifMsg(broadcastAddr, NotifyMsg);
                                if(SOABuffer.GetRegNotify()){
                                   SOABuffer.SetRegNotifySS(false);
                                }
                            }
                     // end adv transmission responding req adv
                    //req adv check and transmission
                            
                            Hashtable expiredServAddrs = CheckAlmostExpiredAdv();
                            if (expiredServAddrs.size()>0){
                                    Enumeration keysExpServAddrs = expiredServAddrs.keys();
                                    //currentTime = System.currentTimeMillis();
                                   // if(currentTime-lastReqAdvTransmittedTime>300){
                                        while(keysExpServAddrs.hasMoreElements()){
                                            String index = keysExpServAddrs.nextElement().toString();
                                            String destSSName = (String) expiredServAddrs.get(index);
                                            boolean IsReqAdvED = SOABuffer.IsSSNameReqForAdvED(destSSName);
                                            if(!IsReqAdvED){
                                                
                                                String destAddr = RegAllSSAddr.getSSAddrOfSSName(destSSName);
                                                String regID = SOABuffer.getSOSJRegID();
                                                String regAddr = SOABuffer.getSOSJRegAddr();
                                                SOABuffer.putCurrReqAdvED(destSSName);
                                                Thread threqadv = new Thread(new ReqAdvTransceive(destSSName, regID, regAddr,destAddr));
                                                threqadv.start();
                                                
                                            }
                                            
                                            //String ReqAdvMsg = sjdisc.ConstructReqAdvertisementMessage(SOABuffer.getSOSJRegID(), SOABuffer.getSOSJRegAddr(), destSSName);
                                            //if(destAddr.equalsIgnoreCase(SOABuffer.getSOSJRegAddr())){
                                           //      SendReqAdvOrNotifMsg("224.0.0.100",ReqAdvMsg);
                                           // } else 
                                            //{
                                                 //SendReqAdvOrNotifMsg(destAddr,ReqAdvMsg);
                                           // }
                                        }
                                        //lastReqAdvTransmittedTime = System.currentTimeMillis();
                                   // }
                            }
                           
                      }
          }
    }
    /*
    private JSONObject ProcessMessageInJSON(String SJMessage,String OfferedServices)
    {
        JSONObject serviceList = new JSONObject();
        JSONObject processedSJMessage = new JSONObject();
        SJSOAMessage sjdisc = new SJSOAMessage();
        try {
            JSONObject jsAllIntServ = new JSONObject(new JSONTokener(OfferedServices));
            JSONObject jsMsg = new JSONObject(new JSONTokener(SJMessage));
            if (jsMsg.getString("MsgType").equalsIgnoreCase("Discovery")){ 
                Enumeration keysjsAllIntServ = jsAllIntServ.keys();
                while (keysjsAllIntServ.hasMoreElements()){
                    Object keyAllIntServ = keysjsAllIntServ.nextElement();
                    JSONObject jsIndivIntServ = jsAllIntServ.getJSONObject(keyAllIntServ.toString());
                    serviceList.put(jsIndivIntServ.getString("serviceName"),jsIndivIntServ);
                }
                if (!serviceList.toString().equalsIgnoreCase("{}")){
                    processedSJMessage = new JSONObject(new JSONTokener(SJMessage));
                    processedSJMessage.put("serviceList",serviceList);
                    processedSJMessage.put("associatedSS",jsMsg.getString("associatedSS"));
                    processedSJMessage.put("CDStats", RegAllCDStats.getAllCDStats());
                    processedSJMessage.put("SSAddrs", RegAllSSAddr.getAllSSAddr());
                    processedSJMessage.put("MsgType","DiscoveryReply");
                } else {
                    processedSJMessage = new JSONObject();   
                }
            }
            else if (jsMsg.getString("MsgType").equalsIgnoreCase("regRequestAdvertise")){
                processedSJMessage = sjdisc.ConstructRegResponseReAdvertisementMessageInJSON(SOABuffer.getSOSJRegBeaconPeriodTime(), SOABuffer.getSOSJRegID(), SOABuffer.getSOSJRegAddr());
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        return processedSJMessage;
    }
    */
    /*
    private String ProcessMessage(JSONObject jsMsg,JSONObject jsAllServ)
    {
        JSONObject serviceList = new JSONObject();
        JSONObject processedSJMessage = new JSONObject();
        
        int i = 1;
        
        System.out.println("RegMessageSenderThread, responding to Msg: " +jsMsg);
        
        try {
           
            if (jsMsg.getString("MsgType").equalsIgnoreCase("Discovery")){ //if handling discovery request message
                
                Enumeration keysSSName = jsAllServ.keys();
                
                while (keysSSName.hasMoreElements()){
                    
                    String keySSName = keysSSName.nextElement().toString();
                    
                    JSONObject jsAllSSServ = jsAllServ.getJSONObject(keySSName);
                    
                            serviceList.put(keySSName,jsAllSSServ);
                    //    }
                    //}  
                }
             
                    if(jsMsg.getString("sourceAddress").equals(SOABuffer.getSOSJRegAddr())){
                        processedSJMessage.put("destinationAddress","224.0.0.100");
                        processedSJMessage.put("sourceAddress","224.0.0.100");
                    } else {
                        processedSJMessage.put("destinationAddress",jsMsg.getString("sourceAddress"));
                        processedSJMessage.put("sourceAddress",SOABuffer.getSOSJRegAddr());
                    }
                  
                    processedSJMessage.put("serviceList",serviceList);
                    //processedSJMessage.put("CDStats", CDLCBuffer.GetAllCDMacroState());
                    processedSJMessage.put("CDStats", RegAllCDStats.getAllCDStats());
                    processedSJMessage.put("SSAddrs", RegAllSSAddr.getAllSSAddr());
                    
                    processedSJMessage.put("MsgType","DiscoveryReply");
                    
                    if(jsMsg.has("associatedSS")){
                        processedSJMessage.put("destSS",jsMsg.getString("associatedSS"));
                    }
                    
                   
               // } 
                
            }
           
           
            
        } catch (JSONException ex) {
            
            ex.printStackTrace();
            System.exit(1);
        }
        
        return processedSJMessage.toString();
 
    }
    */
    
    private void SendBeaconMsg(String ipAddr, String message){
        try
                                   {
                                       InetAddress ipAddress = InetAddress.getByName(ipAddr);
                                       MulticastSocket s = new MulticastSocket(1770);
                                       byte[] msg = new byte[65507];
                                       ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                                       ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(byteStream));
                                       out.writeObject(message);
                                       out.flush();
                                       msg = byteStream.toByteArray();
                                       out.close();
                                       msg = InflaterDeflater.compress(msg);
                                       DatagramPacket hi = new DatagramPacket(msg, msg.length, ipAddress, 1770);
                                       s.setLoopbackMode(true);
                                       s.send(hi);
                                       s.close();
                               }
                               catch (java.net.SocketTimeoutException e)
                               {
                                       e.printStackTrace();
                               }
                               catch (Exception e)
                               {
                                       e.printStackTrace();
                               }
        
    }
    
    private boolean SendNotifMsg(String ipAddr, String message){
        try
                                   {
                                       InetAddress ipAddress = InetAddress.getByName(ipAddr);
                                       byte[] msg = new byte[65507];
                                       MulticastSocket s = new MulticastSocket(1770);
                                       ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                                       ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(byteStream));
                                       out.writeObject(message);
                                       out.flush();
                                       msg = byteStream.toByteArray();
                                       out.close();
                                       msg = InflaterDeflater.compress(msg);
                                       DatagramPacket hi = new DatagramPacket(msg, msg.length, ipAddress, 1770);
                                       s.setLoopbackMode(true);
                                       s.send(hi);
                                       s.close();
                                       return true;
                               }
                               catch (java.net.SocketTimeoutException e)
                               {
                                       return false;
                               }
                               catch (Exception e)
                               {
                                       e.printStackTrace();
                                       return false;
                               }
    }
    
    private void SendDiscReplyMsg(String message, int respPort){
        JSONObject js2 = new JSONObject();
        String Addr=null;
        try {
            js2 = new JSONObject(new JSONTokener(message));
            Addr = js2.getString("destinationAddress");
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        try
                                {
                                    byte[] msg = new byte[65507];
                                    byte[] compmsg = new byte[65507];
                                    InetAddress ipAddress = InetAddress.getByName(Addr);
                                    DatagramSocket s = new DatagramSocket(respPort);
                                    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                                    ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(byteStream));
                                    out.writeObject(message);
                                    out.flush();
                                    msg = byteStream.toByteArray();
                                    out.close();
                                    compmsg = InflaterDeflater.compress(msg);
                                    DatagramPacket hi = new DatagramPacket(compmsg, compmsg.length, ipAddress, respPort);
                                    s.send(hi);
                                    s.close();
                            }
                            catch (Exception e)
                            {
                                    System.out.println("Problem when connecting to ip: " +Addr);
                                    e.printStackTrace();
                            }
    }
   
    private Hashtable CheckAlmostExpiredAdv(){
                        Hashtable answer = SJServiceRegistry.checkServiceExpiryForAdvertiseRequest();
                        if (answer.isEmpty()){
                            return new Hashtable();
                        } else {
                            return answer;
                        }
    }
    private boolean CheckOwnBeaconExp(){
        boolean stat;
        long time = System.currentTimeMillis()-SOABuffer.getRecordedAdvertisementTimeStamp();
        if (time>=(Long.parseLong(SOABuffer.getSOSJRegBeaconPeriodTime()))/3){
                    stat=true;
        } else {
                   stat=false;
         }
            return stat;
    }
    
    private String getBroadcastAddress(String GatewayAddr, String SubnetAddr){
       String[] gtwyaddrSplitted = GatewayAddr.split("\\.");
        String[] subnetmaskSplitted = SubnetAddr.split("\\.");
        String[] broadcastaddrString = new String[4];
        String [] flippedsubnetmaskString = new String[4];
        int[] flippedsubnetmasInt = new int[4];
        String broadcastAddr="";
        for(int i=0;i<gtwyaddrSplitted.length;i++){
            int[] subnetaddrint = new int[gtwyaddrSplitted.length];
            int[] broadcastaddrint = new int[subnetmaskSplitted.length];
            int[] gtwyint = new int[gtwyaddrSplitted.length];
            int[] sbnetmaskint = new int[subnetmaskSplitted.length];
            gtwyint[i] = Integer.parseInt(gtwyaddrSplitted[i]);
            sbnetmaskint[i]= Integer.parseInt(subnetmaskSplitted[i]);
            subnetmaskSplitted[i] = String.format("%8s", Integer.toString(sbnetmaskint[i], 2)).replace(' ', '0');
            subnetaddrint[i] = gtwyint[i] & sbnetmaskint[i];
            flippedsubnetmaskString[i] = subnetmaskSplitted[i].replaceAll("0", "x").replaceAll("1", "0").replaceAll("x", "1");
            flippedsubnetmasInt[i] = Integer.parseInt(flippedsubnetmaskString[i],2);
            //broadcastaddrint[i] = subnetaddrint[i] | ~sbnetmaskint[i];
            broadcastaddrint[i] = subnetaddrint[i] | flippedsubnetmasInt[i];
            broadcastaddrString[i] = Integer.toString(broadcastaddrint[i]);
        }
        broadcastAddr = broadcastaddrString[0]+"."+broadcastaddrString[1]+"."+broadcastaddrString[2]+"."+broadcastaddrString[3];
        return broadcastAddr;
    }
    
}  
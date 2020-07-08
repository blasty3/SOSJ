/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package systemj.common.SOAFacility;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.json.me.JSONTokener;
import systemj.common.CDLCBuffer;
import systemj.common.InflaterDeflater;
import systemj.common.SJSOAMessage;
import systemj.common.SJSSCDSignalChannelMap;
import systemj.common.SJServiceRegistry;
import systemj.common.SOAFacility.Support.NetworkConnCheckSimple;
import systemj.common.SOAFacility.Support.SOABuffer;

/**
 *
 * @author Udayanto
 */
public class MigrationAndLinkReqMsgThread implements Runnable{
    ServerSocket ssLink = null;
    Socket socketSend = null;
    ServerSocket ssMig = null;
    Socket socketSendMig = null;
    @Override
    public void run() {
        //NetworkConnCheckSimple netcheck = new NetworkConnCheckSimple();
        System.out.println("MigrationAndLinkLocalReqMessageReceiver thread started");
        while(true){
               // String connStat = netcheck.CheckNetworkConn(SOABuffer.getGatewayAddr(), 2500);
                //if (connStat.equalsIgnoreCase("Connected")){
                    JSONObject jsMsg = ReceiveMigOrLinkMsg();
                    if (!jsMsg.isEmpty()) {
                            ResponseMigOrLinkReq(jsMsg);
                    }
                //}
        }
    }
    private JSONObject ReceiveMigOrLinkMsg(){
        JSONObject js = new JSONObject();
        MulticastSocket socket = null;
                            try{
                                    byte data[];
                                    byte packet[] = new byte[65507];
                                    socket = new MulticastSocket(1078);
                                    socket.joinGroup(InetAddress.getByName("224.0.0.100"));
                                        DatagramPacket pack;
                                        while (true){
                                            pack = new DatagramPacket(packet, packet.length);
                                            socket.receive(pack);
                                                data = new byte[pack.getLength()];
                                                System.arraycopy(packet, 0, data, 0, pack.getLength());
                                                if(data.length > 0)
                                                {
                                                        if(((int)data[0] == -84) && ((int)data[1] == -19))
                                                        {
                                                                        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
                                                                        Object mybuffer = ois.readObject(); 
                                                                              js = new JSONObject(new JSONTokener(mybuffer.toString().trim()));
                                                                              //System.out.println("MigAndLinkReqMsgReceiving from :" +pack.getAddress()+", msg: " +mybuffer.toString());
                                                                              if(js.getString("destinationSubsystem").equals(SJSSCDSignalChannelMap.getLocalSSName())){
                                                                                  
                                                                                  break;
                                                                              } else {
                                                                                  if(js.getString("destAddr").equalsIgnoreCase(SJSSCDSignalChannelMap.GetLocalSSAddr())){
                                                                                      if(!pack.getAddress().getHostAddress().equalsIgnoreCase(SJSSCDSignalChannelMap.GetLocalSSAddr())){
                                                                                          BouncePacket(mybuffer.toString(),1078);
                                                                                      }
                                                                                  }
                                                                              }
                                                        }
                                                }
                                         }
                                        socket.close();
                        }
                        catch (Exception e)
                        {
                                e.printStackTrace();
                                socket.close();
                        }
                        return js;
    }
    private void BouncePacket(String message, int port){
        MulticastSocket socket = null;
        int infoDebug=0;
        try{
            socket = new MulticastSocket(port);
            byte[] msg = new byte[65507];
            InetAddress ipAddress = InetAddress.getByName("224.0.0.100");
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                 ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(byteStream));
                   out.writeObject(message); 
                    out.flush();
                    msg = byteStream.toByteArray();
                   out.close();
                   DatagramPacket hi = new DatagramPacket(msg, msg.length, ipAddress, port);
                  if (infoDebug ==1 ) System.out.println("Sending data...");
                   socket.send(hi);
                  //System.out.println("RegRemoteMessageReceiverThread, bouncing msg to dest (local) SS, msg sent: " +message);
                   if (infoDebug ==1 ) System.out.println("data has been sent!");
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
    private void ResponseMigOrLinkReq(JSONObject reqMsg){
        String locSSName = SJSSCDSignalChannelMap.getLocalSSName();
        String locSSAddr = SJSSCDSignalChannelMap.GetLocalSSAddr();
        SJSOAMessage sjdisc = new SJSOAMessage();
        try {
            String msgType = reqMsg.getString("MsgType");
            if(msgType.equals("reqLinkCreation")){
                int respPort = Integer.parseInt(reqMsg.getString("respPort"));
                int trnsPort = Integer.parseInt(reqMsg.getString("trnsPort"));
                //String destAddress="";
                //if(reqMsg.getString("sourceAddress").equalsIgnoreCase(locSSAddr)){
             //    destAddress = "224.0.0.100";
                //} else {
                 String destAddress = reqMsg.getString("sourceAddress");
               // }
                                    String destSS = reqMsg.getString("associatedSS");
                                    if(CDLCBuffer.GetLinkCreationBusyFlag()){
                                        String message = sjdisc.CreateLinkCreationRespMsg(locSSName, destSS, destAddress, "BUSY");
                                        RespLink(destAddress, message, respPort);
                                    } else {
                                            int recLinkPortNum = getAvailableReceivingLinkPort();
                                            BindTransmitter(destAddress, trnsPort);
                                                String message = sjdisc.CreateLinkCreationRespMsg(locSSName, destSS, destAddress, "OK",Integer.toString(recLinkPortNum));
                                                RespLink(destAddress, message, respPort);
                                                CDLCBuffer.SetLinkCreationBusyFlag(true);
                                                boolean IsLocal = false;
                                                if(destAddress.equals(locSSAddr)){
                                                    IsLocal = true;
                                                }
                                                 Thread linkcrRecTh = new Thread(new LinkCreationReceiverThread(socketSend,ssLink,destAddress,IsLocal));
                                                 linkcrRecTh.start();
                                    }
            } else if (msgType.equals("requestMigration")){
                //String destAddress;
               // if(reqMsg.getString("sourceAddress").equalsIgnoreCase(locSSAddr)){
                 //String destAddress = "224.0.0.100";
               // } else {
                   String destAddress = reqMsg.getString("sourceAddress");
               // }
                if(CDLCBuffer.getMigrationBusyFlag()){
                                    int recvPort = Integer.parseInt(reqMsg.getString("rcvPort"));
                                    String destSS = reqMsg.getString("associatedSS");
                                    String message = sjdisc.ConstructResponseMigrationMessage("BUSY", locSSName, destSS);
                                    RespMigration(destAddress, recvPort,message);
                            } else {
                                int recvPort = Integer.parseInt(reqMsg.getString("rcvPort"));
                                int recMigPort= getAvailableReceivingMigPort();
                                String destSS = reqMsg.getString("associatedSS");
                                String sourceDestSS = reqMsg.getString("destinationSubsystem");
                                int partnerPort = Integer.parseInt(reqMsg.getString("recPort"));
                                String migType = reqMsg.getString("migType");
                                if(sourceDestSS.equals(locSSName)){
                                    String message = sjdisc.ConstructResponseMigrationMessage("OK",locSSName,destSS,Integer.toString(recMigPort));
                                    RespMigration(destAddress, recvPort,message);
                                        Thread migtrnsrec = new Thread(new MigrationTransferRecThread(ssMig,migType,partnerPort));
                                        CDLCBuffer.setMigrationBusyFlag();
                                        migtrnsrec.start();
                                } else {
                                    String message = sjdisc.ConstructResponseMigrationMessage("NOT OK",locSSName, destSS);
                                    RespMigration(destAddress, recvPort,message);
                                }
                        } 
            } 
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
     }     
      
   private void RespMigration(String destAddress, int port, String message){
        int infoDebug=1;
        Socket socketSendRespMig = null;
        try{
                                       //InetAddress ipAddress = InetAddress.getByName(destAddress);
                                       if (infoDebug ==1 ) System.out.println("Responds Migration to" +destAddress+":"+port);
                                       
                                       /*
                                       byte[] msg = new byte[65507];
                                       DatagramSocket s = new DatagramSocket(port);
                                       ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                                       ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(byteStream));
                                       out.writeObject(message);
                                       out.flush();
                                       msg = byteStream.toByteArray();
                                       out.close();
                                       DatagramPacket hi = new DatagramPacket(msg, msg.length, ipAddress, port);
                                       if (infoDebug ==1 ) System.out.println("Sending ResponseMigrationReqMessage: "+message+" to : " +ipAddress);
                                       s.send(hi);
                                       if (infoDebug ==1 )System.out.println("ResponseMigrationReqMessage has been sent!");
                                       s.close();
                                       */
                                       socketSendRespMig = new Socket(destAddress, port);
                                       ObjectOutputStream sOut = new ObjectOutputStream(socketSendRespMig.getOutputStream());
                                       sOut.writeUTF(message);
                                       sOut.flush();
                                       sOut.close();
                                       socketSendRespMig.close();
                               }
                               catch (Exception e)
                               {
                                       System.out.println("Problem when sending to ip: " + destAddress + " port :" +66);
                                       e.printStackTrace();
                               }
    }
    
   private String RespLink(String destAddress, String message, int port){
        int infoDebug=1;
        String status="";
        Socket socketSendRespLink = null;
        try{
                                       //InetAddress ipAddress = InetAddress.getByName(destAddress);
                                       if (infoDebug ==1 ) System.out.println("Responds Link to" +destAddress+":"+port);
                                       /*
                                       byte[] msg = new byte[65507];
                                       DatagramSocket s = new DatagramSocket(port);
                                       ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                                       ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(byteStream));
                                       out.writeObject(message);
                                       out.flush();
                                       msg = byteStream.toByteArray();
                                       out.close();
                                       DatagramPacket hi = new DatagramPacket(msg, msg.length, ipAddress, port);
                                       if (infoDebug ==1 ) System.out.println("Sending ResponseMigrationReqMessage: "+message+" to : " +ipAddress);
                                       s.send(hi);
                                       if (infoDebug ==1 )System.out.println("ResponseMigrationReqMessage has been sent!");
                                       s.close();
                                               */
                                       socketSendRespLink = new Socket(destAddress, port);
                                       ObjectOutputStream sOut = new ObjectOutputStream(socketSendRespLink.getOutputStream());
                                       sOut.writeUTF(message);
                                       sOut.flush();
                                       System.out.println("Sending ResponseMigrationReqMessage: "+message+" to : " +destAddress+":"+port);
                                       //sOut.close();
                                       socketSendRespLink.close();
                                       //sOut.close();
                               }
                               catch (Exception e)
                               {
                                       System.out.println("Problem when sending to ip: " + destAddress + " port :" + port);
                                       e.printStackTrace();
                               }
        return status;
    }
   private int getAvailableReceivingLinkPort() {
           try {
                    ssLink = new ServerSocket(0);
                } catch (Exception e) {
            } 
         return ssLink.getLocalPort();
    }
   private int getAvailableReceivingMigPort() {
           try {
                    ssMig = new ServerSocket(0);
                } catch (Exception e) {
            } 
         return ssMig.getLocalPort();
    }
   private boolean BindTransmitter(String ipAddr, int port){
       try{
           socketSend = new Socket(InetAddress.getByName(ipAddr), port);
           return true;
       } catch (Exception ex){
           return false;
       }
   }
}

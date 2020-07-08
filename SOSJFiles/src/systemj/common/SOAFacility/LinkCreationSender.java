/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package systemj.common.SOAFacility;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.json.me.JSONTokener;
import systemj.bootstrap.ClockDomain;
import systemj.common.CDLCBuffer;
import systemj.common.CDObjectsBuffer;
import systemj.common.IMBuffer;
import systemj.common.Interconnection;
import systemj.common.InterfaceManager;
import systemj.common.RegAllSSAddr;
import systemj.common.SJSOAMessage;
import systemj.common.SJSSCDSignalChannelMap;
import systemj.common.SJServiceRegistry;
import systemj.interfaces.GenericInterface;
import systemj.interfaces.Scheduler;

/**
 *
 * @author Udayanto
 */
public class LinkCreationSender {
    //DatagramSocket s2 = null;
    ServerSocket ss2 = null;
    Socket socketSend = null;
    ServerSocket ss = null;
    ServerSocket ssLink = null;
    public String SendLinkCreationReq(String originSS, String destSS, String destAddr){
        String answer=null;
        SJSOAMessage sjmsg = new SJSOAMessage();
        String intPort = Integer.toString(getAvailablePort());
        String localSSAddr = SJSSCDSignalChannelMap.GetLocalSSAddr();
            int port = getAvailableReceivingPort();
            String recLinkPort = Integer.toString(port);
            String msg = sjmsg.CreateLinkCreationReqMsg(originSS, destSS, destAddr, localSSAddr,intPort,recLinkPort);
            JSONObject jsAnswer = TransceiveIsLinkCreationFree(destAddr, msg);
            try{
                if(jsAnswer.isEmpty()){
                   answer = "NOT OK";
                   ss.close();
                } else {
                   answer = jsAnswer.getString("data");
                   if(answer.equalsIgnoreCase("BUSY")){
                       ss.close();
                   } else if(answer.equalsIgnoreCase("OK")){
                      String strTrnsPort = jsAnswer.getString("trnsPort");
                      int trnsPort = Integer.parseInt(strTrnsPort);
                      socketSend = new Socket(destAddr, trnsPort);
                   }
                }
            } catch (Exception jex){
                jex.printStackTrace();
            }
        return answer;
    }
    private JSONObject TransceiveIsLinkCreationFree (String destAddress, String reqMigMsg){
        JSONObject jsAnswer = new JSONObject();
                    try{
                        InetAddress ipAddress = null;
                       //if(destAddress.equals(SJSSCDSignalChannelMap.GetLocalSSAddr())){
                       //    ipAddress = InetAddress.getByName("224.0.0.100");
                       // } else {
                            ipAddress = InetAddress.getByName(destAddress);
                       // }
                        byte[] msg = new byte[65507];
                        //byte[] packet = new byte[65507];
                        MulticastSocket s1 = new MulticastSocket(1078);
                        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                        ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(byteStream));
                        out.writeObject(reqMigMsg);
                        out.flush();
                        msg = byteStream.toByteArray();
                        out.close();
                        DatagramPacket req = new DatagramPacket(msg, msg.length, ipAddress,1078);
                         //System.out.println("Sending Link req to :" +ipAddress);
                        //DatagramPacket resp = new DatagramPacket(packet, packet.length);
                        s1.send(req);
                        //System.out.println("Receiving from :" +ipAddress+":" +ss2.getLocalPort());
                        Socket recLinkResp = ss2.accept();
                        ObjectInputStream sInt = new ObjectInputStream(recLinkResp.getInputStream());
                        String respMsg = sInt.readUTF();
                         jsAnswer = new JSONObject(new JSONTokener(respMsg));
                        /*
                        s2.setSoTimeout(3000);
                        s2.receive(resp);
                        byte[] data;
                        data = new byte[resp.getLength()];
                        System.arraycopy(packet, 0, data, 0, resp.getLength());
                          if(data.length > 0)
                          {
                             if(((int)data[0] == -84) && ((int)data[1] == -19))
                             {
                                     ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
                                     Object mybuffer = ois.readObject();
                                        jsAnswer = new JSONObject(new JSONTokener(mybuffer.toString().trim()));
                                        //answer = js.getString("data");
                                        
                             }
                          }
                        s2.close();
                                
                                 
                        */
                        s1.close();
                        ss2.close();
                    }
                   // catch (java.net.SocketTimeoutException e)
                   // {
		//	e.printStackTrace();
                        //s2.close();
                   // }
                    catch (java.net.BindException bex){
                        bex.printStackTrace();
                        //s2.close();
                    }
                    catch (Exception e)
                    {
			e.printStackTrace();
                        //s2.close();
                    }
                 return jsAnswer;  
    }
    /*
    private JSONObject RespLinkToPartner (String destAddress, String Msg){
        JSONObject jsAnswer = new JSONObject();
                    try{
                        InetAddress ipAddress = null;
                        if(destAddress.equals(SJSSCDSignalChannelMap.GetLocalSSAddr())){
                            ipAddress = InetAddress.getByName("224.0.0.100");
                        } else {
                            ipAddress = InetAddress.getByName(destAddress);
                        }
                        byte[] msg = new byte[65507];
                        MulticastSocket s1 = new MulticastSocket(80);
                        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                        ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(byteStream));
                        out.writeObject(Msg);
                        out.flush();
                        msg = byteStream.toByteArray();
                        out.close();
                        DatagramPacket req = new DatagramPacket(msg, msg.length, ipAddress,80);
                        s1.send(req);
                        s1.close();
                    } catch (java.net.BindException bex){
                        bex.printStackTrace();
                    } catch (Exception e){
			e.printStackTrace();
                    }
                 return jsAnswer;  
    }
    */
    public InterfaceManager ExecuteLinkCreation(String destSS, String destAddr, InterfaceManager im) 
    {
        String localSSName = SJSSCDSignalChannelMap.getLocalSSName();
        ObjectOutputStream sOut;
        try{
            System.out.println("Connected to host " + socketSend.getInetAddress());
            sOut = new ObjectOutputStream(socketSend.getOutputStream());
             // 1. start message
             sOut.writeObject("START");
             sOut.flush();
             System.out.println("Sent START message Link Creation");
             //2. receive SSName from the partner
             Socket socketReceive = ss.accept();
             ObjectInputStream sInt = new ObjectInputStream(socketReceive.getInputStream());
             String SSPartnerName = sInt.readUTF();
              //3. receive partner's all port allocation
              String PartnerPortAlloc = (String)sInt.readObject();
             // System.out.println("Receive PartnerPortAlloc " +PartnerPortAlloc);
              sOut.writeUTF(SJSSCDSignalChannelMap.getLocalSSName());
              sOut.flush();
              //send own port alloc
              int locPort=0;
              JSONObject jsLocalPortAlloc = TCPIPLinkRegistry.GetSSAndPortPair();
              boolean localHasPort = false;
              boolean localHasPartnerPort = false; 
              Enumeration keysPortSSLocal = jsLocalPortAlloc.keys();
               while(keysPortSSLocal.hasMoreElements()){
                  String portNum = keysPortSSLocal.nextElement().toString();
                  String SSname = jsLocalPortAlloc.getString(portNum);
                  if(SSname.equals(localSSName)){
                      localHasPort = true;
                      locPort = Integer.parseInt(portNum);
                  }
                  if(SSname.equals(SSPartnerName)){
                      localHasPartnerPort = true;
                  }
               }
              sOut.writeUTF(jsLocalPortAlloc.toString());
              sOut.flush();
              JSONObject jsRemotePortAlloc = new JSONObject(new JSONTokener(PartnerPortAlloc));
               int partPortNum = 0;
               boolean partHasPort = false; //if the partner has link, is the port available 
               boolean partnerHasLocalPort = false;
               Enumeration keysPortSSPartner = jsRemotePortAlloc.keys();
               while(keysPortSSPartner.hasMoreElements()){
                  String portNum = keysPortSSPartner.nextElement().toString();
                  String SSname = jsRemotePortAlloc.getString(portNum);
                  if(SSname.equals(SSPartnerName)){
                      partPortNum = Integer.parseInt(portNum);
                      partHasPort = true;
                  }
                  if(SSname.equals(localSSName)){
                      partnerHasLocalPort = true;
                  }
               }
               if(!localHasPort){
                   locPort = getAvailableReceivingLinkPort();
                   Interconnection ic = im.getInterconnection();
                   Interconnection.Link linko = new Interconnection.Link();
                       String args = destAddr+":"+locPort;
                           GenericInterface gct = (GenericInterface)Class.forName("systemj.desktop.TCPIPInterface").newInstance();
                           Hashtable ht = new Hashtable();
                           ht.put("Class", "systemj.desktop.TCPIPInterface");
                           ht.put("Args", args);
                           ht.put("SubSystem", localSSName);
                           ht.put("serverSocketObj", ssLink);
                           gct.configure(ht);
                           gct.setInterfaceManager(im);
                           gct.invokeReceivingThread();
                           linko.addInterface(SSPartnerName, gct);
                           System.out.println("Link interface addr: " +args+" is created!");
                           RegAllSSAddr.AddSSAddr(SSPartnerName, destAddr);
                           ic.addLink(linko, false);
                           im.setInterconnection(ic);
                           //TCPIPLinkRegistry.AddSSAndPortPair(Integer.toString(locPort), localSSName);
                           sOut.writeInt(locPort);
                           sOut.flush();
               } else {
                   if(!partnerHasLocalPort){
                      sOut.writeInt(locPort);
                      sOut.flush();
                   }
               }
               if(!partHasPort || !localHasPartnerPort){
                       partPortNum = sInt.readInt();
                       Interconnection ic = im.getInterconnection();
                       Interconnection.Link linko = new Interconnection.Link();
                       String args = destAddr+":"+partPortNum;
                           GenericInterface gct = (GenericInterface)Class.forName("systemj.desktop.TCPIPInterface").newInstance();
                           Hashtable ht = new Hashtable();
                           ht.put("Class", "systemj.desktop.TCPIPInterface");
                           ht.put("Args", args);
                           ht.put("SubSystem", SSPartnerName);
                           gct.configure(ht);
                           gct.setInterfaceManager(im);
                           linko.addInterface(SSPartnerName, gct);
                           System.out.println("Link interface addr: " +args+" is created!");
                           RegAllSSAddr.AddSSAddr(SSPartnerName, destAddr);
                           ic.addLink(linko, false);
                           im.setInterconnection(ic);
                           //TCPIPLinkRegistry.AddSSAndPortPair(Integer.toString(partPortNum), destSS);
                    } 
                       sInt.close();
                       sOut.close();
                       socketReceive.close();
                       socketSend.close();
                       ss.close();
        }   catch (JSONException ex3){
            ex3.printStackTrace();
        }   catch (Exception ex){
            ex.printStackTrace();
        }
            return im;
        }
    
    private int getAvailablePort(){
                try {
                    //s2 = new DatagramSocket();
                    
                    ss2 = new ServerSocket(0);
                } catch (Exception e) {
                } 
         return ss2.getLocalPort();
    }
    private int getAvailableReceivingPort(){
            try {
                    ss = new ServerSocket(0);
                } catch (Exception e) {
                }
         return ss.getLocalPort();
    }
    private int getAvailableReceivingLinkPort(){
            try {
                    ssLink = new ServerSocket(0);
                } catch (Exception e) {
                }
         return ssLink.getLocalPort();
    }
}

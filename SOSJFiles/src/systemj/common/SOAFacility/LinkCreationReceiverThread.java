/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package systemj.common.SOAFacility;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
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
import systemj.common.SJSSCDSignalChannelMap;
import systemj.common.SJServiceRegistry;
import systemj.common.SOAFacility.Support.NetworkConnCheckSimple;
import systemj.interfaces.GenericInterface;

/**
 *
 * @author Udayanto
 */
public class LinkCreationReceiverThread implements Runnable{
    Socket socketSend;
    ServerSocket ss;
    boolean isLocal;
    ServerSocket ssLink = null;
    String destAddr = null;
    public LinkCreationReceiverThread(Socket socketSend,ServerSocket ss, String partnerAddr, boolean isLocal){
        this.socketSend = socketSend;
        this.ss = ss;
        this.isLocal = isLocal;
        this.destAddr= partnerAddr;
    }
    @Override
    public void run(){
        String localSSName = SJSSCDSignalChannelMap.getLocalSSName();
                    try {
                    Socket socketReceive = ss.accept();
                    ObjectInputStream sInt = new ObjectInputStream(socketReceive.getInputStream());
                    String initMsg = sInt.readObject().toString();
                   if (initMsg.equalsIgnoreCase("START")){
                      int locPort=0;
                       ObjectOutputStream sOut = new ObjectOutputStream(socketSend.getOutputStream());
                       sOut.writeUTF(SJSSCDSignalChannelMap.getLocalSSName());
                       sOut.flush();
                       String StringSSPortPair = TCPIPLinkRegistry.GetSSAndPortPair().toString();
                       sOut.writeObject(StringSSPortPair);
                       sOut.flush();
                       String SSPartnerName = sInt.readUTF();
                       JSONObject jsLocalPortAlloc = TCPIPLinkRegistry.GetSSAndPortPair();
                        boolean localHasPort = false; //if the partner has link, is the port available 
                        boolean localHasPartnerPort = false; 
                        Enumeration keysPortSSLocal = jsLocalPortAlloc.keys();
                         while(keysPortSSLocal.hasMoreElements()){
                            String portNum = keysPortSSLocal.nextElement().toString();
                            String SSname = jsLocalPortAlloc.getString(portNum);
                            if(SSname.equals(localSSName)){
                                localHasPort = true;
                            }
                            if(SSname.equals(SSPartnerName)){
                                localHasPartnerPort = true;
                            }
                         }
                       String PartnerPortAlloc = sInt.readUTF();
                       JSONObject jsRemotePortAlloc = new JSONObject(new JSONTokener(PartnerPortAlloc));
                        int partPortNum = 0;
                        boolean partHasPort = false;
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
                               locPort = Integer.parseInt(portNum);
                               partnerHasLocalPort = true;
                           }
                        }
                       while(IMBuffer.getIMUpdatingFlag()){}
                       InterfaceManager im = IMBuffer.getInterfaceManagerConfig();
                       String args;        
                       if(!partHasPort || !localHasPartnerPort){
                            partPortNum = sInt.readInt();
                                Interconnection ic = im.getInterconnection();
                                Interconnection.Link linko = new Interconnection.Link();
                                    args = destAddr+":"+partPortNum;
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
                                    //TCPIPLinkRegistry.AddSSAndPortPair(Integer.toString(partPortNum), SSPartnerName);
                        }
                        if(!localHasPort){
                            locPort = getAvailableReceivingLinkPort();
                                    args = destAddr+":"+locPort;
                            Interconnection ic = im.getInterconnection();
                            Interconnection.Link linko = new Interconnection.Link();
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
                                    clearSS();
                                    sOut.writeInt(locPort);
                                    sOut.flush();
                        } else {
                            if(!partnerHasLocalPort){
                                 sOut.writeInt(locPort);
                                 sOut.flush();
                            }
                        }
                       IMBuffer.SaveInterfaceManagerConfig(im);
                   sInt.close();
                   sOut.close();
                   socketReceive.close();
                   ss.close();
                   socketSend.close();
                  }
                }catch (JSONException ex) {
                    ex.printStackTrace();
                } catch (Exception ex){
                    ex.printStackTrace();
                }
             CDLCBuffer.SetLinkCreationBusyFlag(false);
    }

     private void clearSS(){
                    ssLink = null;
    }
     private int getAvailableReceivingLinkPort(){
                try {
                    ssLink = new ServerSocket(0);
                } catch (Exception e) {
                }
                return ssLink.getLocalPort();
    }
}

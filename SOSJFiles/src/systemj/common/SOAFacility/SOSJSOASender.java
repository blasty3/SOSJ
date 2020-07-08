/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemj.common.SOAFacility;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Inflater;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.json.me.JSONTokener;
import systemj.common.CDLCBuffer;
import systemj.common.InflaterDeflater;
import systemj.common.SJRegistryEntry;
import systemj.common.SJSOAMessage;
import systemj.common.SJSSCDSignalChannelMap;
import systemj.common.SJServiceRegistry;
import systemj.common.SOAFacility.Support.NetworkConnCheckSimple;
import systemj.common.SOAFacility.Support.SOABuffer;

 
    
    
/**
 *
 * @author Udayanto
 */
public class SOSJSOASender implements Runnable {
    @Override
    public void run() {
        SJSOAMessage sjdisc = new SJSOAMessage();
        System.out.println("NoP2PServMessageSender thread started");
        NetworkConnCheckSimple netcheck = new NetworkConnCheckSimple();
        //long lastReqAdvTransmittedTime = System.currentTimeMillis();
        //long currentTime = System.currentTimeMillis();
        while (true) {
                String connStat = netcheck.CheckNetworkConn(SOABuffer.getGatewayAddr(), 2500);
                    if (connStat.equalsIgnoreCase("Connected")){
                            //sending req adv to almost expired reg
                        /*
                            Hashtable allRegExpiryDet = SJRegistryEntry.GetAllRegistryExpiryDet();
                            Enumeration keysAllRegDet = allRegExpiryDet.keys();
                            currentTime = System.currentTimeMillis();
                            if(currentTime-lastReqAdvTransmittedTime>400){
                                while(keysAllRegDet.hasMoreElements()){
                                    String regID = keysAllRegDet.nextElement().toString();
                                    Hashtable regExpiryDet = (Hashtable)allRegExpiryDet.get(regID);
                                    long logTime = Long.parseLong((String)regExpiryDet.get("loginTime"));
                                    long expiry = Long.parseLong((String)regExpiryDet.get("expiry"));
                                    String regAddr = (String)regExpiryDet.get("regAddr");
                                    long timeRem = System.currentTimeMillis()-logTime;
                                    if(timeRem>=0.8*expiry){
                                        String regReqAdv = sjdisc.ConstructRegRequestAdvertisementMessage(regAddr,SJSSCDSignalChannelMap.GetLocalSSAddr());

                                        if(regAddr.equals(SJSSCDSignalChannelMap.GetLocalSSAddr()) || regAddr.equalsIgnoreCase("127.0.0.1") || regAddr.equalsIgnoreCase("localhost")){
                                            SendReqAdvMsg("224.0.0.100", regReqAdv);
                                        } else {
                                            SendReqAdvMsg(regAddr, regReqAdv);
                                        }

                                    }
                                
                                }
                                
                                lastReqAdvTransmittedTime = System.currentTimeMillis();
                                
                            }
                            */
                            
                            // end
                            //adv transmission from reg req adv
                            Vector allContentReqAdvBuffer = SOABuffer.getAllContentReqAdvBuffer();
                            if(allContentReqAdvBuffer.size()>0){
                                for (int i=0;i<allContentReqAdvBuffer.size();i++){
                                     JSONObject jsReqAdvMsg = (JSONObject) allContentReqAdvBuffer.get(i);
                                            String destAddr=null;
                                            try {
                                                String advMsg = "";
                                                if (jsReqAdvMsg.getString("MsgType").equalsIgnoreCase("RequestForAdvertisement")){
                                                    String regID = jsReqAdvMsg.getString("regID");
                                                    int portNum = Integer.parseInt(jsReqAdvMsg.getString("respPort"));
                                                    String expTime = Long.toString(SJSSCDSignalChannelMap.GetSSExpiryTime());
                                                    boolean notify = SOABuffer.GetRegNotify();
                                                    boolean notifyCDStat = SOABuffer.GetNotifyChangedCDStat();
                                                    String SSName = SJSSCDSignalChannelMap.getLocalSSName();
                                                    String SSAddr = SJSSCDSignalChannelMap.GetLocalSSAddr();
                                                    advMsg = sjdisc.ConstructAdvertisementMessage(expTime,notify,notifyCDStat,SSName,SSAddr,regID);
                                                    destAddr = jsReqAdvMsg.getString("regAddr");
                                                    SendAdvRespMsg(destAddr, advMsg, portNum);
                                                }
                                            } catch (JSONException ex) {
                                           ex.printStackTrace();
                                        }
                                }
                                SOABuffer.RecordAdvertisementTimeStamp();
                                    SOABuffer.SetAdvTransmissionRequest(false);
                                    SOABuffer.SetRegNotifySS(false);
                                    SOABuffer.SetNotifyChangedCDStat(false);
                                
                            }
                                //regular adv
                            
                            if(SJSSCDSignalChannelMap.GetSSExpiryTime()!=0){
                                boolean startRegAdv = CheckOwnAdv();
                                boolean startAdv = SOABuffer.getAdvTransmissionRequest();
                                if ((startRegAdv || startAdv)){
                                    String LocalSSName = SJSSCDSignalChannelMap.getLocalSSName();
                                    String LocalSSAddr = SJSSCDSignalChannelMap.GetLocalSSAddr();
                                        JSONObject jsAllAvailReg = SJRegistryEntry.GetRegistryFromEntry();
                                        if(jsAllAvailReg.length()>0){
                                            Enumeration keysAvailReg = jsAllAvailReg.keys();
                                            boolean toNotifySS = SOABuffer.GetRegNotify();
                                            boolean changedCDStat = SOABuffer.GetNotifyChangedCDStat();
                                            while(keysAvailReg.hasMoreElements()){
                                                String regID = keysAvailReg.nextElement().toString();
                                                String expTime = Long.toString(SJSSCDSignalChannelMap.GetSSExpiryTime());
                                                String AdvMsg = sjdisc.ConstructAdvertisementMessage(expTime,toNotifySS,changedCDStat,LocalSSName,LocalSSAddr,regID);

                                                try {
                                                    String regAddr = jsAllAvailReg.getString(regID);

                                                    SendAdvMsg(regAddr, AdvMsg, 1770);

                                                } catch (JSONException ex) {
                                                    ex.printStackTrace();
                                                }
                                        }
                                        SOABuffer.RecordAdvertisementTimeStamp();
                                        SOABuffer.SetAdvTransmissionRequest(false);
                                        SOABuffer.SetRegNotifySS(false);
                                        SOABuffer.SetNotifyChangedCDStat(false);
                                        }
                                    }
                            }
                            
                      }
                   
                   
                
                // CD migration... for all conditions whether consumer only, prov only, or both
                
                /*
                Vector allMigrationReqMsg = CDLCBuffer.getMigrationRequestMsg();
                
                if (allMigrationReqMsg.size()>0) {
                    
                    //start migration msg receiver thread
                    
                    int recMsgAmount = allMigrationReqMsg.size();
                    
                    for (int j=0;j<recMsgAmount;j++){
                        
                        //the first message is responded with ACK OK, and then needs to initiate code n sigchan mapping det, the rest of the message responded with ACK NOT OK. Opposite party needs to resend migration req if they wish
                        
                        JSONObject reqMsg = (JSONObject)allMigrationReqMsg.get(j);
                        
                        if(j==0){
                            
                            boolean migPortFree = CheckMigrationRecPortFree();
                            
                            if(CDLCBuffer.getMigrationBusyFlag() || !migPortFree){
                                try {
                                    String destAddress = reqMsg.getString("sourceAddress");
                                
                                    String message = sjdisc.ConstructResponseMigrationMessage("NOT OK", SJSSCDSignalChannelMap.getLocalSSName());
                                
                                    SendRespServMigration(destAddress, message);
                                }   catch (JSONException ex) {
                                    ex.printStackTrace();
                                }
                            } else {
                                try {
                                String destAddress = reqMsg.getString("sourceAddress");
                                String sourceDestSS = reqMsg.getString("destinationSubsystem");
                                String migType = reqMsg.getString("discMsgType");
                                
                                if(SJSSCDSignalChannelMap.getLocalSSName().equals(sourceDestSS)){
                                    
                                    String message = sjdisc.ConstructResponseMigrationMessage("OK", SJSSCDSignalChannelMap.getLocalSSName());
                                
                                    if(migType.equals("strong")){
                                        //Thread CodeOnlyMigMsgRecThr = new Thread(new StrongMigrationMsgReceiverThread());
                    
                                        //CodeOnlyMigMsgRecThr.start();
                                    } else if(migType.equals("weak")){
                                        //Thread WeakMigMsgRecThr = new Thread(new WeakMigrationMsgReceiverThread());
                    
                                        //WeakMigMsgRecThr.start();
                                    }
                                    
                                    
                                
                                    SendRespServMigration(destAddress, message);
                                    
                                } else {
                                    String message = sjdisc.ConstructResponseMigrationMessage("NOT OK", SJSSCDSignalChannelMap.getLocalSSName());
                                
                                    //Thread MigMsgRecThr = new Thread(new MigrationMsgReceiverThread());
                    
                                    //MigMsgRecThr.start();
                                
                                    SendRespServMigration(destAddress, message);
                                }
                                
                                
                                } catch (JSONException ex) {
                                    ex.printStackTrace();
                                }
                            }
                            
                        } else {
                            
                            try {
                                
                                 String LocalSSName = SJSSCDSignalChannelMap.getLocalSSName();
                                
                                String destAddress = reqMsg.getString("sourceAddress");
                                
                                String message = sjdisc.ConstructResponseMigrationMessage("NOT OK", LocalSSName);
                                
                                SendRespServMigration(destAddress, message);
                            } catch (JSONException ex) {
                                ex.printStackTrace();
                            }
                            
                        }
                        
                        
                        
                    }
                    
                }
                */
     
       //  }     
            
           
       }
    }
   
    
    
    
    private void SendAdvMsg(String ipAddr, String message, int port){
        try
                                   {
                                       InetAddress ipAddress = InetAddress.getByName(ipAddr);
                                       byte[] msg = new byte[65507];
                                       MulticastSocket s = new MulticastSocket(port);
                                       ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                                       ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(byteStream));
                                       out.writeObject(message);
                                       out.flush();
                                       msg = byteStream.toByteArray();
                                       out.close();
                                       msg = InflaterDeflater.compress(msg);
                                       DatagramPacket hi = new DatagramPacket(msg, msg.length, ipAddress, port);
                                       s.send(hi);
                                       s.close();
                               }
                               catch (Exception e)
                               {
                                       e.printStackTrace();
                               }
    }
    
    private void SendAdvRespMsg(String ipAddr, String message, int port){
        try
                                   {
                                       InetAddress ipAddress = InetAddress.getByName(ipAddr);
                                       byte[] msg = new byte[65507];
                                       DatagramSocket s = new DatagramSocket(port);
                                       ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                                       ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(byteStream));
                                       out.writeObject(message);
                                       out.flush();
                                       msg = byteStream.toByteArray();
                                       out.close();
                                       msg = InflaterDeflater.compress(msg);
                                       DatagramPacket hi = new DatagramPacket(msg, msg.length, ipAddress, port);
                                       s.send(hi);
                                       //System.out.println("SOSJSOASender,RespAdvMsg sent: " +message);
                                       s.close();
                               }
                               catch (Exception e)
                               {
                                       e.printStackTrace();
                               }
    }
    
    /*
    private boolean SendReqAdvMsg(String ipAddr, String message){
        
        int infoDebug=0;
        
        try
                                   {
                                       
                                       InetAddress ipAddress = InetAddress.getByName(ipAddr);
                                       
                                       if (infoDebug ==1 ) System.out.println("BroadcastMessage send stage 1");
                                       byte[] msg = new byte[65508];
                                      //ipAddress=InetAddress.getByName("192.168.1.255"); //assumed broadcast address
                                      //ipAddress = InetAddress.getByName(super.buffer[1].toString());
                                       
                                       if (infoDebug ==1 ) System.out.println("BroadcastMessage send stage 2");
                                       
                                       
                                       if (infoDebug ==1 ) System.out.println("BroadcastMessage send stage 3");
                                               //SJServiceRegistry.ConstructBroadcastDiscoveryMessage("AllNodes").toString();
                                       
                                       //MulticastSocket s = new MulticastSocket(SJServiceRegistry.getMessageTransmissionPort(SJServiceRegistry.getMessage("BroadcastDiscoveryMessage")));
                                       
                                       MulticastSocket s = new MulticastSocket(1770);
                                       
                                       //s.setLoopbackMode(true);
                                       
                                       ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                                       
                                       //DatagramSocket s = new DatagramSocket(SJServiceRegistry.getMessageTransmissionPort(SJServiceRegistry.getMessage("BroadcastDiscoveryMessage")));
                                      
                                       //DatagramPacket hi = new DatagramPacket(msg.getBytes(), msg.length(), group, Integer.parseInt(str[1]));
                                       
                                       ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(byteStream));
                                     //out.writeObject(SJServiceRegistry.obtainInternalRegistry().toString());
                                       out.writeObject(message); //put service description to be sent to remote devices
                                       out.flush();
                                       if (infoDebug ==1 ) System.out.println("BroadcastMessage send stage 4");
                                       msg = byteStream.toByteArray();
                                       out.close();
                                       if (infoDebug ==1 ) System.out.println("BroadcastMessage send stage 5");
                                       
                                       //compress message
                                       
                                       msg = InflaterDeflater.compress(msg);
                                       
                                       DatagramPacket hi = new DatagramPacket(msg, msg.length, ipAddress, 1770);
                                       if (infoDebug ==1 ) System.out.println("Sending BroadcastDiscoveryMessage");
                                       s.send(hi);
                                       //System.out.println("NoP2PServMsgSender, sending reqRegReAdv: " +message);
                                       if (infoDebug ==1 )System.out.println("data has been sent!");
                                       s.close();
                                       return true;
                                           
                               }
                               catch (java.net.SocketTimeoutException e)
                               {
                                       System.out.println("Timeout when connecting to ip: " + ipAddr + " port :" + 77);
                                       return false;
                               }
                               catch (java.net.UnknownHostException ex){
                                   System.out.println(" ip: " + ipAddr + " port :" + 77 + "Unavailable");
                                   return false;
                               }
        
                               catch (Exception e)
                               {
                                       System.out.println("Problem when connecting to ip: " + ipAddr + " port :" + 77);
                                       e.printStackTrace();
                                       return false;
                               }
        
    }
    */
   
    
    
    
    /*
    private Vector getAlmostExpiredAdvServNames(){
        
        Vector allNames = new Vector();
        
        try {
            String answer = SJServiceRegistry.checkServiceExpiryForAdvertiseRequest();
            JSONObject jsCurrReg = SJServiceRegistry.obtainCurrentRegistry();
            
            JSONObject almostExpNode = jsCurrReg.getJSONObject(answer);
            
            Enumeration allServInd = almostExpNode.keys();
            
            while (allServInd.hasMoreElements()){
                
                String oneServInd = allServInd.nextElement().toString();
                
                JSONObject oneServ = almostExpNode.getJSONObject(oneServInd);
                
                String servName = oneServ.getString("serviceName");
                
                allNames.addElement(servName);
                
            }
            
        } catch (JSONException ex) {
            Logger.getLogger(MessageSenderThread.class.getName()).log(Level.SEVERE, null, ex);
        }
                        
        return allNames;             
                     
    }
    */
    
    
    private boolean CheckOwnAdv(){
        boolean stat;
            if (SJServiceRegistry.getParsingStatus()){
                    long time = System.currentTimeMillis()-SOABuffer.getRecordedAdvertisementTimeStamp();
                if (time>=0.3*SJServiceRegistry.getOwnAdvertisementTimeLimit()){
                    stat=true;
                } else {
                    stat=false;
                }
            } else {
                stat=false;
            }
            return stat;
    }
    
    
    
    /*
    private InetAddress getBroadcastAddress(String Addr){
            
        InetAddress broadcastAddr = null;
             try {
            // TODO code application logic here
                    Enumeration<NetworkInterface> interfaces =
                    NetworkInterface.getNetworkInterfaces();
                    while (interfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = interfaces.nextElement();
                    if (networkInterface.isLoopback())
                        continue;    // Don't want to broadcast to the loopback interface
                        for (InterfaceAddress interfaceAddress :
                            networkInterface.getInterfaceAddresses()) {
                            String addr = interfaceAddress.getAddress().getHostAddress();
                            
                            if(addr.equals(Addr)){
                                 InetAddress broadcast = interfaceAddress.getBroadcast();
                                 
                                  if (broadcast == null) {
                                    continue;
                                  } else {
                                      broadcastAddr = broadcast;
                                  }
                                 
                            }
                            
                           
                            
                            
                          //  if (broadcast == null) {
                         //           continue;
                          //      }
                          //  if (broadcast.toString().contains("192.168.1")) {
                          //          broadcastAddr = broadcast;
                         //       }
                           
                        // Use the address
                         }
                     }
               } catch (SocketException ex) {
                System.out.println("Cannot find address: " +ex.getMessage());
                
            }
             return broadcastAddr;
        }
    */
    
    
    
    
    
    private InetAddress getLocalHostLANAddress() throws UnknownHostException {
    try {
        InetAddress candidateAddress = null;
        // Iterate all NICs (network interface cards)...
        for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
            NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
            // Iterate all IP addresses assigned to each card...
            for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
                InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                if (!inetAddr.isLoopbackAddress()) {

                    if (inetAddr.isSiteLocalAddress()) {
                        // Found non-loopback site-local address. Return it immediately...
                        if (!inetAddr.getHostAddress().equalsIgnoreCase("192.168.7.2")){
                            return inetAddr;
                        } 
                       
                    }
                    else if (candidateAddress == null) {
                        // Found non-loopback address, but not necessarily site-local.
                        // Store it as a candidate to be returned if site-local address is not subsequently found...
                        candidateAddress = inetAddr;
                        // Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
                        // only the first. For subsequent iterations, candidate will be non-null.
                    }
                }
            }
        }
        if (candidateAddress != null) {
            // We did not find a site-local address, but we found some other non-loopback address.
            // Server might have a non-site-local address assigned to its NIC (or it might be running
            // IPv6 which deprecates the "site-local" concept).
            // Return this non-loopback candidate address...
            return candidateAddress;
        }
        // At this point, we did not find a non-loopback address.
        // Fall back to returning whatever InetAddress.getLocalHost() returns...
        InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
        if (jdkSuppliedAddress == null) {
            throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
        }
        return jdkSuppliedAddress;
    }
    catch (Exception e) {
        UnknownHostException unknownHostException = new UnknownHostException("Failed to determine LAN address: " + e);
        unknownHostException.initCause(e);
        throw unknownHostException;
    }
}
    
 
}  
 



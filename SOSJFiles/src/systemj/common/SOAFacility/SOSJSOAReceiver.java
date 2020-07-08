/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemj.common.SOAFacility;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.json.me.JSONTokener;
import systemj.common.CDLCBuffer;
import systemj.common.IMBuffer;
import systemj.common.InflaterDeflater;
import systemj.common.InterfaceManager;
import systemj.common.RegAllCDStats;
import systemj.common.RegAllSSAddr;
import systemj.common.SJRegistryEntry;
import systemj.common.SJSSCDSignalChannelMap;
import systemj.common.SJServiceRegistry;
import systemj.common.SOAFacility.Support.NetworkConnCheckSimple;
import systemj.common.SOAFacility.Support.SOABuffer;

/**
 *
 * @author Udayanto
 */
public class SOSJSOAReceiver implements Runnable{
    MulticastSocket socket=null;
    @Override
    public void run(){
        NetworkConnCheckSimple netcheck = new NetworkConnCheckSimple();
        System.out.println("NoP2PLocalServMessageReceiver thread started");
        while(true){
                String connStat = netcheck.CheckNetworkConn(SOABuffer.getGatewayAddr(), 2500);
                if (connStat.equalsIgnoreCase("Connected")){
                    ReceiveSOAMsg();
                } else {
                    if(socket!=null){
                        socket.close();
                    }
                }
        }
    }
    private void ReceiveSOAMsg(){
        JSONObject js = new JSONObject();
                            try
                            {
                                    byte data[];
                                    byte packet[] = new byte[65507];
                                    socket = new MulticastSocket(1770);
                                    socket.joinGroup(InetAddress.getByName("224.0.0.100"));
                                        DatagramPacket pack = new DatagramPacket(packet, packet.length);
                                        while (true){
                                            pack = new DatagramPacket(packet, packet.length);
                                            socket.receive(pack);
                                                data = new byte[pack.getLength()];
                                                System.arraycopy(packet, 0, data, 0, pack.getLength());
                                                data = InflaterDeflater.decompress(data);
                                                if(data.length > 0)
                                                {
                                                        if(((int)data[0] == -84) && ((int)data[1] == -19))
                                                        {
                                                                        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
                                                                        Object mybuffer = ois.readObject();
                                                                              js = new JSONObject(new JSONTokener(mybuffer.toString()));
                                                                              String msgType = js.getString("MsgType");
                                                                              //System.out.println("SJSOAReceiver, rcvdMsg: " +js.toPrettyPrintedString(2, 0));
                                                                                if(msgType.equalsIgnoreCase("RequestForAdvertisement")){
                                                                                    try {
                                                                                        ArrayList<String> allRegAddr = new ArrayList<String>();
                                                                                        JSONObject jsServRegDet = SJRegistryEntry.GetRegistryFromEntry();
                                                                                        Enumeration keysServReg = jsServRegDet.keys();
                                                                                        while(keysServReg.hasMoreElements()){
                                                                                            String regID = keysServReg.nextElement().toString();
                                                                                            String regAddr = jsServRegDet.getString(regID);
                                                                                            allRegAddr.add(regAddr);
                                                                                        }
                                                                                        String destSS = js.getString("destSS");
                                                                                        if(destSS.equalsIgnoreCase(SJSSCDSignalChannelMap.getLocalSSName())){
                                                                                            SOABuffer.putReqAdvToReqAdvBuffer(js);
                                                                                        }  else {
                                                                                            /*
                                                                                            if(pack.getAddress().getHostAddress().equalsIgnoreCase("224.0.0.100") || pack.getAddress().getHostAddress().equalsIgnoreCase(SJSSCDSignalChannelMap.GetLocalSSAddr())){
                                                                                            } else {
                                                                                                if(allRegAddr.contains(pack.getAddress().getHostAddress())){
                                                                                                    Thread pcktbounce = new Thread(new SOAPacketBouncer(mybuffer.toString().trim()));
                                                                                                    pcktbounce.start();
                                                                                                }
                                                                                            }
                                                                                            */
                                                                                            if(pack.getAddress().getHostAddress().equalsIgnoreCase(SJSSCDSignalChannelMap.GetLocalSSAddr())){
                                                                                                
                                                                                            } else {
                                                                                                if(allRegAddr.contains(pack.getAddress().getHostAddress())){
                                                                                                    Thread pcktbounce = new Thread(new SOAPacketBouncer(mybuffer.toString().trim()));
                                                                                                    pcktbounce.start();
                                                                                                }
                                                                                            }
                                                                                            
                                                                                        }
                                                                                    } catch (JSONException ex) {
                                                                                        ex.printStackTrace();
                                                                                    }
                                                                                } else if(msgType.equalsIgnoreCase("Beacon")){
                                                                                    if(SJRegistryEntry.IsRegistryEntryEmpty()){
                                                                                        SJRegistryEntry.AddRegistryToEntry(js);
                                                                                        SJRegistryEntry.UpdateRegistryExpiry(js);
                                                                                    }
                                                                                } else if (msgType.equalsIgnoreCase("Notify")){
                                                                                    String notID = js.getString("notifyID");
                                                                                    SOABuffer.SetNotifID(Long.parseLong(notID));
                                                                                    /*
                                                                                    if(js.has("CDStats")){
                                                                                        JSONObject jsCDStat = js.getJSONObject("CDStats");
                                                                                        Enumeration keysjsCDStat = jsCDStat.keys();
                                                                                        
                                                                                        while(keysjsCDStat.hasMoreElements()){
                                                                                            String SSName = keysjsCDStat.nextElement().toString();
                                                                                            RegAllCDStats.AddCDStat(SSName, jsCDStat.getJSONObject(SSName));
                                                                                            JSONObject jsCDStats = jsCDStat.getJSONObject(SSName);
                                                                                            Enumeration keysJSCDStats = jsCDStats.keys();
                                                                                                while(keysJSCDStats.hasMoreElements()){
                                                                                                    String keyCDName = keysJSCDStats.nextElement().toString();
                                                                                                    im.addCDLocation(SSName, keyCDName);
                                                                                                    RegAllCDStats.AddCDStat(SSName, jsCDStats);
                                                                                                }
                                                                                        }
                                                                                        IMBuffer.SaveInterfaceManagerConfig(im);
                                                                                        SOABuffer.SetReceivedNotifyChangedCDStat(true);
                                                                                    }
                                                                                    if(js.has("SSAddrs")){
                                                                                        JSONObject jsAllSSAddrs = js.getJSONObject("SSAddrs");
                                                                                            Enumeration keysAllSSAddrs = jsAllSSAddrs.keys();
                                                                                            while(keysAllSSAddrs.hasMoreElements()){
                                                                                                String indivSSNameSSAddr = keysAllSSAddrs.nextElement().toString();
                                                                                                 RegAllSSAddr.AddSSAddr(indivSSNameSSAddr, jsAllSSAddrs.getString(indivSSNameSSAddr));
                                                                                            }
                                                                                    }
                                                                                    */
                                                                                }
                                                                
                                                        }
                                                }
                                         }
                        }
                        catch (Exception e)
                        {
                                e.printStackTrace();
                        }
    }
}
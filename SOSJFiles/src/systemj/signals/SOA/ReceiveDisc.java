/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package systemj.signals.SOA;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.Hashtable;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.json.me.JSONTokener;
import systemj.common.IMBuffer;
import systemj.common.InterfaceManager;
import systemj.common.RegAllCDStats;
import systemj.common.RegAllSSAddr;
import systemj.common.SJRegistryEntry;
import systemj.common.SJSOAMessage;
import systemj.common.SJSSCDSignalChannelMap;
import systemj.interfaces.GenericSignalReceiver;

public class ReceiveDisc extends GenericSignalReceiver{
    String CDName;
    DatagramSocket s1 = null;
    @Override
    public void configure(Hashtable data) throws RuntimeException {
        if(data.containsKey("CDName")){
            CDName = (String)data.get("CDName");
        } else {
            throw new RuntimeException("Need 'CDName' attribute!");
        }
    }
    @Override
    public void run(){
        phyThreadLoop = true;
        while(!terminated){
            Object[] obj = new Object[2];
            if(DiscPortAssignment.IsDiscPortAssignmentExist(CDName)){
                JSONObject jsAllServs = new JSONObject();
                JSONObject jsMsg = new JSONObject();
                    try {
                            s1 = DiscPortAssignment.GetDiscPortAssignment(CDName);
                            jsMsg = ReceiveDiscMsg();
                            if(!jsMsg.isEmpty()){
                                JSONObject jsServList = jsMsg.getJSONObject("serviceList");
                                Enumeration keysServList = jsServList.keys();
                                    while(keysServList.hasMoreElements()){
                                        String indivSSName = keysServList.nextElement().toString();
                                            JSONObject jsAllCDStats = jsMsg.getJSONObject("CDStats");
                                            JSONObject jsAllSSAddrs = jsMsg.getJSONObject("SSAddrs");
                                            Enumeration keysAllCDStats = jsAllCDStats.keys();
                                            while(IMBuffer.getIMUpdatingFlag()){}
                                            InterfaceManager im = IMBuffer.getInterfaceManagerConfig();
                                            while(keysAllCDStats.hasMoreElements()){
                                                String indivSSNameCDStats = keysAllCDStats.nextElement().toString();
                                                RegAllCDStats.AddCDStat(indivSSNameCDStats, jsAllCDStats.getJSONObject(indivSSNameCDStats));
                                                JSONObject jsCDStats = jsAllCDStats.getJSONObject(indivSSNameCDStats);
                                                Enumeration keysJSCDStats = jsCDStats.keys();
                                                while(keysJSCDStats.hasMoreElements()){
                                                    String keyCDName = keysJSCDStats.nextElement().toString();
                                                    im.addCDLocation(indivSSNameCDStats, keyCDName);
                                                }
                                            }
                                            IMBuffer.SaveInterfaceManagerConfig(im);
                                            Enumeration keysAllSSAddrs = jsAllSSAddrs.keys();
                                            while(keysAllSSAddrs.hasMoreElements()){
                                                String indivSSNameSSAddr = keysAllSSAddrs.nextElement().toString();
                                                RegAllSSAddr.AddSSAddr(indivSSNameSSAddr, jsAllSSAddrs.getString(indivSSNameSSAddr));
                                            }
                                    jsAllServs.put(indivSSName, jsServList.getJSONObject(indivSSName));
                                }
                            }
                         
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    } 
                    if(jsAllServs.length()>0){
                        obj[0] = Boolean.TRUE;
                        obj[1] = jsAllServs.toString();
                        super.setBuffer(obj);
                        while(!DiscPortAssignment.GetCDNameTransmittingStat(CDName)){
                            if(terminated){
                                //s1.close();
                                //terminationDone = true;
                                break;
                            }
                        }
                    } else {
                        obj[0] = Boolean.TRUE;
                        obj[1] = "timeout";
                        super.setBuffer(obj);
                        while(!DiscPortAssignment.GetCDNameTransmittingStat(CDName)){
                            if(terminated){
                                //s1.close();
                                //terminationDone = true;
                                break;
                            }
                        }
                    }
                    DiscPortAssignment.RemoveDiscPortAssignment(CDName);
                    
                 } else {
                    obj[0] = Boolean.FALSE;
                    super.setBuffer(obj);
                 }
            
        }
        if(terminated){
                            s1.close();
                            terminationDone = true;
                        }
    }
    public ReceiveDisc(){
		super(); 
    }
        private JSONObject ReceiveDiscMsg(){
        JSONObject js = new JSONObject();
        try{
                                    byte data[];
                                    byte packet[] = new byte[65507];
                                    DatagramPacket pack = new DatagramPacket(packet, packet.length);
                                    DiscPortAssignment.SetDiscReplyPortReady(CDName);
                                            s1.setSoTimeout(4000);
                                            s1.receive(pack);
                                                data = new byte[pack.getLength()];
                                                System.arraycopy(packet, 0, data, 0, pack.getLength());
                                                if(data.length > 0)
                                                {
                                                        if(((int)data[0] == -84) && ((int)data[1] == -19))
                                                        {
                                                                try
                                                                {
                                                                        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
                                                                        Object mybuffer = ois.readObject(); 
                                                                        js = new JSONObject(new JSONTokener(mybuffer.toString().trim()));
                                                                }
                                                                catch(Exception e)
                                                                {
                                                                        e.printStackTrace();
                                                                }
                                                        }
                                                }
                                       s1.close();
                    }catch (SocketTimeoutException stex){
                        s1.close();
                    }catch (IOException iex){
                         iex.printStackTrace();
                         s1.close();
                    }
                   DiscPortAssignment.RemoveDiscPortAssignment(CDName);
                   return js;
    }
}
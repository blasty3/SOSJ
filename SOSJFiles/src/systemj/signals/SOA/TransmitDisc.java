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
import systemj.common.SJRegistryEntry;
import systemj.common.SJSOAMessage;
import systemj.common.SJSSCDSignalChannelMap;
import systemj.interfaces.GenericSignalSender;

public class TransmitDisc extends GenericSignalSender{
    DatagramSocket s1 = null;
    String CDName = null;
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
        try{
            int recPort = getAvailablePort();
            DiscPortAssignment.SetDiscPortAssignment(CDName,s1);
            DiscPortAssignment.SetCDNameTransmittingStat(CDName);
            while(!DiscPortAssignment.GetDiscReplyPortReady(CDName)){
            }
            String SSOrig = SJSSCDSignalChannelMap.getLocalSSName();
            //TransmitDiscMsg(SSOrig, "reg1", "192.168.1.10", recPort);
            
            if(!SJRegistryEntry.IsRegistryEntryEmpty()){
                JSONObject jsReg = SJRegistryEntry.GetRegistryFromEntry();
                String regID="";
                String regAddr = "";
                Enumeration keysJSReg = jsReg.keys();
                while(keysJSReg.hasMoreElements()){
                    regID = keysJSReg.nextElement().toString();
                    regAddr = jsReg.getString(regID);
                    break;
                }
                TransmitDiscMsg(SSOrig, regID, regAddr, recPort);
            }
            
        } catch (JSONException jex){
           jex.printStackTrace();
        }
    }
    private int getAvailablePort() {
         int port = 3333;
         while(true){
                try {
                    s1 = new DatagramSocket(port);
                    break;
                } catch (IOException e) {
                    port++;
                } 
         }
         return port;
    }
    private JSONObject TransmitDiscMsg(String SSOrigin,String regID, String regAddr, int recPort){
        JSONObject js = new JSONObject();
        SJSOAMessage sjdisc = new SJSOAMessage();
        MulticastSocket s = null;
        try{
                    s = new MulticastSocket(1990);
                                String message = sjdisc.ConstructNoP2PServToRegDiscoveryMessage(SSOrigin, regID, recPort);
                                InetAddress ipAddress = InetAddress.getByName(regAddr);
                                       byte[] msg = new byte[65507];
                                       ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                                       ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(byteStream));
                                       out.writeObject(message);
                                       out.flush();
                                       msg = byteStream.toByteArray();
                                       out.close();
                                       DatagramPacket hi = new DatagramPacket(msg, msg.length, ipAddress, 1990);
                                       s.send(hi);
                                       s.close();
                    } catch (Exception iex){
                         iex.printStackTrace();
                         s.close();
                    }
                   return js;
    }
}
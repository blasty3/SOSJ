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
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import org.json.me.JSONObject;
import org.json.me.JSONTokener;
import systemj.common.InflaterDeflater;
import systemj.common.SJSOAMessage;
import systemj.common.SJServiceRegistry;
import systemj.common.SOAFacility.Support.SOABuffer;

/**
 *
 * @author Atmojo
 */
public class ReqAdvTransceive implements Runnable{
    DatagramSocket s1 = null;
    String destinationSS;
    String registryID;
    String regAddress;
    String destAddress;
    public ReqAdvTransceive(String destSS, String regID, String regAddr, String destAddr){
        this.destinationSS = destSS;
        this.registryID = regID;
        this.regAddress = regAddr;
        this.destAddress = destAddr;
    }
    @Override
    public void run() {
        TransceiveReqAdvMsg(destinationSS, registryID, regAddress, destAddress);
    }
    private JSONObject TransceiveReqAdvMsg(String destSS,String regID, String regAddr, String destAddr){
        JSONObject js = new JSONObject();
        SJSOAMessage sjdisc = new SJSOAMessage();
        MulticastSocket s = null;
        try{
                    int recPort = getAvailablePort();
                    s = new MulticastSocket(1770);
                            String message = sjdisc.ConstructReqAdvertisementMessage(regID, regAddr, destSS, Integer.toString(recPort));
                                InetAddress ipAddress = InetAddress.getByName(destAddr);
                                       byte[] msg = new byte[65507];
                                       ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                                       ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(byteStream));
                                       out.writeObject(message); //put service description to be sent to remote devices
                                       out.flush();
                                       msg = byteStream.toByteArray();
                                       out.close();
                                       msg = InflaterDeflater.compress(msg);
                                      // System.out.println("SOSJDisc, transmitting discovery message: " +message);
                                       DatagramPacket hi = new DatagramPacket(msg, msg.length, ipAddress, 1770);
                                       s.send(hi);
                                       s.close();
                                       byte data[];
                                    byte packet[] = new byte[65507];
                                    DatagramPacket pack = new DatagramPacket(packet, packet.length);
                                            s1.setSoTimeout(1000);
                                            s1.receive(pack);
                                                data = new byte[pack.getLength()];
                                                System.arraycopy(packet, 0, data, 0, pack.getLength());
                                                data = InflaterDeflater.decompress(data);
                                                if(data.length > 0)
                                                {
                                                        if(((int)data[0] == -84) && ((int)data[1] == -19))
                                                        {
                                                                try
                                                                {
                                                                        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
                                                                        Object mybuffer = ois.readObject(); 
                                                                              js = new JSONObject(new JSONTokener(mybuffer.toString()));
                                                                              String msgType = js.getString("MsgType");
                                                                                if (msgType.equalsIgnoreCase("Advertisement")){
                                                                                    String assocSS = js.getString("associatedSS");
                                                                                    SJServiceRegistry.SaveAdvertisedServices(js);
                                                                                    SOABuffer.removeCurrReqAdvED(assocSS);
                                                                                }
                                                                        }
                                                                catch(Exception e)
                                                                {
                                                                        e.printStackTrace();
                                                                }
                                                        }
                                                }
                                       s.close();
                                       s1.close();  
        } catch (BindException bex){
                        System.out.println("Discovery and DiscReply ports have been bound and currently used by another CD");
                        bex.printStackTrace();
                        s.close();
                        s1.close();
                    } catch (SocketTimeoutException stex){
                        System.out.println("Disc Reply message Timeout");
                        s.close();
                        s1.close();
                    }
                        catch (IOException iex){
                        System.out.println("Discovery and DiscReply communication problem, check for possible disconnection");
                         iex.printStackTrace();
                         s.close();
                         s1.close();
                    } catch (Exception ex){
                        ex.printStackTrace();
                        s.close();
                        s1.close();
                    }
                   return js;
    }
    
    private int getAvailablePort() {
         //int port = 333;
         
                try {
                    s1 = new DatagramSocket();
                   
                } catch (Exception e) {
                  
                } 
         
         return s1.getLocalPort();
    }
    
}

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
import java.lang.reflect.Field;
import java.net.BindException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.Hashtable;
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
import systemj.lib.input_Channel;

/**
 *
 * @author Udayanto
 */
public class MigrationTransferRecThread implements Runnable{

    String migType;
    ServerSocket ss;
    int trnsPort = 0;
   
    public MigrationTransferRecThread(ServerSocket ss, String migType, int trnsPort){
        this.ss = ss;
        this.migType = migType;
        this.trnsPort = trnsPort;
    }
    
    @Override
    public void run() {
        String localSSName = SJSSCDSignalChannelMap.getLocalSSName();
        String currentOS = System.getProperty("os.name");
                    try {
                        ObjectOutputStream sOut;
                        Socket socketSend;
                    Socket socketReceive = ss.accept();
                    ObjectInputStream sInt = new ObjectInputStream(socketReceive.getInputStream());
                   String StartMsg = sInt.readObject().toString();
                   if (StartMsg.equalsIgnoreCase("START")){
                       String migrAddr = sInt.readObject().toString();
                       String originSSName = sInt.readObject().toString();
                        socketSend = new Socket(InetAddress.getByName(migrAddr),trnsPort);
                        sOut = new ObjectOutputStream(socketSend.getOutputStream());
                   int cd_file_amount = sInt.readInt();
                   int cd_obj_amount=0;
                   if(migType.equalsIgnoreCase("strong")){
                       cd_obj_amount= sInt.readInt();
                   }
                   int user_class_file_amount = sInt.readInt();
                   if(user_class_file_amount>0){
                       for(int k=0;k<user_class_file_amount;k++){
                        String depLoc = sInt.readUTF();
                        String path = System.getProperty("user.dir");
                        String [] splittedDepLoc = depLoc.split("\\.");
                        depLoc = depLoc.replace(".", "/");
                        path = path.replace("\\", "/");
                        String fullPath = path+"/"+depLoc+".class";
                        File libFileClass = new File(fullPath);
                        if(libFileClass.isFile()){
                            sOut.writeBoolean(false);
                            sOut.flush();
                        } else {
                            sOut.writeBoolean(true);
                             sOut.flush();
                             int file_size = sInt.readInt();
                             FileOutputStream outStr = new FileOutputStream(libFileClass);
                             BufferedOutputStream bos = new BufferedOutputStream(outStr);
                             byte[] buffer = null;
                             int total_read_len = 0;
                             while(sInt.readBoolean()){
                                 buffer = (byte[]) sInt.readObject();
                                 total_read_len += buffer.length;
                                 bos.write(buffer);
                                 bos.flush();
                                 //System.out.println("Receive: " + (float)total_read_len/file_size*100 + "%");
                             }
                             bos.close();
                        } 
                    }
                   }
                   if(cd_file_amount>0){
                       for (int j=0;j<cd_file_amount;j++){
                         String fileName = (String) sInt.readObject();
                         String filePath = System.getProperty("user.dir");
                         //System.out.println("File path: " +filePath+" with fileName: " +fileName);
                         File file;
                         String compFilePath;
                         if (currentOS.equalsIgnoreCase("Linux")){
                             file = new File(filePath+"/"+fileName);
                            compFilePath = filePath+"/"+fileName;
                         } else {
                             file = new File(filePath+"\\"+fileName);
                             compFilePath = filePath+"\\"+fileName;
                         }
                         if(file.isFile()){
                             file.delete();
                             //sOut.writeBoolean(false);
                             //sOut.flush();
                         } 
                         //else {
                             sOut.writeBoolean(true);
                             sOut.flush();
                             int file_size = sInt.readInt();
                             FileOutputStream outStr = new FileOutputStream(file);
                             BufferedOutputStream bos = new BufferedOutputStream(outStr);
                             byte[] buffer = null;
                             int total_read_len = 0;
                             while(sInt.readBoolean()){
                                 buffer = (byte[]) sInt.readObject();
                                 total_read_len += buffer.length;
                                 bos.write(buffer);
                                 bos.flush();
                                 //System.out.println("Receive: " + (float)total_read_len/file_size*100 + "%");
                             }
                             bos.close();
                         //}
                         if(migType.equalsIgnoreCase("weak")){
                             String ClassNameOnly = fileName.split("\\.")[0];
                             ClockDomain newCD = null;
                             try{
                                 newCD = (ClockDomain)Class.forName(ClassNameOnly).newInstance();
                             } catch (Exception ex){
                                 ex.printStackTrace();
                             }   
                             newCD.setState("Active");
                             newCD.setName(ClassNameOnly);
                             CDObjectsBuffer.AddCDObjToTempWeakMigBuffer(originSSName,newCD.getName(), newCD); 
                         }
                      }
                   }
                   if(migType.equalsIgnoreCase("strong")){
                       for (int y=0;y<cd_obj_amount;y++){
                        ClockDomain newCD = (ClockDomain)sInt.readObject();
                        CDObjectsBuffer.AddCDObjToTempStrongMigBuffer(originSSName,newCD.getName(), newCD);
                       }
                   }
                   JSONObject allTransferredMapping = new JSONObject();
                   JSONObject jsCurrSigChanMapping = SJSSCDSignalChannelMap.getCurrentSignalChannelMapping();
                        String mapping = (String)sInt.readObject();
                        JSONObject TransferredMapping = new JSONObject(new JSONTokener(mapping));
                        Enumeration keysTransMap = TransferredMapping.keys();
                        while (keysTransMap.hasMoreElements()){
                            String keyTM = keysTransMap.nextElement().toString();
                            allTransferredMapping.put(keyTM,TransferredMapping.getJSONObject(keyTM));
                        }
                           JSONObject jsAllCDs = jsCurrSigChanMapping.getJSONObject(localSSName);
                           Enumeration keysJSAllCDs = jsAllCDs.keys();
                           while (keysJSAllCDs.hasMoreElements()){
                               String CDName = keysJSAllCDs.nextElement().toString();
                               JSONObject indivMap = jsAllCDs.getJSONObject(CDName);
                               allTransferredMapping.put(CDName, indivMap);
                           }
                           JSONObject currMapping = new JSONObject();
                           currMapping.put(localSSName, allTransferredMapping);
                           SJSSCDSignalChannelMap.UpdateAllCurrSignalChannelMapping(currMapping);
                   while(true){
                       boolean NeedToSendSSLoc = sInt.readBoolean();
                        if(NeedToSendSSLoc){
                            String PartnerCDName = sInt.readUTF();
                            String PartnerSSName = sInt.readUTF();
                            String PartnerSSPhyLoc = sInt.readUTF();
                                while(IMBuffer.getIMUpdatingFlag()){}
                                InterfaceManager imMod = IMBuffer.getInterfaceManagerConfig();
                                imMod.addCDLocation(PartnerSSName, PartnerCDName);
                                IMBuffer.SaveInterfaceManagerConfig(imMod);
                                RegAllSSAddr.AddSSAddr(PartnerSSName, PartnerSSPhyLoc);
                        } else {
                            break;
                        }
                   }
                   boolean IsServRegExist = sInt.readBoolean();
                   if(IsServRegExist){
                        String recServDescStr = (String)sInt.readObject();
                        JSONObject recServDesc = new JSONObject(new JSONTokener(recServDescStr));
                        Enumeration keysRecServDesc = recServDesc.keys();
                        while (keysRecServDesc.hasMoreElements()){
                            String keyRecServDesc = keysRecServDesc.nextElement().toString();
                            JSONObject recvdIndivServ = recServDesc.getJSONObject(keyRecServDesc);
                            CDLCBuffer.AddTempMigServDesc(keyRecServDesc, recvdIndivServ);
                        }
                   }
                   String msg = sInt.readUTF();
                   System.out.println("MigTransferRecThread, migration completed");
                   sInt.close();
                   socketReceive.close();
                   ss.close();
                  }
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (ClassNotFoundException ex2){
                    ex2.printStackTrace();
                } catch (JSONException ex) {
                    ex.printStackTrace();
                } 
             if(migType.equalsIgnoreCase("Strong")){
                 CDLCBuffer.SetStrongMigrationDoneFlag();
             } else {
                 CDLCBuffer.SetWeakMigrationDoneFlag();
             }
              CDLCBuffer.releaseMigrationBusyFlag();
    }   
}

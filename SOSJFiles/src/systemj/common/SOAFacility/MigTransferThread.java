/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package systemj.common.SOAFacility;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import systemj.bootstrap.ClockDomain;
import systemj.common.CDLCBuffer;
import systemj.common.CDObjectsBuffer;
import systemj.common.IMBuffer;
import systemj.common.InflaterDeflater;
import systemj.common.InterfaceManager;
import systemj.common.RegAllCDStats;
import systemj.common.RegAllSSAddr;
import systemj.common.SJSSCDSignalChannelMap;
import systemj.common.SJServiceRegistry;
import systemj.common.SOAFacility.Support.SOABuffer;
import systemj.lib.input_Channel;
import systemj.common.SOAFacility.Mig.SJClassVisitor;
import systemj.lib.output_Channel;

/**
 *
 * @author Atmojo
 */
public class MigTransferThread implements Runnable{
    String migType;
    int trnsPort;
    ServerSocket ss;
            String destAddr;
            String destSS;
            InterfaceManager im;
            Vector vecAllCDName;
            Vector vecAllCDIns;
    public MigTransferThread(String migType,String destAddr,String destSS,ServerSocket ss,int trnsPort,Vector vecAllCDName,Vector vecAllCDIns,InterfaceManager im){
        this.migType = migType;
                this.destAddr = destAddr;
                this.destSS = destSS;
                this.im = im;
                this.trnsPort = trnsPort;
                this.ss = ss;
                this.vecAllCDName = vecAllCDName;
                this.vecAllCDIns = vecAllCDIns;
    }
    @Override
    public void run() {
        
        ObjectOutputStream sOut;
        Socket socketSend;
        try{
            String locSSName = SJSSCDSignalChannelMap.getLocalSSName();
            socketSend = new Socket(InetAddress.getByName(destAddr),trnsPort);
            sOut = new ObjectOutputStream(socketSend.getOutputStream());
            Vector cdclassFileNames = new Vector();
            JSONObject jsCurrSigChanMapping = SJSSCDSignalChannelMap.getCurrentSignalChannelMapping();
            JSONObject jsAllCDs = jsCurrSigChanMapping.getJSONObject(locSSName);
            for (int l=0;l<vecAllCDName.size();l++){
                                Enumeration keysJSAllCDs = jsAllCDs.keys();
                                while (keysJSAllCDs.hasMoreElements()){
                                    String CDName = keysJSAllCDs.nextElement().toString();
                                    JSONObject IndivMap = jsAllCDs.getJSONObject(CDName);
                                    if(CDName.equals(vecAllCDName.get(l).toString())){
                                        String fileName =  IndivMap.getString("CDClassName");
                                        if(!cdclassFileNames.contains(fileName)){
                                            cdclassFileNames.addElement(fileName);
                                        }
                                    }
                                }
            }
            int cd_file_amount = cdclassFileNames.size();
             sOut.writeObject("START");
             sOut.flush();
             String locSSAddr = SJSSCDSignalChannelMap.GetLocalSSAddr();
             sOut.writeObject(locSSAddr);
             sOut.flush();
             String ssname = SJSSCDSignalChannelMap.getLocalSSName();
             sOut.writeObject(ssname);
             sOut.flush();
             sOut.writeInt(cd_file_amount);
             sOut.flush();
             int cdobj_amount = vecAllCDName.size();
             if(migType.equalsIgnoreCase("Strong")){
                sOut.writeInt(cdobj_amount);
                sOut.flush();
             }
             
             Vector depLibList = new Vector();
             for(int k=0;k<cdclassFileNames.size();k++){
                 String fileName = cdclassFileNames.get(k).toString();
                 depLibList = ScanUserLib(fileName, depLibList);
             }
            
            
             sOut.writeInt(depLibList.size());
             sOut.flush();
             if(depLibList.size()>0){
                Socket socketReceive = ss.accept();
                ObjectInputStream sInt2 = new ObjectInputStream(socketReceive.getInputStream());
                for(int k=0;k<depLibList.size();k++){
                    String filePath = System.getProperty("user.dir");
                    filePath = filePath.replace("\\", "/");
                    String indivDep = depLibList.get(k).toString();
                    sOut.writeObject(indivDep);
                    sOut.flush();
                   boolean respToIncl = sInt2.readBoolean();
                   socketReceive.close();
                    if(respToIncl){
                     String[] splitIndivDep = indivDep.split("\\.");
                     indivDep = indivDep.replace(".", "/");
                      String fullFilePath = filePath+"/"+indivDep;
                      File clzFile = new File(fullFilePath+".class");
                     BufferedInputStream bis = new BufferedInputStream(new FileInputStream(clzFile));
                       int file_len = (int) clzFile.length();
                       int bytesRead = 0;
                       int total_read_len = 0;
                       byte[] buffer = new byte[file_len];
                        int file_len_2 = file_len;
                        sOut.writeInt(file_len);
                        sOut.flush();
                         boolean sending=true;
                         while(sending){
                             bytesRead = bis.read(buffer);
                             bis.close();
                             file_len_2 -= bytesRead;
                             total_read_len += bytesRead;
                             sOut.writeBoolean(true);
                             sOut.flush();
                             sOut.writeObject(buffer);
                             sOut.flush();
                             if((float)total_read_len/file_len*100>=100){
                                   sending=false;
                             }
                           }
                          sOut.writeBoolean(false);
                          sOut.flush();
                      }
                }
                sInt2.close();
                socketReceive.close();
             }
             if(cdclassFileNames.size()>0){
              Socket socketReceive = ss.accept();
              ObjectInputStream sInt = new ObjectInputStream(socketReceive.getInputStream());
                for(int e=0;e<cdclassFileNames.size();e++){
                       String fileName = cdclassFileNames.get(e).toString();
                                           String filePath = System.getProperty("user.dir");
                                           filePath = filePath.replace("\\", "/");
                                            String fullFilePath;
                                           File testFile;
                                               fullFilePath = filePath+"/"+fileName+".class";
                                               testFile = new File(fullFilePath);
                                               BufferedInputStream bis = new BufferedInputStream(new FileInputStream(testFile));
                                                String[] str = fullFilePath.split("/");
                                               sOut.writeObject(fileName+".class");
                                               sOut.flush();
                                               boolean IsTransfer = sInt.readBoolean();
                                               if(IsTransfer){
                                                   int file_len = (int) testFile.length();
                                                   int bytesRead = 0;
                                                   int total_read_len = 0;
                                                   byte[] buffer = new byte[file_len];
                                                   int file_len_2 = file_len;
                                                   sOut.writeInt(file_len);
                                                   sOut.flush();
                                                   boolean sending=true;
                                                   while(sending){
                                                           bytesRead = bis.read(buffer);
                                                       bis.close();
                                                       file_len_2 -= bytesRead;
                                                       total_read_len += bytesRead;
                                                       sOut.writeBoolean(true);
                                                       sOut.flush();
                                                       sOut.writeObject(buffer);
                                                       sOut.flush();
                                                       if((float)total_read_len/file_len*100>=100){
                                                           sending=false;
                                                       }
                                                   }
                                                       sOut.writeBoolean(false);
                                                       sOut.flush();
                                               }
                }
             sInt.close();
             socketReceive.close();
        }
             if(migType.equalsIgnoreCase("Strong")){
                        ClockDomainLifeCycleSigChanImpl cdlcmsigimpl = new ClockDomainLifeCycleSigChanImpl();
                                         for(int t=0;t<vecAllCDIns.size();t++){
                                                ClockDomain migCDInst = (ClockDomain)vecAllCDIns.get(t);
                                                String CDName = migCDInst.getName();
                                                    if (migCDInst.getState().equalsIgnoreCase("Active")){
                                                        ClockDomain cdObjMod = cdlcmsigimpl.NullifySigPhyIntfForMigration(jsAllCDs, destSS, CDName,migCDInst,im);
                                                        sOut.writeObject(cdObjMod);
                                                        sOut.flush();
                                                    } else if (migCDInst.getState().equalsIgnoreCase("Sleep")){
                                                        ClockDomain cdObj =  CDObjectsBuffer.GetCDInstancesFromBuffer(CDName);
                                                        sOut.writeObject(cdObj);
                                                        sOut.flush();
                                                    }
                                            } 
             }
             JSONObject jsCurrSigChanMap = SJSSCDSignalChannelMap.getCurrentSignalChannelMapping();
             JSONObject allCDsCurrMap = jsCurrSigChanMap.getJSONObject(locSSName);
             JSONObject transferredMap = new JSONObject();
             JSONObject jsSSCDs = new JSONObject();
             boolean anyLocalPartner = false;
             Enumeration keysAllCDsCurrMap = allCDsCurrMap.keys();
             JSONObject jsNewCDList = new JSONObject();
               while(keysAllCDsCurrMap.hasMoreElements()){
                   String CDName = keysAllCDsCurrMap.nextElement().toString();
                     for (int j=0;j<vecAllCDName.size();j++){
                         if(CDName.equals(vecAllCDName.get(j).toString())){
                            transferredMap.put(CDName, allCDsCurrMap.getJSONObject(CDName));
                         }
                     }
                     if(!vecAllCDName.contains(CDName)){
                          jsNewCDList.put(CDName, allCDsCurrMap.getJSONObject(CDName));
                     }
                  }
                        sOut.writeObject(transferredMap.toString());
                        sOut.flush();
                        Vector vecCDNameWithLocPartner = new Vector();
                        for (int j=0;j<vecAllCDIns.size();j++){
                            ClockDomain CDIns = (ClockDomain)vecAllCDIns.get(j);
                            String CDName = CDIns.getName();
                                    JSONObject CDMap =  allCDsCurrMap.getJSONObject(CDName);
                                    JSONObject SChanMap = CDMap.getJSONObject("SChannels");
                                    JSONObject SChanIn = SChanMap.getJSONObject("inputs");
                                    if(!SChanIn.isEmpty()){
                                        Enumeration keysSChanIn = SChanIn.keys();
                                        while(keysSChanIn.hasMoreElements()){
                                            String InChanName = keysSChanIn.nextElement().toString();
                                            JSONObject InChanDet = SChanIn.getJSONObject(InChanName);
                                            String cname = InChanName+"_in";
                                            Field f = CDIns.getClass().getField(cname);
                                            input_Channel inchan = (input_Channel)f.get(CDIns);
                                            String CDNameOriginFrom = InChanDet.getString("From");
                                            String[] pnames = CDNameOriginFrom.split("\\.");
                                            if(!CDNameOriginFrom.equalsIgnoreCase(".")){
                                                String SSLoc;
                                                SSLoc = im.getCDLocation(pnames[0]);
                                                if(!SSLoc.equals(destSS) && !SSLoc.equals(locSSName)){
                                                            inchan.TransmitCDLocChanges(destSS);
                                                            sOut.writeBoolean(true);
                                                            sOut.flush();
                                                            sOut.writeUTF(pnames[0]);
                                                            sOut.flush();
                                                            sOut.writeUTF(SSLoc);
                                                            sOut.flush();
                                                            String SSPartAddr = RegAllSSAddr.getSSAddrOfSSName(SSLoc);
                                                            sOut.writeUTF(SSPartAddr);
                                                            sOut.flush();
                                                } else if(SSLoc.equals(locSSName) && !vecAllCDName.contains(pnames[0])){
                                                    anyLocalPartner = true;
                                                    vecCDNameWithLocPartner.addElement(CDName);
                                                }
                                            }  
                                            while(IMBuffer.getIMUpdatingFlag()){}
                                            InterfaceManager imMod = IMBuffer.getInterfaceManagerConfig();
                                            Hashtable channels = imMod.getAllChannelInstances();
                                            channels.remove(inchan.Name);
                                            imMod.setChannelInstances(channels);
                                            IMBuffer.SaveInterfaceManagerConfig(imMod);
                                        }
                                    }
                                    JSONObject SChanOut = SChanMap.getJSONObject("outputs");
                                    if(!SChanOut.isEmpty()){
                                        Enumeration keysSChanOut = SChanOut.keys();
                                        while(keysSChanOut.hasMoreElements()){
                                            String OutChanName = keysSChanOut.nextElement().toString();
                                            JSONObject OutChanDet = SChanOut.getJSONObject(OutChanName);
                                            String cname = OutChanName+"_o";
                                                        Field f = CDIns.getClass().getField(cname);
                                                        output_Channel ochan = (output_Channel)f.get(CDIns);
                                            String CDNameDestTo = OutChanDet.getString("To");
                                            String[] pnames = CDNameDestTo.split("\\.");
                                            if(!CDNameDestTo.equalsIgnoreCase(".")){
                                                 String SSLoc;
                                                SSLoc = im.getCDLocation(pnames[0]);
                                                if(!SSLoc.equals(destSS) && !SSLoc.equals(locSSName)){
                                                            ochan.TransmitCDLocChanges(destSS);
                                                            sOut.writeBoolean(true);
                                                            sOut.flush();
                                                            sOut.writeUTF(pnames[0]);
                                                            sOut.flush();
                                                            sOut.writeUTF(SSLoc);
                                                            sOut.flush();
                                                            String SSPartAddr = RegAllSSAddr.getSSAddrOfSSName(SSLoc);
                                                            sOut.writeUTF(SSPartAddr);
                                                            sOut.flush();
                                                } else if(SSLoc.equals(locSSName)  && !vecAllCDName.contains(pnames[0])){
                                                    anyLocalPartner = true;
                                                    vecCDNameWithLocPartner.addElement(CDName);
                                                }
                                            }
                                            while(IMBuffer.getIMUpdatingFlag()){}
                                            InterfaceManager imMod = IMBuffer.getInterfaceManagerConfig();
                                            Hashtable channels = imMod.getAllChannelInstances();
                                            channels.remove(ochan.Name);
                                            imMod.setChannelInstances(channels);
                                            IMBuffer.SaveInterfaceManagerConfig(imMod);
                                        }
                                    }
                            }
                            sOut.writeBoolean(false);
                            sOut.flush();    
                            jsSSCDs.put(locSSName, jsNewCDList);
                     SJSSCDSignalChannelMap.UpdateAllCurrSignalChannelMapping(jsSSCDs);
                JSONObject jsIntReg = SJServiceRegistry.obtainInternalRegistry();
                    JSONObject transferredReg  = new JSONObject();
                    for(int m=0;m<vecAllCDName.size();m++){
                        String cdname = vecAllCDName.get(m).toString();
                        Enumeration keysjsIntReg = jsIntReg.keys();
                        while (keysjsIntReg.hasMoreElements()){
                           String servIndex = keysjsIntReg.nextElement().toString();
                           JSONObject indivServ = jsIntReg.getJSONObject(servIndex);
                           if(cdname.equalsIgnoreCase(indivServ.getString("associatedCDName"))){
                               transferredReg.put(servIndex,indivServ);
                               jsIntReg.remove(servIndex);
                           }
                       }
                    }
                    if(transferredReg.isEmpty()){
                        sOut.writeBoolean(false);
                        sOut.flush();
                    } else {
                        sOut.writeBoolean(true);
                        sOut.flush();
                        sOut.writeObject(transferredReg.toString());
                        sOut.flush();
                    }
                    SJServiceRegistry.UpdateAllInternalRegistry(jsIntReg);
                    CDLCBuffer.SetMigrationStatus(destSS, "SUCCESSFUL");
                SJSSCDSignalChannelMap.UpdateAllCurrSignalChannelMapping(jsSSCDs); 
                if(anyLocalPartner){
                    CDLCBuffer.SetAllMigLocPartReconfBuffer(destSS, migType, vecCDNameWithLocPartner,transferredMap);
                    while(CDLCBuffer.MigLocPartReconfBufferHas(destSS)){}
                }
                for(int i=0;i<vecAllCDName.size();i++ ){
                        String cdname = vecAllCDName.get(i).toString();
                        RegAllCDStats.RemoveIntCDMacroState(cdname);
                }
                sOut.writeUTF("STOP");
                sOut.flush();
                SOABuffer.SetAdvTransmissionRequest(true);
                sOut.close();
                ss.close();
        } catch(FileNotFoundException ex){
            ex.printStackTrace();
            CDLCBuffer.RecoverReqMigrateAllCD(destSS, migType, vecAllCDName);
            CDLCBuffer.SetMigrationStatus(destSS, "FAIL");
        } catch(IOException ex2){
            ex2.printStackTrace();
            CDLCBuffer.RecoverReqMigrateAllCD(destSS, migType, vecAllCDName);
            CDLCBuffer.SetMigrationStatus(destSS, "FAIL");
        } catch (JSONException ex3) {
            ex3.printStackTrace();
            CDLCBuffer.RecoverReqMigrateAllCD(destSS, migType, vecAllCDName);
            CDLCBuffer.SetMigrationStatus(destSS, "FAIL");
        } 
        catch (Exception ex){
            ex.printStackTrace();
            CDLCBuffer.RecoverReqMigrateAllCD(destSS, migType, vecAllCDName);
            CDLCBuffer.SetMigrationStatus(destSS, "FAIL");
        }
        CDLCBuffer.SetCDLCMigrationFlagFree();
        CDLCBuffer.RemoveOccuringMigDestSS(destSS);
        
        
        
    }
    
    private Vector ScanUserLib(String CDClassFileName, Vector existingDepList){
        String filePath = System.getProperty("user.dir");
        filePath = filePath.replace("\\", "/");
            try{
                File classesDirSS = new File(filePath);
            SJClassVisitor scv = null;
            InputStream is = ClassLoader.getSystemResourceAsStream(CDClassFileName+".class");
            scv = new SJClassVisitor(Opcodes.ASM5, classesDirSS, CDClassFileName, existingDepList);
            ClassReader cr = new ClassReader(is);
            cr.accept(scv, ClassReader.SKIP_DEBUG);
            is.close();
             existingDepList = scv.getDependencies();
            } catch (Exception ex){
                ex.printStackTrace();
            }
           return existingDepList;
    }
}

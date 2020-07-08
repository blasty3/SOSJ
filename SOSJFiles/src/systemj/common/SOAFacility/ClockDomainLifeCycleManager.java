/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package systemj.common.SOAFacility;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
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
import systemj.common.CyclicScheduler;
import systemj.common.IMBuffer;
import systemj.common.Interconnection;
import systemj.common.InterfaceManager;
import systemj.common.RegAllCDStats;
import systemj.common.RegAllSSAddr;
import systemj.common.SJSOAMessage;
import systemj.common.SJSSCDSignalChannelMap;
import systemj.common.SJServiceRegistry;
import systemj.common.SOAFacility.Support.SOABuffer;
import systemj.common.SOSJConstructsMessageGenerator;
//import systemj.common.SignalObjBuffer;
import systemj.interfaces.GenericInterface;
import systemj.interfaces.GenericSignalReceiver;
import systemj.interfaces.GenericSignalSender;
import systemj.interfaces.Scheduler;
import systemj.lib.AChannel;
import systemj.lib.Signal;
import systemj.lib.input_Channel;
import systemj.lib.output_Channel;

/**
 *
 * @author Udayanto
 */
public class ClockDomainLifeCycleManager {
    
   
    public Vector run(Scheduler sc, InterfaceManager im){
        
        Vector vecSCIM = new Vector();
        SOSJConstructsMessageGenerator sosjcmg = new SOSJConstructsMessageGenerator();
        //System.out.println("CDLCM is run!");
        
         ClockDomainLifeCycleSigChanImpl cdlcimpl = new ClockDomainLifeCycleSigChanImpl();
        
        try {
            JSONObject jsCurrMap = SJSSCDSignalChannelMap.getCurrentSignalChannelMapping();
            
            String keyCurrSS = SJSSCDSignalChannelMap.getLocalSSName();
            
            JSONObject jsLocalCDs  = jsCurrMap.getJSONObject(keyCurrSS);
            while(!CDLCBuffer.IsAllMigLocPartReconfBufferEmpty()){
                    Hashtable hashAllMigrating = CDLCBuffer.GetAllMigLocPartReconfBufferEmpty();
                    Enumeration keysAllMigs = hashAllMigrating.keys();
                    while(keysAllMigs.hasMoreElements()){
                        String keyDestSS = keysAllMigs.nextElement().toString();
                        Hashtable hashMigrating = CDLCBuffer.GetMigLocPartReconfBuffer(keyDestSS);
                        Vector vecAllCDName = (Vector)hashMigrating.get("CDNames");
                    //String DestSS = (String)hashMigrating.get("DestSS");
                        String migType = (String) hashMigrating.get("MigType");
                        JSONObject migratingMap = (JSONObject) hashMigrating.get("MigratingMap");
                        for(int r=0;r<vecAllCDName.size();r++){
                            //ClockDomain MigratingCDIns = (ClockDomain)vecAllCDIns.get(r);
                            String migCDName = vecAllCDName.get(r).toString();
                            vecSCIM = cdlcimpl.ModifyLocalPartner(jsLocalCDs,migratingMap,keyCurrSS,migCDName,migType,keyDestSS,im, sc);
                            sc = (Scheduler)vecSCIM.get(1);
                            im = (InterfaceManager)vecSCIM.get(0);
                        }
                        CDLCBuffer.RemoveMigLocPartReconfBuffer(keyDestSS);
                    }
                   
            }
            
            if(!CDLCBuffer.IsRequestCreateCDEmpty()){
                //int createCDSize = CDLCBuffer.getRequestCreateSize();
                Vector allCreateCDs =  CDLCBuffer.getAllRequestCreateCD();
                JSONObject jsCDSD = CDLCBuffer.GetTempUpdateServDesc();
                Hashtable CrCDs = new Hashtable();
                /*
                for (int i=0;i<allCreateCDs.size();i++){
                    String CDName = allCreateCDs.get(i).toString();
                    //String CDName = CDLCBuffer.getRequestCreateCD(i);
                    JSONObject CDDet = CDLCBuffer.GetTempSigChanMap(CDName);
                    JSONObject jsCDDet = CDDet.getJSONObject(CDName);
                    String sclazz = jsCDDet.getString("CDClassName");
                    ClockDomain newCD = (ClockDomain)Class.forName(sclazz).newInstance();
                    newCD.setName(CDName);
                    CrCDs.put(CDName, newCD);
                }
                */
                
                long totTimeCDDesc = 0;
                long totTimeRmvCD = 0;
                long totTimeAddCD = 0;
                
                for(int i=0;i<allCreateCDs.size();i++){
                    //String CDName = CDLCBuffer.getRequestCreateCD(i);
                    String CDName = allCreateCDs.get(i).toString();
                    //System.out.println("CDLCManager, TempCDMap: " +CDLCBuffer.getAllTempSignalChannelMap().toPrettyPrintedString(2, 0));
                    JSONObject CDDet = CDLCBuffer.GetTempSigChanMap(CDName);
                    JSONObject jsCDDet = CDDet.getJSONObject(CDName);
                    
                    
                    
                    String sclazz = jsCDDet.getString("CDClassName");
                    ClockDomain newCD = (ClockDomain)Class.forName(sclazz).newInstance();
                    
                    
                    
                   
                    
                    
                    
                    newCD.setName(CDName);
                    CrCDs.put(CDName, newCD);
                    
                    if(sc.SchedulerHasCD(CDName)){
                       
                        Vector vec = cdlcimpl.removeCD(jsLocalCDs, keyCurrSS, CDName, "Active", im, sc);
                       
                        
                        sc = (Scheduler)vec.get(1);
                        im = (InterfaceManager) vec.get(0);
                                //JSONObject CDDet = CDLCBuffer.GetTempSigChanMap(CDName);
                                //JSONObject jsCDDet = CDDet.getJSONObject(CDName);
                                //ClockDomain newCD = (ClockDomain)CrCDs.get(CDName);
                                //String sclazz = jsCDDet.getString("CDClassName");
                                //ClockDomain newCD = (ClockDomain)Class.forName(sclazz).newInstance();
                                newCD.setName(CDName);
                                newCD.setState("Active");
                                
                        vec = cdlcimpl.AddCD(jsLocalCDs,CDDet,keyCurrSS, CDName, newCD,CrCDs,"","", true, im, sc);
                       
                        
                        sc = (Scheduler)vec.get(1);
                        im = (InterfaceManager) vec.get(0);
                        SOABuffer.setAdvVisib(CDName, "visible");
                    } else if (CDObjectsBuffer.CDObjBufferHas(CDName)){
                        Vector vec = cdlcimpl.removeCD(jsLocalCDs, keyCurrSS, CDName, "Sleep", im, sc);
                        sc = (Scheduler)vec.get(1);
                        im = (InterfaceManager) vec.get(0);
                            //JSONObject CDDet = CDLCBuffer.GetTempSigChanMap(CDName);
                             //JSONObject jsCDDet = CDDet.getJSONObject(CDName);
                             // ClockDomain newCD = (ClockDomain)CrCDs.get(CDName);
                           // String sclazz = jsCDDet.getString("CDClassName");
                           // ClockDomain newCD = (ClockDomain)Class.forName(sclazz).newInstance();
                            newCD.setName(CDName);
                            newCD.setState("Sleep");
                            vec = cdlcimpl.AddCD(jsLocalCDs,CDDet, keyCurrSS, CDName, newCD, CrCDs,"","",false,im, sc);
                            //vec = cdlcimpl.AddSigChanObjNotInitModifySCIM(jsLocalCDs,CDDet, keyCurrSS, CDName, newCD, CrCDs,im, sc);
                            sc = (Scheduler)vec.get(1);
                            im = (InterfaceManager) vec.get(0);
                            SOABuffer.setAdvVisib(CDName, "invisible");
                    } else {
                        //JSONObject CDDet = CDLCBuffer.GetTempSigChanMap(CDName);
                         //JSONObject CdIndivDet = CDDet.getJSONObject(CDName);
                         // ClockDomain newCD = (ClockDomain)CrCDs.get(CDName);
                         //String sclazz = CdIndivDet.getString("CDClassName");
                           // ClockDomain newCD = (ClockDomain)Class.forName(sclazz).newInstance();
                            newCD.setName(CDName);
                            newCD.setState("Active");
                            
                           
                            
                            Vector vec = cdlcimpl.AddCD(jsLocalCDs,CDDet, keyCurrSS, CDName, newCD, CrCDs, "","",true,im, sc);
                            
                           
                            
                           
                            
                           
                            
                            sc = (Scheduler)vec.get(1);
                            im = (InterfaceManager) vec.get(0);
                            SOABuffer.setAdvVisib(CDName, "visible");
                    }
                    CDLCBuffer.RemoveTempSigChanMap(CDName);
                }
                SJServiceRegistry.AddInternalRegWithServ(jsCDSD);
                System.out.println("CDLCM, CD Creation completed");
                CDLCBuffer.ClearTempUpdateServDesc();
                CDLCBuffer.clearRequestCreateCD();
                
                System.out.println("Tot CreateCD time: " +totTimeCDDesc+ " ms");
                System.out.println("Tot AddCD time: " +totTimeAddCD+ " ms");
                System.out.println("Tot RmvCDUpdate time: " +totTimeRmvCD+ " ms");
                
            } 
            
            if(!CDLCBuffer.IsRequestWakeUpCDEmpty()){
                
                    Vector CDToWakeUp = CDLCBuffer.GetAllRequestWakeUpCD();
                     Hashtable CDinSSGroup = new Hashtable();
                                            for(int i=0;i<CDLCBuffer.GetRequestWakeUpCDSize();i++){
                                                String keyCDName = CDToWakeUp.get(i).toString();
                                                    if(im.getCDLocation(keyCDName).equals(keyCurrSS)){
                                                          if(CDObjectsBuffer.CDObjBufferHas(keyCDName)){
                                                            sc = cdlcimpl.ResumeCD(jsLocalCDs, keyCurrSS, keyCDName, sc);
                                                            SOABuffer.setAdvVisib(keyCDName, "visible");
                                                          }
                                                    } else {
                                                        if(im.hasCDLocation(keyCDName)){
                                                            String ssLoc = im.getCDLocation(keyCDName);
                                                            if(CDinSSGroup.containsKey(ssLoc)){
                                                                JSONObject cdnamevec = (JSONObject)CDinSSGroup.get(ssLoc);
                                                                int hashsize = cdnamevec.length();
                                                                cdnamevec.put(Integer.toString(hashsize),keyCDName);
                                                                CDinSSGroup.put(ssLoc, cdnamevec);
                                                            } else {
                                                                JSONObject cdnamevec = new JSONObject();
                                                                int hashsize = cdnamevec.length();
                                                                cdnamevec.put(Integer.toString(hashsize),keyCDName);
                                                                CDinSSGroup.put(ssLoc, cdnamevec);
                                                            }
                                                        } 
                                                    }
                                                    
                                            }
                                         
                                            
                                            
                                            if(CDinSSGroup.size()>0){
                                                
                                                Enumeration keysSSname = CDinSSGroup.keys();
                                                
                                                while(keysSSname.hasMoreElements()){
                                                 
                                                    String destSS = keysSSname.nextElement().toString();
                                                    
                                                    JSONObject jsCDNames = (JSONObject)CDinSSGroup.get(destSS);
                                                    
                                                    String message = sosjcmg.GenerateWakeUpCDsMessageOfJSON(destSS, jsCDNames);
                                                    
                                                    String SSAddr = RegAllSSAddr.getSSAddrOfSSName(destSS);
                                                    
                                                    sendReconfigMessage(SSAddr, message);
                                                    
                                                }
                                            }
                                            CDLCBuffer.ClearRequestWakeUpCD();
                                        }
            
                                        if(!CDLCBuffer.IsRequestHibernateCDEmpty()){
                                            
                                             Hashtable CDinSSGroup = new Hashtable();
                                            Vector CDToSuspend = CDLCBuffer.GetAllRequestHibernateCD();
                                            for(int i=0;i<CDToSuspend.size();i++){
                                                String keyCDName = CDToSuspend.get(i).toString();
                                                    if(im.getCDLocation(keyCDName).equals(keyCurrSS)){
                                                        if (sc.SchedulerHasCD(keyCDName)){ 
                                                                 sc = cdlcimpl.disableCD(jsLocalCDs, keyCurrSS, keyCDName, sc);
                                                                 SOABuffer.setAdvVisib(keyCDName, "invisible");
                                                        }
                                                    } else {
                                                        if(im.hasCDLocation(keyCDName)){
                                                        
                                                            String ssLoc = im.getCDLocation(keyCDName);
                                                        
                                                            if(CDinSSGroup.containsKey(ssLoc)){

                                                                JSONObject cdnamevec = (JSONObject)CDinSSGroup.get(ssLoc);

                                                                int hashsize = cdnamevec.length();

                                                                cdnamevec.put(Integer.toString(hashsize),keyCDName);
                                                                CDinSSGroup.put(ssLoc, cdnamevec);

                                                            } else {

                                                                JSONObject cdnamevec = new JSONObject();

                                                                int hashsize = cdnamevec.length();
                                                                cdnamevec.put(Integer.toString(hashsize),keyCDName);

                                                                CDinSSGroup.put(ssLoc, cdnamevec);
                                                            }
                                                            
                                                        } 
                                                        
                                                }
                                                
                                            }
                                           
                                            
                                           
                                            
                                            if(CDinSSGroup.size()>0){
                                                
                                                Enumeration keysSSname = CDinSSGroup.keys();
                                                
                                                while(keysSSname.hasMoreElements()){
                                                 
                                                    String destSS = keysSSname.nextElement().toString();
                                                    
                                                    JSONObject jsCDNames = (JSONObject)CDinSSGroup.get(destSS);
                                                    
                                                    String message = sosjcmg.GenerateSuspendCDsMessageOfJSON(destSS, jsCDNames);
                                                    
                                                    String SSAddr = RegAllSSAddr.getSSAddrOfSSName(destSS);
                                                    sendReconfigMessage(SSAddr, message);
                                                }
                                            }
                                            CDLCBuffer.ClearRequestHibernateCD();
                                        }
                                        
                                        if(!CDLCBuffer.IsRequestKillCDEmpty()){
                                           
                                             long totTimeTerminateSigChan = 0;
                                            long totTimeTerminateServDescRemCDFileCD = 0;
                                            
                                             //Vector remvdCDName = new Vector();
                                            for(int i=0;i<CDLCBuffer.GetRequestKillCDSize();i++){
                                                String keyCDName = CDLCBuffer.GetRequestKillCD(i);
                                                Hashtable CDinSSGroup = new Hashtable();
                                                 
                                                if(im.getCDLocation(keyCDName).equalsIgnoreCase(keyCurrSS)){
                                                    
                                                   
                                                
                                                    
                                                        if(sc.SchedulerHasCD(keyCDName)){
                                                            Vector vec = cdlcimpl.removeCD(jsLocalCDs, keyCurrSS, keyCDName,"Active",im, sc);
                                                            sc = (Scheduler)vec.get(1);
                                                            im = (InterfaceManager) vec.get(0);
                                                        } else if (CDObjectsBuffer.CDObjBufferHas(keyCDName)){
                                                            Vector vec = cdlcimpl.removeCD(jsLocalCDs, keyCurrSS, keyCDName,"Sleep",im, sc);
                                                            sc = (Scheduler)vec.get(1);
                                                            im = (InterfaceManager) vec.get(0);
                                                        }
                                                        
                                                       
                                                        
                                                       
                                                        
                                                        JSONObject jsIntServ = SJServiceRegistry.obtainInternalRegistry();
                                                        Enumeration keysServNames = jsIntServ.keys();
                                                        while(keysServNames.hasMoreElements()){
                                                            String servName = keysServNames.nextElement().toString();
                                                            JSONObject jsIndivIntServ = jsIntServ.getJSONObject(servName);
                                                            String assocCDName = jsIndivIntServ.getString("associatedCDName");
                                                            if(assocCDName.equals(keyCDName)){
                                                                    SOABuffer.removeAdvStatOfCDName(keyCDName);
                                                            }
                                                        }
                                                        //remvdCDName.addElement(keyCDName);
                                                        JSONObject CDDet = jsLocalCDs.getJSONObject(keyCDName);
                                                        String sclazz = CDDet.getString("CDClassName");
                                                        String fileRootDir = System.getProperty("user.dir");
                                                        fileRootDir = fileRootDir.replace("\\", "/");
                                                        Path path = FileSystems.getDefault().getPath(fileRootDir,sclazz+".class");
                                                        System.out.println("CDLCM, deleting file if file exists:" +path);
                                                        //File f = new File(fileRootDir+"/"+sclazz+".class");
                                                        //if(f.exists() && !f.isDirectory()){
                                                        if(Files.exists(path)){
                                                            Files.deleteIfExists(path);
                                                        //}
                                                        }
                                                            JSONObject jsCopyMap = new JSONObject();
                                                            JSONObject jsCopiedMap = SJSSCDSignalChannelMap.getCurrentSignalChannelMapping();
                                                                    JSONObject jsAllCDsMap = jsCopiedMap.getJSONObject(keyCurrSS);
                                                                    JSONObject jsCopyAllCDs = new JSONObject();
                                                                    Enumeration keysJSAllCDsMap = jsAllCDsMap.keys();
                                                                    while (keysJSAllCDsMap.hasMoreElements()){
                                                                        String indivCDName = keysJSAllCDsMap.nextElement().toString();
                                                                        if(!indivCDName.equals(keyCDName)){
                                                                            jsCopyAllCDs.put(indivCDName, jsAllCDsMap.getJSONObject(indivCDName));
                                                                        }
                                                                    }
                                                                    jsCopyMap.put(keyCurrSS, jsCopyAllCDs);
                                                            SJSSCDSignalChannelMap.UpdateAllCurrSignalChannelMapping(jsCopyMap);
                                                            
                                                           
                                                }
                                                //if CD is in another SS, probably wont be followed through
                                                else {
                                                         if(im.hasCDLocation(keyCDName)){
                                                        
                                                            String ssLoc = im.getCDLocation(keyCDName);
                                                        
                                                            if(CDinSSGroup.containsKey(ssLoc)){

                                                                JSONObject cdnamevec = (JSONObject)CDinSSGroup.get(ssLoc);

                                                                int hashsize = cdnamevec.length();

                                                                cdnamevec.put(Integer.toString(hashsize),keyCDName);
                                                                CDinSSGroup.put(ssLoc, cdnamevec);

                                                            } else {

                                                                JSONObject cdnamevec = new JSONObject();

                                                                int hashsize = cdnamevec.length();
                                                                cdnamevec.put(Integer.toString(hashsize),keyCDName);

                                                                CDinSSGroup.put(ssLoc, cdnamevec);
                                                            }
                                                            
                                                        } 
                                                         
                                                }
                                                
                                                       if(CDinSSGroup.size()>0){
                                                
                                                            Enumeration keysSSname = CDinSSGroup.keys();

                                                            while(keysSSname.hasMoreElements()){

                                                                String destSS = keysSSname.nextElement().toString();

                                                                JSONObject jsCDNames = (JSONObject)CDinSSGroup.get(destSS);

                                                                String message = sosjcmg.GenerateKillCDsMessageOfJSON(destSS, jsCDNames);

                                                                String SSAddr = RegAllSSAddr.getSSAddrOfSSName(destSS);

                                                                sendReconfigMessage(SSAddr, message);

                                                            }
                                                
                                                     } 
                                                     
                                            }
                                            System.out.println("Total time terminate Sig Chan CD : " +(totTimeTerminateSigChan)+ " ms"); 
                                            System.out.println("Total time remove CD file serv desv CD : " +(totTimeTerminateServDescRemCDFileCD)+ " ms");
                                                System.out.println("Total time kill CD : " +(totTimeTerminateSigChan+totTimeTerminateServDescRemCDFileCD)+ " ms");
                                                CDLCBuffer.ClearRequestKillCD();
                                            
                                        }
                                        
                                        //schedule migrated active cd and store hibernated cd to the appropriate data structure
                                        
                                        if((CDLCBuffer.GetStrongMigrationDoneFlag() || CDLCBuffer.GetWeakMigrationDoneFlag()) && !CDLCBuffer.getMigrationBusyFlag()){
                                            CDLCBuffer.SetCDLCMigrationFlagBusy();
                                            //JSONObject jsBffdCDMap = CDLCBuffer.getAllMigTempSignalChannelMap();
                                            if(CDLCBuffer.GetStrongMigrationDoneFlag() && CDLCBuffer.GetWeakMigrationDoneFlag()){
                                                
                                                jsLocalCDs = SJSSCDSignalChannelMap.getCurrentSignalChannelMapping().getJSONObject(keyCurrSS);
                                                
                                                //Hashtable AllMigratedCDStrong = CDObjectsBuffer.GetGroupSSCDObjStrongMig();
                                               // Hashtable AllMigratedCDWeak = CDObjectsBuffer.GetGroupSSCDObjWeakMig();
                                                Hashtable AllMigratedCDStrong = CDObjectsBuffer.GetAllCDObjToTempStrongMigBuffer();
                                                Hashtable AllMigratedCDWeak = CDObjectsBuffer.GetAllCDObjToTempWeakMigBuffer();
                                                Hashtable AllMigratedCDStrongCopy = AllMigratedCDStrong;
                                                AllMigratedCDStrong.putAll(AllMigratedCDWeak);
                                                
                                                Hashtable AllMigratedCDSS = AllMigratedCDStrong;
                                                
                                                Hashtable allCDs = new Hashtable();
                                                
                                                Enumeration keysAllMigCDs = AllMigratedCDSS.keys();
                                                while(keysAllMigCDs.hasMoreElements()){
                                                    String cdname = keysAllMigCDs.nextElement().toString();
                                                    Hashtable IndivMigratedCD = (Hashtable)AllMigratedCDSS.get(cdname);
                                                    ClockDomain migCD = (ClockDomain)IndivMigratedCD.get("CDInst");
                                                    allCDs.put(cdname, migCD);
                                                }
                                                //for strong migrated CD
                                                
                                                keysAllMigCDs = AllMigratedCDStrongCopy.keys();

                                                while(keysAllMigCDs.hasMoreElements()){
                                                    String cdname = keysAllMigCDs.nextElement().toString();

                                                    Hashtable IndivMigratedCD = (Hashtable)AllMigratedCDStrongCopy.get(cdname);

                                                        ClockDomain migCD = (ClockDomain)IndivMigratedCD.get("CDInst");
                                                        String origSSName = IndivMigratedCD.get("OrigSSName").toString();
                                                        System.out.println("migrated CDname: " +migCD.getName());

                                                    //after CD is obtaines, check the initial state from the original location
                                                        boolean isActive = false;
                                                        if(migCD.getState().equalsIgnoreCase("Active")){
                                                            isActive = true;
                                                        }
                                                        
                                                            Vector vec = cdlcimpl.AddCD(jsLocalCDs,jsLocalCDs,keyCurrSS, cdname, migCD, allCDs,"strong",origSSName,isActive,im, sc);

                                                            sc = (Scheduler)vec.get(1);
                                                            im = (InterfaceManager) vec.get(0);
                                                            JSONObject MigCDServDesc = CDLCBuffer.GetTempMigServDesc(migCD.getName());
                                                            if(!MigCDServDesc.isEmpty()){
                                                                SJServiceRegistry.AddInternalRegWithServ(MigCDServDesc);
                                                            }
                                                            
                                                    //}

                                                }
                                                
                                                //weak migration CD
                                                
                                                 keysAllMigCDs = AllMigratedCDWeak.keys();
                                                
                                                 while(keysAllMigCDs.hasMoreElements()){
                                                    String cdname = keysAllMigCDs.nextElement().toString();

                                                    Hashtable IndivMigratedCD = (Hashtable)AllMigratedCDWeak.get(cdname);
                                                    
                                                    //Enumeration keysAllMigCD = AllMigratedCD.keys();

                                                //while(keysAllMigCD.hasMoreElements()){
                                                    //String cdName = keysAllMigCD.nextElement().toString();

                                                    ClockDomain migCD = (ClockDomain)IndivMigratedCD.get("CDInst");
                                                    String origSSName = IndivMigratedCD.get("OrigSSName").toString();

                                                    System.out.println("migrated CDname: " +migCD.getName());

                                                    //after CD is obtaines, check the initial state from the original location

                                                    //if(migCD.getState().equalsIgnoreCase("Active")){

                                                        System.out.println("CDname: " +migCD.getName()+ " activated");

                                                       Vector vec =  cdlcimpl.AddCD(jsLocalCDs,jsLocalCDs,keyCurrSS, migCD.getName(), migCD, allCDs,"weak",origSSName,true,im, sc);

                                                       sc = (Scheduler)vec.get(1);
                                                       im = (InterfaceManager) vec.get(0);
                                                       
                                                       JSONObject MigCDServDesc = CDLCBuffer.GetTempMigServDesc(migCD.getName());
                                                       if(!MigCDServDesc.isEmpty()){
                                                                SJServiceRegistry.AddInternalRegWithServ(MigCDServDesc);
                                                            }
                                                       SOABuffer.setAdvVisib(migCD.getName(), "visible");
                                                    //} 
                                                    

                                                //}


                                                }
                                                
                                                
                                            } else
                                            
                                              if(CDLCBuffer.GetStrongMigrationDoneFlag()){
                                            
                                                System.out.println("New migrated CD exists");

                                                Hashtable AllMigratedCDStrong = CDObjectsBuffer.GetAllCDObjToTempStrongMigBuffer();
                                                
                                                //Hashtable AllMigratedCDSS = AllMigratedCDStrong;
                                                
                                                Hashtable allCDs = new Hashtable();
                                                
                                                Enumeration keysAllMigCDs = AllMigratedCDStrong.keys();
                                                while(keysAllMigCDs.hasMoreElements()){
                                                    String cdname = keysAllMigCDs.nextElement().toString();
                                                    Hashtable IndivMigratedCD = (Hashtable)AllMigratedCDStrong.get(cdname);
                                                    ClockDomain migCD = (ClockDomain)IndivMigratedCD.get("CDInst");
                                                    allCDs.put(cdname, migCD);
                                                }
                                                //for strong migrated CD
                                                
                                                keysAllMigCDs = AllMigratedCDStrong.keys();

                                                while(keysAllMigCDs.hasMoreElements()){
                                                    String cdname = keysAllMigCDs.nextElement().toString();

                                                    Hashtable IndivMigratedCD = (Hashtable)AllMigratedCDStrong.get(cdname);

                                                        ClockDomain migCD = (ClockDomain)IndivMigratedCD.get("CDInst");
                                                        String origSSName = IndivMigratedCD.get("OrigSSName").toString();
                                                        System.out.println("migrated CDname: " +migCD.getName());

                                                    //after CD is obtaines, check the initial state from the original location
                                                        boolean isActive = false;
                                                        if(migCD.getState().equalsIgnoreCase("Active")){

                                                            isActive = true;

                                                        }
                                                            Vector vec = cdlcimpl.AddCD(jsLocalCDs,jsLocalCDs,keyCurrSS, cdname, migCD, allCDs,"strong",origSSName,isActive,im, sc);

                                                            sc = (Scheduler)vec.get(1);
                                                            im = (InterfaceManager) vec.get(0);
                                                       JSONObject MigCDServDesc = CDLCBuffer.GetTempMigServDesc(migCD.getName());
                                                       if(!MigCDServDesc.isEmpty()){
                                                                SJServiceRegistry.AddInternalRegWithServ(MigCDServDesc);
                                                            }
                                                       SOABuffer.setAdvVisib(migCD.getName(), "visible");
                                                    //}

                                                }
                                               
                                                //Hashtable AllMigratedCD = CDObjectsBuffer.TakeAllCDObjFromTempStrongMigBuffer();

                                                CDLCBuffer.ResetStrongMigrationDoneFlag();
                                            } else

                                            // weak mig

                                            if(CDLCBuffer.GetWeakMigrationDoneFlag()){

                                                System.out.println("New migrated CD exists");

                                                Hashtable AllMigratedCDWeak = CDObjectsBuffer.GetAllCDObjToTempWeakMigBuffer();

                                                 Hashtable allCDs = new Hashtable();
                                                
                                                Enumeration keysAllMigCDs = AllMigratedCDWeak.keys();
                                                while(keysAllMigCDs.hasMoreElements()){
                                                    String cdname = keysAllMigCDs.nextElement().toString();
                                                    Hashtable IndivMigratedCD = (Hashtable)AllMigratedCDWeak.get(cdname);
                                                    ClockDomain migCD = (ClockDomain)IndivMigratedCD.get("CDInst");
                                                    allCDs.put(cdname, migCD);
                                                }
                                                keysAllMigCDs = AllMigratedCDWeak.keys();
                                                while(keysAllMigCDs.hasMoreElements()){
                                                    String cdname = keysAllMigCDs.nextElement().toString();

                                                    Hashtable IndivMigratedCD = (Hashtable)AllMigratedCDWeak.get(cdname);

                                                    //Enumeration keysAllMigCD = AllMigratedCD.keys();

                                                //while(keysAllMigCD.hasMoreElements()){
                                                    //String cdName = keysAllMigCD.nextElement().toString();

                                                    ClockDomain migCD = (ClockDomain)IndivMigratedCD.get("CDInst");
                                                    String origSSName = IndivMigratedCD.get("OrigSSName").toString();
                                                    String CDName = migCD.getName();
                                                    System.out.println("migrated CDname: " +CDName);

                                                    //after CD is obtaines, check the initial state from the original location

                                                   // if(migCD.getState().equalsIgnoreCase("Active")){

                                                       Vector vec =  cdlcimpl.AddCD(jsLocalCDs, jsLocalCDs,keyCurrSS, CDName, migCD, allCDs,"weak",origSSName, true,im, sc);

                                                       sc = (Scheduler)vec.get(1);
                                                       im = (InterfaceManager) vec.get(0);
                                                       JSONObject MigCDServDesc = CDLCBuffer.GetTempMigServDesc(CDName);
                                                       if(!MigCDServDesc.isEmpty()){
                                                                SJServiceRegistry.AddInternalRegWithServ(MigCDServDesc);
                                                            }
                                                       SOABuffer.setAdvVisib(migCD.getName(), "visible");
                                                    //} 
                                                    
                                                //}

                                                }

                                                CDLCBuffer.ResetWeakMigrationDoneFlag();
                                            }
                                            CDLCBuffer.SetCDLCMigrationFlagFree();
                                        }
                                        
                                        // strong mig
                                        
                                        //if(!CDLCBuffer.ISCDLCMigrationThreadBusy()){
                                            if(!CDLCBuffer.IsRequestMigrateEmpty()){
                                                Hashtable ReqMigrate = CDLCBuffer.GetRequestMigrate();
                                                Enumeration keysReqMig = ReqMigrate.keys();
                                                while(keysReqMig.hasMoreElements()){
                                                  String DestSS = keysReqMig.nextElement().toString();
                                                  if(!CDLCBuffer.IsOccuringMigDestSS(DestSS)){
                                                      JSONObject hash = (JSONObject) ReqMigrate.get(DestSS);
                                                        Enumeration keysHash = hash.keys();
                                                        while(keysHash.hasMoreElements()){
                                                          String migType = keysHash.nextElement().toString();
                                                          if(!RegAllSSAddr.getSSAddrOfSSName(DestSS).equalsIgnoreCase("0.0.0.0")){
                                                              String Addr = RegAllSSAddr.getSSAddrOfSSName(DestSS);
                                                              JSONObject CDLists = (JSONObject) hash.get(migType);
                                                              Vector vecAllCDName = (Vector) CDLists.get("CDNameList");
                                                              
                                                              sc = MigHandShake(Addr, DestSS, migType, vecAllCDName, sc, im);
                                                           
                                                             
                                                          } else {
                                                              CDLCBuffer.RemoveReqMigrate(DestSS, migType);
                                                          }
                                                      }
                                                  }
                                             }
                                           }
                                        //}
                } catch (JSONException ex) {
                    ex.printStackTrace();
                } catch (Exception ex){
                    ex.printStackTrace();
                }
    
                SOABuffer.SetNotifyChangedCDStat(true);
                SOABuffer.SetRegNotifySS(true);
                
                vecSCIM.addElement(im);
                vecSCIM.addElement(sc);
                
                return vecSCIM;
        }
    
    private void sendReconfigMessage(String ipAddr, String message){
        try {
            byte[] msg = new byte[65508];
            InetAddress ipAddress = InetAddress.getByName(ipAddr);
            int infoDebug=1;
            //JSONObject js = new JSONObject(message);
              
               ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
               ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(byteStream));
              //out.writeObject(SJServiceRegistry.obtainInternalRegistry().toString());
               out.writeObject(message);
               //out.writeObject(SJServiceRegistry.AdvertiseNodeServices("HelloMessage").toString()); //put service description to be sent to remote devices
               out.flush();
                msg = byteStream.toByteArray();
                out.close(); 
                MulticastSocket s = new MulticastSocket(2120);
                DatagramPacket hi = new DatagramPacket(msg, msg.length, ipAddress, 2120);
                if (infoDebug ==1 )System.out.println("Sending control message:" +message+ "to: " +ipAddress );
                s.send(hi);
                if (infoDebug ==1 ) System.out.println("data has been sent!");
                                          
                //SJServiceRegistry.AcknowledgeHelloMessageSent(true);
                 // if (infoDebug ==1 ) System.out.println("Status acknowledge in sender:" +SJServiceRegistry.getAcknowledgeHelloMessageSent());
                                         // SJServiceRegistry.RecordAdvertisementTimeStamp();
                s.close();
        } catch (UnknownHostException hex) {
            
            System.err.println("ControlMessage, problem IOException: " +hex.getMessage());
        } catch (Exception e){
            System.err.println("ControlMessage, problem Exception: " +e.getMessage());
        }
    }
    
    private Scheduler MigHandShake(String destAddr,String destSS,String migType, Vector vecAllCDName, Scheduler sc, InterfaceManager im){
        ServerSocket ss = null;
        JSONObject jsAllCDMaps = SJSSCDSignalChannelMap.getCurrentSignalChannelMapping();
        ServerSocket ssRecMigResp = null;
        String localSSName = SJSSCDSignalChannelMap.getLocalSSName();
        JSONObject jsLocalCDs = new JSONObject();
        try {
            jsLocalCDs = jsAllCDMaps.getJSONObject(localSSName);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        SJSOAMessage sjsoa = new SJSOAMessage();
            try {
                  ss = new ServerSocket(0);
              } catch (Exception e) {
                  e.printStackTrace();
            }
            try{
                ssRecMigResp = new ServerSocket(0);
            } catch (Exception ex){
                ex.printStackTrace();
            }
            String strRecPort = Integer.toString(ss.getLocalPort());
            String recvMsgPort = Integer.toString(ssRecMigResp.getLocalPort());
            String migReqMsg = sjsoa.ConstructReqMigrationMessage(destSS,migType,strRecPort,recvMsgPort,localSSName);
            boolean initQuery = true;
            while(initQuery){
                
                    JSONObject resp = QueryIsMigAvail(destAddr,migReqMsg, ssRecMigResp);
                    
               
                    if(!resp.isEmpty()){
                       String answer = null; 
                        try {
                            answer = resp.getString("data");
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                        if(answer.equalsIgnoreCase("OK")){
                            CDLCBuffer.SetCDLCMigrationFlagBusy();
                               Vector allMigObjCD = new Vector();
                                                         for(int num=0;num<vecAllCDName.size();num++){
                                                             String cdname = (String)vecAllCDName.get(num);
                                                             RegAllCDStats.AddIntCDMacroState(cdname,"Migrating");
                                                             if(sc.SchedulerHasCD(cdname)){
                                                                 ClockDomain cdins = sc.getClockDomain(cdname);
                                                                 try {
                                                                     removeInSigThread(jsLocalCDs, cdname, cdins);
                                                                 } catch (Exception ex) {
                                                                     ex.printStackTrace();
                                                                 }
                                                                 allMigObjCD.addElement(cdins);
                                                                 sc.removeClockDomain(cdname);
                                                             } else if (CDObjectsBuffer.CDObjBufferHas(cdname)){
                                                                 ClockDomain cdins = CDObjectsBuffer.GetCDInstancesFromBuffer(cdname);
                                                                 allMigObjCD.addElement(cdins);
                                                                 CDObjectsBuffer.RemoveCDInstancesFromBuffer(cdname);
                                                             }
                                                             
                                                             SOABuffer.removeAdvStatOfCDName(cdname);
                                                         }
                                     CDLCBuffer.AddOccuringMigDestSS(destSS);
                                     int recvPort=0;
                                    try {
                                        recvPort = Integer.parseInt(resp.getString("recPort"));
                                    } catch (JSONException ex) {
                                        ex.printStackTrace();
                                    }
                                     Thread th = new Thread(new MigTransferThread(migType, destAddr, destSS, ss, recvPort, vecAllCDName, allMigObjCD, im));
                                     th.start();
                                     CDLCBuffer.RemoveReqMigrate(destSS, migType);
                              
                        }
                        initQuery = false;
                    } else {
                        try {
                            ss.close();
                            ssRecMigResp.close();
                        } catch (IOException ex) {
                           ex.printStackTrace();
                        }
                        initQuery = false;
                    }
            }
            return sc;
    }
    
   private JSONObject QueryIsMigAvail(String destAddress, String reqMigMsg, ServerSocket s2){
        JSONObject response = new JSONObject();
                    try
                    {
                        InetAddress ipAddress = InetAddress.getByName(destAddress);
                        byte[] msg = new byte[65507];
                        MulticastSocket s1 = new MulticastSocket(1078);
                        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                        ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(byteStream));
                        out.writeObject(reqMigMsg);
                        out.flush();
                        msg = byteStream.toByteArray();
                        out.close();
                        DatagramPacket req = new DatagramPacket(msg, msg.length, ipAddress,1078);
                        s1.send(req);
                        Socket socketReceiveMigResp = s2.accept();
                        ObjectInputStream sInt = new ObjectInputStream(socketReceiveMigResp.getInputStream());
                        String respMsg = sInt.readUTF();
                        response = new JSONObject(new JSONTokener(respMsg));
                        sInt.close();
                        socketReceiveMigResp.close();
                        s2.close();
                        s1.close();
                    }
                    catch (java.net.BindException bex){
                        bex.printStackTrace();
                    }
                    catch (Exception e)
                    {
			System.out.println("ControlMessage: Problem when connecting : "+ e.getMessage());
			e.printStackTrace();
                    }
                return response;    
    }
   
   private void removeInSigThread(JSONObject jsLocalCDs,String keyCDName, ClockDomain cdins) throws JSONException, Exception
        {
                                                JSONObject jsSigsChans = jsLocalCDs.getJSONObject(keyCDName);
                                               
                                                            JSONObject jsSigs = jsSigsChans.getJSONObject("signals");
                                                            JSONObject jsSigsInputs = jsSigs.getJSONObject("inputs");
                                                            Enumeration keysSigsInputsName = jsSigsInputs.keys();
                                                            while (keysSigsInputsName.hasMoreElements()){
                                                                String SigsInputsName = keysSigsInputsName.nextElement().toString();
                                                                Field f = cdins.getClass().getField(SigsInputsName);
                                                                Signal signal = (Signal)f.get(cdins); 
                                                                signal.killInputSignalThread();
                                                                if(signal.IsInputSignalPhyThreadContLoop()){
                                                                    while(!signal.getInputSignalThreadTerminated()){}
                                                                }
                                                                //while(!signal.getInputSignalThreadTerminated()){}
                                                            }
                                                            JSONObject jsSigsOutputs = jsSigs.getJSONObject("outputs");
                                                            Enumeration keysSigsOutputsName = jsSigsOutputs.keys();
                                                            while (keysSigsOutputsName.hasMoreElements()){
                                                                String SigsOutputsName = keysSigsOutputsName.nextElement().toString();
                                                                Field f = cdins.getClass().getField(SigsOutputsName);
                                                                Signal signal = (Signal)f.get(cdins); 
                                                                GenericSignalSender gss = signal.getClient();
                                                                //String phySigName = gss.getPhySigName();
                                                                //if(phySigName.equalsIgnoreCase("gpioWriter")){
                                                                    gss.resetPhySig();
                                                                //}
                                                            }
                                                
        }
    
}

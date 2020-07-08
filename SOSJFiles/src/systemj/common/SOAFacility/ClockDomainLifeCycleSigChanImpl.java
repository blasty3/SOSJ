/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package systemj.common.SOAFacility;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.nio.channels.Channels;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.json.me.JSONTokener;
import systemj.bootstrap.ClockDomain;
import systemj.common.CDLCBuffer;
import systemj.common.CDObjectsBuffer;
import systemj.common.IMBuffer;
import systemj.common.Interconnection;
import systemj.common.InterfaceManager;
import systemj.common.RegAllCDStats;
import systemj.common.RegAllSSAddr;
import systemj.common.SJSOAMessage;
import systemj.common.SJSSCDSignalChannelMap;
import systemj.common.SJServiceRegistry;
import systemj.common.SOAFacility.Support.SOABuffer;
import systemj.common.SchedulersBuffer;
//import systemj.common.SignalObjBuffer;
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
public class ClockDomainLifeCycleSigChanImpl {
    
     public Vector ReconfigInvServInChannel(String inchanName, String inchanCDName, String partnerCDName, String partnerChanName, String partnerCDSSLoc, Scheduler sc, InterfaceManager im){
         
         Vector vec = new Vector();     
         try{
             
             String localSSName = SJSSCDSignalChannelMap.getLocalSSName();
         
             JSONObject jsAllMap = SJSSCDSignalChannelMap.getCurrentSignalChannelMapping();
             JSONObject jsLocalCDMap = jsAllMap.getJSONObject(localSSName);
             Hashtable channels = im.getAllChannelInstances();

             if(sc.SchedulerHasCD(inchanCDName)){

                 ClockDomain cdInChan = sc.getClockDomain(inchanCDName);
                 
                 Field f = cdInChan.getClass().getField(inchanName+"_in");
                   input_Channel inchan = (input_Channel)f.get(cdInChan);
                   
                   im.addCDLocation(partnerCDSSLoc, partnerCDName);
                   inchan.PartnerName=partnerCDName+"."+partnerChanName+"_o";
                   inchan.setDistributed();
                   inchan.setInterfaceManager(im);
                   channels.put(inchan.Name, inchan);
                   
                   sc.updateClockDomain(cdInChan, inchanCDName);

             } else if (CDObjectsBuffer.CDObjBufferHas(inchanCDName)){

                 ClockDomain cdInChan = CDObjectsBuffer.GetCDInstancesFromBuffer(inchanCDName);
                 
                 Field f = cdInChan.getClass().getField(inchanName+"_in");
                   input_Channel inchan = (input_Channel)f.get(cdInChan);
                   
                   im.addCDLocation(partnerCDSSLoc, partnerCDName);
                   inchan.PartnerName=partnerCDName+"."+partnerChanName+"_o";
                   inchan.setDistributed();
                   inchan.setInterfaceManager(im);
                   channels.put(inchan.Name, inchan);
                   
                   CDObjectsBuffer.AddCDInstancesToBuffer(inchanCDName, cdInChan);

             }
             
             JSONObject jsCD = jsLocalCDMap.getJSONObject(inchanCDName);
             JSONObject jsAllChan = jsCD.getJSONObject("SChannels");
             JSONObject jsInChans = jsAllChan.getJSONObject("inputs");
             JSONObject jsInChan = jsInChans.getJSONObject(inchanName);
             String newOrig = partnerCDName+"."+partnerChanName;
             jsInChan.put("From", newOrig);
             jsInChans.put(inchanName, jsInChan);
             jsAllChan.put("inputs", jsInChans);
             jsCD.put("SChannels", jsAllChan);
             jsLocalCDMap.put(inchanCDName, jsCD);
             SJSSCDSignalChannelMap.UpdateLocalCurrSignalChannelMap(localSSName, jsLocalCDMap);
             //jsAllMap.put(localSSName, jsLocalCDMap);
             
            // SJSSCDSignalChannelMap.UpdateAllCurrSignalChannelMapping(jsAllMap);
             
             im.setChannelInstances(channels);
             
         } catch (JSONException jex){
             jex.printStackTrace();
         } catch (Exception ex){
             ex.printStackTrace();
         }
         
         vec.addElement(im);
         vec.addElement(sc);
         
         return vec;
         
     }
    
    public Vector ReconfigInvServChannel(String CDSenderName, String ChanSenderName, String CDReceiverName,String ChanReceiverName, boolean isLocal,Scheduler sc, InterfaceManager im){
      Vector vec = new Vector();     
      ClockDomain cdSender = sc.getClockDomain(CDSenderName);
      String localSSName = SJSSCDSignalChannelMap.getLocalSSName();
      output_Channel ochan;
        try {
            
            Field f = cdSender.getClass().getField(ChanSenderName+"_o");
            ochan = (output_Channel)f.get(cdSender);
            String partnerInChan = CDReceiverName+"."+ChanReceiverName+"_in";
            
                if(isLocal){
                   ClockDomain cdReceiver=null;
                   if(sc.SchedulerHasCD(CDReceiverName)){
                     cdReceiver = sc.getClockDomain(CDReceiverName);
                   } else if (CDObjectsBuffer.CDObjBufferHas(CDReceiverName)){
                     cdReceiver = CDObjectsBuffer.GetCDInstancesFromBuffer(CDReceiverName);
                   }
                        input_Channel inchan;
                        Field f2 = cdReceiver.getClass().getField(ChanReceiverName+"_in");
                        inchan = (input_Channel)f2.get(cdReceiver);
                        if(!inchan.getOccupiedStat()){
                            if(partnerInChan.equals(ochan.PartnerName)){
                                inchan.setOccupiedStat(true);  
                                CDLCBuffer.AddInvStatChanReconfig(CDSenderName, ChanSenderName, true);
                            } else {
                                String OldOChanPartnerCDName  = ochan.PartnerName.split("\\.")[0];
                                String OldOChanPartnerChanName = ochan.PartnerName.split("\\.")[1].split("_")[0];
                                
                                JSONObject jsCurrMap = SJSSCDSignalChannelMap.getCurrentSignalChannelMapping();
                                JSONObject jsLocalCDs = jsCurrMap.getJSONObject(localSSName);
                                if(!OldOChanPartnerCDName.equalsIgnoreCase("")){
                                    if(jsLocalCDs.has(OldOChanPartnerCDName)){
                                        JSONObject jsCDOldOChanConf = jsLocalCDs.getJSONObject(OldOChanPartnerCDName);
                                        JSONObject jsChanCDOldOChanConf = jsCDOldOChanConf.getJSONObject("SChannels");
                                        ClockDomain cdOldOChanPartner = null;
                                        if(sc.SchedulerHasCD(OldOChanPartnerCDName)){
                                          cdOldOChanPartner = sc.getClockDomain(OldOChanPartnerCDName);
                                        } else if(CDObjectsBuffer.CDObjBufferHas(OldOChanPartnerCDName)){
                                          cdOldOChanPartner = CDObjectsBuffer.GetCDInstancesFromBuffer(OldOChanPartnerCDName);
                                        }
                                      JSONObject jsInChansCDOldOChanConf = jsChanCDOldOChanConf.getJSONObject("inputs");
                                      JSONObject jsInChanCDOldOChanConf = jsInChansCDOldOChanConf.getJSONObject(OldOChanPartnerChanName);
                                      input_Channel OldOChanPartner;
                                      Field f3 = cdOldOChanPartner.getClass().getField(OldOChanPartnerChanName+"_in");
                                      OldOChanPartner = (input_Channel)f3.get(cdOldOChanPartner);
                                      OldOChanPartner.PartnerName = ".";
                                      OldOChanPartner.set_partner_smp(new output_Channel());
                                      jsInChanCDOldOChanConf.put("From", ".");
                                      jsInChansCDOldOChanConf.put(OldOChanPartnerChanName, jsInChanCDOldOChanConf);
                                      jsChanCDOldOChanConf.put("inputs", jsInChansCDOldOChanConf);
                                      jsCDOldOChanConf.put("SChannels", jsChanCDOldOChanConf);
                                      jsLocalCDs.put(OldOChanPartnerCDName, jsCDOldOChanConf);
                                  } else {
                                      System.out.println("old partner not local: " +OldOChanPartnerCDName );
                                      ochan.TransmitPartnerReconfigChanChanges(".");
                                  }
                                   
                                } 
                                  
                                //* change 20 Jan 2017 to refresh channel communication status before being reconfigured
                                inchan.refresh();
                                //* end change 20 Jan 2017 refresh channel comm status
                                
                                    JSONObject jsCDSenderConf = jsLocalCDs.getJSONObject(CDSenderName);
                                    JSONObject jsCDReceiverConf = jsLocalCDs.getJSONObject(CDReceiverName);
                                    JSONObject jsChanCDSenderConf = jsCDSenderConf.getJSONObject("SChannels");
                                    JSONObject jsChanCDReceiverConf = jsCDReceiverConf.getJSONObject("SChannels");
                                    JSONObject jsOChansCDSenderConf = jsChanCDSenderConf.getJSONObject("outputs");
                                    JSONObject jsInChansCDReceiverConf = jsChanCDReceiverConf.getJSONObject("inputs");
                                    JSONObject jsOChanCDSenderConf = jsOChansCDSenderConf.getJSONObject(ChanSenderName);
                                    JSONObject jsInChanCDReceiverConf = jsInChansCDReceiverConf.getJSONObject(ChanReceiverName);
                                inchan.PartnerName = CDSenderName+"."+ChanSenderName+"_o";
                                ochan.PartnerName = CDReceiverName+"."+ChanReceiverName+"_in";
                                if(!ochan.IsChannelLocal()){
                                    ochan.setLocal();
                                }
                                if(!inchan.IsChannelLocal()){
                                    inchan.setLocal();
                                }
                                inchan.setOccupiedStat(true);  
                                inchan.set_partner_smp(ochan);
                                ochan.set_partner_smp(inchan);
                                Hashtable channels = im.getAllChannelInstances();
                                channels.put(inchan.Name,inchan);
                                channels.put(ochan.Name, ochan);
                                jsOChanCDSenderConf.put("To", CDReceiverName+"."+ChanReceiverName);
                                jsInChanCDReceiverConf.put("From",CDSenderName+"."+ChanSenderName);
                                jsOChansCDSenderConf.put(CDSenderName,jsOChanCDSenderConf);
                                jsInChansCDReceiverConf.put(CDReceiverName, jsInChanCDReceiverConf);
                                jsChanCDSenderConf.put("outputs", jsOChansCDSenderConf);
                                jsChanCDReceiverConf.put("inputs", jsInChansCDReceiverConf);
                                jsCDSenderConf.put("SChannels", jsChanCDSenderConf);
                                jsCDReceiverConf.put("SChannels", jsChanCDReceiverConf);
                                jsLocalCDs.put(CDSenderName, jsCDSenderConf);
                                jsLocalCDs.put(CDReceiverName, jsCDReceiverConf);
                                
                                //jsCurrMap.put(localSSName, jsLocalCDs);
                                //SJSSCDSignalChannelMap.UpdateAllCurrSignalChannelMapping(jsCurrMap);
                                SJSSCDSignalChannelMap.AddOneSSToCurrSignalChanMapping(localSSName, jsLocalCDs);
                                CDLCBuffer.AddInvStatChanReconfig(CDSenderName, ChanSenderName, true);
                                sc.updateClockDomain(cdSender, CDSenderName);
                                if(sc.SchedulerHasCD(CDReceiverName)){
                                    sc.updateClockDomain(cdReceiver, CDReceiverName);
                                } else if(CDObjectsBuffer.CDObjBufferHas(CDReceiverName)){
                                    CDObjectsBuffer.AddCDInstancesToBuffer(CDReceiverName, cdReceiver);
                                }
                                im.setChannelInstances(channels);
                                }
                        } else {
                            CDLCBuffer.AddInvStatChanReconfig(CDSenderName, ChanSenderName, false);
                        }
                } else {
                    
                     String ssDest = null;
                     if(im.hasCDLocation(CDReceiverName)){
                        ssDest = im.getCDLocation(CDReceiverName);
                     } else {
                        JSONObject jsAllRCDSt = RegAllCDStats.getAllCDStats();
                        Enumeration keysjsAllRCDSt = jsAllRCDSt.keys();
                        while(keysjsAllRCDSt.hasMoreElements()){
                            String indivSSKey = keysjsAllRCDSt.nextElement().toString();
                            JSONObject jsCDStats = jsAllRCDSt.getJSONObject(indivSSKey);
                            if(jsCDStats.has(CDReceiverName)){
                                ssDest = indivSSKey;
                                break;
                            }
                        }
                     }
                       //
                      
                                                            boolean linkCreated = false;
                                                            Interconnection ic = im.getInterconnection();
                                                            Vector availRemoteLink = ic.getRemoteDestinationInterfaces(ssDest);
                                                            Vector availOwnLink = ic.getRemoteDestinationInterfaces(localSSName);
                                                            //if(RegAllSSAddr.IsSSAddrOfSSNameAvail(ssDest)){
                                                                
                                                            //} else {
                                                            //    h
                                                           // }
                                                            if(availRemoteLink.size()==0 || availOwnLink.size()==0){
                                                                LinkCreationSender lcsh = new LinkCreationSender();
                                                                String destAddr = RegAllSSAddr.getSSAddrOfSSName(ssDest);
                                                                while(true){
                                                                    String linkCreationResp = lcsh.SendLinkCreationReq(SJSSCDSignalChannelMap.getLocalSSName(), ssDest, destAddr);
                                                                        if(linkCreationResp.equals("OK")){
                                                                                im = lcsh.ExecuteLinkCreation(SJSSCDSignalChannelMap.getLocalSSName(), destAddr, im);
                                                                                linkCreated = true;
                                                                                break;
                                                                        } else if(linkCreationResp.equals("NOT OK")){
                                                                                CDLCBuffer.AddInvStatChanReconfig(CDSenderName, ChanSenderName, false);
                                                                                break;
                                                                        }
                                                                }
                                                            }
                        if(linkCreated || availRemoteLink.size()>0){
                              String destAddr = RegAllSSAddr.getSSAddrOfSSName(ssDest);
                                //System.out.println("ChanRecName3: " +CDReceiverName+"."+ChanReceiverName);
                                //System.out.println("ChanSenderName3: " +ochan.Name);
                                
                               // String localSSName = SJSSCDSignalChannelMap.getLocalSSName();
                                //ochan.TransmitOccupyChan(CDReceiverName+"."+ChanReceiverName+"_in", localSSName);
                                int PortNum = TCPIPLinkRegistry.GetSSNamePortNum(ssDest);
                                Object[] toSend = new Object[4];
                                toSend[0] = CDReceiverName+"."+ChanReceiverName+"_in";              
                                toSend[1] = ochan.Name;
                                toSend[2] = "OccupyChan";
                                toSend[3] = localSSName;
                                //System.out.println("trans");
                                TransmitOccupyChan(toSend, destAddr, PortNum);
                                while(true){
                                    boolean test = ochan.CheckRcvdQueryRspMsg();
                                    if(test){
                                        break;
                                    }
                                }
                               boolean RespOccupiedQuery = ochan.GetRespOccupyQuery();
                               if(RespOccupiedQuery){
                                   //System.out.println("ClockDomainLifeCycleSigChanImpl debug, RespOccupiedQuery, new ochan Partner: " +CDReceiverName+"."+ChanReceiverName);
                                    ochan.PartnerName = CDReceiverName+"."+ChanReceiverName+"_in";
                                    if(ochan.IsChannelLocal()){
                                        ochan.setDistributedReconfiguration();
                                        ochan.setInterfaceManager(im);
                                    }
                                    Hashtable channels = im.getAllChannelInstances();
                                    channels.put(ochan.Name, ochan);
                                    im.setChannelInstances(channels);
                                    JSONObject jsCurrMap = SJSSCDSignalChannelMap.getCurrentSignalChannelMapping();
                                    JSONObject jsLocalCDs = jsCurrMap.getJSONObject(localSSName);
                                    JSONObject jsCDSenderConf = jsLocalCDs.getJSONObject(CDSenderName);
                                    //JSONObject jsCDReceiverConf = jsLocalCDs.getJSONObject(CDReceiverName);
                                    JSONObject jsChanCDSenderConf = jsCDSenderConf.getJSONObject("SChannels");
                                    //JSONObject jsChanCDReceiverConf = jsCDReceiverConf.getJSONObject("SChannels");
                                    JSONObject jsOChanCDSenderConf = jsChanCDSenderConf.getJSONObject("outputs");
                                    jsOChanCDSenderConf.put("To", CDReceiverName+"."+ChanReceiverName);
                                    jsChanCDSenderConf.put("outputs", jsOChanCDSenderConf);
                                    jsCDSenderConf.put("SChannels", jsChanCDSenderConf);
                                    //jsCDReceiverConf.put("SChannels", jsChanCDReceiverConf);
                                    jsLocalCDs.put(CDSenderName, jsCDSenderConf);
                                    SJSSCDSignalChannelMap.UpdateLocalCurrSignalChannelMap(localSSName,jsLocalCDs);
                                    //jsCurrMap.put(localSSName, jsLocalCDs);
                                   // SJSSCDSignalChannelMap.UpdateAllCurrSignalChannelMapping(jsCurrMap);  
                                    CDLCBuffer.AddInvStatChanReconfig(CDSenderName, ChanSenderName, true);
                                    sc.updateClockDomain(cdSender, CDSenderName);
                                } else {
                                    CDLCBuffer.AddInvStatChanReconfig(CDSenderName, ChanSenderName, false);
                                }
                        }       
                }
        } catch (Exception ex) {
            ex.printStackTrace();
        } 
        vec.addElement(im);
        vec.addElement(sc);
        return vec;
    }
    
    /*
    public Vector ResetOccupiedInChan(Scheduler sc, InterfaceManager im){
         
         Vector vec = new Vector();      

         
         try{
             
             String localSSName = SJSSCDSignalChannelMap.getLocalSSName();
         
             JSONObject jsAllMap = SJSSCDSignalChannelMap.getCurrentSignalChannelMapping();
             JSONObject jsLocalCDMap = jsAllMap.getJSONObject(localSSName);
            // Hashtable channels = im.getAllChannelInstances();
             
             
             Enumeration keysjsIndivCDMaps = jsLocalCDMap.keys();
             
             while(keysjsIndivCDMaps.hasMoreElements()){
                 
                 String CDName = keysjsIndivCDMaps.nextElement().toString();
                 
                 JSONObject jsAllCDSigChans = jsLocalCDMap.getJSONObject(CDName);
                 
                 JSONObject jsAllCDChans = jsAllCDSigChans.getJSONObject("SChannels");
                 
                 JSONObject jsAllCDInChans = jsAllCDChans.getJSONObject("inputs");
                 
                 if(!jsAllCDInChans.isEmpty()){
                     
                     Enumeration keysjsAllCDInChans = jsAllCDInChans.keys();
                     
                     while(keysjsAllCDInChans.hasMoreElements()){
                         
                         String ChanName = keysjsAllCDInChans.nextElement().toString();
                         
                         JSONObject jsInChan = jsAllCDInChans.getJSONObject(ChanName);
                         
                         String OutPartner = jsInChan.getString("From");
                         
                         String CDOut = OutPartner.split("\\.")[0];
                         
                         JSONObject jsAllCDStats = RegAllCDStats.getAllCDStats();
                         
                         Enumeration keysSSCDStats = jsAllCDStats.keys();
                         
                         while(keysSSCDStats.hasMoreElements()){
                             
                             String SSName = keysSSCDStats.nextElement().toString();
                             
                             JSONObject jsCDStats = jsAllCDStats.getJSONObject(SSName);
                             
                             if(jsCDStats.has(CDOut)){
                                 
                                 String CDstat = jsCDStats.getString(CDOut);
                                 
                                 if(CDstat.equalsIgnoreCase("Killed")){
                                     
                                     if(sc.SchedulerHasCD(CDName)){
                                        
                                         ClockDomain cdInChan = sc.getClockDomain(CDName);
                                         
                                         Field f = cdInChan.getClass().getField(ChanName+"_in");
                                         input_Channel inchan = (input_Channel)f.get(cdInChan);
                                         
                                         inchan.setOccupiedStat(false);
                                         inchan.forcePreempt();
                                         
                                     } else if (CDObjectsBuffer.CDObjBufferHas(CDName)){
                                         
                                         ClockDomain cdInChan = CDObjectsBuffer.GetCDInstancesFromBuffer(CDName);
                                         
                                         Field f = cdInChan.getClass().getField(ChanName+"_in");
                                         input_Channel inchan = (input_Channel)f.get(cdInChan);
                                         
                                         inchan.setOccupiedStat(false);
                                         inchan.forcePreempt();
                                         
                                     }
                                     
                                     
                                 }
                                 
                             }
                             
                         }
                         
                     }
                     
                 }
                 
             }
             
             

            
             
         } catch (JSONException jex){
             jex.printStackTrace();
         } catch (Exception ex){
             ex.printStackTrace();
         }
         
         vec.addElement(im);
         vec.addElement(sc);
         
         return vec;
         
     }
    */
    //if new partner is Local
    
    public Vector ReconfigPartnerChannel(JSONObject jsLocalCDs, String keyCurrSS, String CDName, String ChanName, String PartnerChanDir, String PartnerChanName, String PartnerChanCDName, String partnerSS,InterfaceManager im, Scheduler sc)        
    {
       
        Vector vec = new Vector();
        if(!partnerSS.equals("")){
            im.addCDLocation(partnerSS, PartnerChanCDName);
        }
        try{
            ClockDomain cdObj = null;
                    Hashtable channels = im.getAllChannelInstances();
            
                                                    //ClockDomain cdins = SJSSCDSignalChannelMap.GetCDInstancesFromMap(keyCDName);

                                                    //JSONObject jsSigsChans = jsLocalCDs.getJSONObject(migCDName);
                                                    if(jsLocalCDs.has(PartnerChanCDName) || keyCurrSS.equals(im.getCDLocation(PartnerChanCDName))){
                                                        
                                                        JSONObject jsSigsChans = jsLocalCDs.getJSONObject(PartnerChanCDName);

                                                        
                                                                JSONObject jsSigsChansInd = jsSigsChans.getJSONObject("SChannels");
                      
                                                            //Enumeration keysSChansInOuts = jsSigsChansInd.keys();

                                                                if(PartnerChanDir.equalsIgnoreCase("input")){
                                                                    
                                                                    JSONObject jsSChansInputs = jsSigsChansInd.getJSONObject("inputs");
                                                                    
                                                                    JSONObject SChansInputConfigs = jsSChansInputs.getJSONObject(PartnerChanName);
                                                                    
                                                                    String cname = PartnerChanName+"_in";
                                                                    
                                                                    if(sc.SchedulerHasCD(PartnerChanCDName)){
                                                                        cdObj = sc.getClockDomain(PartnerChanCDName);
                                                                    } else if (CDObjectsBuffer.CDObjBufferHas(PartnerChanCDName)){
                                                                        cdObj = CDObjectsBuffer.GetCDInstancesFromBuffer(PartnerChanCDName);
                                                                    }
                                                                                                input_Channel inchan;

                                                                                                Field fInchanOld = cdObj.getClass().getField(cname);

                                                                                                inchan = (input_Channel)fInchanOld.get(cdObj);
                                                                                                
                                                                                                //inchan.set_preempted();
                                                                                                
                                                                                                    //String Origin = SChansInputConfigs.getString("From");
                                                                        
                                                                    
                                                                                           //if(!Origin.equalsIgnoreCase(".")){

                                                                                           // String OrigCDName = Origin.split("\\.")[0];

                                                                                            //String OrigPartChanNameO = Origin.split("\\.")[1]+"_o";
                                                                                                /*
                                                                                                if(jsLocalCDs.has(OrigCDName) || keyCurrSS.equals(im.getCDLocation(OrigCDName))){

                                                                                                   ClockDomain partnercdOld = null;

                                                                                                   if(sc.SchedulerHasCD(OrigCDName)){
                                                                                                       partnercdOld = sc.getClockDomain(OrigCDName);
                                                                                                   } else if(CDObjectsBuffer.CDObjBufferHas(OrigCDName)){
                                                                                                       partnercdOld = CDObjectsBuffer.GetCDInstancesFromBuffer(OrigCDName);
                                                                                                   }
                                                                                                   
                                                                                                   JSONObject jsOldPartSigsChans = jsLocalCDs.getJSONObject(OrigCDName);

                                                                                                    Enumeration keysOldPartSigsChans = jsOldPartSigsChans.keys();

                                                                                                    while (keysOldPartSigsChans.hasMoreElements()){

                                                                                                        String tagOldPartSigsChans = keysOldPartSigsChans.nextElement().toString();

                                                                                                        if (tagOldPartSigsChans.equalsIgnoreCase("SChannels")){

                                                                                                            JSONObject jsOldPartSigsChansInd = jsOldPartSigsChans.getJSONObject(tagOldPartSigsChans);

                                                                                                        //Enumeration keysSChansInOuts = jsSigsChansInd.keys();
                                                                                                                output_Channel ochanOld;
                                                                                                                    Field fOChanOld = partnercdOld.getClass().getField(OrigPartChanNameO);
                                                                                                                    ochanOld = (output_Channel)fOChanOld.get(partnercdOld);

                                                                                                                    //ochanOld.set_preempted();

                                                                                                                    ochanOld.PartnerName = ".";


                                                                                                                    //inchan.set_partner_smp(new output_Channel());
                                                                                                                    ochanOld.set_partner_smp(new input_Channel());
                                                                                                           

                                                                                                                JSONObject jsOldPartSChansOutputs = jsOldPartSigsChansInd.getJSONObject("outputs");

                                                                                                                JSONObject SChansOldPartOutputConfigs = jsOldPartSChansOutputs.getJSONObject(OrigCDName);
                                                                                                                
                                                                                                                SChansOldPartOutputConfigs.put("To", ".");
                                                                                                                jsOldPartSChansOutputs.put(OrigCDName, SChansOldPartOutputConfigs);
                                                                                                                jsOldPartSigsChansInd.put("outputs", jsOldPartSChansOutputs);
                                                                                                                jsOldPartSigsChans.put("SChannels",jsOldPartSigsChansInd);
                                                                                                                jsLocalCDs.put(OrigCDName,jsOldPartSigsChans);
                                                                                                                
                                                                                                        }
                                                                                                    }

                                                                                                                    

                                                                                                } else {

                                                                                                    inchan.TransmitPartnerReconfigChanChanges(".");

                                                                                                }
                                                                                                */
                                                                                
                                                                                                inchan.PartnerName=".";
                                                                                                channels.put(inchan.Name, inchan);
                                                                                                
                                                                                                SChansInputConfigs.put("From", ".");
                                                                                                jsSChansInputs.put(PartnerChanName, SChansInputConfigs);
                                                                                                jsSigsChansInd.put("inputs", jsSChansInputs);
                                                                                                jsSigsChans.put("SChannels",jsSigsChansInd);
                                                                                                jsLocalCDs.put(PartnerChanCDName,jsSigsChans);
                                                                                                
                                                                                               //} 
                                                                                        
                                                                } else if (PartnerChanDir.equalsIgnoreCase("output")){
                                                                    
                                                                    
                                                                    JSONObject jsSChansOutputs = jsSigsChansInd.getJSONObject("outputs");
                                                                    
                                                                    JSONObject SChansOutputConfigs = jsSChansOutputs.getJSONObject(PartnerChanName);
                                                                    
                                                                    
                                                                    String cname = PartnerChanName+"_o";
                                                                    
                                                                    
                                                                    if(sc.SchedulerHasCD(PartnerChanCDName)){
                                                                        cdObj = sc.getClockDomain(PartnerChanCDName);
                                                                    } else if (CDObjectsBuffer.CDObjBufferHas(PartnerChanCDName)){
                                                                        cdObj = CDObjectsBuffer.GetCDInstancesFromBuffer(PartnerChanCDName);
                                                                    }
                                                                    
                                                                                                output_Channel ochan;

                                                                                                Field fochanOld = cdObj.getClass().getField(cname);

                                                                                                ochan = (output_Channel)fochanOld.get(cdObj);
                                                                                                
                                                                                                //ochan.set_preempted();
                                                                                                                                                                                 
                                                                                                //String Destination = SChansOutputConfigs.getString("To");
                                                                        
                                                                    
                                                                                        //if(!Destination.equalsIgnoreCase(".")){

                                                                                            //String DestCDName = Destination.split("\\.")[0];

                                                                                            //String DestPartChanNameIn = Destination.split("\\.")[1]+"_in";
/*
                                                                                                if(jsLocalCDs.has(DestCDName) || keyCurrSS.equals(im.getCDLocation(DestCDName))){

                                                                                                   ClockDomain partnercdOld = null;

                                                                                                   if(sc.SchedulerHasCD(DestCDName)){
                                                                                                       partnercdOld = sc.getClockDomain(DestCDName);
                                                                                                   } else if(CDObjectsBuffer.CDObjBufferHas(DestCDName)){
                                                                                                       partnercdOld = CDObjectsBuffer.GetCDInstancesFromBuffer(DestCDName);
                                                                                                   }

                                                                                                                    input_Channel inchanOld;
                                                                                                                    Field fInChanOld = partnercdOld.getClass().getField(DestPartChanNameIn);
                                                                                                                    inchanOld = (input_Channel)fInChanOld.get(partnercdOld);

                                                                                                                    //inchanOld.set_preempted();

                                                                                                                    inchanOld.PartnerName = ".";

                                                                                                                    ochan.set_partner_smp(new input_Channel());
                                                                                                                    inchanOld.set_partner_smp(new output_Channel());
                                                                                                                    

                                                                                                } else {

                                                                                                    ochan.TransmitPartnerReconfigChanChanges(".");

                                                                                                }
                                                                                */
                                                                                

                                                                                                ochan.PartnerName=".";
                                                                                                channels.put(ochan.Name, ochan);
                                                                        
                                                                                    //} 
                                                                                   
                                                                                                SChansOutputConfigs.put("To", "."); 
                                                                                                jsSChansOutputs.put(PartnerChanName, SChansOutputConfigs);
                                                                                                jsSigsChansInd.put("outputs", jsSChansOutputs);
                                                                                                jsSigsChans.put("SChannels",jsSigsChansInd);
                                                                                                jsLocalCDs.put(PartnerChanCDName,jsSigsChans);
                                                                    
                                                                    
                                                                }
                                                                
                                                            //}
                                                            
                                                       // }
                                                        
                                                                if(sc.SchedulerHasCD(PartnerChanCDName)){
                                                                    sc.updateClockDomain(cdObj, PartnerChanCDName);
                                                                } else if (CDObjectsBuffer.CDObjBufferHas(PartnerChanCDName)){
                                                                    CDObjectsBuffer.AddCDInstancesToBuffer(PartnerChanCDName, cdObj);
                                                                } 
                                                                
                                                    } else {
                                                        //ClockDomain cdCurr = null;
                                                        if(sc.SchedulerHasCD(CDName)){
                                                            cdObj = sc.getClockDomain(CDName);
                                                        } else if (CDObjectsBuffer.CDObjBufferHas(CDName)){
                                                            cdObj = CDObjectsBuffer.GetCDInstancesFromBuffer(CDName);
                                                        }
                                                        
                                                        if(PartnerChanDir.equalsIgnoreCase("output")){
                                                            
                                                            String cname = ChanName+"_in";
                                                            
                                                             input_Channel inchan;
                                                             
                                                             Field f = cdObj.getClass().getField(cname);

                                                             inchan = (input_Channel)f.get(cdObj);
                                                             if(!inchan.PartnerName.equals("._o")){
                                                                 inchan.TransmitPartnerReconfigChanChanges(".");
                                                             }
                                                        } else if (PartnerChanDir.equalsIgnoreCase("input")){
                                                            
                                                            String cname = ChanName+"_o";
                                                            
                                                            output_Channel ochan;
                                                             
                                                             Field f = cdObj.getClass().getField(cname);

                                                             ochan = (output_Channel)f.get(cdObj);
                                                             if(!ochan.PartnerName.equals("._in")){
                                                                 ochan.TransmitPartnerReconfigChanChanges(".");
                                                             }
                                                        }
                                                        
                                                    }
                                                    
                                                    im.setChannelInstances(channels);
                                                    JSONObject currMap = SJSSCDSignalChannelMap.getCurrentSignalChannelMapping();
                                                    currMap.put(keyCurrSS, jsLocalCDs);
                                                    SJSSCDSignalChannelMap.UpdateAllCurrSignalChannelMapping(currMap);
                                                    
            
        } catch (JSONException jex){
            jex.printStackTrace();
        } catch (Exception ex){
            ex.printStackTrace();
        }
            
           
                                                    //ClockDomainLifeCycleStatusRepository.AddCDNameAndStatus(keyCDName, "Active");
                                                    
        
        
                                                    vec.addElement(im);
                                                    
                                                    vec.addElement(sc);
                                
                                return vec;
            
        }
    
    public Vector ModifyLocalPartner(JSONObject jsLocalCDs, JSONObject jsMigratingMap, String keyCurrSS, String migCDName, String MigType, String DestSS,InterfaceManager im, Scheduler sc)        
    {
        im.addCDLocation(DestSS, migCDName);
        Vector vec = new Vector();
        try{
            
                    //Hashtable channels = im.getAllChannelInstances();
            
                                                    //ClockDomain cdins = SJSSCDSignalChannelMap.GetCDInstancesFromMap(keyCDName);

                                                    //JSONObject jsSigsChans = jsLocalCDs.getJSONObject(migCDName);
                    
                                                    JSONObject jsSigsChans = jsMigratingMap.getJSONObject(migCDName);

                                                   // Enumeration keysSigsChans = jsSigsChans.keys();

                                                   // while (keysSigsChans.hasMoreElements()){

                                                 //       String tagSigsChans = keysSigsChans.nextElement().toString();

                                                       // if (tagSigsChans.equalsIgnoreCase("SChannels")){

                                            JSONObject jsSigsChansInd = jsSigsChans.getJSONObject("SChannels");

                                                //Enumeration keysSChansInOuts = jsSigsChansInd.keys();

                                                //while (keysSChansInOuts.hasMoreElements()){

                                                   // String keyInOut = keysSChansInOuts.nextElement().toString();

                                                    //if (keyInOut.equalsIgnoreCase("inputs")){

                                                        JSONObject jsSChansInputs = jsSigsChansInd.getJSONObject("inputs");

                                                        Enumeration keysSigsInputsName = jsSChansInputs.keys();

                                                        while (keysSigsInputsName.hasMoreElements()){

                                                            String SChansInputsName = keysSigsInputsName.nextElement().toString();

                                                            JSONObject SChansInputConfigs = jsSChansInputs.getJSONObject(SChansInputsName);

                                                                //String cname = SChansInputsName.trim()+"_in";
                                                                String pname = SChansInputConfigs.getString("From").trim()+"_o";
                                                                //String pname2 = SChansInputConfigs.getString("From").trim();
                                                                String[] pnames = pname.split("\\.");
                                                                //Field f = migratingcdins.getClass().getField(cname);
                                                                                
                                                                        //input_Channel inchan = (input_Channel)f.get(migratingcdins);
                                                                       
                                                                        //channels.remove(inchan.Name);

                                            // If the channel is local
                                                                //System.out.println("SigRec, InChan cd location of: "+pnames[0]+"is " +keyCurrSS);

                                                                    if(keyCurrSS.equals(im.getCDLocation(pnames[0]))){

                                                                        //System.out.println("Creating Input channel!");
                                                                        
                                                                        
                                                                        ClockDomain partnercd=null;
                                                                        
                                                                        if (sc.SchedulerHasCD(pnames[0])){
                                                                            
                                                                            partnercd = sc.getClockDomain(pnames[0]);
                                                                            
                                                                        } else if(CDObjectsBuffer.CDObjBufferHas(pnames[0])){
                                                                                partnercd = CDObjectsBuffer.GetCDInstancesFromBuffer(pnames[0]);        
                                                                            }
                                                                        
                                                                         output_Channel ochan;
                                                                                        Field f2 = partnercd.getClass().getField(pnames[1]);
                                                                                        ochan = (output_Channel)f2.get(partnercd);
                                                                                        
                                                                                        if(MigType.equals("strong")){
                                                                                            ochan.setDistributedReconfiguration();
                                                                                        } else {
                                                                                            ochan.setDistributedWeakMigration();
                                                                                        }
                                                                                        
                                                                                        //Interconnection ic = im.getInterconnection();
                                                                                        //SJSSCDSignalChannelMap.AddChanLinkUserToSS(DestSS, pnames[0], "output", SChansInputsName);
                                                                                        
                                                                                        //im.setInterconnection(ic);
                                                                                        
                                                                                        ochan.setInterfaceManager(im);
                                                                        
                                                                    }
                                                                   
                                                         }

                                               // } 
                                                    //else if (keyInOut.equalsIgnoreCase("outputs")){

                                                    JSONObject jsSChansOutputs = jsSigsChansInd.getJSONObject("outputs");

                                                    Enumeration keysChansOutputsName = jsSChansOutputs.keys();

                                                        while (keysChansOutputsName.hasMoreElements()){

                                                            String SChansOutputsName = keysChansOutputsName.nextElement().toString();

                                                            JSONObject SigOutputConfigs = jsSChansOutputs.getJSONObject(SChansOutputsName);

                                                            //String cname = SChansOutputsName.trim()+"_o";
                                                            String pname = SigOutputConfigs.getString("To").trim()+"_in";
                                                            String[] pnames = pname.split("\\.");
                                                           // Field f = migratingcdins.getClass().getField(cname);
                                                                    
                                                                    //output_Channel ochan = (output_Channel) f.get(migratingcdins);
                                                                    
                                                                    //channels.remove(ochan.Name);
                                                                if(keyCurrSS.equals(im.getCDLocation(pnames[0]))){
                                                                    ClockDomain partnercd = null;
                                                                    if (sc.SchedulerHasCD(pnames[0])){
                                                                        
                                                                            partnercd = sc.getClockDomain(pnames[0]);
                                                                           
                                                                        } else 
                                                                            
                                                                            // if cd in Idle state, fetch from CDObjBuffer
                                                                            if(CDObjectsBuffer.CDObjBufferHas(pnames[0])){
                                                                                
                                                                                partnercd = CDObjectsBuffer.GetCDInstancesFromBuffer(pnames[0]);
                                                                       
                                                                            }
                                                                    
                                                                    input_Channel inchan;
                                                                        Field f2 = partnercd.getClass().getField(pnames[1]);
                                                                        inchan = (input_Channel)f2.get(partnercd);
                                                                           if(MigType.equals("strong")){
                                                                              inchan.setDistributedStrongMigration();
                                                                           } else {
                                                                              inchan.setDistributedWeakMigration();
                                                                           }
                                                                           
                                                                            //im.addCDLocation(DestSS, migCDName);
                                                                            inchan.setInterfaceManager(im);
                                                        }
                                                                
                                                       
                                              }

                                           //}

                                        //}

                                       // } 
                                                        /*
                                                        else if (tagSigsChans.equalsIgnoreCase("AChannels")){

                                            JSONObject jsSigsChansInd = jsSigsChans.getJSONObject(tagSigsChans);

                                             Enumeration keysAChansInOuts = jsSigsChansInd.keys();

                                                while (keysAChansInOuts.hasMoreElements()){

                                                    String keyInOut = keysAChansInOuts.nextElement().toString();

                                                    if (keyInOut.equalsIgnoreCase("inputs") || keyInOut.equalsIgnoreCase("outputs")){

                                                        
                                                        JSONObject jsAChans = new JSONObject();
                                                        
                                                        if (keyInOut.equalsIgnoreCase("inputs")){
                                                            
                                                            jsAChans = jsSigsChansInd.getJSONObject("inputs");
                                                            
                                                        } else if(keyInOut.equalsIgnoreCase("outputs")){
                                                        
                                                            jsAChans= jsSigsChansInd.getJSONObject("outputs");
                                                        
                                                        }
            
                                                        Enumeration keysAChansName = jsAChans.keys();

                                                        while (keysAChansName.hasMoreElements()){

                                                            String AChansName = keysAChansName.nextElement().toString();

                                                            JSONObject AChansConfigs = jsAChans.getJSONObject(AChansName);

                                                            String cname = AChansName;

                                                            String pname="";
                                                            
                                                            if (keyInOut.equalsIgnoreCase("inputs")){
                                                            
                                                                pname = AChansConfigs.getString("From").trim();
                                                            
                                                            } else if(keyInOut.equalsIgnoreCase("outputs")){
                                                        
                                                                pname = AChansConfigs.getString("To").trim();
                                                        
                                                            }
                                                            
                                                                String[] pnames = pname.split("\\.");

                                                                im.addCDLocation(DestSS, pnames[0]);
                                                                
                                                                    if(keyCurrSS.equals(im.getCDLocation(pnames[0]))){
                                                                        
                                                                        if(sc.SchedulerHasCD(pnames[0])){
                                                                            
                                                                            ClockDomain partnercd = sc.getClockDomain(pnames[0]);
                                                                            //if(partnercd == null)
                                                                            //throw new RuntimeException("Clock-domain "+pnames[0]+" not found");
                                                                            //AChannel chan;

                                                                                
                                                                                Field f2 = partnercd.getClass().getField(pnames[1]);
                                                            //f.set(cdins, chan);
                                                                                AChannel chan = (AChannel)f2.get(partnercd);
                                                                                //AChannel chan = (AChannel)f.get(cdins);
                                                                                //f2.set(partnercd, chan); // sharing achan obj
                                                                                
                                                                                //chan.setDistributedRetainState();
                                                                                //channels.put(keyCDName+"."+cname, chan);
                                                                                //channels.put(pname, chan);

                                                                                //SJSSCDSignalChannelMap.addInOutChannelObjectToMap(keyCurrSS, keyCDName, "AChannel", "input", AChansName,(Object) chan);
                                                                            
                                                                            
                                                                        } else if(CDObjectsBuffer.CDObjBufferHas(pnames[0])){
                                                                            
                                                                            
                                                                             ClockDomain partnercd = CDObjectsBuffer.GetCDInstancesFromBuffer(pnames[0]);
                                                                            //if(partnercd == null)
                                                                            //throw new RuntimeException("Clock-domain "+pnames[0]+" not found");
                                                                            AChannel chan;

                                                                                Field f = migratingcdins.getClass().getField(cname);
                                                                                Field f2 = partnercd.getClass().getField(pnames[1]);
                                                            //f.set(cdins, chan);
                                                                                //chan = (AChannel)f.get(cdins);
                                                                                chan = (AChannel)f2.get(partnercd);
                                                                                f.set(migratingcdins, chan);
                                                                                //f2.set(partnercd, chan); // sharing achan obj
                                                                                chan.setInit();
                                                                                //channels.put(keyCDName+"."+cname, chan);
                                                                               // channels.put(pname, chan);

                                                                               // SJSSCDSignalChannelMap.addInOutChannelObjectToMap(keyCurrSS, keyCDName, "AChannel", "input", AChansName,(Object) chan);
                                                                            
                                                                        }
                                                                        
                                                                    }

                                                                }

                                                } 
                                                    
                                                    else if (keyInOut.equalsIgnoreCase("outputs")){

                                                    JSONObject jsAChansOutputs = jsSigsChansInd.getJSONObject("outputs");

                                                    Enumeration keysAChansOutputsName = jsAChansOutputs.keys();

                                                        while (keysAChansOutputsName.hasMoreElements()){

                                                            String AChansOutputsName = keysAChansOutputsName.nextElement().toString();

                                                            JSONObject SigOutputConfigs = jsAChansOutputs.getJSONObject(AChansOutputsName);

                                                            String pname = SigOutputConfigs.getString("To").trim();
                                                            String cname = AChansOutputsName;
                                                            String[] pnames = pname.split("\\.");

                                    // If the channel is local

                                                                if(keyCurrSS.equals(im.getCDLocation(pnames[0]))){
                                                                    ClockDomain partnercd = (ClockDomain)CDObjectsBuffer.getAllCDInstancesFromBuffer().get(pnames[0]);

                                                                    AChannel chan;

                                                                    Field f = cdins.getClass().getField(cname);
                                                                    Field f2 = partnercd.getClass().getField(pnames[1]);
                                                                    //f.set(cdins, chan);
                                                                    chan = (AChannel)f.get(cdins);
                                                                    f2.set(partnercd, chan); // sharing achan obj
                                                                    chan.setInit();

                                                                    }
                                                                    else{
                                                                        AChannel chan;

                                                                        Field f = cdins.getClass().getField(cname);
                                                                        chan = (AChannel)f.get(cdins);
                                                                        chan.setInit();

                                                                        chan.Name = keyCDName+"."+cname;
                                                                        chan.PartnerName = pname;
                                                                        chan.setDistributed();
                                                                        chan.setInterfaceManager(im);

                                                                        }

                                                                      }

                                                               }
                                                               
                                                            }

                                                         }
                                                        */

                                                    //}
                                                    
                                                    //im.setChannelInstances(channels);
                                                    
                                                    
            
        } catch (JSONException jex){
            jex.printStackTrace();
        } catch (Exception ex){
            ex.printStackTrace();
        }
            
                                                    vec.addElement(im);
                                                    vec.addElement(sc);
                                
                                return vec;
            
        }
    
    public Vector AddCD(JSONObject jsLocalCDs, JSONObject CDConf, String keyCurrSS,String keyCDName, ClockDomain cdins, Hashtable AllCDs, String MigType, String OriginSS, boolean isActive, InterfaceManager im, Scheduler sc) throws JSONException, Exception
        {
            Vector vec = new Vector();
            Hashtable channels = im.getAllChannelInstances();
                                    im.addCDLocation(keyCurrSS, keyCDName);
                                                    JSONObject jsSigsChans = CDConf.getJSONObject(keyCDName);
                                                            JSONObject jsSigs = jsSigsChans.getJSONObject("signals");
                                                            GenericSignalReceiver server = null;
                                                            GenericSignalSender client = null;
                                                            Hashtable config = new Hashtable();
                                                                JSONObject jsSigsInputs = jsSigs.getJSONObject("inputs");
                                                                Enumeration keysSigsInputsName = jsSigsInputs.keys();
                                                                while (keysSigsInputsName.hasMoreElements()){
                                                                    String SigsInputsName = keysSigsInputsName.nextElement().toString();
                                                                    JSONObject SigInputConfigs = jsSigsInputs.getJSONObject(SigsInputsName);
                                                                    Enumeration keysSigInputConfigs = SigInputConfigs.keys();
                                                                    while (keysSigInputConfigs.hasMoreElements()){
                                                                        String keySigInputConfig = keysSigInputConfigs.nextElement().toString();
                                                                        config.put(keySigInputConfig, SigInputConfigs.getString(keySigInputConfig));
                                                                        if(keySigInputConfig.equalsIgnoreCase("SOSJDisc") || keySigInputConfig.equalsIgnoreCase("SOSJDiscReply")){
                                                                            config.put("CDName",keyCDName);
                                                                        }
                                                                    }
                                                            if(SigInputConfigs.get("Name").toString().equals("SOSJDiscReply")){
                                                                server = (GenericSignalReceiver) Class.forName("systemj.signals.SOA.ReceiveDisc").newInstance();
                                                            } else {
                                                                server = (GenericSignalReceiver) Class.forName(SigInputConfigs.get("Class").toString()).newInstance();
                                                            }   
                                                            server.cdname = keyCDName;
                                                            server.configure(config);
                                                            Field f = cdins.getClass().getField(SigsInputsName);
                                                            Signal signal = (Signal)f.get(cdins);
                                                            signal.setServer(server);
                                                            if(isActive){
                                                                signal.setuphook();
                                                            }
                                                            signal.setInit();
                                                        }
                                                    JSONObject jsSigsOutputs = jsSigs.getJSONObject("outputs");
                                                    Enumeration keysSigsOutputsName = jsSigsOutputs.keys();
                                                        while (keysSigsOutputsName.hasMoreElements()){
                                                            String SigsOutputsName = keysSigsOutputsName.nextElement().toString();
                                                            JSONObject SigOutputConfigs = jsSigsOutputs.getJSONObject(SigsOutputsName);
                                                            Enumeration keysSigOutputConfigs = SigOutputConfigs.keys();
                                                            while (keysSigOutputConfigs.hasMoreElements()){
                                                                String keySigOutputConfig = keysSigOutputConfigs.nextElement().toString();
                                                                config.put(keySigOutputConfig, SigOutputConfigs.getString(keySigOutputConfig));
                                                                if(keySigOutputConfig.equalsIgnoreCase("SOSJDisc") || keySigOutputConfig.equalsIgnoreCase("SOSJDiscReply")){
                                                                        config.put("CDName",keyCDName);
                                                                }
                                                            }
                                                            if (SigOutputConfigs.get("Name").toString().equalsIgnoreCase("SOSJDisc")){
                                                                client = (GenericSignalSender) Class.forName("systemj.signals.SOA.TransmitDisc").newInstance();
                                                            } else {
                                                                client = (GenericSignalSender) Class.forName(SigOutputConfigs.getString("Class")).newInstance();
                                                            }
                                                            client.cdname = keyCDName;
                                                            client.configure(config);
                                                            Field f = cdins.getClass().getField(SigsOutputsName);
                                                            Signal signal = (Signal)f.get(cdins);
                                                            signal.setClient(client);
                                                            signal.setInit();
                                                        }
                                                       JSONObject jsSChansInd = jsSigsChans.getJSONObject("SChannels");
                                                        JSONObject jsSChansInputs = jsSChansInd.getJSONObject("inputs");
                                                        Enumeration keysSChansInputsName = jsSChansInputs.keys();
                                                        while (keysSChansInputsName.hasMoreElements()){
                                                            String SChansInputsName = keysSChansInputsName.nextElement().toString();
                                                            JSONObject SChansInputConfigs = jsSChansInputs.getJSONObject(SChansInputsName);
                                                                String cname = SChansInputsName.trim()+"_in";
                                                                String pname = SChansInputConfigs.getString("From").trim()+"_o";
                                                                if(SChansInputConfigs.getString("From").equalsIgnoreCase(".")){
                                                                    input_Channel inchan;
                                                                                        Field f = cdins.getClass().getField(cname);
                                                                                        inchan = (input_Channel)f.get(cdins);
                                                                                        inchan.setLocal();
                                                                                        inchan.set_partner_smp(new output_Channel());
                                                                                        inchan.setInit();
                                                                                        inchan.setInterfaceManager(im);
                                                                                        inchan.Name = keyCDName+"."+cname;
                                                                                        inchan.PartnerName = ".";
                                                                                        channels.put(inchan.Name, inchan);
                                                                } else {
                                                                    String[] pnames = pname.split("\\.");
                                                                    if((im.getCDLocation(pnames[0]).equals(keyCurrSS)) || AllCDs.containsKey(pnames[0])){
                                                                        JSONObject tempMap = CDLCBuffer.getAllTempSignalChannelMap();
                                                                        ClockDomain partnercd=null;
                                                                        JSONObject jsPartnerCDMap  = new JSONObject();
                                                                        if(jsLocalCDs.has(pnames[0])){
                                                                            jsPartnerCDMap = jsLocalCDs.getJSONObject(pnames[0]);
                                                                        } else if(tempMap.has(pnames[0])){
                                                                            jsPartnerCDMap = tempMap.getJSONObject(pnames[0]);
                                                                        }
                                                                        JSONObject jsPartnerCDSChanMap = jsPartnerCDMap.getJSONObject("SChannels");
                                                                        JSONObject jsPartnerCDOSChansMap = jsPartnerCDSChanMap.getJSONObject("outputs");
                                                                        JSONObject jsPartnerCDOSChanMap = jsPartnerCDOSChansMap.getJSONObject(pnames[1].split("_")[0]);
                                                                        String PartnerDest = jsPartnerCDOSChanMap.getString("To");
                                                                        if(im.getCDLocation(pnames[0]).equals(keyCurrSS)){
                                                                             if (sc.SchedulerHasCD(pnames[0])){
                                                                                partnercd = sc.getClockDomain(pnames[0]);
                                                                            } else if (CDObjectsBuffer.CDObjBufferHas(pnames[0])) {
                                                                                partnercd = CDObjectsBuffer.GetCDInstancesFromBuffer(pnames[0]);
                                                                            }
                                                                                output_Channel ochan;
                                                                                        Field f2 = partnercd.getClass().getField(pnames[1]);
                                                                                        ochan = (output_Channel)f2.get(partnercd);
                                                                                        if(!ochan.PartnerName.equals(".")){
                                                                                            String oldPartnerOfOChanCD = (ochan.PartnerName).split("\\.")[0];
                                                                                            String oldPartnerOfOChanIChanName = (ochan.PartnerName).split("\\.")[1].split("_")[0];
                                                                                            String oldPartnerOfOChanCNAME = oldPartnerOfOChanIChanName+"_in";
                                                                                            if(im.getCDLocation(oldPartnerOfOChanCD).equals(keyCurrSS)){
                                                                                                if(!(oldPartnerOfOChanCD.equals(keyCDName))){
                                                                                                    ClockDomain partnerpartnercd = null;
                                                                                                    if (sc.SchedulerHasCD(oldPartnerOfOChanCD)){
                                                                                                        partnerpartnercd = sc.getClockDomain(oldPartnerOfOChanCD);
                                                                                                    } else if (CDObjectsBuffer.CDObjBufferHas(oldPartnerOfOChanCD)){
                                                                                                        partnerpartnercd = CDObjectsBuffer.GetCDInstancesFromBuffer(oldPartnerOfOChanCD);
                                                                                                    }
                                                                                                    Field f3 = partnerpartnercd.getClass().getField(oldPartnerOfOChanCNAME);
                                                                                                    input_Channel inchanPartOchan = (input_Channel)f3.get(partnerpartnercd);
                                                                                                    inchanPartOchan.PartnerName=".";
                                                                                                    inchanPartOchan.set_partner_smp(new output_Channel());
                                                                                                    inchanPartOchan.forcePreempt();
                                                                                                    inchanPartOchan.setOccupiedStat(false);
                                                                                                    channels.put(inchanPartOchan.Name, inchanPartOchan);
                                                                                                    JSONObject jsOldPartOfPartCDMap = jsLocalCDs.getJSONObject(oldPartnerOfOChanCD);
                                                                                                    JSONObject jsOldPartOfPartSChans = jsOldPartOfPartCDMap.getJSONObject("SChannels");
                                                                                                    JSONObject jsOldPartOfPartSInChans = jsOldPartOfPartSChans.getJSONObject("inputs");
                                                                                                    JSONObject jsOldPartOfPartInChan = jsOldPartOfPartSInChans.getJSONObject(oldPartnerOfOChanIChanName);
                                                                                                    jsOldPartOfPartInChan.put("From", ".");
                                                                                                    jsOldPartOfPartSInChans.put(oldPartnerOfOChanIChanName,jsOldPartOfPartInChan);
                                                                                                    jsOldPartOfPartSChans.put("inputs",jsOldPartOfPartSInChans);
                                                                                                    jsOldPartOfPartCDMap.put("SChannels",jsOldPartOfPartSChans);
                                                                                                    jsLocalCDs.put(oldPartnerOfOChanCD,jsOldPartOfPartCDMap);
                                                                                                }
                                                                                            } else {
                                                                                                ochan.TransmitPartnerReconfigChanChanges(".");
                                                                                            }
                                                                                        }
                                                                                            PartnerDest = keyCDName+"."+SChansInputsName;
                                                                                            jsPartnerCDOSChanMap.put("To", PartnerDest);
                                                                                            jsPartnerCDOSChansMap.put(pnames[1].split("_")[0],jsPartnerCDOSChanMap);
                                                                                            jsPartnerCDSChanMap.put("outputs", jsPartnerCDOSChansMap);
                                                                                            jsPartnerCDMap.put("SChannels",jsPartnerCDSChanMap);
                                                                                            jsLocalCDs.put(pnames[0],jsPartnerCDMap);
                                                                                        //ochan.Name = pname;
                                                                                        ochan.PartnerName = keyCDName+"."+cname;
                                                                                        ochan.setLocal();
                                                                                        input_Channel inchan;
                                                                                        Field f = cdins.getClass().getField(cname);
                                                                                        inchan = (input_Channel)f.get(cdins);
                                                                                        inchan.setInit();
                                                                                        inchan.Name = keyCDName+"."+cname;
                                                                                        inchan.PartnerName = pname;
                                                                                        inchan.setInterfaceManager(im);
                                                                                        //System.out.println("AddCDCheck2, inchan Loc?: " +inchan.IsChannelLocal());
                                                                                        //System.out.println("AddCDCheck2, ochan Loc?: " +ochan.IsChannelLocal());
                                                                                        if(MigType.equals("Strong")){
                                                                                            ochan.set_partner_smp_migration(inchan);
                                                                                        } else {
                                                                                            ochan.set_partner_smp(inchan);
                                                                                            ochan.forcePreempt();
                                                                                        }
                                                                                        
                                                                                        if(MigType.equals("Strong")){
                                                                                           inchan.set_partner_smp_migration(ochan);
                                                                                        } else {
                                                                                           inchan.set_partner_smp(ochan);
                                                                                        }
                                                                                        
                                                                                        /*
                                                                                        if(MigType.equals("Strong")){
                                                                                            if(oldPartnerName.equals(pname)){
                                                                                                inchan.set_partner_smp_migration(ochan);
                                                                                            } else {
                                                                                                inchan.set_partner_smp(ochan);
                                                                                                inchan.forcePreempt();
                                                                                            }
                                                                                        } else {
                                                                                           inchan.set_partner_smp(ochan);
                                                                                        }
                                                                                        */
                                                                                        channels.put(inchan.Name, inchan);
                                                                                        channels.put(ochan.Name,ochan);
                                                                        } 
                                                                        if(AllCDs.containsKey(pnames[0])){
                                                                                partnercd = (ClockDomain) AllCDs.get(pnames[0]);
                                                                                input_Channel inchan;
                                                                                Field f = cdins.getClass().getField(cname);
                                                                                inchan = (input_Channel)f.get(cdins);
                                                                                inchan.setInterfaceManager(im);
                                                                                inchan.setInit();
                                                                                inchan.setLocal();
                                                                                inchan.Name = keyCDName+"."+cname;
                                                                                inchan.PartnerName = pname;
                                                                                output_Channel ochan;
                                                                                        Field f2 = partnercd.getClass().getField(pnames[1]);
                                                                                        ochan = (output_Channel)f2.get(partnercd);
                                                                                        if(PartnerDest.equalsIgnoreCase(".")){
                                                                                            PartnerDest = keyCDName+"."+SChansInputsName;
                                                                                            jsPartnerCDOSChanMap.put("To", PartnerDest);
                                                                                            jsPartnerCDSChanMap.put("outputs", jsPartnerCDOSChanMap);
                                                                                            jsPartnerCDMap.put("SChannels",jsPartnerCDSChanMap);
                                                                                            jsLocalCDs.put(pnames[0],jsPartnerCDMap);
                                                                                        }
                                                                                            ochan.setInit();
                                                                                        //ochan.Name = pname;
                                                                                        ochan.PartnerName = keyCDName+"."+cname;
                                                                                        ochan.setLocal();
                                                                                        if(MigType.equals("Strong")){
                                                                                            ochan.set_partner_smp_migration(inchan);
                                                                                        } else {
                                                                                            ochan.set_partner_smp(inchan);
                                                                                            ochan.forcePreempt();
                                                                                        }
                                                                                        if(MigType.equals("Strong")){
                                                                                           inchan.set_partner_smp_migration(ochan);
                                                                                        } else {
                                                                                           inchan.set_partner_smp(ochan);
                                                                                        }
                                                                                        /*
                                                                                        if(MigType.equals("Strong")){
                                                                                            if(oldPartnerName.equals(pname)){
                                                                                                inchan.set_partner_smp_migration(ochan);
                                                                                            } else {
                                                                                                inchan.set_partner_smp(ochan);
                                                                                                inchan.forcePreempt();
                                                                                            }
                                                                                        } else {
                                                                                           inchan.set_partner_smp(ochan);
                                                                                        }
                                                                                        */
                                                                                        /*
                                                                                        if(MigType.equals("Strong")){
                                                                                           inchan.set_partner_smp_migration(ochan);
                                                                                        } else {
                                                                                           inchan.set_partner_smp(ochan);
                                                                                        }
                                                                                        */
                                                                                        channels.put(inchan.Name, inchan);
                                                                                        channels.put(ochan.Name,ochan);
                                                                        }   
                                                                    } else if(im.IsCDNameRegisteredInAnotherSS(pnames[0])){
                                                                         String partnerSSLoc = im.getCDLocation(pnames[0]);
                                                                         String partnerSSAddr = RegAllSSAddr.getSSAddrOfSSName(partnerSSLoc);
                                                                        Interconnection ic = im.getInterconnection();
                                                                        Vector allLinksToPartnerSS = ic.getRemoteDestinationInterfaces(partnerSSLoc);
                                                                        Vector allLinksToLocalSS = ic.getInterfaces(keyCurrSS);
                                                                        if(allLinksToPartnerSS.size()==0 || allLinksToLocalSS.size()==0){
                                                                            LinkCreationSender lcsh = new LinkCreationSender();
                                                                                String resp = lcsh.SendLinkCreationReq(keyCurrSS, partnerSSLoc, partnerSSAddr);
                                                                                    if(resp.equals("OK")){
                                                                                            im = lcsh.ExecuteLinkCreation(partnerSSLoc, partnerSSAddr, im);
                                                                                    }
                                                                        }
                                                                        input_Channel inchan;
                                                                        Field f = cdins.getClass().getField(cname);
                                                                        inchan = (input_Channel)f.get(cdins);
                                                                        inchan.Name = keyCDName+"."+cname;
                                                                        inchan.PartnerName = pname;
                                                                        inchan.setInit();
                                                                        if(!OriginSS.equalsIgnoreCase("")){
                                                                            if(im.getCDLocation(pnames[0]).equals(OriginSS)){
                                                                                if(MigType.equals("Strong")){
                                                                                    inchan.setDistributedStrongMigration();
                                                                                } else {
                                                                                    inchan.setDistributed();
                                                                                }
                                                                            } else {
                                                                                inchan.setDistributed();
                                                                            }
                                                                        } else {
                                                                            inchan.setDistributed();
                                                                        }
                                                                        inchan.setInterfaceManager(im);
                                                                        if(OriginSS.equalsIgnoreCase("")){
                                                                             inchan.TransmitReconfigChanChanges((inchan.Name).split("_")[0]);
                                                                        }
                                                                       
                                                                            channels.put(inchan.Name, inchan);
                                                                    }
                                                                        else{
                                                                            input_Channel inchan;
                                                                                        Field f = cdins.getClass().getField(cname);
                                                                                        inchan = (input_Channel)f.get(cdins);
                                                                                        inchan.setDistributed();
                                                                                        inchan.setInit();
                                                                                        inchan.Name = keyCDName+"."+cname;
                                                                                        inchan.PartnerName = pname;
                                                                                        inchan.setInterfaceManager(im);
                                                                                        channels.put(inchan.Name, inchan);
                                                                    }
                                                                }
                                                         }
                                                    JSONObject jsSChansOutputs = jsSChansInd.getJSONObject("outputs");
                                                    Enumeration keysChansOutputsName = jsSChansOutputs.keys();
                                                        while (keysChansOutputsName.hasMoreElements()){
                                                            String SChansOutputsName = keysChansOutputsName.nextElement().toString();
                                                            JSONObject SChanOutputConfigs = jsSChansOutputs.getJSONObject(SChansOutputsName);
                                                            String cname = SChansOutputsName.trim()+"_o";
                                                            String pname = SChanOutputConfigs.getString("To").trim()+"_in";
                                                            if(SChanOutputConfigs.getString("To").equalsIgnoreCase(".")){
                                                                output_Channel ochan;
                                                                                        Field f = cdins.getClass().getField(cname);
                                                                                    ochan = (output_Channel)f.get(cdins);
                                                                                    ochan.Name = keyCDName+"."+cname;
                                                                                    ochan.PartnerName = ".";
                                                                                    ochan.setLocal();
                                                                                    ochan.setInit();
                                                                                    ochan.set_partner_smp(new input_Channel());
                                                                                    channels.put(ochan.Name, ochan);
                                                            } else {
                                                            String[] pnames = pname.split("\\.");
                                                                if(keyCurrSS.equals(im.getCDLocation(pnames[0]))|| AllCDs.containsKey(pnames[0])){
                                                                    ClockDomain partnercd = null;
                                                                    JSONObject tempMap = CDLCBuffer.getAllTempSignalChannelMap();
                                                                        JSONObject jsPartnerCDMap  = new JSONObject();
                                                                        if(jsLocalCDs.has(pnames[0])){
                                                                            jsPartnerCDMap = jsLocalCDs.getJSONObject(pnames[0]);
                                                                        } else if(tempMap.has(pnames[0])){
                                                                            jsPartnerCDMap = tempMap.getJSONObject(pnames[0]);
                                                                        }
                                                                        JSONObject jsPartnerCDSChanMap = jsPartnerCDMap.getJSONObject("SChannels");
                                                                        JSONObject jsPartnerCDInSChansMap = jsPartnerCDSChanMap.getJSONObject("inputs");
                                                                        JSONObject jsPartnerCDInSChanMap = jsPartnerCDInSChansMap.getJSONObject(pnames[1].split("_")[0]);
                                                                        String PartnerDest = jsPartnerCDInSChanMap.getString("From");
                                                                        if(keyCurrSS.equals(im.getCDLocation(pnames[0]))){
                                                                             if (sc.SchedulerHasCD(pnames[0])){
                                                                                partnercd = sc.getClockDomain(pnames[0]);
                                                                            } else if(CDObjectsBuffer.CDObjBufferHas(pnames[0])){
                                                                                partnercd = CDObjectsBuffer.GetCDInstancesFromBuffer(pnames[0]);
                                                                            }
                                                                            input_Channel inchan;
                                                                            Field f2 = partnercd.getClass().getField(pnames[1]);
                                                                            inchan = (input_Channel)f2.get(partnercd);
                                                                                    if(!inchan.PartnerName.equals(".")){
                                                                                        String oldPartnerOfInChanCD = (inchan.PartnerName).split("\\.")[0];
                                                                                        String oldPartnerOfInChanOChanName = (inchan.PartnerName).split("\\.")[1].split("_")[0];
                                                                                        String oldPartnerOfInChanCNAME = oldPartnerOfInChanOChanName+"_o";
                                                                                        //System.out.println("AddCDCheck1,: " +im.getCDLocation(oldPartnerOfInChanCD)+" keyCurrSS: " +keyCurrSS);
                                                                                        if(im.getCDLocation(oldPartnerOfInChanCD).equals(keyCurrSS) ){
                                                                                            if(!(oldPartnerOfInChanCD.equals(keyCDName))){
                                                                                                 ClockDomain partnerpartnercd = null;
                                                                                                if (sc.SchedulerHasCD(oldPartnerOfInChanCD)){
                                                                                                    partnerpartnercd = sc.getClockDomain(oldPartnerOfInChanCD);
                                                                                                } else if (CDObjectsBuffer.CDObjBufferHas(oldPartnerOfInChanCD)){
                                                                                                    partnerpartnercd = CDObjectsBuffer.GetCDInstancesFromBuffer(oldPartnerOfInChanCD);
                                                                                                }
                                                                                                Field f3 = partnerpartnercd.getClass().getField(oldPartnerOfInChanCNAME);
                                                                                                output_Channel ochanPartInchan = (output_Channel)f3.get(partnerpartnercd);
                                                                                                ochanPartInchan.PartnerName=".";
                                                                                                ochanPartInchan.set_partner_smp(new input_Channel());
                                                                                                ochanPartInchan.forcePreempt();
                                                                                                channels.put(ochanPartInchan.Name, ochanPartInchan);
                                                                                                    PartnerDest = keyCDName+"."+SChansOutputsName;
                                                                                                    jsPartnerCDInSChanMap.put("From", PartnerDest);
                                                                                                    jsPartnerCDInSChansMap.put(pnames[1].split("_")[0], jsPartnerCDInSChanMap);
                                                                                                    jsPartnerCDSChanMap.put("inputs", jsPartnerCDInSChansMap);
                                                                                                    jsPartnerCDMap.put("SChannels",jsPartnerCDSChanMap);
                                                                                                    jsLocalCDs.put(pnames[0],jsPartnerCDMap);
                                                                                                JSONObject jsOldPartOfPartCDMap = jsLocalCDs.getJSONObject(oldPartnerOfInChanCD);
                                                                                                JSONObject jsOldPartOfPartSChans = jsOldPartOfPartCDMap.getJSONObject("SChannels");
                                                                                                JSONObject jsOldPartOfPartSInChans = jsOldPartOfPartSChans.getJSONObject("outputs");
                                                                                                JSONObject jsOldPartOfPartInChan = jsOldPartOfPartSInChans.getJSONObject(oldPartnerOfInChanOChanName);
                                                                                                jsOldPartOfPartInChan.put("To", ".");
                                                                                                jsOldPartOfPartSInChans.put(oldPartnerOfInChanOChanName,jsOldPartOfPartInChan);
                                                                                                jsOldPartOfPartSChans.put("outputs",jsOldPartOfPartSInChans);
                                                                                                jsOldPartOfPartCDMap.put("SChannels",jsOldPartOfPartSChans);
                                                                                                jsLocalCDs.put(oldPartnerOfInChanCD,jsOldPartOfPartCDMap);
                                                                                            }
                                                                                        } else {
                                                                                            inchan.TransmitPartnerReconfigChanChanges(".");
                                                                                        }
                                                                                    }
                                                                                        output_Channel ochan;
                                                                            Field f = cdins.getClass().getField(cname);
                                                                            ochan = (output_Channel)f.get(cdins);
                                                                            ochan.Name = keyCDName+"."+cname;
                                                                            ochan.PartnerName = pname;
                                                                            ochan.setInit();
                                                                            ochan.setInterfaceManager(im);
                                                                            inchan.setInit();
                                                                            inchan.setLocal();
                                                                            //inchan.Name = pname; 
                                                                            inchan.PartnerName = keyCDName+"."+cname;
                                                                            inchan.setOccupiedStat(false);
                                                                            //System.out.println("AddCDCheck3, inchan Loc?: " +inchan.IsChannelLocal());
                                                                            // System.out.println("AddCDCheck3, ochan Loc?: " +ochan.IsChannelLocal());
                                                                            if(MigType.equals("Strong")){
                                                                               inchan.set_partner_smp_migration(ochan);
                                                                            } else {
                                                                               inchan.set_partner_smp(ochan);
                                                                               inchan.forcePreempt();
                                                                            }
                                                                            if(MigType.equals("Strong")){
                                                                                ochan.set_partner_smp_migration(inchan);
                                                                            } else {
                                                                                ochan.set_partner_smp(inchan);
                                                                            }
                                                                            channels.put(ochan.Name, ochan);
                                                                            channels.put(inchan.Name,inchan);
                                                                        }
                                                                        
                                                                                if(AllCDs.containsKey(pnames[0])){
                                                                                    partnercd = (ClockDomain)AllCDs.get(pnames[0]);
                                                                                        output_Channel ochan;
                                                                                        Field f = cdins.getClass().getField(cname);
                                                                                    ochan = (output_Channel)f.get(cdins);      // Mine
                                                                                    ochan.Name = keyCDName+"."+cname;
                                                                                    ochan.PartnerName = pname;
                                                                                    ochan.setLocal();
                                                                                    ochan.setInit();
                                                                                    ochan.setInterfaceManager(im);
                                                                                    input_Channel inchan;
                                                                                    Field f2 = partnercd.getClass().getField(pnames[1]);
                                                                                    inchan = (input_Channel)f2.get(partnercd);
                                                                                    if(PartnerDest.equalsIgnoreCase(".")){
                                                                                            PartnerDest = keyCDName+"."+SChansOutputsName;
                                                                                            jsPartnerCDInSChanMap.put("From", PartnerDest);
                                                                                            jsPartnerCDSChanMap.put("inputs", jsPartnerCDInSChanMap);
                                                                                            jsPartnerCDMap.put("SChannels",jsPartnerCDSChanMap);
                                                                                            jsLocalCDs.put(pnames[0],jsPartnerCDMap);
                                                                                            JSONObject NewCurrMap = new JSONObject();
                                                                                            NewCurrMap.put(keyCurrSS, jsLocalCDs);
                                                                                            SJSSCDSignalChannelMap.UpdateAllCurrSignalChannelMapping(NewCurrMap);
                                                                                        }
                                                                                    inchan.setInit();
                                                                                    inchan.setLocal();
                                                                                    //inchan.Name = pname; 
                                                                                    inchan.PartnerName = keyCDName+"."+cname;
                                                                                    
                                                                                    if(MigType.equals("Strong")){
                                                                                        ochan.set_partner_smp_migration(inchan);
                                                                                    } else {
                                                                                        ochan.set_partner_smp(inchan);
                                                                                        ochan.forcePreempt();
                                                                                    }
                                                                                    if(MigType.equals("Strong")){
                                                                                        inchan.set_partner_smp_migration(ochan);
                                                                                    } else {
                                                                                        inchan.set_partner_smp(ochan);
                                                                                    }
                                                                                    channels.put(ochan.Name, ochan);
                                                                                    channels.put(inchan.Name,inchan);
                                                                                }
                                                        } else if(im.IsCDNameRegisteredInAnotherSS(pnames[0])){
                                                             String partnerCDSSLoc = im.getCDLocation(pnames[0]);
                                                                    String partnerSSAddr = RegAllSSAddr.getSSAddrOfSSName(partnerCDSSLoc);
                                                                    Interconnection ic = im.getInterconnection();
                                                                        Vector allLinksToPartnerSS = ic.getRemoteDestinationInterfaces(partnerCDSSLoc);
                                                                        Vector availOwnLink = ic.getRemoteDestinationInterfaces(keyCurrSS);
                                                                        //System.out.println("AddCD Check 4 Got here ");
                                                                        if(allLinksToPartnerSS.size()==0 || availOwnLink.size()==0){
                                                                            LinkCreationSender lcsh = new LinkCreationSender();
                                                                            String resp = lcsh.SendLinkCreationReq(keyCurrSS,partnerCDSSLoc, partnerSSAddr);
                                                                                if(resp.equals("OK")){
                                                                                        im = lcsh.ExecuteLinkCreation(partnerCDSSLoc, partnerSSAddr, im);
                                                                                } 
                                                                        }
                                                                         //System.out.println("AddCD Check 5 Got here ");
                                                                    output_Channel ochan;
                                                                    Field f = cdins.getClass().getField(cname);
                                                                    ochan = (output_Channel)f.get(cdins);
                                                                    ochan.setInit();
                                                                    ochan.Name = keyCDName+"."+cname;
                                                                    ochan.PartnerName = pname;
                                                                    
                                                                    if(!OriginSS.equalsIgnoreCase("")){
                                                                            if(im.getCDLocation(pnames[0]).equals(OriginSS)){
                                                                                if(MigType.equals("Strong")){
                                                                                    ochan.setDistributedReconfiguration();
                                                                                } else {
                                                                                    ochan.setDistributed();
                                                                                }
                                                                            } else {
                                                                                ochan.setDistributed();
                                                                            }
                                                                        } else {
                                                                            ochan.setDistributed();
                                                                        }
                                                                    ochan.setInterfaceManager(im);
                                                                    if(OriginSS.equalsIgnoreCase("")){
                                                                        ochan.TransmitReconfigChanChanges((ochan.Name).split("_")[0]);
                                                                    }
                                                                    channels.put(ochan.Name,ochan);
                                                        } else {
                                                            output_Channel ochan;
                                                                                    Field f = cdins.getClass().getField(cname);
                                                                                    ochan = (output_Channel)f.get(cdins);       // Mine
                                                                                    ochan.Name = keyCDName+"."+cname;
                                                                                    ochan.PartnerName = pname;
                                                                                    ochan.setChannelCDState("Active");
                                                                                    ochan.setDistributed();
                                                                                    ochan.setInit();
                                                                                    ochan.setInterfaceManager(im);
                                                                                    channels.put(ochan.Name, ochan);
                                                             }
                                                          }
                                                        }
                                                        if(isActive){
                                                            cdins.setState("Active");
                                                            sc.addClockDomain(cdins);
                                                            RegAllCDStats.AddIntCDMacroState(keyCDName, "Active");
                                                            
                                                        } else {
                                                            cdins.setState("Sleep");
                                                            CDObjectsBuffer.AddCDInstancesToBuffer(keyCDName, cdins);
                                                            RegAllCDStats.AddIntCDMacroState(keyCDName, "Sleep");
                                                            
                                                        }
                                                        if(MigType.equalsIgnoreCase("") || OriginSS.equalsIgnoreCase("")){
                                                            SJSSCDSignalChannelMap.AddOneCDToLocalCurrSignalChannelMap(keyCDName,keyCurrSS , CDConf);
                                                        }
                                                    im.setChannelInstances(channels);
                                                    //System.out.println("AddCD Check 6 Got here ");
                                                    vec.addElement(im);
                                                    vec.addElement(sc);
                                return vec;
        }
    
    
        public Scheduler ResumeCD(JSONObject jsLocalCDs, String keyCurrSS,String keyCDName, Scheduler sc) throws JSONException, Exception
        {
                                                    ClockDomain cdins = CDObjectsBuffer.GetCDInstancesFromBuffer(keyCDName);
                                                    JSONObject jsSigsChans = jsLocalCDs.getJSONObject(keyCDName);
                                                            JSONObject jsSigs = jsSigsChans.getJSONObject("signals");
                                                                JSONObject jsSigsInputs = jsSigs.getJSONObject("inputs");
                                                                Enumeration keysSigsInputsName = jsSigsInputs.keys();
                                                                while (keysSigsInputsName.hasMoreElements()){
                                                                    String SigsInputsName = keysSigsInputsName.nextElement().toString();
                                                                    Field f = cdins.getClass().getField(SigsInputsName);
                                                                    Signal signal = (Signal)f.get(cdins);
                                                                    signal.setuphook();
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
                                                                    gss.reinstatePhySig();
                                                                //}
                                                            }
                                                    cdins.setState("Active");
                                                    sc.addClockDomain(cdins);
                                                    CDObjectsBuffer.RemoveCDInstancesFromBuffer(keyCDName);
                                                    
                                RegAllCDStats.AddIntCDMacroState(keyCDName, "Active");
                                return sc;
        }
        
        public Scheduler disableCD(JSONObject jsLocalCDs, String keyCurrSS,String keyCDName, Scheduler sc) throws JSONException, Exception
        {
                                                JSONObject jsSigsChans = jsLocalCDs.getJSONObject(keyCDName);
                                                ClockDomain cdins = sc.getClockDomain(keyCDName);
                                                
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
                                                cdins.setState("Sleep");
                                                sc.removeClockDomain(keyCDName);
                                                CDObjectsBuffer.AddCDInstancesToBuffer(keyCDName, cdins);
                                                RegAllCDStats.AddIntCDMacroState(keyCDName, "Sleep");
                                                return sc;
        }
        
        public Vector removeCD(JSONObject jsLocalCDs, String keyCurrSS,String keyCDName, String CDState,InterfaceManager im, Scheduler sc) throws JSONException, Exception
        {
            Vector vec = new Vector();
            if(jsLocalCDs.has(keyCDName)){
                 JSONObject jsSigsChans = jsLocalCDs.getJSONObject(keyCDName);
                                                Hashtable channels = im.getAllChannelInstances();
                                                ClockDomain cdins = null;
                                                if(CDState.equalsIgnoreCase("Active")){
                                                   cdins = sc.getClockDomain(keyCDName);
                                                } else {
                                                   cdins = CDObjectsBuffer.GetCDInstancesFromBuffer(keyCDName);
                                                }
                                                    JSONObject jsSigs = jsSigsChans.getJSONObject("signals");
                                                            JSONObject jsSigsInputs = jsSigs.getJSONObject("inputs");
                                                            Enumeration keysSigsInputsName = jsSigsInputs.keys();
                                                            while (keysSigsInputsName.hasMoreElements()){
                                                                String SigsInputsName = keysSigsInputsName.nextElement().toString();
                                                                if (CDState.equals("Active")){
                                                                    Field f = cdins.getClass().getField(SigsInputsName);
                                                                    Signal signal = (Signal)f.get(cdins);
                                                                    signal.killInputSignalThread();
                                                                    if(signal.IsInputSignalPhyThreadContLoop()){
                                                                        while(!signal.getInputSignalThreadTerminated()){}
                                                                    }
                                                                    //while(!signal.getInputSignalThreadTerminated()){}
                                                                }
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
                                        JSONObject jsSigsChansInd = jsSigsChans.getJSONObject("SChannels");
                                                    JSONObject jsSChansInputs = jsSigsChansInd.getJSONObject("inputs");
                                                    Enumeration keysSChansInputsName = jsSChansInputs.keys();
                                                    while (keysSChansInputsName.hasMoreElements()){
                                                        String SChansInputsName = keysSChansInputsName.nextElement().toString();
                                                        JSONObject SChansInputConfigs = jsSChansInputs.getJSONObject(SChansInputsName);
                                                            String cname = SChansInputsName.trim()+"_in";
                                                            String pname = SChansInputConfigs.getString("From").trim()+"_o";
                                                            String[] pnames = pname.split("\\.");
                                                            if(keyCurrSS.equals(im.getCDLocation(pnames[0])) || pnames[0].equalsIgnoreCase("")){
                                                                Field f = cdins.getClass().getField(cname);
                                                                input_Channel inchan = (input_Channel) f.get(cdins);
                                                                output_Channel ochan;
                                                                  if(keyCurrSS.equals(im.getCDLocation(pnames[0]))){
                                                                      ClockDomain partnercd = null;
                                                                        if(sc.SchedulerHasCD(pnames[0])){
                                                                                partnercd = sc.getClockDomain(pnames[0]);
                                                                            } else if (CDObjectsBuffer.CDObjBufferHas(pnames[0])){
                                                                                partnercd = CDObjectsBuffer.GetCDInstancesFromBuffer(pnames[0]);
                                                                            }
                                                                        Field f2 = partnercd.getClass().getField(pnames[1]);
                                                                        ochan = (output_Channel) f2.get(partnercd);
                                                                  } else {
                                                                      ochan = new output_Channel();
                                                                      //ochan.set_partner_smp(new input_Channel());
                                                                  }
                                                                      ochan.set_partner_smp(new input_Channel());
                                                                      ochan.forcePreempt();
                                                                      channels.remove(inchan.Name);
                                                                }
                                                                    else{
                                                                        Field f = cdins.getClass().getField(cname);
                                                                        input_Channel inchan =(input_Channel)f.get(cdins);
                                                                        if(!inchan.IsChannelLocal()){
                                                                            inchan.setPreemptChan(true);
                                                                        }
                                                                        channels.remove(inchan.Name);
                                                                    }
                                                            }
                                                JSONObject jsSChansOutputs = jsSigsChansInd.getJSONObject("outputs");
                                                Enumeration keysChansOutputsName = jsSChansOutputs.keys();
                                                    while (keysChansOutputsName.hasMoreElements()){
                                                        String SChansOutputsName = keysChansOutputsName.nextElement().toString();
                                                        JSONObject SigOutputConfigs = jsSChansOutputs.getJSONObject(SChansOutputsName);
                                                        String cname = SChansOutputsName.trim()+"_o";
                                                        String pname = SigOutputConfigs.getString("To").trim()+"_in";
                                                        String[] pnames = pname.split("\\.");
                                                    if(keyCurrSS.equals(im.getCDLocation(pnames[0])) || pnames[0].equalsIgnoreCase("")){
                                                        
                                                            Field f = cdins.getClass().getField(cname);
                                                            output_Channel ochan = (output_Channel) f.get(cdins);
                                                            input_Channel inchan;
                                                            if(keyCurrSS.equals(im.getCDLocation(pnames[0]))){
                                                                ClockDomain partnercd = null;
                                                                if(sc.SchedulerHasCD(pnames[0])){
                                                                    partnercd = sc.getClockDomain(pnames[0]);
                                                                } else if (CDObjectsBuffer.CDObjBufferHas(pnames[0])){
                                                                    partnercd = CDObjectsBuffer.GetCDInstancesFromBuffer(pnames[0]);
                                                                }
                                                                Field f2 = partnercd.getClass().getField(pnames[1]);
                                                                inchan = (input_Channel) f2.get(partnercd);
                                                            } else {
                                                                inchan = new input_Channel();
                                                            }
                                                            inchan.set_partner_smp(new output_Channel());
                                                            inchan.forcePreempt();
                                                            inchan.setOccupiedStat(false);
                                                            channels.remove(ochan.Name);
                                                      } else {
                                                                        Field f = cdins.getClass().getField(cname);
                                                                        output_Channel ochan = (output_Channel) f.get(cdins);
                                                                        ochan.setChannelCDState("Killed");
                                                                        ochan.setPreemptChan(true);
                                                                        ochan.TransmitResetOccupyChan(keyCurrSS);
                                                                        channels.remove(ochan.Name);
                                                         }
                                                    }
                                                    if(CDState.equalsIgnoreCase("Active")){
                                                        sc.removeClockDomain(keyCDName);
                                                    } else {
                                                        CDObjectsBuffer.RemoveCDInstancesFromBuffer(keyCDName);
                                                    }
                                                  im.setChannelInstances(channels);
                                                }
                                                vec.addElement(im);
                                                vec.addElement(sc);
                                                RegAllCDStats.RemoveIntCDMacroState(keyCDName);
                                                SOABuffer.removeAdvStatOfCDName(keyCDName);
                                                return vec;
        }
        
        public ClockDomain NullifySigPhyIntfForMigration(JSONObject jsLocalCDs, String keyCurrSS,String keyCDName, ClockDomain cdins, InterfaceManager im) throws JSONException, Exception
        {
            
            Hashtable channels = im.getAllChannelInstances();
            
                                    //im.addCDLocation(keyCurrSS, keyCDName);
                                                
                                                    //ClockDomain cdins = SJSSCDSignalChannelMap.GetCDInstancesFromMap(keyCDName);

                                                    JSONObject jsSigsChans = jsLocalCDs.getJSONObject(keyCDName);

                                                  //  Enumeration keysSigsChans = jsSigsChans.keys();

                                                   // while (keysSigsChans.hasMoreElements()){

                                                        //String tagSigsChans = keysSigsChans.nextElement().toString();

                                                     //   if (tagSigsChans.equalsIgnoreCase("signals")){

                                                            JSONObject jsSigsChansInd = jsSigsChans.getJSONObject("signals");

                                                            //GenericSignalReceiver server = null;
                                                            //GenericSignalSender client = null;
                                                            //Hashtable config = new Hashtable();

                                                            //Enumeration keysSigsInOuts = jsSigsChansInd.keys();

                                                        //    while (keysSigsInOuts.hasMoreElements()){

                                                                //String keyInOut = keysSigsInOuts.nextElement().toString();

                                                              //  if (keyInOut.equalsIgnoreCase("inputs")){

                                                                JSONObject jsSigsInputs = jsSigsChansInd.getJSONObject("inputs");

                                                                Enumeration keysSigsInputsName = jsSigsInputs.keys();

                                                                while (keysSigsInputsName.hasMoreElements()){

                                                                    String SigsInputsName = keysSigsInputsName.nextElement().toString();

                                                                    //Object classNewInst = (Object) Class.forName(config.get("Class").toString()).newInstance();

                                                            //server = (GenericSignalReceiver) classNewInst;
                                                            

                                                            // Reflection !!
                                                            Field f = cdins.getClass().getField(SigsInputsName);
                                                            Signal signal = (Signal)f.get(cdins);
                                                            //signal.setServer(server);
                                                            //signal.setuphook();
                                                            signal.disableInit();
                                                            signal.nullifyServer();

                                                            //SignalObjBuffer.putInputSignalClassInstanceToMap((Object) signal, keyCurrSS, keyCDName, SigsInputsName);
                                                            //SignalObjBuffer.putInputSignalGSRInstanceToMap((Object) server, keyCurrSS, keyCDName, SigsInputsName);

                                                        }

                                               // } 
                                                    //            else if (keyInOut.equalsIgnoreCase("outputs")){

                                                    JSONObject jsSigsOutputs = jsSigsChansInd.getJSONObject("outputs");

                                                    Enumeration keysSigsOutputsName = jsSigsOutputs.keys();

                                                        while (keysSigsOutputsName.hasMoreElements()){

                                                            String SigsOutputsName = keysSigsOutputsName.nextElement().toString();

                                                            // Reflection !!
                                                            Field f = cdins.getClass().getField(SigsOutputsName);
                                                            Signal signal = (Signal)f.get(cdins);
                                                            signal.disableInit();
                                                            signal.nullifyClient();

                                                        }

                                                //}

                                           // }

                                       // } 
                                       

                                    //}
                                                    
                                                    //ClockDomainLifeCycleStatusRepository.AddCDNameAndStatus(keyCDName, "Active");
                                                    
                                                    im.setChannelInstances(channels);
                                                    
                                                    return cdins;
                                                    
        }
        
        
        private void TransmitOccupyChan(Object[] data, String ip, int port){
            try {
                    //if(IsLocal){
                        //Socket client = new Socket(ip, port);
                        
                   // if(client ==null){
                        Socket client = new Socket(ip, port);
                   // } else 
                   // {
                        ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
			out.writeObject(data);
                        out.flush();
                        
                       
			client.close();
                   // }
                    
			// Uses simple object output stream. no hassle.
			
                    
		}
		catch(java.net.ConnectException e){
			System.out.println("Could not reach server "+ip+":"+port);
                        //e.printStackTrace();
			//return false;
		}
                catch(java.net.SocketException e){
                        //e.printStackTrace();
                        System.out.println("Cannot bind"+ip+":"+port);
			//return false;
                }
		catch(Exception e){
			e.printStackTrace();
			//return false;
		}
		
		//return true;
        }       
}



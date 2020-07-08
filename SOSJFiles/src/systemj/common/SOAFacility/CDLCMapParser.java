package systemj.common.SOAFacility;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.json.me.*;
import systemj.common.IMBuffer;
import systemj.common.Interconnection;
import systemj.common.InterfaceManager;
import systemj.common.SJSSCDSignalChannelMap;
import systemj.interfaces.GenericInterface;
import systemj.interfaces.GenericSignalReceiver;
import systemj.interfaces.GenericSignalSender;
public class CDLCMapParser{
JSONObject jsParsedCD = new JSONObject();
JSONObject jsParsedSS = new JSONObject();
JSONObject jsAllSSName = new JSONObject();
public JSONObject parse(String file) throws Exception{
        SAXBuilder builder = new SAXBuilder();
        Document doc;
                File f = new File(file);
                doc = builder.build(f);
        Element e = doc.getRootElement();
        //parseInterconnection(e);
        JSONObject jsCDMap = parseCDs(e);
        return jsCDMap;
}
public JSONObject parse(String CDName, String ClassName, String file) throws Exception{
        SAXBuilder builder = new SAXBuilder();
        Document doc;
                File f = new File(file);
                doc = builder.build(f);
        Element e = doc.getRootElement();
        //parseInterconnection(e);
        JSONObject jsCDMap = parseCDs(CDName,ClassName, e);
        return jsCDMap;
}

public JSONObject parse(String CDName, String ClassName, String inchanName,String file) throws Exception{
        SAXBuilder builder = new SAXBuilder();
        Document doc;
                File f = new File(file);
                doc = builder.build(f);
        Element e = doc.getRootElement();
        //parseInterconnection(e);
        JSONObject jsCDMap = parseCDs(CDName,ClassName, inchanName,e);
        return jsCDMap;
}

public JSONObject parse(String CDName, String ClassName, String inchanName,String ochanName,String file) throws Exception{
        SAXBuilder builder = new SAXBuilder();
        Document doc;
                File f = new File(file);
                doc = builder.build(f);
        Element e = doc.getRootElement();
        //parseInterconnection(e);
        JSONObject jsCDMap = parseCDs(CDName,ClassName, inchanName,ochanName,e);
        return jsCDMap;
}

public JSONObject parse(String CDName, String file) throws Exception{
        SAXBuilder builder = new SAXBuilder();
        Document doc;
                File f = new File(file);
                doc = builder.build(f);
        Element e = doc.getRootElement();
        //parseInterconnection(e);
        JSONObject jsCDMap = parseCDs(CDName,e);
        return jsCDMap;
}
public JSONObject parseCDs(String CDName, String ClassName,Element subsystem){
        List<Element> cds = subsystem.getChildren("ClockDomain");
        Hashtable clockdomains = new Hashtable();
        Hashtable channels = new Hashtable();
        JSONObject jsCDtot = new JSONObject();
                for(Element cd : cds){
                    jsCDtot = parseClockDomain(CDName, ClassName,cd,channels, clockdomains);
                }
        return jsCDtot;
}

public JSONObject parseCDs(String CDName, String ClassName,String inchanName,Element subsystem){
        List<Element> cds = subsystem.getChildren("ClockDomain");
        Hashtable clockdomains = new Hashtable();
        Hashtable channels = new Hashtable();
        JSONObject jsCDtot = new JSONObject();
                for(Element cd : cds){
                    jsCDtot = parseClockDomain(CDName, ClassName,inchanName,cd,channels, clockdomains);
                }
        return jsCDtot;
}
public JSONObject parseCDs(String CDName, String ClassName,String inchanName,String ochanName,Element subsystem){
        List<Element> cds = subsystem.getChildren("ClockDomain");
        Hashtable clockdomains = new Hashtable();
        Hashtable channels = new Hashtable();
        JSONObject jsCDtot = new JSONObject();
                for(Element cd : cds){
                    jsCDtot = parseClockDomain(CDName, ClassName,inchanName,ochanName,cd,channels, clockdomains);
                }
        return jsCDtot;
}


public JSONObject parseCDs(String CDName, Element subsystem){
        List<Element> cds = subsystem.getChildren("ClockDomain");
        JSONObject jsCDtot = new JSONObject();
                for(Element cd : cds){
                    jsCDtot = parseClockDomain(CDName, cd);
                }
        return jsCDtot;
}

public JSONObject parseCDs(Element subsystem){
        List<Element> cds = subsystem.getChildren("ClockDomain");
        JSONObject jsCDtot = new JSONObject();
                for(Element cd : cds){
                    jsCDtot = parseClockDomain(cd);
                }
        return jsCDtot;
}
/*
public void parseInterconnection(Element el){
        List<Element> p = el.getChildren("Interconnection");
        if(p.size() > 0){
            Element q = p.get(0);
        //List<Element> l = q.getChildren("Link");
       
        
        for(Element link : l){
                Interconnection.Link linko = new Interconnection.Link();
                
                List<Element> intfs = link.getChildren("Interface");
               // Vector SSNameColl = new Vector();
                for(Element intf : intfs){
                    
                        String SS = intf.getAttributeValue("SubSystem");
                        String clazz = intf.getAttributeValue("Class");
                        String interf = intf.getAttributeValue("Interface");
                        String args = intf.getAttributeValue("Args");
                        
                        if(SS == null || clazz == null || intf == null || args == null)
                                throw new RuntimeException("Interface should have the following elements : SubSystem, Class, Interface, Args");

                        try {
                                
                                        GenericInterface gct = (GenericInterface)Class.forName(clazz).newInstance();
                                        Hashtable ht = new Hashtable();
                                        ht.put("Class", clazz);
                                        ht.put("Interface", interf);
                                        ht.put("Args", args);
                                        ht.put("SubSystem", SS);
                                        gct.configure(ht);
                                        linko.addInterface(SS, gct);
                                       // SSNameColl.addElement(SS);
                                        SJSSCDSignalChannelMap.AddGenericInterfaceDet(SS, clazz, interf, args, link.getAttributeValue("Type"));
                                        //SJSSCDSignalChannelMap.addInterconnectionLink(SS,(Object) linko);   //link of a specific SS
                                
                        } catch(Exception e){
                                e.printStackTrace();
                                System.exit(1);
                        }
                }
                
                
                
                String type = link.getAttributeValue("Type") ;
                if(type == null)
                        throw new RuntimeException("Link 'Type' Missing");
                else if(type.equals("Local")){
                        ic.addLink(linko, true);
                }
                else if(type.equals("Destination")){
                        ic.addLink(linko, false);
                }
                else
                        throw new RuntimeException("Unrecognized Link Type : "+type);
        }
        
       
        /
        //im.setInterconnection(ic);
       
        //IMBuffer.SaveInterfaceManagerConfig(im);

       // ic.printInterconnection(); // Debug
            
        }
        

}
*/

/**
 * 
 * Parses clock-domain and initializes interface ports using reflection
 * 
     * @param CDName
 * @param cd
 * @param subsystem
 * @param ClassName
 * @return ClockDomain instance initialized from this method call
 */
public JSONObject parseClockDomain(String CDName, String ClassName, Element cd, Hashtable channels, Hashtable clockdomains){

    JSONObject jsSigInOut = new JSONObject();

    JSONObject jsSChanInOut = new JSONObject();
    JSONObject jsSigsChans = new JSONObject();

    JSONObject jsAChanInOut = new JSONObject();
    //JSONObject jsAChans = new JSONObject();
    
    JSONObject jsCDSigsChans = new JSONObject();
    
        String CDClassName = ClassName;
        //Element signalChannel = cd.getChild("SignalChannel");
        List <Element> ports = cd.getChild("SignalChannel").getChildren();

        
        if (ports.isEmpty() || ports==null){
            throw new RuntimeException("'SignalChannel' attribute doesn't exist in CD: " + cd.getName()+ "please check your xml file");
        }
        //System.out.println("ports total:" +ports.size());
        


    // end Udayanto modif

        

        //List<Element> ports = cd.getChildren();

        // Udayanto modification --> save service description and to make the information easily accessible by other classes
        // obtain all available service in a subsystem\program (internal on a node) and to make it available on a registry that other machines could get that information

    //    for (Element port : ports){  

            //System.out.println("port value: " +port.);

   //         if (port.getName().equals("serviceDescription")){
      //          servChecker=true;
     //       }
     //   }

        

        //end all

        JSONObject jsSigIn = new JSONObject();
                JSONObject jsSigOut = new JSONObject();
                JSONObject jsSChanInput = new JSONObject();
                JSONObject jsSChanOutput = new JSONObject();
                JSONObject jsAChanInput = new JSONObject();
                JSONObject jsAChanOutput = new JSONObject();
        
        for(Element port : ports){ //for each clock domain obtain signal/channel port config
                
                //System.out.println("ports:" +port.getName());
            
            /*
                if(port.getName().equals("iSignal") || port.getName().equals("oSignal")){
                        if(port.getAttributeValue("Class") == null || port.getAttributeValue("Name") == null)
                                throw new RuntimeException("Interface signals must have both Name and Class attribute");
                }
                */
            
                if(port.getName().equals("iSignal")){
                    
                    if(port.getAttributeValue("Name")==null){
                        throw new RuntimeException("Interface signals must have Name)");
                    }
                    
                    if(!port.getAttributeValue("Name").equals("SOSJDiscReply") && port.getAttributeValue("Class") == null){
                        throw new RuntimeException("Interface signals must have both Name and Class attribute");
                    }
                    
                } else if (port.getName().equals("oSignal")){
                    
                   // if(port.getAttributeValue("Class") == null || port.getAttributeValue("Name") == null)
                         ///       throw new RuntimeException("Interface signals must have both Name and Class attribute");
                    
                    if(port.getAttributeValue("Name")==null){
                        throw new RuntimeException("Interface signals must have Name)");
                    }
                    
                    if(!port.getAttributeValue("Name").equals("SOSJDisc") && port.getAttributeValue("Class") == null){
                        throw new RuntimeException("Interface signals must have both Name and Class attribute");
                    }
                    
                }
            
                else if(port.getName().equals("iChannel") || port.getName().equals("iAChannel")){
                        if(port.getAttributeValue("From") == null || port.getAttributeValue("Name") == null)
                                throw new RuntimeException("Input channels must have both Name and From attributes");
                }
                else if(port.getName().equals("oChannel") || port.getName().equals("oAChannel")){
                        if(port.getAttributeValue("To") == null || port.getAttributeValue("Name") == null)
                                throw new RuntimeException("Output channels must have both Name and To attributes");
                }

                //.....///
                //Udayanto modification ,  start here to add SOA attributes


                //....///
                // End here


                GenericSignalReceiver server = null;
                GenericSignalSender client = null;


                String portname = port.getName();

                try {
                        List<Attribute> attributes = port.getAttributes();

                        Hashtable config = new Hashtable();
                        JSONObject jsSigChan = new JSONObject();

                       
                        for(Attribute attribute : attributes){
                                config.put(attribute.getName(), attribute.getValue());
                                
                                jsSigChan.put(attribute.getName(), attribute.getValue());
                                
                        }
                        
                        // Udayanto modification , get SOA attribute for all signals and channels



                //	if (port.getName().equals("serviceDescription")){



                                //List<Attribute> SOAAttributes = ports.get(ports.indexOf("ServiceDescription")).getAttributes();  //service description information is passed to signal/channel classes in case needed

                    //            List<Attribute> SOAAttributes = port.getAttributes();
                                        //ports.get(ports.indexOf("ServiceDescription")).getAttributes();  //service description information is passed to signal/channel classes in case needed

                                //if (!SOAAttributes.contains("NodeIPAddress")){

                                //}

                //		for (Attribute SOAAttribute : SOAAttributes){


                    //                    localServAttr.put(SOAAttribute.getName(), SOAAttribute.getValue());
                                        //localServAttr.clear();
                //			config.put(SOAAttribute.getName(), SOAAttribute.getValue());
                        //		mp.toBuffer(cn+".put(\""+SOAAttribute.getName()+"\", \""+SOAAttribute.getValue()+"\");"); //?? what is this
                                       // if (SOAAttribute.getName().trim().equalsIgnoreCase("NodeIPAddress")){
                                       //     IPChecker = true;
                                      //  }
                        //	}

                            //    if (IPChecker==false){
                            //        throw new RuntimeException("'NodeIPAddress' attribute needs to be included in the service description, which is the node IP address (IPv4)");
                             //   }

                //	}

                        // end here

                        if(portname.equals("iSignal")){
                                
                                jsSigIn.put(port.getAttributeValue("Name"),jsSigChan);

                               //System.out.println("iSignal detected");
                                
                        }
                        else if(portname.equals("oSignal")){
                            
                                jsSigOut.put(port.getAttributeValue("Name"),jsSigChan);
                            
                                
                        }

                        else if(portname.equals("iChannel")){
                            
                                jsSChanInput.put(port.getAttributeValue("Name"),jsSigChan);
                            
                                

                        }
                        else if(portname.equals("oChannel")){
                            
                                jsSChanOutput.put(port.getAttributeValue("Name"),jsSigChan);
                            
                        }
                        else if(portname.equals("iAChannel")|| portname.equals("oAChannel")){
                               
                                if(portname.equals("iAChannel")){
                                    
                                        
                                        jsAChanInput.put(port.getAttributeValue("Name"),jsSigChan);
                                }
                                else
                                        jsAChanOutput.put(port.getAttributeValue("Name"),jsSigChan);
                                
                                
                                        
                                
                        }
                } catch(Exception e){
                        e.printStackTrace();
                        System.exit(1);
                }
        }
        
    try {
        jsSigInOut.put("inputs",jsSigIn);
        jsSigInOut.put("outputs",jsSigOut);
        //jsSigs.put("signals",jsSigInOut);
        jsSChanInOut.put("inputs",jsSChanInput);
        jsSChanInOut.put("outputs",jsSChanOutput);
        
        jsAChanInOut.put("inputs", jsAChanInput);
        jsAChanInOut.put("outputs",jsAChanOutput);
        //jsSChans.put("SChannels", jsSChanInOut);
        //jsAChans.put("AChannels",hfefe);
        
        jsSigsChans.put("signals",jsSigInOut);
        jsSigsChans.put("SChannels",jsSChanInOut);
        jsSigsChans.put("AChannels",jsAChanInOut);
        //jsSigsChans.put("CDSSLocation",ssname);
        jsSigsChans.put("CDClassName",CDClassName);
        //jsSigsChans.put("CDLifeStatus","Alive");
        
        //jsSigsChans.put("AChannels",jsSigInOut);
        
        jsCDSigsChans.put(CDName,jsSigsChans);
        
        

        
        
        //jsParsedCD = jsCDSigsChans;
    } catch (JSONException ex) {
        
        ex.printStackTrace();
    }
    
    //System.out.println("CDLCMapParser Mapped signals channels one CD:" +jsCDSigsChans);
        return jsCDSigsChans;
}

public JSONObject parseClockDomain(String CDName, String ClassName, String inchanName,Element cd, Hashtable channels, Hashtable clockdomains){

    JSONObject jsSigInOut = new JSONObject();

    JSONObject jsSChanInOut = new JSONObject();
    JSONObject jsSigsChans = new JSONObject();

    JSONObject jsAChanInOut = new JSONObject();
    //JSONObject jsAChans = new JSONObject();
    
    JSONObject jsCDSigsChans = new JSONObject();
    
        String CDClassName = ClassName;
        //Element signalChannel = cd.getChild("SignalChannel");
        List <Element> ports = cd.getChild("SignalChannel").getChildren();

        
        if (ports.isEmpty() || ports==null){
            throw new RuntimeException("'SignalChannel' attribute doesn't exist in CD: " + cd.getName()+ "please check your xml file");
        }
        //System.out.println("ports total:" +ports.size());
        


    // end Udayanto modif

        

        //List<Element> ports = cd.getChildren();

        // Udayanto modification --> save service description and to make the information easily accessible by other classes
        // obtain all available service in a subsystem\program (internal on a node) and to make it available on a registry that other machines could get that information

    //    for (Element port : ports){  

            //System.out.println("port value: " +port.);

   //         if (port.getName().equals("serviceDescription")){
      //          servChecker=true;
     //       }
     //   }

        

        //end all

        JSONObject jsSigIn = new JSONObject();
                JSONObject jsSigOut = new JSONObject();
                JSONObject jsSChanInput = new JSONObject();
                JSONObject jsSChanOutput = new JSONObject();
                JSONObject jsAChanInput = new JSONObject();
                JSONObject jsAChanOutput = new JSONObject();
        
        for(Element port : ports){ //for each clock domain obtain signal/channel port config
                
                //System.out.println("ports:" +port.getName());
            
            /*
                if(port.getName().equals("iSignal") || port.getName().equals("oSignal")){
                        if(port.getAttributeValue("Class") == null || port.getAttributeValue("Name") == null)
                                throw new RuntimeException("Interface signals must have both Name and Class attribute");
                }
                */
            
                if(port.getName().equals("iSignal")){
                    
                    if(port.getAttributeValue("Name")==null){
                        throw new RuntimeException("Interface signals must have Name)");
                    }
                    
                    if(!port.getAttributeValue("Name").equals("SOSJDiscReply") && port.getAttributeValue("Class") == null){
                        throw new RuntimeException("Interface signals must have both Name and Class attribute");
                    }
                    
                } else if (port.getName().equals("oSignal")){
                    
                   // if(port.getAttributeValue("Class") == null || port.getAttributeValue("Name") == null)
                         ///       throw new RuntimeException("Interface signals must have both Name and Class attribute");
                    
                    if(port.getAttributeValue("Name")==null){
                        throw new RuntimeException("Interface signals must have Name)");
                    }
                    
                    if(!port.getAttributeValue("Name").equals("SOSJDisc") && port.getAttributeValue("Class") == null){
                        throw new RuntimeException("Interface signals must have both Name and Class attribute");
                    }
                    
                }
            
                else if(port.getName().equals("iChannel") || port.getName().equals("iAChannel")){
                        if(port.getAttributeValue("From") == null || port.getAttributeValue("Name") == null)
                                throw new RuntimeException("Input channels must have both Name and From attributes");
                }
                else if(port.getName().equals("oChannel") || port.getName().equals("oAChannel")){
                        if(port.getAttributeValue("To") == null || port.getAttributeValue("Name") == null)
                                throw new RuntimeException("Output channels must have both Name and To attributes");
                }

                //.....///
                //Udayanto modification ,  start here to add SOA attributes


                //....///
                // End here


                GenericSignalReceiver server = null;
                GenericSignalSender client = null;


                String portname = port.getName();

                try {
                        List<Attribute> attributes = port.getAttributes();

                        Hashtable config = new Hashtable();
                        JSONObject jsSigChan = new JSONObject();

                       
                        for(Attribute attribute : attributes){
                                config.put(attribute.getName(), attribute.getValue());
                                
                                jsSigChan.put(attribute.getName(), attribute.getValue());
                                
                        }
                        
                        // Udayanto modification , get SOA attribute for all signals and channels



                //	if (port.getName().equals("serviceDescription")){



                                //List<Attribute> SOAAttributes = ports.get(ports.indexOf("ServiceDescription")).getAttributes();  //service description information is passed to signal/channel classes in case needed

                    //            List<Attribute> SOAAttributes = port.getAttributes();
                                        //ports.get(ports.indexOf("ServiceDescription")).getAttributes();  //service description information is passed to signal/channel classes in case needed

                                //if (!SOAAttributes.contains("NodeIPAddress")){

                                //}

                //		for (Attribute SOAAttribute : SOAAttributes){


                    //                    localServAttr.put(SOAAttribute.getName(), SOAAttribute.getValue());
                                        //localServAttr.clear();
                //			config.put(SOAAttribute.getName(), SOAAttribute.getValue());
                        //		mp.toBuffer(cn+".put(\""+SOAAttribute.getName()+"\", \""+SOAAttribute.getValue()+"\");"); //?? what is this
                                       // if (SOAAttribute.getName().trim().equalsIgnoreCase("NodeIPAddress")){
                                       //     IPChecker = true;
                                      //  }
                        //	}

                            //    if (IPChecker==false){
                            //        throw new RuntimeException("'NodeIPAddress' attribute needs to be included in the service description, which is the node IP address (IPv4)");
                             //   }

                //	}

                        // end here

                        if(portname.equals("iSignal")){
                                
                                jsSigIn.put(port.getAttributeValue("Name"),jsSigChan);

                               //System.out.println("iSignal detected");
                                
                        }
                        else if(portname.equals("oSignal")){
                            
                                jsSigOut.put(port.getAttributeValue("Name"),jsSigChan);
                            
                                
                        }

                        else if(portname.equals("iChannel")){
                            
                                jsSChanInput.put(inchanName,jsSigChan);
                            
                                

                        }
                        else if(portname.equals("oChannel")){
                            
                                jsSChanOutput.put(port.getAttributeValue("Name"),jsSigChan);
                            
                        }
                        else if(portname.equals("iAChannel")|| portname.equals("oAChannel")){
                               
                                if(portname.equals("iAChannel")){
                                    
                                        
                                        jsAChanInput.put(port.getAttributeValue("Name"),jsSigChan);
                                }
                                else
                                        jsAChanOutput.put(port.getAttributeValue("Name"),jsSigChan);
                                
                                
                                        
                                
                        }
                } catch(Exception e){
                        e.printStackTrace();
                        System.exit(1);
                }
        }
        
    try {
        jsSigInOut.put("inputs",jsSigIn);
        jsSigInOut.put("outputs",jsSigOut);
        //jsSigs.put("signals",jsSigInOut);
        jsSChanInOut.put("inputs",jsSChanInput);
        jsSChanInOut.put("outputs",jsSChanOutput);
        
        jsAChanInOut.put("inputs", jsAChanInput);
        jsAChanInOut.put("outputs",jsAChanOutput);
        //jsSChans.put("SChannels", jsSChanInOut);
        //jsAChans.put("AChannels",hfefe);
        
        jsSigsChans.put("signals",jsSigInOut);
        jsSigsChans.put("SChannels",jsSChanInOut);
        jsSigsChans.put("AChannels",jsAChanInOut);
        //jsSigsChans.put("CDSSLocation",ssname);
        jsSigsChans.put("CDClassName",CDClassName);
        //jsSigsChans.put("CDLifeStatus","Alive");
        
        //jsSigsChans.put("AChannels",jsSigInOut);
        
        jsCDSigsChans.put(CDName,jsSigsChans);
        
        

        
        
        //jsParsedCD = jsCDSigsChans;
    } catch (JSONException ex) {
        
        ex.printStackTrace();
    }
    
    //System.out.println("CDLCMapParser Mapped signals channels one CD:" +jsCDSigsChans);
        return jsCDSigsChans;
}

public JSONObject parseClockDomain(String CDName, String ClassName, String inchanName,String ochanName,Element cd, Hashtable channels, Hashtable clockdomains){

    JSONObject jsSigInOut = new JSONObject();

    JSONObject jsSChanInOut = new JSONObject();
    JSONObject jsSigsChans = new JSONObject();

    JSONObject jsAChanInOut = new JSONObject();
    //JSONObject jsAChans = new JSONObject();
    
    JSONObject jsCDSigsChans = new JSONObject();
    
        String CDClassName = ClassName;
        //Element signalChannel = cd.getChild("SignalChannel");
        List <Element> ports = cd.getChild("SignalChannel").getChildren();

        
        if (ports.isEmpty() || ports==null){
            throw new RuntimeException("'SignalChannel' attribute doesn't exist in CD: " + cd.getName()+ "please check your xml file");
        }
        //System.out.println("ports total:" +ports.size());
        


    // end Udayanto modif

        

        //List<Element> ports = cd.getChildren();

        // Udayanto modification --> save service description and to make the information easily accessible by other classes
        // obtain all available service in a subsystem\program (internal on a node) and to make it available on a registry that other machines could get that information

    //    for (Element port : ports){  

            //System.out.println("port value: " +port.);

   //         if (port.getName().equals("serviceDescription")){
      //          servChecker=true;
     //       }
     //   }

        

        //end all

        JSONObject jsSigIn = new JSONObject();
                JSONObject jsSigOut = new JSONObject();
                JSONObject jsSChanInput = new JSONObject();
                JSONObject jsSChanOutput = new JSONObject();
                JSONObject jsAChanInput = new JSONObject();
                JSONObject jsAChanOutput = new JSONObject();
        
        for(Element port : ports){ //for each clock domain obtain signal/channel port config
                
                //System.out.println("ports:" +port.getName());
            
            /*
                if(port.getName().equals("iSignal") || port.getName().equals("oSignal")){
                        if(port.getAttributeValue("Class") == null || port.getAttributeValue("Name") == null)
                                throw new RuntimeException("Interface signals must have both Name and Class attribute");
                }
                */
            
                if(port.getName().equals("iSignal")){
                    
                    if(port.getAttributeValue("Name")==null){
                        throw new RuntimeException("Interface signals must have Name)");
                    }
                    
                    if(!port.getAttributeValue("Name").equals("SOSJDiscReply") && port.getAttributeValue("Class") == null){
                        throw new RuntimeException("Interface signals must have both Name and Class attribute");
                    }
                    
                } else if (port.getName().equals("oSignal")){
                    
                   // if(port.getAttributeValue("Class") == null || port.getAttributeValue("Name") == null)
                         ///       throw new RuntimeException("Interface signals must have both Name and Class attribute");
                    
                    if(port.getAttributeValue("Name")==null){
                        throw new RuntimeException("Interface signals must have Name)");
                    }
                    
                    if(!port.getAttributeValue("Name").equals("SOSJDisc") && port.getAttributeValue("Class") == null){
                        throw new RuntimeException("Interface signals must have both Name and Class attribute");
                    }
                    
                }
            
                else if(port.getName().equals("iChannel") || port.getName().equals("iAChannel")){
                        if(port.getAttributeValue("From") == null || port.getAttributeValue("Name") == null)
                                throw new RuntimeException("Input channels must have both Name and From attributes");
                }
                else if(port.getName().equals("oChannel") || port.getName().equals("oAChannel")){
                        if(port.getAttributeValue("To") == null || port.getAttributeValue("Name") == null)
                                throw new RuntimeException("Output channels must have both Name and To attributes");
                }

                //.....///
                //Udayanto modification ,  start here to add SOA attributes


                //....///
                // End here


                GenericSignalReceiver server = null;
                GenericSignalSender client = null;


                String portname = port.getName();

                try {
                        List<Attribute> attributes = port.getAttributes();

                        Hashtable config = new Hashtable();
                        JSONObject jsSigChan = new JSONObject();

                       
                        for(Attribute attribute : attributes){
                                config.put(attribute.getName(), attribute.getValue());
                                
                                jsSigChan.put(attribute.getName(), attribute.getValue());
                                
                        }
                        
                        // Udayanto modification , get SOA attribute for all signals and channels



                //	if (port.getName().equals("serviceDescription")){



                                //List<Attribute> SOAAttributes = ports.get(ports.indexOf("ServiceDescription")).getAttributes();  //service description information is passed to signal/channel classes in case needed

                    //            List<Attribute> SOAAttributes = port.getAttributes();
                                        //ports.get(ports.indexOf("ServiceDescription")).getAttributes();  //service description information is passed to signal/channel classes in case needed

                                //if (!SOAAttributes.contains("NodeIPAddress")){

                                //}

                //		for (Attribute SOAAttribute : SOAAttributes){


                    //                    localServAttr.put(SOAAttribute.getName(), SOAAttribute.getValue());
                                        //localServAttr.clear();
                //			config.put(SOAAttribute.getName(), SOAAttribute.getValue());
                        //		mp.toBuffer(cn+".put(\""+SOAAttribute.getName()+"\", \""+SOAAttribute.getValue()+"\");"); //?? what is this
                                       // if (SOAAttribute.getName().trim().equalsIgnoreCase("NodeIPAddress")){
                                       //     IPChecker = true;
                                      //  }
                        //	}

                            //    if (IPChecker==false){
                            //        throw new RuntimeException("'NodeIPAddress' attribute needs to be included in the service description, which is the node IP address (IPv4)");
                             //   }

                //	}

                        // end here

                        if(portname.equals("iSignal")){
                                
                                jsSigIn.put(port.getAttributeValue("Name"),jsSigChan);

                               //System.out.println("iSignal detected");
                                
                        }
                        else if(portname.equals("oSignal")){
                            
                                jsSigOut.put(port.getAttributeValue("Name"),jsSigChan);
                            
                                
                        }

                        else if(portname.equals("iChannel")){
                            
                                jsSChanInput.put(inchanName,jsSigChan);
                            
                                

                        }
                        else if(portname.equals("oChannel")){
                            
                                jsSChanOutput.put(ochanName,jsSigChan);
                            
                        }
                        else if(portname.equals("iAChannel")|| portname.equals("oAChannel")){
                               
                                if(portname.equals("iAChannel")){
                                    
                                        
                                        jsAChanInput.put(port.getAttributeValue("Name"),jsSigChan);
                                }
                                else
                                        jsAChanOutput.put(port.getAttributeValue("Name"),jsSigChan);
                                
                                
                        }
                } catch(Exception e){
                        e.printStackTrace();
                        System.exit(1);
                }
        }
        
    try {
        jsSigInOut.put("inputs",jsSigIn);
        jsSigInOut.put("outputs",jsSigOut);
        //jsSigs.put("signals",jsSigInOut);
        jsSChanInOut.put("inputs",jsSChanInput);
        jsSChanInOut.put("outputs",jsSChanOutput);
        
        jsAChanInOut.put("inputs", jsAChanInput);
        jsAChanInOut.put("outputs",jsAChanOutput);
        //jsSChans.put("SChannels", jsSChanInOut);
        //jsAChans.put("AChannels",hfefe);
        
        jsSigsChans.put("signals",jsSigInOut);
        jsSigsChans.put("SChannels",jsSChanInOut);
        jsSigsChans.put("AChannels",jsAChanInOut);
        //jsSigsChans.put("CDSSLocation",ssname);
        jsSigsChans.put("CDClassName",CDClassName);
        //jsSigsChans.put("CDLifeStatus","Alive");
        
        //jsSigsChans.put("AChannels",jsSigInOut);
        
        jsCDSigsChans.put(CDName,jsSigsChans);
        
        

        
        
        //jsParsedCD = jsCDSigsChans;
    } catch (JSONException ex) {
        
        ex.printStackTrace();
    }
    
    //System.out.println("CDLCMapParser Mapped signals channels one CD:" +jsCDSigsChans);
        return jsCDSigsChans;
}

/**
 * 
 * Parses clock-domain and initializes interface ports using reflection
 * 
     * @param CDName
 * @param cd
 * @param subsystem
 * @return ClockDomain instance initialized from this method call
 */
public JSONObject parseClockDomain(String CDName, Element cd){
    JSONObject jsSigInOut = new JSONObject();
    JSONObject jsSChanInOut = new JSONObject();
    JSONObject jsSigsChans = new JSONObject();
    JSONObject jsAChanInOut = new JSONObject();
    JSONObject jsCDSigsChans = new JSONObject();
        String CDClassName = cd.getAttributeValue("Class");
        List <Element> ports = cd.getChild("SignalChannel").getChildren();
        if (ports.isEmpty() || ports==null){
            throw new RuntimeException("'SignalChannel' attribute doesn't exist in CD: " + cd.getName()+ "please check your xml file");
        }
        JSONObject jsSigIn = new JSONObject();
                JSONObject jsSigOut = new JSONObject();
                JSONObject jsSChanInput = new JSONObject();
                JSONObject jsSChanOutput = new JSONObject();
                JSONObject jsAChanInput = new JSONObject();
                JSONObject jsAChanOutput = new JSONObject();
        for(Element port : ports){ 
            if(port.getName().equals("iSignal")){
                    if(port.getAttributeValue("Name")==null){
                        throw new RuntimeException("Interface signals must have Name)");
                    }
                    if(!port.getAttributeValue("Name").equals("SOSJDiscReply") && port.getAttributeValue("Class") == null){
                        throw new RuntimeException("Interface signals must have both Name and Class attribute");
                    }
                } else if (port.getName().equals("oSignal")){
                    if(port.getAttributeValue("Name")==null){
                        throw new RuntimeException("Interface signals must have Name)");
                    }
                    if(!port.getAttributeValue("Name").equals("SOSJDisc") && port.getAttributeValue("Class") == null){
                        throw new RuntimeException("Interface signals must have both Name and Class attribute");
                    }
                }
                else if(port.getName().equals("iChannel") || port.getName().equals("iAChannel")){
                        if(port.getAttributeValue("From") == null || port.getAttributeValue("Name") == null)
                                throw new RuntimeException("Input channels must have both Name and From attributes");
                }
                else if(port.getName().equals("oChannel") || port.getName().equals("oAChannel")){
                        if(port.getAttributeValue("To") == null || port.getAttributeValue("Name") == null)
                                throw new RuntimeException("Output channels must have both Name and To attributes");
                }
                String portname = port.getName();
                try {
                        List<Attribute> attributes = port.getAttributes();
                        JSONObject jsSigChan = new JSONObject();
                        for(Attribute attribute : attributes){
                                jsSigChan.put(attribute.getName(), attribute.getValue());
                        }
                        if(portname.equals("iSignal")){
                                jsSigIn.put(port.getAttributeValue("Name"),jsSigChan);
                        }
                        else if(portname.equals("oSignal")){
                                jsSigOut.put(port.getAttributeValue("Name"),jsSigChan);
                        }
                        else if(portname.equals("iChannel")){
                                jsSChanInput.put(port.getAttributeValue("Name"),jsSigChan);
                        }
                        else if(portname.equals("oChannel")){
                                jsSChanOutput.put(port.getAttributeValue("Name"),jsSigChan);
                        }
                        else if(portname.equals("iAChannel")|| portname.equals("oAChannel")){
                                if(portname.equals("iAChannel")){
                                        jsAChanInput.put(port.getAttributeValue("Name"),jsSigChan);
                                }
                                else
                                        jsAChanOutput.put(port.getAttributeValue("Name"),jsSigChan);
                        }
                } catch(Exception e){
                        e.printStackTrace();
                }
        }
        
    try {
        jsSigInOut.put("inputs",jsSigIn);
        jsSigInOut.put("outputs",jsSigOut);
        jsSChanInOut.put("inputs",jsSChanInput);
        jsSChanInOut.put("outputs",jsSChanOutput);
        jsAChanInOut.put("inputs", jsAChanInput);
        jsAChanInOut.put("outputs",jsAChanOutput);
        jsSigsChans.put("signals",jsSigInOut);
        jsSigsChans.put("SChannels",jsSChanInOut);
        jsSigsChans.put("AChannels",jsAChanInOut);
        jsSigsChans.put("CDClassName",CDClassName);
        jsCDSigsChans.put(CDName,jsSigsChans);
    } catch (JSONException ex) {
        ex.printStackTrace();
    }
        return jsCDSigsChans;
}
/**
 * 
 * Parses clock-domain and initializes interface ports using reflection
 * 
 * @param cd
 * @param subsystem
 * @return ClockDomain instance initialized from this method call
 */
public JSONObject parseClockDomain(Element cd){
    JSONObject jsSigInOut = new JSONObject();
    JSONObject jsSChanInOut = new JSONObject();
    JSONObject jsSigsChans = new JSONObject();
    JSONObject jsAChanInOut = new JSONObject();
    JSONObject jsCDSigsChans = new JSONObject();
        String cdname = cd.getAttributeValue("Name");
        String CDClassName = cd.getAttributeValue("Class");
        List <Element> ports = cd.getChild("SignalChannel").getChildren();
        if (ports.isEmpty() || ports==null){
            throw new RuntimeException("'SignalChannel' attribute doesn't exist in CD: " + cd.getName()+ "please check your xml file");
        }
        JSONObject jsSigIn = new JSONObject();
                JSONObject jsSigOut = new JSONObject();
                JSONObject jsSChanInput = new JSONObject();
                JSONObject jsSChanOutput = new JSONObject();
                JSONObject jsAChanInput = new JSONObject();
                JSONObject jsAChanOutput = new JSONObject();
        for(Element port : ports){ 
            if(port.getName().equals("iSignal")){
                    if(port.getAttributeValue("Name")==null){
                        throw new RuntimeException("Interface signals must have Name)");
                    }
                    if(!port.getAttributeValue("Name").equals("SOSJDiscReply") && port.getAttributeValue("Class") == null){
                        throw new RuntimeException("Interface signals must have both Name and Class attribute");
                    }
                } else if (port.getName().equals("oSignal")){
                    if(port.getAttributeValue("Name")==null){
                        throw new RuntimeException("Interface signals must have Name)");
                    }
                    if(!port.getAttributeValue("Name").equals("SOSJDisc") && port.getAttributeValue("Class") == null){
                        throw new RuntimeException("Interface signals must have both Name and Class attribute");
                    }
                }
                else if(port.getName().equals("iChannel") || port.getName().equals("iAChannel")){
                        if(port.getAttributeValue("From") == null || port.getAttributeValue("Name") == null)
                                throw new RuntimeException("Input channels must have both Name and From attributes");
                }
                else if(port.getName().equals("oChannel") || port.getName().equals("oAChannel")){
                        if(port.getAttributeValue("To") == null || port.getAttributeValue("Name") == null)
                                throw new RuntimeException("Output channels must have both Name and To attributes");
                }
                String portname = port.getName();
                try {
                        List<Attribute> attributes = port.getAttributes();
                        JSONObject jsSigChan = new JSONObject();
                        for(Attribute attribute : attributes){
                                jsSigChan.put(attribute.getName(), attribute.getValue());
                        }
                        if(portname.equals("iSignal")){
                                jsSigIn.put(port.getAttributeValue("Name"),jsSigChan);
                        }
                        else if(portname.equals("oSignal")){
                                jsSigOut.put(port.getAttributeValue("Name"),jsSigChan);
                        }
                        else if(portname.equals("iChannel")){
                                jsSChanInput.put(port.getAttributeValue("Name"),jsSigChan);
                        }
                        else if(portname.equals("oChannel")){
                                jsSChanOutput.put(port.getAttributeValue("Name"),jsSigChan);   
                        }
                        else if(portname.equals("iAChannel")|| portname.equals("oAChannel")){
                                if(portname.equals("iAChannel")){
                                        jsAChanInput.put(port.getAttributeValue("Name"),jsSigChan);
                                }
                                else
                                        jsAChanOutput.put(port.getAttributeValue("Name"),jsSigChan);      
                        }
                } catch(Exception e){
                        e.printStackTrace();
                        System.exit(1);
                }
        }
    try {
        jsSigInOut.put("inputs",jsSigIn);
        jsSigInOut.put("outputs",jsSigOut);
        jsSChanInOut.put("inputs",jsSChanInput);
        jsSChanInOut.put("outputs",jsSChanOutput);
        jsAChanInOut.put("inputs", jsAChanInput);
        jsAChanInOut.put("outputs",jsAChanOutput);
        jsSigsChans.put("signals",jsSigInOut);
        jsSigsChans.put("SChannels",jsSChanInOut);
        jsSigsChans.put("AChannels",jsAChanInOut);
        jsSigsChans.put("CDClassName",CDClassName);
        jsCDSigsChans.put(cdname,jsSigsChans);
    } catch (JSONException ex) {
        ex.printStackTrace();
    }
        return jsCDSigsChans;
   }
}

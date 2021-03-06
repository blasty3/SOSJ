package systemj.common;

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import systemj.bootstrap.ClockDomain;
import systemj.common.util.LinkQueue;
import systemj.interfaces.GenericChannel;
import systemj.interfaces.GenericInterface;
import systemj.interfaces.Scheduler;
import systemj.lib.input_Channel;
import systemj.lib.output_Channel;

/**
 * Manages routing
 * 
 * Must be compatible with CLDC 1.1
 * @author hpar081
 *
 */
public class InterfaceManager {
	private String ssname;
	private final LinkQueue OutQueue = new LinkQueue();
        private final LinkQueue StateChangeQueue = new LinkQueue();
	private Vector LocalInterface = new Vector();;
	private Interconnection ic = new Interconnection();
	private Hashtable cdlocation = new Hashtable();
        private static final Object cdlocationLock = new Object();
	private Hashtable chanins = new Hashtable();
	private Hashtable cachedintf = new Hashtable();
	private Vector unsentdata = new Vector();
        private Vector unsentStateChangeData = new Vector();
	private final int MAX_UNSENT_DATA = 100;
        private static final Object TransmitTerminationSignalLock = new Object();
	
	/**
	 * Internal use
	 * @param ci
	 */
	public void addCDLocation(String ss, String cd){
		//if(cdlocation.containsKey(cd))
			//throw new RuntimeException("Tried to add duplicated CD to the map : "+cd);
		//considered overwriting
		cdlocation.put(cd, ss);
                
	}
        
        public boolean hasCDLocation(String cdname){
            if(cdlocation.containsKey(cdname)){
                return true;
            } else {
                return false;
            }
        }
        
        public void removeCDLocation(String cd){
		if(cdlocation.containsKey(cd))
                    cdlocation.remove(cd);
                //else
		//	throw new RuntimeException("Tried to remove non existent CD to the map : "+cd);
		
		
	}
	
	/**
	 * Internal use
	 * @param ci
	 */
	public String getCDLocation(String cd){
            
            String ssLoc = "";
            
            if(cdlocation.containsKey(cd)){
                ssLoc = (String)cdlocation.get(cd);
            }
            
            return ssLoc;
		
	}
        
        public String getAllCDLocation(){
		return cdlocation.toString();
	}
        
        public void removeCDInSS(String ssname){
            synchronized(cdlocationLock){
                Enumeration keysCDName = cdlocation.keys();
                
                while(keysCDName.hasMoreElements()){
                    String cdname = keysCDName.nextElement().toString();
                    
                    if(cdlocation.get(cdname).toString().equals(ssname)){
                        cdlocation.remove(cdname);
                    }
                }
            }
        }
        
        public void updateCDLocationInIMFromServReg(){
            
            synchronized(cdlocationLock){
                try{
                    
                    JSONObject jsAllExtCDsDesc = RegAllCDStats.getAllCDStats();
                    
                    //JSONObject jsAllExtCDsDesc = SJServiceRegistry.obtainAllService(); 

                    Enumeration keysJSAllExtCDs = jsAllExtCDsDesc.keys();

                    while(keysJSAllExtCDs.hasMoreElements()){
                        String ssNameExtCDs = keysJSAllExtCDs.nextElement().toString();

                        JSONObject jsAllServNamesInSS = jsAllExtCDsDesc.getJSONObject(ssNameExtCDs);

                        Enumeration keysAllServNames = jsAllServNamesInSS.keys();

                        while (keysAllServNames.hasMoreElements()){

                            String ServNameExtCD = keysAllServNames.nextElement().toString();

                            //JSONObject jsServCDDet = jsAllServNamesInSS.getJSONObject(ServNameExtCD);

                            //String CDName = jsServCDDet.getString("associatedCDName");

                            //now after getting the CDName, update the IM cd loc

                            cdlocation.put(ServNameExtCD,ssNameExtCDs);

                        }

                    }
                } catch (JSONException jex){
                    jex.printStackTrace();
                }
            }
            
        }
        
        public void updateCDLocationInIMFromServRegSOSJNoP2P(){
            
            synchronized(cdlocationLock){
                try{
                    /*
                    JSONObject jsAllExtCDsDesc = SJServiceRegistry.obtainCurrentRegistry(); 

                    Enumeration keysJSAllExtCDs = jsAllExtCDsDesc.keys();

                    while(keysJSAllExtCDs.hasMoreElements()){
                        String ssNameExtCDs = keysJSAllExtCDs.nextElement().toString();

                        JSONObject jsAllServNamesInSS = jsAllExtCDsDesc.getJSONObject(ssNameExtCDs);

                        Enumeration keysAllServNames = jsAllServNamesInSS.keys();

                        while (keysAllServNames.hasMoreElements()){

                            String ServNameExtCD = keysAllServNames.nextElement().toString();

                            JSONObject jsServCDDet = jsAllServNamesInSS.getJSONObject(ServNameExtCD);

                            String CDName = jsServCDDet.getString("associatedCDName");

                            //now after getting the CDName, update the IM cd loc

                            cdlocation.put(CDName,ssNameExtCDs);

                        }

                    }
                    */
                    
                    JSONObject jsAllExtCDsDesc = RegAllCDStats.getAllCDStats();
                    
                    //JSONObject jsAllExtCDsDesc = SJServiceRegistry.obtainAllService(); 

                    Enumeration keysJSAllExtCDs = jsAllExtCDsDesc.keys();

                    while(keysJSAllExtCDs.hasMoreElements()){
                        String ssNameExtCDs = keysJSAllExtCDs.nextElement().toString();

                        JSONObject jsAllServNamesInSS = jsAllExtCDsDesc.getJSONObject(ssNameExtCDs);

                        Enumeration keysAllServNames = jsAllServNamesInSS.keys();

                        while (keysAllServNames.hasMoreElements()){

                            String ServNameExtCD = keysAllServNames.nextElement().toString();

                            //JSONObject jsServCDDet = jsAllServNamesInSS.getJSONObject(ServNameExtCD);

                            //String CDName = jsServCDDet.getString("associatedCDName");

                            //now after getting the CDName, update the IM cd loc

                            cdlocation.put(ServNameExtCD,ssNameExtCDs);

                        }

                    }
                    
                } catch (JSONException jex){
                    jex.printStackTrace();
                }
            }
            
        }
        
        public boolean IsCDNameRegisteredInAnotherSS(String cdname){
            if(cdlocation.containsKey(cdname)){
                String ssloc = (String)cdlocation.get(cdname);
                
                if(!ssloc.equals(SJSSCDSignalChannelMap.getLocalSSName())){
                    return true;
                } else {
                    return false;
                }
                
            } else {
                return true;
            }
               
        }
	
	/**
	 * Internal use
	 * @param ci
	 */
	public void setChannelInstances(Hashtable ci){
		chanins = ci;
	}
	
	/**
	 * Internal use
	 * @param ci
	 */
	private Object getChannelInstance(String n){
		return chanins.get(n);
	}
        
        public Hashtable getAllChannelInstances(){
            return chanins;
        }
	
	/**
	 * Internal use
	 * @param ci
	 */
	public void setInterconnection(Interconnection ic){
		this.ic = ic;
	}
        
        
        public Interconnection getInterconnection(){
            return ic;
        }
	
	/**
	 * Internal use
	 * @param ci
	 */
	public void setLocalInterface(String ssname){
		LocalInterface = ic.getInterfaces(ssname);
		this.ssname = ssname;
	}

	// invokeReceivingThread() does not have to invoke a thread (implementation dependent)
	/**
	 * Internal use
	 * @param ci
	 */
	public void init(){
		for(int i=0;i<LocalInterface.size(); i++){
			((GenericInterface)LocalInterface.elementAt(i)).invokeReceivingThread();
			((GenericInterface)LocalInterface.elementAt(i)).setInterfaceManager(this);
		}
	}
        
        public void initIM(){
            for(int i=0;i<LocalInterface.size(); i++){
			//((GenericInterface)LocalInterface.elementAt(i)).invokeReceivingThread();
			((GenericInterface)LocalInterface.elementAt(i)).setInterfaceManager(this);
		}
        }
        
        public void terminateInterface(){
            for(int i=0;i<LocalInterface.size(); i++){
			((GenericInterface)LocalInterface.elementAt(i)).TerminateInterface();
			
            }
        }
        
	/**
	 * This is called from channel instances
	 * @param o - An object to be inserted into the queue
	 * @return True when the channel data has been successfully pushed to the queue, false otherwise
	 */
	public boolean pushToQueue(Object o){
		if(OutQueue.isFull())
			return false;
		else
			return OutQueue.push(o);
	}
       

	/**
	 * Internal use
	 * @param ci
	 */
	private synchronized void addToUnsent(Object[] o){
		
		for(int i=0;i<unsentdata.size(); i++){
			if(((Object[])unsentdata.elementAt(i))[0].equals(o[0])){
				unsentdata.setElementAt(o, i);
				return;
			}
		}
	
		if(unsentdata.size() <=0.9*MAX_UNSENT_DATA){
			unsentdata.addElement(o);
                }
                
                //else buffer full, need to stop adding data. Des
			//throw new RuntimeException("Unbounded unsent channel data detected : check XML routing table settings");

	}
        
        private synchronized boolean IsUnsentBufferFull(){
            if(unsentdata.size()==75){
                return true;
            } else {
                return false;
            }
        }
        
       
	/**
	 * Internal use
	 * @param ci
	 */
	private boolean tryToSend(Object[] o){
            /*
            for debugging
                int size = o.length;
                System.out.println("data to send: ");
                for (int i=0;i<size;i++){
                    System.out.println(", "+o[i]);
                }
                */
		String destcd = ((String)o[0]).substring(0, ((String)o[0]).indexOf(".")); // CD name
		String dest = this.getCDLocation(destcd);			   // Corresponding SubSystem name
                
                //System.out.println("InterfaceManager, All CD Location:  "+this.getAllCDLocation());
                //System.out.println("destCD val: " +destcd);
                //Interconnection ic = this.getInterconnection();
               
                //System.out.println("InterfaceManager IC: ");
                //ic.printInterconnection();
               
                //System.out.println("SS location of destCD val: " +dest);
                //System.out.println("PartnerName val: " +(String)o[0]);
                if(dest!=null){
                  //  System.out.println("InterfaceManager TryToSend Debug 1");
                    GenericInterface gi = null;
                
                    if(cachedintf.containsKey(dest)){
			gi = (GenericInterface)cachedintf.get(dest);
			gi.setup(o);
                     //   System.out.println("InterfaceManager TryToSend Debug 2");
                    }
                    else{
                   // System.out.println("InterfaceManager TryToSend Debug 3");
			Vector l = ic.getInterfaces(ssname, dest);
			if(l.size() > 0){
                     //       System.out.println("InterfaceManager TryToSend Debug 4");
				cachedintf.put(dest, l.elementAt(0));
				gi = (GenericInterface)l.elementAt(0);
				gi.setup(o);
                         //       System.out.println("InterfaceManager TryToSend Debug 5");
			}
			else{
                            
				System.err.println("1st cond : SubSystem "+dest+" is not reachable : "+destcd);
				return false;
			}
		}
		//System.out.println("InterfaceManager TryToSend Debug 6");
		if(!gi.transmitData()){
                      // System.out.println("InterfaceManager TryToSend Debug 9");
			Vector l = ic.getInterfaces(ssname, dest);
			for(int i=0;i<l.size(); i++){
                           
				gi = (GenericInterface)l.elementAt(i);
				gi.setup(o);
				if(gi.transmitData()){
                                   
					cachedintf.put(dest, gi);
					return true;
				}
			}
			System.err.println("2nd cond : SubSystem "+dest+" is not reachable : "+destcd);
			return false;
		  } else {
                       // System.out.println("InterfaceManager TryToSend Debug 7");
			return true;
                  }
                    
                } else {
                   // System.out.println("InterfaceManager TryToSend Debug 8");
                    return false;
                }
                
	}
	
	/**
	 * this is called from GenericInterface (both threaded non-threaded)
	 * @param o
	 */
	public void forwardChannelData(Object[] o){
		GenericChannel chan = (GenericChannel)getChannelInstance(((String)o[0]));
		String destcd = ((String)o[0]).substring(0, ((String)o[0]).indexOf("."));
                
                //System.out.println("InterfaceManager, Name:"+o[1]+"PartnerName: " +o[0]+ "Chan: " +chan+ "ChannelInst: " +getAllChannelInstances()+"Msg type: " +o[2]);
                
		if(chan == null){
			if(this.getCDLocation(destcd).equals(ssname)){
                            
				System.out.println("The channel "+o[0]+" not present in the local sub-system - discarding received data");
                        
                                if(o[2].toString().equalsIgnoreCase("OccupyChan")){
                                    Object[] toSend = new Object[4];  // Creating an Object!!
                                    toSend[0] = o[1].toString();
                                    toSend[1] = o[0].toString();
                                    toSend[2] = "RespOccupyChan";
                                    toSend[3] = "false";
                                    pushToQueue(toSend);
                                }
                                
                        } else{
				System.out.println("Trying to re-route channel data "+o[0]);
				this.addToUnsent(o);
                                
			}
                        
                        
		}
                else{
                    //if(o.length>3){
                    //if(o.length==4 && o[2].toString().equalsIgnoreCase("RespReconfigChan")){
                        
                        
                        
                    //} else {
                        chan.setBuffer(o);
                    //}
                    
                       // chan.setBuffer(o);
                    //} 
			
                }
	}
	
	/**
	 * Internal use
	 * @param ci
	 */
	private synchronized void resendUnsent(){
		for(int i=0;i<unsentdata.size(); i++){
			boolean done = tryToSend((Object[])unsentdata.elementAt(i));
			if(done){
				unsentdata.removeElementAt(i);
				i--;
			}
		}
	}

	/*
	 * TODO:
	 * 1. Need to pop out the element from the queue
	 * 2. Then choose correct GenericInterface object which can be retrieved from the Interconnection instance 'ic'
	 * 3. Execute GenericInterface.setup(Object[] o) followed by GenericInterface.transmitData()
	 * 
	 */
	private void transmit(){
            
            
		if(unsentdata.size() > 0) // is this safe?
			resendUnsent();
		
		while(!OutQueue.isEmpty()){
                    
			Object[] o = (Object[])OutQueue.pop();
                        //System.out.println("InterfaceManager transmit check A");
                        //check destination here??
                        
			boolean done = tryToSend(o);
                        //System.out.println("InterfaceManager transmit check B");
			if(!done){
                            if(!this.IsUnsentBufferFull()){
                                 //System.out.println("InterfaceManager transmit check C");
				this.addToUnsent(o);
                                //System.out.println("InterfaceManager transmit check D");
                            }
                           // System.out.println("InterfaceManager transmit check E");
                        }
                        //System.out.println("InterfaceManager transmit check F");
		}
                //System.out.println("InterfaceManager transmit check G");
               
	}
	
	private void receive(){
		for(int i=0;i<LocalInterface.size(); i++)
			((GenericInterface)LocalInterface.elementAt(i)).receiveData();
	}

	public void run(){
            //System.out.println("InterfaceManager transmit TryToSend 1: ");
		receive();
             //   System.out.println("InterfaceManager transmit TryToSend 2: ");
		transmit();
             //   System.out.println("InterfaceManager transmit TryToSend 3: ");
                //transmitStateChangeSignal();
	}
        
        
	
	
	// For debugging purpose
	public Hashtable getcdmap(){return cdlocation;}
	public void printLocalInterface(){
		System.out.println("\nLocalInterface : ");
		System.out.println(this.LocalInterface);
	}
}

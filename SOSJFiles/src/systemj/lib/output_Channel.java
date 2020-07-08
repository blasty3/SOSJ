package systemj.lib;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import systemj.bootstrap.ClockDomain;
import systemj.common.CDLCBuffer;
import systemj.common.CDObjectsBuffer;
import systemj.common.IMBuffer;
import systemj.common.Interconnection;
import systemj.common.InterfaceManager;
import systemj.common.SJSSCDSignalChannelMap;
import systemj.common.SJServiceRegistry;

import systemj.interfaces.*;

public class output_Channel extends GenericChannel implements Serializable{
        //private String CDNameLoc;
        private String CDState="Created";
        //private boolean terminated = false;
        
        protected String NewPartnerName;
        //protected Vector toRecRespChanQueryBuf = new Vector();
        protected volatile Object[] queryRespBufObj = new Object[4];
        
	public input_Channel partner;
	private int preempted = 0;
	private int w_s = 0;
	private int w_r = 0;
	public output_Channel(){}
	public void set_value(Object in){
            this.value = in; 
            this.modified = true;
        }
	public Object get_value(){
            return this.value; 
        }
	public int refresh(){
		this.value = null;
		this.w_s = 0;
		this.w_r = 0;
		set_preempted();
		this.modified = true;
		return 1;
	}
        
        private void clean_value_buffer(){
            this.value=null;
        }
        
	public void set_w_s(int in){
            this.w_s = in; 
            this.modified = true;
        }
	public void set_w_r(int in){this.w_r = in; this.modified = true;}
	public int get_w_s(){return this.w_s;}
	public int get_w_r(){return this.w_r;}
       
        public void terminate_blocking_ochan(){ //forcing blocking to terminate??
            
            //reset to 0?
            
            if(get_w_r()!=0){
                this.w_r=0;
            }
            if(get_w_s()!=0){
                this.w_s=0;
            }
            
            //if(!terminated){
            //    terminated=true;
            //}
            
            //set_preempted();
            
        }
        
        
        public void setPreemptChan(boolean stat){
            Object[] toSend = new Object[3];  // Creating an Object!!
                                    toSend[0] = PartnerName;
                                    toSend[1] = Name;  // Creating an Object!!
                                    toSend[2] = "PartnerTerminated";
                                    super.pushToQueue(toSend);
        }
        
       
	private int get_r_r(){return init ? partner.get_r_r() : 0;}
	public void update_w_r(){
            
		if(init){
			if(this.preempted == partner.get_preempted_val()){
                            
                            if(isLocal){
                                
                                if(partner_ref.getChannelCDState()!=null ){  //to avoid uninitialized partner state data, if its uninit'd, then proceed with copying
                                    if(!partner_ref.getChannelCDState().equals("Killed")){
                                        this.w_r = get_r_r();
                                    } else {
                                        terminate_blocking_ochan();
                                    }
                                } else {
                                    this.w_r = get_r_r();
                                }
                                
                            } else {
                                
                                if(partner.getChannelCDState()!=null ){  //to avoid uninitialized partner state data, if its uninit'd, then proceed with copying
                                    if(!partner.getChannelCDState().equals("Killed")){
                                        this.w_r = get_r_r();
                                    } else {
                                        terminate_blocking_ochan();
                                    }
                                } else {
                                    this.w_r = get_r_r();
                                }
                                
                            }
                            
                            
                            
                        }
		}
	}
	public void set_partner(input_Channel partner){this.partner = partner;}
	public int get_preempted_val(){return this.preempted; }
	public void set_preempted() {++this.preempted; ; this.modified = true;}
	
	/**
	 * Tests whether partner input channel is preempted or re-initialized
	 * @return <b>true</b> - when partner is preempted or re-initialized <br> <b>false</b> - otherwise
	 */
	public boolean get_preempted(){
		// Now output channel is preempted when the input channel is re-initialized (i.e. w_s < w_r)
		
                //need to check if partner is not terminated, if terminated, return true;
            
                if(init){
                    //System.out.println("State : " +partner.getChannelCDState());
			if(partner.get_preempted_val() > this.preempted || this.w_s < this.w_r) 
				return true; 
		}
		return false;
	}

	// Modular
	public void set_preempted(int num){this.preempted=num; this.modified = true;}
	public void gethook(){
            
		if(init){
			if(isLocal) {
                            
                          //  if(!partner.getChannelCDState().equals("Killed")){
                                partner_ref.updateLocalPartner(this.partner);
                          //  } else {
                                
                                if(incoming){
                                    this.getBuffer();
                                    //if(partner.get_preempted_val() < this.preempted) 
				    //	modified = true;
                                }
                            //    terminate_blocking_w_s_w_r();
                
                                //refresh();
                         //   }
                            
                        } else {
                            
                            if(incoming){
				this.getBuffer();
				// Little trick to make sure that partner channel knows preemption status of this channel
				if(partner.get_preempted_val() < this.preempted) 
					modified = true;
                            } 
                            
                          //  if (stateChangeIncoming){
                          //      this.getStateChangeBuffer();
                           // }
                            
                        }
		}
	}
        public void forcePreempt(){
            int val = this.preempted+1;
            partner.set_preempted(val);
        }
	public synchronized void getBuffer(){
            if(toReceiveBuffer.size()>0){
                for(int y=0;y<toReceiveBuffer.size();y++){
                Object[] rcvdObj = (Object[])toReceiveBuffer.get(y);
                 if(rcvdObj.length==6){
                    partner.set_r_s(((Integer)rcvdObj[1]).intValue());
                    partner.set_r_r(((Integer)rcvdObj[2]).intValue());
                    partner.set_preempted(((Integer)rcvdObj[3]).intValue());
                } else if(rcvdObj.length==7){
                        String partnerName = (String)rcvdObj[5];
                        String partnerCDName = partnerName.split("\\.")[0];

                        //InterfaceManager im = super.getInterfaceManager();
                        while(IMBuffer.getIMUpdatingFlag()){}
                        InterfaceManager im = IMBuffer.getInterfaceManagerConfig();

                        //System.out.println("OChan buffer received, SSLoc : " +rcvdObj[6]+ "partnerCDName: " +partnerCDName);

                        im.addCDLocation((String)rcvdObj[6], partnerCDName);
                        super.setInterfaceManager(im);
                        IMBuffer.SaveInterfaceManagerConfig(im);
                        this.modified = true;

                    } else if(rcvdObj.length==4 || rcvdObj.length==9){
                         String CDName = Name.split("\\.")[0];
                        String ChanName = Name.split("\\.")[1].split("_")[0];
                        //partner.set_preempted();
                        
                        if(rcvdObj[2].toString().equalsIgnoreCase("ReconfigChan")){
                            String changeInPartner = (String)rcvdObj[3];
                           // System.out.println("oChan, changeInPartner: "+changeInPartner );
                            String oldPartnerChan = PartnerName;
                            String partnerSS = rcvdObj[8].toString();
                            if(!changeInPartner.equalsIgnoreCase(PartnerName)){
                                //String partnerSS = rcvdObj[8].toString();
                                String OldCDPartName = oldPartnerChan.split("\\.")[0];
                                String OldChanPartName = oldPartnerChan.split("\\.")[1].split("_")[0];
                                CDLCBuffer.AddOldPartnerChanReconfig(CDName, ChanName,"input", OldCDPartName, OldChanPartName,partnerSS);
                            }
                             if(!changeInPartner.equalsIgnoreCase(".")){
                                // String partnerSS = rcvdObj[8].toString();
                                partner.set_r_s(((Integer)rcvdObj[4]).intValue());
                                partner.set_r_r(((Integer)rcvdObj[5]).intValue());
                                partner.set_preempted(((Integer)rcvdObj[6]).intValue());
                                String NewCDPartName = changeInPartner.split("\\.")[0];
                                String NewChanPartName = changeInPartner.split("\\.")[1];
                                //String partnerSS = rcvdObj[8].toString();
                                CDLCBuffer.AddDistChanReconfig(CDName,ChanName,"output",NewCDPartName,NewChanPartName,partnerSS);
                            } else {
                                CDLCBuffer.AddDistChanReconfig(CDName, ChanName, "output", "", "","");
                            }
                            // this.modified = true;

                        } else if(rcvdObj[2].toString().equalsIgnoreCase("ReconfigPartnerChan")){
                            String changeInPartnersPartner = (String)rcvdObj[3];
                            if(!changeInPartnersPartner.equalsIgnoreCase(".")){
                                String NewCDPartName = changeInPartnersPartner.split("\\.")[0];
                                String NewChanPartName = changeInPartnersPartner.split("\\.")[1];
                               
                                 partner.set_r_s(((Integer)rcvdObj[4]).intValue());
                                 partner.set_r_r(((Integer)rcvdObj[5]).intValue());
                                 partner.set_preempted(((Integer)rcvdObj[6]).intValue());
                                 String partnerSS = rcvdObj[8].toString();
                                CDLCBuffer.AddDistChanReconfig(CDName, ChanName, "output", NewCDPartName, NewChanPartName,partnerSS);
                               
                            } else {
                                CDLCBuffer.AddDistChanReconfig(CDName, ChanName, "output", "", "","");
                            }
                        } 
                        /*
                        else if(rcvdObj[2].toString().equalsIgnoreCase("SubscribeChan")){
                             String orig = rcvdObj[1].toString();
                             String SSName = rcvdObj[3].toString();
                             String dest = orig+"_o";
                             if(get_w_s()!=0){
                                 Hashtable hash = new Hashtable();
                                 hash.put("SSName", SSName);
                                 hash.put("Stat",Boolean.toString(false));
                                 toTransmitRespSubscribe.put(dest, hash);
                             } else {
                                 Hashtable hash = new Hashtable();
                                 hash.put("Stat",Boolean.toString(true));
                                 toTransmitRespSubscribe.put(dest, hash);
                                 String cname = rcvdObj[1].toString();
                                 PartnerName = cname+"_o";
                                 String newPartnerCDName = cname.split("\\.")[0];
                                 String newPartnerChanName = cname.split("\\.")[1];
                                 String inchanCDName = Name.split("\\.")[0];
                                 String inchanName = Name.split("\\.")[1];
                                 CDLCBuffer.AddReconfigInChanConfigIMBuffer(inchanCDName, inchanName, newPartnerCDName, newPartnerChanName, SSName);
                             }
                         }
                        */
                        //this.modified = true;
                    } 
                    /*
                    else if (rcvdObj.length==2){

                        String changeInPartner = (String)rcvdObj[0];

                        String CDPartName = changeInPartner.split("\\.")[0];
                        String ChanPartName = changeInPartner.split("\\.")[1].split("_")[0];

                        PartnerName = CDPartName+"."+ChanPartName+"_in";

                    } 
                    */
                    else if (rcvdObj.length==3){
                            if(rcvdObj[2].toString().equalsIgnoreCase("PartnerTerminated")){
                            //int val = this.preempted+1;
                            //partner.set_preempted(val);
                            String ChCDName = Name.split("\\.")[0];
                            String ChName = Name.split("\\.")[1].split("_")[0];
                            CDLCBuffer.AddDistChanReconfig(ChCDName, ChName, "output", "", "","");
                        }
                    }
                }
                toReceiveBuffer = new Vector();
	        incoming = false;
            }
	}
        
        public synchronized boolean getIncoming(){
            return incoming;
        }
        
        public synchronized Object[] getToReceiverBuffer(){
            return queryRespBufObj;
        }
        
        public synchronized Vector getAllBuf(){
            return toReceiveBuffer;
        }
        
        public synchronized boolean CheckRcvdQueryRspMsg(){
            boolean stat = false;
            if(getIncoming()){
                Vector allBuf = this.getAllBuf();
                //System.out.println("output_Channel,CheckRcvdQueryRspMsg incoming data: " +allBuf);
                for(int i=0;i<allBuf.size();i++){
                    Object[] toRecBuf = (Object[])allBuf.get(i);
                    if (toRecBuf.length==4){
                        //System.out.println("output_Channel,CheckRcvdQueryRspMsg, toRecBuf length = 4");
                        if(toRecBuf[2].toString().equalsIgnoreCase("RespOccupyChan")){
                            //System.out.println("output_Channel,CheckRcvdQueryRspMsg, toRecBuf msg: RespOccupyChan");
                            queryRespBufObj = toRecBuf;
                            stat = true;
                        } 
                    }
                }
            }
            return stat;
        }
        public synchronized boolean GetRespOccupyQuery(){
            Object[] toRecBuf = getToReceiverBuffer();
            boolean stat = Boolean.parseBoolean(toRecBuf[3].toString());
            queryRespBufObj = new Object[4];
            return stat;
        }
        /*
        public synchronized void getStateChangeBuffer(){
            
            //also updates remote (distributed) partner state
            
                partner.setChannelCDState((String)stateChangetoReceive[2]);
		
		stateChangeIncoming = false;
	}
        */
	
	public void sethook(){
            
            
            if(init){
                
                if(isLocal){
                    
                    if(partner_ref.getChannelCDState()!=null){
                         if(partner_ref.getChannelCDState().equals("Killed")){
                            //terminate_blocking_ochan(); // already reset the communication status
                            //clean_value_buffer(); //clear the value buffer
                         } else {
                             //if(terminated){
                             //    terminated = false;
                             //}
                         }
                    }
                    
                } 
                else {
                    
                    if(partner.getChannelCDState()!=null){
                        if(partner.getChannelCDState().equals("Killed")){
                            terminate_blocking_ochan(); // already reset the communication status
                            clean_value_buffer(); //clear the value buffer
                        } else{
                           // if(terminated){
                           //      terminated = false;
                           // }
                        }
                            
                    }
                    
                }
                
            }
           
             //System.out.println("set hook");
		if(init && (this.modified || this.cdmigrated)){
			if(isLocal){
                                updateLocalCopy();
			}
			else{
                            
                            if(!PartnerName.equalsIgnoreCase(".")){
                                
                               // System.out.println("OChan trying to send data via LINK");
                                            Object[] toSend = new Object[6];  // Creating an Object!!
                                            toSend[0] = PartnerName;
                                            toSend[1] = new Integer(this.get_w_s()); // Creating an Object!!
                                            toSend[2] = new Integer(this.get_w_r());
                                            toSend[3] = new Integer(this.get_preempted_val());
                                            toSend[5] = Name;
                                            //toSend[6] = getChannelCDState();
                                            if(value != null)
                                                    toSend[4] = value;
                                            if(super.pushToQueue(toSend))
                                                    this.modified = false; // This is set to false ONLY if the data is received by other side
                             
                            } else {
                                this.modified = false;
                            }
                            
			}
                }
                
                /*
                if(init && this.preemptChan && !isLocal){
                    Object[] toSend = new Object[3];  // Creating an Object!!
                                    toSend[0] = PartnerName;
                                    toSend[1] = Name;  // Creating an Object!!
                                    toSend[2] = "PartnerTerminated";
                                    if(super.pushToQueue(toSend))
                                            this.preemptChan = false;
                }
                */
              
	}
        
        

	// SMCHAN
	// In this case, partner is a local-copy and partner_ref has real reference to the partner object
	private input_Channel partner_ref = null;
	public void setDistributed(){ isLocal = false; partner = new input_Channel(); }
        
        public void setDistributedWeakMigration(){ isLocal = false; partner = new input_Channel(); this.modified = true;}
        public void setDistributedReconfiguration(){
            isLocal=false;
            this.modified = true;
        }
        
        public void TransmitCDLocChanges(String newSSLoc){
                    Object[] toSend = new Object[7];  // Creating an Object!!
                                    toSend[0] = PartnerName;
                                            toSend[1] = new Integer(this.get_w_s()); // Creating an Object!!
                                            toSend[2] = new Integer(this.get_w_r());
                                            toSend[3] = new Integer(this.get_preempted_val());
                                    toSend[5] = Name;
                                    //toSend[6] = getChannelCDState();
                                    if(value != null)
                                            toSend[4] = value;
                                    toSend[6] = newSSLoc;
                                    super.pushToQueue(toSend);
                                            
        }
       
        public void TransmitReconfigChanChanges(String NewPartner){
                    if(NewPartner.equalsIgnoreCase(".")){
                         Object[] toSend = new Object[4]; 
                       toSend[0] = PartnerName;
                        toSend[1] = Name;
                        toSend[2] = "ReconfigChan";
                        toSend[3] = NewPartner;
                         super.pushToQueue(toSend);
                    } else {
                        Object[] toSend = new Object[9];  // Creating an Object!!
                        toSend[0] = PartnerName;
                        toSend[1] = Name;
                        toSend[2] = "ReconfigChan";
                        toSend[3] = NewPartner;
                        toSend[4] = new Integer(this.get_w_s()); // Creating an Object!!
                                            toSend[5] = new Integer(this.get_w_r());
                                            toSend[6] = new Integer(this.get_preempted_val());
                                            if(value != null) toSend[7] = value;
                                             toSend[8] = SJSSCDSignalChannelMap.getLocalSSName();
                                             super.pushToQueue(toSend);
                    }
        }
        
        public void TransmitOutChanStat(){
            //System.out.println("TransmitOutChanStat sent");
            Object[] toSend = new Object[6];
                                            toSend[0] = PartnerName;
                                            toSend[1] = new Integer(this.get_w_s()); // Creating an Object!!
                                            toSend[2] = new Integer(this.get_w_r());
                                            toSend[3] = new Integer(this.get_preempted_val());
                                            toSend[5] = Name;
                                            if(value != null)
                                                    toSend[4] = value;
                                            super.pushToQueue(toSend);
        }
        
        
        
         public void TransmitPartnerReconfigChanChanges(String NewPartner){
             if(NewPartner.equalsIgnoreCase(".")){
                 Object[] toSend = new Object[4];  
                       toSend[0] = PartnerName;
                        toSend[1] = Name;
                        toSend[2] = "ReconfigPartnerChan";
                        toSend[3] = NewPartner;
                        super.pushToQueue(toSend);
             } else {
                    Object[] toSend = new Object[9];  // Creating an Object!!
                    toSend[0] = PartnerName;
                    toSend[1] = Name;
                    toSend[2] = "ReconfigPartnerChan";
                    toSend[3] = NewPartner;
                    toSend[4] = new Integer(this.get_w_s()); // Creating an Object!!
                                            toSend[5] = new Integer(this.get_w_r());
                                            toSend[6] = new Integer(this.get_preempted_val());
                                            if(value != null){toSend[7] = value;}   
                                            toSend[8] = SJSSCDSignalChannelMap.getLocalSSName();
                                            super.pushToQueue(toSend);
             }                   
        }
         
        public void TransmitOccupyChan(String partnerName, String SSName){
                    Object[] toSend = new Object[4];
                    toSend[0] = partnerName;              
                    toSend[1] = Name;
                    toSend[2] = "OccupyChan";
                    toSend[3] = SSName;
                    super.pushToQueue(toSend);
        }
        public void TransmitResetOccupyChan(String SSName){
                    Object[] toSend = new Object[4];  
                    //toSend[0] = PartnerName;
                      toSend[0] = PartnerName;              
                    toSend[1] = Name;
                    toSend[2] = "ResetOccupyChan";
                    toSend[3] = SSName;
                    super.pushToQueue(toSend);
        }
        
        //public synchronized void ResetQueryFlag(){
        //    queryFlag = false;
        //}
        
        //public synchronized boolean GetLinkFlag(){
       ///     return incoming;
       // }
        
        public void setLocal(){isLocal=true;}
	// My data-structure copies to be read by partner
	private int w_s_copy = 0;
	private int w_r_copy = 0;
	private int preempted_copy = 0;
        private String CDState_copy;
	public void set_partner_smp(input_Channel partner){this.partner_ref = partner; this.partner = new input_Channel();}
	
        public void set_partner_smp_migration(input_Channel partner){this.partner_ref=partner;this.cdmigrated=true;}
        
        //public void clear_partner_smp(){this.partner_ref=null;this.partner=null;}
    
        
        public input_Channel get_partner_smp(){return this.partner_ref;};
        
        protected synchronized void updateLocalPartner(output_Channel p){
		// This copying operation is regarded as an atomic operation
		p.w_s = this.w_s_copy;
		p.w_r = this.w_r_copy;
		p.preempted = this.preempted_copy;
                p.CDState = this.CDState_copy;
                
		if(value!=null){
                   
			p.value = this.value;
                }
	}
	protected synchronized void updateLocalCopy(){
		// This copying operation is regarded as an atomic operation
		this.w_s_copy = get_w_s();
		this.w_r_copy = get_w_r();
		this.preempted_copy = get_preempted_val();
                this.CDState_copy = getChannelCDState();
                this.cdmigrated = false;
	}
        
        /*
        public String getChannelCDNameLocation(){
            return this.CDNameLoc;
        }
        
        public void setChannelCDNameLocation(String cdname){
            this.CDNameLoc = cdname;
        }
        
        public String getPartnerCDName(){
            return partner.getChannelCDNameLocation();
        }
        */
        
        public String getChannelCDState(){
            return this.CDState;
        }
        
        public void setChannelCDState(String cdstate){
            this.CDState = cdstate;
        }
        
      //  public boolean IsOutputChannelTerminated(){
       //     return terminated;
       // }
         
        public boolean IsChannelLocal(){
            return isLocal;
        }
        /*
        public void TransmitRespSubscribeChan(String partnerName,String ack){
                    Object[] toSend = new Object[4];  // Creating an Object!!
                    toSend[0] = partnerName;
                                    
                    toSend[1] = Name;
                                    //toSend[6] = getChannelCDState();
                           
                    toSend[2] = "RespSubscribeChan";
                    toSend[3] = ack;
                    super.pushToQueue(toSend);
        }
        */
        
        /*
        public void UpdateRemotePartner(){
            System.out.println("OChan trying to send data via LINK");
                             //   if(dest!=null || destcd!=null){
                                            Object[] toSend = new Object[6];  // Creating an Object!!
                                            toSend[0] = PartnerName;
                                            toSend[1] = new Integer(this.get_w_s()); // Creating an Object!!
                                            toSend[2] = new Integer(this.get_w_r());
                                            toSend[3] = new Integer(this.get_preempted_val());
                                            toSend[5] = Name;
                                            //toSend[6] = getChannelCDState();
                                            if(value != null)
                                                    toSend[4] = value;
                                            super.pushToQueue(toSend);
        }
        */
        
}

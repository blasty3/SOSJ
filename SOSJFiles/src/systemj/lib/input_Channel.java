package systemj.lib;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Hashtable;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import systemj.bootstrap.ClockDomain;
import systemj.common.CDLCBuffer;
import systemj.common.IMBuffer;
import systemj.common.Interconnection;
import systemj.common.InterfaceManager;
import systemj.common.SJSSCDSignalChannelMap;
import systemj.common.SJServiceRegistry;

import systemj.interfaces.*;

public class input_Channel extends GenericChannel implements Serializable{
        private String CDState="Created";
        //private boolean terminated = false;
        protected Hashtable toTransmitRespOccupy = new Hashtable();
        protected String NewPartnerName;
        private boolean occupied = false;
	private int preempted = 0;
	private int r_r = 0;
	private int r_s = 0;
	public output_Channel partner;
	public input_Channel(){}
	public void set_partner(output_Channel partner){this.partner = partner;}
	public Object get_value(){
		Object ret = this.value;
                this.value = null;
                //occupied = false;
                //setOccupiedStat(false);
                return ret;
	}
	/**
	 * This method now just equalize r_r with r_s, not using 'int in' at all.. (which is ++ in the code)
	 * @param in
	 */
	public void set_r_r(int in){this.r_r  = /*in*/ this.r_s; this.modified = true; }
	public void get_val(){
            
		if(init)
			this.value = partner.get_value();
                occupied=false;
                setOccupiedStat(false);
	}
	public int get_r_r(){
            //System.out.println("r_r val: " +this.r_r); 
            return this.r_r; 
        }
	public int get_r_s(){
            //System.out.println("r_s val: " +this.r_s); 
            return this.r_s; 
        }
        
        public void setPreemptChan(boolean stat){
            Object[] toSend = new Object[3];  // Creating an Object!!
                                    toSend[0] = PartnerName;
                                    toSend[1] = Name;  // Creating an Object!!
                                    toSend[2] = "PartnerTerminated";
                                    super.pushToQueue(toSend);
        }
       
	public void set_r_s(int in){this.r_s = in; this.modified = true;}
        
	private int get_w_s(){
		return init ? partner.get_w_s() : 0; 
	}
	public int get_preempted_val(){return this.preempted; }
	public void set_preempted() {++this.preempted; ; this.modified = true;}
        
	public void update_r_s(){ 
            
		if(init){
                    
			if(partner.get_preempted_val() == this.preempted){ //still considering local only
                            
                            if(isLocal){
                                
                                if(partner_ref.getChannelCDState()!=null){
                                
                                    if(!partner_ref.getChannelCDState().equals("Killed")){
                                        this.r_s = get_w_s(); //if partner is killed, the r_s is not updated
                                    } 

                                } else {
                                    this.r_s = get_w_s();
                                }
                                
                                //System.out.println("Inchan, partner w_s" +get_w_s()+" and r_s value: " +get_r_s()+ " and r_r value:" +get_r_r());
                                
                            } else {
                                
                                if(partner.getChannelCDState()!=null){
                                
                                    if(!partner.getChannelCDState().equalsIgnoreCase("Killed")){
                                        this.r_s = get_w_s(); //if partner is killed, the r_s is not updated
                                    } 

                                } else {
                                    this.r_s = get_w_s();
                                }
                                
                            }
                            
                            
                        }
		}
	}
	/**
	 * Tests whether partner output channel is preempted or re-initialized
	 * @return <b>true</b> - when partner is preempted or re-initialized <br> <b>false</b> - otherwise
	 */
	public boolean get_preempted() {
		// Now input channel is preempted when the output channel is re-initialized (i.e. r_s < r_r)
                
                //need to check if partner is not terminated, if terminated, return true;
            
                //System.out.println("State : " +partner.getChannelCDState());
            
                if(init){
                        //System.out.println("State : " +partner.getChannelCDState());
			if(partner.get_preempted_val() > this.preempted || this.r_s < this.r_r)
				return true; 
		}
		return false;
	}
	
	public int refresh(){
            
		this.value = null;    this.r_r = 0;
		this.r_s = 0;
		set_preempted();
		this.modified = true;
                
		return 1;
	}
        
        public int refresh_commstat_only(){
            this.value = null;    this.r_r = 0;
		this.r_s = 0;
                set_preempted();
                return 1;
        }
        
        
	
	// Modular
	public void set_preempted(int num){this.preempted=num; this.modified = true;}
	public void gethook(){
           
		if(init){
			if(isLocal){
                            
				partner_ref.updateLocalPartner(this.partner);
                        
                                if(incoming){
                                    this.getBuffer();
                                }
                                
                              //  if(partner.getChannelCDState()!=null){ //if still uninitialized
                                
                                //    if(partner.getChannelCDState().equals("Killed")){
                                    
                                //        terminate_blocking_r_r_r_s();
                                        //this.modified=true;
                                 //   }
                                
                                //}
                                
                                //System.out.println("gethook inchan first, r_s val:" +get_r_s()+", r_r val: "+get_r_r()+", partner state: " +partner.getChannelCDState() );

                                
                                //System.out.println("gethook inchan after, r_s val:" +get_r_s()+", r_r val: "+get_r_r()+", partner state: " +partner.getChannelCDState() );
                        }
			else {
                            if(incoming){
                                
				this.getBuffer();
				// Little trick to make sure that partner channel knows preemption status of this channel
				if(partner.get_preempted_val() < this.preempted) 
					modified = true;
                            }
                            
                            //if(stateChangeIncoming){
                           //     this.getStateChangeBuffer();
                           // }
                        }
                            
		}
	}
	
	public synchronized void getBuffer(){
            int y=0;
            if(toReceiveBuffer.size()>0){
                 while(toReceiveBuffer.size()>0){
                Object[] rcvdObj = (Object[])toReceiveBuffer.get(y);
                if(rcvdObj.length==6){
                    partner.set_w_s(((Integer)rcvdObj[1]).intValue());
                    partner.set_w_r(((Integer)rcvdObj[2]).intValue());
                    partner.set_preempted(((Integer)rcvdObj[3]).intValue());
                    if(rcvdObj[4] != null) partner.set_value(rcvdObj[4]);
                    toReceiveBuffer.remove(y);
                } else if(rcvdObj.length==7){
                    String partnerName = (String)rcvdObj[5];
                    String partnerCDName = partnerName.split("\\.")[0];
                    while(IMBuffer.getIMUpdatingFlag()){}
                    InterfaceManager im = IMBuffer.getInterfaceManagerConfig();
                    im.addCDLocation((String)rcvdObj[6], partnerCDName);
                    //System.out.println("InChan buffer received, partner w_s: " +((Integer)rcvdObj[1]).intValue()+ "partner w_r: " +((Integer)rcvdObj[2]).intValue()+" SSLoc : " +rcvdObj[6]+ " partnerCDName: " +partnerCDName);
                    super.setInterfaceManager(im);
                    IMBuffer.SaveInterfaceManagerConfig(im);
                    InterfaceManager imTest = super.getInterfaceManager();
                    //System.out.println("InChan, Updated IM CD location: "+imTest.getAllCDLocation());
                    toReceiveBuffer.remove(y);
                } else if(rcvdObj.length==4 || rcvdObj.length==9) {
                    if(rcvdObj[2].toString().equalsIgnoreCase("ReconfigChan")){
                        String changeInPartner = (String)rcvdObj[3];
                        String oldPartnerChan = PartnerName;
                        String CDName = Name.split("\\.")[0];
                        String ChanName = Name.split("\\.")[1].split("_")[0];
                        String partnerSS = rcvdObj[8].toString();
                        //System.out.println("ReconfigChan PartnerName, :" +PartnerName);
                        if(!changeInPartner.equalsIgnoreCase(PartnerName)){
                            String OldCDPartName = oldPartnerChan.split("\\.")[0];
                            String OldChanPartName = oldPartnerChan.split("\\.")[1].split("_")[0];
                            
                            CDLCBuffer.AddOldPartnerChanReconfig(CDName, ChanName,"output", OldCDPartName, OldChanPartName, partnerSS);   
                        } 
                            //else {
                       //     CDLCBuffer.AddOldPartnerChanReconfig(CDName, ChanName,"output", "", "", "");   
                        //}
                        if(!changeInPartner.equalsIgnoreCase(".")){
                            String NewCDPartName = changeInPartner.split("\\.")[0];
                            String NewChanPartName = changeInPartner.split("\\.")[1].split("_")[0];
                            //String partnerSS = rcvdObj[8].toString();
                            //NewPartnerName = NewCDPartName+"."+NewChanPartName+"_o";
                            CDLCBuffer.AddDistChanReconfig(CDName, ChanName, "input", NewCDPartName, NewChanPartName,partnerSS);
                            partner.set_w_s(((Integer)rcvdObj[4]).intValue());
                            partner.set_w_r(((Integer)rcvdObj[5]).intValue());
                            partner.set_preempted(((Integer)rcvdObj[6]).intValue());
                            if(rcvdObj[7] != null) partner.set_value(rcvdObj[7]);
                        } else {
                            CDLCBuffer.AddDistChanReconfig(CDName, ChanName, "input", "", "","");
                        }
                        //this.modified = true;
                        toReceiveBuffer.remove(y);
                    } else if(rcvdObj[2].toString().equalsIgnoreCase("ReconfigPartnerChan")){
                        String changeInPartnersPartner = (String)rcvdObj[3];
                        String CDName = Name.split("\\.")[0];
                        String ChanName = Name.split("\\.")[1].split("_")[0];
                        if(!changeInPartnersPartner.equalsIgnoreCase(".")){
                            String NewCDPartName = changeInPartnersPartner.split("\\.")[0];
                            String NewChanPartName = changeInPartnersPartner.split("\\.")[1].split("_")[0];
                            partner.set_w_s(((Integer)rcvdObj[4]).intValue());
                            partner.set_w_r(((Integer)rcvdObj[5]).intValue());
                            partner.set_preempted(((Integer)rcvdObj[6]).intValue());
                            if(rcvdObj[7] != null) partner.set_value(rcvdObj[7]);
                            String partnerSS = rcvdObj[8].toString();
                            CDLCBuffer.AddDistChanReconfig(CDName, ChanName, "input", NewCDPartName, NewChanPartName,partnerSS);
                        } else {
                            CDLCBuffer.AddDistChanReconfig(CDName, ChanName, "input", "", "","");
                        }
                        //this.modified = true;
                        toReceiveBuffer.remove(y);
                    } else if(rcvdObj[2].toString().equalsIgnoreCase("ResetOccupyChan")){ 
                        occupied = false;
                        partner.set_preempted();
                    }
                     else if(rcvdObj[2].toString().equalsIgnoreCase("OccupyChan")){
                         //String orig = rcvdObj[1].toString();
                         String dest = rcvdObj[1].toString();
                         String SSName = rcvdObj[3].toString();
                         //String dest = orig+"_o";
                         if(occupied){
                             Hashtable hash = new Hashtable();
                             hash.put("SSName", SSName);
                             hash.put("Stat",Boolean.toString(false));
                             toTransmitRespOccupy.put(dest, hash);
                         } else {
                             occupied=true;
                             Hashtable hash = new Hashtable();
                             hash.put("Stat",Boolean.toString(true));
                             toTransmitRespOccupy.put(dest, hash);
                             String cname = rcvdObj[1].toString();
                             //PartnerName = cname+"_o";
                             PartnerName = cname;
                             String newPartnerCDName = cname.split("\\.")[0];
                             String newPartnerChanName = cname.split("\\.")[1].split("_")[0];
                             String inchanCDName = Name.split("\\.")[0];
                             String inchanName = Name.split("\\.")[1].split("_")[0];
                             CDLCBuffer.AddReconfigInChanConfigIMBuffer(inchanCDName, inchanName, newPartnerCDName, newPartnerChanName, SSName);
                         }
                         toReceiveBuffer.remove(y);
                     } 
                } else if(rcvdObj.length==3){
                    if(rcvdObj[2].toString().equalsIgnoreCase("PartnerTerminated")){
                        //int val = this.preempted+1;
                        //partner.set_preempted(val);
                        String ChCDName = Name.split("\\.")[0];
                        String ChName = Name.split("\\.")[1].split("_")[0];
                        CDLCBuffer.AddDistChanReconfig(ChCDName, ChName, "input", "", "","");
                        toReceiveBuffer.remove(y);
                    }
                }
               
              }
             incoming = false;
            }
           
	}
        
        public boolean getOccupiedStat(){
            return occupied;
        }
        //public void setSubscribedStat(){
        //    subscribed = true;
        //}
        public void setOccupiedStat(boolean stat){
            occupied = stat;
        }
	
	/**
	 * As we do not want to use link traffics all the time, chan status is only transferred only if it is modified during the last
	 * tick
	 */
	public void sethook(){
            if(init){
                if(isLocal){
                   if(partner_ref.getChannelCDState()!=null){
                
                        if(partner_ref.getChannelCDState().equals("Killed")){

                            //System.out.println("sethook inchan after, r_s val:" +get_r_s()+", r_r val: "+get_r_r()+", partner state: " +partner_ref.getChannelCDState() );
                           
                              //terminate_blocking_local_inchan();
                            /*
                             if(get_r_s()<=get_r_r()){
                                 
                                //make sure that the r_r value is reset, not more than 0 for initial state, ensuring this value persists unti the CD read the channel status
                                if(get_r_r()>0){
                                    //this.r_r=0;
                                    set_r_r(0);
                                    
                                }
                                
                                int st = get_r_r()+1;

                                set_r_s(st);

                                //this.r_s = this.r_r;
                                //this.r_s++;

                            }
                             */
                             
                            //System.out.println("sethook inchan after, r_s val:" +get_r_s()+", r_r val: "+get_r_r()+", partner state: " +partner_ref.getChannelCDState() );


                        } else {
                           // if(terminated){
                          //      terminated = false;
                          //  }
                        }
                    }
                    
                } else {
                    
                    if(partner.getChannelCDState()!=null){
                        
                        if(partner.getChannelCDState().equalsIgnoreCase("Killed")){
                            //terminate_blocking_local_inchan();
                        } else {
                           // if(terminated){
                           //     terminated = false;
                           // }
                        }
                        
                    }
                    
                }
                if(toTransmitRespOccupy.size()>0){
                    Enumeration keysToTransmitResp = toTransmitRespOccupy.keys();
                    while(keysToTransmitResp.hasMoreElements()){
                        String dest = keysToTransmitResp.nextElement().toString();
                        Hashtable hashToSend = (Hashtable)toTransmitRespOccupy.get(dest);
                        String stat = hashToSend.get("Stat").toString();
                        TransmitRespOccupyChan(dest, stat);
                    }
                    toTransmitRespOccupy = new Hashtable();
                }
            }
		if(init && (this.modified || this.cdmigrated)){
			if(isLocal){
				updateLocalCopy();
			}
			else{
                           
                            if(!PartnerName.equalsIgnoreCase(".")){
                                Object[] toSend = new Object[6];  // Creating an Object!!
                                    toSend[0] = PartnerName;
                                    toSend[1] = new Integer(this.get_r_s());  // Creating an Object!!
                                    toSend[2] = new Integer(this.get_r_r());
                                    toSend[3] = new Integer(this.get_preempted_val());
                                    toSend[5] = Name;
                                    if(value != null)
                                            toSend[4] = value;
                                    //System.out.println("InChan update data via Link:" +toSend[0]+"|"+toSend[1]+"|" +toSend[2]+"|"+toSend[3]+"|"+toSend[5]);
                                    if(super.pushToQueue(toSend))
                                            this.modified = false;
                            } else {
                                this.modified = false;
                            }
                            //System.out.println("input_Channel debug, this.modified oops, Channel:" +Name+"has PartnerName: " +PartnerName);
                                     
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
	private output_Channel partner_ref = null;
	public void setDistributed(){isLocal = false; partner = new output_Channel();}
        public void setDistributedWeakMigration(){isLocal = false; partner = new output_Channel(); this.modified=true;}
        public void setDistributedStrongMigration(){
            isLocal=false;
            //trick to reverse status back, that it should get an update from partner remotely, otherwise it will be in the same state as if the partner is local and has sent the data
            if(get_r_s()>get_r_r()){
                set_r_s(get_r_r());
            }
            this.modified = true;
        }
        
        public void TransmitCDLocChanges(String newSSLoc){
                    Object[] toSend = new Object[7]; 
                                    toSend[0] = PartnerName;
                                    toSend[1] = new Integer(this.get_r_s());
                                    toSend[2] = new Integer(this.get_r_r());
                                    toSend[3] = new Integer(this.get_preempted_val());
                                    if(value != null)
                                            toSend[4] = value;
                                    toSend[5] = Name;
                                    toSend[6] = newSSLoc;
                                    super.pushToQueue(toSend);
        }
        
        public void TransmitInChanStat(){
            //System.out.println("TransmitInChanStat sent");
                    Object[] toSend = new Object[6]; 
                                    toSend[0] = PartnerName;
                                    toSend[1] = new Integer(this.get_r_s());
                                    toSend[2] = new Integer(this.get_r_r());
                                    toSend[3] = new Integer(this.get_preempted_val());
                                    if(value != null)
                                            toSend[4] = value;
                                    toSend[5] = Name;
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
                 Object[] toSend = new Object[9]; 
                        toSend[0] = PartnerName;
                        toSend[1] = Name;
                        toSend[2] = "ReconfigChan";
                        toSend[3] = NewPartner;
                        toSend[4] = new Integer(this.get_r_s());
                                    toSend[5] = new Integer(this.get_r_r());
                                    toSend[6] = new Integer(this.get_preempted_val());
                                    toSend[8] = SJSSCDSignalChannelMap.getLocalSSName();
                                    super.pushToQueue(toSend);
            }                          
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
                Object[] toSend = new Object[9];  
                        toSend[0] = PartnerName;
                        toSend[1] = Name;
                        toSend[2] = "ReconfigPartnerChan";
                        toSend[3] = NewPartner;
                        toSend[4] = new Integer(this.get_r_s());
                                    toSend[5] = new Integer(this.get_r_r());
                                    toSend[6] = new Integer(this.get_preempted_val());
                                    toSend[8] = SJSSCDSignalChannelMap.getLocalSSName();
                                    super.pushToQueue(toSend);
            }
        }
        
        public void TransmitRespOccupyChan(String partnerName,String ack){
                    Object[] toSend = new Object[4];  // Creating an Object!!
                    toSend[0] = partnerName;
                    toSend[1] = Name;
                    toSend[2] = "RespOccupyChan";
                    toSend[3] = ack;
                    super.pushToQueue(toSend);
        }
        
        public void forcePreempt(){
            int val = this.preempted+1;
            partner.set_preempted(val);
        }
        
        public void setLocal(){isLocal=true;}
	// My data-structure copies to be read by partner
	private int r_r_copy = 0;
	private int r_s_copy = 0;
	private int preempted_copy = 0;
        private String CDState_copy="";
	public void set_partner_smp(output_Channel partner){this.partner_ref = partner; this.partner = new output_Channel();}
        
        public void set_partner_smp_migration(output_Channel partner){this.partner_ref = partner;this.cdmigrated=true;}
        
        //public void clear_partner_smp(){this.partner_ref = null;this.partner=null;}
        
        public output_Channel get_partner_smp(){return this.partner_ref;}
	protected synchronized void updateLocalPartner(input_Channel p){
		// This copying operation is regarded as an atomic operation
		p.r_s = this.r_s_copy;
		p.r_r = this.r_r_copy;
		p.preempted = this.preempted_copy;
                p.CDState = this.CDState_copy;
		if(value!=null)
			p.value = this.value;
	}
	protected synchronized void updateLocalCopy(){
		// This copying operation is regarded as an atomic operation
		this.r_s_copy = get_r_s();
		this.r_r_copy = get_r_r();
		this.preempted_copy = get_preempted_val();
                this.CDState_copy = getChannelCDState();
                this.cdmigrated = false;
	}
        
        public String getChannelCDState(){
            return this.CDState;
        }
        
        public void setChannelCDState(String cdstate){
            this.CDState = cdstate;
        }
        
        public boolean IsChannelLocal(){
            return isLocal;
        }
        
        public synchronized boolean getIncoming(){
            return incoming;
        }
        
        //public synchronized Object[] getToReceiverBuffer(){
        //    return toReceive;
        //}
        
}

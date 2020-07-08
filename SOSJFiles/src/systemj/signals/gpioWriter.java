//author : Yizhan Wang
//gpioWrite read signal from SystemJ and write signal value into gpio of BealgeBone.
//To seperate with gpio reader, this gpioWrite read "high" or "low" from SystemJ.
//In xml file it need "Name", "Path" and "gpioPort"

package systemj.signals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Hashtable;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.json.me.JSONTokener;
import systemj.common.SJMessageConstants;
import systemj.common.SJResponseMessage;

import systemj.interfaces.GenericSignalSender;

public class gpioWriter extends GenericSignalSender implements Serializable{
	String signalName = "";
	String dir;
	File file;
	BufferedWriter writer;
	String gpioPort;
	String IOstate;
        boolean previousOutputPinStat;
        /*
	public gpioWriter(){
		super();
	}
        * */
	
	@Override
	public void configure(Hashtable data) throws RuntimeException {
		if(data.containsKey("Name")){
			signalName = (String)data.get("Name");
		} else throw new RuntimeException("The configuration parameter 'Name' is required!");
		
		if(data.containsKey("Path")){
			dir = (String) data.get("Path");
		} else throw new RuntimeException("The configuration parameter 'Path' is required!");
		if(data.containsKey("gpioPort")){
			gpioPort =(String) data.get("gpioPort");
		}else throw new RuntimeException("The configuration parameter 'gpioPort' is required!");
		
              //  if (data.containsKey("IsSOA")){
                //    IsSOA=Boolean.parseBoolean("IsSOA");
               // } else throw new RuntimeException("Attribute 'IsSOA' is needed. choose 'true' for SOA or 'false' for regular SJ signal");
                
	}

	@Override
	public void run() {
            
            int debug=0;
		Object[] obj = super.buffer;
		String data = (String) obj[1];
            
       
           // } else {
                
                    if (debug==1) System.out.println("data to output is: " +data);
                    String location = 	"/sys/class/gpio/gpio" + gpioPort + "/value";
                    if (data == null){
			System.out.println(" data is null");
			Process p;   

                    System.out.println("signal is " + signalName);
			try {   
				  
				   String[] aaaa = {"/bin/bash", "-c", "su;echo high >/sys/class/gpio/gpio45/direction"};
				   p = Runtime.getRuntime().exec(aaaa);
	
			System.out.println(123);
				   
					//is.close();
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
		else if(data.equalsIgnoreCase("high")){
			Process p;   
			try {   
                                if (debug==1) System.out.println("writing output of gpio: " +gpioPort);
				   FileWriter commandfile = new FileWriter(location);
				   commandfile.write("1");
				   commandfile.flush();
                                   previousOutputPinStat = true;
				   
			}
			catch (Exception e){
				e.printStackTrace();
			}


		}
		else if(data.equalsIgnoreCase("low")){

			Process p;   
			try {   
                        if (debug==1) System.out.println("writing output of gpio: " +gpioPort);
    			FileWriter commandfile = new FileWriter(location);
				commandfile.write("0");
				commandfile.flush();
                                previousOutputPinStat = false;
			}
			catch (Exception e){
				e.printStackTrace();
			}

		}

		else{
			System.out.println("signal is " + signalName);
			System.out.println("data is " + data);
			System.out.println("gpio writer: not valid data, it must be 'high' or 'low' ");
		}
       //     }
              	
	}
        
        @Override
        public void resetPhySig(){
            if(previousOutputPinStat){
                
                 String location = 	"/sys/class/gpio/gpio" + gpioPort + "/value";
			try {   
                        
    			FileWriter commandfile = new FileWriter(location);
				commandfile.write("0");
				commandfile.flush();
			}
			catch (Exception e){
				e.printStackTrace();
			}
            }
        }
        
         @Override
        public void reinstatePhySig(){
            if(previousOutputPinStat){
               
                 String location = 	"/sys/class/gpio/gpio" + gpioPort + "/value";
			try {   
                        
    			FileWriter commandfile = new FileWriter(location);
				commandfile.write("1");
				commandfile.flush();
			}
			catch (Exception e){
				e.printStackTrace();
			}
            }
        }
}

package systemj.desktop;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Hashtable;

//import com.google.gson.*;

import systemj.common.IMBuffer;
import systemj.common.Interconnection;
import systemj.common.InterfaceManager;
import systemj.common.SOAFacility.TCPIPLinkRegistry;
import systemj.interfaces.GenericInterface;


public class TCPIPInterface extends GenericInterface implements Runnable {
	private String ip;
	private int port;
	private String ssname; // May be used later? Not ATM
	private Object[] buffer;
        private boolean IsLocal=true;
        
        ServerSocket serverSocket = null;
        Socket client = null;
        
        //Gson gson = new Gson();
        
	@Override
	public void configure(Hashtable ht){
		//if(ht.containsKey("Interface")){
			// This can be used later..
		//}
                
		if(ht.containsKey("Args")){
			String[] args = ((String)ht.get("Args")).trim().split(":");
			if(args.length != 2)
				throw new RuntimeException("Incorrect Args for TCP/IP interface : must be <IP>:<Port>");
			ip = args[0];
			port = new Integer(args[1]).intValue();
		}
		else
			throw new RuntimeException("Missing Args");
                /*
                if(ht.containsKey("DestArgs")){
			String[] DestArgs = ((String)ht.get("DestArgs")).trim().split(":");
			if(DestArgs.length != 2)
				throw new RuntimeException("Incorrect Destination Args for TCP/IP interface : must be <IP>:<Port>");
			destIp = DestArgs[0];
			destPort = new Integer(DestArgs[1]).intValue();
		} else {
                    throw new RuntimeException("Missing Args");
                }
		*/
		if(ht.containsKey("SubSystem")){
			ssname = ((String)ht.get("SubSystem")).trim();
		}
                
                if(ht.containsValue("systemj.desktop.TCPIPInterface")){
                    if(ht.containsKey("serverSocketObj")){
                        this.serverSocket = (ServerSocket)ht.get("serverSocketObj");
                    }
                }
                
	}

	@Override
	public void invokeReceivingThread() {
                unterminated = true;
		new Thread(this).start();
	}
        
        @Override
        public void TerminateInterface(){
            unterminated=false;
            
            //to synchronize with the thread.run instance, wait until socket timesout and current socket ends before restarting a new one
            while(timeoutcomplete){
                timeoutcomplete=false;
            }
            
        }

	@Override
	public void setup(Object[] o) {
		buffer = o;
	}

	@Override
	public boolean transmitData() {
		try {
                    //if(IsLocal){
                        //Socket client = new Socket(ip, port);
                       // System.out.println("TCPIPInterface transmit 1");
                   // if(client ==null){
                        client = new Socket(ip, port);
                   // } else 
                   // {
                        //System.out.println("TCPIPInterface transmit 2");
                        ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                        
                        
                        //* Changed for python-based programs - to JSON format
                        //String d = gson.toJson(buffer);
                        //byte[] b = d.getBytes();
                        //out.write(b,0,b.length);
                        
                        //* End changed for python-based programs
                        
                        //* Original SOSJ transmittint byte stream
			out.writeObject(buffer);
                        
                        out.flush();
                        
                        //* End original SOSJ
                           /*
                            System.out.println("TCPIPInterface Transmitting data: ");
                            for(int j=0;j<buffer.length;j++){
                                System.out.println("| " +buffer[j]+" |");
                            }
                            System.out.println("to: " +ip+":"+port);
                            */
			client.close();
                   // }
                    
			// Uses simple object output stream. no hassle.
			
                    
		}
		catch(java.net.ConnectException e){
			System.out.println("Could not reach server "+ip+":"+port);
                        e.printStackTrace();
			return false;
		}
                catch(java.net.SocketException e){
                        e.printStackTrace();
                        System.out.println("Cannot bind"+ip+":"+port);
			return false;
                }
                catch(UnknownHostException e){
			e.printStackTrace();
			return false;
		}
                catch(IOException e){
			e.printStackTrace();
			return false;
		}
               
		//catch(Exception e){
		//	e.printStackTrace();
		//	return false;
		//}
		
		return true;
	}

	@Override
	public void receiveData() {/* empty */}

	@Override
	public void run() {
            
		// This replace receive data for TCP case
            //System.out.println("Trying to establish link interface IPPort: " +ip+":"+port);
            
            ServerSocket serverSocket = null;
            try {
                
                if(this.serverSocket == null){
                    serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ip));
                    //System.out.println("link interface IPPort: " +ip+":"+port+ "has been created!");
                } else {
                    serverSocket = this.serverSocket;
                }
                //ServerSocket serverSocket = null
            } catch (IOException ex) {
                System.out.println("TCPIPInterface of IPPort: " +ip+":"+port+"has been bound");
                //ex.printStackTrace();
            }
            Socket socket=null;
            //while(unterminated){
            
                if(serverSocket!=null){
                    
                    System.out.println("TCPIPInterface, listening IPPort: " +ip+":"+port+ "!");
                    
                    try {
                        while(unterminated){
                            //serverSocket.setSoTimeout(1000);
                            try{
                                //serverSocket.setSoTimeout(100);
                                    socket = serverSocket.accept();
                                    
                                 /*   
                                    String s = "";
				//try (BufferedReader buffer2 = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
				//	s = buffer2.lines().collect(Collectors.joining("\n"));
				//}
				
				JsonElement je = new JsonParser().parse(s);
				JsonArray ja = je.getAsJsonArray();
                                int jArraySize = ja.size();
                                Object[] o = new Object[jArraySize];
                                
                                for(int j=0;j<jArraySize;j++){
                                    o[j] = (Object)ja.get(j).getAsString();
                                }
                                
				o[0] = (Object)ja.get(0).getAsString();
				o[1] = (Object)ja.get(1).getAsInt();
				o[2] = (Object)ja.get(2).getAsInt();
				o[3] = (Object)ja.get(3).getAsInt();
				if(ja.size() == 5)
					o[4] = (Object)ja.get(4).getAsString();
                                    */
                                    // ORIGINAL SOSJ link receive with Object serialized
                                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                                    Object[] o = (Object[])ois.readObject();

                                    
                                    
                                    super.forwardChannelData(o);
                                    socket.setSoLinger(true, 0);
                                    socket.close();

                            }catch (SocketTimeoutException ex){

                            }
                                    //System.out.println("Listening on "+ip+" "+port);

                          }

                        if(socket!=null){
                            socket.close();
                            serverSocket.close();
                        }
                        
                        System.out.println("TCPIPInterface, IPPort: " +ip+":"+port+ " closed!");

                          timeoutcomplete=true;
                            //}
                    }

                    catch (BindException bex){
                        bex.printStackTrace();
                            try {
                                socket.close();
                                } catch (IOException ex1) {
                                ex1.printStackTrace();
                            }
                    }
                    catch (ClassNotFoundException cex){
                        cex.printStackTrace();
                            try {
                                socket.close();
                                } catch (IOException ex1) {
                                ex1.printStackTrace();
                            }
                    }
                    catch(RuntimeException e){
                            e.printStackTrace();

                            try {
                                socket.close();
                                } catch (IOException ex1) {
                                ex1.printStackTrace();
                            }
                            //System.exit(1);
                    }
                    
                    catch(IOException e){

                            try {
                                socket.close();
                                } catch (IOException ex1) {
                                ex1.printStackTrace();
                            }

                            System.err.println("Error occured in TCPIPInterface, check the TCP/IP setting in the XML Interface");
                            e.printStackTrace();
                            
                            while(IMBuffer.getIMUpdatingFlag()){}
                            InterfaceManager im = IMBuffer.getInterfaceManagerConfig();

                            Interconnection ic = im.getInterconnection();

                            //JSONObject js = TCPIPLinkRegistry.GetSSAndPortPair();
                            TCPIPLinkRegistry.removePort(Integer.toString(port));

                            ic.removeRemoteInterfaces(ssname);
                            
                            im.setInterconnection(ic);
                            
                            IMBuffer.SaveInterfaceManagerConfig(im);
                            
                            //System.exit(1);
                        }
                    
                }
		
		//finally{
		//	System.exit(1);
		//}

	    //}
           
        }
}

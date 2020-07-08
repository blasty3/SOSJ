/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package systemj.signals;

/**
 *
 * @author Atmojo
 */


import systemj.interfaces.GenericSignalSender;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class BaxterSignal
  extends GenericSignalSender
{
  String ip;
  int port;
  Socket c = new Socket();
  OutputStream os;
  static Map<String, BaxterFeedback> m = new HashMap();
  
  public void configure(Hashtable data)
    throws RuntimeException
  {
    if (!data.containsKey("Name")) {
      throw new RuntimeException("BaxterControl : Missing attribute - Name");
    }
    if (!data.containsKey("IP")) {
      throw new RuntimeException("BaxterControl : Missing attribute - IP");
    }
    if (!data.containsKey("Port")) {
      throw new RuntimeException("BaxterControl : Missing attribute - Port");
    }
    this.name = ((String)data.get("Name"));
    this.ip = ((String)data.get("IP"));
    this.port = Integer.valueOf((String)data.get("Port")).intValue();
    try
    {
      this.c.close();
    }
    catch (IOException e)
    {
      e.printStackTrace();
      System.exit(1);
    }
  }
  
  public boolean setup(Object[] obj)
  {
    if ((obj.length != 2) || (!(obj[1] instanceof String))) {
      return false;
    }
    this.c = new Socket();
   
    try
    {
      this.c.connect(new InetSocketAddress(this.ip, this.port), 50);
      this.os = this.c.getOutputStream();
    }
    catch (SocketTimeoutException|ConnectException e)
    {
      try
      {
        this.c.close();
        return false;
      }
      catch (IOException e1)
      {
        e1.printStackTrace();
        System.exit(1);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }
    this.buffer = obj;
    return true;
  }
  
  class Worker
    implements Runnable
  {
    Socket c;
    InputStream is;
    String fbid;
    
    public Worker(Socket c, InputStream is, String fbid)
    {
      this.c = c;
      this.is = is;
      this.fbid = fbid;
    }
    
    public void run()
    {
      StringBuilder sb = new StringBuilder();
      byte[] b = new byte[5];
      try
      {
        while (this.is.read(b, 0, 5) >= 0)
        {
          sb.append(new String(b));
          b = new byte[5];
        }
        //System.out.println("Received data from Baxter: " +sb.toString().trim());
        BaxterFeedback fb = (BaxterFeedback)BaxterSignal.m.get(this.fbid);
        if (fb != null) {
          fb.setBuffer(new Object[] { Boolean.valueOf(true), sb.toString().trim() });
        } else {
            throw new RuntimeException("Baxter Feedback signal: " +this.fbid+" is not found");
        }
        return;
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
      finally
      {
        try
        {
          this.c.close();
          this.is.close();
        }
        catch (IOException e)
        {
          e.printStackTrace();
          System.exit(1);
        }
      }
    }
  }
  
  public void run()
  {
    String d = (String)this.buffer[1];
    d = d + "\n";
    try
    {
        
        //sending data to env
      this.os.write(d.getBytes());
      //System.out.println("Starting TCP thread to listen");
      Worker w = new Worker(this.c, this.c.getInputStream(), this.cdname + "." + this.name + "fb");
      //Worker w = new Worker(this.c, this.c.getInputStream(), this.cdname + "." +BaxterFeedback. );
      //Worker w = new Worker(this.c, this.c.getInputStream(), this.cdname + "." + this.name);
      new Thread(w).start();
    }
    catch (IOException e)
    {
      e.printStackTrace();
      try
      {
        this.c.close();
        this.os.close();
      }
      catch (IOException e1)
      {
        e1.printStackTrace();
        System.exit(1);
      }
    }
  }
  
  
}


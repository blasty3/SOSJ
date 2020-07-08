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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Hashtable;

public class BaxterSignalBlock
  extends GenericSignalSender
{
  String ip;
  int port;
  Socket c = new Socket();
  OutputStream os;
  
  public void configure(Hashtable data)
    throws RuntimeException
  {
    if (!data.containsKey("IP")) {
      throw new RuntimeException("BaxterControl : Missing attribute - IP");
    }
    if (!data.containsKey("Port")) {
      throw new RuntimeException("BaxterControl : Missing attribute - Port");
    }
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
    if (this.c.isClosed())
    {
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
    }
    this.buffer = obj;
    return true;
  }
  
  public void run()
  {
    String d = (String)this.buffer[1];
    d = d + "\n";
    try
    {
      this.os.write(d.getBytes());
      InputStream is = this.c.getInputStream();
      byte[] b = new byte[5];
      while (is.read(b, 0, 5) >= 0) {}
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


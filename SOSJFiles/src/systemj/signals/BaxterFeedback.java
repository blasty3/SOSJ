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

import systemj.interfaces.GenericSignalReceiver;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class BaxterFeedback
  extends GenericSignalReceiver
{
  String id;
  static Map<String, BaxterFeedback> m = new HashMap();
  
  public void configure(Hashtable arg0)
    throws RuntimeException
  {
    if (!arg0.containsKey("Name")) {
      throw new RuntimeException("BaxterControlFB needs Name attribute");
    }
    this.name = ((String)arg0.get("Name"));
    
    this.id = (this.cdname + "." + this.name);
    if (BaxterSignal.m.containsKey(this.id)) {
      throw new RuntimeException("Duplicated signals " + this.id);
    }
    BaxterSignal.m.put(this.id, this);
    System.out.println(BaxterSignal.m);
    System.out.println("initialized " + this.id);
  }
  
  public String getISigName(){
      return this.name;
  }
  
  public void run() {}
}

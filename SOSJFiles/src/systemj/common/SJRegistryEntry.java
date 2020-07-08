/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package systemj.common;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.me.JSONException;
import org.json.me.JSONObject;


/**
 *
 * @author Udayanto
 */
public class SJRegistryEntry {
    
    private static Hashtable availRegistryExpiryDet = new Hashtable();
    private static final Object availRegistryExpiryDetLock = new Object();
    
    private static JSONObject currentAvailRegistry = new JSONObject();
    private static final Object currentAvailRegistryLock = new Object();
    public static void AddRegistryToEntry(JSONObject jsRegAdvMsg){
        synchronized(currentAvailRegistryLock){
            try {
                String regID = jsRegAdvMsg.getString("regID");
                String regAddr = jsRegAdvMsg.getString("regAddr");
                currentAvailRegistry.put(regID, regAddr);
            } catch (JSONException ex) {
            ex.printStackTrace();
            } 
        }
    }
    public static JSONObject GetRegistryFromEntry(){
        synchronized(currentAvailRegistryLock){
            return currentAvailRegistry;
        }
    }
    
    public static boolean IsRegistryEntryEmpty(){
        synchronized(currentAvailRegistryLock){
            if(currentAvailRegistry.length()==0){
               return true;
            } else {
               return false;
            }
        }
    }
    
    public static void RemoveRegistryInEntry(String regID){
        synchronized(currentAvailRegistryLock){
            if(currentAvailRegistry.has(regID)){
                currentAvailRegistry.remove(regID);
            }
        }
    }
    public static void UpdateAllRegistryExpiry(Hashtable hash){
        synchronized(availRegistryExpiryDetLock){
            availRegistryExpiryDet = hash;
        }
    }
    public static void UpdateRegistryExpiry(JSONObject jsRegAdvMsg){
        synchronized(availRegistryExpiryDetLock){
            try {
            String regID = jsRegAdvMsg.getString("regID");
            String regAddr = jsRegAdvMsg.getString("regAddr");
            String regExpTime = jsRegAdvMsg.getString("retransmissionTime");
            if(availRegistryExpiryDet.containsKey(regID)){
               Hashtable regExpiryDet = (Hashtable)availRegistryExpiryDet.get(regID);
               regExpiryDet.put("loginTime",Long.toString(System.currentTimeMillis()));
               regExpiryDet.put("regAddr",regAddr);
               regExpiryDet.put("expiry",regExpTime);
            } else {
                 Hashtable regExpiryDet = new Hashtable();
                 regExpiryDet.put("loginTime",Long.toString(System.currentTimeMillis()));
                 regExpiryDet.put("regAddr",regAddr);
                 regExpiryDet.put("expiry",regExpTime);
               availRegistryExpiryDet.put(regID, regExpiryDet);
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        }
    }
    private static Hashtable GetRegistryExpiryDet(String regID){
        synchronized(availRegistryExpiryDetLock){
            return (Hashtable)availRegistryExpiryDet.get(regID);
        }
    }
    public static void UpdateRegistryEntryWithNewList(JSONObject js){
        synchronized(currentAvailRegistryLock){
            currentAvailRegistry = js;
        }
    }
    public static Hashtable GetAllRegistryExpiryDet(){
        synchronized(availRegistryExpiryDetLock){
            return availRegistryExpiryDet;
        }
    }
    public static Vector getExpiredRegistry(){
        Vector ExpiredReg = new Vector();
        JSONObject allReg = GetRegistryFromEntry();
        Enumeration keysAllReg = allReg.keys();
        while(keysAllReg.hasMoreElements()){
            String regID = keysAllReg.nextElement().toString();
            Hashtable expiryDetOfId = GetRegistryExpiryDet(regID);
            long regLoginTime = Long.parseLong((String)expiryDetOfId.get("loginTime"));
            long expiryTime = Long.parseLong((String) expiryDetOfId.get("expiry"));
            long deltaT = System.currentTimeMillis()-(regLoginTime);
            System.out.println("Check expiry of RegID: " +regID+ " Expiry: " +expiryTime+ " LoginTime: " +regLoginTime+ " and difference: " +deltaT );
            if(deltaT>=expiryTime){
               ExpiredReg.addElement(regID);
            }
        }
          return ExpiredReg;
    }
}

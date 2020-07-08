/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package systemj.common;

import java.util.Enumeration;
import org.json.me.JSONException;
import org.json.me.JSONObject;

/**
 *
 * @author Atmojo
 */
public class RegAllCDStats {
    private static JSONObject IntCDMStateInLocSS = new JSONObject();
    private static final Object IntCDMStateInLocSSLock = new Object();
    private static JSONObject RegAllCDMStats = new JSONObject();
    private final static Object RegAllCDMStatsLock = new Object();
    public static void AddCDStat(String SSName, JSONObject CDStats){
        synchronized(RegAllCDMStatsLock){
            try {
                JSONObject newAllCDStats = new JSONObject();
                    Enumeration keysCDStats = CDStats.keys();
                    while(keysCDStats.hasMoreElements()){
                        String CDName = keysCDStats.nextElement().toString();
                        newAllCDStats.put(CDName, CDStats.getString(CDName));   
                    }
                    RegAllCDMStats.put(SSName, newAllCDStats);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
    }
    public static JSONObject getAllCDStatsOfSSName(String SSName){
        JSONObject AllCDStats = new JSONObject();
        synchronized (RegAllCDMStatsLock){
            try {
                AllCDStats  = RegAllCDMStats.getJSONObject(SSName);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return AllCDStats;
    }
    public static JSONObject getAllCDStats(){
        synchronized (RegAllCDMStatsLock){
            return RegAllCDMStats;
        }
    }
    public static void removeAllCDStatInSS(String SSName){
        synchronized (RegAllCDMStatsLock){
            RegAllCDMStats.remove(SSName);
        }
    }
     public static void AddIntCDMacroState(String CDName, String CDState){
        synchronized(IntCDMStateInLocSSLock){
            try {
                IntCDMStateInLocSS.put(CDName, CDState);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
    }
    public static String GetIntCDMacroState(String CDName){
        synchronized(IntCDMStateInLocSSLock){
            try {
                return IntCDMStateInLocSS.getString(CDName);
            } catch (JSONException ex) {
                ex.printStackTrace();
                return "None";
            }
        }
    }
    public static JSONObject GetAllIntCDMacroState(){
        synchronized(IntCDMStateInLocSSLock){
            return IntCDMStateInLocSS;
        }
    }
    public static void RemoveIntCDMacroState(String CDName){
        synchronized(IntCDMStateInLocSSLock){
            if(IntCDMStateInLocSS.has(CDName)){
                IntCDMStateInLocSS.remove(CDName);
            }
        }
    }
}
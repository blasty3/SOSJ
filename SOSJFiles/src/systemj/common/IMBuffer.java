/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package systemj.common;
/**
 *
 * @author Udayanto
 */
public class IMBuffer {
    private static InterfaceManager imObj;
    public final static Object imObjLock = new Object();
    private static boolean IMIsUpdating = false;
    public static void SaveInterfaceManagerConfig(InterfaceManager im){
        synchronized(imObjLock){
            imObj = im;
            IMIsUpdating = false;
        }
    }
    public static InterfaceManager getInterfaceManagerConfig(){
        synchronized(imObjLock){
            return imObj;
        }
    }
    public static boolean getIMUpdatingFlag(){
        boolean stat = false;
        synchronized(imObjLock){
            if(!IMIsUpdating){
                stat=IMIsUpdating;
                IMIsUpdating = true;
            }
             return stat;
        }
    }
    public static void setIMUpdatingFlag(boolean stat){
        synchronized(imObjLock){
             IMIsUpdating=stat;
        }
    }
}

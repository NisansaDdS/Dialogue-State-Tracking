/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
/**
 *
 * @author Zhuoran Wang
 */
public class TurnObject {
    
    public String sysStartTime = "";
    public String sysUtterance = "";
    public ArrayList<DialogAct> sysDialogActs = new ArrayList<DialogAct>();
    
    public String usrStartTime = "";
    public ArrayList<DialogAct> usrSLUHypos = new ArrayList<DialogAct>();
    
    public String toString()
    {
        String result = "System: ";
        for ( DialogAct sys :  sysDialogActs )
        {
            if ( sys != null )
                result += sys.toString() + " ";
            else
                result += "null ";
        }
        result += "\n\n";
        
        for ( DialogAct usr : usrSLUHypos )
        {
            if ( usr != null )
                result += usr.toString() + " " + usr.getConfScore() + "\n";
        }
        
        return result;
    }
    
    
    
}

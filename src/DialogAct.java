/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
/**
 *
 * @author zw57
 */
public class DialogAct {
    
    private float confScore = 0;
    
    String actionType = "";
    
    ArrayList<SVP> svPairs = null;
    
    public int slotNum()
    {
        if ( svPairs == null )
            return 0;
        return svPairs.size();
    }
    
    public void insertSVP(SVP svp)
    {
        
        if ( svp != null )
        {
            
            if (  svPairs == null )
                svPairs = new ArrayList<SVP>();
            
            svPairs.add(svp);
            
        }

    }
    
    public void setActionType(String actT)
    {
        actionType = actT;
    }
    
    public DialogAct()
    {
        
    }
    
    public DialogAct(String act, String slots)
    {
        actionType = act;
        
        if ( !slots.isEmpty() )
        {
            if ( svPairs == null )
                svPairs = new ArrayList<SVP>();
            
            String[] toks = slots.split(",");
            for ( int i = 0; i < toks.length; ++i )
            {
                String[] sv = toks[i].split("=");
                String theSlot = sv[0].trim();
                String theValue = null;
                if ( sv.length > 1 )
                    theValue = sv[1].trim();
                svPairs.add(new SVP(theSlot, theValue));
            }
        }
    }
    
    public boolean isRestart()
    {
        return actionType.equals("restart");
    }
    
    public boolean isRequest()
    {
        return actionType.equals("request");
    }
    
    public boolean isCantHelp()
    {
        return actionType.startsWith("canthelp");
    }
    
    public int isConfirm()
    {
        
            if ( actionType.equals("impl-conf") )
                return 1;
            
            if ( actionType.equals("expl-conf") )
                return 2;
            
            return 0;
    }
    
    public boolean isAffirm()
    {
        return actionType.equals("affirm");
    }
    
    public boolean isNegate()
    {
        return actionType.equals("negate");
    }
    
    public boolean isDeny()
    {
        return actionType.equals("deny");
    }
    
    public boolean isInform()
    {
        return actionType.equals("inform");
    }
    
    public boolean isNextbus()
    {
        return actionType.equals("nextbus");
    }
    
    public boolean isPrevbus()
    {
        return actionType.equals("prevbus");
    }
    
    public void setScore(float score)
    {
        confScore = score;
    }
    
    public float getConfScore()
    {
        return confScore;
    }
    
    public String toString()
    {
        String result = actionType+"(";
        if ( svPairs != null )
        {
            for( SVP svp : svPairs )
            {
                if ( svp != null )
                    result += ", "+svp.toString();
            }
            result = result.replaceFirst(", ", "");
        }
        
        result += ")";
        return result;
    }
    
    public ArrayList<DialogAct> split()
    {
        ArrayList<DialogAct> result = new ArrayList<DialogAct>();
        if ( actionType != null )
        {
            if ( svPairs == null )
                result.add(this);
            else
            {
                for ( SVP svp : svPairs )
                {
                    DialogAct da = new DialogAct();
                    da.setActionType(actionType);
                    da.insertSVP(svp);
                    da.setScore(confScore);
                    result.add(da);
                }
            }
        }
        
        return result;
            
    }
    
    /*
    public boolean sv_equals(DialogAct da)
    {
        if ( this.slotNum() > 1 || da.slotNum() > 1 || this.slotNum() != da.slotNum() )
            return false;
        
        if ( this.svPairs == null )
        {
            if ( da.svPairs == null )
                return true;
            else
                return false;
        }
        else
        {
            if ( da.svPairs == null )
                return false;
            else
                return this.svPairs.get(0).equals(da.svPairs.get(0));
        }
        
    }
    
    public boolean slot_equals(DialogAct da)
    {
        if ( this.slotNum() > 1 || da.slotNum() > 1 || this.slotNum() != da.slotNum() )
            return false;
        
        if ( this.svPairs == null )
        {
            if ( da.svPairs == null )
                return true;
            else
                return false;
        }
        else
        {
            if ( da.svPairs == null )
                return false;
            else
                return this.svPairs.get(0).getSlot().equals(da.svPairs.get(0).getSlot());
        }
        
    }
    */
    
    public boolean equals(DialogAct da)
    {
        if ( this.slotNum() > 1 || da.slotNum() > 1 || this.slotNum() != da.slotNum() )
            return false;
        
        if ( this.actionType == null && da.actionType == null )
            return true;
        
        if ( this.actionType != null && this.actionType.equals(da.actionType) )
        {
            if ( this.svPairs == null )
            {
                if ( da.svPairs == null )
                    return true;
                else
                    return false;
            }
            else
            {
                if ( da.svPairs == null )
                    return false;
                else
                {
                    for ( int i = 0; i < this.slotNum(); ++i )
                        if ( !this.svPairs.get(i).equals(da.svPairs.get(i)) )
                            return false;
                    return true;
                }
            }
        }
        
        return false;
    }
    
}

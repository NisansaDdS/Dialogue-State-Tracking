/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author zw57
 */
public class SVP {
    
    private String slot = null;
    private String value = null;
    
    private String[] stopwordList = { "from", "to", "at", "the", "i'm", "going" };
    
    public SVP(String s, String v)
    {
        setSlot(s);
        setValue(v);
    }
    
    public SVP() 
    {
        
    }
    
    public boolean is(String type)
    {
        return slot.startsWith(type);
    }
    
    public String getMainSlot()
    {
        int pos = slot.indexOf(".");
        if ( pos > 0 )
            return slot.substring(0, pos);
        else
            return "";
    }
    
    public String getSubSlot()
    {
        int pos = slot.indexOf(".");
        if ( pos < 0 )
            return null;
        return slot.substring(pos+1);
    }
    
    public String getValue()
    {
        return value;
    }
    
    public String getSlot()
    {
        return slot;
    }
    
    public void setValue(String v)
    {
        if ( v != null )
            value = v; //normalize(v);
    }
    
    public void setSlot(String s)
    {
        slot = s;
    }
    
    public String toString()
    {
        String result = null;
        if ( slot != null )
        {    
            result = slot;
            if ( value != null )
                result += "=" + value;
        }
        return result;
    }
    
    public void normalize()
    {        
        //value = value.trim().toLowerCase();
        String[] words = value.split(" ");
        String result = "";
        for(String s : words)
        {
            if ( ! isStopword(s) )
            {
                result += regularize(s) + " ";
            }
        }
        
        result = result.trim();
        
        if ( result.isEmpty() )
            value = null;
        else
            value = result;         
    }
    
    private boolean isStopword(String word)
    {
        for ( String s : stopwordList )
            if ( s.equals(word) )
                return true;
        return false;
    }
    
    private String regularize(String word)
    {
        if ( word.equals("ave") )
            return "avenue";
        return word;
    }
    
    public boolean equals(SVP svp)
    {
        if ( svp == null ) return false;
        if ( this.slot != null )
        {
            if ( this.slot.equals(svp.slot) )
            {
               if ( this.value != null )
               {
                   return this.value.equals(svp.value);
               }
               else
                   return svp.value == null;
            }
        }
        else if ( svp.slot == null )
            return true;
        
        return false;
    }
}

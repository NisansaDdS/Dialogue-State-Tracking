/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author zw57
 */
import java.util.ArrayList;
import java.util.Iterator;
import java.io.BufferedWriter;

public class BeliefTracker {
    
    private BeliefStruct curBelief = new BeliefStruct();
    
    private ArrayList<SVP> pendingBlockRules = new ArrayList();
    private ArrayList<ArrayList<SVP> > pendingJointBlockRules = new ArrayList();
    
    private void normalise(ArrayList<DialogAct> obs)
    {
        for ( DialogAct da : obs )
        {
            if ( da.svPairs != null )
            {
                for ( SVP sv : da.svPairs )
                    sv.normalize();
            }
        }
    }
    
    public BeliefTracker()
    {
        curBelief.init();
    }
    
    public void update(ArrayList<DialogAct> sysAct, ArrayList<DialogAct> obs, int turnid)
    {
        
        //normalise(obs);
        /*
        for ( DialogAct da : obs )
        {
            if ( da.svPairs != null )
                curBelief.updateObsDateTimeHistory(da.svPairs);
        }
        */
        ArrayList<DialogAct> compact_obs = prune(merge(obs));
        
        boolean emptyObs = compact_obs.isEmpty();
        
        if ( !emptyObs && ( !pendingBlockRules.isEmpty() || !pendingJointBlockRules.isEmpty() ) )
        {
            for ( SVP sv : pendingBlockRules )
                curBelief.addBlockRule(sv);
            pendingBlockRules.clear();
            
            for ( ArrayList<SVP> svs : pendingJointBlockRules )
                curBelief.addJointBlockRule(svs);
            
            pendingJointBlockRules.clear();
        }
        
        for ( DialogAct a : sysAct )
        {    
            //restart
            if ( a.isRestart() )
            {    
                curBelief.init();
                break;
            }
            
            //can't help, block the slot-value
            if ( a.isCantHelp() && a.svPairs != null )
            {
                ArrayList<SVP> sv = a.svPairs;
                if ( sv.size() == 1 )
                {
                    if ( emptyObs )
                        pendingBlockRules.add(sv.get(0));
                    else
                        curBelief.addBlockRule(sv.get(0));
                    break;
                }
                else
                {
                    if ( emptyObs )
                        pendingJointBlockRules.add(sv);
                    else
                        curBelief.addJointBlockRule(sv);
                    
                    String slot = null;
                    for ( DialogAct r : sysAct )
                    {
                        if ( r.isRequest() )
                        {
                            slot = r.svPairs.get(0).getSlot();
                            break;
                        }
                    }
                    if ( slot == null ) continue;
                    
                    for ( SVP s : sv )
                    {
                        if ( s.getSlot().startsWith(slot) )
                        {
                            if ( emptyObs )
                                pendingBlockRules.add(sv.get(0));
                            else
                                curBelief.addBlockRule(s);
                        }
                    }
                }
            }
            
            //impl-conf: if no explicit affirm/negate/re-inform, taken as implicit affirm
            if ( a.isConfirm() == 1 )
            {
                boolean agree = true;
                for ( DialogAct da : compact_obs )
                {
                    if ( da.isAffirm() || da.isNegate() )
                        agree = false;
                    else if ( da.isInform() || da.isDeny() )
                    {
                        for ( SVP sv : a.svPairs )
                        {
                            if ( da.svPairs.get(0).getSlot().equals(sv.getSlot()) )
                                agree = false;
                        }
                    }
                }
                
                if ( agree )
                {
                    DialogAct tmp = new DialogAct();
                    tmp.setActionType("affirm");
                    tmp.setScore(1);
                    compact_obs.add(tmp);
                }
            }
        }
        
        
        for ( DialogAct da : compact_obs )
            update(sysAct, da, turnid);
        
        
        curBelief.genBeliefs();
        
    }
    
    private void update(ArrayList<DialogAct> sysAct, DialogAct da, int turnid)
    {
        if ( da.isAffirm() )
        {
            for ( int i = 0; i < sysAct.size(); ++i )
            {
                DialogAct sa = sysAct.get(i);
                if ( sa.isConfirm() > 0 )
                {
                    for ( SVP sv : sa.svPairs )
                        curBelief.update(sv, da.getConfScore(), false, turnid);
                }
            }
        }
        else if ( da.isNegate() )
        {
            for ( int i = 0; i < sysAct.size(); ++i )
            {
                DialogAct sa = sysAct.get(i);
                if ( sa.isConfirm() > 0 )
                {
                    for ( SVP sv : sa.svPairs )
                        curBelief.update(sv, da.getConfScore(), true, turnid);
                }
            }
        }
        
        if ( da.isInform() )
        {
            curBelief.update(da.svPairs.get(0), da.getConfScore(), false, turnid);
            
        }
        else if ( da.isDeny() )
        {
            curBelief.update(da.svPairs.get(0), da.getConfScore(), true, turnid);
        }
        
        
    }
    
    private ArrayList<DialogAct> merge(ArrayList<DialogAct> obs)
    {
        ArrayList<DialogAct> compact = new ArrayList<DialogAct>();
        for ( DialogAct da : obs )
        {
            ArrayList<DialogAct> sub = da.split();
            for ( DialogAct s : sub )
            {
                compact = merge(compact,s);
            }
        }
        return compact;
    }
    
    private  ArrayList<DialogAct> merge(ArrayList<DialogAct> compact, DialogAct da)
    {
        boolean merged = false;
        for ( int i = 0; i < compact.size(); ++i )
        {
            DialogAct tmp = compact.get(i);
            if ( tmp.equals(da) )
            {
                tmp.setScore(tmp.getConfScore()+da.getConfScore());
                if ( tmp.getConfScore() > 1 )
                    tmp.setScore(1);
                compact.set(i, tmp);
                merged = true;
            }
        }
        if ( !merged )
            compact.add(da);
        return compact;
    }
    
    private ArrayList<DialogAct> prune(ArrayList<DialogAct> compact)
    {
        Iterator itr = compact.iterator();
        while ( itr.hasNext() )
        {
            DialogAct da = (DialogAct)itr.next();
            if ( ! da.isInform() && ! da.isAffirm() && !da.isDeny() && !da.isNegate() )
                itr.remove();
        }
        
        return compact;
    }
    
    public void printBeliefs()
    {
        curBelief.print();
    }
    
    public void printJSON(BufferedWriter bw) 
    {
        curBelief.printJSON(bw);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
////// Methods used by Query Engine
    public void ShowHypo(){
        curBelief.ShowHypo();
    }

    public ArrayList<BeliefStruct.SlotHypo> getHypo(){
        return curBelief.getHypo();
    }


    public void ShowJointHypo(){
        curBelief.ShowJointHypo();
    }

    public ArrayList<BeliefStruct.JointHypo> getJointHypo(){
        return curBelief.getJointHypo();
    }
    
    // Add by Miao
    public void AddpendingBlockRules(SVP sv){
    	if(!pendingBlockRules.contains(sv))
    		pendingBlockRules.add(sv);
    }
    public void AddpendingJointBlockRules(ArrayList<SVP> svs){
    	if(!pendingJointBlockRules.contains(svs))
    		pendingJointBlockRules.add(svs);
    }
}

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
import java.util.Arrays;
import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.Map;

public class BeliefStruct {
    
    private final int MAX_COUNT = 100;
    
    public class ValueHypo {
        public String name = "";
        
        public double belief_prob = 0;
        
        
        public void update(double p, boolean deny)
        {
            if ( ! deny )
            {
                if ( belief_prob == 0 )
                    belief_prob = p;
                else
                    belief_prob = 1-(1-belief_prob)*(1-p);
            }
            else
            {
                belief_prob *= (1-p);
            }
        }
        
        //for debug
        public void print()
        {
            System.out.println("\n\tVALUE: "+name);
            System.out.println("\tBelief: "+belief_prob);
            //System.out.print("\t\t");
            //for ( int i = 0; i <= count; ++i )
            //    System.out.print(i+":"+count_probs[i]+" ");
            
        }
    }
    
    public class SlotHypo {
        
        public String name = "";
        public ArrayList<ValueHypo> value_hypos = new ArrayList();
        //public int count = 0;
        public double nullProb = 1;
        public int turn = -1;
        
        public void update(String value, double p, boolean deny, int turnid)
        {
            int i = find(value,0,value_hypos.size()-1,true);
            value_hypos.get(i).update(p, deny);
            if ( turnid != turn )
            {
                //count ++;
                turn = turnid;
            }
        }
        
        private int find(String value, int start, int end, boolean addnew)
        {
            if (end < start )
            {
                if ( addnew )
                {
                    ValueHypo vh = new ValueHypo();
                    vh.name = value;
                    value_hypos.add(start, vh);
                    return start;
                }
                else
                    return -1;
            }
            else
            {
              
              int imid = (int)((start+end)/2);
              
              int cmp = value_hypos.get(imid).name.compareTo(value);
              
              if ( cmp >0 )
                return find(value, start, imid-1, addnew);
              else if (cmp < 0)
                return find(value, imid+1, end, addnew);
              else
                return imid;
            }
             
        }
        
        public void recalcBeliefs()
        {
            double sum = 0;
            double[] y = new double[value_hypos.size()];
            for ( int i = 0; i < value_hypos.size(); ++i )
            {    
                y[i] = value_hypos.get(i).belief_prob;
                if ( y[i] == 0 )
                    y[i] = 1E-9;
                sum += y[i];
            }
            if ( sum > 1 + 1E-6 )
            {
                MyOptimizer opt = new MyOptimizer();
                opt.dim = value_hypos.size();
                opt.y = y;
                double[] x = opt.normalise(y);//opt.computeBelief();
                for ( int i = 0; i < value_hypos.size(); ++i )
                    value_hypos.get(i).belief_prob = x[i];
                sum = 1;
            }
            
            nullProb = 1-sum;
        }
        
        //for debug
        public void print()
        {
            System.out.println("SLOT: " + name);
            System.out.println("\tnullHypo: " + nullProb);
            
            for ( ValueHypo vh : value_hypos )
                vh.print();
            
            System.out.println();
        }
    }

    
    public class DateTimeHypo {
        
        public double belief_prob = 0;
        public String type = "";
        
        public HashMap<String,String> content;
        
        
        public DateTimeHypo(String t)
        {
            type = t;
            content = new HashMap<String,String>();
        }
        
        public DateTimeHypo(DateTimeHypo h)
        {
            type = h.type;
            content = new HashMap<String,String>(h.content);
        }
        
        public boolean is(String name)
        {
            return type.equals(name);
        }
        
        public boolean addSVP(SVP sv)
        {
            String sub_slot = sv.getSubSlot();
            
            if ( !sv.getMainSlot().equals(type) )
                return false;
            
            if ( sub_slot == null || content.containsKey(sub_slot) )
                return false;
            
            content.put(sub_slot, sv.getValue());
            
            return true;
        }
        

        public boolean isEmpty()
        {
            return content.isEmpty();
        }
        
        public boolean isValid()
        {
            if ( type.equals("time") )
            {
                if ( content.containsKey("rel") )
                {
                    if ( content.containsKey("hour") || content.containsKey("minute") || content.containsKey("ampm") )
                        return false;
                }
                
                if ( content.containsKey("hour") && ! content.containsKey("minute") )
                    return false;
                
                if ( !content.containsKey("hour") && content.containsKey("minute") )
                    return false;
                
                if ( content.containsKey("ampm") && !(content.containsKey("hour") && content.containsKey("minute")) )
                    return false;
                
                if ( content.containsKey("arriveleave") && content.size() == 1 )
                    return false;
                            
            }
            else if ( type.equals("date") )
            {
                /*
                if ( content.containsKey("day") || content.containsKey("relweek") )
                {
                    if ( content.containsKey("absmonth") || content.containsKey("absday") )
                        return false;
                }
                */
                //if ( content.containsKey("absmonth") && !content.containsKey("absday") )
                //    return false;
                //if ( !content.containsKey("absmonth") && content.containsKey("absday") )
                //    return false;
            }
            else
                return false;
                
            return true;
        }

        public boolean equals(DateTimeHypo h)
        {
            
            return type.equals(h.type) && content.equals(h.content);
        }
        
        public void printJSON(BufferedWriter bw)
        {
            try {
                
                boolean first = true;
                for ( Map.Entry<String,String> sv : content.entrySet() )
                {
                    if ( first )
                    {
                        if ( sv.getKey().equals("minute") || sv.getKey().equals("hour") || sv.getKey().startsWith("abs") )
                            bw.write("\t\t\t\t\t\t\t\t\t\""+type+"."+sv.getKey()+"\": "+sv.getValue());
                        else
                            bw.write("\t\t\t\t\t\t\t\t\t\""+type+"."+sv.getKey()+"\": \""+sv.getValue()+"\"");
                        first = false;
                    }
                    else
                    {
                        if ( sv.getKey().equals("minute") || sv.getKey().equals("hour") || sv.getKey().startsWith("abs") )
                            bw.write(",\n\t\t\t\t\t\t\t\t\t\""+type+"."+sv.getKey()+"\": "+sv.getValue());
                        else
                            bw.write(",\n\t\t\t\t\t\t\t\t\t\""+type+"."+sv.getKey()+"\": \""+sv.getValue()+"\"");
                    }
                }

            }
            catch ( Exception e)
            {
                e.printStackTrace();
            }
        }
        /*
        public void update(double p, boolean deny)
        {
            if ( deny )
                belief_prob *= (1-p);
            else
            {
                if ( belief_prob == 0 )
                    belief_prob = p;
                else
                    belief_prob = 1-(1-belief_prob)*(1-p);
            }
        }
         
         */
    }
    
    
    public class JointHypo {
        
        public HashMap<String,String> content;
        public DateTimeHypo timeHypo = null;
        public DateTimeHypo dateHypo = null;
        
        public double belief_prob = 0;
        
        public JointHypo()
        {
            content = new HashMap<String,String>();
        }
        
        public JointHypo(JointHypo h)
        {
            content = new HashMap<String,String>(h.content);
            if ( h.timeHypo != null )
                timeHypo = new DateTimeHypo(h.timeHypo);
            if ( h.dateHypo != null )
                dateHypo = new DateTimeHypo(h.dateHypo);
            belief_prob = h.belief_prob;
        }
        
        public boolean isEmpty()
        {
            return content.isEmpty() && timeHypo == null && dateHypo == null;
        }
        
        private boolean compare(Object o1, Object o2)
        {
            if ( o1 == null )
            {
                if ( o2 != null )
                    return false;
            }
            else if ( !o1.equals(o2) )
                return false;
            return true;
        }
        
        public boolean equals(JointHypo h)
        {
            return content.equals(h.content) && compare(timeHypo,h.timeHypo) && compare(dateHypo,h.dateHypo);
        }
        
        public boolean in(JointHypo h)
        {
//            if ( content.isEmpty() || h.content.isEmpty() )
//                return false;
            
            for ( Map.Entry<String,String> sv : content.entrySet() )
            {
                if ( !h.content.containsKey(sv.getKey()) )
                    return false;
                if ( h.content.get(sv.getKey()) != null && !h.content.get(sv.getKey()).equals(sv.getValue()) )
                    return false;
   
            }
            
            if ( timeHypo != null && !timeHypo.equals(h.timeHypo) )
                return false;
            if ( dateHypo != null && !dateHypo.equals(h.dateHypo) )
                return false;
            return true;
        }
        
        
        public boolean addSVP(SVP sv)
        {
            
            if ( content.containsKey(sv.getSlot()) )
                return false;
            
            content.put(sv.getSlot(), sv.getValue());
            return true;
        }
        
        public boolean addDateTimeHypo( DateTimeHypo dt )
        {
            if ( dt.is("time") )
            {
                if ( timeHypo != null )
                    return false;
                timeHypo = dt;
                return true;
            }
            else if ( dt.is("date") )
            {
                if ( dateHypo != null )
                    return false;
                dateHypo = dt;
                return true;
            }
            return false;
        }
        
        public void printJSON(BufferedWriter bw)
        {
            try {
                
                
                boolean first = true;
                for ( Map.Entry<String,String> sv : content.entrySet() )
                {
                    if ( first )
                    {
                        bw.write("\t\t\t\t\t\t\t\t\t\""+sv.getKey()+"\": \""+sv.getValue()+"\"");
                        first = false;
                    }
                    else
                        bw.write(",\n\t\t\t\t\t\t\t\t\t\""+sv.getKey()+"\": \""+sv.getValue()+"\"");
                }
                if ( timeHypo != null )
                    for ( Map.Entry<String,String> sv : timeHypo.content.entrySet() )
                    {
                        if ( first )
                        {
                            if ( sv.getKey().equals("hour") || sv.getKey().equals("minute") )
                                bw.write("\t\t\t\t\t\t\t\t\t\""+"time."+sv.getKey()+"\": "+sv.getValue());
                            else
                                bw.write("\t\t\t\t\t\t\t\t\t\""+"time."+sv.getKey()+"\": \""+sv.getValue()+"\"");
                            first = false;
                        }
                        else
                        {
                            if ( sv.getKey().equals("hour") || sv.getKey().equals("minute") )
                                bw.write(",\n\t\t\t\t\t\t\t\t\t\""+"time."+sv.getKey()+"\": "+sv.getValue());
                            else
                                bw.write(",\n\t\t\t\t\t\t\t\t\t\""+"time."+sv.getKey()+"\": \""+sv.getValue()+"\"");
                        }
                    }
                if ( dateHypo != null )
                    for ( Map.Entry<String,String> sv : dateHypo.content.entrySet() )
                    {
                        if ( first )
                        {
                            if ( sv.getKey().startsWith("abs") )
                                bw.write("\t\t\t\t\t\t\t\t\t\""+"date."+sv.getKey()+"\": "+sv.getValue());
                            else
                                bw.write("\t\t\t\t\t\t\t\t\t\""+"date."+sv.getKey()+"\": \""+sv.getValue()+"\"");
                            first = false;
                        }
                        else
                        {
                            if ( sv.getKey().startsWith("abs") )
                                bw.write(",\n\t\t\t\t\t\t\t\t\t\""+"date."+sv.getKey()+"\": "+sv.getValue());
                            else
                                bw.write(",\n\t\t\t\t\t\t\t\t\t\""+"date."+sv.getKey()+"\": \""+sv.getValue()+"\"");
                        }
                    }
            }
            catch ( Exception e)
            {
                e.printStackTrace();
            }
        }
        
    }
    
    public ArrayList<SlotHypo> slot_hypos = new ArrayList();
    private ArrayList<SVP> blocked_single = new ArrayList();
    private ArrayList<JointHypo> blocked_joint = new ArrayList();
    
    ArrayList<DateTimeHypo> time_hypos = new ArrayList<DateTimeHypo>();
    ArrayList<DateTimeHypo> date_hypos = new ArrayList<DateTimeHypo>();
    ArrayList<JointHypo> joint_hypos = new ArrayList();
    
    ArrayList<DateTimeHypo> observed_date_time_history = new ArrayList();
    
    public void update(SVP sv, double p, boolean deny, int turnid)
    {
        for ( int j = 0; j < blocked_single.size(); ++j )
            if ( blocked_single.get(j).equals(sv) )
                return;
        
        int i  = find(sv.getSlot(),0,slot_hypos.size()-1,true);
        slot_hypos.get(i).update(sv.getValue(), p, deny, turnid);

    }
    
    
     
    public void genBeliefs()
    {
        for ( SlotHypo sh : slot_hypos )
            sh.recalcBeliefs();
    }
    
    public void addBlockRule(SVP sv)
    {
        for ( int j = 0; j < blocked_single.size(); ++j )
            if ( blocked_single.get(j).equals(sv) )
                return;
        blocked_single.add(sv);
        
        int i = find(sv.getSlot(),0,slot_hypos.size()-1,false);
        
        if ( i >= 0 )
        {
            int j = slot_hypos.get(i).find(sv.getValue(), 0, 
                                        slot_hypos.get(i).value_hypos.size()-1, false);
            if ( j >= 0 )
            {    
                slot_hypos.get(i).value_hypos.remove(j);
                if ( slot_hypos.get(i).value_hypos.isEmpty() )
                    slot_hypos.remove(i);
            }
        }
        
    }
    
    public void addJointBlockRule(ArrayList<SVP> svs)
    {
        JointHypo jh = new JointHypo();
        DateTimeHypo dh = new DateTimeHypo("date");
        DateTimeHypo th = new DateTimeHypo("time");
        
        for ( SVP sv : svs )
        {
            jh.addSVP(sv);
            dh.addSVP(sv);
            th.addSVP(sv);
        }
        
        if ( !dh.isEmpty() )
            jh.addDateTimeHypo(dh);
        
        if ( !th.isEmpty() )
            jh.addDateTimeHypo(th);
        
        if ( !jh.isEmpty() )
        {
            boolean exist = false;
            for ( JointHypo h : blocked_joint )
                if ( h.in(jh) )
                    exist = true;
            if ( ! exist )
            {
                Iterator iter = blocked_joint.iterator();
                while ( iter.hasNext() )
                {
                    JointHypo h = (JointHypo)iter.next();
                    if ( jh.in(h) )
                        iter.remove();
                }
                blocked_joint.add(jh);
            }
        }
    }
    
    private void buildDateTimeHypos(ArrayList<DateTimeHypo> hypos, String type, String[] subslots)
    {
        //double nullProb = 1;
        hypos.clear();
        
        SlotHypo[] subHypos = new SlotHypo[subslots.length];
  
         
        for ( SlotHypo sh : slot_hypos )
        {
            if ( !sh.name.startsWith(type) ) continue;
            for ( int i = 0; i < subslots.length; ++i )
                if ( sh.name.equals(type+"."+subslots[i]) )
                    subHypos[i] = sh;
        }
        
        DateTimeHypo nh = new DateTimeHypo(type);
        nh.belief_prob = 1;
        hypos.add(nh);
        
        int bound = 1;
        for ( SlotHypo sh : subHypos )
        {
            if ( sh == null || sh.value_hypos.isEmpty() )
                continue;
            
            for ( ValueHypo vh : sh.value_hypos )
            {
                for ( int i = 0; i < bound; ++i )
                {
                    DateTimeHypo th = new DateTimeHypo(hypos.get(i));
                    th.addSVP(new SVP(sh.name,vh.name));
                    th.belief_prob = hypos.get(i).belief_prob * vh.belief_prob;
                    if ( th.belief_prob > 0.0001 )
                        hypos.add(th);
                }
            }
            for ( int i = 0; i < bound; ++i )
                hypos.get(i).belief_prob *= sh.nullProb;
            
            prune(hypos);
            
            bound = hypos.size();
        }
        
        Iterator iter = hypos.iterator();
        if ( iter.hasNext() ) iter.next(); //skip the first
        while ( iter.hasNext() )
        {
            DateTimeHypo h = (DateTimeHypo)iter.next();
            
            /*
            boolean observed = false;
            for ( DateTimeHypo ht : observed_date_time_history )
            {
                if ( ht.equals(h) )
                {
                    observed = true;
                    break;
                }
            }
            if ( !observed )
             * */
            if ( !h.isValid() )
                iter.remove();
        }
    }
    
    private void buildJointHypos()
    {
        joint_hypos.clear();
        
        JointHypo nh = new JointHypo();
        nh.belief_prob = 1;
        joint_hypos.add(nh);
        
        
        for ( SlotHypo sh : slot_hypos )
        {
            if ( sh.name.equals("route") || sh.name.startsWith("from") || sh.name.startsWith("to") )
                buildOneField(sh);
                
        }
        
        
        if ( time_hypos != null && time_hypos.size() > 1 )
            buildDateTimeField(time_hypos);
        
        if ( date_hypos != null && date_hypos.size() > 1 )
            buildDateTimeField(date_hypos); 
        
    }
    
    private void buildOneField(SlotHypo sh)
    {
        int bound = joint_hypos.size();
        double nullProb = 1;
        
        
 //       for ( SlotHypo sh : subHypos )
 //       {
            if ( sh == null || sh.value_hypos.isEmpty() )
                return;
            
            for ( ValueHypo vh : sh.value_hypos )
            {
                for ( int i = 0; i < bound; ++i )
                {
                    JointHypo th = new JointHypo(joint_hypos.get(i));
                    th.addSVP(new SVP(sh.name,vh.name));
                    th.belief_prob = joint_hypos.get(i).belief_prob * vh.belief_prob;
                    if ( th.belief_prob > 0.0001 )
                    {
                        boolean blocked = false;
                        for ( JointHypo bh : blocked_joint )
                            if ( bh.in(th) )
                            {
                                blocked = true;
                                break;
                            }
                        if ( !blocked )
                            joint_hypos.add(th);
                    }
                }
            }
            nullProb *= sh.nullProb;
 //       }
        for ( int i = 0; i < bound; ++i )
            joint_hypos.get(i).belief_prob *= nullProb;
        
        prune(joint_hypos);
    }
    
    private void buildDateTimeField(ArrayList<DateTimeHypo> subHypos)
    {
        int bound = joint_hypos.size();
        
        for ( DateTimeHypo sh : subHypos )
        {
            if ( sh.isEmpty() ) continue;
            
            for ( int i = 0; i < bound; ++i )
            {
                if ( joint_hypos.get(i).isEmpty() )
                    continue;
                
                JointHypo th = new JointHypo(joint_hypos.get(i));
                th.addDateTimeHypo(sh);
                th.belief_prob = joint_hypos.get(i).belief_prob * sh.belief_prob;
                if ( th.belief_prob > 0.0001 )
                {
                    boolean blocked = false;
                    for ( JointHypo bh : blocked_joint )
                        if ( bh.in(th) )
                        {
                            blocked = true;
                            break;
                        }
                    if ( !blocked )
                        joint_hypos.add(th);
                }
            }
        }
        
        for ( int i = 0; i < bound; ++i )
            joint_hypos.get(i).belief_prob *= subHypos.get(0).belief_prob;
        
        prune(joint_hypos);
    }
    
    private void prune(ArrayList hypos)
    {
        Iterator iter = hypos.iterator();
        if ( iter.hasNext() ) iter.next(); //skip the first
        while ( iter.hasNext() )
        {
            Object h = iter.next();
            if  ( h.getClass().isInstance(JointHypo.class) )
            {
                if ( ((JointHypo)h).belief_prob < 0.0001 )
                    iter.remove();
            }
            else if ( h.getClass().isInstance(DateTimeHypo.class) )
            {
                if ( ((DateTimeHypo)h).belief_prob < 0.0001 )
                    iter.remove();
            }
        }
    }
    
    public void updateObsDateTimeHistory(ArrayList<SVP> svs)
    {
        DateTimeHypo ht = new DateTimeHypo("time");
        DateTimeHypo hd = new DateTimeHypo("date");
        
        for ( SVP sv : svs )
        {
            ht.addSVP(sv);
            hd.addSVP(sv);
        }
        
        if ( !ht.isEmpty() )
            observed_date_time_history.add(ht);
        if ( !hd.isEmpty() )
            observed_date_time_history.add(hd);
    }
    
    
    public void init()
    {
        slot_hypos.clear();
        blocked_single.clear();
        blocked_joint.clear();
        time_hypos.clear();
        date_hypos.clear();
        joint_hypos.clear();
        observed_date_time_history.clear();
    }
    
    private int find(String slot, int start, int end, boolean addnew)
    {
        if ( end < start )
        {
            //if ( end < 0 ) end = 0;
            if ( addnew )
            {
                SlotHypo sh = new SlotHypo();
                sh.name = slot;
                slot_hypos.add(start, sh);
                return start;
            }
            else
                return -1;
        }
        else
        {

          int imid = (int)((start+end)/2);

          int cmp = slot_hypos.get(imid).name.compareTo(slot);

          if ( cmp >0 )
            return find(slot, start, imid-1, addnew);
          else if (cmp < 0)
            return find(slot, imid+1, end, addnew);
          else
            return imid;
        }
    }
    
    //for debug only
    public void print()
    {
        for ( SlotHypo sh : slot_hypos )
            sh.print();
    }
    
    public void printJSON(BufferedWriter bw)
    {
        try {
            
            bw.write("\t\t\t\t{\n");
            
            printSlotJSON(bw,"route");
            printSlotJSON(bw,"from.desc");
            printSlotJSON(bw,"from.neighborhood");
            printSlotJSON(bw,"from.monument");
            printSlotJSON(bw,"to.desc");
            printSlotJSON(bw,"to.neighborhood");
            printSlotJSON(bw,"to.monument");
            printSlotJSON(bw,"date");
            printSlotJSON(bw,"time");
            
            printjointJSON(bw);
            
            bw.write("\t\t\t\t}");
            
            
        } catch (Exception e) {
                e.printStackTrace();
        }
    }
    
    private void printSlotJSON(BufferedWriter bw, String slot)
    {
        try {
           
            
            bw.write( "\t\t\t\t\t\""+slot+"\": {\n" );
            bw.write( "\t\t\t\t\t\t\"hyps\": [" );
            boolean start = true;
            
            if ( slot.equals("time") )
            {
                
                String[] subslots = {"hour","minute","ampm","arriveleave","rel"};
                buildDateTimeHypos(time_hypos, slot, subslots);
                
                for ( DateTimeHypo th : time_hypos )
                {
                    if ( th.isEmpty() ) continue;
                    
                    if ( start )
                    {    
                        bw.write("\n");
                        start = false;
                        bw.write("\t\t\t\t\t\t\t{\n");
                    }
                    else
                        bw.write(",\n\t\t\t\t\t\t\t{\n");
                    
                    bw.write("\t\t\t\t\t\t\t\t\"slots\": {\n");
                    th.printJSON(bw);
                    bw.write("\n\t\t\t\t\t\t\t\t},\n");
                    bw.write("\t\t\t\t\t\t\t\t\"score\": " + th.belief_prob + "\n");

                    bw.write("\t\t\t\t\t\t\t}");
                }
                
            }
            else if ( slot.equals("date") )
            {
                
                String[] subslots = {"day","absmonth","absday","relweek"};
                buildDateTimeHypos(date_hypos, slot, subslots);
                
                for ( DateTimeHypo th : date_hypos )
                {
                    if ( th.isEmpty() || th.belief_prob < 0.0001 ) continue;
                    
                    if ( start )
                    {    
                        bw.write("\n");
                        start = false;
                        bw.write("\t\t\t\t\t\t\t{\n");
                    }
                    else
                        bw.write(",\n\t\t\t\t\t\t\t{\n");
                    
                    bw.write("\t\t\t\t\t\t\t\t\"slots\": {\n");
                    th.printJSON(bw);
                    bw.write("\n\t\t\t\t\t\t\t\t},\n");
                    bw.write("\t\t\t\t\t\t\t\t\"score\": " + th.belief_prob + "\n");

                    bw.write("\t\t\t\t\t\t\t}");
                }
            }
            else if ( slot.equals("joint") )
            {
                buildJointHypos();
                
                for ( JointHypo jh : joint_hypos )
                {
                    if ( jh.isEmpty() || jh.belief_prob < 0.0001 ) continue;
                    
                    if ( start )
                    {    
                        bw.write("\n");
                        start = false;
                        bw.write("\t\t\t\t\t\t\t{\n");
                    }
                    else
                        bw.write(",\n\t\t\t\t\t\t\t{\n");
                    
                    bw.write("\t\t\t\t\t\t\t\t\"slots\": {\n");
                    jh.printJSON(bw);
                    bw.write("\n\t\t\t\t\t\t\t\t},\n");
                    bw.write("\t\t\t\t\t\t\t\t\"score\": " + jh.belief_prob + "\n");

                    bw.write("\t\t\t\t\t\t\t}");
                }
                
            }
            else
            {
                
                for ( SlotHypo sh : slot_hypos )
                {
                    if ( sh.name.startsWith(slot) )
                    {
                        for ( ValueHypo vh : sh.value_hypos )
                        {
                            if ( start )
                            {    
                                bw.write("\n");
                                start = false;
                                bw.write("\t\t\t\t\t\t\t{\n");
                            }
                            else
                                bw.write(",\n\t\t\t\t\t\t\t{\n");

                            bw.write("\t\t\t\t\t\t\t\t\"slots\": {\n");
                            bw.write("\t\t\t\t\t\t\t\t\t\""+sh.name+"\": \""+vh.name+"\"\n");
                            bw.write("\t\t\t\t\t\t\t\t},\n");
                            bw.write("\t\t\t\t\t\t\t\t\"score\": " + vh.belief_prob + "\n");


                            bw.write("\t\t\t\t\t\t\t}");
                        }

                    }
                }
            }
            
            if (start)
                bw.write("]\n");
            else
                bw.write( "\n\t\t\t\t\t\t]\n" );
           
            
            if ( slot.equals("joint") )
                bw.write( "\t\t\t\t\t}\n" );
            else
                bw.write( "\t\t\t\t\t},\n" );
            
            
        } catch (Exception e) {
                e.printStackTrace();
        }
    }
    
    private void printjointJSON(BufferedWriter bw)
    {
        printSlotJSON(bw,"joint");
    }

///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
////// Methods used by Query Engine

    public void ShowHypo(){
        for ( SlotHypo sh : slot_hypos ){
            System.out.print(sh.name + " {");
            for ( ValueHypo vh : sh.value_hypos ){
                System.out.print(vh.name + " ");
            }
            System.out.print( "} ");
        }
        System.out.println();
    }

    public ArrayList<SlotHypo> getHypo(){
        return slot_hypos;
    }

}

import ontology.COntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class ontoQuery {
    private COntology oOnto = null;


    public ontoQuery(String ontoName) throws OWLOntologyCreationException {
        this.oOnto = new COntology();
        this.oOnto.LoadOwlOnto(ontoName);
    }
    public ontoQuery(String ontoName, boolean bDebug) throws OWLOntologyCreationException {
        this.oOnto = new COntology();
        this.oOnto.LoadOwlOnto(ontoName, bDebug);
    }

    public void SetQueryEngine(){
        this.oOnto.setReasoner();
        this.oOnto.setQuery();
    }
    public Set<String> querySuperClasses(String sQuery){
        return this.oOnto.getSuperClassesString(sQuery);
    }
    public Set<String> queryEquivalentClasses(String sQuery){
        return this.oOnto.getEquivalentClassesString(sQuery);
    }
    public Set<String> querySubClasses(String sQuery){
        return this.oOnto.getSubClassesString(sQuery);
    }
    public Set<String> queryIndividuals(String sQuery){
        return this.oOnto.getIndividualsString(sQuery);
    }

    public String queryGeneration(){
        String sQuery = "";
        return sQuery;
    }

    public void State(ArrayList<BeliefStruct.SlotHypo> slots){
        String sQuery = "";
        for ( BeliefStruct.SlotHypo sh : slots ){
            System.out.print(sh.name + " {");
            for ( BeliefStruct.ValueHypo vh : sh.value_hypos ){
//                if ( sh.name.matches("route") ){
//                    sQuery = sQuery.concat( "route_has_number value \"" );
//                }
//                sQuery = sQuery.concat( vh.name + "\"" );
                System.out.print(vh.name + " ");
            }
            System.out.print( "} ");
        }
        System.out.println();
    }

    public void StateJ(ArrayList<BeliefStruct.JointHypo> jointHypo) {
        for ( BeliefStruct.JointHypo jh : jointHypo ){
            System.out.println("<Name> {");
            boolean ou=checkJointHypo(jh);
            System.out.println(ou);
            System.out.println("}\n");

            if(ou){
                System.out.println("<Name> {");
                for ( Map.Entry<String,String> sv : jh.content.entrySet()) {
                    System.out.print(sv.getKey() + ": " + sv.getValue()+"\n");
                }
                System.out.println("}\n");
            /*    try {
                    System.in.read();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }
        }
    }


    public boolean checkJointHypo(BeliefStruct.JointHypo jh){    //can Nisansa finish a function like this by checking the JointHypo in Ontology

       boolean retVal=false;
       /* for ( Map.Entry<String,String> sv : jh.content.entrySet()) {
            System.out.print(sv.getKey() + ": " + sv.getValue()+"\n");
        }*/

        String neighborhoodLeading="inverse neighborhood_has_bus_stops value ";

        String toDesStr=jh.content.get("to.desc");
       // toDesStr="squirrel hill";
        String fromDesStr=jh.content.get("from.desc");
       // fromDesStr="east pittsburgh";
        String routeS= jh.content.get("route");

        if(routeS==null){ //If this is null, all we can do is check whether the 'to' and 'from' destinations really exist.



            if(toDesStr!=null) {
                toDesStr = toDesStr.toUpperCase();
                toDesStr = toDesStr.replace(' ', '_');


                try {
                    Set<String> s = queryIndividuals(neighborhoodLeading + toDesStr);
                    if (!s.isEmpty()) {
                        retVal = true;
                    }
                }
                catch(Exception e){
                    return false; //To is set to invalid val.
                }

            }



            if(fromDesStr!=null) {
                fromDesStr = fromDesStr.toUpperCase();
                fromDesStr = fromDesStr.replace(' ', '_');

                try {
                    Set<String> s = queryIndividuals(neighborhoodLeading + fromDesStr);
                    if (!s.isEmpty()) {
                        retVal = true;
                    }
                }
                catch(Exception e){
                    return false; //From is set to invalid val.
                }
            }



        }
        else{ //Route has been set!
            String routeQuery="inverse has_bus_stop some (route_has_number value \""+routeS+"\")";
            String toHalf=null;
            String fromHalf=null;

            if(toDesStr!=null) {
                toHalf="("+routeQuery+") and ("+neighborhoodLeading + toDesStr+")";
            }
            if(fromDesStr!=null) {
                fromHalf = "(" + routeQuery + ") and (" + neighborhoodLeading + fromDesStr + ")";
            }


            if(toHalf!=null && fromHalf!=null) {
                try {
                    Set<String> s = queryIndividuals("( " + toHalf + ") and (" + fromHalf + " )");
                    if (!s.isEmpty()) {
                        return true;
                    }
                } catch (Exception e) {
                    return false; //Invalid route
                }
            }
            else if(toHalf!=null) {
                try {
                    Set<String> s = queryIndividuals("( " + toHalf + ")");
                    if (!s.isEmpty()) {
                        return true;
                    }
                } catch (Exception e) {
                    return false; //To is set to invalid val.
                }
            }
            else if(fromHalf!=null) {
                try {
                    Set<String> s = queryIndividuals("( " + fromHalf + ")");
                    if (!s.isEmpty()) {
                        return true;
                    }
                } catch (Exception e) {
                    return false; //From is set to invalid val.
                }
            }
            else{
                return false;
            }

        }




        //toDesStr="DOWNTOWN";





        //doQuery("to.desc","squirrel hill");



        /*
        if (jh is valid in Ontology)
            return true;
        else   */
        return retVal;

    }

   /* private boolean doQuery(String key,String val){

    }*/

    public static void main(String[] args) throws OWLOntologyCreationException {
        ontoQuery ontoQ = new ontoQuery( args[0], true );
        ontoQ.SetQueryEngine();
    }


}

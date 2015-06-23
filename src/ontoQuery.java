import ontology.COntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.util.ArrayList;
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




    public static void main(String[] args) throws OWLOntologyCreationException {
        ontoQuery ontoQ = new ontoQuery( args[0], true );
        ontoQ.SetQueryEngine();
    }
}

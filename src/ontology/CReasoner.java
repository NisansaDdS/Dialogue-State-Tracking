package ontology;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import com.clarkparsia.owlapi.explanation.BlackBoxExplanation;
import com.clarkparsia.owlapi.explanation.HSTExplanationGenerator;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

public class CReasoner {
	private OWLOntology oOwl = null;
	private OWLReasoner oReasoner = null;
	private ReasonerFactory rFactory = null;
	private ShortFormProvider sFormat = null;
	private BidirectionalShortFormProviderAdapter bAdapter = null;
	private OWLDataFactory oFactory = null;
	
	public CReasoner(OWLOntology owl){
		this.oOwl = owl;
		Configuration configuration = new Configuration();
		configuration.throwInconsistentOntologyException = false;
		this.rFactory = new Reasoner.ReasonerFactory() {
            protected OWLReasoner createHermiTOWLReasoner(org.semanticweb.HermiT.Configuration configuration,OWLOntology ontology) {
                configuration.throwInconsistentOntologyException=false;
                return new Reasoner(configuration,ontology);
            }
        };
        this.oReasoner = this.rFactory.createReasoner(oOwl, configuration);
	}
	
	public void removeReasoner(){
		this.oReasoner.dispose();
		this.oReasoner = null;
		this.oOwl = null;
		this.rFactory = null;
	}
	
	public boolean isEntailed(OWLAxiom axiom){
		return this.oReasoner.isEntailed(axiom);
	}
	public boolean isConsistency(){
		return this.oReasoner.isConsistent();
	}
	public boolean isSatisfiable( OWLClassExpression axiom ){
		return this.oReasoner.isSatisfiable(axiom);
	}
	public boolean isSatisfiable(){
		for (OWLClass cls : this.oOwl.getClassesInSignature()) {
			if( ! this.isSatisfiable(cls) ) 
             return false;
        }
		return true;
	}

	public Set<Set<OWLAxiom>> findExplanations(OWLClass uns){
    	this.oReasoner.isSatisfiable(uns);
    	BlackBoxExplanation exp = new BlackBoxExplanation(this.oOwl, this.rFactory, this.oReasoner);
        HSTExplanationGenerator multExplanator  = new HSTExplanationGenerator(exp);	
        return multExplanator.getExplanations( uns );
	}
	public Set<OWLClass> findUnsatisfiable(){
		return this.oReasoner.getUnsatisfiableClasses().getEntities();
	}
    public Set<Set<OWLAxiom>> findExplanations(){
    	Set<Set<OWLAxiom>> oAxioms = new HashSet<Set<OWLAxiom>>();
    	for( OWLClass unsat : this.oReasoner.getUnsatisfiableClasses().getEntities() ){
    		oAxioms.addAll(this.findExplanations(unsat));
    	}
    	return oAxioms;
    }

	public void SetQueryEngine(){
		OWLOntologyManager oManager = this.oReasoner.getRootOntology().getOWLOntologyManager();
		Set<OWLOntology> sClosure = this.oReasoner.getRootOntology().getImportsClosure();

		this.sFormat = new SimpleShortFormProvider();
		this.bAdapter = new BidirectionalShortFormProviderAdapter(oManager, sClosure, this.sFormat);
		this.oFactory = oManager.getOWLDataFactory();
	}
	private OWLClassExpression parseClassExpression(String sExpression) {
		ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser( this.oFactory, sExpression);
		parser.setDefaultOntology(this.oReasoner.getRootOntology() );
		OWLEntityChecker entityChecker = new ShortFormEntityChecker( this.bAdapter );
		parser.setOWLEntityChecker(entityChecker);
		return parser.parseClassExpression();
	}
	public Set<OWLClass> getSuperClasses(String classQuery, boolean bDirect) {
		if (classQuery.trim().length() == 0) {
			return Collections.emptySet();
		}
		OWLClassExpression classExpression = parseClassExpression(classQuery);
		NodeSet<OWLClass> superClasses = this.oReasoner.getSuperClasses(classExpression, bDirect);
		return superClasses.getFlattened();
	}
	public Set<OWLClass> getEquivalentClasses(String classQuery) {
		if (classQuery.trim().length() == 0) {
			return Collections.emptySet();
		}
		OWLClassExpression classExpression = parseClassExpression(classQuery);
		Node<OWLClass> equivalentClasses = this.oReasoner.getEquivalentClasses(classExpression);
		if (classExpression.isAnonymous())
			return equivalentClasses.getEntities();
		else
			return equivalentClasses.getEntitiesMinus(classExpression.asOWLClass());
	}
	public Set<OWLClass> getSubClasses(String classQuery, boolean bDirect) {
		if (classQuery.trim().length() == 0) {
			return Collections.emptySet();
		}
		OWLClassExpression classExpression = parseClassExpression(classQuery);
		NodeSet<OWLClass> subClasses = this.oReasoner.getSubClasses(classExpression, bDirect);
		return subClasses.getFlattened();
	}
	public Set<OWLNamedIndividual> getInstances(String classQuery, boolean bDirect) {
		if (classQuery.trim().length() == 0) {
			return Collections.emptySet();
		}
		OWLClassExpression classExpression = parseClassExpression(classQuery);
		NodeSet<OWLNamedIndividual> individuals = this.oReasoner.getInstances(classExpression, bDirect);
		return individuals.getFlattened();
	}
	public Set<String> EntityToString(Set<? extends OWLEntity> entities){
		Set<String> sEntities = new HashSet<String>();
		for (OWLEntity oEntity: entities)
			sEntities.add( this.sFormat.getShortForm(oEntity));
		return sEntities;
	}



    public static void main(String[] args) throws OWLOntologyCreationException{
    	OWLOntologyManager oManager = OWLManager.createOWLOntologyManager();
    	OWLDataFactory oFactory = oManager.getOWLDataFactory();
    	File fLocation = new File(args[0]);
    	OWLOntology oOwl = oManager.loadOntologyFromOntologyDocument(fLocation);
    	IRI iOwl = oOwl.getOntologyID().getOntologyIRI();
    	PrefixManager pManager = new DefaultPrefixManager( iOwl.toString() );
    	
    	CReasoner oReasoner = new CReasoner(oOwl);
    	System.out.println( oReasoner.isConsistency() );
    	System.out.println( oReasoner.isSatisfiable() );
    	;
    }
    
}

package ontology;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import java.io.File;
import java.util.Set;


public class COntology {
	private OWLOntologyManager oManager = null;
	private OWLDataFactory oFactory = null;
	private PrefixManager pManager = null;
	private OWLOntology oOwl = null;
	private IRI iOwl = null;
	private File fLocation = null;
	private CReasoner oReasoner = null;

	public COntology() {
		this.oManager = OWLManager.createOWLOntologyManager();
		this.oFactory = oManager.getOWLDataFactory();
	}

	public boolean LoadOwlOnto(String sFile, boolean bDebug) throws OWLOntologyCreationException {
		this.fLocation = new File(sFile);
		if (this.fLocation.exists()) {
			this.oOwl = oManager.loadOntologyFromOntologyDocument(fLocation);
			this.iOwl = this.oOwl.getOntologyID().getOntologyIRI();
			this.pManager = new DefaultPrefixManager(this.iOwl.toString());
			if (bDebug) {
				System.out.println("Loaded ontology: " + oOwl.getOntologyID().getOntologyIRI());
				System.out.println(" from: " + sFile);
			}
			return true;
		} else {
			System.err.println("No file found: " + sFile );
			return false;
		}
	}
	public boolean LoadOwlOnto(String sFile) throws OWLOntologyCreationException {
		return this.LoadOwlOnto(sFile, false);
	}
	public boolean ReLoadOnto() throws Exception {
		if (this.fLocation.exists()) {
			this.oOwl = oManager.loadOntologyFromOntologyDocument(this.fLocation);
			this.iOwl = this.oOwl.getOntologyID().getOntologyIRI();
			this.pManager = new DefaultPrefixManager(this.iOwl.toString());
			return true;
		} else {
			System.out.print("No file found");
			return false;
		}
	}
	public void removeOntology() {
		if (this.oReasoner != null)
			this.removeReasoner();

		this.oFactory.purge();
		this.oManager.removeOntology(this.oOwl);
		this.oOwl = null;
	}
	public boolean isEmpty() {
		if (this.oManager == null || this.oFactory == null || this.oOwl == null)
			return true;
		else
			return false;
	}

	public void addClass(String sClass) {
		OWLClass oClass = this.oFactory.getOWLClass("#" + sClass, this.pManager);
		OWLAxiom axiom = this.oFactory.getOWLDeclarationAxiom(oClass);
		this.oManager.addAxiom(this.oOwl, axiom);
	}
	public void addClassInstance(String sClass, String sInstance) {
		OWLClass oClass = this.oFactory.getOWLClass("#" + sClass, this.pManager);
		OWLNamedIndividual oInstance = this.oFactory.getOWLNamedIndividual("#" + sInstance, this.pManager);
		OWLClassAssertionAxiom oAssertion = this.oFactory.getOWLClassAssertionAxiom(oClass, oInstance);
		this.oManager.addAxiom(this.oOwl, oAssertion);
	}
	public void addSubClass(String sParent, String sChild) {
		OWLClassExpression ocParent = this.oFactory.getOWLClass("#" + sParent, pManager);
		OWLClassExpression ocChild = this.oFactory.getOWLClass("#" + sChild, pManager);
		OWLAxiom axiom = this.oFactory.getOWLSubClassOfAxiom(ocChild, ocParent);
		this.oManager.addAxiom(this.oOwl, axiom);
	}
	public void addPropertyAssertion(String sProperty, String sDomain, String sRange) {
		OWLNamedIndividual oiDomain = this.oFactory.getOWLNamedIndividual("#" + sDomain, pManager);
		OWLNamedIndividual oiRange = this.oFactory.getOWLNamedIndividual("#" + sRange, pManager);
		OWLObjectProperty oiProperty = this.oFactory.getOWLObjectProperty("#" + sProperty, pManager);
		OWLObjectPropertyAssertionAxiom propertyAssertion = this.oFactory.getOWLObjectPropertyAssertionAxiom(oiProperty, oiDomain, oiRange);
		this.oManager.addAxiom(this.oOwl, propertyAssertion);
	}
	public void addProperty(String sProperty, String sDomain, String sRange) {
		OWLClassExpression ocDomain = this.oFactory.getOWLClass("#" + sDomain, pManager);
		OWLClassExpression ocRange = this.oFactory.getOWLClass("#" + sRange, pManager);
		OWLObjectProperty opProperty = this.oFactory.getOWLObjectProperty("#" + sProperty, pManager);
		this.oManager.addAxiom(this.oOwl, this.oFactory.getOWLObjectPropertyDomainAxiom(opProperty, ocDomain));
		this.oManager.addAxiom(this.oOwl, this.oFactory.getOWLObjectPropertyRangeAxiom(opProperty, ocRange));
	}

	public void setReasoner() {
		this.oReasoner = new CReasoner(this.oOwl);
	}
	public void removeReasoner() {
		this.oReasoner.removeReasoner();
		this.oReasoner = null;
	}

	public boolean setConsistency() {
		return this.oReasoner.isConsistency();
	}
	public boolean setSatisfiability() {
		return this.oReasoner.isSatisfiable();
	}
	public boolean setEntailment(String sProperty, String sDomain, String sRange) {
		if (sProperty.equals("can be")) {
			OWLClassExpression ocParent = this.oFactory.getOWLClass("#" + sDomain, pManager);
			OWLClassExpression ocChild = this.oFactory.getOWLClass("#" + sRange, pManager);
			return this.oReasoner.isEntailed(this.oFactory.getOWLSubClassOfAxiom(ocChild, ocParent));
		} else if (sProperty.equals("is")) {
			OWLClassExpression ocParent = this.oFactory.getOWLClass("#" + sRange, pManager);
			OWLClassExpression ocChild = this.oFactory.getOWLClass("#" + sDomain, pManager);
			return this.oReasoner.isEntailed(this.oFactory.getOWLSubClassOfAxiom(ocChild, ocParent));
		} else {
			OWLClassExpression ocDomain = this.oFactory.getOWLClass("#" + sDomain, pManager);
			OWLClassExpression ocRange = this.oFactory.getOWLClass("#" + sRange, pManager);
			OWLObjectProperty opProperty = this.oFactory.getOWLObjectProperty("#" + sProperty, pManager);
			return
					this.oReasoner.isEntailed(this.oFactory.getOWLObjectPropertyDomainAxiom(opProperty, ocDomain))
							&& this.oReasoner.isEntailed(this.oFactory.getOWLObjectPropertyRangeAxiom(opProperty, ocRange));
		}

	}
	public void getExplanation() {
		Set<Set<OWLAxiom>> explanations = this.oReasoner.findExplanations();
		for (Set<OWLAxiom> explanation : explanations) {
			System.out.println("------------------\nAxioms causing the unsatisfiability: ");
			for (OWLAxiom causingAxiom : explanation) {
				System.out.println(causingAxiom);
			}
			System.out.println("------------------");
		}
	}

	public void setQuery(){
		this.oReasoner.SetQueryEngine();
	}
	public Set<OWLClass> getSuperClassesEntities(String sQuery){
		return this.oReasoner.getSuperClasses(sQuery, true);
	}
	public Set<String> getSuperClassesString(String sQuery){
		return this.oReasoner.EntityToString( this.oReasoner.getSuperClasses(sQuery, true) );
	}
	public Set<OWLClass> getEquivalentClassesEntities(String sQuery){
		return this.oReasoner.getEquivalentClasses(sQuery);
	}
	public Set<String> getEquivalentClassesString(String sQuery){
		return this.oReasoner.EntityToString( this.oReasoner.getEquivalentClasses(sQuery) );
	}
	public Set<OWLClass> getSubClassesEntities(String sQuery){
		return this.oReasoner.getSubClasses(sQuery, true);
	}
	public Set<String> getSubClassesString(String sQuery){
		return this.oReasoner.EntityToString( this.oReasoner.getSubClasses(sQuery, true) );
	}
	public Set<OWLNamedIndividual> getIndividualsEntities(String sQuery){
		return this.oReasoner.getInstances(sQuery, true);
	}
	public Set<String> getIndividualsString(String sQuery){
		return this.oReasoner.EntityToString( this.oReasoner.getInstances(sQuery, true) );
	}

	public void ShowClass(boolean bDebug) {
		for (OWLClass cls : this.oOwl.getClassesInSignature()) {
			System.out.println("Class: " + (bDebug ? cls.toString().split("#")[1].replace(">", "") : cls));
		}
	}
	public void ShowInstances(boolean bDebug) {
		for (OWLIndividual ind : this.oOwl.getIndividualsInSignature()) {
			System.out.println("Individuals: " + (bDebug ? ind.toString().split("#")[1].replace(">", "") : ind));
		}
	}
	public void ShowObjectProperties(boolean bDebug) {
		for (OWLObjectProperty pty : this.oOwl.getObjectPropertiesInSignature()) {
			System.out.println("Object Property: " + (bDebug ? pty.toString().split("#")[1].replace(">", "") : pty));
		}
	}
	public void ShowAxioms(boolean bDebug) {
		for (OWLAxiom ax : this.oOwl.getAxioms()) {
			System.out.println("Axiom Property: " + ax);
		}
	}
	public void ShowAxioms(String sElement) {
		for (OWLAxiom ax : this.oOwl.getAxioms()) {
			if (ax.toString().contains(sElement))
				System.out.println("Axiom Property: " + ax);
		}
	}
	public void ShowConsistency() {
		System.out.println("Is consistent?: " + this.setConsistency());
	}
	public void ShowUnsatisfiable() {
		System.out.println("------------------\nAxioms causing the unsatisfiability: ");
		for (OWLClass unsat : this.oReasoner.findUnsatisfiable()) {
			System.out.println(unsat);
		}
		System.out.println("------------------");
	}
	public void ShowQuery( String sQuery ){
		Set<String> superClasses = this.oReasoner.EntityToString( this.oReasoner.getSuperClasses(sQuery, true) );
		Set<String> equivalentClasses = this.oReasoner.EntityToString(this.oReasoner.getEquivalentClasses(sQuery));
		Set<String> subClasses = this.oReasoner.EntityToString(this.oReasoner.getSubClasses(sQuery, true) );
		Set<String> individuals = this.oReasoner.EntityToString(this.oReasoner.getInstances(sQuery, true) );

		System.out.println("SuperClasses: ");
		for(String sElement : superClasses)
			System.out.println(sElement);
		System.out.println("\nEquivalent Classes: ");
		for(String sElement : equivalentClasses)
			System.out.println(sElement);
		System.out.println("\nSubClasses: ");
		for(String sElement : subClasses)
			System.out.println(sElement);
		System.out.println("\nIndividuals: ");
		for(String sElement : individuals)
			System.out.println(sElement);
	}


	public static void main(String[] args) throws OWLOntologyCreationException, Exception {
		COntology onto = new COntology();
		onto.LoadOwlOnto(args[0]);
		onto.setReasoner();

		onto.setQuery();
		onto.ShowQuery( "Street" );

		onto.ShowConsistency();
		onto.getExplanation();
		onto.ShowUnsatisfiable();
	}
}

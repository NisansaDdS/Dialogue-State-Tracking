/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author zw57
 */

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.*;
import java.util.ArrayList;

public class BTC_simple {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws OWLOntologyCreationException {
        
        if ( args.length < 2 )
        {
            System.err.println("run: BTC_simple data_path config_file "+ args.length);
            return;
        }
        
        String jsonPathPrefix = args[0]; //"/Users/zw57/Desktop/btrack/data/";
        String configFile = args[1];// "/Users/zw57/Desktop/btrack/config/train3.sessions";
        
        String sessionFile = configFile.substring(configFile.lastIndexOf("/")+1);
        String outFile = sessionFile.replace("sessions", "hw") +".out.json";
        
        JSONReader fileParser = new JSONReader();
        long startTimeMs = System.currentTimeMillis();

//      Loads ontology and starts the query engine
        String ontoPath = args[2];
        ontoQuery ontoQ = new ontoQuery(ontoPath);
        ontoQ.SetQueryEngine();

//        File file = new File(jsonPath + "/");
//        String[] files = file.list();
        BufferedWriter output = null;
        try {
                // iterate over json files to extract training data
//                for (int i=0; files != null && i<files.length; i++) 
                BufferedReader input =  new BufferedReader(new FileReader(new File(configFile)));
                output = new BufferedWriter(new FileWriter(new File(outFile)));
                
                output.write("{\n\t\"wall-time\": 0,\n\t\"dataset\": \""+sessionFile.substring(0, sessionFile.lastIndexOf(".sessions")) +"\", \n");
                output.write("\t\"sessions\": [\n");
                
                String line = null; 
                
                boolean start = true;
       
                while (( line = input.readLine()) != null)
                {
                        String[] toks = line.split("/");
                        String sessionName = toks[1];
                        //if (sessionName.startsWith(".")) continue;
                        //String labelsFile = jsonPath + "/" + sessionName +"/dstc.labels.json";
                        String logFile = jsonPathPrefix + "/" + line +"/dstc.log.json";
                        //System.out.println("jsonFile=" + jsonFile);
                        
//                        JSONReader fileParser = new JSONReader(labelsFile, logFile);
                        ArrayList<TurnObject> turns = new ArrayList();
                                
                        String sessionID  = fileParser.processDataFiles(logFile,turns);
                        
                        if ( sessionID == null )
                            System.err.println("Error parsing " + sessionName);
                        
                        if ( start )
                        {
                            output.write("\t\t{\n");
                            start = false;
                        }
                        else
                            output.write(",\n\t\t{\n");
                        
                        output.write("\t\t\t\"session-id\": \""+sessionID+"\",\n");
                        output.write("\t\t\t\"turns\": [\n");
                        
                        BeliefTracker bm = new BeliefTracker();
                        //System.out.println("==========="+sessionName+"===========");
                        System.out.println(sessionID);
                        
                        int turnid = 0;
                        System.out.println(" Turn");
                        for ( TurnObject t : turns )
                        {
                            if ( turnid > 0 )
                                output.write(",\n");
                            //System.out.println("-----------Dialog Turn------------");
                            //System.out.println(t.toString());

                            //System.out.println("Here we use Query the ontology");
                            //bm.ShowHypo();
                            //ontoQ.State( bm.getHypo() );

                            //bm.ShowJointHypo();
                            System.out.println(turnid);
                            
                            
                            // Add by Miao start
                            /*
                            // check BeliefState
                            for ( BeliefStruct.JointHypo jh : bm.getJointHypo() ){
                                boolean ou=ontoQ.checkJointHypo(jh);
                                if (ou){
                                	//generate SVP list
                                	ArrayList<SVP> b_SVPs = jh.GetSVPs();
                                	System.out.println("find unvalid joint hypo!");
                                	System.out.println(b_SVPs);
                                	if(b_SVPs.isEmpty())
                                		;
                                	else if(b_SVPs.size()==1){
                                		bm.AddpendingBlockRules(b_SVPs.get(0));
                                	}
                                	else {
										bm.AddpendingJointBlockRules(b_SVPs);
									}
                                }
                            }
                                
                            //ontoQ.StateJ( bm.getJointHypo() );
                            */
                            // test JointHypos
                            for ( BeliefStruct.JointHypo jh : bm.getJointHypo() ){
                                boolean ou;

                                //To, from and route -> True Positive
                                BeliefStruct.JointHypo jh1 = jh.createDummyJointHypo("Neighborhood_2", "Neighborhood_1", "Route_1");
                                ou=ontoQ.checkJointHypo(jh1);
                                System.out.println(ou);

                                //To only -> True Positive
                                BeliefStruct.JointHypo jh2 = jh.createDummyJointHypo("Neighborhood_2", "", "");
                                ou=ontoQ.checkJointHypo(jh2);
                                System.out.println(ou);

                                //From only -> True Positive
                                BeliefStruct.JointHypo jh3 = jh.createDummyJointHypo("", "Neighborhood_1", "");
                                ou=ontoQ.checkJointHypo(jh3);
                                System.out.println(ou);

                                //To and From only -> True Positive
                                BeliefStruct.JointHypo jh4 = jh.createDummyJointHypo("Neighborhood_2", "Neighborhood_1", "");
                                ou=ontoQ.checkJointHypo(jh4);
                                System.out.println(ou);

                                //To, from and route -> True Negative
                                BeliefStruct.JointHypo jh5 = jh.createDummyJointHypo("Neighborhood_2", "Neighborhood_1", "Route_2");
                                ou=ontoQ.checkJointHypo(jh5);
                                System.out.println(ou);

                                //To only -> True Negative
                                BeliefStruct.JointHypo jh6 = jh.createDummyJointHypo("Neighborhood_3", "", "");
                                ou=ontoQ.checkJointHypo(jh6);
                                System.out.println(ou);

                                //From only -> True Negative
                                BeliefStruct.JointHypo jh7 = jh.createDummyJointHypo("", "Neighborhood_4", "");
                                ou=ontoQ.checkJointHypo(jh7);
                                System.out.println(ou);

                                //To and From only -> True Negative (Case 1)
                                BeliefStruct.JointHypo jh8 = jh.createDummyJointHypo("Neighborhood_3", "Neighborhood_1", "");
                                ou=ontoQ.checkJointHypo(jh8);
                                System.out.println(ou);

                                //To and From only -> True Negative (Case 2)
                                BeliefStruct.JointHypo jh9 = jh.createDummyJointHypo("Neighborhood_2", "Neighborhood_4", "");
                                ou=ontoQ.checkJointHypo(jh9);
                                System.out.println(ou);
                            }
                            
                            // Add by Miao end.
							
                            bm.update(t.sysDialogActs, t.usrSLUHypos, turnid++);
                            bm.printJSON(output);
                            //System.out.println("+++++++++++++Beliefs++++++++++++++");
                            //bm.printBeliefs();
                            //System.in.read();
                        }
                        
                        output.write("\n\t\t\t]\n\t\t}");
                        output.flush();
                }
                output.write("\n\t]\n}");
                
        } catch (Exception e) {
                e.printStackTrace();
        } finally {
            if ( output != null )
            {   
                try {
                    output.flush();
                    output.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        long taskTimeMs  = System.currentTimeMillis( ) - startTimeMs;
        
        System.out.println("Wall-time:" + taskTimeMs/1000.0);
        
    }
    
    
}

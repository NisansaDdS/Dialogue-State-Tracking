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
                        System.out.println(" Trun");
                        for ( TurnObject t : turns )
                        {
                            if ( turnid > 0 )
                                output.write(",\n");
                            //System.out.println("-----------Dialog Turn------------");
                            //System.out.println(t.toString());

                            System.out.println("Here we use Query the ontology");
                            //bm.ShowHypo();
                            //ontoQ.State( bm.getHypo() );

                            //bm.ShowJointHypo();
                            ontoQ.StateJ( bm.getJointHypo() );

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

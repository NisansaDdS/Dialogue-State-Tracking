// Copyright (C) 2012-2013 Heriberto Cuay�huitl (h.cuayahuitl@gmail.com)
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation; either version 2.1 of
// the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNUf
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
// 02111-1307, USA.
// =================================================================

//package btc_simple;
import java.lang.Throwable;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;


public class JSONReader {

	/*
	 * description: reads a directory of dialogue log files using the json format
	 * param 0: file path pointing to the data (e.g. data/train3)
	 */
//        public ArrayList<ArrayList<TurnObject> > theData = new ArrayList<ArrayList<TurnObject> >();

	public JSONReader() {

//            ArrayList<TurnObject> arrayTurns = processDataFiles(labelsFile, logFile);


	}

	/*
	 * description: print a dialogue of system-user responses at the word and dialogue-act level
	 * param 0: file name (e.g. dstc.labels.json)
	 * param 1: file name (e.g. dstc.log.json)
	 */
	public String processDataFiles(String logFile, ArrayList<TurnObject> arrayTurns) {
		//HashMap<String,ArrayList<String>> usrInputs = getUserTurns(labelsFile);
		arrayTurns.clear();
		String sessionID = null;
		try {
			String jsonTxt = IOUtils.toString(new FileInputStream(logFile));
			JSONObject jsonObject = JSONObject.fromObject(jsonTxt);
			String lastSysDialAct = "";
			String corrUsrDialAct = "";
			String recoUsrDialAct = "";
			String bargein = "";

			// system+user turns
			sessionID = jsonObject.getString("session-id");
			JSONArray array = jsonObject.getJSONArray("turns");
			for (Object obj : array) {
				JSONObject jsonObj = (JSONObject) obj;
				String turnIndex = jsonObj.getString("turn-index");
				//ArrayList<String> labels = usrInputs.get(turnIndex);

				TurnObject aTurn = new TurnObject();

				// system features
				JSONObject output = jsonObj.getJSONObject("output");
//				String sysStartTime = output.getString("start-time");

				//String sysUtterance = output.getString("transcript");

				//System.err.println("SYS:" + sysStartTime + ":" + sysUtterance);

				//aTurn.sysStartTime = sysStartTime;
				//aTurn.sysUtterance = sysUtterance;
				aTurn.sysDialogActs = new ArrayList<DialogAct>();

				// system dialogue acts
				JSONArray subarray = null;
				try {
					if ( output.has("dialog-acts") )
					{
						subarray = output.getJSONArray("dialog-acts");
						for (Object subobj : subarray) {
							JSONObject jsonSubObj = (JSONObject) subobj;
							String act = jsonSubObj.getString("act");

							// skips dialogue acts "example" (remove if needed)
							if (act.equals("example")) continue;

							String slots = "";
							JSONArray subsubarray = jsonSubObj.getJSONArray("slots");
							for (Object subsubobj : subsubarray) {
								String value = getKeyValuePairFromString(subsubobj.toString());
								slots += (slots.equals("")) ? value : "," + value;
							}
							slots = (act.equals("schedule")) ? "*" : slots;
							lastSysDialAct = act + "(" + slots + ")";

							aTurn.sysDialogActs.add(new DialogAct(act,slots));

							//System.err.println("    " + lastSysDialAct);
						}
					}

				} catch ( JSONException e )
				{
					System.err.println(logFile+"\n"+jsonObj.toString());
					e.printStackTrace();
				}
    /*
				// bargein feature
				JSONObject systemSpecific = jsonObj.getJSONObject("system-specific");
				subarray = systemSpecific.getJSONArray("sys_action_details");
				for (Object subobj : subarray) {
					JSONObject jsonSubObj = (JSONObject) subobj;
					bargein = jsonSubObj.getString("bargein");
					//System.err.println("    Bargein:" + bargein);
					break;
				}
*/
				// user features
				JSONObject input = jsonObj.getJSONObject("input");
				JSONObject asrHyps = input.getJSONObject("live");
				//String usrStartTime = input.getString("start-time");
				//System.err.println("USR:" + usrStartTime + ":" + labels.get(0));

				//aTurn.usrStartTime = usrStartTime;

				aTurn.usrSLUHypos = new ArrayList<DialogAct>();

				// SLU info
				float bestScore = 0;
				try {
					subarray = asrHyps.getJSONArray("slu-hyps");
					for (int j=0; j<subarray.size(); j++) {
						Object subobj = subarray.get(j);
						JSONObject jsonSubObj = (JSONObject) subobj;



						String dialogueAct = "";
						DialogAct usrDialogAct = new DialogAct();

						JSONArray subsubarray = jsonSubObj.getJSONArray("slu-hyp");
						for (Object subsubobj : subsubarray) {
							JSONObject jsonSubSubObj = (JSONObject) subsubobj;
							String usrAct = jsonSubSubObj.getString("act");
							String usrSlots = jsonSubSubObj.getString("slots");
							//String tmpSlots = usrSlots;
							usrSlots = getKeyValuePairFromString(usrSlots);
							dialogueAct = usrAct + "(" + usrSlots + ")";

							usrDialogAct = new DialogAct(usrAct, usrSlots);

							//if ( dialogueAct.contains("time.hour") )
							//    System.err.println("\t\t" + sessionID+" : " +tmpSlots+ " " + dialogueAct );
						}
						String score = jsonSubObj.getString("score");
						//String label = labels.get(j+1);


						usrDialogAct.setScore(Float.parseFloat(score));

						aTurn.usrSLUHypos.add(usrDialogAct);

						//if (label.equals("true")) {
						//	corrUsrDialAct = dialogueAct;
						//}

						if (Float.parseFloat(score) >= bestScore) {
							recoUsrDialAct = dialogueAct;
						}
					}
				} catch ( JSONException e )
				{
					e.printStackTrace();
				}

				// ASR info
				String asrHypotheses = "";
				subarray = asrHyps.getJSONArray("asr-hyps");
				for (int j=0; j<subarray.size(); j++) {
					Object subobj = subarray.get(j);
					JSONObject jsonSubObj = (JSONObject) subobj;

					asrHypotheses = jsonSubObj.getString("asr-hyp");
					//System.err.println("    asr-hyps:" + asrHypotheses);
					break; // note: takes the 1-best only
				}
				//System.err.println();
				arrayTurns.add(aTurn);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return sessionID;
	}

	/**
	 * param 0: string to format as "key=value"
	 */
	public String getKeyValuePairFromString(String input) {
		String pair = input;

		try {
			pair = pair.replace(",", "=");
			pair = pair.replace("[", " ");
			pair = pair.replace("]", " ");
			//pair = StringUtil.getStringWithoutCharacter(pair, "\"");
			pair = pair.replace("\"", "");
			pair = pair.trim();
			pair = (pair.endsWith("=null")) ? pair.substring(0, pair.indexOf("=")) : pair;
			pair = pair.replace(" = ", " , ");

		} catch (Exception e) {
			e.printStackTrace();
		}

		return pair;
	}

	/**
	 * return an ArrayList generated from the given word sequence
	 */
	public static String getStringWithoutCharacter(String wordSequence, String separator) {
		String result = "";

		if (wordSequence != null) {
			StringTokenizer toks = new StringTokenizer(wordSequence, separator);
			while (toks.hasMoreTokens()) {
				result += toks.nextToken();
			}
		}

		return result;
	}

	/**
	 * description: return a HashMap of user inputs with true labels
	 * param 0: file name (e.g. dstc.labels.json)
	 */
	public HashMap<String,ArrayList<String>> getUserTurns(String jsonFile) {
		HashMap<String,ArrayList<String>> turns = new HashMap<String,ArrayList<String>>();

		try {
			String jsonTxt = IOUtils.toString(new FileInputStream(jsonFile));
			JSONObject jsonObject = JSONObject.fromObject(jsonTxt);

			// user turns
			JSONArray array = jsonObject.getJSONArray("turns");
			for (Object obj : array) {
				JSONObject jsonObj = (JSONObject) obj;

				// system features
				String turnIndex = jsonObj.getString("turn-index");
				ArrayList<String> labels = new ArrayList<String>();

				if (turnIndex.equals(""+(array.size()-1))) {
					labels.add("null");

				} else {
					String transcription = "";
					if (jsonObj.toString().indexOf("\"transcription\"") == -1) {
						transcription = "empty";
					} else {
						transcription = jsonObj.getString("transcription");
					}
					JSONObject sluLabels = jsonObj.getJSONObject("slu-labels");
					labels.add(transcription);

					// system dialogue acts
					JSONArray subarray = sluLabels.getJSONArray("live");
					for (Object subobj : subarray) {
						String elems = subobj.toString();
						if (elems.indexOf("\"label\":true")>=0) {
							labels.add("true");
						} else {
							labels.add("false");
						}
					}
				}

				turns.put(turnIndex,labels);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return turns;
	}

	private void showOneFile(ArrayList<TurnObject> dial, String fileName)
	{
		System.out.println("--------Dialogue: "+fileName+"------------\n");
		for ( TurnObject turn :  dial)
		{
			System.out.println(turn.toString());
		}
	}

	/**
	 * description: test method
	 * param 0: path to data (e.g. data/train3)
	 */
	//public static void main(String[] args) {
	//	new JSONReader(args[0]);
	//}
}

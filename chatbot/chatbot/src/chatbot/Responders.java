package chatbot;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.didion.jwnl.data.POS;

import opennlp.tools.parser.Parse;

@SuppressWarnings("unchecked")
public class Responders {

	WordRelations wr = new WordRelations();
	
	private HashMap<String, LinkedList<Entry>> responseTables = buildResponseTables();
	private HashMap<String, Response> defaultResponses = buildDefaultResponses();
	private HashMap<String, LinkedList<Entry>> buildResponseTables() {
		HashMap<String, LinkedList<Entry>> ret = new HashMap<String, LinkedList<Entry>>();
		
		//build response tables..
		ret.put("S", S());
		//ret.put("SBAR", SBAR());
		ret.put("SBARQ", SBARQ());
		//ret.put("SINV", SBARQ());
		ret.put("SQ", SQ());
		
		//Sort each response table so that the largest word pattern is tried first
		Comparator<Entry> comparator = new Comparator<Entry>() {
			@Override
			public int compare(Entry arg0, Entry arg1) {
				if(arg0.wordPattern.length() < arg1.wordPattern.length()) return 1;
				else if(arg0.wordPattern.length() == arg1.wordPattern.length()) return 0;
				else return -1;
			}
			
		};
		
		for(LinkedList<Entry> list : ret.values()) {
			Collections.sort(list, comparator);
		}
		
		return ret;
	}
	
	
	private HashMap<String, Response> buildDefaultResponses() {
		HashMap<String, Response> ret = new HashMap<String, Response>();
		
		//build default responses..
		ret.put("S", defaultS());
		//ret.put("SBAR", defaultSBAR());
		ret.put("SBARQ", defaultSBARQ());
		//ret.put("SINV", defaultSBARQ());
		ret.put("SQ", defaultSQ());
		
		return ret;
	}

	public String response(Parse parse, HashMap<String,Object> context) {
		
		//Get the first non top entity
		Parse[] children = parse.getChildren();
		Parse p = children[0];
		System.out.println("p is " + p.getType());
			
		
		//Put the user sentence into context structure.. it might be useful later
		if(!context.containsKey("userSentences")) {
			context.put("userSentences", new HashMap<String,Integer>());
		}
		Global.incrementKey( ((HashMap<String,Integer>)context.get("userSentences")), p.toString());
		
		
		//Get the response table that corresponds to this "top level" parse
		LinkedList<Entry> tableOfInterest = responseTables.get(p.getType());
		
		//If no table, do a generic response
		if(tableOfInterest == null) {
			return Global.randomChoice("Pardon me?", "Come again?", "What was that?", "I don't understand.", "Hmmm...");
		}
		
		//Scan the table of interest for matches
		//int minScore = Integer.MAX_VALUE;
		//Entry bestMatch = null;
		for(Entry ent : tableOfInterest ) {
	/*	    int tmp = ent.topHyperMatchInSentence(p);
		    if(tmp < minScore) {
		    	bestMatch = ent;
		    	minScore = tmp;
		    }*/
			if(ent.hypernymMatchesSentence(p)) {
				return ent.r.response(p, context);
			}
		}
		/*
		if(bestMatch != null) {
			return bestMatch.r.response(p, context);
		}
		*/
		
		//No hyper match found. Check word matches.
		
		
		//Scan the table of interest for word matches
		for(Entry ent : tableOfInterest ) {
			if(ent.wordMatchesSentence(p)) {
				return ent.r.response(p,context);
			}
		}
		//If no matches, use the default
		Response defaultResponse = defaultResponses.get(p.getType());
		if(defaultResponse == null) {
			System.out.println("Bug! no default response for: " + p.getType());
		}
		else {
			return defaultResponse.response(p, context);
		}
		
		//Shouldn't get here
		return "Bug!";
		
	}
	



	/******************************************************/
	/* Response Actions                                    */
	/******************************************************/
	//Make the default responder for S. This is the "fall through" response that
	//is invoked if no matches are found.
	private static Response defaultS() {
		Response defaultResponse = new Response() {
			@Override
			public String response(Parse p, HashMap<String, Object> context) {
				//Flip possessive statements
			    String sent = Global.flipPossesives(p.toString());
			    
			    //Put this into the statements context.. it might be useful later
			    if(!context.containsKey("statements")) {
			    	context.put("statements", new LinkedList<String>());
			    }
			    ((LinkedList<String>)context.get("statements")).add(sent); 
			    			    
				return Global.randomChoice("So "  + sent + ", huh?",
						"Why does it matter if "  + sent + "?",
						"Let me get this straight, "  + sent + "?",
						"Fascinating.",
						sent + ".... Cool story bro.");
		     }
	    };
	    return defaultResponse;
	}

	
	private static Response defaultSBARQ() {
		Response defaultResponse = new Response() {
			@Override
			public String response(Parse p, HashMap<String, Object> context) {
				//Flip possessive statements
			    String sent = Global.flipPossesives(p.toString());
			    
			    //Put this into the statements context.. it might be useful later
			    if(!context.containsKey("questions")) {
			    	context.put("questions", new LinkedList<String>());
			    }
			    ((LinkedList<String>)context.get("questions")).add(sent); 
			    			    
				return Global.randomChoice("How should I know?",
						"Do I look like Watson to you?",
						"Hmm better give me some more time to think about that?",
						"Sure..?",
						"Ummm I would rather not say.");
		     }
	    };
	    return defaultResponse;
	}
	
	private static Response defaultSQ() {
		Response defaultResponse = new Response() {
			@Override
			public String response(Parse p, HashMap<String, Object> context) {
				//Flip possessive statements
			    String sent = Global.flipPossesives(p.toString());
			    
			    //Put this into the statements context.. it might be useful later
			    if(!context.containsKey("directQuestions")) {
			    	context.put("directQuestions", new LinkedList<String>());
			    }
			    ((LinkedList<String>)context.get("directQuestions")).add(sent); 
			    			    
				return Global.randomChoice("Yes",
						"No.",
						"Negatory.",
						"Heck yeah!",
						"Hmmmm..... No.");
			    
			}
	    };
	    return defaultResponse;
	}
	
	
	//Return the response actions for S type parses
	private LinkedList<Entry> S() {
		LinkedList<Entry> ret = new LinkedList<Entry>();
		
		//Add response actions here...

		
		//Advanced response example: I want a....
		Response IWantA = new Response() {
			@Override
			public String response(Parse p, HashMap<String, Object> context) {
				//Get the first noun phrase occurring after "I want"
				LinkedList<Parse> nounPhrases = Global.findAllTags(p, new String[] { "NP" });
				for(Parse nounPhrase : nounPhrases) {
					//Don't care about "i or a" part.
					if(nounPhrase.toString().equalsIgnoreCase("I") ) {
						continue;
					}
					
					String str = nounPhrase.toString();
					
					return Global.randomChoice("What's so great about " + str + " anyhow?",
							                   "I'm sensing that " + str + " is somehow important to you."); 
				}
				
				//Could not get the noun. Do something generic
				return "You want many things.";
			}
		};
		
		
		
		ret.add(makeWordMatchEntry(IWantA,"I want a"));
		ret.add(makeWordMatchEntry(IWantA,"I want an"));
		ret.add(makeWordMatchEntry(IWantA,"I want some"));
		
		
		//Example: some basic responses. Basic responses just return a canned string.
		ret.add( makeWordMatchEntry(new BasicResponse("In Soviet Russia, dog walks you!"), "walk the dog"));
		ret.add( makeHyperMatchEntry(new BasicResponse("You're making me hungry!"), "food", POS.NOUN));
		ret.add( makeHyperMatchEntry(new BasicResponse("I really don't care about living things...I'm a machine!"), "animal", POS.NOUN));
		ret.add( makeWordMatchEntry(new BasicResponse("How do you know what I want?"), "you want to"));
		ret.add( makeWordMatchEntry(new BasicResponse("Well I want to go to Mars, that doesn't mean that I will."), "I want to go"));
		ret.add( makeWordMatchEntry(new BasicResponse("You're very needy, aren't you?"), "I need to"));
		ret.add( makeWordMatchEntry(new BasicResponse("It's probably best to look for help else where."), "I need your help"));
		ret.add( makeWordMatchEntry(new BasicResponse("Don't you already have enough?"), "I have to"));
		ret.add( makeWordMatchEntry(new BasicResponse("You thought wrong."), "I thought you"));
		return ret;
		
	}
	
	//Return the response actions for SBARQ type parses
	private LinkedList<Entry> SBARQ() {
		LinkedList<Entry> ret = new LinkedList<Entry>();
		
		//Advanced response example: What is.
		Response Whatisa = new Response() {
			@Override
			public String response(Parse p, HashMap<String, Object> context) {
				
				String str = "";
				String adj = "";
				LinkedList<Parse> Adjective = null;
				Parse Noun = null;
				
				if (Global.findFirstTag(p, new String[] { "JJ" }) != null){
					Adjective = Global.findAllTags(p, new String[] { "JJ" });
					for (Parse Adj : Adjective){
						adj += Adj.toString() + " and ";
					}
					adj = adj.substring(0, adj.length()-5);
				}
				
				if(Global.findFirstTag(p, new String[] { "NN" }) != null){
					Noun = Global.findFirstTag(p, new String[] { "NN" });
				}
											
				if (Noun != null){
					str = Noun.toString();
				}
				
			
			    if (Adjective != null && Noun != null){	
			    	if (Global.beginsWithVowel(str)){
			    		return  Global.randomChoice("I know what an " + str + " is but what makes it " + adj + "?",
			                    "Well thats a matter of opinion now isn't it.");
				    }else{
				    	return  Global.randomChoice("I know what a " + str + " is but what makes it " + adj + "?",
				    								"Well thats a matter of opinion now isn't it.");
				    }
			    }else if(Adjective == null && Noun != null){
			    	if (Global.beginsWithVowel(str)){
			    		return Global.randomChoice("An " + str + " is " + WordRelations.getDefinition(str, POS.NOUN) + ".");
			    	}else{
			    		return Global.randomChoice("A " + str + " is " + WordRelations.getDefinition(str, POS.NOUN) + ".");
			    	}
			    }else if(Adjective != null && Noun == null){
			    	if (Global.beginsWithVowel(adj)){
			    		return Global.randomChoice("An " + adj + " is " + WordRelations.getDefinition(adj, POS.ADJECTIVE) + ".");
			    	}else{
			    		return Global.randomChoice("A " + adj + " is " + WordRelations.getDefinition(adj, POS.ADJECTIVE) + ".");
			    	}
			    }else{
			    	return Global.randomChoice("You got me.", "I really have no clue.", "I have no idea.");
			    }					
			}
			
		};

		ret.add(makeWordMatchEntry(Whatisa,"What is a"));
		ret.add(makeWordMatchEntry(Whatisa,"What is an"));
		ret.add(makeWordMatchEntry(new BasicResponse("Not too shabby."), "How are you"));
		ret.add(makeWordMatchEntry(new BasicResponse("Pretty good, how are you?"), "How's it going"));
		ret.add(makeWordMatchEntry(new BasicResponse("Pretty good, how are you?"), "How is it going"));
		ret.add(makeWordMatchEntry(new BasicResponse("Why don't you look at the task bar."), "What time is it"));
		ret.add(makeWordMatchEntry(new BasicResponse("Well I'm talking to your now arn't I."), "What is going"));
		ret.add(makeWordMatchEntry(new BasicResponse("It goes."), "How goes it"));
		ret.add(makeWordMatchEntry(new BasicResponse("Damn fine, how are you?"), "Hows it be"));
		ret.add(makeWordMatchEntry(new RandomResponse(), "What should we talk about"));
		ret.add(makeWordMatchEntry(new RandomResponse(), "What do you want to talk about"));
		return ret;
		
	}	
	
	
	// Return the response list for SQ type parses.
	private LinkedList<Entry> SQ() {
		LinkedList<Entry> ret = new LinkedList<Entry>();
		
		ret.add(makeWordMatchEntry(new BasicResponse("Can't say that I do."), "Do you know"));
		ret.add(makeWordMatchEntry(new BasicResponse("Definately not!"), "Are you"));
		return ret;
	}

	
	/******************************************************/
	/* Helper functions, classes, structures              */
	/******************************************************/
	private static Responders inst = new Responders();
	public Entry makeWordMatchEntry(Response r, String wordPattern) {
		Entry ret = new Entry();
		ret.r = r;
		ret.wordPattern = wordPattern;
		return ret;
	}
	
	public Entry makeHyperMatchEntry(Response r, String hyperPattern, POS pos) {
		Entry ret = new Entry();
		ret.r = r;
		ret.hypernymPattern = hyperPattern;
		ret.pos = pos;
		return ret;
	}
	
	//Basic response --- Just returns a string of text.
	public class BasicResponse implements Response {

		private final String dumbText;
		
		public BasicResponse(String text) {
			dumbText = text;
		}
		
		@Override
		public String response(Parse p, HashMap<String, Object> context) {
			
			//If this is an "S" type, pick up this simple clause as a statement
			if(p.getType().equals("S")) {
			    if(!context.containsKey("statements")) {
			    	context.put("statements", new LinkedList<String>());
			    }
			    ((LinkedList<String>)context.get("statements")).add(Global.flipPossesives(p.toString())); 
			}
			return dumbText;
		}
		
	}
	
	//Make the random convo starters in case the user wants a new topic.
	public class RandomResponse implements Response {
					@Override
		public String response(Parse p, HashMap<String, Object> context) {
			    
			//Put this into the statements context.. it might be useful later
			if(!context.containsKey("starters")) {
				context.put("starters", new LinkedList<String>());
			}
			
			if(!((HashMap<String,Integer>)context.get("userSentences")).isEmpty()){
				HashMap<String, Integer> sents = ((HashMap<String,Integer>)context.get("userSentences"));
				
				for(String sent : sents.keySet()){
					//Flip possessive statements
					Parse users = Global.parseString(sent);
					
					Parse[] level2 = users.getChildren();
					if (level2[0].getType().equals("S")){
						sent = Global.flipPossesives(sent.toString());		    			    
						return Global.randomChoice("Well, you said "  + sent.substring(0,sent.length()-1) + ", tell me more.");     
					}
						
				}
			}
			
			String sent = "How's your family?";
			int tries = 0;
					
			while (((LinkedList<String>)context.get("starters")).contains(sent) && tries <= 20){
					sent = Global.randomChoice("What is your favorite sport?",
					"How do you feel about this freakin weather?",
					"Well what do you wanna talk about?",
					"Do you like sports?",
					"Do you know who I am?",
					"Do you have any siblings?",
					"What is your favorite food?",
					"How old are you?",
					"What did you do today?");
					tries++;
			}
			
			if (tries == 20){
				sent = "I'm tired of coming up with conversation ideas.";
			}
				
			((LinkedList<String>)context.get("starters")).add(sent);
				
			return sent;
		}	
	}
}

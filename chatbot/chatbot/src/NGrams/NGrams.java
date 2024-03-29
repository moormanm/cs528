package NGrams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


import chatbot.Global;
import chatbot.chatbot;


public class NGrams {
	public static void main(String[] args) {

		// Find examples of sentence for a given top POS.
		/*
		System.out.println("Loading POS models..");
		Global.loadModels();
		POSNGram.getSentPOSExamples("/hack", "SINV");
		*/
		
		//Uncomment these if you want the POS NGrams (or the POS filtered WordNGrams)
		Global.stdin = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Loading POS models..");
		Global.loadModels();

		System.out.println("Done");
		
		POSDividedWordNGram wg = new POSDividedWordNGram("SBARQ");
		wg.ProcessFile("/aggregate.txt");
		wg.ProcessFile("/play");
		wg.ProcessFile("/hack");
		wg.ProcessFile("/play2");
		wg.ProcessFile("/sic");
		
		
		for(int i = 0; i < wg.size(); i++) {
			WordNGram.TruncBelowDeviations(wg.get(i),3);
			System.out.println("\n\n" + (i+1) + " level NGram:\n------------------------------------------------");
			System.out.println(POSNGram.NGramEnt2Str(wg.get(i)));
			//ArrayList<parseObject> tags = wg.filterPhrases(wg.tagWordNGram(i));
			//System.out.println(wg.arrayListParseToString(tags));
		}
		

		
       
	}
}

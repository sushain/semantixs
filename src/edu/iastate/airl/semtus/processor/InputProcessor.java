/*
 
SEMANTIXS (System for Extraction of doMAin-specific iNformation
from unstructured Text Including compleX Structures)

Copyright (c) 2010 Sushain Pandit.

Info: SEMANTIXS is a system for ontology-guided extraction and semantic 
representation of structured information from unstructured text.
For further information, please visit - http://www.sushain.com/semantixs/home

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, see <http://www.gnu.org/licenses/>.

*/

package edu.iastate.airl.semtus.processor;

import java.util.ArrayList;
import java.util.StringTokenizer;

import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.Word;

public class InputProcessor {
	
	public static ArrayList <Sentence<Word>> getSentences (final String [] params) {
	
		String processedInput = params[1].replaceAll("[^a-zA-Z0-9.?&\t\n\r\b:; ]", "");
		
		processedInput = processedInput.replaceAll("&", "and");
		
		processedInput = processedInput.replaceAll("[:;]", ".");
		
		processedInput = processedInput.replaceAll("\t\n\r\b", " ");
		
		StringTokenizer tokenizer = new StringTokenizer (processedInput, ".?");
		
		ArrayList <Sentence<Word>> sentenceList = new ArrayList <Sentence<Word>> (); 
		
		String sentence; 
		
		while (tokenizer.hasMoreTokens()) {
			
			sentence = tokenizer.nextToken();
			 
			if (sentence == null || sentence.trim().equals("") == true) {
				
				continue;
			}
			
			StringTokenizer wordTokenizer = new StringTokenizer (sentence, " ");
			
			Sentence <Word> sent = new Sentence <Word> ();
			
			while (wordTokenizer.hasMoreTokens()) {
				
				sent.add (new Word (wordTokenizer.nextToken()));
			}
			
			sentenceList.add(sent);
		}
		
		return sentenceList;
	}
}

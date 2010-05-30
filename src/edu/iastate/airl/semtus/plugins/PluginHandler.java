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

package edu.iastate.airl.semtus.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import edu.iastate.airl.semtus.parser.Parser;
import edu.iastate.airl.semtus.util.Utils;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.trees.Tree;

public class PluginHandler {

	public static void handlePlugins () {

		try {

			Runtime rt = Runtime.getRuntime();

			Process tag = rt.exec(".\\resources\\tagger.exe -i .\\resources\\test.txt -o .\\resources\\out.txt");

			InputStream stderr = tag.getErrorStream();

			InputStreamReader isr = new InputStreamReader(stderr);

			BufferedReader br = new BufferedReader(isr);

			String line = null;

			System.out.println("<ERROR>");

			while ((line = br.readLine()) != null) {

				System.out.println(line);
			}

			System.out.println("</ERROR>");

			System.out.println(tag.waitFor());

			stderr.close ();
			
			isr.close();
			
			br.close();
			
			/***************************************/
			
			Process parse = rt.exec(".\\resources\\parser.exe -i .\\resources\\out.txt -o .\\resources\\parsed.txt");
			
			stderr = parse.getErrorStream();

			isr = new InputStreamReader(stderr);

			br = new BufferedReader(isr);

			line = null;

			System.out.println("<ERROR>");

			while ((line = br.readLine()) != null) {

				System.out.println(line);
			}

			System.out.println("</ERROR>");

			System.out.println(tag.waitFor());

		} catch (Exception excp) {
			
			excp.printStackTrace();
		} 
	}
	
	public static List <Tree> generateTrees (final ArrayList<Sentence<Word>> sentenceList) throws IOException {
		
		/**************************************************************************
		 * Generate dependencies here
		 **************************************************************************/

		ObjectInputStream thisInputStream = IOUtils.readStreamFromString(Utils.OUTPUT_DIRECTORY + "grammar/englishPCFG.ser.gz");
		
		Parser thisParser = new Parser(thisInputStream);
		
	    List <Tree> theseTrees = thisParser.parse(sentenceList);
	    
	    return theseTrees;
	}
}

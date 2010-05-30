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

package edu.iastate.airl.semtus.main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.iastate.airl.semtus.processor.InputProcessor;
import edu.iastate.airl.semtus.util.Utils;
import edu.iastate.airl.semtus.generator.impl.Generator;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.Word;


public class ServiceController {
	
	private ArrayList<Sentence<Word>> sentenceList = null;
	
	private String fullQualifiedOntologyName = null;

	private Generator generatorInstance = null;
	
	private Model model = ModelFactory.createDefaultModel();
	
	
	public ServiceController(final String [] params) {
			
		this.fullQualifiedOntologyName = params[0];
		
		this.sentenceList = InputProcessor.getSentences(params);
		
		this.generatorInstance = new Generator (this.fullQualifiedOntologyName, this.model, params[2]);
		 
		this.generatorInstance.process(sentenceList);
		
		model.write(System.out);
		
		try {
			model.write(new BufferedWriter(new FileWriter (Utils.OUTPUT_DIRECTORY + "output.rdf")));
			
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
	
	public ArrayList<Sentence<Word>> getSentenceList() {
		
		return this.sentenceList;
	}

	public void setSentenceList(final ArrayList<Sentence<Word>> sentenceList) {
		
		this.sentenceList = sentenceList;
	}

	public String getFullQualifiedOntologyName() {
		
		return this.fullQualifiedOntologyName;
	}

	public void setFullQualifiedOntologyName(final String fullQualifiedOntologyName) {
		
		this.fullQualifiedOntologyName = fullQualifiedOntologyName;
	}	
}
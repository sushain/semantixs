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

package edu.iastate.airl.semtus.generator.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ReifiedStatement;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

import edu.iastate.airl.semtus.generator.contract.IGenerator;
import edu.iastate.airl.semtus.parser.StructureAnalyzer;
import edu.iastate.airl.semtus.plugins.PluginHandler;
import edu.iastate.airl.semtus.processor.RDFProcessor;
import edu.iastate.airl.semtus.rdf.Triple;
import edu.iastate.airl.semtus.rules.contract.IRuleEngine;
import edu.iastate.airl.semtus.rules.impl.RuleEngine;
import edu.iastate.airl.semtus.util.Utils;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;

public class Generator implements IGenerator {

	private String uri = "";
	
	private ArrayList <Triple> tripleList = new ArrayList <Triple> ();
	
	private RDFProcessor rdfProcessor = null;
	
	private IRuleEngine ruleProcessor = null;
	
	private Model model = null;
	
	private List <Tree> generatedTrees = null;
	
	private Object [][][] generatedDependencies = null;
	
	private String level;
	
	
	// private Log4JLogger logger = new Log4JLogger (Generator.class.getName());
	
	
	public Generator (final String fullQualifiedOntologyName, Model model, final String level) {
		
		this.model = model;
		
		this.rdfProcessor = new RDFProcessor (this, fullQualifiedOntologyName, level);
		
		this.level = level;
	}
	
	public void process (final ArrayList<Sentence<Word>> sentenceList) {
		
		ruleProcessor = new RuleEngine(this);
	
		/**************************************************************************
		 * Generate dependencies here
		 **************************************************************************/
		
		try {
			
			this.generatedTrees = PluginHandler.generateTrees(sentenceList);
			
			StructureAnalyzer thisAnalyzer = new StructureAnalyzer(); 
		    
			this.generatedDependencies = new Object[this.generatedTrees.size()][][];
			                 
			int count=0;
			
		    for (Tree thisTree : this.generatedTrees) {
		    	
				// System.out.println(thisTree); 
				GrammaticalStructure thisStructure = thisAnalyzer.analyzeTree(thisTree);
				
			    Collection <TypedDependency> theseDependencies = thisStructure.allTypedDependencies();
			    
			    this.generatedDependencies[count] = new Object[theseDependencies.size()][3];
				
			    int i = 0;
				
				for (TypedDependency thisDependency : theseDependencies) {
					
					this.generatedDependencies[count][i][0] = thisDependency.reln().toString();
					this.generatedDependencies[count][i][1] = thisDependency.gov().toString().substring(0, thisDependency.gov().toString().indexOf("-"));
					this.generatedDependencies[count][i][2] = thisDependency.dep().toString().substring(0, thisDependency.dep().toString().indexOf("-"));
				    
				    System.out.println(this.generatedDependencies[count][i][0].toString() + " ( " + this.generatedDependencies[count][i][1].toString() + ", " + this.generatedDependencies[count][i][2].toString() + " )");
				    i++;
				}
				
				count++;
		    }

		} catch (IOException excp) {
			
			excp.printStackTrace();
		} 
			
		if (this.generatedDependencies == null) {
			
			// logger.debug("Failed to parse the sentence");
			
			System.out.println("Failed to parse the sentence");
		}
		
		this.ruleProcessor.process (this.generatedTrees, this.generatedDependencies);
	    
		Iterator <Triple> iter = this.tripleList.iterator();
		
		while (iter.hasNext()) {
			
			Triple thisTriple = (Triple)iter.next();
			
			generateRDFTriples (thisTriple.getSubjectResourceString(), thisTriple.getObject(), thisTriple.getPredicateStringArray());
		}
	}
	    
	public Statement generateRDFTriples (final String subjectResourceString, final Object object, final String [] predicateStringArray) {
		
		System.out.println(subjectResourceString + "--" + object + "--" + predicateStringArray);
		
		if (subjectResourceString == null || object == null || predicateStringArray == null) {
			
			System.out.println("Bad Triple / Chucking and moving on..");
			
			return null;
		}
			
		// Lookup the subject
		Resource subjectResource = this.rdfProcessor.lookupString(subjectResourceString.trim(), "", Utils.INPUT_ONT);
		
		/***********************************************************************************
		 * If we're into the advanced level 2 and the subject isn't found in the input 
		 * ontology, try looking it up in the instance assertions (FOAF for now)
		 ***********************************************************************************/
		if ((this.level.equals(Utils.LEVEL_TWO) || this.level.equals(Utils.LEVEL_THREE)) && subjectResource == null) {
			
			subjectResource = this.rdfProcessor.lookupInstances(subjectResourceString.trim());
		}

		/***********************************************************************************
		 * If we're into the advanced level 2 and the subject isn't found in the input 
		 * ontology, try looking it up in the pre-defined Auxiliary knowledge-base (dbpedia) 
		 ***********************************************************************************/
		if ((this.level.equals(Utils.LEVEL_TWO) || this.level.equals(Utils.LEVEL_THREE)) && subjectResource == null) {
			
			subjectResource = this.rdfProcessor.lookupAuxKB(subjectResourceString.trim());
		}
		
		// Correct the uri-refs for subjects matched against FOAF
		if (subjectResource != null && subjectResource.getURI().trim().equals("") == false && subjectResource.getURI().contains("http") == false) {
			
			subjectResource = new ResourceImpl ("http://myfoafdata#", subjectResource.getURI().trim().substring(subjectResource.getURI().trim().lastIndexOf("#") + 1));
		}
		
		/***********************************************************************************
		 * If we're into the advanced level 3 and the subject isn't found by level 2 
		 * procedures, create a new resource (enrichment)
		 ***********************************************************************************/
		if (this.level.equals(Utils.LEVEL_THREE) && subjectResource == null) {
			
			subjectResource = new ResourceImpl ("http://edu.iastate.airl/semtus/Thing#", subjectResourceString.trim());
		}
		
		System.out.println("this.subjectResource = " + subjectResource);
		
		/****************************************************************************************************/
		
		// Check each candidate predicate for a matching property in the given ontology
		
		Property predicate=null;
		
		for (int count=0; count<predicateStringArray.length; count++) {
			
			if (predicateStringArray[count].equals ("") || predicateStringArray[count].equals (" ") || Utils.checkTrivial(predicateStringArray[count])) {
				
				continue;
			}
				
			if (this.rdfProcessor.lookupString(null, predicateStringArray[count].trim(), Utils.INPUT_ONT) != null) {
				
				predicate = this.model.createProperty(this.uri);
									
				break;
				
			} else {
				
				predicate = null;
			}
		}
		
		/***********************************************************************************
		 * If we're into the advanced level 2 and the predicate isn't found in the input 
		 * ontology, try looking it up in the pre-defined Auxiliary knowledge-base (dbpedia+FOAF) 
		 ***********************************************************************************/
		if ((this.level.equals(Utils.LEVEL_TWO) || this.level.equals(Utils.LEVEL_THREE)) && predicate == null) {
			
			// Check each candidate predicate for a matching property in the given ontology 
			for (int count=0; count<predicateStringArray.length; count++) {
				
				if (predicateStringArray[count].equals ("") || predicateStringArray[count].equals (" ") || Utils.checkTrivial(predicateStringArray[count])) {
					
					continue;
				}
					
				if (this.rdfProcessor.lookupString(null, predicateStringArray[count].trim(), Utils.AUXILIARY_KB) != null) {
					
					predicate = this.model.createProperty(this.uri);
					
					break;
					
				} else {
					
					predicate = null;
				}
			}
			
		}
		
		/***********************************************************************************
		 * If we're into the advanced level 3 and the predicate isn't found by level 2 
		 * procedures, create a new property (enrichment)
		 ***********************************************************************************/
		if (this.level.equals(Utils.LEVEL_THREE) && predicate == null) {
			
			for (int count=0; count<predicateStringArray.length; count++) {
				
				if (predicateStringArray[count].equals ("") || predicateStringArray[count].equals (" ") || Utils.checkTrivial(predicateStringArray[count])) {
					
					continue;
				}
					
				predicate = this.model.createProperty("http://edu.iastate.airl/semtus/GenericRelation#", predicateStringArray[count].trim());
				
				break;
			}
		}
		
		System.out.println("this.predicate = " + predicate);
		
		/************************** Add triple to the RDF model by figuring out what the object is *******************************/
		
		Resource objectResource;
		
		if (subjectResource != null && predicate != null && object != null) {
			
			if (object instanceof String) {
				
				//Lookup the object
				objectResource = this.rdfProcessor.lookupString(object.toString().trim(), "", Utils.INPUT_ONT);
				
				/***********************************************************************************
				 * If we're into the advanced level 2 and the object isn't found in the input 
				 * ontology, try looking it up in the instance assertions (FOAF for now)
				 ***********************************************************************************/
				if ((this.level.equals(Utils.LEVEL_TWO) || this.level.equals(Utils.LEVEL_THREE)) && objectResource == null) {
					
					objectResource = this.rdfProcessor.lookupInstances(object.toString().trim());
				}
				
				/***********************************************************************************
				 * If we're into the advanced level 2 and the object isn't found in the input 
				 * ontology, try looking it up in the pre-defined Auxiliary knowledge-base (dbpedia) 
				 ***********************************************************************************/
				if ((this.level.equals(Utils.LEVEL_TWO) || this.level.equals(Utils.LEVEL_THREE)) && objectResource == null) {
					
					objectResource = this.rdfProcessor.lookupAuxKB(object.toString().trim());
				
				}
				
				if (objectResource != null && objectResource.getURI().trim().equals("") == false && objectResource.getURI().contains("http") == false) {
					
					objectResource = new ResourceImpl ("http://myfoafdata#", objectResource.getURI().trim().substring(objectResource.getURI().trim().lastIndexOf("#") + 1));
				}
				
				/***********************************************************************************
				 * If we're into the advanced level 3 and the object isn't found by level 2 
				 * procedures, create a new resource (enrichment)
				 ***********************************************************************************/
				if (this.level.equals(Utils.LEVEL_THREE) && objectResource == null) {
					
					objectResource = new ResourceImpl ("http://edu.iastate.airl/semtus/Thing#", object.toString().trim());
				}
				
				Statement stmt;
				
				if (objectResource != null) {
					
					System.out.println("this.objectResource = " + objectResource);
					
					stmt = model.createStatement(subjectResource, predicate, objectResource);
					
				} else {
			
					System.out.println("this.object = " + object);
					
					stmt = model.createStatement(subjectResource, predicate, object.toString());
				}
				
				this.model.add(stmt);
				
				System.out.println("Cool Triple / Added and continuing..");
				
				return stmt;
				
			} else {
			
				// Generate internal reified statement first.
				Statement stmt = generateRDFTriples(((Triple)object).getSubjectResourceString(), ((Triple)object).getObject(), ((Triple)object).getPredicateStringArray());
				
				if (stmt == null) {
					
					System.out.println("Bad Triple / Chucking and moving on..");
					
					return null;
				}
				
				ReifiedStatement rstmt = stmt.createReifiedStatement();
				
				this.model.add(rstmt.getStatement());
				
				// Generate the overall statement now.
				this.model.add(subjectResource, predicate, rstmt.as(ReifiedStatement.class));
				
				// Remove the internal statement since it's already captured in the overall statement above.
				this.model.remove(stmt);
				
				System.out.println("Cool Triple / Added and continuing..");
				
				return null;
			}
		} else {
		
			System.out.println("Bad Triple / Chucking and moving on..");		
		}
		return null;
		
		/**************************************** Ends ****************************************/
	}
	
	public void setUri(final String uri) {
		this.uri = uri;
	}

	public void setTripleList(ArrayList <Triple> tripleList) {
		this.tripleList = tripleList;
	}

	public ArrayList <Triple> getTripleList() {
		return tripleList;
	}
}
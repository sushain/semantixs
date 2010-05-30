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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.StringTokenizer;

import com.google.gwt.i18n.rebind.keygen.FullyQualifiedMethodNameKeyGenerator;
import com.hp.hpl.jena.ontology.impl.DatatypePropertyImpl;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NsIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.sparql.pfunction.library.str;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.iastate.airl.semtus.generator.contract.IGenerator;
import edu.iastate.airl.semtus.util.Utils;

public class RDFProcessor {

	private IGenerator generatorAlias = null;
	
	private Model ontologyModel = null;
	
	private Model aboxModel = null;
	
	public RDFProcessor (final IGenerator gen, final String fullQualifiedOntologyName, final String level) {
		
		generatorAlias = gen;
		
		try {
			/*in = new FileInputStream(new File(fullQualifiedOntologyName));
		
			// Create an empty in-memory model and populate it from the graph
			this.ontologyModel = ModelFactory.createMemModelMaker()
					.createModel("Input ontology Model");
	
			// null base URI, since model URIs are absolute
			ontologyModel.read(in, null);
	
			in.close();*/
			
			System.out.println("Loading: " + fullQualifiedOntologyName);
			
			this.ontologyModel = FileManager.get().loadModel(fullQualifiedOntologyName);
			
			if (level.equals(Utils.LEVEL_TWO) || level.equals(Utils.LEVEL_THREE)) {
			
				System.out.println("Loading Auxiliary Knowledge base..");
			
				this.aboxModel = FileManager.get().loadModel(Utils.AUX_DIRECTORY + "foafdata.rdf");
			
				this.aboxModel.add(FileManager.get().loadModel(Utils.AUX_DIRECTORY + "dbpedia_3.4.owl"));
				
				this.aboxModel.add(FileManager.get().loadModel(Utils.AUX_DIRECTORY + "instancetype_en.nt"));
			}
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
	
	public Resource processRDFQuery (final String queryString) {

		Resource res = null;

		/**************** RDF processing *******************/

		try {

			Query query = QueryFactory.create(queryString);

			QueryExecution qe = QueryExecutionFactory.create(query,
					ontologyModel);

			ResultSet results = qe.execSelect();

			Model resultModel = ModelFactory.createMemModelMaker().createModel(
					"Resulting RDF Model");

			res = ResultSetFormatter.asRDF(resultModel, results);

			qe.close();

		} catch (Exception e) {
			// TODO: handle exception
		}

		return res;
	}

	public Resource lookupString(final String inputString,
			final String property, final String whichModel) {

		Model modelToUse = null;
		
		if (whichModel.equals(Utils.INPUT_ONT)) {
			
			modelToUse = this.ontologyModel; 
				
		} else {
			
			modelToUse = this.aboxModel;
		}
		
		Resource res = null;

		/**************** RDF processing *******************/

		try {

			ResIterator iter;
			
			// InputString isn't null => we are dealing with subject
			if (inputString != null) {
				
				iter = modelToUse.listSubjects();
				
				while(iter.hasNext()) {
					
					res = (Resource)iter.nextResource();
					
					if (inputString.indexOf(" ") != -1) {
						
						StringTokenizer strTok = new StringTokenizer(inputString, " ");
						
						while (strTok.hasMoreTokens()) {
							
							String thisToken = strTok.nextToken();
							
							if ((res.toString().toLowerCase()).contains(thisToken.toLowerCase())) {
								
								return res;
							}
						}
						
					} else {
						
						if ((res.toString().toLowerCase()).contains(inputString.toLowerCase())) {
							
							return res;
						}
					}
				}
				
			} else {	// InputString is null => we are dealing with property
				
				/*******************************************************************
				* First check whether we have a schema property match
				/*******************************************************************/
				// find a relevant namespace for the property under consideration
				NsIterator nsIter = modelToUse.listNameSpaces();
				
				while (nsIter.hasNext()) {
					
					String nameSpace = nsIter.nextNs();
					
					iter = modelToUse.listResourcesWithProperty(modelToUse
							.createProperty(nameSpace, property));
					
					while (iter.hasNext()) {

						res = iter.nextResource();
						
						// just set the uri so that it can be used in generator while creating the predicate field
						generatorAlias.setUri (res.getURI());

						return res;
					}
				}
				
				/*******************************************************************
				* Next check whether we have a custom property match
				/*******************************************************************/
				
				/*iter = this.ontologyModel.listSubjects();
				
				while(iter.hasNext()) {
					
					res = (Resource)iter.nextResource();
					
					if ((res.toString().toLowerCase()).contains(property.toLowerCase())) {
						
						// just set the uri so that it can be used in generator while creating the predicate field.						
						generatorAlias.setUri (res.getNameSpace());
						
						// set the property name here itself in this case since we just check for a partial match (using contains)
						// (eg, alternate -> alternateName)
						// So we can't be sure that what we parsed is indeed the complete property name or not.  
						generatorAlias.setPredicateName (res.getLocalName());
						
						return res;
					}
				}*/
				
				// Check in OWL object properties
				Resource classClass =  modelToUse.getResource(OWL.getURI() + "ObjectProperty" );
	
				iter = modelToUse.listSubjectsWithProperty(RDF.type, classClass );
				
				while(iter.hasNext()) {
					
					res = (Resource)iter.nextResource();
					
					// System.out.println("res: " + res);
					
					if (property.indexOf(" ") != -1) {
						
						StringTokenizer strTok = new StringTokenizer(property, " ");
						
						while (strTok.hasMoreTokens()) {
							
							String thisToken = strTok.nextToken();
							
							if ((res != null) && (res.getLocalName().toLowerCase()).contains(thisToken.toLowerCase())) {
								
								generatorAlias.setUri(res.getURI());
								
								return res;
							}
						}
						
					} else {
						
						if ((res != null) && (res.getLocalName().toLowerCase()).contains(property.toLowerCase())) {
							
							generatorAlias.setUri(res.getURI());
							
							return res;
						}
					}
				}
				
				// Check in OWL datatype properties
				classClass =  modelToUse.getResource(OWL.getURI() + "DatatypeProperty" );
	
				iter = modelToUse.listSubjectsWithProperty(RDF.type, classClass );
				
				while(iter.hasNext()) {
					
					res = (Resource)iter.nextResource();
					
					// System.out.println("res: " + res);
					
					if (property.indexOf(" ") != -1) {
						
						StringTokenizer strTok = new StringTokenizer(property, " ");
						
						while (strTok.hasMoreTokens()) {
							
							String thisToken = strTok.nextToken();
							
							if ((res != null) && (res.getLocalName().toLowerCase()).contains(thisToken.toLowerCase())) {
								
								generatorAlias.setUri(res.getURI());
								
								return res;
							}
						}
						
					} else {
						
						if ((res != null) && (res.getLocalName().toLowerCase()).contains(property.toLowerCase())) {
							
							generatorAlias.setUri(res.getURI());
							
							return res;
						}
					}
				}
				
				// Check in RDF 'property' types
				classClass =  modelToUse.getResource(RDF.getURI() + "Property" );
	
				iter = modelToUse.listSubjectsWithProperty(RDF.type, classClass );
				
				while(iter.hasNext()) {
					
					res = (Resource)iter.nextResource();
					
					// System.out.println("res: " + res);
					
					if (property.indexOf(" ") != -1) {
						
						StringTokenizer strTok = new StringTokenizer(property, " ");
						
						while (strTok.hasMoreTokens()) {
							
							String thisToken = strTok.nextToken();
							
							if ((res != null) && (res.getLocalName().toLowerCase()).contains(thisToken.toLowerCase())) {
								
								generatorAlias.setUri(res.getURI());
								
								return res;
							}
						}
						
					} else {
						
						if ((res != null) && (res.getLocalName().toLowerCase()).contains(property.toLowerCase())) {
							
							generatorAlias.setUri(res.getURI());
							
							return res;
						}
					}
				}
			}

		} catch (Exception excp) {

			excp.printStackTrace();
		}

		return null;
	}
	
	/**
	 * Method to check whether the subject is predefined in the Aux KB
	 * 
	 * @param inputString
	 * @return
	 */
	public Resource lookupAuxKB(final String inputString) {

		Resource res = null;

		/**************** RDF processing *******************/

		try {

			ResIterator iter;
			
			if (inputString != null) {
			//	this.aboxModel.lis
				Resource classClass =  this.aboxModel.getResource(OWL.getURI() + "Class" );
	
				iter = this.aboxModel.listSubjectsWithProperty(RDF.type, classClass );
				
				while(iter.hasNext()) {
					
					res = (Resource)iter.nextResource();
					
					Property label = this.aboxModel.getProperty(RDFS.getURI() + "label" );
					
					Statement labelStmt = res.getProperty(label);
					
					// System.out.println("labelStmt: " + labelStmt);
					
					if (inputString.indexOf(" ") != -1) {
						
						StringTokenizer strTok = new StringTokenizer(inputString, " ");
						
						while (strTok.hasMoreTokens()) {
							
							String thisToken = strTok.nextToken();
							
							if ((labelStmt != null) && (labelStmt.getString().toLowerCase()).contains(thisToken.toLowerCase())) {
								
								return res;
							}
						}
						
					} else {
						
						if ((labelStmt != null) && (labelStmt.getString().toLowerCase()).contains(inputString.toLowerCase())) {
							
							return res;
						}
					}
				}
			}

		} catch (Exception excp) {

			excp.printStackTrace();
		}

		return null;
	}
	
	/**
	 * Method to check whether the subject appears in one of the asserted instances in FOAF
	 * 
	 * @param inputString
	 * @return
	 */
	public Resource lookupInstances(final String inputString) {

		Resource res = null;

		/**************** RDF processing *******************/

		try {

			ResIterator iter;
			
			if (inputString != null) {
			//	this.aboxModel.lis
				Resource personClass =  this.aboxModel.getResource(FOAF.getURI() + "Person" );
				
				iter = this.aboxModel.listSubjectsWithProperty(RDF.type, personClass );
				
				while(iter.hasNext()) {
					
					res = (Resource)iter.nextResource();
					
					Property name = this.aboxModel.getProperty(FOAF.getURI() + "name" );
					
					Statement nameStmt = res.getProperty(name);
					    
					if (inputString.indexOf(" ") != -1) {
						
						StringTokenizer strTok = new StringTokenizer(inputString, " ");
						
						while (strTok.hasMoreTokens()) {
							
							String thisToken = strTok.nextToken();
							
							if ((nameStmt != null) && (nameStmt.getString().toLowerCase()).contains(thisToken.toLowerCase())) {
								
								return res;
							}
						}
						
					} else {
						
						if ((nameStmt != null) && (nameStmt.getString().toLowerCase()).contains(inputString.toLowerCase())) {
							
							return res;
						}
					}
				}
			}

		} catch (Exception excp) {

			excp.printStackTrace();
		}

		return null;
	}
}

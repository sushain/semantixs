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

package edu.iastate.airl.semtus.rules.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import edu.iastate.airl.semtus.generator.contract.IGenerator;
import edu.iastate.airl.semtus.rdf.Triple;
import edu.iastate.airl.semtus.rules.contract.IRuleEngine;

public class ParseTreeRuleEngine extends RuleEngine {

	private IGenerator generatorAlias = null;
	
	private ArrayList <String> predicateList;
	
	
	public ParseTreeRuleEngine (final IGenerator gen) {
		
		generatorAlias = gen; 
	}
	
	public void process(final String sentenceParse, final HashMap <String, String> dependencyAnalysisMap) {

		try {
			
			if (RuleEngine.getCachedTriple() != null)
				System.out.println("Inside process: cachedTriple.getSubjectResourceString(): " + RuleEngine.getCachedTriple().getSubjectResourceString());
			
			Triple tripleInstance=null;
			
			// Check what we've got from DependencyGraphRules before proceeding to fine-grain sentence analysis
			// to extract triples
			
			String [] predicateArray = new String [IRuleEngine.MAX_REIFICATION_COMPLEXITY];
			
			System.out.println(dependencyAnalysisMap.get(IRuleEngine.REIFICATION_FLAG));
			
			// Check for reification
			if (dependencyAnalysisMap.get(IRuleEngine.REIFICATION_FLAG) != null && dependencyAnalysisMap.get(IRuleEngine.REIFICATION_FLAG).equals(IRuleEngine.FOUND)) {
				
				// Break the parse-tree about the Main predicate
				
				Iterator <String> iter = predicateList.iterator();
				
				int index=0;
				
				while (iter.hasNext()) {
					
					predicateArray[index++] = iter.next();
				}
				
				System.out.println(predicateArray[0] + " - " + predicateArray[1]);
				
				String innerSentenceParse = sentenceParse.substring(sentenceParse.indexOf(predicateArray[0])+predicateArray[0].length()); 
					
				System.out.println(innerSentenceParse);
				
				String outerSentenceParse = sentenceParse.substring(0, sentenceParse.indexOf(predicateArray[0]));
				
				System.out.println(outerSentenceParse);
				
				// find the main subject first
				String mainSubject = findSubject (outerSentenceParse);
				
				System.out.println(mainSubject);
				
				// A clausal--conj_and structure
				if (dependencyAnalysisMap.get(IRuleEngine.CONJ_AND) != null && dependencyAnalysisMap.get(IRuleEngine.CONJ_AND).equals(IRuleEngine.FOUND)) {
					
					// Break the parse-tree about 'and'
					String firstSentenceParse = innerSentenceParse.substring(0, innerSentenceParse.indexOf(IRuleEngine.CONJ_AND)); 
					
					firstSentenceParse = firstSentenceParse.substring(0, firstSentenceParse.lastIndexOf(")") + 1);
					
					System.out.println(firstSentenceParse);
					
					tripleInstance = createTriple(firstSentenceParse);
					
					// In case of conj_and, we only need to cache the first sentence.
					RuleEngine.setCachedTriple(tripleInstance);
					
					Triple tempHolderForFirstSentence = tripleInstance; 
					
					Triple clausalTripleInstance = new Triple (mainSubject, tripleInstance, new String [] {predicateArray[0]});
					
					this.generatorAlias.getTripleList().add(clausalTripleInstance );
					
					String secondSentenceParse = innerSentenceParse.substring(innerSentenceParse.indexOf(IRuleEngine.CONJ_AND)+ IRuleEngine.CONJ_AND.length());
					
					System.out.println(secondSentenceParse);
					
					// Check whether the secondSentenceParse is of the form NN* - VB* - Object or simply - NN* / - VB*
					// & delegate to the triple creation logic accordingly.
					if (secondSentenceParse.contains("VB") && (secondSentenceParse.contains("NN") || secondSentenceParse.contains("NP"))) {
						
						// Of the form: and - NN* - VB* - Object
						
							if (secondSentenceParse.contains("PRP")) {
								
								// Case of a preposition in subject of second sentence => Replace by the subject of first sentence.
								
								String temp = secondSentenceParse.substring(secondSentenceParse.indexOf("PRP") + 4);
								
								String proposition = temp.substring(0, temp.indexOf(")"));
								
								secondSentenceParse = secondSentenceParse.replace(proposition, tempHolderForFirstSentence.getSubjectResourceString());
								
								System.out.println("secondSentenceParse: " + secondSentenceParse);
								
								tripleInstance = createTriple(secondSentenceParse);
								
								// just make sure that the subject is set. If not, manually set it to the subject of the 
								// first sentence since we know that has to be true.
								if (tripleInstance.getSubjectResourceString().equals("")) {
									
									tripleInstance.setSubjectResourceString(tempHolderForFirstSentence.getSubjectResourceString());
								}
								
								clausalTripleInstance = new Triple (mainSubject, tripleInstance, new String [] {predicateArray[0]});
								
								this.generatorAlias.getTripleList().add(clausalTripleInstance );
								
							} else {
								
								// O'wise simply create another triple based on the second sentence
								
								clausalTripleInstance = new Triple (mainSubject, createTriple(secondSentenceParse), new String [] {predicateArray[0]});
								
								this.generatorAlias.getTripleList().add(clausalTripleInstance);
							}	
						
						} else if (secondSentenceParse.contains("VB") || secondSentenceParse.contains("VP")) {
							
							// Of the form: and - VB* - Object 
							
							tripleInstance = createTriple(secondSentenceParse);
							
							// Subject isn't found (expected) => Utilize the subject of first sentence.  
							if (tripleInstance.getSubjectResourceString().equals("")) {
								
								tripleInstance.setSubjectResourceString(tempHolderForFirstSentence.getSubjectResourceString());
								
								clausalTripleInstance = new Triple (mainSubject, tripleInstance, new String [] {predicateArray[0]});
								
								this.generatorAlias.getTripleList().add(clausalTripleInstance );	
							}
							
						} else { // Of the form: and - Object
							
							// Extract the object from the second sentence and utilize the subject and predicate from the first
							String object = findObject(secondSentenceParse);
							
							Triple newTriple = new Triple(tempHolderForFirstSentence.getSubjectResourceString(), object, tempHolderForFirstSentence.getPredicateStringArray());
							
							clausalTripleInstance = new Triple (mainSubject, newTriple, new String [] {predicateArray[0]});
							
							this.generatorAlias.getTripleList().add(clausalTripleInstance);
						}
					}
				
					// Not a clausal--conj_and structure  
					else {
						Triple innerTriple = createTriple(innerSentenceParse);
									
						tripleInstance = new Triple (mainSubject, innerTriple, new String [] {predicateArray[0]});
						
						this.generatorAlias.getTripleList().add(tripleInstance);
						
						RuleEngine.setCachedTriple(tripleInstance);
					}
				
			  // Check for the presence of conjunct
			} else if (dependencyAnalysisMap.get(IRuleEngine.CONJ_AND) != null && dependencyAnalysisMap.get(IRuleEngine.CONJ_AND).equals(IRuleEngine.FOUND)) {
				
				// Break the parse-tree about 'and'
				String firstSentenceParse = sentenceParse.substring(0, sentenceParse.indexOf(IRuleEngine.CONJ_AND)); 
				
				firstSentenceParse = firstSentenceParse.substring(0, firstSentenceParse.lastIndexOf(")") + 1);
				
				System.out.println(firstSentenceParse);
				
				tripleInstance = createTriple(firstSentenceParse);
				
				Triple tempHolderForFirstSentence = tripleInstance; 
				
				this.generatorAlias.getTripleList().add(tripleInstance);
				
				// In case of conj_and, we only need to cache the first sentence.
				RuleEngine.setCachedTriple(tripleInstance);
				
				String secondSentenceParse = sentenceParse.substring(sentenceParse.indexOf(IRuleEngine.CONJ_AND)+ IRuleEngine.CONJ_AND.length());
				
				System.out.println(secondSentenceParse);
				
				// Check whether the secondSentenceParse is of the form NN* - VB* - Object or simply - NN* / - VB*
				// & delegate to the triple creation logic accordingly.
				if (secondSentenceParse.contains("VB") && (secondSentenceParse.contains("NN") || secondSentenceParse.contains("NP"))) {
					
					// Of the form: and - NN* - VB* - Object
					
					if (secondSentenceParse.contains("PRP")) {
						
						// Case of a preposition in subject of second sentence => Replace by the subject of first sentence.
						
						String temp = secondSentenceParse.substring(secondSentenceParse.indexOf("PRP") + 4);
						
						String proposition = temp.substring(0, temp.indexOf(")"));
						
						secondSentenceParse = secondSentenceParse.replace(proposition, tempHolderForFirstSentence.getSubjectResourceString());
						
						System.out.println("secondSentenceParse: " + secondSentenceParse);
						
						tripleInstance = createTriple(secondSentenceParse);
						
						// just make sure that the subject is set. If not, manually set it to the subject of the 
						// first sentence since we know that has to be true.
						if (tripleInstance.getSubjectResourceString().equals("")) {
							
							tripleInstance.setSubjectResourceString(tempHolderForFirstSentence.getSubjectResourceString());
						}
						
						this.generatorAlias.getTripleList().add(tripleInstance);
						
					} else {
						
						// O'wise simply create another triple based on the second sentence
						
						this.generatorAlias.getTripleList().add(createTriple(secondSentenceParse));
					}	
					
				} else if (secondSentenceParse.contains("VB") || secondSentenceParse.contains("VP")) {
					
					// Of the form: and - VB* - Object 
					
					tripleInstance = createTriple(secondSentenceParse);
					
					// Subject isn't found (expected) => Utilize the subject of first sentence.  
					if (tripleInstance.getSubjectResourceString().equals("")) {
						
						tripleInstance.setSubjectResourceString(tempHolderForFirstSentence.getSubjectResourceString());
						
						this.generatorAlias.getTripleList().add(tripleInstance);
					}
					
				} else { // Of the form: and - Object
					
					// Extract the object from the second sentence and utilize the subject and predicate from the first
					String object = findObject(secondSentenceParse);
					
					this.generatorAlias.getTripleList().add(new Triple(tempHolderForFirstSentence.getSubjectResourceString(), object, tempHolderForFirstSentence.getPredicateStringArray()));
				}
				
			  // Check for the presence of prepositional modifier
			} else if (dependencyAnalysisMap.get(IRuleEngine.PREP_WITH) != null && dependencyAnalysisMap.get(IRuleEngine.PREP_WITH).equals(IRuleEngine.FOUND)) {
				
				//TODO:
				
			} else {
				
				tripleInstance = createTriple(sentenceParse);
				
				this.generatorAlias.getTripleList().add(tripleInstance);
				
				RuleEngine.setCachedTriple(tripleInstance);
			}
			
		} catch (Exception excp) {
			
			excp.printStackTrace();
			
			System.out.println("Parsing Error");
			
			System.exit (-1);
		}
	}
	
	public Triple createTriple(final String sentenceParse) {
	
		Triple tripleInstance = null;
		
		/************************** Process parsed sentence ************************/

		String leftPart="";
		String rightPart=""; 
	
		try {
			
			// Break the sentence about the verb
			if (sentenceParse.indexOf("VBZ") != -1) {
				
				leftPart = sentenceParse.substring(0, sentenceParse.indexOf("VBZ"));
			
				rightPart = sentenceParse.substring(sentenceParse.indexOf("VBZ"));
				
			} else if (sentenceParse.indexOf("VBN") != -1) {
				
				leftPart = sentenceParse.substring(0, sentenceParse.indexOf("VBN"));
				
				rightPart = sentenceParse.substring(sentenceParse.indexOf("VBN"));
				
			} else if (sentenceParse.indexOf("VBP") != -1) {
				
				leftPart = sentenceParse.substring(0, sentenceParse.indexOf("VBP"));
				
				rightPart = sentenceParse.substring(sentenceParse.indexOf("VBP"));
				
			} else if (sentenceParse.indexOf("VBD") != -1) {
				
				leftPart = sentenceParse.substring(0, sentenceParse.indexOf("VBD"));
				
				rightPart = sentenceParse.substring(sentenceParse.indexOf("VBD"));
				
			} else if (sentenceParse.indexOf("VP") != -1) {
				
				leftPart = sentenceParse.substring(0, sentenceParse.indexOf("VP"));
				
				rightPart = sentenceParse.substring(sentenceParse.indexOf("VP"));
			}
			
			// Extracting the subject;
			String subject = findSubject(leftPart);
			
			System.out.println("Subject: " + subject);
			
			System.out.println(rightPart);
			
			// Extracting the object
			String object = findObject (rightPart);
			
			System.out.println("Object: " + object);
			
			// Extracting the predicate
			String middlePart="";
			
			if (rightPart.indexOf("VBZ") != -1 && rightPart.lastIndexOf("(") != -1) {
				
				middlePart = rightPart.substring(rightPart.indexOf("VBZ") + 4, rightPart.lastIndexOf("("));
				
			} else if(rightPart.indexOf("VBN") != -1 && rightPart.lastIndexOf("(") != -1) {
				
				middlePart = rightPart.substring(rightPart.indexOf("VBN") + 4, rightPart.lastIndexOf("("));
				
			} else if(rightPart.indexOf("VBP") != -1 && rightPart.lastIndexOf("(") != -1) {
				
				middlePart = rightPart.substring(rightPart.indexOf("VBP") + 4, rightPart.lastIndexOf("("));
				
			} else if(rightPart.indexOf("VBD") != -1 && rightPart.lastIndexOf("(") != -1) {
				
				middlePart = rightPart.substring(rightPart.indexOf("VBD") + 4, rightPart.lastIndexOf("("));
				
			} else if(rightPart.indexOf("VP") != -1 && rightPart.lastIndexOf("(") != -1) {
				
				middlePart = rightPart.substring(rightPart.indexOf("VP") + 4 + 1 + 4, rightPart.lastIndexOf("("));
			}
	
			if (middlePart.equals("") != true && middlePart.indexOf(")") != -1) {
				
				String candidatePredicateOne = middlePart.substring(0, middlePart.indexOf(")"));
				
				System.out.println("candidatePredicateOne: " + candidatePredicateOne);
				
				String candidatePredicateTwo = "";
				
				if (middlePart.indexOf("RB") != -1) {
					
					String temp = middlePart.substring(middlePart.indexOf("RB"));
					
					if (temp.indexOf(")") != -1) {
				
						candidatePredicateTwo = temp.substring(temp.indexOf("RB") + 3, temp.indexOf(")"));
					}
				}
				
				String candidatePredicateThree = "";
				
				if (middlePart.indexOf("VBN") != -1) {
					
					String temp = middlePart.substring(middlePart.indexOf("VBN"));
					
					if (temp.indexOf(")") != -1) {
					
						candidatePredicateThree = temp.substring(temp.indexOf("VBN") + 4, temp.indexOf(")"));
					}
				}
				
				String candidatePredicateFour = "";
				
				if (middlePart.indexOf("NNP") != -1) {
					
					String temp = middlePart.substring(middlePart.indexOf("NNP"));
					
					if (temp.indexOf(")") != -1) {
					
						candidatePredicateFour = temp.substring(temp.indexOf("NNP") + 4, temp.indexOf(")"));
					}
				}
				
				String candidatePredicateFive = "";
				
				if (middlePart.indexOf("JJ") != -1) {
					
					String temp = middlePart.substring(middlePart.indexOf("JJ"));
					
					if (temp.indexOf(")") != -1) {
						
						candidatePredicateFive = temp.substring(temp.indexOf("JJ") + 3, temp.indexOf(")"));
					}
				}
				
				String candidatePredicateSix = "";
				
				if (middlePart.indexOf("NN") != -1) {
					
					String temp = middlePart.substring(middlePart.indexOf("NN"));
					
					if (temp.indexOf(")") != -1) {
					
						candidatePredicateSix = temp.substring(temp.indexOf("NN") + 3, temp.indexOf(")"));
					}
				} 
				
				String candidatePredicateSeven = "";
				
				if (middlePart.indexOf("IN") != -1) {
					
					String temp = middlePart.substring(middlePart.indexOf("IN"));
					
					if (temp.indexOf(")") != -1) {
					
						candidatePredicateSeven = temp.substring(temp.indexOf("IN") + 3, temp.indexOf(")"));
					}
				} 			
				
				System.out.println("Predicate 1: " + candidatePredicateOne);
		
				System.out.println("Predicate 2: " + candidatePredicateTwo);
				
				System.out.println("Predicate 3: " + candidatePredicateThree);
				
				System.out.println("Predicate 4: " + candidatePredicateFour);
				
				System.out.println("Predicate 5: " + candidatePredicateFive);
				
				System.out.println("Predicate 6: " + candidatePredicateSix);
				
				System.out.println("Predicate 7: " + candidatePredicateSeven);
				
				tripleInstance = new Triple (subject, object, new String [] {candidatePredicateOne, candidatePredicateTwo, candidatePredicateThree, candidatePredicateFour, candidatePredicateFive, candidatePredicateSix, candidatePredicateSeven});
				
			} else {
				
				tripleInstance = new Triple (subject, object, null);
			}
		} catch (java.lang.StringIndexOutOfBoundsException excp) {
		
			tripleInstance = new Triple ("", "", null);
		}
		
		return tripleInstance;
	}

	private String findSubject (final String leftPart) {
		
		String subject="";
		
		String mainSubjectCandidate = "";
		
		// extract all part of the name (first, middle, last)
		if (leftPart.indexOf("NNP") != -1) {
			
			mainSubjectCandidate = leftPart.substring(leftPart.indexOf("NNP") + 4);
			
			if (mainSubjectCandidate.indexOf(")") != -1) {

				subject = subject + mainSubjectCandidate.substring(0, mainSubjectCandidate.indexOf(")")) + " ";
			}
		}
		
		// loop over and extract all part of the name (first, middle, last)
		while (mainSubjectCandidate.indexOf("NNP") != -1) {

			mainSubjectCandidate = mainSubjectCandidate.substring(mainSubjectCandidate.indexOf("NNP") + 4);
			
			if (mainSubjectCandidate.indexOf(")") != -1) {
			
				subject = subject + mainSubjectCandidate.substring(0, mainSubjectCandidate.indexOf(")")) + " ";
				
				// return here itself since we've found a good-enough NNP; 
				// chances are that whatever we find below this would be merely 
				// modifying this dominant NNP in some way, which isn't 
				// very significant in the current scope that we're trying to 
				// cover.
				return subject;
			}
		}
		
		if (leftPart.indexOf("NNS") != -1) {
				
			String potentialSubjectCandidate = leftPart.substring(leftPart.indexOf("NNS") + 4);
			
			if (potentialSubjectCandidate.indexOf(")") != -1) {

				subject = subject + potentialSubjectCandidate.substring(0, potentialSubjectCandidate.indexOf(")")) + " ";
			}
		}

		if (leftPart.indexOf("NN ") != -1) {
			
			String potentialSubjectCandidate = leftPart.substring(leftPart.indexOf("NN") + 3);
			
			if (potentialSubjectCandidate.indexOf(")") != -1) {
			
				subject = potentialSubjectCandidate.substring(0, potentialSubjectCandidate.indexOf(")")) + subject;
			}
		}
		
		if (leftPart.lastIndexOf("NP") != -1) {
			
			String potentialSubjectCandidate = leftPart.substring(leftPart.lastIndexOf("NP") + 3);
			
			System.out.println("potentialSubjectCandidate: " + potentialSubjectCandidate);
			
			// Simplistic case of pronominal resolution - use the subject from previous sentence
			
			if (potentialSubjectCandidate.contains("PRP")) {
				
				if (RuleEngine.getCachedTriple() != null) {
					
					subject =  RuleEngine.getCachedTriple().getSubjectResourceString();
				}
			}
		}
	
		if (leftPart.indexOf("JJ") != -1) {
			
			String potentialSubjectCandidate = leftPart.substring(leftPart.indexOf("JJ") + 3);
			
			if (potentialSubjectCandidate.indexOf(")") != -1) {
				
				subject =  potentialSubjectCandidate.substring(0, potentialSubjectCandidate.indexOf(")")) + subject;
			}
		}
		
		if (leftPart.lastIndexOf("CD") != -1) {
			
			String potentialSubjectCandidate = leftPart.substring(leftPart.lastIndexOf("CD") + 3);
			
			if (potentialSubjectCandidate.indexOf(")") != -1) {
				
				subject =  potentialSubjectCandidate.substring(0, potentialSubjectCandidate.indexOf(")")) + subject;
			}
		}
		
		return subject;
	}
	
	private String findObject (final String rightPart) {
		
		String object = null;
		
		if (rightPart.equals("") != true && rightPart.lastIndexOf("(") != -1) {
			
			String objectPart = rightPart.substring(rightPart.lastIndexOf("("));
			
			if (objectPart.indexOf(")") != -1) {

				object = objectPart.substring(objectPart.indexOf (" ") + 1, objectPart.indexOf(")"));
			}
		}
		
		return object;
	}
	
	public ArrayList <String> getPredicateList () {
		
		return predicateList;
	}

	public void setPredicateList (ArrayList <String> predicateList) {
		
		this.predicateList = predicateList;
	}
}

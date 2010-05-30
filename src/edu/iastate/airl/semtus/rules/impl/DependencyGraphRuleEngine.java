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

import edu.iastate.airl.semtus.rules.contract.IRuleEngine;

public class DependencyGraphRuleEngine extends RuleEngine {
	
	private HashMap <String, String> dependencyAnalysisMap;
	
	public DependencyGraphRuleEngine () {
	
		dependencyAnalysisMap = new HashMap <String, String> ();
	}
	
	public ArrayList <String> process(final Object [][] dependencies) {
	
		ArrayList <String> predicateList = new ArrayList <String> ();
		
		/************************** Process dependency graph ************************/
		
		boolean conjFoundLocal = false;
		
		boolean andFoundLocal = false;
		
		for (int index = 0; index < dependencies.length; index++) {
			
			// Case of reification -- handling only upto the 1st level for now
			if (dependencies[index][0].toString().trim().equals(IRuleEngine.CCOMP)) {
				
				// we found a clausal complement 
				
				System.out.println("dependencies[index][1].toString() -- " +  dependencies[index][1].toString());
				
				this.dependencyAnalysisMap.put(IRuleEngine.REIFICATION_FLAG, IRuleEngine.FOUND);
				
				if (predicateList.contains(dependencies[index][1]) == false) {
					
					predicateList.add (dependencies[index][1].toString());
				
				} 
				
				if (predicateList.contains(dependencies[index][2]) == false) {
				
					predicateList.add (dependencies[index][2].toString());
				} 
				
			} else if (dependencies[index][0].toString().trim().equals(IRuleEngine.CONJ)) {
				
				// we found a conjunct
				
				conjFoundLocal = true;
			}
			
			if (dependencies[index][1].toString().trim().contains(IRuleEngine.CONJ_AND) || dependencies[index][2].toString().trim().contains(IRuleEngine.CONJ_AND)) {
				
				// we found an 'and' conjunct
				
				andFoundLocal = true;
			}
		}
		
		// Found a conj_and, set in the hashmap
		if (conjFoundLocal && andFoundLocal) {
		
			this.dependencyAnalysisMap.put(IRuleEngine.CONJ_AND, IRuleEngine.FOUND);
		}
		
		return predicateList;
	}
	
	public HashMap <String, String> getDependencyAnalysisMap() {
		
		return this.dependencyAnalysisMap;
	}
}

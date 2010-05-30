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
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.iastate.airl.semtus.generator.contract.IGenerator;
import edu.iastate.airl.semtus.rdf.Triple;
import edu.iastate.airl.semtus.rules.contract.IRuleEngine;
import edu.stanford.nlp.trees.Tree;

public class RuleEngine implements IRuleEngine {

	private IGenerator generatorAlias = null;
	
	
	/**
	 * Utilized for caching the triple for immediate predesessor 
	 * sentence for pronominal resolution purposes.
	 * 
	 */
	private static Triple cachedTriple = null;

	public RuleEngine () {
		
	}
	
	public RuleEngine (final IGenerator gen) {
		
		generatorAlias = gen; 
	}
	
	public void process(final List <Tree> generatedTrees, final Object [][][] generatedDependencies) {

		try {
			
			Iterator <Tree> iter = generatedTrees.iterator();
			
			DependencyGraphRuleEngine dgRuleEngineInstanceForThisSentence;
			
			int count=0;
			
			while (iter.hasNext()) {
			
				/************************** Process dependency graph ************************/
				
				dgRuleEngineInstanceForThisSentence = new DependencyGraphRuleEngine();
				
				ArrayList <String> predicateList = dgRuleEngineInstanceForThisSentence.process(generatedDependencies[count]);
				
				/************************** Process parsed sentence ************************/
				
				Pattern p = Pattern.compile("\\[[0-9.]*\\]");
				
		        Matcher m = p.matcher("");
		        
		        m.reset (iter.next().toString());
		        
		        String finalParseTree = m.replaceAll("");
		        
		        System.out.println(finalParseTree);
		        
		        ParseTreeRuleEngine ptRuleEngineInstanceForThisSentence = new ParseTreeRuleEngine(generatorAlias);
		        
		        ptRuleEngineInstanceForThisSentence.setPredicateList (predicateList);
		        
		        ptRuleEngineInstanceForThisSentence.process(finalParseTree, dgRuleEngineInstanceForThisSentence.getDependencyAnalysisMap());
		        
				count++;
			}
			
		} catch (Exception excp){
			
			excp.printStackTrace();
			
			System.out.println("Parsing Error");
			
			System.exit (-1);
		}
	}
	
	public static Triple getCachedTriple() {
		
		return cachedTriple;
	}

	public static void setCachedTriple(final Triple cachedTriple) {
		
		RuleEngine.cachedTriple = cachedTriple;
	}
}

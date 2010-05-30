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

package edu.iastate.airl.semtus.rules.contract;

import java.util.List;

import edu.stanford.nlp.trees.Tree;

public interface IRuleEngine {

	public static final int MAX_REIFICATION_COMPLEXITY = 5;
	
	public static final String REIFICATION_FLAG = "reificationFlag";
	
	public static final String CONJ_AND = "and";
	
	public static final String PREP_WITH = "with";
	
	public static final String FOUND = "found";
	
	public static final String NOT_FOUND = "notFound";
	
	public static final String CCOMP = "ccomp";
	
	public static final String CONJ = "conj";
	
	public void process(List <Tree> generatedTrees, Object [][][] generatedDependencies);
}

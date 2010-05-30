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

package edu.iastate.airl.semtus.parser;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.tregex.ParseException;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Grammatical Structure Analyzer
 * 
 * @author Bernard Bou
 * 
 */
public class StructureAnalyzer
{
    /**
     * Grammatical structure factory
     */
    private GrammaticalStructureFactory theFactory;

    // C O N S T R U C T

    /**
     * Constructor
     * 
     */
    public StructureAnalyzer() {
    	
    	TreebankLanguagePack thisTlp = new PennTreebankLanguagePack();
    	theFactory = thisTlp.grammaticalStructureFactory();
    }

    /**
     * Analyze tree
     * 
     * @param thisTree
     *                tree to analyze
     * @return grammatical structure
     */
    public GrammaticalStructure analyzeTree(Tree thisTree) {
    	
    	return theFactory.newGrammaticalStructure(thisTree);
    }
}

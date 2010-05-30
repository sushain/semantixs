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

/*
 * Copyright (c) 2004 Hewlett-Packard Development Company, L.P.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

/*
 * This is the part of the source code for an experimental RDF Graph Visualizer 
 * developed at HP Labs, Palo Alto. Please note that this was never intended to 
 * be of production quality. It was written quickly because we needed an internal
 * solution and is made available in the hope that others might find it useful.
 * We are unfortunately not able to offer any support for this experimental software.
 * 
 * For further information please see:
 *    http://www.hpl.hp.com/personal/Craig_Sayers/rdf/visual
 *  
 */
package edu.iastate.airl.semtus.visualizer;

import java.io.*;
import java.util.*;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.util.iterator.Filter;


public class ModelToSVG extends NodeToSVG {
	
	public ModelToSVG() {
	}
	
	public void visualizeModel( PrintStream out, AnalyzedGraph aGraph, Filter nodeFilter, PageInfo pageInfo) {
		positionedNodes.clear();
		position( aGraph, nodeFilter, pageInfo);
		beginModel(out, pageInfo);
		print(out, pageInfo);
		endModel(out, pageInfo);
		advancePage(pageInfo);
	}
	
	protected void position(AnalyzedGraph aGraph, Filter nodeFilter, PageInfo pageInfo) {

		double allowableMin = -65;
		double allowableMax = 85;

		final int endOfColumnMarker = -1;
		int columns = 3;
		double xSpacing = (allowableMax - allowableMin) / columns;

		double[] columnEnds = new double[columns];

		for (int i = 0; i < columns; i++)
			columnEnds[i] = 0;

		boolean earlyFinish = false;
		// Now we want to order the nodes based on interconnections.
		// Start with nodes that have no outgoing connections and work
		// from there.
		Iterator it = aGraph._nodes.values().iterator();
		while (it.hasNext() && !earlyFinish) {
			AnalyzedGraph.NodeInfo nodeInfo = (AnalyzedGraph.NodeInfo) it.next();
			if (nodeFilter.accept(nodeInfo)) {
				PositionedNode position = new PositionedNode(nodeInfo);
				if( position.nodeInfo.node instanceof Literal)
					continue; // ignore literals
				int column;
				if (nodeInfo.forwardNodes.size() == 0)
					column = 2;
				else if (nodeInfo.backwardNodes.size() == 0)
					column = 0;
				else
					column = 1;

				if (columnEnds[column] != endOfColumnMarker) {
					if (columnEnds[column] > 250) {
						position = new ContinuationNode(nodeInfo);
						position.labelDirection = 0;
						position.label = null;
					} else {
						position.labelDirection = 1;
						position.labelXoffset = 1.2;
						position.labelYoffset = 0;
						position.label = nodeInfo.mediumLabel;						
					}
					position.x = allowableMin + column * xSpacing;
					position.y = columnEnds[column] + pageInfo.ySpacing;
					positionedNodes.add(position);
					if (columnEnds[column] > 250) {
						columnEnds[column] = endOfColumnMarker;
					} else {
						columnEnds[column] = position.y;
				}


				} else if (
					columnEnds[(column + 1) % 3] == endOfColumnMarker
						&& columnEnds[(column + 2) % 3] == endOfColumnMarker) {
					earlyFinish = true;
				}

			}
		}

	}
		
	
	protected void print( PrintStream out, PageInfo pageInfo) {		
		Iterator it = positionedNodes.iterator();
		while(it.hasNext()) {
			PositionedNode position  = (PositionedNode)it.next();
			printNode(out, position, pageInfo);
		}		
	}
	
	
	protected void beginModel(PrintStream out, PageInfo pageInfo) {
		double yOffset = 1; 
		double xOffset= 0;
		out.println("<g transform=\"translate("+(xOffset)+","+(pageInfo.yStart+yOffset)+")\" >");
	}
	
	
	protected void endModel(PrintStream out, PageInfo pageInfo) {
		out.println("</g>");
	}
		
}

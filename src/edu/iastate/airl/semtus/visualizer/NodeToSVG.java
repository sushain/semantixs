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
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ConcatenatedIterator;
import com.hp.hpl.jena.util.iterator.Filter;


public class NodeToSVG {
	protected ArrayList positionedNodes = new ArrayList();
	protected ArrayList positionedArcs = new ArrayList();

	/** A node-centric visualizer that renders RDF nodes using SVG.
	 */
	public NodeToSVG() {
	}

	/** Start the visualization document.
	 * @param out PrintStream for the visualization.
	 * @param pageInfo Page formatting information.
	 */
	public void visualizeStart(PrintStream out, PageInfo pageInfo) {
		beginDoc(out, pageInfo);
	}

	/** End the visualization document.
	 * @param out PrintStream for the visualization.
	 * @param pageInfo Page formatting information.
	 */
	public void visualizeEnd(PrintStream out, PageInfo pageInfo) {
		endDoc(out, pageInfo);
	}

	/** Visualize a sub-heading.
	 * @param out PrintStream for the visualization.
	 * @param pageInfo Page formatting information.
	 * @param text Subheading text.
	 */
	public void visualizeSubHeading(PrintStream out, PageInfo pageInfo, String text) {
		printSubHeading(out, -60, pageInfo.yStart + 1.2*pageInfo.subheadingFontSize, text,
				pageInfo.subheadingFontSize);
		pageInfo.lastItemSize = pageInfo.subheadingFontSize-0.5*pageInfo.ySpacing;
		advancePage(pageInfo);
	}

	/** Visualize a vertical continuation mark.
	 * @param out PrintStream for the visualization.
	 * @param pageInfo Page formatting information.
	 */
	public void visualizeVerticalContinuation(PrintStream out, PageInfo pageInfo) {
		printVerticalContinuation(out, 9, pageInfo.yStart - 0.5);
		pageInfo.lastItemSize = 1;
		advancePage(pageInfo);
	}

	/** Visualize a particular node.
	 * @param out PrintStream for the visualization.
	 * @param aGraph Analyzed Graph to use.
	 * @param node Node to visualize.
	 * @param nodeInfoFilter A filter to accept/reject nodes for this visualization.
	 * @param arcInfoFilter A filter to accept/reject arcs for this visualization.
	 * @param pageInfo Page formatting information.
	 */
	public void visualizeNode(PrintStream out, AnalyzedGraph aGraph, RDFNode node,
			Filter nodeInfoFilter, Filter arcInfoFilter, PageInfo pageInfo) {
		AnalyzedGraph.NodeInfo nodeInfo = (AnalyzedGraph.NodeInfo) aGraph._nodes.get(node);
		if (nodeInfo == null)
			return;
		else
			visualizeNodeInfo(out, aGraph, nodeInfo, nodeInfoFilter, arcInfoFilter, pageInfo);
	}

	/** Visualize a particular node.
	 * @param out PrintStream for the visualization.
	 * @param aGraph Analyzed Graph to use.
	 * @param nodeInfo Node Info from within aGraph.
	 * @param nodeInfoFilter A filter to accept/reject nodes for this visualization.
	 * @param arcInfoFilter A filter to accept/reject arcs for this visualization.
	 * @param pageInfo Page formatting information.
	 */
	public void visualizeNodeInfo(PrintStream out, AnalyzedGraph aGraph,
			AnalyzedGraph.NodeInfo nodeInfo, Filter nodeInfoFilter, Filter arcInfoFilter,
			PageInfo pageInfo) {
		positionedNodes.clear();
		positionedArcs.clear();
		PositionedNode positionedNode = subgraph(aGraph, nodeInfo, nodeInfoFilter, arcInfoFilter,
				pageInfo);
		position(aGraph, positionedNode, pageInfo);
		beginNode(out, pageInfo);
		print(out, pageInfo);
		endNode(out, pageInfo);
		advancePage(pageInfo);
	}

	/** Advance the visualized page.
	 * @param pageInfo
	 */
	public void advancePage(PageInfo pageInfo) {
		advancePage(pageInfo, 1.0);
	}

	/** Advance the visualized page by a specified fractional amount.
	 * @param pageInfo
	 * @param fractionalSpacing the number of blank lines to advance.  For example 1.5 is one-and-a-half times the usual vertical spacing.
	 */
	public void advancePage(PageInfo pageInfo, double fractionalSpacing) {
		pageInfo.yStart += pageInfo.lastItemSize + pageInfo.ySpacing * fractionalSpacing;
		pageInfo.lastItemSize = 0.0;
	}
	
	/** Interface for converting a Jena Resource to a String for display.
	 */
	public interface ResourceToString {
		public String convert(Resource resource);

		public String convert(Resource resource, int length);
	}
	
	/** Collect together all the information which alters page formatting.
	 * 
	 * Use these parameters to modify the layout and style of the visualization.
	 * For now, all the controls are just public parameters - this is a
	 * quick and easy fix - in the future some form of style sheet might be
	 * preferable.
	 */
	public static class PageInfo {
		
		static double fontScale = 1.0;
		
		double lastItemSize = 0;

		public double yStart = 0;
		public double ySpacing = 2.6*fontScale; 
		public double xSpacing = 32*fontScale; 
		public int maxForwardArcs = 2;
		public int maxBackArcs = 1;
		public int maxLiteralLines = 10;
		public int maxLiteralLineLength = (int)(55/fontScale); 
		public double lineSpacing = 2.2*fontScale; 
		public double baseFontSize = 1.65*fontScale; 
		public double subheadingFontSize = 2.1*fontScale; 
		public Set literalsToHighlight = null;
		public String textToHightlight = null;
		public boolean ignoreTagsInLiterals = true; // ignore anything between &lt; and &gt;
		public ResourceToString resourceToHREF = null;
		public ResourceToString propertyToHREF = null;

		public PageInfo() {
		}
	}

	protected PositionedNode subgraph(AnalyzedGraph aGraph, AnalyzedGraph.NodeInfo nodeInfo,
			Filter nodeInfoFilter, Filter arcInfoFilter, PageInfo pageInfo) {
		PositionedNode position;
		position = new PositionedNode(nodeInfo);
		positionedNodes.add(position);
		if (pageInfo.maxForwardArcs > 0)
			subgraph(aGraph, position, nodeInfoFilter, arcInfoFilter, 1, 1, 22, pageInfo);
		if (pageInfo.maxBackArcs > 0)
			subgraph(aGraph, position, nodeInfoFilter, arcInfoFilter, 1, -1, 22, pageInfo);
		// First subgraph the circular arcs
		Iterator arcs = nodeInfo.circularArcs.iterator();
		while (arcs.hasNext()) {
			AnalyzedGraph.ArcInfo arcInfo = (AnalyzedGraph.ArcInfo) arcs.next();
			if (arcInfoFilter.accept(arcInfo)) {
				PositionedArc positionedArc = new PositionedCircularArc(arcInfo);
				positionedArcs.add(positionedArc);
				positionedArc.subjectPosition = position;
				positionedArc.subjectPosition = position;
				position.circularArcPositions.add(positionedArc);
			}
		}
		return position;
	}

	protected void subgraph(AnalyzedGraph aGraph, PositionedNode startNode, Filter nodeInfoFilter,
			Filter arcInfoFilter, int arcIndex, int direction, int maxArcs, PageInfo pageInfo) {
		int arcsPerNodeAtNextLevel;
		Iterator arcs;
		if (direction > 0) {
			if (arcIndex > 1)
				arcs = new ConcatenatedIterator(startNode.nodeInfo.departingArc.iterator(),
						startNode.nodeInfo.circularArcs.iterator());
			else
				arcs = startNode.nodeInfo.departingArc.iterator();
			arcsPerNodeAtNextLevel = 4
					+ maxArcs
					/ (1 + startNode.nodeInfo.departingArc.size() + startNode.nodeInfo.circularArcs
							.size());
		} else {
			arcs = startNode.nodeInfo.arrivingArcs.iterator();
			arcsPerNodeAtNextLevel = 4 + maxArcs / (1 + startNode.nodeInfo.arrivingArcs.size());
		}
		int arcCount = 0;
		HashMap reachableNodes = new HashMap();
		boolean stopEarly = false;
		while (arcs.hasNext() && !stopEarly) {
			AnalyzedGraph.ArcInfo arcInfo = (AnalyzedGraph.ArcInfo) arcs.next();
			AnalyzedGraph.NodeInfo endNode;
			if (direction > 0)
				endNode = arcInfo.end;
			else
				endNode = arcInfo.start;
			if (arcInfoFilter.accept(arcInfo) && nodeInfoFilter.accept(endNode)) {
				arcCount++;
				boolean needToExpandNode;
				PositionedArc positionedArc;
				positionedArc = new PositionedArc(arcInfo);
				positionedArcs.add(positionedArc);
				PositionedNode positionedEndNode = (PositionedNode) reachableNodes.get(endNode);
				if (positionedEndNode == null) {
					if (arcCount >= maxArcs) {
						positionedEndNode = new ContinuationNode(endNode);
						needToExpandNode = false;
						stopEarly = true;
					} else {
						positionedEndNode = new PositionedNode(endNode);
						needToExpandNode = true;
					}
					reachableNodes.put(endNode, positionedEndNode);
					positionedNodes.add(positionedEndNode);
				} else {
					needToExpandNode = false;
				}
				if (direction > 0) {
					positionedArc.subjectPosition = startNode;
					positionedArc.objectPosition = positionedEndNode;
				} else {
					positionedArc.objectPosition = startNode;
					positionedArc.subjectPosition = positionedEndNode;
				}
				positionedArc.direction = direction;
				if (direction > 0) {
					startNode.forwardArcPositions.add(positionedArc);
					positionedEndNode.backwardArcPositions.add(positionedArc);
					if (arcIndex < pageInfo.maxForwardArcs && needToExpandNode)
						subgraph(aGraph, positionedEndNode, nodeInfoFilter, arcInfoFilter,
								arcIndex + 1, direction, arcsPerNodeAtNextLevel, pageInfo);
				} else {
					startNode.backwardArcPositions.add(positionedArc);
					positionedEndNode.forwardArcPositions.add(positionedArc);
					if (arcIndex < pageInfo.maxBackArcs && needToExpandNode)
						subgraph(aGraph, positionedEndNode, nodeInfoFilter, arcInfoFilter,
								arcIndex + 1, direction, arcsPerNodeAtNextLevel, pageInfo);
				}
			}
		}
	}

	protected void position(AnalyzedGraph aGraph, PositionedNode startNodePosition,
			PageInfo pageInfo) {
		startNodePosition.x = 0.0;
		startNodePosition.y = 2.0;
		double arrivingY = position(aGraph, startNodePosition, 1, 1, pageInfo);
		double departingY = position(aGraph, startNodePosition, 1, -1, pageInfo);
		// Now position the circular arcs
		double startY = 0.0;
		double ySpacing = pageInfo.ySpacing;
		// special case, if there's only one circular arc we can use a
		// simlified view
		if (startNodePosition.circularArcPositions.size() == 1
				&& startNodePosition.forwardArcPositions.size() == 0
				&& startNodePosition.backwardArcPositions.size() == 0) {
			startY = startNodePosition.y;
		} else {
			startY = Math.max(arrivingY, departingY) + 2.5 * ySpacing;
			if (startY < 5.5 * pageInfo.ySpacing)
				startY = 5.5 * pageInfo.ySpacing;
		}
		double intermediateY = startY - 2.5 * pageInfo.ySpacing;
		Iterator arcs = startNodePosition.circularArcPositions.iterator();
		while (arcs.hasNext()) {
			PositionedCircularArc positionedArc = (PositionedCircularArc) arcs.next();
			positionedArc.y = startY;
			positionedArc.intermediateY = intermediateY;
			positionedArc.label = positionedArc.arcInfo.shortLabel;
			if (positionedArc.y + 0.5 * pageInfo.ySpacing > pageInfo.lastItemSize)
				pageInfo.lastItemSize = positionedArc.y + 0.5 * pageInfo.ySpacing;
			startY = positionedArc.y + ySpacing;
		}
	}

	protected double position(AnalyzedGraph aGraph, PositionedNode startNode, int arcIndex,
			int direction, PageInfo pageInfo) {
		double startX = startNode.x + pageInfo.xSpacing * direction;
		double startY = startNode.y;
		double endY = startY;
		if (startNode instanceof ContinuationNode) {
			startNode.labelDirection = direction;
			startNode.labelXoffset = 0;
			startNode.labelYoffset = 0;
			return endY + pageInfo.ySpacing * 0.1;
		}
		if (startNode.nodeInfo.node instanceof Literal) {
			startNode.labelDirection = (direction == 0) ? 1 : direction;
			startNode.labelXoffset = 1.2 * startNode.labelDirection;
			startNode.labelYoffset = 0.0;
			startNode.label = null;
			int maxLines = pageInfo.maxLiteralLines;
			int maxLineLength = pageInfo.maxLiteralLineLength;
			if (arcIndex > 2) {
				maxLines = (int) (pageInfo.maxLiteralLines / 3 + 0.7);
				maxLineLength = (int) (maxLineLength * 0.4);
			}
			startNode.textLines = formatLiteralNodeInfo(startNode.nodeInfo, pageInfo, maxLines,
					maxLineLength);
			if (startNode.textLines.size() > 1)
				endY += pageInfo.lineSpacing * (startNode.textLines.size() - 1);
			return endY + pageInfo.ySpacing * 0.1;
		}
		// label the node based on direction and incoming and outgoing arcs
		List arcs = null;
		boolean forceAboveNode = false;
		if( pageInfo.maxBackArcs == 0 || arcIndex == 2)
			startNode.label = startNode.nodeInfo.mediumLabel;
		else if ( arcIndex < 2) {
			startNode.label = startNode.nodeInfo.longLabel;
			if( startNode.label.length() > 40)
					forceAboveNode = true;
		}
		else
			startNode.label = startNode.nodeInfo.shortLabel;
		if (direction > 0) {
			if (startNode.forwardArcPositions.size() == 0 && !forceAboveNode) {
				startNode.labelDirection = 1;
				startNode.labelXoffset = 1.2;
				startNode.labelYoffset = 0;
			} else {
				startNode.labelDirection = 0;
				startNode.labelXoffset = 0;
				startNode.labelYoffset = -(0.5 + pageInfo.baseFontSize);
			}
			arcs = startNode.forwardArcPositions;
		} else if (direction < 0) {
			if (startNode.forwardArcPositions.size() == 0 && !forceAboveNode) {
				startNode.labelDirection = 1;
				startNode.labelXoffset = 1.2;
				startNode.labelYoffset = 0;
			} else if (startNode.backwardArcPositions.size() == 0 && !forceAboveNode) {
				startNode.labelDirection = -1;
				startNode.labelXoffset = -1.2;
				startNode.labelYoffset = 0;
			} else {
				startNode.labelDirection = 0;
				startNode.labelXoffset = 0;
				startNode.labelYoffset = -(0.5 + pageInfo.baseFontSize);
			}
			arcs = startNode.backwardArcPositions;
		}
		// special case, if the label on this node is long then
		// we need to lower the subsequent arcs a bit
		if (forceAboveNode || (startNode.nodeInfo.longLabel.length() > 20 && startNode.forwardArcPositions.size() > 0
				&& startNode.backwardArcPositions.size() > 0))
			startY += 0.75 * pageInfo.baseFontSize;
		SortedSet reachableNodes = new TreeSet();
		Iterator it = arcs.iterator();
		while (it.hasNext()) {
			PositionedArc positionedArc = (PositionedArc) it.next();
			if (direction > 0) {
				if (!reachableNodes.contains(positionedArc.objectPosition))
					reachableNodes.add(positionedArc.objectPosition);
			} else {
				if (!reachableNodes.contains(positionedArc.subjectPosition))
					reachableNodes.add(positionedArc.subjectPosition);
			}
		}
		Iterator nodes = reachableNodes.iterator();
		while (nodes.hasNext()) {
			PositionedNode position = (PositionedNode) nodes.next();
			position.x = startX;
			position.y = startY;
			Iterator arcIt;
			if (direction > 0) {
				arcIt = position.backwardArcPositions.iterator();
			} else {
				arcIt = position.forwardArcPositions.iterator();
			}
			if (position.forwardArcPositions.size() > 0 && position.backwardArcPositions.size() > 0) {
				// allow extra space for the node label (it must go above the
				// node)
				startY += pageInfo.baseFontSize + 0.75;
				position.y = startY;
			} else {
				if (!(position.nodeInfo.node instanceof Literal)) {
					// for non-literals place node in the middle of arcs to it
					// (for literals
					// its better to have the node at the top so there's more
					// room for multi-line text).
					if (direction > 0) {
						position.y = position.y
								+ ((0.5 * (position.backwardArcPositions.size() - 1)) * 0.9 * pageInfo.ySpacing);
					} else {
						position.y = position.y
								+ ((0.5 * (position.forwardArcPositions.size() - 1)) * 0.9 * pageInfo.ySpacing);
					}
				}
			}
			int count = 0;
			while (arcIt.hasNext()) {
				PositionedArc positionedArc = (PositionedArc) arcIt.next();
				positionedArc.y = startY + 0.9 * pageInfo.ySpacing * count++;
				positionedArc.direction = direction;
				if (position instanceof ContinuationNode)
					positionedArc.label = "";
				else
					positionedArc.label = positionedArc.arcInfo.shortLabel;
			}
			endY = startY + (count - 1) * pageInfo.ySpacing;
			endY = Math.max(endY, position(aGraph, position, arcIndex + 1, direction, pageInfo));
			startY = endY + pageInfo.ySpacing;
		}
		endY = endY + pageInfo.ySpacing * 0.1;
		if (endY > pageInfo.lastItemSize)
			pageInfo.lastItemSize = endY;
		return endY;
	}

	protected void print(PrintStream out, PageInfo pageInfo) {
		// print arcs
		Iterator arcsToPrint = positionedArcs.iterator();
		while (arcsToPrint.hasNext()) {
			PositionedArc positionedArc = (PositionedArc) arcsToPrint.next();
			printArc(out, positionedArc, pageInfo);
		}
		// print nodes
		Iterator nodesToPrint = positionedNodes.iterator();
		while (nodesToPrint.hasNext()) {
			PositionedNode positionedNode = (PositionedNode) nodesToPrint.next();
			printNode(out, positionedNode, pageInfo);
		}
	}

	protected void printArc(PrintStream out, PositionedArc positionedArc, PageInfo pageInfo) {
		PositionedNode start, end;
		String href = pageInfo.propertyToHREF.convert(positionedArc.arcInfo.predicate);
		if (positionedArc instanceof PositionedCircularArc) {
			printCircularArc(out, positionedArc.subjectPosition.x, positionedArc.subjectPosition.y,
					((PositionedCircularArc) positionedArc).y, positionedArc.y, pageInfo.xSpacing,
					pageInfo.ySpacing, positionedArc.label, pageInfo.baseFontSize);
		} else if (positionedArc.direction > 0) {
			start = positionedArc.subjectPosition;
			end = positionedArc.objectPosition;
			printArc(out, start.x, start.y, positionedArc.y, end.x, end.y, positionedArc.direction,
					positionedArc.label, href, pageInfo.baseFontSize);
		} else {
			start = positionedArc.objectPosition;
			end = positionedArc.subjectPosition;
			printArc(out, start.x, start.y, positionedArc.y, end.x, end.y, positionedArc.direction,
					positionedArc.label, href, pageInfo.baseFontSize);
		}
	}

	protected void printNode(PrintStream out, PositionedNode positionedNode, PageInfo pageInfo) {
		if (positionedNode instanceof ContinuationNode) {
			printContinuationNode(out, positionedNode.x, positionedNode.y,
					positionedNode.labelDirection);
		} else {
			AnalyzedGraph.NodeInfo nodeInfo = positionedNode.nodeInfo;
			if (nodeInfo.node instanceof Resource) {
				String href = pageInfo.resourceToHREF.convert((Resource) nodeInfo.node);
				printNode(out, positionedNode.x, positionedNode.y, href, pageInfo.baseFontSize);
				printLeaf(out, positionedNode.x + positionedNode.labelXoffset, positionedNode.y
						+ positionedNode.labelYoffset, positionedNode.labelDirection,
						positionedNode.label, pageInfo.baseFontSize);
			} else {
				printLiteral(out, positionedNode.x + positionedNode.labelXoffset, positionedNode.y
						+ positionedNode.labelYoffset, positionedNode.textLines,
						pageInfo.baseFontSize, pageInfo.lineSpacing);
			}
		}
	}

	protected void beginDoc(PrintStream out, PageInfo pageInfo) {
		out.println("<?xml version=\"1.0\" standalone=\"no\"?>\n"
				+ "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.0//EN\"\n"
				+ "\"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\">\n"
				+ "<svg xmlns=\"http://www.w3.org/2000/svg\"\n"
				+ "xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n"
				+ "width=\"100%\" height=\"200%\" viewBox=\"-75 0 160 320\" preserveAspectRatio=\"xMidYMin slice\"\n"
				+ " xml:space=\"preserve\">\n"
				+ "<defs>\n"
				+ "<linearGradient id=\"backfill\" x1=\"0%\" y1=\"0%\" x2=\"0%\" y2=\"100%\">\n"
				+ "<stop offset=\"0%\" style=\"stop-color:#DFDFDF;stop-opacity:1\"/>\n"
				+ "<stop offset=\"100%\" style=\"stop-color:#AFAFAF;stop-opacity:1\"/>\n"
				+ "</linearGradient>\n"
				+ "</defs>\n"
				+ "<defs>\n"
				+ "<radialGradient id=\"grey_blue\" cx=\"50%\" cy=\"50%\" r=\"50%\" fx=\"50%\" fy=\"50%\">\n"
				+ "<stop offset=\"0%\" style=\"stop-color:#6F6fAF\"/>\n"
				+ "<stop offset=\"100%\" style=\"stop-color:#0000FF\"/>\n"
				+ "</radialGradient>\n" 
				+ "</defs>");
	}

	protected void beginNode(PrintStream out, PageInfo pageInfo) {
		double yOffset = 0;
		double xOffset = -10;
		out.println("<g transform=\"translate(" + (xOffset) + "," + (pageInfo.yStart + yOffset)+ ")\" >");
	}

	protected void printLeaf(PrintStream out, double x, double y, int direction, String label, double fontSize) {
		out.println("<g transform=\"translate(" + (x) + "," + (y + 0.5) + ")\" ");
		if (direction == 0)
			out.println("text-anchor=\"middle\" ");
		else if (direction > 0)
			out.println("text-anchor=\"start\"  ");
		else
			out.println("text-anchor=\"end\" ");
		out.println("font-size=\"" + fontSize
				+ "\"   fill=\"blue\" stroke=\"none\" font-family=\"Verdana\">");

		int posn = label.indexOf(":");
		// see if we can conveniently split the label to align the ":" with the
		// center
		if (direction == 0 && label.length() > 10 && posn > 0 && posn < label.length() / 2) {
			out.println(" <text x=\"0\" y=\"0\" text-anchor=\"end\">"
				+ label.substring(0, posn + 1) + "</text>\n"
				+ " <text x=\"0\" y=\"0\" text-anchor=\"start\">" + label.substring(posn + 1)
				+ "</text>");
		} else {
			out.println(" <text  x=\"0\" y=\"0\">" + label + "</text>");
		}
		out.println("</g>");
	}

	protected void printLiteral(PrintStream out, double x, double y, ArrayList textLines, double fontSize, double lineSpacing) {
		out.println("<g transform=\"translate(" + (x) + "," + (y + 0.5) + ")\" "
				+ "text-anchor=\"start\" font-size=\"" + fontSize + "\" "
				+ "fill=\"blue\" stroke=\"none\" font-family=\"Verdana\">\n");
		// TODO investigate the next version of SVG and check support for
		// flowing text
		double yStart = 0;
		Iterator it = textLines.iterator();
		while (it.hasNext()) {
			out.println("<text x=\"0\" y=\"" + yStart + "\">" + it.next() + "</text>");
			yStart += lineSpacing;
		}
		out.println("</g>");
	}

	protected void printNode(PrintStream out, double x, double y, String label, double fontSize) {
		out.println("<g transform=\"translate(" + x + "," + y + ")\" fill=\"blue\" "
				+ "stroke=\"none\" font-size=\"" + fontSize + "\" font-family=\"Verdana\">");
		if (label != null) {
			out.println("   <a xlink:href=\"" + label + "\">");
		}
		out.println("  <circle  cx=\"0\" cy=\"0\" r=\"0.8\" style=\"fill:url(#grey_blue)\"/>");
		if (label != null)
			out.println("  </a>");
		out.println("</g>");
	}

	protected void printVerticalContinuation(PrintStream out, double x, double y) {
		out.println("<g transform=\"translate(" + x + "," + y + ")\" fill=\"blue\" "
				+ "stroke=\"none\" font-size=\"1.5\" font-family=\"Verdana\">\n"
				+ "  <circle  cx=\"0\" cy=\"0\" r=\"0.25\" style=\"fill:url(#grey_blue)\"/>\n"
				+ "  <circle  cx=\"0\" cy=\"1\" r=\"0.25\" style=\"fill:url(#grey_blue)\"/>\n"
				+ "  <circle  cx=\"0\" cy=\"2\" r=\"0.25\" style=\"fill:url(#grey_blue)\"/>\n"
				+ "</g>");
	}

	protected void printContinuationNode(PrintStream out, double x, double y, int direction) {
		if( direction == 0)
			printVerticalContinuation(out,x,y);
		else
			out.println("<g transform=\"translate(" + x + "," + y + ")\" fill=\"blue\""
				+ " stroke=\"none\" font-size=\"1.5\" font-family=\"Verdana\">\n"
				+ "  <circle  cx=\"" + (direction * 1)
				+ "\" cy=\"0\" r=\"0.3\" style=\"fill:url(#grey_blue)\"/>\n" + "  <circle  cx=\""
				+ (direction * 2) + "\" cy=\"0\" r=\"0.3\" style=\"fill:url(#grey_blue)\"/>\n"
				+ "  <circle  cx=\"" + (direction * 3)
				+ "\" cy=\"0\" r=\"0.3\" style=\"fill:url(#grey_blue)\"/>\n" + "</g>");
	}

	protected void printArc(PrintStream out, double sx, double sy, double labelY, double ex,
			double ey, int direction, String label, String href, double fontSize) {
		double labelX = (ex + sx) * 0.5 + direction * 4.0;
		double xspacing = Math.abs(ex - sx) - 8;
		out.println("<g >");
		out.println("  <path d=\"M" + sx + "," + sy + " C" + (sx + direction * 0.3 * xspacing)
				+ "," + sy + " " + (labelX - direction * 8 - direction * 0.3 * xspacing) + ","
				+ labelY + " " + (labelX - direction * 8) + "," + labelY + " " + "L"
				+ (labelX + direction * 8) + "," + labelY + "C"
				+ (labelX + direction * 8 + direction * 0.15 * xspacing) + "," + labelY + " "
				+ (ex - direction * 0.15 * xspacing) + "," + ey + " " + ex + "," + ey
				+ "\" fill=\"none\" stroke=\"blue\" stroke-width=\"0.2\" />");
		if (href != null) {
			out.println("   <a xlink:href=\"" + href + "\">");
		}
		out.println("  <text  x=\"" + labelX + "\" y=\"" + (labelY - 0.3)
				+ "\" text-anchor=\"middle\" fill=\"blue\"" + " stroke=\"none\" font-size=\""
				+ fontSize + "\" font-family=\"Verdana\">" + label + "</text>");
		if (href != null)
			out.println("  </a>");
		out.println("</g>");
	}

	protected void printSubHeading(PrintStream out, double sx, double sy, String label,
			double fontSize) {
		out.println("<g >\n" + "  <text  x=\"" + sx + "\" y=\"" + sy
				+ "\" text-anchor=\"start\" fill=\"blue\"" + " stroke=\"none\" font-size=\""
				+ fontSize + "\" font-family=\"Verdana\">" + label + "</text>" + "</g>");
	}

	protected void printCircularArc(PrintStream out, double sx, double sy, double firstY,
			double labelY, double xSpacing, double ySpacing, String label, double fontSize) {
		double labelX = 0;
		double arcY = firstY - 2 * ySpacing;
		double upperXspacing = 2.5 + (firstY - sy) / ySpacing * 0.09;
		double midXspacing = 4 + (firstY - sy) / ySpacing * 0.09;
		double labelSpacing = 8.5;
		out.println("<g >");
		if (labelY <= sy) {
			double endX = xSpacing + sx;
			labelX = sx + xSpacing * 0.5 + 4.0;
			labelY = sy;
			out.println("  <path d=\"M" + sx + "," + sy + " L" + endX + "," + (sy) + " C"
					+ (endX + ySpacing / 2) + "," + (sy) + " " + (endX + ySpacing / 2)
					+ "," + (sy + ySpacing / 2) + " " + (endX) + "," + (sy + ySpacing / 2)
					+ " " + " L" + (sx + ySpacing / 2) + "," + (sy + ySpacing / 2) + " C"
					+ (sx) + "," + (sy + ySpacing / 2) + " " + (sx) + ","
					+ (sy - ySpacing / 2) + " " + (sx) + "," + (sy) + " "
					+ "\" fill=\"none\" stroke=\"blue\" stroke-width=\"0.2\" />");
		} else {
			out.println("  <path d=\"M" + sx + "," + sy + " C" + (sx + upperXspacing) + "," + sy
					+ " " + (labelX + midXspacing + 1 - 0.5 * ySpacing) + ","
					+ (arcY - 3.5 * ySpacing) + " " + (labelX + midXspacing + 1) + ","
					+ (arcY - 1.5 * ySpacing) + " " + "C"
					+ (labelX + midXspacing + 1 + 0.5 * ySpacing + (labelY - arcY) * 0.05) + ","
					+ (arcY + 0.5 * ySpacing) + " "
					+ (labelX + labelSpacing + 1 * ySpacing + (labelY - arcY) + 0.05) + ","
					+ (labelY) + " " + (labelX + labelSpacing) + "," + (labelY) + " " + "L"
					+ (labelX) + "," + labelY + " " + "M" + sx + "," + sy + " C"
					+ (sx - upperXspacing) + "," + sy + " "
					+ (labelX - midXspacing - 1 + 0.5 * ySpacing) + "," + (arcY - 3.5 * ySpacing)
					+ " " + (labelX - midXspacing - 1) + "," + (arcY - 1.5 * ySpacing) + " " + "C"
					+ (labelX - midXspacing - 1 - 0.5 * ySpacing - (labelY - arcY) * 0.05) + ","
					+ (arcY + 0.5 * ySpacing) + " "
					+ (labelX - labelSpacing - 1 * ySpacing - (labelY - arcY) + 0.05) + ","
					+ (labelY) + " " + (labelX - labelSpacing) + "," + (labelY) + " " + "L"
					+ (labelX) + "," + labelY + " "
					+ "\" fill=\"none\" stroke=\"blue\" stroke-width=\"0.2\" />");
		}
		out.println("  <text  x=\"" + labelX + "\" y=\"" + (labelY - 0.2)
				+ "\" text-anchor=\"middle\" fill=\"blue\"" + "  stroke=\"none\" font-size=\""
				+ fontSize + "\" font-family=\"Verdana\">" + label + "</text>");
		out.println("</g>");
	}

	protected void endNode(PrintStream out, PageInfo pageInfo) {
		out.println("</g>");
	}

	protected void endDoc(PrintStream out, PageInfo pageInfo) {
		out.println("</svg>");
	}

	protected static String shortenString(String string, int length) {
		if (string.length() > length)
			return string.substring(0, length - 3) + "...";
		else
			return string;
	}

	protected ArrayList formatLiteralNodeInfo(AnalyzedGraph.NodeInfo literalNodeInfo,
			PageInfo pageInfo, int maxLines, int maxLineLength) {
		Literal literal = (Literal) literalNodeInfo.node;
		String literalText = literal.getLexicalForm();
		if (literal.getLanguage() != null && literal.getLanguage().length() > 0)
			literalText = literalText + " (lang=" + literal.getLanguage() + ")";
		if (literal.getDatatypeURI() != null)
			literalText = literalText + " (type=" + literal.getDatatypeURI() + ")";
		if (pageInfo.literalsToHighlight != null
				&& pageInfo.literalsToHighlight.contains(literalNodeInfo))
			return formatMultipleLines(literalText, pageInfo.textToHightlight, maxLines,
					maxLineLength, pageInfo);
		else
			return formatMultipleLines(literalText, null, maxLines, maxLineLength, pageInfo);
	}

	static protected ArrayList formatMultipleLines(String content, String searchText, int maxLines,
			int lineLength, PageInfo pageInfo) {
		// format the literal into lines, keep an eye out for the search text
		int firstLineWithSearchText = -1;
		ArrayList result = new ArrayList();
		boolean partialMatch = false;
		if (searchText != null && searchText.endsWith("*")) {
			searchText = searchText.substring(0, searchText.length() - 1);
			partialMatch = true;
		}
		// This isn't perfect since we don't know exactly how big each letter
		// is,
		// but it doesn't look too bad
		// TODO investigate the next version of SVG which supports flowing text
		int visibleChars = 0;
		boolean insideTag = false;
		StringTokenizer tokenizer = new StringTokenizer(content, " \n\t\r,():-\"/\\!?$@&;", true);
		StringBuffer line = new StringBuffer();
		String previousToken = null;
		while (tokenizer.hasMoreTokens() && result.size() < maxLines) {
			String token = tokenizer.nextToken();
			if (token.compareTo("&") == 0 && pageInfo.ignoreTagsInLiterals == true
					&& tokenizer.hasMoreTokens()) {
				token = tokenizer.nextToken();
				if (token.startsWith("lt") && tokenizer.hasMoreTokens()) {
					token = tokenizer.nextToken();
					if (token.compareTo(";") == 0) {
						insideTag = true;
						continue;
					} else
						token = "&lt" + token;
				} else if (token.startsWith("gt") && tokenizer.hasMoreTokens()) {
					token = tokenizer.nextToken();
					if (token.compareTo(";") == 0) {
						insideTag = false;
						continue;
					} else
						token = "&lt" + token;
				} else
					token = "&amp;" + token;
			}
			if (insideTag)
				continue; // ignore tokens inside tag
			if ("\n\r\t".indexOf(token) != -1) {
				token = " "; // ignore manual formatting
			}
			if (visibleChars + token.length() > lineLength + 15
					&& previousToken.compareTo("&") != 0) {
				// this single token is too big so truncate it
				line
						.append(token.substring(0, Math.max(0, lineLength + 10 - visibleChars))
								+ "...");
				visibleChars = lineLength + 1; // force a line break
				token = " ";
			}
			if (visibleChars > lineLength && " ,/\\!?.".indexOf(token) != -1) {
				// break line here
				line.append(token);
				token = "";
				result.add(line.toString());
				line = new StringBuffer();
				visibleChars = 0;
				// Special case, if we're looking for a particular token and
				// haven't
				// found it yet and it occurs later, then we need to remove
				// some of the
				// earlier lines to make space.
				if (result.size() == maxLines) {
					if (searchText != null && firstLineWithSearchText == -1) {
						result.remove(0);
						if (result.size() > 0)
							result.set(0, "..." + result.get(0).toString());
						else {
							line.append("... ");
							visibleChars = 4;
						}
					}
					continue;
				}
			}
			if ((visibleChars == 0 || previousToken.compareTo(" ") == 0)
					&& token.compareTo(" ") == 0)
				continue; // ignore spaces at the beginning of a line and
						  // duplicate spaces
			if (searchText != null) {
				if (token.compareToIgnoreCase(searchText) == 0) {
					// found a match
					line.append("<tspan fill=\"red\" >" + token + "</tspan>");
					firstLineWithSearchText = result.size();
				} else if (partialMatch == true && token.toLowerCase().startsWith(searchText)) {
					// found a match
					line.append("<tspan fill=\"red\" >" + token.substring(0, searchText.length())
							+ "</tspan>" + token.substring(searchText.length()));
					firstLineWithSearchText = result.size();
				} else {
					line.append(token);
				}
			} else {
				line.append(token);
			}
			visibleChars += token.length();
			previousToken = token;
		}
		if (line.length() > 0) {
			result.add(line.toString());
		}
		if (tokenizer.hasMoreTokens())
			result.set(result.size() - 1, result.get(result.size() - 1) + "...");
		return result;
	}
	static protected class PositionedNode implements Comparable {
		AnalyzedGraph.NodeInfo nodeInfo;
		List forwardArcPositions = new ArrayList();
		List backwardArcPositions = new ArrayList();
		List circularArcPositions = new ArrayList();
		double x;
		double y;
		double labelXoffset;
		double labelYoffset;
		int labelDirection;
		String label = null;
		ArrayList textLines = null;

		public PositionedNode(AnalyzedGraph.NodeInfo nodeInfo) {
			this.nodeInfo = nodeInfo;
		}

		public int compareTo(Object o2) {
			PositionedNode n2 = (PositionedNode) o2;
			if (this instanceof ContinuationNode)
				return 1;
			else if (n2 instanceof ContinuationNode)
				return -1;
			else
				return nodeInfo.compareTo(n2.nodeInfo);
		}
	}
	static protected class ContinuationNode extends PositionedNode {
		public ContinuationNode(AnalyzedGraph.NodeInfo nodeInfo) {
			super(nodeInfo);
			label = "...";
		}
	}
	static protected class PositionedArc {
		AnalyzedGraph.ArcInfo arcInfo;
		PositionedNode subjectPosition;
		PositionedNode objectPosition;
		int direction;
		double y;
		String label;

		public PositionedArc(AnalyzedGraph.ArcInfo arcInfo) {
			this.arcInfo = arcInfo;
		}
	}
	static protected class PositionedCircularArc extends PositionedArc {
		double intermediateY; // for circular arcs there is an extra parameter
							  // since the arc must

		// be shaped to avoid overlapping the forward and backward nodes from
		// the same starting node.
		public PositionedCircularArc(AnalyzedGraph.ArcInfo arcInfo) {
			super(arcInfo);
		}
	}
}

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

import java.util.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;


public class AnalyzedGraph {
	static private final int SHORT_LABEL_LENGTH = 25;
	static private final int MEDIUM_LABEL_LENGTH = 45;
	static private final int LONG_LABEL_LENGTH = 65;
	protected HashMap _nodes = new HashMap();
	protected HashMap _arcs = new HashMap();
	protected LiteralIndex _index = new LiteralIndex();
	protected Model _model;

/**
 * Construct an analyzed graph from a Jena RDF Model. Note that this performs
 * an analysis on the specified model and that can take some time for very
 * large models. Also note that any new nodes/arcs added to the model after
 * construction of the analyzed graph will not be shown in the visualization.
 * 
 * @param m Model to analyze.
 */
	public AnalyzedGraph(Model m) {
		_model = m;
		analyze();
	}

	/** Get the model from which this analyzed graph was constructed.
	 * @return Model used for the analysis.
	 */
	public Model getModel() {
		return _model;
	}
	
	/**
	 * Return literals containing a desired text word.
	 * 
	 * @param word on which to search literals (this may end in "*" to accept
	 *             any trailing characters).
	 * @return Set of literal NodeInfos.
	 */
	public Set findLiteralNodeInfos(String word) {
		return _index.findLiteralNodeInfos(word);
	}

	/** Return the nodes which have an arc to literals containing a desired text word.
	 * @param word on which to search literals (this may end in "*" to accept any trailing characters).
	 * @return HashMap containing sets of NodeInfos sorted indexed by rdf:type.
	 */
	public HashMap findTypedSubjectNodeInfos(String word) {
		return _index.findTypedSubjectNodeInfos(word);
	}

	/** Return all information on arcs with a given predicate.
	 * 
	 * @param p Predicate resource for which to look.
	 * @return Sorted set of ArcInfos.
	 */
	public SortedSet findArcInfos(Resource p) {
		return (SortedSet) _arcs.get(p);
	}

	private void analyze() {
		StmtIterator stmtIterator = _model.listStatements();
		HashSet allLiterals = new HashSet();
		while (stmtIterator.hasNext()) {
			HashMap nodesToAddLater = new HashMap();
			Statement stmt = stmtIterator.nextStatement();
			Resource source = stmt.getSubject();
			NodeInfo srcNodeInfo = (NodeInfo) _nodes.get(source);
			if (srcNodeInfo == null) {
				srcNodeInfo = new NodeInfo(source);
				nodesToAddLater.put(source, srcNodeInfo);
			}
			RDFNode destination = stmt.getObject();
			NodeInfo dstNodeInfo = (NodeInfo) _nodes.get(destination);
			if (dstNodeInfo == null) {
				dstNodeInfo = new NodeInfo(destination);
				nodesToAddLater.put(destination, dstNodeInfo);
				if (destination instanceof Literal)
					allLiterals.add(dstNodeInfo);
			}
			ArcInfo arcInfo = new ArcInfo(srcNodeInfo, stmt.getPredicate(), dstNodeInfo);
			SortedSet arcs = (SortedSet) _arcs.get(stmt.getPredicate());
			if (arcs == null) {
				arcs = new TreeSet();
				_arcs.put(stmt.getPredicate(), arcs);
			}
			arcs.add(arcInfo);
			if (srcNodeInfo.equals(dstNodeInfo)) {
				// its a circular arc
				srcNodeInfo.circularArcs.add(arcInfo);
			} else {
				srcNodeInfo.departingArc.add(arcInfo);
				dstNodeInfo.arrivingArcs.add(arcInfo);
				if (!srcNodeInfo.forwardNodes.contains(dstNodeInfo)) {
					srcNodeInfo.forwardNodes.add(dstNodeInfo);
				}
				if (!dstNodeInfo.backwardNodes.contains(srcNodeInfo)) {
					dstNodeInfo.backwardNodes.add(srcNodeInfo);
				}
			}
			if (stmt.getPredicate().equals(RDF.type))
				srcNodeInfo.types.add(dstNodeInfo);
			// we add these last because the nodes list is sorted and we need
			// all the
			// arc info to be in place before the comparisons can be performed.
			_nodes.putAll(nodesToAddLater);
		}
		// Now index the literals
		Iterator literalNodes = allLiterals.iterator();
		while (literalNodes.hasNext()) {
			AnalyzedGraph.NodeInfo literalNodeInfo = (AnalyzedGraph.NodeInfo) literalNodes.next();
			_index.add(literalNodeInfo);
		}
		// Now postprocess the index (this sorts the indexed subjects by node
		// type).
		_index.postProcess();
	}

	/** Collect information about a Node in the analyzed graph.
	 * 
	 * The collected node information includes sorted indexes
	 * of arriving and departing arcs, the nodes reachable via those
	 * arcs, and also human-readable labels for displaying the node.
	 * 
	 */
	public static class NodeInfo extends Object implements Comparable {
		RDFNode node;
		String shortLabel;
		String mediumLabel;
		String longLabel;
		SortedSet arrivingArcs = new TreeSet();
		SortedSet departingArc = new TreeSet();
		SortedSet circularArcs = new TreeSet();
		SortedSet forwardNodes = new TreeSet();
		SortedSet backwardNodes = new TreeSet();
		SortedSet types = new TreeSet();

		public NodeInfo(RDFNode n) {
			node = n;
			shortLabel = formatRDFNode(n, SHORT_LABEL_LENGTH);
			mediumLabel = formatRDFNode(n, MEDIUM_LABEL_LENGTH);
			longLabel = formatRDFNode(n, LONG_LABEL_LENGTH);
		}

		public int compareTo(Object o2) {
			NodeInfo node2 = (NodeInfo) o2;
			if (node instanceof Literal) {
				if (node2.node instanceof Resource)
					return -1;
				// both are literals
				int result = 0;
				// compare them based on the predicate of the first arc to them
				if (arrivingArcs.size() > 0 && node2.arrivingArcs.size() > 0)
					result = ((ArcInfo) arrivingArcs.first())
							.compareTo(node2.arrivingArcs.first());
				if (result == 0)
					result = ((Literal) node).getLexicalForm().compareTo(
							((Literal) node2.node).getLexicalForm());
				if (result == 0) {
					// need to make sure two literals with different languages
					// or datatypes are distinguishable as being different. So if the lexical
					// form is the same, we take the easy way out and compare the toString
					// results (since we know that includes mention of the language/datatype). It's
					// not good practice to rely on toString like this, but its a quick and easy fix for now.
					// TODO rewrite to do a proper comparison.
					return ((Literal) node).toString().compareTo(((Literal) node2.node).toString());
				} else
					return result;
			} else {
				if (node2.node instanceof Literal)
					return 1;
				if (((Resource) node).isAnon()) {
					if (((Resource) node2.node).isAnon())
						return ((Resource) node).getId().toString().compareTo(
								((Resource) node2.node).getId().toString());
					else
						return -1;
				} else if (((Resource) node2.node).isAnon()) {
					return 1;
				}
				int result = ((Resource) node).getURI().compareToIgnoreCase(
						((Resource) node2.node).getURI());
				if (result != 0)
					return result;
				else if (node.equals(node2.node))
					return 0;
				else
					return 1; // must ensure that two nodes never compare equal
							  // if they're not "equal".
			}
		}
	}

	/** Collect information about an Arc in the analyzed graph.
	 * 
	 * The collected information includes the starting and ending nodes
	 * along with human-readable labels for displaying the arc.
	 * 
	 */
	public static class ArcInfo extends Object implements Comparable {
		NodeInfo start;
		NodeInfo end;
		Resource predicate;
		String shortLabel;
		String mediumLabel;
		String longLabel;

		public ArcInfo(NodeInfo start, Resource predicate, NodeInfo end) {
			this.start = start;
			this.predicate = predicate;
			this.end = end;
			shortLabel = formatRDFNode(predicate, 20);
			longLabel = formatRDFNode(predicate, 60);
		}

		public int compareTo(Object o2) {
			ArcInfo arc2 = (ArcInfo) o2;
			int result = predicate.getURI().compareToIgnoreCase(arc2.predicate.getURI());
			if (result == 0) {
				result = start.node.toString().compareTo(arc2.start.node.toString());
				if (result == 0)
					return end.node.toString().compareTo(arc2.end.node.toString());
				else
					return result;
			} else
				return result;
		}
	}

	protected static class LiteralIndex {
		protected static class IndexInfo {
			Set literalNodeInfos = new HashSet();
			SortedSet subjectNodeInfos = new TreeSet();
			HashMap typedSubjectNodeInfos = new HashMap();
		}
		static Model tempModel = ModelFactory.createDefaultModel();
		static NodeInfo resourceNodeInfo = new NodeInfo(RDFS.Resource.inModel(tempModel));
		HashMap index = new HashMap();
		TreeSet sortedLiteralWords = new TreeSet();

		protected LiteralIndex() {
		}

		protected void add(NodeInfo nodeInfo) {
			if (nodeInfo.node instanceof Literal) {
				Literal literal = (Literal) nodeInfo.node;
				StringTokenizer tokenizer = new StringTokenizer(literal.getLexicalForm()
						.toLowerCase(), " \n\t\r,.():-\"/\\!?$@&");
				while (tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					IndexInfo indexInfo = (IndexInfo) index.get(token);
					if (indexInfo == null) {
						indexInfo = new IndexInfo();
						index.put(token, indexInfo);
						sortedLiteralWords.add(token);
					}
					indexInfo.literalNodeInfos.add(nodeInfo);
					indexInfo.subjectNodeInfos.addAll(nodeInfo.backwardNodes);
				}
			}
		}

		protected void postProcess() {
			Iterator infos = index.values().iterator();
			while (infos.hasNext()) {
				IndexInfo indexInfo = (IndexInfo) infos.next();
				// Place each subject node into a bucket based on it's type.
				// If no type is defined, then default to being of type Resource.
				Iterator subjects = indexInfo.subjectNodeInfos.iterator();
				while (subjects.hasNext()) {
					NodeInfo subjectNodeInfo = (NodeInfo) subjects.next();
					Iterator types = subjectNodeInfo.types.iterator();
					if (!types.hasNext()) {
						SortedSet set = (SortedSet) indexInfo.typedSubjectNodeInfos
								.get(resourceNodeInfo);
						if (set == null) {
							set = new TreeSet();
							indexInfo.typedSubjectNodeInfos.put(resourceNodeInfo, set);
						}
						set.add(subjectNodeInfo);
					} else {
						while (types.hasNext()) {
							NodeInfo typeNode = (NodeInfo) types.next();
							SortedSet set = (SortedSet) indexInfo.typedSubjectNodeInfos
									.get(typeNode);
							if (set == null) {
								set = new TreeSet();
								indexInfo.typedSubjectNodeInfos.put(typeNode, set);
							}
							set.add(subjectNodeInfo);
						}
					}
				}
			}
		}

		protected Set findLiteralNodeInfos(String word) {
			if (word.endsWith("*")) {
				Set result = new TreeSet();
				// look for matching words
				// to keep things tractable, we only return the first 100
				// matches
				int toGet = 1000;
				String matchingWord = word.toLowerCase().substring(0, word.length() - 1);
				Iterator it = sortedLiteralWords.tailSet(matchingWord).iterator();
				while (it.hasNext() && toGet > 0) {
					String searchTerm = (String) it.next();
					if (!searchTerm.startsWith(matchingWord))
						break;
					IndexInfo info = (IndexInfo) index.get(searchTerm);
					if (info != null) {
						result.addAll(info.literalNodeInfos);
						toGet -= info.literalNodeInfos.size();
					}
				}
				if (result.size() == 0)
					return null;
				else
					return result;
			}
			IndexInfo info = (IndexInfo) index.get(word.toLowerCase());
			if (info == null)
				return null;
			return info.literalNodeInfos;
		}

		protected HashMap findTypedSubjectNodeInfos(String word) {
			if (word.endsWith("*")) {
				HashMap result = new HashMap();
				// look for matching words
				// to keep things tractable, we only return the first 100
				// matches
				int toGet = 100;
				String matchingWord = word.toLowerCase().substring(0, word.length() - 1);
				Iterator it = sortedLiteralWords.tailSet(matchingWord).iterator();
				while (it.hasNext() && toGet > 0) {
					String searchTerm = (String) it.next();
					if (!searchTerm.startsWith(matchingWord))
						break;
					IndexInfo info = (IndexInfo) index.get(searchTerm);
					if (info != null) {
						Iterator typeIt = info.typedSubjectNodeInfos.keySet().iterator();
						while (typeIt.hasNext() && toGet > 0) {
							Object type = typeIt.next();
							SortedSet set = (SortedSet) result.get(type);
							if (set == null) {
								set = new TreeSet();
								result.put(type, set);
							}
							set.addAll((SortedSet) info.typedSubjectNodeInfos.get(type));
							toGet -= info.typedSubjectNodeInfos.size();
						}
					}
				}
				if (result.size() == 0)
					return null;
				else
					return result;
			}
			IndexInfo info = (IndexInfo) index.get(word.toLowerCase());
			if (info == null)
				return null;
			else
				return info.typedSubjectNodeInfos;
		}
	}

	protected static String shortenString(String string, int length) {
		if (string.length() > length)
			return string.substring(0, length - 2) + "...";
		else
			return string;
	}

	protected static String formatRDFNode(RDFNode node, int length) {
		if (node instanceof Literal) {
			String result = ((Literal) node).getLexicalForm();
			String lang = ((Literal) node).getLanguage();
			if (lang != null && lang.length() > 0)
				result = result + " (lang=" + lang + ")";
			String datatype = (((Literal) node).getDatatypeURI());
			if (datatype != null)
				result = result + " (type=" + datatype + ")";
			if (result.length() > length)
				return (result.substring(0, length - 2) + "...").replaceAll("&", "&amp;");
			else
				return result.replaceAll("&", "&amp;");
		}
		Resource resource = (Resource) node;
		if (resource.isAnon())
			return "";
		String namespace = resource.getNameSpace();
		String localname = resource.getLocalName();
		String uri = resource.getURI();
		String prefix = null;
		String label = null;
		Statement stmt = resource.getProperty(RDFS.label); // TODO consider
														   // languages when
														   // choosing label
		if (stmt != null) {
			label = ((Literal) stmt.getObject()).getLexicalForm();
		}
		if (namespace != null && localname != null && localname.length() > 0)
			prefix = resource.getModel().getNsURIPrefix(namespace);
		if (prefix != null) {
			if (label != null) {
				if (length < LONG_LABEL_LENGTH)
					uri = prefix + ":'" + label + "'";
				else
					uri = prefix + ":" + localname;
			} else
				uri = prefix + ":" + localname;
		} else {
			if (label != null) {
				if (length < LONG_LABEL_LENGTH)
					uri = "`" + label + "'";
				else
					; // uri = uri + " ('" + label + "')";
			}
		}
		// If it all fits then all is easy.
		if (uri.length() <= length + 2)
			return uri.replaceAll("&", "&amp;");
		// need to shorten things - try to leave the beginning and end intact
		int end = uri.lastIndexOf("/");
		end = end + 1;
		if (end != 0 && (uri.length() - end) - 3 < length && uri.length() - end > 2) {
			if (length - (uri.length() - end) - 3 > 10)
				return (uri.substring(0, length - (uri.length() - end) - 3) + "..." + uri
						.substring(end)).replaceAll("&", "&amp;");
			else
				return "..." + uri.substring(end - 1).replaceAll("&", "&amp;");
		} else
			return ("..." + uri.substring(uri.length() - length - 1)).replaceAll("&", "&amp;");
	}
}

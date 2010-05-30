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

package edu.iastate.airl.semtus.visualizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.io.*;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.Filter;

import edu.iastate.airl.semtus.util.Utils;


public class HttpVisualizeHandler extends HttpServlet {
	
	static public String PATH = "/semtuswebapp/visualize";
	protected boolean _started = false;
	protected HashMap _analyzedModels;

	/**
	 * An HTTP Handler for visualizing RDF Graphs.
	 * 
	 * @param directory
	 *            A directory containing rdf files to visualize. The system
	 *            will look for files ending in ".rdf", ".owl" or ".n3".
	 */
	public void loadRDF(File directory) {
	
		_analyzedModels = new HashMap();
		
		if (directory.isDirectory()) {
			File[] files = directory.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].canRead()
						&& files[i].isFile()
						&& (files[i].getName().endsWith(".rdf")
								|| files[i].getName().endsWith(".owl") || files[i].getName()
								.endsWith(".n3"))) {
					Model model = ModelFactory.createDefaultModel();
					System.out.println("Preparing to visualize:" + files[i].getName());
					String type = null;
					if (files[i].getName().endsWith(".rdf") || files[i].getName().endsWith(".owl"))
						type = "RDF/XML";
					else if (files[i].getName().endsWith(".n3"))
						type = "N3";
					if (type != null) {
						System.out.print("\tLoading...");
						System.out.flush();
						long time = System.currentTimeMillis();
						model.read("file:" + files[i].getAbsolutePath(), type);
						System.out.println("done ("
								+ (int) ((System.currentTimeMillis() - time) / 1000 + 0.5)
								+ " seconds)");
						System.out.print("\tAnalyzing...");
						System.out.flush();
						time = System.currentTimeMillis();
						AnalyzedGraph aGraph = new AnalyzedGraph(model);
						System.out.println("done ("
								+ (int) ((System.currentTimeMillis() - time) / 1000 + 0.5)
								+ " seconds)");
						_analyzedModels.put(files[i].getName(), aGraph);
					}
				}
			}
		} else {
			throw new RuntimeException("Error, expected a directory.");
		}
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
     throws ServletException, IOException {
		 
		 handle (req, resp);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
	 throws ServletException, IOException {
		
		doGet(request, response);
	}
	 
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mortbay.http.HttpHandler#handle(java.lang.String,
	 *      java.lang.String, org.mortbay.http.HttpRequest,
	 *      org.mortbay.http.HttpResponse)
	 */
	public void handle(HttpServletRequest arg2, HttpServletResponse arg3)
			throws ServletException, IOException {
			    
		Utils.findCurrentDirectory();
	    loadRDF (new File (Utils.OUTPUT_DIRECTORY));
	    
		String method = arg2.getMethod();
		String lang = arg2.getParameter("lang");
		String render = arg2.getParameter("render");
		String model = arg2.getParameter("model");
		String searchString = arg2.getParameter("search");
		ByteArrayOutputStream array = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(array);
		if ((lang == null || (lang != null && lang.compareToIgnoreCase("visual") == 0))
				&& render == null) {
			
			
			String url = arg2.getRequestURI().toString();
			
			if (arg2.getQueryString() != null) {
				
				url = url.concat("?").concat(arg2.getQueryString());
			}
			
			System.out.println(url);
			
			if (url.indexOf("lang=") != -1)
				url = url.replaceFirst("=visual", "=svg");
			else
				url = null;
			
			Iterator it = _analyzedModels.keySet().iterator();
			
			String modelName=""; 
			
			while (it.hasNext()) {
				modelName = (String) it.next();
			}
			
			StringBuffer pacString = new StringBuffer("<html>\n"
					
					+ "<HEAD><TITLE=\"RDF Search\"></HEAD>"
					+ "<BODY text=\"#ff9000\" link=\"#00ff00\" bgcolor=\"#000000\">"
					+ "<table height=\"30%\" width=\"100%\">" 
					+ "<tr>" 
					+ "<td colspan=\"3\" style=\"background: rgb(216, 196, 122) none repeat scroll 0% 50%; -moz-background-clip: -moz-initial; -moz-background-origin: -moz-initial; -moz-background-inline-policy: -moz-initial\" height=5px width=\"100%\">" 
					+ "</td>"
					+ "</tr>" 
					+ "<tr>"
					+ "<td>"
					+ "<br/>"
					+ "<center>"
					+ "<font color=\"#f0e190\" face=\"georgia\" size=\"6\">SEMTUS - A Semantic Text Understanding System</font>" 
					+ "</center>"
					+ "<br/>"
					+ "</td>"
					+ "</tr>" 
					
					+ "<tr>" 
					+ "<td colspan=\"3\" height=\"2%\" width=\"100%\">" 
					+ "<p align=\"center\">"
					
					+ "<font color=\"#e0c190\" face=\"verdana\" size=\"1\">" 
					+ "                   Powered by Jena Semantic Web Framework, Google Web Toolkit and Stanford Parser</font>"              
					+ "		    <br/><br/>"
					+ "		    <font color=\"#e0c190\" face=\"verdana\" size=\"1\">"
					+ "         <a href=\"http://www.cs.iastate.edu/~pandit/semtus/\" target=\"_blank\">Click here to know all about SEMTUS</a></font>"
					+ "</p>"
					+ "<br/>"
					+ "</td></tr>" 

					+ "<tr>" 
					+ "<td colspan=\"3\" style=\"background: rgb(216, 196, 122) none repeat scroll 0% 50%; -moz-background-clip: -moz-initial; -moz-background-origin: -moz-initial; -moz-background-inline-policy: -moz-initial\" height=5px width=\"100%\">"
					+ "</td>"
					+ "</tr>"
					
					+ "<tr>"
					+ "<td>"
					+ "<div id=\"contentarea\" style=\"width:100%; height:95%\">" 

					+ "<table border=\"0\" cellpadding=\"3\" cellspacing=\"3\" width=\"100%\">"
					+ "<tr>"
					+ "<td align=\"left\" valign=\"top\" style=\"color:#7777ff;font-family: sans-serif\">"
					+ "<font size=\"+2\"></font>"
					+ "</td>"
					+ "<td align=\"right\" valign=\"bottom\" style=\"color:#333333;font-family: sans-serif\">"
					+ "<FORM ACTION=\"http://" + arg2.getServerName() + ":" + arg2.getServerPort() + arg2.getRequestURI() + "\">"
					+ "<INPUT TYPE=\"hidden\" NAME=\"lang\" VALUE=\"visual\" />\n"
					+ "<INPUT TYPE=\"hidden\" NAME=\"model\" VALUE=\"" + modelName + "\">");
			
			System.out.println("<FORM ACTION=\"http://" + arg2.getServerName() + ":" + arg2.getServerPort() + arg2.getRequestURI() + "\">");
			
			pacString.append("</SELECT>"
							+ "<INPUT TYPE=\"hidden\" NAME=\"search\" SIZE=\"20\" ");
			if( searchString != null)
				pacString.append("VALUE=\""+searchString+"\"");
			pacString.append("/>\n"
							+ "<INPUT NAME=\"submit\" TYPE=\"submit\" VALUE=\"Reset to Initial View\" style=\"color:#444444;font-family: sans-serif\"/><BR>"
							+ "</FORM>" + "</td>" + "</tr>" + "</table></div></td></tr></table>");
			if (url != null)
				pacString.append("<div style=\"background-color: #ffffff;\"><embed src=\""
								+ url.replaceFirst(" type=", "").replaceFirst(
										"=visual", "=svg")
								+ "\" type=\"image/svg+xml\" pluginspace=\"http://www.adobe.com/svg/viewer/install/\" width=\"100%\" height=\"70%\" ></div>");
			else
				pacString.append("<font color=\"#e0c190\" face=\"verdana\" size=\"2\">"
								+ "<P align=\"center\">To view the visualization, you'll need browser support for Scalable Vector Graphics. <br/><br/>These visualizations are best viewed in internet explorer with SVG plugin installed.");
								
			pacString.append("</BODY> </html>");
			arg3.setStatus(HttpServletResponse.SC_OK);
			arg3.setContentType("text/html");
			//PrintWriter ow = arg3.getWriter();
			//ow.println(pacString);
			//System.out.println(pacString);
			OutputStream os = arg3.getOutputStream();
			BufferedOutputStream bos = new BufferedOutputStream(os);
			bos.write(pacString.toString().getBytes());
			bos.flush();
			os.flush();
		} else if (model != null && lang != null
				&& lang.compareToIgnoreCase("svg") == 0) {
			NodeToSVG visualizer = null;
			String resourceString = arg2.getParameter("r");
			String styleString = arg2.getParameter("style");
			AnalyzedGraph aModel = (AnalyzedGraph) _analyzedModels.get(model);
			if (resourceString == null && (searchString == null || searchString.length()==0)) {
				ModelToSVG modelVis = new ModelToSVG();
				NodeToSVG.PageInfo pageInfo = new NodeToSVG.PageInfo();
				pageInfo.resourceToHREF = new myResourceToHREF(model, searchString, "list");
				pageInfo.propertyToHREF = new myResourceToHREF(model, searchString, "arcs");
				pageInfo.maxBackArcs = 0;
				pageInfo.maxForwardArcs = 0;
				modelVis.visualizeStart(out, pageInfo);
				Filter nodeFilter = Filter.any;
				modelVis.visualizeModel(out, aModel, nodeFilter, pageInfo);
				modelVis.visualizeEnd(out, pageInfo);
				visualizer = modelVis;
			} else 	if (resourceString != null) {
				resourceString = resourceString.replaceAll(";hash;", "#").replaceAll(";dot;", ".");
				Resource toDisplay;
				Model tempModel = ModelFactory.createDefaultModel();
				if (resourceString.startsWith("_")) {
					resourceString = resourceString.substring(1); // remove leading "_"
					toDisplay = tempModel.createResource(new AnonId(resourceString));
				} else
					toDisplay = tempModel.createResource(resourceString);
				ArrayList modelList = new ArrayList(); // list of models from
				// which to display
				// results
				modelList.add(model);
				Iterator it = _analyzedModels.keySet().iterator();
				while (it.hasNext()) {
					String modelName = (String) it.next();
					AnalyzedGraph potentialModel = (AnalyzedGraph) _analyzedModels.get(modelName);
					if (!modelList.contains(modelName)
							&& potentialModel._nodes.containsKey(toDisplay)) {
						modelList.add(modelName);
					}
				}
				NodeToSVG.PageInfo pageInfo = new NodeToSVG.PageInfo();
				if (searchString != null) {
					pageInfo.textToHightlight = searchString;
				}
				for (int i = 0; i < modelList.size(); i++) {
					String modelName = (String) modelList.get(i);
					pageInfo.resourceToHREF = new myResourceToHREF(modelName, null, "list");
					pageInfo.propertyToHREF = new myResourceToHREF(modelName, null, "arcs");
					pageInfo.maxBackArcs = 1;
					pageInfo.maxForwardArcs = 2;
					AnalyzedGraph modelToVisualize = (AnalyzedGraph) _analyzedModels.get(modelName);
					if (modelToVisualize == null) {
						visualizer = null;
						break;
					}
					if (searchString != null) {
						pageInfo.textToHightlight = searchString;
						pageInfo.literalsToHighlight = modelToVisualize
								.findLiteralNodeInfos(searchString);
					}
					visualizer = new NodeToSVG();
					if (i == 0) {
						visualizer.visualizeStart(out, pageInfo);
					}
					visualizer.visualizeSubHeading(out, pageInfo, modelName);
					visualizer.advancePage(pageInfo);
					if (i == 0 && styleString != null && styleString.equalsIgnoreCase("arcs")) {
						// first look for and display any nodes with a matching
						// predicate
						SortedSet arcs = modelToVisualize.findArcInfos(toDisplay);
						if (arcs != null) {
							Iterator ait = arcs.iterator();
							pageInfo.maxBackArcs = 0;
							pageInfo.maxForwardArcs = 1;
							int previousMaxLiteralLines = pageInfo.maxLiteralLines;
							pageInfo.maxLiteralLines = 2;
							pageInfo.ySpacing *= 0.5;
							if (searchString != null)
								pageInfo.literalsToHighlight = modelToVisualize
										.findLiteralNodeInfos(searchString);
							int count = 0;
							while (ait.hasNext() && count <= 10) {
								AnalyzedGraph.ArcInfo ainfo = (AnalyzedGraph.ArcInfo) ait.next();
								if (count++ == 10 && arcs.size() > 11) {
									visualizer.visualizeVerticalContinuation(out, pageInfo);
									ainfo = (AnalyzedGraph.ArcInfo) arcs.last();
								}
								visualizer.visualizeNodeInfo(out, modelToVisualize, ainfo.start,
										Filter.any, new EqualityFilter(ainfo),
										pageInfo);
							}
							pageInfo.ySpacing *= 2.0;
							pageInfo.maxLiteralLines = previousMaxLiteralLines;
							if (arcs.size() > 0)
								visualizer.advancePage(pageInfo, 2.0);
							pageInfo.maxBackArcs = 2;
							pageInfo.maxForwardArcs = 2;
						}
					}
					visualizer.visualizeNode(out, modelToVisualize, toDisplay, Filter.any,
							Filter.any, pageInfo);
					if (i == modelList.size() - 1)
						visualizer.visualizeEnd(out, pageInfo);
				}
			} else if (resourceString == null && searchString != null) {
				// Find all literals with that text
				HashMap results;
				if (searchString == null || searchString.length() == 0)
					results = null;
				else
					results = aModel.findTypedSubjectNodeInfos(searchString);
				if (results == null || results.size() == 0) {
					visualizer = null;
				} else {
					visualizer = new NodeToSVG();
					NodeToSVG.PageInfo pageInfo = new NodeToSVG.PageInfo();
					pageInfo.resourceToHREF = new myResourceToHREF(model, searchString, "list");
					pageInfo.propertyToHREF = new myResourceToHREF(model, searchString, "arcs");
					pageInfo.maxBackArcs = 0;
					pageInfo.maxForwardArcs = 1;
					pageInfo.textToHightlight = searchString;
					pageInfo.literalsToHighlight = aModel.findLiteralNodeInfos(searchString);
					pageInfo.maxLiteralLines = 3;
					visualizer.visualizeStart(out, pageInfo);
					boolean endEarly = false;
					Iterator types = results.keySet().iterator();
					while (types.hasNext() && !endEarly) {
						AnalyzedGraph.NodeInfo typeNode = (AnalyzedGraph.NodeInfo) types.next();
						visualizer.visualizeSubHeading(out, pageInfo, typeNode.longLabel);
						Set set = (Set) results.get(typeNode);
						Iterator resultIt = set.iterator();
						while (resultIt.hasNext() && !endEarly) {
							AnalyzedGraph.NodeInfo nodeInfo = (AnalyzedGraph.NodeInfo) resultIt
									.next();
							visualizer.visualizeNodeInfo(out, aModel, nodeInfo, Filter.any,
									new ArcInfoDestinationNodeFilter(
											pageInfo.literalsToHighlight), pageInfo);
							if (pageInfo.yStart > 250 && resultIt.hasNext()) {
								visualizer.visualizeSubHeading(out, pageInfo,
										"(too many results to display)");
								endEarly = true;
							}
						}
						visualizer.advancePage(pageInfo);
					}
					visualizer.visualizeEnd(out, pageInfo);
				}
			} else {
				// error
			}
			if (visualizer == null) {
				visualizer = new NodeToSVG();
				NodeToSVG.PageInfo pageInfo = new NodeToSVG.PageInfo();
				pageInfo.resourceToHREF = new myResourceToHREF(null, searchString, null);
				pageInfo.propertyToHREF = new myResourceToHREF(null, searchString, null);
				visualizer.visualizeStart(out, pageInfo);
				if (searchString == null)
					visualizer.visualizeSubHeading(out, pageInfo,
							"Sorry, no matching resources found.");
				else
					visualizer.visualizeSubHeading(out, pageInfo,
							"Sorry, no matches found for search: \"" + searchString + "\"");
				visualizer.visualizeEnd(out, pageInfo);
			}
			String pacString = array.toString();
			arg3.setStatus(HttpServletResponse.SC_OK);
			//arg3.setField(HttpFields.__ContentEncoding, "image/svg+xml");
			arg3.setContentType("image/svg+xml");
			//PrintWriter ow = arg3.getWriter();
			//ow.println(pacString);
			OutputStream os = arg3.getOutputStream();
			BufferedOutputStream bos = new BufferedOutputStream(os);
			bos.write(pacString.getBytes("UTF-8"));
			bos.flush();
			os.flush();
			/* } */
		}
	}
	
	private static class myResourceToHREF implements NodeToSVG.ResourceToString {
		String _search;
		String _modelName;
		String _style;

		public myResourceToHREF(String modelName, String search, String style) {
			_search = search;
			_modelName = modelName;
			_style = style;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.hp.hpl.jena.rdf.visualize.RDFNodeToHREF#convert(com.hp.hpl.jena.rdf.model.RDFNode)
		 */
		public String convert(Resource node) {
			try {
				String searchString;
				if (_search == null)
					searchString = "";
				else
					searchString = "&amp;search=" + URLEncoder.encode(_search, "UTF-8");
				String uri;
				if (node.isAnon())
					uri = URLEncoder.encode("_" + node.getId(), "UTF-8");
				else
					uri = URLEncoder.encode(node.getURI(), "UTF-8");
				uri = uri.replaceAll("#", ";hash;");//.replaceAll("\\.",";dot;");
				return PATH + "?lang=visual"
							+ ((_modelName != null) ? "&amp;model=" + _modelName : "")
							+ "&amp;r=" + uri
							+ searchString
							+ ((_style != null) ? "&amp;style=" + _style : "");
			} catch (Exception e) {
				return null;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.hp.hpl.jena.rdf.visualize.NodeVisualizer.ResourceToString#convert(com.hp.hpl.jena.rdf.model.Resource,
		 *      int)
		 */
		public String convert(Resource resource, int length) {
			return convert(resource);
		}
	}
	
	private static class ArcInfoDestinationNodeFilter extends Filter {
		
		Set _set;
		
		public ArcInfoDestinationNodeFilter( Set set ) {
			_set = set;
		}
		
		/* (non-Javadoc)
		 * @see com.hp.hpl.jena.util.iterator.Map1#map1(java.lang.Object)
		 */
		public boolean accept(Object o) {
			AnalyzedGraph.ArcInfo arcInfo = (AnalyzedGraph.ArcInfo)o;			
			AnalyzedGraph.NodeInfo end = arcInfo.end;			
			return _set.contains(end);
		}		
	}
	
	
	private static class EqualityFilter extends Filter {
		Object object;

		public EqualityFilter(Object o) {
			object = o;
		}

		public boolean accept(Object o) {
			return o.equals(object);
		}
	}


}

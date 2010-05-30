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

package edu.iastate.airl.semtus.util;

import java.io.File;

public class Utils {

	// Before finally deploying the .war in tomcat, change these paths to reflect refereces w.r.t local app name within \webapps folder
	public static final String UPLOAD_DIRECTORY = "C:\\Program Files\\Apache Software Foundation\\Tomcat 6.0\\resources\\ontology";
	//public static final String UPLOAD_DIRECTORY = "./webapps/semtuswebapp/resources/ontology";
	
	public static final String OUTPUT_DIRECTORY = "C:\\Program Files\\Apache Software Foundation\\Tomcat 6.0\\resources\\";
	//public static final String OUTPUT_DIRECTORY = "./webapps/semtuswebapp/resources/";
	
	public static final String ONTOLOGY_DIRECTORY = "resources/ontology/";
	//public static final String ONTOLOGY_DIRECTORY = "./webapps/semtuswebapp/resources/ontology/";
	
	public static final String AUX_DIRECTORY = "resources/auxkb/";
	//public static final String AUX_DIRECTORY = "./webapps/semtuswebapp/resources/auxkb/";
	
	public static final String INPUT_ONT = "input";
	
	public static final String AUXILIARY_KB = "auxiliary";
	
	public static final String LEVEL_ONE = "1";
	
	public static final String LEVEL_TWO = "2";
	
	public static final String LEVEL_THREE = "3";
	
	
	public static void findCurrentDirectory () {
		
		File dir1 = new File (".");
	    
		try {
	    	 
	    	 System.out.println ("Current dir : " + dir1.getCanonicalPath());
	    	 
	    } catch(Exception e) {
	       
	    	 e.printStackTrace();
	    }
	}
	
	// Check whether the passed predicate is trivial.
	public static boolean checkTrivial (final String predicate) {
		
		if (predicate.trim().equalsIgnoreCase("is") || predicate.trim().equalsIgnoreCase("has") || predicate.trim().equalsIgnoreCase("was") 
				|| predicate.trim().equalsIgnoreCase("in") || predicate.trim().equalsIgnoreCase("as") || predicate.trim().equalsIgnoreCase("in") 
				|| predicate.trim().equalsIgnoreCase("with") || predicate.trim().equalsIgnoreCase("by")) {
			
			return true;
		}
		
		else {
			
			return false;
		}
	}
}

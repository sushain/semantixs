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

package edu.iastate.airl.semtus.rdf;


public class Triple  {

	private Object object = null;
	
	private String subjectResourceString = null;
	
	private String [] predicateStringArray = null;
	
	
	public Triple (final String subjectResourceString, final Object object, final String [] predicateStringArray) {
		
		this.setObject(object);
		
		this.setSubjectResourceString(subjectResourceString);
		
		this.setPredicateStringArray(predicateStringArray);
	}


	public void setObject(Object object) {
		this.object = object;
	}


	public Object getObject() {
		return object;
	}


	public void setSubjectResourceString(String subjectResourceString) {
		this.subjectResourceString = subjectResourceString;
	}


	public String getSubjectResourceString() {
		return subjectResourceString;
	}


	public void setPredicateStringArray(String [] predicateStringArray) {
		this.predicateStringArray = predicateStringArray;
	}


	public String [] getPredicateStringArray() {
		return predicateStringArray;
	}
}

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

package edu.iastate.airl.semtus.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDF;
import edu.iastate.airl.semtus.util.Utils;


public class TestJena {

	public static void main (String [] args) throws FileNotFoundException {

		 //System.out.println("\"abracadf$%^&*()_*&12 \t\t\t    \"141\" ?? ..  31313131. ^ %$#@!~!@ #$%^&ar vabsbaj ha^^&$%^^*&(*&\"".replaceAll("[^a-zA-Z0-9.?&\t ]", ""));

		 //System.out.println("hsjkahjsh;kjadkjahkd.dkjahdjksa:".replaceAll("[:;]", "."));

		/*Utils.findCurrentDirectory();

		FileInputStream in = new FileInputStream(new File("war/" + Utils.ONTOLOGY_DIRECTORY + "foafdata.rdf"));

		Model m = ModelFactory.createMemModelMaker().createModel("Input ontology Model");

		// null base URI, since model URIs are absolute
		m.read(in, null);

		// Model m = FileManager.get().loadModel("war/" + Utils.ONTOLOGY_DIRECTORY + "foafdata.rdf");

		listPeople(m);*/
	}

	/** Print out all named resources of type foaf:Person */
	protected static void listPeople( Model m ) {

		String FOAF_NS = FOAF.getURI();

	  // get all resources of type foaf:Person
	  Resource personClass = m.getResource( FOAF_NS + "Person" );
	  ResIterator i = m.listSubjectsWithProperty( RDF.type, personClass );

	  // for each person, show their foaf:name if known
	  Property name = m.getProperty( FOAF_NS + "name" );
	  Property firstName = m.getProperty( FOAF_NS + "firstName" );

	  while (i.hasNext()) {
	        Resource person = i.nextResource();

	    Statement nm = person.getProperty( name );
	    nm = (nm == null) ? person.getProperty( firstName ) : nm;

	    if (nm != null) {
	      System.out.println( "Person named: " + nm.getString() );
	    }
	  }
	}
}

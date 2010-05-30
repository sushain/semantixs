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

package edu.iastate.airl.semtus.server;

import java.io.File;
import java.util.HashMap;

import edu.iastate.airl.semtus.client.SemtusService;
import edu.iastate.airl.semtus.main.Main;
import edu.iastate.airl.semtus.shared.FieldVerifier;
import edu.iastate.airl.semtus.util.Utils;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class SemtusServiceImpl extends RemoteServiceServlet implements
		SemtusService {	
	
	public String handleInput(String input, String level) throws IllegalArgumentException {
		
		// Verify that the input is valid. 
		if (!FieldVerifier.isValidName(input)) {
			// If the input is not valid, throw an IllegalArgumentException back to
			// the client.
			throw new IllegalArgumentException(
					"Name must be at least 7 characters long");
		}
		
		String [] params= new String[3];
		
		String [] ls = new File (Utils.UPLOAD_DIRECTORY).list ();
		
		params[0] = Utils.ONTOLOGY_DIRECTORY + ls[0];
		
		params[1] = input;
		
		params[2] = level;
		
		String output = Main.main(params);
		
		return output;
	}
}

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
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;

import edu.iastate.airl.semtus.util.Utils;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class UploadServiceController extends HttpServlet {	 
    
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        super.doGet(req, resp);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp)
           throws ServletException, IOException {
    	
        // process only multipart requests
        if (ServletFileUpload.isMultipartContent(req)) {

            // Create a factory for disk-based file items
           FileItemFactory factory = new DiskFileItemFactory();

            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload(factory);

            // Parse the request
            try {
                List<FileItem> items = upload.parseRequest(req);
                for (FileItem item : items) {
                    // process only file upload - discard other form item types
                    if (item.isFormField()) continue;
                    
                   String fileName = item.getName();
                    // get only the file name not whole path
                   if (fileName != null) {
                        fileName = FilenameUtils. getName(fileName);
                    }

                    File uploadedFile = new File(Utils.UPLOAD_DIRECTORY, fileName);
                    
                    // first clean-up the folder; we want no clutter on the server :-)
                    String [] ls = new File (Utils.UPLOAD_DIRECTORY).list ();

                    for (int idx = 0; idx < ls.length; idx++) {
                    	
                      File file = new File (Utils.UPLOAD_DIRECTORY, ls [idx]);                      
                      file.delete ();
                    }
                    
                    if (uploadedFile.createNewFile()) {
                    	
                        item.write(uploadedFile);
                        resp.setStatus(HttpServletResponse.SC_CREATED);
                        resp.getWriter().print("The file was created successfully.");
                        resp.flushBuffer();
                        
                    } else {
                    	
                        throw new IOException("The file already exists in repository.");
                    }
                }
           } catch (Exception e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "An error occurred while creating the file : " + e.getMessage());
            }

        } else {
            resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                            "Request contents type is not supported by the servlet.");
        }
    }

}

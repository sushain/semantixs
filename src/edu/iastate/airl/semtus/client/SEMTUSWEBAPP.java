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

package edu.iastate.airl.semtus.client;

import edu.iastate.airl.semtus.shared.FieldVerifier;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class SEMTUSWEBAPP implements EntryPoint {

	// public static final String SEMTUS_HOSTING_SERVER_NAME =
	// "babbage.cs.iastate.edu";
	public static final String SEMTUS_HOSTING_SERVER_NAME = "localhost";

	public static final String SEMTUS_HOSTING_PORT_NAME = "8080";

	public static final String SEMTUS_APP_NAME = "semtuswebapp";

	public static final String LEVEL_ONE = "1";

	public static final String LEVEL_TWO = "2";

	public static final String LEVEL_THREE = "3";

	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Greeting
	 * service.
	 */
	private final SemtusServiceAsync greetingService = GWT
			.create(SemtusService.class);

	private DecoratorPanel panelHoldingTabPanel;

	private DialogBox uploadBox;

	private DecoratedTabPanel tabPanel;

	private TextArea inputField;

	private TextArea resultField;

	private DialogBox dialogBoxLoadingBar;

	private Button sendButton;

	private DialogBox dialogBox;

	private DialogBox announcementBox;

	private HTML serverResponseLabel;

	private HTML announcementLabel;

	private Button closeButton;

	private Button announcementCloseButton;

	private Button w3Button;

	private VerticalPanel dialogVPanel;

	private VerticalPanel announcementDialogVPanel;

	private String level;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {

		MenuBar menu = (MenuBar) createMenu();

		sendButton = new Button("Send");
		inputField = new TextArea();
		inputField
				.setText("Input some text based on the uploaded ontology for analysis.\n\n [PS: Remember to upload an ontology model for your domain to get relevant results.");
		final Label errorLabel = new Label();

		// We can add style names to widgets
		sendButton.addStyleName("sendButton");
		inputField.setSize("590px", "200px");

		// Create a table to layout the form options
		FlexTable layout = new FlexTable();
		layout.setCellSpacing(6);
		FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

		// Add a title to the form
		cellFormatter.setColSpan(0, 0, 3);
		cellFormatter.setHorizontalAlignment(0, 0,
				HasHorizontalAlignment.ALIGN_CENTER);

		// Add some standard form options
		layout.setWidget(1, 1, inputField);
		layout.setWidget(2, 1, sendButton);

		DecoratorPanel panel = new DecoratorPanel();

		// Add the nameField and sendButton to the RootPanel
		panel.add(layout);

		// Focus the cursor on the name field when the app loads
		inputField.setFocus(true);
		inputField.selectAll();

		// Create the popup dialog box
		dialogBox = new DialogBox();
		dialogBox
				.setText("<p align=\"center\"><font color=\"#000000\" face=\"verdana\" size=\"2\">Visualize Complete RDF Graph</font></p>");
		dialogBox.setAnimationEnabled(true);
		closeButton = new Button("Close");
		// We can set the id of a widget by accessing its Element
		closeButton.getElement().setId("closeButton");
		final Label textToServerLabel = new Label();
		serverResponseLabel = new HTML();

		dialogBoxLoadingBar = new DialogBox();
		dialogBoxLoadingBar.setAnimationEnabled(true);
		final VerticalPanel dialogVPanelLoadingBar = new VerticalPanel();
		dialogVPanelLoadingBar.setWidth("200");
		dialogVPanelLoadingBar
				.add(new Label(
						"---------------------------------- Processing --------------------------------"));
		dialogVPanelLoadingBar.add(new Image(
				"http://sushain.com/profile/images/con-bar3.gif"));
		dialogBoxLoadingBar.setWidget(dialogVPanelLoadingBar);

		dialogVPanel = new VerticalPanel();
		dialogVPanel.addStyleName("dialogVPanel");
		// dialogVPanel.add(textToServerLabel);
		// dialogVPanel.add(new HTML("<br><b>Server replies:</b>"));
		dialogVPanel.add(serverResponseLabel);
		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		dialogVPanel.add(closeButton);
		dialogBox.setWidget(dialogVPanel);

		announcementBox = new DialogBox();
		announcementBox
				.setHTML("<p align=\"center\"><font color=\"#000000\" face=\"verdana\" size=\"2\">SEMANTIXS Analysis Complexity Level Setting</font></p>");
		announcementCloseButton = new Button("Close");
		announcementDialogVPanel = new VerticalPanel();
		announcementLabel = new HTML();
		announcementDialogVPanel.addStyleName("announcementDialogVPanel");
		announcementDialogVPanel.add(announcementLabel);
		announcementDialogVPanel
				.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		announcementDialogVPanel.add(announcementCloseButton);
		announcementBox.setWidget(announcementDialogVPanel);

		// Create a tab panel
		tabPanel = new DecoratedTabPanel();
		tabPanel.setWidth("645px");
		tabPanel.setHeight("100px");
		tabPanel.setAnimationEnabled(true);

		// Add a home tab
		String[] tabTitles = new String[] { "Workspace View",
				"Input Ontology-based Text Here",
				"View Extracted Semantic Data" };
		HTML homeText = new HTML(
				"<p align=\"center\"><font color=\"#e0c190\" face=\"Times\" size=\"6\">"
						+ "<b>SEMANTIXS</b></font>"
						+ "<br/><font color=\"#e0c190\" face=\"Times\" size=\"3\">version 1.0.0</font><br/>"
						+ "</p>");

		tabPanel.add(homeText, tabTitles[0]);

		// Add a tab
		VerticalPanel vPanel = new VerticalPanel();
		vPanel.add(panel);
		tabPanel.add(vPanel, tabTitles[1]);

		// Create a table to layout the form options
		FlexTable layout1 = new FlexTable();
		layout1.setCellSpacing(6);
		FlexCellFormatter cellFormatter1 = layout1.getFlexCellFormatter();

		// Add a title to the form
		cellFormatter1.setColSpan(0, 0, 3);
		cellFormatter1.setHorizontalAlignment(0, 0,
				HasHorizontalAlignment.ALIGN_CENTER);

		resultField = new TextArea();
		resultField
				.setText("Send some text for processing before trying to view the results in this tab.");
		resultField.setSize("590px", "225px");

		// Add some standard form options
		layout1.setWidget(1, 1, resultField);
		DecoratorPanel panel1 = new DecoratorPanel();

		// Add the nameField and sendButton to the RootPanel
		panel1.add(layout1);
		VerticalPanel vPanel1 = new VerticalPanel();
		vPanel1.add(panel1);
		tabPanel.add(vPanel1, tabTitles[2]);

		tabPanel.selectTab(0);
		tabPanel.ensureDebugId("cwTabPanel");

		panelHoldingTabPanel = new DecoratorPanel();
		panelHoldingTabPanel.setHeight("450px");
		panelHoldingTabPanel.setWidth("650px");
		panelHoldingTabPanel.setWidget(tabPanel);
		panelHoldingTabPanel.setVisible(false);

		panelHoldingTabPanel.setStyleName("panelHoldingTabPanel");

		RootPanel.get("nameFieldContainer").add(menu);
		RootPanel.get("sendButtonContainer").add(panelHoldingTabPanel);
		// RootPanel.get("errorLabelContainer").add(errorLabel);

		RootPanel.getBodyElement().setId("main");

		FormPanel uploadForm = upload();
		uploadBox = new DialogBox();
		uploadBox.setText("Ontology Upload");
		uploadBox.setAnimationEnabled(true);
		uploadBox.setWidget(uploadForm);
		uploadBox.hide();

		// Set default analysis level for SEMANTIXS as 1
		level = LEVEL_ONE;

		// Add a handler to close the DialogBox
		closeButton.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {

				dialogBox.hide();
				tabPanel.selectTab(2);
				sendButton.setEnabled(true);
				sendButton.setFocus(false);
			}
		});

		// Add a handler to close the AnnouncementBox
		announcementCloseButton.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {

				announcementBox.hide();
			}
		});

		// Create a handler for the sendButton and nameField
		class MyHandler implements ClickHandler, KeyUpHandler {
			/**
			 * Fired when the user clicks on the sendButton.
			 */
			public void onClick(ClickEvent event) {
				sendTextToServer();
			}

			/**
			 * Fired when the user types in the nameField.
			 */
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					sendTextToServer();
				}
			}

			/**
			 * Send the name from the nameField to the server and wait for a
			 * response.
			 */
			private void sendTextToServer() {
				// First, we validate the input.
				errorLabel.setText("");
				String textToServer = inputField.getText();

				if (!FieldVerifier.isValidName(textToServer)) {
					errorLabel
							.setText("Please enter a long enough sentence for SEMANTIXS to be able to map it to the ontology definitions");
					return;
				}

				dialogBoxLoadingBar.center();

				// Then, we send the input to the server.
				sendButton.setEnabled(false);

				textToServerLabel.setText(textToServer);
				serverResponseLabel.setText("");

				greetingService.handleInput(textToServer, level,
						new AsyncCallback<String>() {
							public void onFailure(Throwable caught) {
								// Show the RPC error message to the user

								dialogBoxLoadingBar.hide();
								sendButton.setEnabled(false);
								dialogBox
										.setText("Remote Procedure Call - Failure");
								serverResponseLabel
										.addStyleName("serverResponseLabelError");
								serverResponseLabel.setHTML(SERVER_ERROR);
								dialogBox.center();
								closeButton.setFocus(true);
							}

							public void onSuccess(String result) {

								dialogBoxLoadingBar.hide();
								sendButton.setEnabled(false);
								dialogBox.setText("SEMANTIXS response:");
								serverResponseLabel
										.removeStyleName("serverResponseLabelError");

								serverResponseLabel
										.setHTML("<b>SEMANTIXS has finished processing the text. Please view the extracted results under - \"View Extracted Semantic Data\" tab.</b>");
								resultField.setText(result);
								dialogBox.center();
								closeButton.setFocus(true);
							}
						});
			}
		}

		// Add a handler to send the name to the server
		MyHandler handler = new MyHandler();
		sendButton.addClickHandler(handler);
		inputField.addKeyUpHandler(handler);
	}

	public Widget createMenu() {

		Command menuCommandNws = new Command() {

			public void execute() {

				tabPanel.selectTab(0);
				inputField.selectAll();
				resultField.selectAll();
				panelHoldingTabPanel.setVisible(true);
			}
		};

		Command menuCommandLom = new Command() {

			public void execute() {

				uploadBox.center();
			}
		};

		Command menuCommandEx = new Command() {

			public void execute() {

				tabPanel.selectTab(0);
				inputField
						.setText("Input some text based on the uploaded ontology for analysis.\n\n [PS: Remember to upload an ontology model for your domain to get relevant results.");
				resultField
						.setText("Send some text for processing before trying to view the results in this tab.");
				level = LEVEL_ONE;
				panelHoldingTabPanel.setVisible(false);
			}
		};

		Command menuCommandPod = new Command() {

			public void execute() {

				announcementLabel
						.setHTML("<p align=\"center\"><b>SEMANTIXS Analysis Level has been set to 1 (default).</b><br/><br/>"
								+ "In this setting, SEMANTIXS expects an rdf/owl file as input (via upload option under File menu), containing the domain ontology (TBOX: class concepts + relationships) as well as instance assertions (A-BOX) that you expect in text.</p>");

				announcementBox.center();

				level = LEVEL_ONE;
			}
		};

		Command menuCommandUone = new Command() {

			public void execute() {

				announcementLabel
						.setHTML("<p align=\"center\"><b>SEMANTIXS Analysis Level has been set to 2.</b><br/><br/>"
								+ "In this setting, SEMANTIXS still expects an rdf/owl file as input (via upload option under File menu), however it doesn't assume that it's complete in the sense of covering all the instances expected in text. "
								+ "It thus utilizes some pre-generated instance data (for eg., FOAF, DBPedia) in addition "
								+ "to the given ontology for acquiring knowledge, however in the current version, it won't attempt to automatically map those instances to the input ontology and instead, retain their original references.</center>");

				announcementBox.center();

				level = LEVEL_TWO;
			}
		};

		Command menuCommandUowe = new Command() {

			public void execute() {

				announcementLabel
						.setHTML("<p align=\"center\"><b>SEMANTIXS Analysis Level has been set to 3.</b><br/><br/>"
								+ "In this setting, SEMANTIXS still expects an rdf/owl file as input (via upload option under File menu), however it doesn't assume that it's complete in the sense of covering all the relationships expected in text, or the instance data. "
								+ "Thus, it sacrifices some correctness for exploring wilderness in trying to enrich the given ontology by learning new relationships and possible classes / instances as it reads text.</center>");

				announcementBox.center();

				level = LEVEL_THREE;
			}
		};

		Command menuCommandVsg = new Command() {

			public void execute() {

				Window
						.open(
								"http://"
										+ SEMTUS_HOSTING_SERVER_NAME
										+ ":"
										+ SEMTUS_HOSTING_PORT_NAME
										+ "/"
										+ SEMTUS_APP_NAME
										+ "/visualize?lang=visual&model=output.rdf&search=&submit=search",
								"_blank",
								"menubar=no,location=no,resizable=yes,scrollbars=yes,status=no");
			}
		};

		Command menuCommandVcg = new Command() {

			public void execute() {

				tabPanel.selectTab(0);

				dialogBox
						.setHTML("<p align=\"center\"><font color=\"#000000\" face=\"verdana\" size=\"2\">Visualize Complete RDF Graph</font></p>");

				serverResponseLabel.removeStyleName("serverResponseLabelError");

				serverResponseLabel
						.setHTML("<p align=\"center\">"
								+ "In order to visualize the entire RDF graph, please click on the following button to go to the W3 Visualization servlet."
								+ " On the servlet page, please copy-paste the RDF structure generated by SEMANTIXS into the textarea titled - <b>Check by Direct Input</b>."
								+ " Set the <b>Display Result Options</b> to <b>Graph only</b> and click on <b>Parse RDF</b> to generate the graph.</p>");

				w3Button = new Button("Go to W3 Validator Servlet");

				w3Button.getElement().setId("closeButton");

				dialogVPanel.remove(closeButton);

				dialogVPanel.add(w3Button);

				dialogBox.center();

				// Add a handler to invoke W3 Servlet
				w3Button.addClickHandler(new ClickHandler() {

					public void onClick(ClickEvent event) {

						Window
								.open("http://www.w3.org/RDF/Validator/",
										"_blank",
										"menubar=no,location=no,resizable=yes,scrollbars=yes,status=no");

						dialogBox.hide();

						dialogVPanel.remove(w3Button);

						dialogVPanel.add(closeButton);
					}
				});
			}
		};

		Command menuCommandSo = new Command() {

			public void execute() {

			}
		};

		Command menuCommandUg = new Command() {

			public void execute() {

				panelHoldingTabPanel.setVisible(true);
			}
		};

		Command menuCommandAs = new Command() {

			public void execute() {

				tabPanel.selectTab(0);

				dialogBox
						.setHTML("<p align=\"center\"><font color=\"#000000\" face=\"verdana\" size=\"2\">About SEMANTIXS</font></p>");

				serverResponseLabel.removeStyleName("serverResponseLabelError");

				serverResponseLabel
						.setHTML("<p align=\"center\"><font color=\"#a29364\" face=\"Times\" size=\"6\">"
								+ "<b>SEMANTIXS</b></font>"
								+ "<br/><font color=\"#a29364\" face=\"Times\" size=\"3\">version 1.0.0</font><br/><br/></br></br>"
								+ "Copyright (c) <a href=\"http://sushain.com\" target=\"_blank\">Sushain Pandit</a>. All rights reserved."

								+ "<br/>"
								+ "<a href=\"http://www.cs.iastate.edu/~honavar/aigroup.html\" target=\"_blank\">Artificial Intelligence Research Lab</a>, Iowa State University.</p>");

				dialogBox.center();
			}
		};

		// Create a menu bar
		MenuBar menu = new MenuBar();
		menu.setAutoOpen(true);
		menu.setWidth("400px");
		menu.setAnimationEnabled(true);

		// menu.setStylePrimaryName("menu");

		// Create the file menu
		MenuBar fileMenu = new MenuBar(true);
		fileMenu.setAnimationEnabled(true);
		menu.addItem(new MenuItem("File", fileMenu));

		String[] fileOptions = new String[] { "New Workspace",
				"Load Ontology Model", "Exit" };

		fileMenu.addItem(fileOptions[0], menuCommandNws);
		fileMenu.addSeparator();
		fileMenu.addItem(fileOptions[1], menuCommandLom);
		fileMenu.addSeparator();
		fileMenu.addItem(fileOptions[2], menuCommandEx);

		menu.addSeparator();

		// Create the edit menu
		MenuBar editMenu = new MenuBar(true);
		menu.addItem(new MenuItem("Analysis Complexity", editMenu));
		String[] editOptions = new String[] {
				"Level 1 (default) - Purely Ontology Dependent [No Enrichment]",
				"Level 2 - Utilizes Ontology + other sources [No Enrichment]",
				"Level 3 - Utilizes Ontology + other sources [With Enrichment]" };

		editMenu.addItem(editOptions[0], menuCommandPod);
		editMenu.addSeparator();
		editMenu.addItem(editOptions[1], menuCommandUone);
		editMenu.addSeparator();
		editMenu.addItem(editOptions[2], menuCommandUowe);

		menu.addSeparator();

		// Create the GWT menu
		MenuBar gwtMenu = new MenuBar(true);
		menu.addItem(new MenuItem("User Options", true, gwtMenu));
		String[] gwtOptions = new String[] { "Analyze RDF sub-graphs",
				"Visualize complete RDF graph", "Output file in .rdf Format" };

		gwtMenu.addItem(gwtOptions[0], menuCommandVsg);
		gwtMenu.addSeparator();
		gwtMenu.addItem(gwtOptions[1], menuCommandVcg);
		gwtMenu.addSeparator();
		gwtMenu.addItem(gwtOptions[2], menuCommandSo);

		// Create the help menu
		MenuBar helpMenu = new MenuBar(true);
		menu.addSeparator();
		menu.addItem(new MenuItem("Help", helpMenu));
		String[] helpOptions = new String[] { "User Guide", "About SEMANTIXS" };

		helpMenu.addItem(helpOptions[0], menuCommandUg);
		helpMenu.addSeparator();
		helpMenu.addItem(helpOptions[1], menuCommandAs);

		// Return the menu
		menu.ensureDebugId("cwMenuBar");

		return menu;
	}

	public FormPanel upload() {

		final FormPanel form = new FormPanel();
		form.setAction(GWT.getModuleBaseURL() + "upload");

		// Because we're going to add a FileUpload widget, we'll need to set the
		// form to use the POST method, and multipart MIME encoding.
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
		form.setMethod(FormPanel.METHOD_POST);

		// Create a panel to hold all of the form widgets.
		HorizontalPanel panel = new HorizontalPanel();
		form.setWidget(panel);

		// Create a FileUpload widget.
		final FileUpload upload = new FileUpload();
		upload.setName("uploadFormElement");
		panel.add(upload);

		panel.add(new HTML("&nbsp;&nbsp;"));

		// Add a 'submit' button.
		panel.add(new Button("Upload", new ClickHandler() {

			public void onClick(ClickEvent event) {

				String filename = upload.getFilename();

				if (filename.length() == 0) {

					Window.alert("No File Specified");

				} else {

					form.submit();
				}
			}
		}));

		panel.add(new HTML("&nbsp;"));

		// Add a 'Done' button.
		panel.add(new Button("Done", new ClickHandler() {
			public void onClick(ClickEvent event) {

				uploadBox.hide();
				tabPanel.selectTab(0);
			}
		}));

		// Add an event handler to the form.
		form.addSubmitHandler(new FormPanel.SubmitHandler() {
			public void onSubmit(SubmitEvent event) {
			}
		});

		form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
			public void onSubmitComplete(SubmitCompleteEvent event) {

				// Check if there's been an error in upload
				if (event.getResults() == null) {

					Window
							.alert("Ontology Upload Failed. Please give us feedback [sushain.pandit@gmail.com] explaining the parameters of upload (filesize, format, etc) and we'll try to get it resolved asap.");

				} else if (event.getResults().toLowerCase().contains(
						"file already exists")) { // if it's already present on
													// server, then we're just
													// fine.

					Window
							.alert("You just uploaded this ontology. Please proceed to the workspace for text input. Thanks !");

				} else if (event.getResults().toLowerCase().contains("error")
						|| event.getResults().toLowerCase().contains(
								"not available")) { // o'wise flag an error

					Window
							.alert("Ontology Upload Failed. Please give us feedback [sushain.pandit@gmail.com] explaining the parameters of upload (filesize, format, etc) and we'll try to get it resolved asap.");

				} else {

					Window.alert(event.getResults());
					Window.alert("Ontology Upload Successful");
				}
			}
		});

		return form;
	}
}

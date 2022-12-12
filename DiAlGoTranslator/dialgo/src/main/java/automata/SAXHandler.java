package automata;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
/**
 * SAXHandler for parsing a timed automata network from UPPAAL by an XML File. 
 * @author Torben Friedrich Goerner
 */
public class SAXHandler extends DefaultHandler {
	
	/**
	 * automata network object to build by an uppaal xml
	 */
	private AutomataNetwork automataNetwork;
	
	/**
	 * temporary template to add in automaton
	 */
	private Template tempTemplate;
	/**
	 * temporary location to add in automaton
	 */
	private Location tempLocation;
	/**
	 * temporary transition to add in automaton
	 */
	private Transition tempTransition;
	/**
	 * temporary label to add in automaton
	 */
	private Label tempLabel;
	/**
	 * temporary nail to add to transition 
	 */
	private Nail tempNail;
	/**
	 * temporary query for verification 
	 */
	private Query tempQuery;
	
	/**
	 * name/type of the actual temporary object
	 */
	private String tempObjectName;
	/**
	 * temporary context String
	 */
	private String tempContentText;
	
	
	public SAXHandler() {
		automataNetwork = new AutomataNetwork();
		tempTemplate = null;
		tempLocation = null;
		tempTransition = null;
		tempLabel = null;
		tempNail = null;
		tempQuery = null;
		tempObjectName = "";
		tempContentText = "";
	}
	
	@Override
    public void startDocument() throws SAXException {
		automataNetwork = new AutomataNetwork();
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    	
		switch (qName) {
		
		case "template":
			tempTemplate = new Template();
			break;

		case "location":
			tempLocation = new Location();
			tempLocation.setId(attributes.getValue(0));
			tempLocation.setPosX(Integer.parseInt(attributes.getValue(1)));
			tempLocation.setPosY(Integer.parseInt(attributes.getValue(2)));
			tempLocation.setColor(attributes.getValue(3));
			break;

		case "transition":
			tempTransition = new Transition();
			break;

		case "label":
			tempLabel = new Label();
			tempLabel.setKind(attributes.getValue(0));
			tempLabel.setPosX(Integer.parseInt(attributes.getValue(1)));
			tempLabel.setPosY(Integer.parseInt(attributes.getValue(2)));
			break;
			
		case "source" :
			tempTransition.setSource(attributes.getValue(0));
			break;
			
		case "target" :
			tempTransition.setTarget(attributes.getValue(0));
			break;
			
		case "init":
			tempTemplate.getLocationById(attributes.getValue(0)).setInitial(true);
			break;
			
		case "urgent" : 
			tempLocation.setUrgent(true);
			break;
			
		case "committed" :
			tempLocation.setCommitted(true);
			break;
			
		case "nail" :
			tempNail = new Nail();
			tempNail.setPosX(Integer.parseInt(attributes.getValue(0)));
			tempNail.setPosY(Integer.parseInt(attributes.getValue(1)));
			break;
			
		case "name" :
			if(tempLocation != null) {
				tempLocation.setPosXName(Integer.parseInt(attributes.getValue(0)));
				tempLocation.setPosYName(Integer.parseInt(attributes.getValue(1)));
			}
			break;
		
		case "query" :
			tempQuery = new Query();
			break;

		default:
			break;
		}
    	
    	tempObjectName = qName;
    	tempContentText = "";
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

		switch (qName) {
		
		case "template":
			automataNetwork.addTemplate(tempTemplate);
			tempTemplate = null;
			break;

		case "location":
			tempTemplate.addLocation(tempLocation);
			tempLocation = null;
			break;

		case "transition":
			tempTemplate.addTransition(tempTransition);
			tempTransition = null;
			break;

		case "label":
			if(tempLocation == null) {
				tempTransition.addLabel(tempLabel);
			}else {
				tempLocation.setLabel(tempLabel);
			}
			
			tempLabel = null;
			break;
			
		case "nail" :
			if(tempTransition != null) tempTransition.addNail(tempNail);
			tempNail = null;
			break;
			
		case "query" :
			automataNetwork.addQuery(tempQuery);
			tempQuery = null;
			break;

		default:
			break;
		}
		
    }
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {

    	tempContentText += new String(ch, start, length).replaceAll("\t", "");
    	
    	switch(tempObjectName) {
    	
    	case "parameter":
			tempTemplate.setParametes(tempContentText);
			break;
    	
    	case "declaration" :
    		if(tempTemplate == null) {
    			automataNetwork.setDeclaration(tempContentText);
    		}else {
    			tempTemplate.setDeclaration(tempContentText);
    		}
    		break;
    		
		case "name":
			if(tempLocation == null) {
				if(tempTemplate.getName().equals("")) {
					tempTemplate.setName(tempContentText.replaceAll("\n", ""));
				}
			}else {
				tempLocation.setName(tempContentText.replaceAll("\n", ""));
			}
			break;
		
		case "label" :
			if(tempLabel != null) {
				tempLabel.setContent(tempContentText);
			}
			break;
			
		case "system" :
			automataNetwork.setSystemDeclaration(tempContentText);
			break;
			
		case "formula" :
			if(tempQuery != null) {
				tempQuery.setFormula(tempContentText);
			}
			break;
			
		case "comment" :
			if(tempQuery != null) {
				tempQuery.setComment(tempContentText);
			}
			break;
    		
    	default : 
    		break;
    	}
    	
    }

	public AutomataNetwork getAutomataNetwork() {return this.automataNetwork;}

}

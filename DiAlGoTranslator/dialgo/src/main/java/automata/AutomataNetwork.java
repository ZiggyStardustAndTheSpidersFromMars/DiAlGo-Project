package automata;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
/**
 * Timed automata network object with SAXHandler to build a new automaton by XML File.
 * @author Torben Friedrich Goerner
 */
public class AutomataNetwork {

	/**
	 * global declaration
	 */
	private String declaration;
	/**
	 * automaton templates from uppaal
	 */
	private List<Template> templates;
	/**
	 * system declarations from uppaal
	 */
	private String systemDeclaration;
	/**
	 * querys for verification in uppaal
	 */
	private List<Query> querys;
	
	private static SAXHandler handler;
	

	public AutomataNetwork() {
		this.declaration = "";
		this.templates = new LinkedList<>();
		this.querys = new LinkedList<>();
	}
	
	/**
	 * creates a automaton from a xml file
	 * @param path to file
	 * @return automaton
	 */
	public static AutomataNetwork fromXML(String path) {
		
		AutomataNetwork automataNetwork = null;
		SAXParser parser = null;
		
        try {
        	
        	SAXParserFactory factory = SAXParserFactory.newInstance();
        	factory.setValidating(false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); // ignore external dtd 
        	
            parser = factory.newSAXParser();
            handler = new SAXHandler();
            File file = new File(path);
            
            parser.parse(file, handler);

            automataNetwork = handler.getAutomataNetwork();

        } catch (ParserConfigurationException e) {
            System.err.println("ERROR : SAX PARSER CONFIGURATION FAILED!\n" + e);
            System.exit(-1);
        } catch (SAXException e) {
            System.err.println("ERROR : PLEASE MAKE SURE YOUR XML FILE IS AN UPPAAL CORRECT AUTOMATA NETWORK!\n" + e);
            System.exit(-1);
        } catch (IOException e) {
            System.err.println("ERROR : READING XML FILE FAILED!\n" + e);
            System.exit(-1);
        }
		
		return automataNetwork;
	}
	
	@Override
	public String toString() {
		return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + 
				"\r\n" + 
				"<nta>\r\n" + 
				"	<declaration>" + 
				this.declaration.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;")
				.replaceAll("\"", "&quot;").replaceAll("'", "&apos;") + 
				"</declaration>" +
				this.templates.stream().map(e -> e.toString()).reduce("", String::concat) +
				"<system>" + this.systemDeclaration + "</system>\r\n" +
				"<queries>\r\n" +
				this.querys.stream().map(e -> e.toString()).reduce("", String::concat) +
				"</queries>\r\n" +
				"</nta>";
	}
	
	public void addTemplate(Template template) {
		this.templates.add(template);
	}
	
	public void addQuery(Query query) {
		this.querys.add(query);
	}
	
	public void replaceTemplate(Template template) {
		for(Template t : templates) {
			if(t.getName().equals(template.getName())) {
				t = template;
				break;
			}
		}
	}
	
	public Template getTemplateByName(String name) {
		for(Template template : templates) {
			if(template.getName().equals(name)) {
				return template;
			}
		}
		return null;
	}

	//------------Getter and Setter-------------

	public String getDeclaration() {return this.declaration;}
	public void setDeclaration(String declaration) {this.declaration = declaration;}

	public List<Template> getTemplates() {return this.templates;}
	public void setTemplates(List<Template> templates) {this.templates = templates;}

	public String getSystemDeclaration() {return systemDeclaration;}
	public void setSystemDeclaration(String systemDeclaration) {this.systemDeclaration = systemDeclaration;}

}

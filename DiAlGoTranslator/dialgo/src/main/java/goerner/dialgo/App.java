package goerner.dialgo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import automata.AutomataNetwork;
import res.Commands;
import res.Messages;

/**
 * DiAlGo Translator
 * Part of Master-Thesis 2022
 * Torben Friedrich Goerner 
 * TH Luebeck
 * Prof. Dr. Andreas Schaefer
 * 
 * Simulation and Visualisation of Distributed Algorithms using GO and UPPAAL
 * 
 * Tool : DiAlGo Translator (Distributed Algorithms with GO and UPPAAL)
 *
 */
public class App {
	
    public static void main( String[] args ) {
        System.out.println(Messages.WELCOME_MSG + "\n\n");
        
        if(args.length == 0) { // Missing args
        	System.err.println(Messages.MISSING_ARGUMENTS + "\n");
        	System.out.println(Messages.USER_MANUAL);
        }else { // start chosen mode
        	switch(args[0].toLowerCase()) {
        		case Commands.CMD_HELP :
        			System.out.println(Messages.USER_MANUAL);
        			break;
        		case Commands.CMD_DROGGELBECHER :
        			System.out.println(Messages.DROGGELBECHER);
        			break;
        		case Commands.CMD_TRANSLATE :
        			doTranslation(args);
        			break;
        		case Commands.CMD_MODIFY:
        			Modifier.modify(args);
        			break;
        		default :
        			System.err.println("ERROR : NO MATCH FOR OPTION '" + args[0] 
        					+ "'!\nUSE '-help' TO SEE THE OPERATING INSTRUCTIONS.");
        			break;
        	}
        }
    }
    
    
    private static void doTranslation(String[] args) {
    	GoHandler goHandler = new GoHandler();
    	
    	try {
    		if(args.length >= 4 && args[3].equals(Commands.CMD_MARKER)) {
    			goHandler.doMarkerLocations(true);
    		}
    	}catch(Exception e) {}
    	
    	try {
			AutomataNetwork an = goHandler.parseAutomataNetworkFromGoFile(args[1], Integer.parseInt(args[2]));
			if(an != null) { 
				writeFile(an.toString(), args[1].replaceFirst("[.][^.]+$", ""), "xml"); 
			}
		} catch(IndexOutOfBoundsException e) {
			System.err.println(Messages.MISSING_ARGUMENTS);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public static void writeFile(String content, String name, String type) {
    	String path = name + "." + type;
    	try {
			Files.write( Paths.get(path), content.getBytes());
			System.out.println(name + "." + type + " " + Messages.WRITE_FILE); 
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
     
}
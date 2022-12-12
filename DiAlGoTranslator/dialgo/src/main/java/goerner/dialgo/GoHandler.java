package goerner.dialgo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import automata.AutomataNetwork;
import goparser.GoLexer;
import goparser.GoParser;
import goparser.GoParser.FunctionDeclContext;
import goparser.GoParser.SourceFileContext;
import goparser.GoParserBaseListener;
import res.Messages;

/**
 * Go Handler for parsing a GO file using a GoContextParser
 * @author Torben Friedrich Goerner
 *
 */
@SuppressWarnings("deprecation")
public class GoHandler {
	
	/**
	 * GoContextHandler for translating the AutomatonNetwork by contexts from the Go File parse tree
	 */
	private GoContextHandler goContextHandler = new GoContextHandler();
	
	/**
	 * Parses a GO File and builds an AutomataNetwork from the parsed contexts by 
	 * using the GoContextHandler.
	 * @param path to GO File
	 * @param nodes amount of nodes in the net
	 * @return AutomataNetwork
	 */
	public AutomataNetwork parseAutomataNetworkFromGoFile(String path, int nodes) {
		AutomataNetwork an = new AutomataNetwork();
		String goFile = readFile(path);
		
		if(goFile == null) { return null; }
		
		GoLexer lexer = new GoLexer(new ANTLRInputStream(goFile));
		GoParser parser = new GoParser(new CommonTokenStream(lexer));
		GoParserBaseListener listener = new GoParserBaseListener();
		parser.addParseListener(listener);

		SourceFileContext context = parser.sourceFile();
		
		// TEST AREA BEGIN
		//String parseTreeS = context.toStringTree(parser);
		//System.out.println("Parse Tree:\n" + parseTreeS);
		// TEST AREA END
		
		// Translate from Contexts with GoContextHandler
		FunctionDeclContext mainFunc = null;
		List<FunctionDeclContext> funcs = new LinkedList<>();
		for(FunctionDeclContext funcContext : context.functionDecl()) {
			if(funcContext.IDENTIFIER().getText().equals("main")) {
				mainFunc = funcContext;
			}else {
				funcs.add(funcContext);
			}
		}
		
		an = goContextHandler.addSystemDeclarations(an, mainFunc);
		an = goContextHandler.addTemplates(an, funcs);
		an = goContextHandler.addGlobalDeclarations(an, context, mainFunc, nodes);
		an = AutomataNetworkBuilder.addDialgoCode(an);
		an = goContextHandler.addTemplateLocalDeclarations(an, funcs);
		an = goContextHandler.addTemplateLogic(an, funcs);
		
		return an;
	}
	
	private String readFile(String path) {
		String content = "";
		Path filePath = Path.of(path);
		try {
			content = Files.readString(filePath);
		} catch (IOException e) {
			System.err.println(Messages.CANT_READ_FILE_ERROR);
			e.printStackTrace();
			return null;
		}
		return content;
	}
	
	public void doMarkerLocations(boolean doMarkerLocations) {
		goContextHandler.setDoMarkerLocations(doMarkerLocations);
	}
	
	public GoContextHandler getGoContextHandler() {
		return this.goContextHandler;
	}

}

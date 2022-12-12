package goerner.dialgo;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import automata.AutomataNetwork;
import automata.Template;
import goparser.GoParser.AssignmentContext;
import goparser.GoParser.BlockContext;
import goparser.GoParser.DeclarationContext;
import goparser.GoParser.ExpressionContext;
import goparser.GoParser.ForStmtContext;
import goparser.GoParser.FunctionDeclContext;
import goparser.GoParser.GoStmtContext;
import goparser.GoParser.IfStmtContext;
import goparser.GoParser.IncDecStmtContext;
import goparser.GoParser.RecvStmtContext;
import goparser.GoParser.SelectStmtContext;
import goparser.GoParser.ShortVarDeclContext;
import goparser.GoParser.SimpleStmtContext;
import goparser.GoParser.SourceFileContext;
import goparser.GoParser.StatementContext;
import res.Constants;
import res.Strings;

/**
 * Go Context Handler for translating go contexts 
 * and building components for the UPPAAL model (AutomataNetwork)
 * @author Torben Friedrich Goerner
 *
 */
public class GoContextHandler {
	
	/**
	 * Map of channel with their buffer size -> 0 = sync, 1 = async, 2 = async with buffer 
	 */
	private Map<String, Integer> channels; 
	/**
	 * Map of channels with their data types
	 */
	private Map<String, String> channelTypes;
	
	// Event counter variables
	private int asgmtCount;
	private int forCount;
	private int ifCount;
	private int elseCount;
	private int sendCount;
	private int receiveCount;
	private int randomBoolCount;
	private int randomIntCount;
	private int defaultCount;
	private int markerCount;
	
	private boolean doMarkerLocations;
	
	public GoContextHandler() {
		channels = new HashMap<>();
		channelTypes = new HashMap<>();
		resetEventCounter();
	}
	
	
	private void resetEventCounter() {
		asgmtCount = 0;
		forCount = 0;
		ifCount = 0;
		elseCount = 0;
		sendCount = 0;
		receiveCount = 0;
		randomBoolCount = 0;
		randomIntCount = 0;
		defaultCount = 0;
		markerCount = 0;
	}
	
	/**
	 * Builds the system declarations from context
	 * @param an
	 * @param mainContext
	 * @return
	 */
	public AutomataNetwork addSystemDeclarations(AutomataNetwork an, FunctionDeclContext mainContext) {
		List<GoStmtContext> funcCalls = new LinkedList<>();
		for(StatementContext stContext : mainContext.block().statementList().statement()) {
			if(stContext.goStmt() != null) {
				funcCalls.add(stContext.goStmt());
			}
		}
		
		List<String> names = new LinkedList<>();
		List<String> parameter = new LinkedList<>();
		for(GoStmtContext c : funcCalls) {
			BlockContext block = c.expression().primaryExpr().primaryExpr().operand().literal().functionLit().block();
			String funcName = block.statementList().statement(0).simpleStmt().expressionStmt().expression()
					.primaryExpr().primaryExpr().getText();
			String funcParameter = block.statementList().statement(0).simpleStmt().expressionStmt().expression()
					.primaryExpr().arguments().expressionList().expression(0).getText();
			names.add(funcName);
			parameter.add(funcParameter);
		}
		
		an = AutomataNetworkBuilder.setSystemDeclaration(an, names, parameter);
		return an;
	}
	
	/**
	 * Adds a new Template for a function
	 * @param an
	 * @param funcContexts
	 * @return
	 */
	public AutomataNetwork addTemplates(AutomataNetwork an, List<FunctionDeclContext> funcContexts) {
		for(FunctionDeclContext funcContext : funcContexts) {
			String name = funcContext.IDENTIFIER().getText();
			String parameters = "const int pid";
			AutomataNetworkBuilder.addTemplate(an, name, parameters, "");
		}
		return an;
	}
	
	/**
	 * Builds the local declaration of a template from context
	 * @param an
	 * @param funcContexts
	 * @return
	 */
	public AutomataNetwork addTemplateLocalDeclarations(AutomataNetwork an, List<FunctionDeclContext> funcContexts) {
		String decl = "";
		for(FunctionDeclContext funcContext : funcContexts) {
			List<StatementContext> statementContexts = funcContext.block().statementList().statement();
			for(StatementContext statementContext : statementContexts) {
				try {
					if(statementContext.declaration().varDecl() != null) { 
						if (statementContext.declaration().varDecl().varSpec(0).type_().typeLit() != null) { // Array Type
							String type = statementContext.declaration().varDecl().varSpec(0).type_().getText()
									.substring(statementContext.declaration().varDecl().varSpec(0).type_().getText()
											.indexOf("]") + 1);
							String array = statementContext.declaration().varDecl().varSpec(0).type_().getText()
									.substring(0, statementContext.declaration().varDecl().varSpec(0).type_().getText()
											.indexOf("]") + 1);
							String stmt = type + " "
									+ statementContext.declaration().varDecl().varSpec(0).identifierList().getText()
									+ array
									+ ";";
							decl += stmt + "\r\n";
						} else { // Normal Type
							String stmt = statementContext.declaration().varDecl().varSpec(0).type_().getText() + " "
									+ statementContext.declaration().varDecl().varSpec(0).identifierList().getText()
									+ " = "
									+ statementContext.declaration().varDecl().varSpec(0).expressionList().getText()
									+ ";";
							decl += stmt + "\r\n";
						}
					}
				}catch(Exception e) {}
			}
			decl = "int broadcastCounter = 0;\r\nbool sendLossy = false;\r\n\r\n" + decl;
			an.getTemplateByName(funcContext.IDENTIFIER().getText()).setDeclaration(decl);
			decl = "";
		}
		return an;
	}
	
	/**
	 * Builds the global declarations from context
	 * @param an
	 * @param context
	 * @param mainFunc
	 * @param nodes
	 * @return
	 */
	public AutomataNetwork addGlobalDeclarations(AutomataNetwork an, SourceFileContext context, FunctionDeclContext mainFunc, int nodes) {
		int edges = nodes * (nodes - 1); //n*(n-1)
		an = AutomataNetworkBuilder.setAmountOfNodesAndEdges(an, nodes, edges);
		
		List<DeclarationContext> declarations = context.declaration();
		for(DeclarationContext decl : declarations) {
			if (decl.constDecl() != null) {
				String constDecl = "const ";
				constDecl += decl.constDecl().constSpec(0).type_().getText() + " ";
				constDecl += decl.constDecl().constSpec(0).identifierList().getText() + " = ";
				constDecl += decl.constDecl().constSpec(0).expressionList().getText() + ";";
				an = AutomataNetworkBuilder.addGlobalDeclaration(an, constDecl);
			} else if (decl.varDecl() != null) {
				if(decl.varDecl().varSpec(0).type_().typeLit().arrayType() != null) { // Array
					if(decl.varDecl().varSpec(0).type_().typeLit().arrayType().elementType().type_().typeLit().channelType() != null) { // Channel
						String name = decl.varDecl().varSpec(0).identifierList().getText();
						int bufferSize = 0;
						String type = "int";
						// parse type of channel -> prefix in identifier -> string_<name> 
						if(name.contains("_")) {
							type = name.substring(0, name.indexOf("_"));
						}
						channelTypes.put(name, type);
						// parse bufferSize -> 0 - sync, 1 - async, n - async with buffer
						for(StatementContext stmt : mainFunc.block().statementList().statement()) {
							if(stmt.forStmt() != null) {
								if(stmt.forStmt().rangeClause().expression().primaryExpr().operand().operandName().getText().equals(name)) {
									bufferSize = stmt.forStmt().block().statementList().statement(0).simpleStmt().assignment().expressionList(1).expression(0).primaryExpr().arguments().expressionList() 
											!= null ? Integer.parseInt(stmt.forStmt().block().statementList().statement(0).simpleStmt().assignment().expressionList(1).expression(0).primaryExpr().arguments().expressionList().getText()) : 0;
								}
							}
						};
						switch(bufferSize) {
							case 0 :
								an = AutomataNetworkBuilder.addSyncChan(an, name, type);
								channels.put(name, 0);
								break;
							case 1 :
								an = AutomataNetworkBuilder.addAsyncChan(an, name, type);
								channels.put(name, 1);
								break;
							default : 
								an = AutomataNetworkBuilder.addAsyncBufferChan(an, name, type, bufferSize);
								channels.put(name, 2);
								break;
						}
					}
				}
			}
		}
		
		return an;
	}
	
	/**
	 * Builds the Logic of all given templates from contexts
	 * @param an
	 * @param funcContexts
	 * @return
	 */
	public AutomataNetwork addTemplateLogic(AutomataNetwork an, List<FunctionDeclContext> funcContexts) {
		for(FunctionDeclContext funcContext : funcContexts) {
			Template template = an.getTemplateByName(funcContext.IDENTIFIER().getText());
			resetEventCounter();
			for(StatementContext stmt : funcContext.block().statementList().statement()) {
				String locationId = template.getHighestLocationId();
				an.replaceTemplate(addStatement(template, stmt, locationId));
			}
		}
		return an;
	}
	
	/**
	 * Builds the statements from context
	 * @param template
	 * @param stmt
	 * @param locationId
	 * @return
	 */
	private Template addStatement(Template template, StatementContext stmt, String locationId) {
		if(stmt.forStmt() != null) {
			template = addFor(template, stmt.forStmt(), locationId);
		}else if(stmt.ifStmt() != null) {
			template = addIfElse(template, stmt.ifStmt(), locationId);
		}else if(stmt.simpleStmt() != null) {
			template = addSimpleStmt(template, stmt.simpleStmt(), locationId);
		}else if(stmt.selectStmt() != null) {
			template = addReceiveSelect(template, stmt.selectStmt(), locationId);
		}
		return template;
	}
	
	/**
	 * Builds the simple statements from context
	 * @param template
	 * @param stmt
	 * @param locationId
	 * @return
	 */
	private Template addSimpleStmt(Template template, SimpleStmtContext stmt, String locationId) {
		// Assignment Action
		if (stmt.assignment() != null) return addAssignment(template, stmt.assignment(), locationId);
		
		// Inc Dec Stmt Action
		if(stmt.incDecStmt() != null) return addIncDecStmt(template, stmt.incDecStmt(), locationId);
		
		try { // Send Action
			if(stmt.expressionStmt().expression().primaryExpr().primaryExpr().operand().operandName().getText().equals("Send")) {
				template = addSend(template, stmt.expressionStmt().expression(), locationId);
				return template;
			}
		}catch(Exception e) {}
		
		try { // Receive Action
			if(stmt.shortVarDecl().expressionList().expression(0).getText().contains("<-")) {
				template = addReceive(template, stmt.shortVarDecl(), locationId, 0);
				return template;
			}
		}catch(Exception e) {}
		
		try { // Broadcast Action
			if(stmt.expressionStmt().expression().primaryExpr().primaryExpr().operand().operandName().getText().equals("Broadcast")) {
				template = addBroadcast(template, stmt.expressionStmt().expression(), locationId);
				return template;
			}
		}catch(Exception e) {}
		
		try { // Broadcast Lossy Action
			if(stmt.expressionStmt().expression().primaryExpr().primaryExpr().operand().operandName().getText().equals("BroadcastLossy")) {
				template = addBroadcastLossy(template, stmt.expressionStmt().expression(), locationId);
				return template;
			}
		}catch(Exception e) {}
		
		try { // Send Lossy Action
			if(stmt.expressionStmt().expression().primaryExpr().primaryExpr().operand().operandName().getText().equals("SendLossy")) {
				template = addSendLossy(template, stmt.expressionStmt().expression(), locationId);
				return template;
			}
		}catch(Exception e) {}
		
		try { // Fmt Statement (Print)
			if(stmt.expressionStmt().expression().primaryExpr().primaryExpr().primaryExpr().operand().operandName().getText().equals("fmt")) {
				template = addMarker(template, stmt, locationId);
			}
		}catch(Exception e) {}
		
		// other SimpleStmt handling can be added here
		
		return template;
	}

	/**
	 * Builds the send component from context
	 * @param template
	 * @param stmt
	 * @param locationId
	 * @return
	 */
	private Template addSend(Template template, ExpressionContext stmt, String locationId) {
		String chan = stmt.primaryExpr().arguments().expressionList().expression(2).getText();
		String pidB = stmt.primaryExpr().arguments().expressionList().expression(1).getText();
		String msg = stmt.primaryExpr().arguments().expressionList().expression(3).getText();
		sendCount++;
		if(channels.get(chan) == 0) { // sync
			template = AutomataNetworkBuilder.addSendActionSync(template, pidB, msg, chan, locationId, sendCount);
		}else if(channels.get(chan) == 1) { // async
			template = AutomataNetworkBuilder.addSendActionAsync(template, pidB, msg, chan, locationId, sendCount);
		}else if(channels.get(chan) == 2) { // async with buffer
			template = AutomataNetworkBuilder.addSendActionAsyncWithBuffer(template, pidB, msg, chan, locationId, sendCount);
		}
		return template;
	}
	
	/**
	 * Builds the receive component from context
	 * @param template
	 * @param stmt
	 * @param locationId
	 * @param offset
	 * @return
	 */
	private Template addReceive(Template template, ShortVarDeclContext stmt, String locationId, int offset) {
		String chan = stmt.expressionList().expression(0).expression(0).primaryExpr().primaryExpr().operand().getText();
		String pidB = stmt.expressionList().expression(0).expression(0).primaryExpr().index().expression().primaryExpr().arguments().expressionList().expression(1).getText();
		String var = stmt.identifierList().getText();
		receiveCount++;
		if(channels.get(chan) == 0) { // sync
			template = AutomataNetworkBuilder.addReceiveActionSync(template, pidB, var, channelTypes.get(chan), chan, locationId, offset, receiveCount);
		}else if(channels.get(chan) == 1) { // async
			template = AutomataNetworkBuilder.addReceiveActionAsync(template, pidB, var, channelTypes.get(chan), chan, locationId, offset, receiveCount);
		}else if(channels.get(chan) == 2) { // async with buffer
			template = AutomataNetworkBuilder.addReceiveActionAsyncWithBuffer(template, pidB, var, channelTypes.get(chan), chan, locationId, offset, receiveCount);
		}
		return template;
	}
	
	/**
	 * Builds the receive component from context
	 * @param template
	 * @param stmt
	 * @param locationId
	 * @param offset
	 * @return
	 */
	private Template addReceiveFromRecvStmt(Template template, RecvStmtContext stmt, String locationId, int offset) {
		String chan = stmt.expression().expression(0).primaryExpr().primaryExpr().operand().getText();
		String pidB = stmt.expression().expression(0).primaryExpr().index().expression().primaryExpr().arguments().expressionList().expression(1).getText();
		String var = stmt.identifierList().getText();
		receiveCount++;
		if(channels.get(chan) == 0) { // sync
			template = AutomataNetworkBuilder.addReceiveActionSync(template, pidB, var, channelTypes.get(chan), chan, locationId, offset, receiveCount);
		}else if(channels.get(chan) == 1) { // async
			template = AutomataNetworkBuilder.addReceiveActionAsync(template, pidB, var, channelTypes.get(chan), chan, locationId, offset, receiveCount);
		}else if(channels.get(chan) == 2) { // async with buffer
			template = AutomataNetworkBuilder.addReceiveActionAsyncWithBuffer(template, pidB, var, channelTypes.get(chan), chan, locationId, offset, receiveCount);
		}
		return template;
	}
	
	/**
	 * Builds the GO Select component from context
	 * @param template
	 * @param stmt
	 * @param locationId
	 * @return
	 */
	private Template addReceiveSelect(Template template, SelectStmtContext stmt, String locationId) {
		int amountOfCases = stmt.commClause().size();
		boolean lastCaseIsDefault = stmt.commClause(amountOfCases - 1).commCase().DEFAULT() != null;
		
		List<Integer> lastLocations = new LinkedList<>();
		
		String defaultCondition = "";
		
		for(int i = 0; i < amountOfCases; i++) {
			int offset = 0;
			int mid = amountOfCases / 2;
			if(i < mid) { // left offset
    			offset = Constants.RECEIVE_OFFSET * (mid - i) * -1;
    		}else if(i == mid) { // mid offset
    			offset = 0;
    		}else { // right offset
    			offset = Constants.RECEIVE_OFFSET * (i - mid);
    		}
			if(!lastCaseIsDefault || (lastCaseIsDefault && i != amountOfCases - 1)) { // normal case
				template = addReceiveFromRecvStmt(template, stmt.commClause(i).commCase().recvStmt(), locationId, offset);
				String chan = stmt.commClause(i).commCase().recvStmt().expression().expression(0).primaryExpr().primaryExpr().operand().getText();
				String pidB = stmt.commClause(i).commCase().recvStmt().expression().expression(0).primaryExpr().index().expression().primaryExpr().arguments().expressionList().expression(1).getText();
				defaultCondition += defaultCondition.equals("") ? chan + "Size[getReceiveIndex(pid," + pidB + ")] == 0" 
						: "\r\n&& " + chan + "Size[getReceiveIndex(pid," + pidB + ")] == 0" ;
			}else { // default case
				String chan = stmt.commClause(0).commCase().recvStmt().expression().expression(0).primaryExpr().primaryExpr().operand().getText();
				defaultCount++;
				if(channels.get(chan) == 0) {
					template = AutomataNetworkBuilder.addSelectDefaultBlock(template, stmt, locationId, offset, defaultCount);
				}else {
					template = AutomataNetworkBuilder.addSelectDefaultBlockWithCondition(template, stmt, locationId, offset, defaultCondition, defaultCount);	
				}
			}
			
			if( stmt.commClause(i).statementList() != null) {
				for(StatementContext stmtContext : stmt.commClause(i).statementList().statement()) {
					template = addStatement(template, stmtContext, template.getHighestLocationId());
				}
			}
			lastLocations.add(template.getLocations().size() - 1);
		}
		
		// Merge Branches
		template = AutomataNetworkBuilder.addMergeBranches(template, lastLocations, template.getLocationById(locationId).getPosX());
		
		return template;
	}
	
	/**
	 * Builds the If/Else component from context
	 * @param template
	 * @param stmt
	 * @param locationId
	 * @return
	 */
	private Template addIfElse(Template template, IfStmtContext stmt, String locationId) {
		List<Integer> locationIds = new LinkedList<>();
		List<Integer> lastLocationIdsInBlocks = new LinkedList<>();
		boolean ifElse = stmt.block(1) != null;
		boolean simpleIf = stmt.block(1) == null && stmt.ifStmt() == null;
		
		if(ifElse) { // If with Else Block
			// Add if
			locationIds.add(template.getLocations().size()); // if location
			ifCount++;
			template = AutomataNetworkBuilder.addConditionBlock(template, stmt.expression().getText(), locationId, Strings.IF, Strings.LEFT_SHIFT, ifCount);			
			// Build if block
			int stmtCount = 0;
			for(StatementContext statemant : stmt.block(0).statementList().statement()) {
				if(stmtCount == 0) {
					template = addStatement(template, statemant, "id" + locationIds.get(0));
				}else {
					template = addStatement(template, statemant, "id" + (template.getLocations().size() - 1));
				}
				stmtCount++;
			}
			lastLocationIdsInBlocks.add(Integer.parseInt(template.getHighestLocationId().replaceAll("id", "")));
			stmtCount = 0;
			// Add else
			locationIds.add(template.getLocations().size()); // else location
			elseCount++;
			template = AutomataNetworkBuilder.addConditionBlock(template, "!(" + stmt.expression().getText() + ")", locationId, Strings.ELSE, Strings.RIGHT_SHIFT, elseCount);
			// Build else block
			for(StatementContext statemant : stmt.block(1).statementList().statement()) {
				if(stmtCount == 0) {
					template = addStatement(template, statemant, "id" + locationIds.get(1));
				}else {
					template = addStatement(template, statemant, "id" + (template.getLocations().size() - 1));
				}
				stmtCount++;
			}
			lastLocationIdsInBlocks.add(Integer.parseInt(template.getHighestLocationId().replaceAll("id", "")));
			// Merge branches
			template = AutomataNetworkBuilder.addMergeBranches(template, lastLocationIdsInBlocks, template.getLocationById(locationId).getPosX());
		}else if(simpleIf){ // If without Else Block
			// Add if
			locationIds.add(template.getLocations().size()); // if location
			ifCount++;
			template = AutomataNetworkBuilder.addConditionBlock(template, stmt.expression().getText(), locationId, Strings.IF, Strings.LEFT_SHIFT, ifCount);
			// Build if block
			int stmtCount = 0;
			for (StatementContext statemant : stmt.block(0).statementList().statement()) {
				if (stmtCount == 0) {
					template = addStatement(template, statemant, "id" + locationIds.get(0));
				} else {
					template = addStatement(template, statemant, "id" + (template.getLocations().size() - 1));
				}
				stmtCount++;
			}
			lastLocationIdsInBlocks.add(Integer.parseInt(template.getHighestLocationId().replaceAll("id", "")));
			stmtCount = 0;
			// Add else
			locationIds.add(template.getLocations().size()); // else location
			elseCount++;
			template = AutomataNetworkBuilder.addConditionBlock(template, "!(" + stmt.expression().getText() + ")", locationId, Strings.ELSE, Strings.RIGHT_SHIFT, elseCount);
			lastLocationIdsInBlocks.add(Integer.parseInt(template.getHighestLocationId().replaceAll("id", "")));
			// Merge branches
			template = AutomataNetworkBuilder.addMergeBranches(template, lastLocationIdsInBlocks, template.getLocationById(locationId).getPosX());
		} else { // If with Else If Blocks
			List<Integer> elseLocations = new LinkedList<>();
			// Add if
			locationIds.add(template.getLocations().size()); // if location
			ifCount++;
			template = AutomataNetworkBuilder.addConditionBlock(template, stmt.expression().getText(), locationId, Strings.IF,Strings.LEFT_SHIFT, ifCount);
			// Build if block
			int stmtCount = 0;
			for (StatementContext statemant : stmt.block(0).statementList().statement()) {
				if (stmtCount == 0) {
					template = addStatement(template, statemant, "id" + locationIds.get(0));
				} else {
					template = addStatement(template, statemant, "id" + (template.getLocations().size() - 1));
				}
				stmtCount++;
			}
			lastLocationIdsInBlocks.add(Integer.parseInt(template.getHighestLocationId().replaceAll("id", "")));
			stmtCount = 0;
			// Add first else
			locationIds.add(template.getLocations().size()); // else location
			elseLocations.add(template.getLocations().size());
			elseCount++;
			template = AutomataNetworkBuilder.addConditionBlock(template, "!(" + stmt.expression().getText() + ")", locationId, Strings.ELSE, Strings.RIGHT_SHIFT, elseCount);
			// Add else ifs
			IfStmtContext tempIf = stmt.ifStmt();
			BlockContext elseBlock = tempIf.block(1);
			int elseIfCount = 0;
			while(tempIf != null) {
				// Add else
				locationIds.add(template.getLocations().size()); // else location
				elseLocations.add(template.getLocations().size());
				elseCount++;
				if(elseIfCount == 0) {
					template = AutomataNetworkBuilder.addConditionBlock(template, "!(" + tempIf.expression().getText() + ")", "id" + (locationIds.get(locationIds.size() - 2)), Strings.ELSE, Strings.RIGHT_SHIFT, elseCount);
				}else {
					template = AutomataNetworkBuilder.addConditionBlock(template, "!(" + tempIf.expression().getText() + ")", "id" + (locationIds.get(locationIds.size() - 3)), Strings.ELSE, Strings.RIGHT_SHIFT, elseCount);
				}
				// Add if
				locationIds.add(template.getLocations().size()); // if location
				ifCount++;
				if(elseIfCount == 0) {
					template = AutomataNetworkBuilder.addConditionBlock(template, tempIf.expression().getText(), "id" + (locationIds.get(locationIds.size() - 3)), Strings.IF,Strings.LEFT_SHIFT, ifCount);
				}else {
					template = AutomataNetworkBuilder.addConditionBlock(template, tempIf.expression().getText(), "id" + (locationIds.get(locationIds.size() - 4)), Strings.IF,Strings.LEFT_SHIFT, ifCount);
				}
				// Build If Block
				for (StatementContext statemant : tempIf.block(0).statementList().statement()) {
					template = addStatement(template, statemant, "id" + (template.getLocations().size() - 1));
					stmtCount++;
				}
				lastLocationIdsInBlocks.add(Integer.parseInt(template.getHighestLocationId().replaceAll("id", "")));
				stmtCount = 0;
				
				if(tempIf.ifStmt() != null) {
					elseBlock = tempIf.ifStmt().block(1);
				}
				elseIfCount++;
				tempIf = tempIf.ifStmt();
			}
			// Build Else Block
			if (elseBlock != null) {
				for (StatementContext statemant : elseBlock.statementList().statement()) {
					if (stmtCount == 0) {
						template = addStatement(template, statemant,
								"id" + (elseLocations.get(elseLocations.size() - 1)));
					} else {
						template = addStatement(template, statemant, "id" + (template.getLocations().size() - 1));
					}
					stmtCount++;
				}
			}
			lastLocationIdsInBlocks.add(Integer.parseInt(template.getHighestLocationId().replaceAll("id", "")));
			// Merge Branches
			template = AutomataNetworkBuilder.addMergeBranches(template, lastLocationIdsInBlocks, template.getLocationById(locationId).getPosX());
		}
		
		return template;
	}
	
	/**
	 * Builds the For-Loop component from context
	 * @param template
	 * @param stmt
	 * @param locationId
	 * @return
	 */
	private Template addFor(Template template, ForStmtContext stmt, String locationId) {
		boolean isExpr = stmt.expression() != null;
		boolean isForClause = stmt.forClause() != null;
		
		String startLocation = "id" + (Integer.parseInt(locationId.replaceAll("id", "")) + 1);
		
		// Build head
		if (isExpr) {
			String condition = stmt.expression().getText();
			forCount++;
			template = AutomataNetworkBuilder.addForStart(template, condition, locationId, forCount);
		}else if(isForClause) {
			forCount++;
			String varDecl = stmt.forClause().simpleStmt(0).getText().replaceAll(":", "");
			asgmtCount++;
			template = AutomataNetworkBuilder.addAssignment(template, varDecl, locationId, asgmtCount);
			if(!template.getDeclaration().contains("int " + varDecl + ";")) {
				template.setDeclaration(template.getDeclaration() + "\r\n" + "int " + varDecl + ";");
			}
			String condition = stmt.forClause().expression().getText();
			template = AutomataNetworkBuilder.addForStart(template, condition, template.getHighestLocationId(), forCount);
			startLocation = "id" + (Integer.parseInt(locationId.replaceAll("id", "")) + 2);
		}
		
		// Build block
		for (StatementContext stmtContext : stmt.block().statementList().statement()) {
			template = addStatement(template, stmtContext, "id" + (template.getLocations().size() - 1));
		}
		
		// Build return
		if (isExpr) {
			String condition = stmt.expression().getText();
			template = AutomataNetworkBuilder.addForEnd(template, template.getHighestLocationId(), startLocation, condition);
		}else if(isForClause) {
			String condition = stmt.forClause().expression().getText();
			asgmtCount++;
			template = AutomataNetworkBuilder.addAssignment(template, stmt.forClause().simpleStmt(1).getText(), template.getHighestLocationId(), asgmtCount);
			template = AutomataNetworkBuilder.addForEnd(template, template.getHighestLocationId(), startLocation, condition);
		}

		return template;
	}
	
	/**
	 * Builds the Assignment component from context
	 * @param template
	 * @param stmt
	 * @param locationId
	 * @return
	 */
	private Template addAssignment(Template template, AssignmentContext stmt, String locationId) {
		if(stmt.getText().contains("RandomInt")) { // with RandomInt() call
			int lower = Integer.parseInt(stmt.expressionList(1).expression(0).primaryExpr().arguments().expressionList().expression(0).getText());
			int upper = Integer.parseInt(stmt.expressionList(1).expression(0).primaryExpr().arguments().expressionList().expression(1).getText());
			String var = stmt.expressionList(0).getText();
			randomIntCount++;
			template = AutomataNetworkBuilder.addSelect(template, locationId, var, lower, upper, randomIntCount);
			return template;
		} 
		
		if(stmt.getText().contains("RandomBool")) { // with RandomBool() call
			randomBoolCount++;
			template = AutomataNetworkBuilder.addRandomBool(template, locationId, stmt.expressionList(0).getText(), randomBoolCount);
			return template;
		}
		
		// normal assignment
		asgmtCount++;
		String assignment = stmt.getText().contains(".") ? stmt.getText().substring(0, stmt.getText().indexOf(".")) : stmt.getText();
		template = AutomataNetworkBuilder.addAssignment(template, assignment, locationId, asgmtCount);
		return template;
	}
	
	/**
	 * Builds the Inc or Dec Statement component from context
	 * @param template
	 * @param stmt
	 * @param locationId
	 * @return
	 */
	private Template addIncDecStmt(Template template, IncDecStmtContext stmt, String locationId) {
		asgmtCount++;
		template = AutomataNetworkBuilder.addAssignment(template, stmt.getText(), locationId, asgmtCount);
		return template;
	}
	
	/**
	 * Builds the Broadcast component from context
	 * @param template
	 * @param stmt
	 * @param locationId
	 * @return
	 */
	private Template addBroadcast(Template template, ExpressionContext stmt, String locationId) {
		asgmtCount++;
		forCount++;
		ifCount++;
		elseCount++;
		template = AutomataNetworkBuilder.addAssignment(template, "broadcastCounter = 0", locationId, asgmtCount);
		template = AutomataNetworkBuilder.addForStart(template, "broadcastCounter < NODES", "id" + (template.getLocations().size() - 1), forCount);
		template = AutomataNetworkBuilder.addConditionBlock(template, "pid != broadcastCounter", "id" + (template.getLocations().size() - 1), Strings.IF, Strings.LEFT_SHIFT, ifCount);
		template = AutomataNetworkBuilder.addConditionBlock(template, "pid == broadcastCounter", "id" + (template.getLocations().size() - 2), Strings.ELSE, Strings.RIGHT_SHIFT, elseCount);
		
		String chan = stmt.primaryExpr().arguments().expressionList().expression(1).getText();
		String msg = stmt.primaryExpr().arguments().expressionList().expression(2).getText();
		if(channels.get(chan) == 0) { // sync
			sendCount++;
			template = AutomataNetworkBuilder.addSendActionSync(template, "broadcastCounter", msg, chan, "id" + (template.getLocations().size() - 2), sendCount);
		}else if(channels.get(chan) == 1) { // async
			sendCount++;
			template = AutomataNetworkBuilder.addSendActionAsync(template, "broadcastCounter", msg, chan, "id" + (template.getLocations().size() - 2), sendCount);
		}else if(channels.get(chan) == 2) { // async with buffer
			sendCount++;
			template = AutomataNetworkBuilder.addSendActionAsyncWithBuffer(template, "broadcastCounter", msg, chan, "id" + (template.getLocations().size() - 2), sendCount);
		}
		
		List<Integer> lastLocations = new LinkedList<>();
		lastLocations.add(template.getLocations().size() - 1);
		lastLocations.add(template.getLocations().size() - 2);
		template = AutomataNetworkBuilder.addMergeBranches(template, lastLocations, template.getLocationById("id" + (template.getLocations().size() - 4)).getPosX());
		
		asgmtCount++;
		template = AutomataNetworkBuilder.addAssignment(template, "broadcastCounter++", "id" + (template.getLocations().size() - 1), asgmtCount);
		template = AutomataNetworkBuilder.addForEnd(template, "id" + (template.getLocations().size() - 1), "id" + (template.getLocations().size() - 7), "broadcastCounter < NODES");
		
		return template;
	}
	
	/**
	 * Builds the Broadcast Lossy component from context
	 * @param template
	 * @param stmt
	 * @param locationId
	 * @return
	 */
	private Template addBroadcastLossy(Template template, ExpressionContext stmt, String locationId) {
		// TODO
		return template;
	}
	
	/**
	 * Builds the Send Lossy component from context
	 * @param template
	 * @param stmt
	 * @param locationId
	 * @return
	 */
	private Template addSendLossy(Template template, ExpressionContext stmt, String locationId) {
		randomBoolCount++;
		template = AutomataNetworkBuilder.addRandomBool(template, locationId, "sendLossy", randomBoolCount);
		String locationIdRandomBool = template.getHighestLocationId();
		ifCount++;
		elseCount++;
		template = AutomataNetworkBuilder.addConditionBlock(template, "sendLossy", locationIdRandomBool, Strings.IF, Strings.LEFT_SHIFT, ifCount);
		String locationIdIf = template.getHighestLocationId();
		template = AutomataNetworkBuilder.addConditionBlock(template, "!sendLossy", locationIdRandomBool, Strings.ELSE, Strings.RIGHT_SHIFT, elseCount);
		String locationIdElse = template.getHighestLocationId();
		template = addSend(template, stmt, locationIdIf);
		List<Integer> lastLocationIds = new LinkedList<>();
		lastLocationIds.add(Integer.parseInt(locationIdElse.replaceAll("id", "")));
		lastLocationIds.add(Integer.parseInt(template.getHighestLocationId().replaceAll("id", "")));
		template = AutomataNetworkBuilder.addMergeBranches(template, lastLocationIds, template.getLocationById(locationId).getPosX());
		return template;
	}

	/**
	 * Adds a marker location to a template 
	 * @param template
	 * @param stmt
	 * @param locationId
	 * @return
	 */
	private Template addMarker(Template template, SimpleStmtContext stmt, String locationId) {
		if(doMarkerLocations) {
			markerCount++;
			template = AutomataNetworkBuilder.addMarker(template, locationId, markerCount);
		}
		return template;
	}
	
	//------------------------------Getter and Setter----------------------------
	
	public void setDoMarkerLocations(boolean doMarkerLocations) {
		this.doMarkerLocations = doMarkerLocations;
	}
	
	public boolean getDoMarkerLocations() {
		return this.doMarkerLocations;
	}
	
}

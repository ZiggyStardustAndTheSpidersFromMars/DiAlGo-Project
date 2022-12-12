package goerner.dialgo;

import java.util.List;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import automata.AutomataNetwork;
import automata.Label;
import automata.Location;
import automata.Nail;
import automata.Template;
import automata.Transition;
import goparser.GoParser.SelectStmtContext;
import res.Constants;
import res.StringTemplates;
import res.Strings;
/**
 * Build Handler for creating AutomataNetwork and logic components of
 * templates, declarations, etc. with string templates
 * @author Torben Friedrich Goerner
 *
 */
@SuppressWarnings("unused")
public class AutomataNetworkBuilder {
	
	//private static STGroup templates = new STGroupFile("src/main/java/res/templates.stg");
	
	/**
	 * Adds Consts for amount of nodes and edges to the global decl. 
	 * @param an
	 * @param nodes
	 * @param edges
	 * @return
	 */
	public static AutomataNetwork setAmountOfNodesAndEdges(AutomataNetwork an, int nodes, int edges) {
		//ST st = templates.getInstanceOf("NODES_EDGES");
		ST st = StringTemplates.NODES_EDGES;
		st.remove("nodes");
		st.remove("edges");
		st.add("nodes", nodes);
		st.add("edges", edges);
		an.setDeclaration(an.getDeclaration() + st.render());
		return an;
	}
	
	/**
	 * Adds the standard Dialgo Code to global decl.
	 * @param an
	 * @return
	 */
	public static AutomataNetwork addDialgoCode(AutomataNetwork an) {
		//ST st = templates.getInstanceOf("DIALGOCODE");
		ST st = StringTemplates.DIALGOCODE;
		an.setDeclaration(an.getDeclaration() + st.render());
		return an;
	}
	
	/**
	 * Adds a Template.
	 * @param an
	 * @param name
	 * @param parameter
	 * @param localVar
	 * @return
	 */
	public static AutomataNetwork addTemplate(AutomataNetwork an, String name, String parameter, String localVar) {
		Template ta = new Template();
		ta.setName(name);
		ta.setParametes(parameter);
		ta.setDeclaration(localVar);
		Location location = new Location();
		location.setId("id0");
		location.setInitial(true);
		location.setName("Start");
		location.setPosX(0);
		location.setPosY(0);
		location.setPosXName(10);
		location.setPosYName(0);
		ta.addLocation(location);
		an.addTemplate(ta);
		return an;
	}
	
	/**
	 * Adds the system decl.
	 * @param an
	 * @param names
	 * @param parameters
	 * @return
	 */
	public static AutomataNetwork setSystemDeclaration(AutomataNetwork an, List<String> names, List<String> parameters) {
		an.setSystemDeclaration("");
		for(int i = 0; i < names.size() ; i++) {
			//ST st = templates.getInstanceOf("SYSTEM_DECL_TEMPLATE");
			ST st = StringTemplates.SYSTEM_DECL_TEMPLATE;
			st.remove("name");
			st.remove("pid");
			st.remove("parameter");
			st.add("name", names.get(i));
			st.add("pid", i);
			st.add("parameter", parameters.get(i));
			an.setSystemDeclaration(an.getSystemDeclaration() + st.render() + "\r\n");
		}
		
		an.setSystemDeclaration(an.getSystemDeclaration() + "\r\n" + "system ");
		for(int i = 0; i < names.size() ; i++) {
			an.setSystemDeclaration(an.getSystemDeclaration() + names.get(i) + i + ",");
		}
		an.setSystemDeclaration(an.getSystemDeclaration().substring(0, an.getSystemDeclaration().length() - 1) + ";");
		
		return an;
	}
	
	/**
	 * Adds a sync channel to global decl.
	 * @param an
	 * @param chan
	 * @param type
	 * @return
	 */
	public static AutomataNetwork addSyncChan(AutomataNetwork an, String chan, String type) {
		//ST st = templates.getInstanceOf("CHAN_SYNC");
		ST st = StringTemplates.CHAN_SYNC;
		st.remove("chan");
		st.remove("type");
		st.add("chan", chan);
		st.add("type", type);
		an.setDeclaration(an.getDeclaration() + "\r\n" + st.render());
		return an;
	} 
	
	/**
	 * Adds a async channel to global decl.
	 * @param an
	 * @param chan
	 * @param type
	 * @return
	 */
	public static AutomataNetwork addAsyncChan(AutomataNetwork an, String chan, String type) {
		//ST st = templates.getInstanceOf("CHAN_ASYNC");
		ST st = StringTemplates.CHAN_ASYNC;
		st.remove("chan");
		st.remove("type");
		st.add("chan", chan);
		st.add("type", type);
		an.setDeclaration(an.getDeclaration() + "\r\n" + st.render());
		return an;
	} 
	
	/**
	 * Adds a async channel with buffer to global decl.
	 * @param an
	 * @param chan
	 * @param type
	 * @param bufferSize
	 * @return
	 */
	public static AutomataNetwork addAsyncBufferChan(AutomataNetwork an, String chan, String type, int bufferSize) {
		//ST st = templates.getInstanceOf("CHAN_ASYNC_WITH_BUFFER");
		ST st = StringTemplates.CHAN_ASYNC_WITH_BUFFER;
		st.remove("chan");
		st.remove("type");
		st.remove("size");
		st.add("chan", chan);
		st.add("type", type);
		st.add("size", bufferSize);
		an.setDeclaration(an.getDeclaration() + "\r\n" + st.render());
		return an;
	} 
	
	public static Template addSendActionSync(Template ta, String pidB, String msg, String chan, String locationId, int eventId) {
		//ST stUpdate = templates.getInstanceOf("SEND_SYNC_UPDATE");
		//ST stSync = templates.getInstanceOf("SEND_SYNC_SYNC");
		ST stUpdate = StringTemplates.SEND_SYNC_UPDATE;
		ST stSync = StringTemplates.SEND_SYNC_SYNC;
		
		stUpdate.remove("chan");
		stUpdate.remove("msg");
		stUpdate.remove("pidB");
		stSync.remove("chan");
		stSync.remove("pidB");
		
		stUpdate.add("chan", chan);
		stUpdate.add("msg", msg);
		stUpdate.add("pidB", pidB);
		
		stSync.add("chan", chan);
		stSync.add("pidB", pidB);

		Location send = new Location(); 
		send.setId("id" + ta.getLocations().size());
		send.setName("Send" + eventId);
		send.setPosX(ta.getLocationById(locationId).getPosX());
		send.setPosY(ta.getLocationById(locationId).getPosY() - 90);
		send.setPosXName(send.getPosX() + Constants.LOCATION_POSX_NAME_OFFSET);
		send.setPosYName(send.getPosY());
		ta.addLocation(send);
		
		Transition transition = new Transition();
		transition.setSource(locationId);
		transition.setTarget(send.getId());
		Label update = new Label();
		update.setKind(Strings.UPDATE);
		update.setContent(stUpdate.render());
		update.setPosX(send.getPosX() + Constants.LABEL_POSX_OFFSET);
		update.setPosY(send.getPosY() + 25);
		Label sync = new Label();
		sync.setKind(Strings.SYNC);
		sync.setContent(stSync.render());
		transition.addLabel(sync);
		transition.addLabel(update);
		sync.setPosX(send.getPosX() + Constants.LABEL_POSX_OFFSET);
		sync.setPosY(send.getPosY() + 50);
		ta.addTransition(transition);

		return ta;
	}
	
	public static Template addSendActionAsync(Template ta, String pidB, String msg, String chan, String locationId, int eventId) {
		//ST stUpdate = templates.getInstanceOf("SEND_ASYNC_UPDATE");
		//ST stGuard = templates.getInstanceOf("SEND_ASYNC_GUARD");
		ST stUpdate = StringTemplates.SEND_ASYNC_UPDATE;
		ST stGuard = StringTemplates.SEND_ASYNC_GUARD;
		
		stUpdate.remove("chan");
		stUpdate.remove("msg");
		stUpdate.remove("pidB");
		stGuard.remove("chan");
		stGuard.remove("pidB");
		
		stUpdate.add("chan", chan);
		stUpdate.add("msg", msg);
		stUpdate.add("pidB", pidB);
		
		stGuard.add("chan", chan);
		stGuard.add("pidB", pidB);
		
		Location send = new Location(); 
		send.setId("id" + ta.getLocations().size());
		send.setName("Send" + eventId);
		send.setPosX(ta.getLocationById(locationId).getPosX());
		send.setPosY(ta.getLocationById(locationId).getPosY() - 120);
		send.setPosXName(send.getPosX() + Constants.LOCATION_POSX_NAME_OFFSET);
		send.setPosYName(send.getPosY());
		ta.addLocation(send);
		
		Transition transition = new Transition();
		transition.setSource(locationId);
		transition.setTarget(send.getId());
		Label update = new Label();
		update.setKind(Strings.UPDATE);
		update.setContent(stUpdate.render());
		update.setPosX(send.getPosX() + Constants.LABEL_POSX_OFFSET);
		update.setPosY(send.getPosY() + 50);
		Label guard = new Label();
		guard.setKind(Strings.GUARD);
		guard.setContent(stGuard.render());
		transition.addLabel(guard);
		transition.addLabel(update);
		guard.setPosX(send.getPosX() + Constants.LABEL_POSX_OFFSET);
		guard.setPosY(send.getPosY() + 25);
		ta.addTransition(transition);
		
		return ta;
	}
	
	public static Template addSendActionAsyncWithBuffer(Template ta, String pidB, String msg, String chan, String locationId, int eventId) {
		//ST stUpdate = templates.getInstanceOf("SEND_ASYNC_BUFFER_UPDATE");
		//ST stGuard = templates.getInstanceOf("SEND_ASYNC_BUFFER_GUARD");
		ST stUpdate = StringTemplates.SEND_ASYNC_BUFFER_UPDATE;
		ST stGuard = StringTemplates.SEND_ASYNC_BUFFER_GUARD;
		
		stUpdate.remove("chan");
		stUpdate.remove("msg");
		stUpdate.remove("pidB");
		stGuard.remove("chan");
		stGuard.remove("pidB");
		
		stUpdate.add("chan", chan);
		stUpdate.add("msg", msg);
		stUpdate.add("pidB", pidB);
		
		stGuard.add("chan", chan);
		stGuard.add("pidB", pidB);
		
		Location send = new Location(); 
		send.setId("id" + ta.getLocations().size());
		send.setName("Send" + eventId);
		send.setPosX(ta.getLocationById(locationId).getPosX());
		send.setPosY(ta.getLocationById(locationId).getPosY() - 140);
		send.setPosXName(send.getPosX() + Constants.LOCATION_POSX_NAME_OFFSET);
		send.setPosYName(send.getPosY());
		ta.addLocation(send);
		
		Transition transition = new Transition();
		transition.setSource(locationId);
		transition.setTarget(send.getId());
		Label update = new Label();
		update.setKind(Strings.UPDATE);
		update.setContent(stUpdate.render());
		update.setPosX(send.getPosX() + Constants.LABEL_POSX_OFFSET);
		update.setPosY(send.getPosY() + 50);
		Label guard = new Label();
		guard.setKind(Strings.GUARD);
		guard.setContent(stGuard.render());
		transition.addLabel(guard);
		transition.addLabel(update);
		guard.setPosX(send.getPosX() + Constants.LABEL_POSX_OFFSET);
		guard.setPosY(send.getPosY() + 25);
		ta.addTransition(transition);
		
		return ta;
	}
	
	public static Template addSendActionNonFifo(Template ta, String pidB, String msg, String chan, String locationId, int eventId) {
		//ST stUpdate = templates.getInstanceOf("SEND_NON_FIFO_UPDATE");
		//ST stGuard = templates.getInstanceOf("SEND_ASYNC_BUFFER_GUARD");
		ST stUpdate = StringTemplates.SEND_NON_FIFO_UPDATE;
		ST stGuard = StringTemplates.SEND_ASYNC_BUFFER_GUARD;
		
		stUpdate.remove("chan");
		stUpdate.remove("msg");
		stUpdate.remove("pidB");
		stGuard.remove("chan");
		stGuard.remove("pidB");
		
		stUpdate.add("chan", chan);
		stUpdate.add("msg", msg);
		stUpdate.add("pidB", pidB);
		
		stGuard.add("chan", chan);
		stGuard.add("pidB", pidB);
		
		Location send = new Location(); 
		send.setId("id" + ta.getLocations().size());
		send.setName("Send" + eventId);
		send.setPosX(ta.getLocationById(locationId).getPosX() + 10);
		send.setPosY(ta.getLocationById(locationId).getPosY() - 160);
		send.setPosXName(send.getPosX() + Constants.LOCATION_POSX_NAME_OFFSET);
		send.setPosYName(send.getPosY());
		ta.addLocation(send);
		
		Transition transition = new Transition();
		transition.setSource(locationId);
		transition.setTarget(send.getId());
		Label update = new Label();
		update.setKind(Strings.UPDATE);
		update.setContent(stUpdate.render());
		update.setPosX(send.getPosX() + Constants.LABEL_POSX_OFFSET);
		update.setPosY(send.getPosY() + 50);
		Label guard = new Label();
		guard.setKind(Strings.GUARD);
		guard.setContent(stGuard.render());
		transition.addLabel(guard);
		transition.addLabel(update);
		guard.setPosX(send.getPosX() + Constants.LABEL_POSX_OFFSET);
		guard.setPosY(send.getPosY() + 25);
		ta.addTransition(transition);
		
		return ta;
	}
	
	public static Template addReceiveActionSync(Template ta, String pidB, String var, String type, String chan, String locationId, int offset, int eventId) {
		//ST stUpdate = templates.getInstanceOf("RECEIVE_SYNC_UPDATE");
		//ST stSync = templates.getInstanceOf("RECEIVE_SYNC_SYNC");
		ST stUpdate = StringTemplates.RECEIVE_SYNC_UPDATE;
		ST stSync = StringTemplates.RECEIVE_SYNC_SYNC;
		
		if(!ta.getDeclaration().contains( type + " " + var + ";")) {
			ta.setDeclaration(ta.getDeclaration() + "\r\n" + type + " " + var + ";");
		}
		
		stUpdate.remove("chan");
		stUpdate.remove("var");
		stUpdate.remove("pidB");
		stSync.remove("chan");
		stSync.remove("pidB");
		
		stUpdate.add("chan", chan);
		stUpdate.add("var", var);
		stUpdate.add("pidB", pidB);
		
		stSync.add("chan", chan);
		stSync.add("pidB", pidB);
		
		Location receive = new Location(); 
		receive.setId("id" + ta.getLocations().size());
		receive.setName("Receive" + eventId);
		receive.setPosX(ta.getLocationById(locationId).getPosX() + offset);
		receive.setPosY(ta.getLocationById(locationId).getPosY() - 90);
		receive.setPosXName(receive.getPosX() + Constants.LOCATION_POSX_NAME_OFFSET);
		receive.setPosYName(receive.getPosY());
		ta.addLocation(receive);
		
		Transition transition = new Transition();
		transition.setSource(locationId);
		transition.setTarget(receive.getId());
		Label update = new Label();
		update.setKind(Strings.UPDATE);
		update.setContent(stUpdate.render());
		update.setPosX(receive.getPosX() + Constants.LABEL_POSX_OFFSET);
		update.setPosY(receive.getPosY() + 25);
		Label sync = new Label();
		sync.setKind(Strings.SYNC);
		sync.setContent(stSync.render());
		transition.addLabel(sync);
		transition.addLabel(update);
		sync.setPosX(receive.getPosX() + Constants.LABEL_POSX_OFFSET);
		sync.setPosY(receive.getPosY() + 50);
		ta.addTransition(transition);
		
		return ta;
	}
	
	public static Template addReceiveActionAsync(Template ta, String pidB, String var, String type, String chan, String locationId, int offset, int eventId) {
		//ST stUpdate = templates.getInstanceOf("RECEIVE_ASYNC_UPDATE");
		//ST stGuard = templates.getInstanceOf("RECEIVE_ASYNC_GUARD");
		ST stUpdate = StringTemplates.RECEIVE_ASYNC_UPDATE;
		ST stGuard = StringTemplates.RECEIVE_ASYNC_GUARD;
		
		if(!ta.getDeclaration().contains( type + " " + var + ";")) {
			ta.setDeclaration(ta.getDeclaration() + "\r\n" + type + " " + var + ";");
		}
		
		stUpdate.remove("chan");
		stUpdate.remove("var");
		stUpdate.remove("pidB");
		stGuard.remove("chan");
		stGuard.remove("pidB");
		
		stUpdate.add("chan", chan);
		stUpdate.add("var", var);
		stUpdate.add("pidB", pidB);
		
		stGuard.add("chan", chan);
		stGuard.add("pidB", pidB);
		
		Location receive = new Location(); 
		receive.setId("id" + ta.getLocations().size());
		receive.setName("Receive" + eventId);
		receive.setPosX(ta.getLocationById(locationId).getPosX() + offset);
		receive.setPosY(ta.getLocationById(locationId).getPosY() - 120);
		receive.setPosXName(receive.getPosX() + Constants.LOCATION_POSX_NAME_OFFSET);
		receive.setPosYName(receive.getPosY());
		ta.addLocation(receive);
		
		Transition transition = new Transition();
		transition.setSource(locationId);
		transition.setTarget(receive.getId());
		Label update = new Label();
		update.setKind(Strings.UPDATE);
		update.setContent(stUpdate.render());
		update.setPosX(receive.getPosX() + Constants.LABEL_POSX_OFFSET);
		update.setPosY(receive.getPosY() + 50);
		Label guard = new Label();
		guard.setKind(Strings.GUARD);
		guard.setContent(stGuard.render());
		transition.addLabel(guard);
		transition.addLabel(update);
		guard.setPosX(receive.getPosX() + Constants.LABEL_POSX_OFFSET);
		guard.setPosY(receive.getPosY() + 25);
		ta.addTransition(transition);
		
		return ta;
	}
	
	public static Template addReceiveActionAsyncWithBuffer(Template ta, String pidB, String var, String type, String chan, String locationId, int offset, int eventId) {
		//ST stUpdate = templates.getInstanceOf("RECEIVE_ASYNC_BUFFER_UPDATE");
		//ST stGuard = templates.getInstanceOf("RECEIVE_ASYNC_BUFFER_GUARD");
		ST stUpdate = StringTemplates.RECEIVE_ASYNC_BUFFER_UPDATE;
		ST stGuard = StringTemplates.RECEIVE_ASYNC_BUFFER_GUARD;
		
		if(!ta.getDeclaration().contains( type + " " + var + ";")) {
			ta.setDeclaration(ta.getDeclaration() + "\r\n" + type + " " + var + ";");
		}
		
		stUpdate.remove("chan");
		stUpdate.remove("var");
		stUpdate.remove("pidB");
		stGuard.remove("chan");
		stGuard.remove("pidB");
		
		stUpdate.add("chan", chan);
		stUpdate.add("var", var);
		stUpdate.add("pidB", pidB);
		
		stGuard.add("chan", chan);
		stGuard.add("pidB", pidB);
		
		Location receive = new Location(); 
		receive.setId("id" + ta.getLocations().size());
		receive.setName("Receive" + eventId);
		receive.setPosX(ta.getLocationById(locationId).getPosX() + offset);
		receive.setPosY(ta.getLocationById(locationId).getPosY() - 140);
		receive.setPosXName(receive.getPosX() + Constants.LOCATION_POSX_NAME_OFFSET);
		receive.setPosYName(receive.getPosY());
		ta.addLocation(receive);
		
		Transition transition = new Transition();
		transition.setSource(locationId);
		transition.setTarget(receive.getId());
		Label update = new Label();
		update.setKind(Strings.UPDATE);
		update.setContent(stUpdate.render());
		update.setPosX(receive.getPosX() + Constants.LABEL_POSX_OFFSET);
		update.setPosY(receive.getPosY() + 50);
		Label guard = new Label();
		guard.setKind(Strings.GUARD);
		guard.setContent(stGuard.render());
		transition.addLabel(guard);
		transition.addLabel(update);
		guard.setPosX(receive.getPosX() + Constants.LABEL_POSX_OFFSET);
		guard.setPosY(receive.getPosY() + 25);
		ta.addTransition(transition);
		
		return ta;
	}
	
	public static Template addReceiveActionNonFifo(Template ta, String pidB, String var, String type, String chan, String locationId, int offset, int eventId) {
		// TODO
		return ta;
	}
	
	public static AutomataNetwork addChannelSync(AutomataNetwork an, String chanName, String type) {
		//ST st = templates.getInstanceOf("CHAN_SYNC");
		ST st = StringTemplates.CHAN_SYNC;
		st.remove("chan");
		st.remove("type");
		st.add("chan", chanName);
		st.add("type", type);
		an.setDeclaration(an.getDeclaration() + "\n" + st.render());
		return an;
	}
	
	public static AutomataNetwork addChannelAsync(AutomataNetwork an, String chanName, String type) {
		//ST st = templates.getInstanceOf("CHAN_ASYNC");
		ST st = StringTemplates.CHAN_ASYNC;
		st.remove("chan");
		st.remove("type");
		st.add("chan", chanName);
		st.add("type", type);
		an.setDeclaration(an.getDeclaration() + "\n" + st.render());
		return an;
	}
	
	public static AutomataNetwork addChannelAsyncWithBuffer(AutomataNetwork an, String chanName, String type, int bufferSize) {
		//ST st = templates.getInstanceOf("CHAN_ASYNC_WITH_BUFFER");
		ST st = StringTemplates.CHAN_ASYNC_WITH_BUFFER;
		st.remove("chan");
		st.remove("type");
		st.remove("size");
		st.add("chan", chanName);
		st.add("type", type);
		st.add("size", bufferSize);
		an.setDeclaration(an.getDeclaration() + "\n" + st.render());
		return an;
	}
	
	public static Template addRandomBool(Template ta, String locationId, String var, int eventId) {
		Location rbStart = new Location();
		rbStart.setId("id" + ta.getLocations().size());
		rbStart.setName("RandomBool" + eventId);
		rbStart.setPosX(ta.getLocationById(locationId).getPosX());
		rbStart.setPosY(ta.getLocationById(locationId).getPosY() - 50);
		rbStart.setPosXName(rbStart.getPosX() + Constants.LOCATION_POSX_NAME_OFFSET);
		rbStart.setPosYName(rbStart.getPosY());
		
		Location rbTrue = new Location();
		rbTrue.setId("id" + (ta.getLocations().size() + 1));
		rbTrue.setPosX(rbStart.getPosX() - 50);
		rbTrue.setPosY(rbStart.getPosY() - 50);
		rbTrue.setPosXName(rbTrue.getPosX() + Constants.LOCATION_POSX_NAME_OFFSET);
		rbTrue.setPosYName(rbTrue.getPosY());
		
		Location rbFalse = new Location();
		rbFalse.setId("id" + (ta.getLocations().size() + 2));
		rbFalse.setPosX(rbStart.getPosX() + 50);
		rbFalse.setPosY(rbStart.getPosY() - 50);
		rbFalse.setPosXName(rbFalse.getPosX() + Constants.LOCATION_POSX_NAME_OFFSET);
		rbFalse.setPosYName(rbFalse.getPosY());
		
		Location rbEnd = new Location();
		rbEnd.setId("id" + (ta.getLocations().size() + 3));
		rbEnd.setPosX(rbStart.getPosX());
		rbEnd.setPosY(rbStart.getPosY() - 100);
		rbEnd.setPosXName(rbEnd.getPosX() + Constants.LOCATION_POSX_NAME_OFFSET);
		rbEnd.setPosYName(rbEnd.getPosY());
		
		Transition t1 = new Transition();
		t1.setSource(locationId);
		t1.setTarget(rbStart.getId());
		
		Transition tt = new Transition();
		tt.setSource(rbStart.getId());
		tt.setTarget(rbTrue.getId());
		Label lt = new Label();
		lt.setKind(Strings.UPDATE);
		lt.setContent(var + " = true");
		lt.setPosX(rbTrue.getPosX() - 15);
		lt.setPosY(rbTrue.getPosY() + 25);
		tt.addLabel(lt);
		
		Transition tf = new Transition();
		tf.setSource(rbStart.getId());
		tf.setTarget(rbFalse.getId());
		Label lf = new Label();
		lf.setKind(Strings.UPDATE);
		lf.setContent(var + " = false");
		lf.setPosX(rbFalse.getPosX() - 15);
		lf.setPosY(rbFalse.getPosY() + 25);
		tf.addLabel(lf);
		
		Transition ttend = new Transition();
		ttend.setSource(rbTrue.getId());
		ttend.setTarget(rbEnd.getId());
		
		Transition tfend = new Transition();
		tfend.setSource(rbFalse.getId());
		tfend.setTarget(rbEnd.getId());
		
		ta.addLocation(rbStart);
		ta.addLocation(rbTrue);
		ta.addLocation(rbFalse);
		ta.addTransition(t1);
		ta.addTransition(tt);
		ta.addTransition(tf);
		ta.addLocation(rbEnd);
		ta.addTransition(ttend);
		ta.addTransition(tfend);
		
		return ta;
	}
	
	/**
	 * Adds a RandomInt() Event (UPPAAL Select).
	 * @param ta
	 * @param locationId
	 * @param var
	 * @param min
	 * @param max
	 * @param eventId
	 * @return
	 */
	public static Template addSelect(Template ta, String locationId, String var, int min, int max, int eventId) {
		Location l = new Location();
		l.setId("id" + ta.getLocations().size());
		l.setName("RandomInt" + eventId);
		l.setPosX(ta.getLocationById(locationId).getPosX());
		l.setPosY(ta.getLocationById(locationId).getPosY() - 80);
		l.setPosXName(l.getPosX() + Constants.LOCATION_POSX_NAME_OFFSET);
		l.setPosYName(l.getPosY());
		
		Transition t = new Transition();
		t.setSource(locationId);
		t.setTarget(l.getId());
		Label select = new Label();
		select.setKind(Strings.SELECT);
		select.setContent("i : int[" + min + "," + max + "]");
		select.setPosX(l.getPosX() + Constants.LABEL_POSX_OFFSET);
		select.setPosY(l.getPosY() + 20);
		Label update = new Label();
		update.setKind(Strings.UPDATE);
		update.setContent(var + " = i");
		update.setPosX(l.getPosX() + Constants.LABEL_POSX_OFFSET);
		update.setPosY(l.getPosY() + 40);
		t.addLabel(update);
		t.addLabel(select);

		ta.addLocation(l);
		ta.addTransition(t);
		
		return ta;
	}
	
	public static AutomataNetwork addGlobalDeclaration(AutomataNetwork an, String decl) {
		an.setDeclaration(an.getDeclaration() + "\r\n" + decl);
		return an;
	}
	
	public static Template addAssignment(Template ta, String assignment, String locationId, int eventId) {
		Location l = new Location();
		l.setId("id" + ta.getLocations().size());
		l.setName("Assignment" + eventId);
		l.setPosX(ta.getLocationById(locationId).getPosX());
		l.setPosY(ta.getLocationById(locationId).getPosY() - 55);
		l.setPosXName(l.getPosX() + Constants.LOCATION_POSX_NAME_OFFSET);
		l.setPosYName(l.getPosY());
		
		Transition t = new Transition();
		t.setSource(locationId);
		t.setTarget(l.getId());
		Label a = new Label();
		a.setKind(Strings.UPDATE);
		a.setContent(assignment);
		a.setPosX(l.getPosX() + Constants.LABEL_POSX_OFFSET);
		a.setPosY(l.getPosY() + 20);
		
		t.addLabel(a);
		ta.addLocation(l);
		ta.addTransition(t);
		
		return ta;
	}
	
	public static Template addConditionBlock(Template ta, String condition, String locationId, String type, String shift, int eventId) {
		Location location = new Location();
		location.setId("id" + ta.getLocations().size());
		location.setName(type + eventId);
		location.setPosX(ta.getLocationById(locationId).getPosX());
		location.setPosY(ta.getLocationById(locationId).getPosY() - 50);
		location.setPosXName(location.getPosX() + Constants.LOCATION_POSX_NAME_OFFSET);
		location.setPosYName(location.getPosY());
		
		switch (shift) {
		case Strings.LEFT_SHIFT:
			location.setPosX(location.getPosX() - Constants.CONDITION_SHIFT);
			location.setPosXName(location.getPosXName() - Constants.CONDITION_SHIFT);
			break;
		case Strings.RIGHT_SHIFT:
			location.setPosX(location.getPosX() + Constants.CONDITION_SHIFT);
			location.setPosXName(location.getPosXName() + Constants.CONDITION_SHIFT);
			break;
		case Strings.NO_SHIFT:
			break;
		default:
			break;
		}
		
		Transition t = new Transition();
		t.setSource(locationId);
		t.setTarget(location.getId());
		Label con = new Label();
		con.setKind(Strings.GUARD);
		con.setContent(condition);
		con.setPosX(location.getPosX() - 25);
		con.setPosY(location.getPosY() + 20);
		
		switch (shift) {
		case Strings.LEFT_SHIFT:
			con.setPosX(location.getPosX() + (int)(Constants.CONDITION_SHIFT / 3));
			break;
		case Strings.RIGHT_SHIFT:
			con.setPosX(location.getPosX() - (int)(Constants.CONDITION_SHIFT / 3));
			break;
		case Strings.NO_SHIFT:
			con.setPosX(location.getPosX() + Constants.LABEL_POSX_OFFSET);
			break;
		default:
			break;
		}
		
		t.addLabel(con);
		ta.addLocation(location);
		ta.addTransition(t);

		return ta;
	}
	
	/**
	 * Merges a list of branches. 
	 * @param ta
	 * @param locationIds
	 * @param xPos
	 * @return
	 */
	public static Template addMergeBranches(Template ta, List<Integer> locationIds, int xPos) {
		Location location = new Location();
		location.setId("id" + ta.getLocations().size());
		location.setPosX(xPos);
		int posY = 0;
		for(Location l : ta.getLocations()) {
			if(l.getPosY() < posY) posY = l.getPosY();
		}
		location.setPosY(posY - 50);
		location.setPosXName(location.getPosX() + Constants.LOCATION_POSX_NAME_OFFSET);
		location.setPosYName(location.getPosY());
		ta.addLocation(location);
		
		for(int id : locationIds) {
			Transition t = new Transition();
			t.setSource("id" + id);
			t.setTarget(location.getId());
			ta.addTransition(t);
		}

		return ta;
	}
	
	/**
	 * Adds the Head of a for statement.
	 * @param ta
	 * @param condition
	 * @param locationId
	 * @param eventId
	 * @return
	 */
	public static Template addForStart(Template ta, String condition, String locationId, int eventId) {
		Location start = new Location();
		start.setId("id" + ta.getLocations().size());
		start.setName("For" + eventId);
		start.setPosX(ta.getLocationById(locationId).getPosX());
		start.setPosY(ta.getLocationById(locationId).getPosY() - 50);
		start.setPosXName(start.getPosX() + Constants.LOCATION_POSX_NAME_OFFSET);
		start.setPosYName(start.getPosY());
		
		Transition t1 = new Transition();
		t1.setSource(locationId);
		t1.setTarget(start.getId());
		
		ta.addLocation(start);
		ta.addTransition(t1);
		
		// Add Condition for the start of the for block
		ta = addConditionBlock(ta, condition, start.getId(), Strings.NONE, Strings.NO_SHIFT, eventId);
		
		return ta;
	}
	
	/**
	 * Adds the End of a for statement.
	 * @param ta
	 * @param locationIdSource
	 * @param locationIdTarget
	 * @param condition
	 * @return
	 */
	public static Template addForEnd(Template ta, String locationIdSource, String locationIdTarget, String condition) {
		// Return to start
		Transition t = new Transition();
		t.setSource(locationIdSource);
		t.setTarget(locationIdTarget);
		ta.addTransition(t);
		
		Nail n1 = new Nail();
		n1.setPosX(ta.getLocationById(locationIdTarget).getPosX() + 200);
		n1.setPosY(ta.getLocationById(locationIdSource).getPosY());
		Nail n2 = new Nail();
		n2.setPosX(ta.getLocationById(locationIdTarget).getPosX() + 200);
		n2.setPosY(ta.getLocationById(locationIdTarget).getPosY());
		
		t.addNail(n1);
		t.addNail(n2);
		
		// Add End
		Location end = new Location();
		end.setId("id" + ta.getLocations().size());
		end.setName(end.getId());
		end.setPosX(ta.getLocationById(locationIdTarget).getPosX() - 250);
		end.setPosY(ta.getLocationById(locationIdTarget).getPosY());
		end.setPosXName(end.getPosX() + Constants.LOCATION_POSX_NAME_OFFSET);
		end.setPosYName(end.getPosY());
		
		Transition t1 = new Transition();
		t1.setSource(locationIdTarget);
		t1.setTarget(end.getId());
		Label l = new Label();
		l.setKind(Strings.GUARD);
		l.setContent("!(" + condition + ")");
		l.setPosX(ta.getLocationById(locationIdTarget).getPosX() - 100);
		l.setPosY(ta.getLocationById(locationIdTarget).getPosY() - 15);
		t1.addLabel(l);
		
		ta.addLocation(end);
		ta.addTransition(t1);
		
		return ta;
	}

	/**
	 * Adds a GO Select Default Block. For Sync communication.
	 * @param ta
	 * @param stmt
	 * @param locationId
	 * @param offset
	 * @param eventId
	 * @return
	 */
	public static Template addSelectDefaultBlock(Template ta, SelectStmtContext stmt, String locationId, int offset, int eventId) {
		Location location = new Location();
		location.setId("id" + ta.getLocations().size());
		location.setName("Default" + eventId);
		location.setPosX(ta.getLocationById(locationId).getPosX() + offset);
		location.setPosY(ta.getLocationById(locationId).getPosY() - 100);
		location.setPosXName(location.getPosX() + Constants.LOCATION_POSX_NAME_OFFSET);
		location.setPosYName(location.getPosY());
		
		Transition t = new Transition();
		t.setSource(locationId);
		t.setTarget(location.getId());
		
		ta.addLocation(location);
		ta.addTransition(t);
		
		return ta;
	}
	
	/**
	 * Adds a GO Select Default Block (with a condition). For Async communication.
	 * @param ta
	 * @param stmt
	 * @param locationId
	 * @param offset
	 * @param condition
	 * @param eventId
	 * @return
	 */
	public static Template addSelectDefaultBlockWithCondition(Template ta, SelectStmtContext stmt, String locationId, int offset, String condition, int eventId) {
		Location location = new Location();
		location.setId("id" + ta.getLocations().size());
		location.setName("Default" + eventId);
		location.setPosX(ta.getLocationById(locationId).getPosX() + offset);
		location.setPosY(ta.getLocationById(locationId).getPosY() - 100);
		location.setPosXName(location.getPosX() + Constants.LOCATION_POSX_NAME_OFFSET);
		location.setPosYName(location.getPosY());
		
		Transition t = new Transition();
		t.setSource(locationId);
		t.setTarget(location.getId());
		
		Label con = new Label();
		con.setKind(Strings.GUARD);
		con.setContent(condition);
		con.setPosX(location.getPosX() - 25);
		con.setPosY(location.getPosY() + 20);
		t.addLabel(con);
		
		ta.addLocation(location);
		ta.addTransition(t);
		
		return ta;
	}
	
	public static Template addMarker(Template ta, String locationId, int eventId) {
		Location location = new Location();
		location.setId("id" + ta.getLocations().size());
		location.setName("Marker" + eventId);
		location.setPosX(ta.getLocationById(locationId).getPosX());
		location.setPosY(ta.getLocationById(locationId).getPosY() - 50);
		location.setPosXName(location.getPosX() + Constants.LOCATION_POSX_NAME_OFFSET);
		location.setPosYName(location.getPosY());
		
		Transition t = new Transition();
		t.setSource(locationId);
		t.setTarget(location.getId());
		
		ta.addLocation(location);
		ta.addTransition(t);
		
		return ta;
	}

}

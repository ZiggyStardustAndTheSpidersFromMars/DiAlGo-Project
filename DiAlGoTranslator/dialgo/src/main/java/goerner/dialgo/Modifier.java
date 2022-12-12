package goerner.dialgo;

import automata.AutomataNetwork;
import automata.Label;
import automata.Location;
import automata.Nail;
import automata.Template;
import automata.Transition;
import res.Commands;
import res.Messages;
import res.Strings;

/**
 * Modifier for UPPAAL Models (AutomataNetworks).
 * To add Templates or do annotation.
 * @author Torben Friedrich Goerner 
 *
 */
public class Modifier {
	
	/**
	 * Modifies an AutomataNetwork from args (Usage: -modify <path> <arg1,arg2,..,argN>)
	 * @param args
	 */
	public static void modify(String args[]) {
		if(args.length < 3) {
			System.err.println(Messages.MISSING_ARGUMENTS);
			System.exit(0);
		}
		
		try {
			AutomataNetwork an = AutomataNetwork.fromXML(args[1]);
			for (String modification : args) {
				switch (modification) {
					case Commands.ARG_ADD_CRASHER:
						an = addCrasher(an);
						break;
					default :
						break;
				}
			}
			App.writeFile(an.toString(), args[1].replaceFirst("[.][^.]+$", "") + "_modified", "xml");
		}catch(Exception e) {
			System.err.println(Messages.UPS);
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds a Crasher Template and Annotation for simulating crashing nodes.
	 * @param an AutomataNetwork
	 * @return AutomataNetwork
	 */
	public static AutomataNetwork addCrasher(AutomataNetwork an) {
		String globalDecl = an.getDeclaration().substring(0, an.getDeclaration().indexOf("//")) 
				+ "bool nodeCrash[NODES];\n\n" 
				+ an.getDeclaration().substring(an.getDeclaration().indexOf("//"));
		an.setDeclaration(globalDecl);
		an = addCrasherTemplate(an);
		an.setSystemDeclaration(an.getSystemDeclaration().substring(0, an.getSystemDeclaration().lastIndexOf(";")) + ", NodeCrasher;");
		
		for (Template template : an.getTemplates()) {
			if (!template.getName().equals("NodeCrasher")) {
				for (Transition transition : template.getTransitions()) {
					boolean hasGuard = false;
					for (Label label : transition.getlabels()) {
						if (label.getKind().equals(Strings.GUARD)) {
							hasGuard = true;
							label.setContent(label.getContent() + " && !nodeCrash[pid]");
						}
					}
					if (!hasGuard) {
						Label label = new Label();
						label.setKind(Strings.GUARD);
						label.setContent("!nodeCrash[pid]");
						label.setPosX(template.getLocationById(transition.getSource()).getPosX());
						label.setPosY(template.getLocationById(transition.getSource()).getPosY() - 15);
						transition.addLabel(label);
					}
				}
			}
		}
		
		return an;
	}
	
	private static AutomataNetwork addCrasherTemplate(AutomataNetwork an) {
		Template crasher = new Template();
		crasher.setName("NodeCrasher");
		crasher.setParametes("");
		
		Location idle = new Location();
		idle.setId("id0");
		idle.setInitial(true);
		idle.setName("Idle");
		idle.setPosX(0);
		idle.setPosY(0);
		idle.setPosXName(-8);
		idle.setPosYName(17);
		
		Location turnOff = new Location();
		turnOff.setId("id1");
		turnOff.setName("TurnOff");
		turnOff.setPosX(76);
		turnOff.setPosY(-93);
		turnOff.setPosXName(66);
		turnOff.setPosYName(-127);
		
		Location turnOn = new Location();
		turnOn.setId("id2");
		turnOn.setName("TurnOn");
		turnOn.setPosX(-76);
		turnOn.setPosY(-93);
		turnOn.setPosXName(-86);
		turnOn.setPosYName(-127);
		
		Transition on = new Transition();
		on.setSource("id0");
		on.setTarget("id2");
		Label select1 = new Label();
		select1.setKind(Strings.SELECT);
		select1.setContent("i:int[0,NODES-1]");
		select1.setPosX(-161);
		select1.setPosY(-59);
		on.addLabel(select1);
		Label update1 = new Label();
		update1.setKind(Strings.UPDATE);
		update1.setContent("nodeCrash[i] = false");
		update1.setPosX(-153);
		update1.setPosY(-42);
		on.addLabel(update1);
		
		Transition off = new Transition();
		off.setSource("id0");
		off.setTarget("id1");
		Label select2 = new Label();
		select2.setKind(Strings.SELECT);
		select2.setContent("i:int[0,NODES-1]");
		select2.setPosX(59);
		select2.setPosY(-59);
		off.addLabel(select2);
		Label update2 = new Label();
		update2.setKind(Strings.UPDATE);
		update2.setContent("nodeCrash[i] = true");
		update2.setPosX(34);
		update2.setPosY(-42);
		off.addLabel(update2);
		
		Transition onBack = new Transition();
		onBack.setSource("id2");
		onBack.setTarget("id0");
		Nail nailOnBack = new Nail();
		nailOnBack.setPosX(-34);
		nailOnBack.setPosY(-93);
		onBack.addNail(nailOnBack);
		
		Transition offBack = new Transition();
		offBack.setSource("id1");
		offBack.setTarget("id0");
		Nail nailOffBack = new Nail();
		nailOffBack.setPosX(34);
		nailOffBack.setPosY(-93);
		offBack.addNail(nailOffBack);
		
		crasher.addLocation(idle);
		crasher.addLocation(turnOff);
		crasher.addLocation(turnOn);
		crasher.addTransition(on);
		crasher.addTransition(off);
		crasher.addTransition(offBack);
		crasher.addTransition(onBack);
		an.addTemplate(crasher);
		
		return an;
	}

}

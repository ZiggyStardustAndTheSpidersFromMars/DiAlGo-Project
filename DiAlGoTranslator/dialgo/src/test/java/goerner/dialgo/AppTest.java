package goerner.dialgo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import automata.AutomataNetwork;
import automata.Location;
import automata.Template;
import res.StringTemplates;

/**
 * Simple Unit Tests
 * @author Torben Friedrich Goerner
 */
public class AppTest {
	
	@SuppressWarnings("unused")
	private static STGroup templates = new STGroupFile("src/main/java/res/templates.stg");
    
    @Test
    public void testStringTemplate() {
    	AutomataNetwork an = new AutomataNetwork();	
    	an = AutomataNetworkBuilder.setAmountOfNodesAndEdges(an, 4, 12);
    	assertEquals(an.getDeclaration(), "const int NODES = 4;\r\nconst int EDGES = 12;");
    }
    
    @Test
    public void testST_CHAN_SYNC() {
    	//ST st = templates.getInstanceOf("CHAN_SYNC");
    	ST st = StringTemplates.CHAN_SYNC;
		st.add("chan", "chanMsg");
		st.add("type", "int");
		assertEquals(st.render(), "int chanMsgVar[EDGES];\r\nchan chanMsgChan[EDGES];");
    }
    
    @Test
    public void testST_CHAN_ASYNC() {
    	//ST st = templates.getInstanceOf("CHAN_ASYNC");
    	ST st = StringTemplates.CHAN_ASYNC;
		st.add("chan", "chanMsg");
		st.add("type", "int");
		assertEquals(st.render(), "const int chanMsgBuffer = 1;\r\nint chanMsgVar[EDGES];"
				+"\r\nint chanMsgSize[EDGES];");
    }
    
    @Test
    public void testST_CHAN_ASYNC_BUFFER() {
    	//ST st = templates.getInstanceOf("CHAN_ASYNC_WITH_BUFFER");
    	ST st = StringTemplates.CHAN_ASYNC_WITH_BUFFER;
    	st.add("chan", "chanMsg");
		st.add("type", "int");
		st.add("size", 2);
		assertEquals(st.render(), "const int chanMsgBuffer = 2;\r\n"
				+ "int chanMsgVar[EDGES][chanMsgBuffer];\r\n"
				+ "int chanMsgSize[EDGES];\r\n"
				+ "int chanMsgReadIndex[EDGES];\r\n"
				+ "int chanMsgWriteIndex[EDGES];");
    }
    
    @Test
    public void testAddTemplate() {
    	AutomataNetwork an = new AutomataNetwork();
    	an = AutomataNetworkBuilder.addTemplate(an, "Test", "", "int i;");
    	assertEquals(an.getTemplates().get(0).getName(), "Test");
    }
    
    @Test
    public void testAddGlobalDecl() {
    	AutomataNetwork an = new AutomataNetwork();
    	an = AutomataNetworkBuilder.addGlobalDeclaration(an, "test test");
    	assertEquals(an.getDeclaration(), "\r\ntest test");
    }
    
    @Test
    public void testSetSystemDeclaration() {
    	AutomataNetwork an = new AutomataNetwork();
    	List<String> names = new LinkedList<>();
    	List<String> parameters = new LinkedList<>();
    	names.add("Test");
    	names.add("Test");
    	names.add("Lul");
    	parameters.add("0");
    	parameters.add("1");
    	parameters.add("2");
    	an = AutomataNetworkBuilder.setSystemDeclaration(an, names, parameters);
    	//System.out.println(an.getSystemDeclaration());
    	assertEquals(an.getSystemDeclaration(), "Test0 = Test(0);\r\n"
    			+ "Test1 = Test(1);\r\n"
    			+ "Lul2 = Lul(2);\r\n\r\n"
    			+ "system Test0,Test1,Lul2;");
    }
    
    @Test
    public void testTemplateGetHighestLocationId() {
    	Template t = new Template();
    	Location l0 = new Location();
    	Location l1 = new Location();
    	Location l2 = new Location();
    	l0.setId("id0");
    	l1.setId("id42");
    	l2.setId("id2");
    	t.addLocation(l0);
    	t.addLocation(l1);
    	t.addLocation(l2);
    	//System.out.println(t.getHighestLocationId());
    	assertEquals(t.getHighestLocationId(), l1.getId());
    }
    
    @Test
    public void testModifierAddNodeCrasher() {
    	AutomataNetwork an = new AutomataNetwork();
    	an = AutomataNetworkBuilder.addGlobalDeclaration(an, "// //");
    	an.setSystemDeclaration("system ;");
    	an = Modifier.addCrasher(an);
    	assertEquals(an.getTemplates().get(0).getName(), "NodeCrasher");
    }
    
    @Test
    public void testSetDoMarkerLocations() {
    	GoHandler gh = new GoHandler();
    	gh.doMarkerLocations(true);
    	assertTrue(gh.getGoContextHandler().getDoMarkerLocations());
    }
    
}

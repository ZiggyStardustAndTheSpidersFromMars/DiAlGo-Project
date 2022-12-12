package res;

/**
 * Commands used in DiAlGo
 * @author Torben Friedrich Goerner
 *
 */
public class Commands {
	
	/**
	 * To show the operating instructions.
	 */
	public static final String CMD_HELP = "-help";

	/**
	 * Secret droggelbecher mode.
	 */
	public static final String CMD_DROGGELBECHER = "-droggelbecher";
	
	/**
	 * To translate a distributed algorithm from GO to UPPAAL XML.
	 */
	public static final String CMD_TRANSLATE = "-translate";
	
	/**
	 * To add marker locations for print statements
	 */
	public static final String CMD_MARKER = "-m";
	
	/**
	 * To modify a UPPAAL network
	 */
	public static final String CMD_MODIFY = "-modify";
	
	/**
	 * To add a Crasher Template (usage: -modify add_crasher)
	 */
	public static final String ARG_ADD_CRASHER = "add_crasher";
	

}

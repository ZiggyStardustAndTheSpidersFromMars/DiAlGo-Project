package res;

/**
 * Messages (Strings) used in DiAlGo
 * @author Torben Friedrich Goerner
 *
 */
public class Messages {
	
	public static final String WELCOME_MSG =  "  ____                      _         _         ____      U  ___ u \r\n"
										    + " |  _\"\\        ___      U  /\"\\  u    |\"|     U /\"___|u     \\/\"_ \\/ \r\n"
											+ "/| | | |      |_\"_|      \\/ _ \\/   U | | u   \\| |  _ /     | | | | \r\n"
											+ "U| |_| |\\      | |       / ___ \\    \\| |/__   | |_| |  .-,_| |_| | \r\n"
											+ " |____/ u    U/| |\\u    /_/   \\_\\    |_____|   \\____|   \\_)-\\___/  \r\n"
											+ "  |||_    .-,_|___|_,-.  \\\\    >>    //  \\\\    _)(|_         \\\\    \r\n"
											+ " (__)_)    \\_)-' '-(_/  (__)  (__)  (_\")(\"_)  (__)__)       (__)   \r\n\n"
											+ "Simulation and Visualisation of Distributed Algorithms using GO and UPPAAL\n\n"
											+ "From Torben Friedrich Goerner TH Luebeck 2022";
	
	public static final String USER_MANUAL = "USER MANUAL:\n\n"
											+ "Usage : java -jar DiAlGo.jar [-options] [args]\n\n"
											+ "Options :\n\n"
											+ "\t-help			See operating instructions.\n"
											+ "\t-translate		Translate a distributed algorithm from GO to UPPAAL XML.\n"
											+ "\t-modify			Modifies a UPPAAL network.\n"
											+ "\nArguments :\n\n"
											+ "\t-translate		arg1:Path to GO File.\n"
											+ "\t			arg2:Amount of Nodes.\n"
											+ "\t			arg3(optional):-m to create marker locations for print statements\n"
											+ "\t-modify			arg1:Path to Uppaal XML, arg2:List of modifications(mod1,mod2,..modN).\n"
											+ "\n\nModifications :\n\n"
											+ "\t- add_crasher		Adds a Template for crashing nodes.\n";
	
	public static final String CANT_READ_FILE_ERROR = "ERROR : CAN'T READ THE FILE!";
	
	public static final String MISSING_ARGUMENTS = "ERROR : MISSING ARGUMENTS! USE '-help' TO SEE THE OPERATING INSTRUCTIONS.";
	
	public static final String DROGGELBECHER = "DiAlGo : Droggelbecher Droggelbecher Droggel Droggelbecher.\nDROGGELBECHER!!!";
	
	public static final String UPS = "ERROR : UPS... SOMETHING WENT WRONG!";
	
	public static final String WRITE_FILE = "File was written.";
	
}

package res;

import org.stringtemplate.v4.ST;

/**
 * All String Templates. Use if Maven mess up path to templates.stg
 * @author Torben Friedrich Goerner
 *
 */
public class StringTemplates {
	
	public static ST NODES_EDGES = new ST("const int NODES = <nodes>;\r\n"
			+ "const int EDGES = <edges>;");
	
	public static ST DIALGOCODE = new ST("\r\n"
			+ "//-------------------Dialgo Code---------------------\r\n"
			+ "\r\n"
			+ "int getReceiveIndex(int pidA, int pidB){\r\n"
			+ "    int in = 0;\r\n"
			+ "    if (pidA > pidB) {\r\n"
			+ "		in = pidA - 1;\r\n"
			+ "	} else {\r\n"
			+ "		in = pidA;\r\n"
			+ "	}\r\n"
			+ "	return ((NODES - 1) * pidB) + in;\r\n"
			+ "}\r\n"
			+ "\r\n"
			+ "int getSendIndex(int pidA, int pidB){\r\n"
			+ "    int in = 0;\r\n"
			+ "    if (pidB > pidA) {\r\n"
			+ "		in = pidB - 1;\r\n"
			+ "	} else {\r\n"
			+ "		in = pidB;\r\n"
			+ "	}\r\n"
			+ "	return ((NODES - 1) * pidA) + in;\r\n"
			+ "}\r\n"
			+ "\r\n"
			+ "int setNewChanIndex(int index, int bufferSize){\r\n"
			+ "    if(index == bufferSize - 1){\r\n"
			+ "        return 0;\r\n"
			+ "    }else{\r\n"
			+ "        return index + 1;\r\n"
			+ "    }\r\n"
			+ "}");

	public static ST SYSTEM_DECL_TEMPLATE = new ST("<name><pid> = <name>(<parameter>);");
	
	public static ST SEND_SYNC_UPDATE = new ST("<chan>Var[getSendIndex(pid,<pidB>)] = <msg>");
	
	public static ST SEND_SYNC_SYNC = new ST("<chan>Chan[getSendIndex(pid,<pidB>)]!");
	
	public static ST RECEIVE_SYNC_UPDATE = new ST("<var> = <chan>Var[getReceiveIndex(pid,<pidB>)]");
	
	public static ST RECEIVE_SYNC_SYNC = new ST("<chan>Chan[getReceiveIndex(pid,<pidB>)]?");
	
	public static ST SEND_ASYNC_UPDATE = new ST("<chan>Var[getSendIndex(pid,<pidB>)] = <msg>,\r\n"
			+ "<chan>Size[getSendIndex(pid, <pidB>)]++");
	
	public static ST SEND_ASYNC_GUARD = new ST("<chan>Size[getSendIndex(pid, <pidB>)] \\< <chan>Buffer");
	
	public static ST RECEIVE_ASYNC_UPDATE = new ST("<var> = <chan>Var[getReceiveIndex(pid,<pidB>)],\r\n"
			+ "<chan>Size[getReceiveIndex(pid, <pidB>)]--");
	
	public static ST RECEIVE_ASYNC_GUARD = new ST("<chan>Size[getReceiveIndex(pid, <pidB>)] != 0");
	
	public static ST SEND_ASYNC_BUFFER_UPDATE = new ST("<chan>Var[getSendIndex(pid,<pidB>)][<chan>WriteIndex[getSendIndex(pid,<pidB>)]] = <msg>,\r\n"
			+ "<chan>Size[getSendIndex(pid, <pidB>)]++,\r\n"
			+ "<chan>WriteIndex[getSendIndex(pid, <pidB>)] = setNewChanIndex(<chan>WriteIndex[getSendIndex(pid, <pidB>)],<chan>Buffer)");
	
	public static ST SEND_ASYNC_BUFFER_GUARD = new ST("<chan>Size[getSendIndex(pid, <pidB>)] \\< <chan>Buffer");
	
	public static ST SEND_NON_FIFO_UPDATE = new ST("<chan>WriteIndex[getSendIndex(pid, <pidB>)] = setNewChanIndexNonFifo(<chan>State, pid, <pidB>),\r\n"
			+ "<chan>Var[getSendIndex(pid,<pidB>)][<chan>WriteIndex[getSendIndex(pid,<pidB>)]] = <msg>,\r\n"
			+ "<chan>State[getSendIndex(pid, <pidB>)][<chan>WriteIndex[getSendIndex(pid, <pidB>)]] = true,\r\n"
			+ "<chan>Size[getSendIndex(pid, <pidB>)]++");
	
	public static ST RECEIVE_ASYNC_BUFFER_UPDATE = new ST("<var> = <chan>Var[getReceiveIndex(pid,<pidB>)][<chan>ReadIndex[getReceiveIndex(pid,<pidB>)]],\r\n"
			+ "<chan>Size[getReceiveIndex(pid, <pidB>)]--,\r\n"
			+ "<chan>ReadIndex[getReceiveIndex(pid, <pidB>)] = setNewChanIndex(<chan>ReadIndex[getReceiveIndex(pid, <pidB>)],<chan>Buffer)");
	
	public static ST RECEIVE_ASYNC_BUFFER_GUARD = new ST("<chan>Size[getReceiveIndex(pid, <pidB>)] != 0");
	
	public static ST CHAN_SYNC = new ST("<type> <chan>Var[EDGES];\r\n"
			+ "chan <chan>Chan[EDGES];");
	
	public static ST CHAN_ASYNC = new ST("const int <chan>Buffer = 1;\r\n"
			+ "<type> <chan>Var[EDGES];\r\n"
			+ "int <chan>Size[EDGES];");
	
	public static ST CHAN_ASYNC_WITH_BUFFER = new ST("const int <chan>Buffer = <size>;\r\n"
			+ "<type> <chan>Var[EDGES][<chan>Buffer];\r\n"
			+ "int <chan>Size[EDGES];\r\n"
			+ "int <chan>ReadIndex[EDGES];\r\n"
			+ "int <chan>WriteIndex[EDGES];");
	
}

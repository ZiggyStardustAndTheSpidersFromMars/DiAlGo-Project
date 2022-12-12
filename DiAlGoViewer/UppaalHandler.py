from threading import local
import DiAlGoObjects
import xml.etree.ElementTree as ET 
import numpy as np

# Uppaal Handler for reading and parsing Uppaal xml and xtr,
# Author : Torben Friedrich Goerner

class Model():
    """Model of a Network"""
    def __init__(self, pathModel, pathXtr):
        self.network = parseNetwork(pathModel, pathXtr)

    def getNetwork(self):
        return self.network

def readFile(path):
    """Reads a file as String."""
    text_file = open(path, "r")
    data = text_file.read()
    text_file.close()
    return data

def parseNetwork(pathModel, pathXtr):
    """Parses a Network of Nodes, Events, Messages from Uppaal model and xtr."""
    nodes = parseNodes(pathModel)
    events = parseEvents(pathXtr)
    variables = parseVariables(pathModel)
    messages = parseMessages(variables, events)
    timeStamp = 0
    return DiAlGoObjects.Network(nodes, variables, messages, events, timeStamp)

def parseNodes(pathModel):
    """Parses a list of Node from a uppaal model."""
    modelTree = ET.parse(pathModel) # https://docs.python.org/3/library/xml.etree.elementtree.html
    modelRoot = modelTree.getroot() 
    systemDecl = modelRoot.findall('system')[0].text.split("\n")
    templates = modelRoot.findall('template')
    nodes = []
    for i in systemDecl:
        if i == "":
            break
        n = i.split()
        name = n[2][0:n[2].index("(")]
        pid = (int)(n[2][n[2].index("(")+1:n[2].index(")")])
        localVariales = ""
        for t in templates:
            if t.find('name').text == name:
                localVariales = t.find('declaration').text.replace(";", "").replace("\n\n", "\n").replace("\r", "").replace("\t", "")
        localVarsEdit = localVariales.split("\n")
        newLocalVarsEdit = []
        for s in localVarsEdit:
            if '=' in s:
                s = s[0:s.index('=') + 1]
                newLocalVarsEdit.append(s)
            elif s != "":
                newLocalVarsEdit.append(s + "=")
                
        newLocalVars = ""
        for s in newLocalVarsEdit:
            newLocalVars = newLocalVars + s + "\n"
        node = DiAlGoObjects.Node(name, pid, newLocalVars)
        nodes.append(node)
    return nodes 

def parseVariables(pathModel):
    """Parses global Variables / global declarations of a model."""
    modelTree = ET.parse(pathModel) # https://docs.python.org/3/library/xml.etree.elementtree.html
    modelRoot = modelTree.getroot() 
    decl = modelRoot.findall('declaration')[0].text.replace(";", "").replace("\n\n", "\n").replace("\r", "").replace("\t", "").split("\n")
    newDecl = []
    for s in decl:
        if "Dialgo Code" in s:
            break
        if '=' in s and 'const' not in s:
            s = s[0:s.index('=') + 1]
            newDecl.append(s)
        elif 'const' in s:
            newDecl.append(s)
        elif s != "":
            newDecl.append(s + "=")
    finalDecl = ""
    for s in newDecl:
            finalDecl = finalDecl + s + "\n"
    return finalDecl

def parseEvents(pathXtr):
    """Parses a list of Events from a xtr file."""
    events = []
    xtr = readFile(pathXtr).replace("\r", "")
    trace = []
    write = False
    countPoints = 0
    event = ""
    for c in xtr:
        if write:
            event = event + c

        if c == ".":
            countPoints += 1  

        if countPoints == 1 and c != "\n" and c != ".":
            countPoints = 0          
        
        if countPoints == 2:
            write = True
        elif countPoints == 3:
            countPoints = 0
            if event != "":
                trace.append(event.replace(".", ""))
                event = ""
        else:
            write = False

    tempEvent = ""
    for e in trace:
        if e != tempEvent:
            events.append(e)
            tempEvent = e
    return events

def calcSenderPid(index, n, e):
    """Calculates the sender pid from an index"""
    b = e / n
    bi = index / b
    return (int)(bi)

def calcReceiverPid(index, pidSender, n, e):
    """Calculates the receiver pid from an index"""
    b = e / n
    bt = b * pidSender
    i = index - bt
    if i >= pidSender:
        return (int)(i + 1) 
    else:
        return (int)(i)

def parseMessage(arrayNew, arrayOld, nodes, edges, time):
    """Parses a Message from new set of values and old set of values"""
    index = 0
    for v in arrayNew:
        if v != arrayOld[index]:
            break
        index += 1
    pidSender = calcSenderPid(index, nodes, edges)
    pidReceiver = calcReceiverPid(index, pidSender, nodes, edges)
    value = arrayNew[index]
    message = DiAlGoObjects.Message(pidSender, pidReceiver, value, time, time + 1)
    # Time Receiving must be set later -> init with time + 1
    return message

def parseMessages(globalDecl, trace):
    """Parses Messages from variables / global decl and a list of Events."""
    messages = []
    variables = globalDecl.split("\n")

    # amount of nodes and edges
    nodes = (int)(variables[0][
        variables[0].index("=") + 1 : len(variables[0])].strip())
    edges = (int)(variables[1][
        variables[1].index("=") + 1 : len(variables[1])].strip())

    # check for every event all channel variables
    valuesOld = []
    time = 0
    for t in trace:
        values = t.replace("\n\n", "\n").split("\n")[1 : len(t)]
        varCount = 0
        for variable in variables:
            if "Var[EDGES]" in variable: 
                arrayNew = values[varCount * edges : varCount * edges  + edges]
                arrayOld = valuesOld[varCount * edges : varCount * edges  + edges]
                varCount += 1 
                if not np.array_equal(arrayNew, arrayOld) and arrayOld != []:
                    message = parseMessage(arrayNew, arrayOld, nodes, edges, time)
                    messages.append(message)
        valuesOld = values 
        time += 1  

    return messages

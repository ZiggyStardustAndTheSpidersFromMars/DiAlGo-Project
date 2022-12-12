# DiAlGo Objects
# Author : Torben Friedrich Goerner

class Node:
    """Node in a network"""
    def __init__(self, name, pid, state):
        self.name = name
        self.pid = pid
        self.state = state
    
    def getName(self):
        return self.name

    def getPid(self):
        return self.pid

    def getState(self):
        return self.state

class Message:
    """Message Object"""
    def __init__(self, sender, receiver, content, timeSending, timeReceiving):
        self.sender = sender
        self.receiver = receiver
        self.content = content
        self.timeSending = timeSending
        self.timeReceiving = timeReceiving

    def getSender(self):
        return self.sender

    def getReceiver(self):
        return self.receiver
    
    def getContent(self):
        return self.content

    def getTimeSending(self):
        return self.timeSending

    def getTimeReceiving(self):
        return self.timeReceiving

    def setTimeReceiving(self, time):
        self.timeReceiving = time

class Network:
    """Network of Nodes with Events and Message Trafic and a current Timestamp"""
    def __init__(self, nodes, variables, messages, events, timeStamp):
        self.nodes = nodes
        self.variables = variables
        self.messages = messages
        self.events = events
        self.timeStamp = timeStamp

    def getNodes(self):
        return self.nodes

    def getVariables(self):
        return self.variables

    def getMessages(self):
        return self.messages
    
    def getEvents(self):
        return self.events

    def getTimeStamp(self):
        return self.timeStamp

    def updateTimeStamp(self, value):
        self.timeStamp = value

class Event:
    """Event Object with a set of states of all variables"""
    def __init__(self, valueSet, timeStamp):
        self.valueSet = valueSet
        self.timeStamp = timeStamp

    def getValueSet(self):
        return  self.valueSet

    def getTimeStamp(self):
        return self.timeStamp
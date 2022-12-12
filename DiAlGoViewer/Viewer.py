from cProfile import run
from inspect import currentframe
from xml.etree.ElementInclude import include
from PyQt5 import QtWidgets, QtCore, QtGui
from PyQt5.QtWidgets import QApplication, QMainWindow, QSizePolicy, QWidget, QLabel
import sys, math, DiAlGoObjects, UppaalHandler, Values
from functools import partial
import time
from threading import Thread

# DiAlGo Viewer for visualsing a simulation of a distributed algorithm from Uppaal xml and xtr.
# Part of Master-Thesis 2022 - Simulation and Visualisation of Distributed Algorithms using GO and UPPAAL
# Author : Torben Friedrich Goerner

pathModel = "ModelAndXtr/Huang.xml" # Path to model (demo with huang)
pathXtr = "ModelAndXtr/HuangSimulation256.xtr" # Path to xtr (demo with huang)
model = UppaalHandler.Model(pathModel, pathXtr)
running = False # State of the auto play
tempNode = None
tempMsg = None
ui = None

#-------------------------------------- Control Elements------------------------------------------

def nodeClicked(node):
    ui.showNodeWindow(node, False)

def backClicked():
    timeStamp = model.getNetwork().getTimeStamp() - 1
    if timeStamp >= 0 :
        model.getNetwork().updateTimeStamp(timeStamp)
        ui.showMessages()
        updatePbar()
        ui.showNodeWindow(tempNode, True)

def playClicked():
    global running
    running = not running
    ui.changePlayButton()
    updatePbar()
    if running:
        ui.playerStart()

def play():
    '''Auto-Playing the Simulation while running is true'''
    while running:
        timeStamp = model.getNetwork().getTimeStamp() + 1
        if timeStamp < len(model.getNetwork().getEvents()) :
            model.getNetwork().updateTimeStamp(timeStamp)
            ui.showMessages()
            updatePbar()
            ui.showNodeWindow(tempNode, True)
            time.sleep(Values.PLAY_TIME)
        else:
            break

def skipClicked():
    timeStamp = model.getNetwork().getTimeStamp() + 1
    if timeStamp < len(model.getNetwork().getEvents()) :
        model.getNetwork().updateTimeStamp(timeStamp)
        ui.showMessages()
        updatePbar()
        ui.showNodeWindow(tempNode, True)

def goToEndClicked():
    model.getNetwork().updateTimeStamp(len(model.getNetwork().getEvents()) - 1)
    ui.showMessages()
    updatePbar()
    ui.showNodeWindow(tempNode, True)

def goToStartClicked():
    model.getNetwork().updateTimeStamp(0)
    ui.showMessages()
    updatePbar()
    ui.showNodeWindow(tempNode, True)

def backgroundClicked():
    print("background click!")
    # possible to show global Variables or constants here

#--------------------------------------------------------------------------------------------
class Player(QtCore.QThread):
    '''Thread for Auto-Play'''
    def run(self):
        play()

class Window(QMainWindow):
    """Main Window - GUI for visualising the Simulation"""
    def __init__(self):
        super(Window, self).__init__()
        self.setFixedSize(QtCore.QSize(Values.FIELD_SIZE_X, Values.FIELD_SIZE_Y))
        self.setWindowTitle("DiAlGo Viewer")
        self.button_back = QtWidgets.QPushButton()
        self.button_play = QtWidgets.QPushButton()
        self.button_skip = QtWidgets.QPushButton()
        self.button_goToStart = QtWidgets.QPushButton()
        self.button_goToEnd = QtWidgets.QPushButton()

        self.pbar = QtWidgets.QProgressBar(self)
        self.pbar.setValue(0)

        self.iniUI()
        self.drawBackground()
        self.buttons_Nodes = createNodes(self)
        self.nodeText = QtWidgets.QTextBrowser(self)
        self.nodeText.setVisible(False)

        self.buttons_Messages = []
        count = 0
        for m in model.getNetwork().getMessages():
            text = "Message" + str(count) + "\nValue:" \
                + model.getNetwork().getMessages()[count].getContent()
            btn = QtWidgets.QPushButton(text, self)
            btn.clicked.connect(partial(self.showMessageInfo, count))
            btn.setVisible(False)
            self.buttons_Messages.append(btn)
            count += 1
        self.messageInfoText = QtWidgets.QTextBrowser(self)
        self.messageInfoText.setVisible(False)

        self.timestamp = QLabel(Values.LABEL_TIMESTAMP + "0", self)
        self.timestamp.setGeometry(Values.FIELD_SIZE_X - 150, 5, 250, 25)
        self.timestamp.setFont(QtGui.QFont('Arial', 10))

        self.player = Player()

    def drawBackground(self):
        ring = (int)(Values.FIELD_SIZE_Y - Values.NODE_SIZE * 2.3)
        midX = (int)(Values.FIELD_SIZE_X / 2)
        midY = (int)(Values.FIELD_SIZE_Y / 2.2)
        backgroundButton = QtWidgets.QPushButton('', self)
        backgroundButton.clicked.connect(backgroundClicked)
        backgroundButton.setText(pathModel[pathModel.rindex("/") + 1:pathModel.index(".")])
        backgroundButton.setGeometry((int)(midX - (ring / 2)), (int)(midY - (ring / 2)), ring, ring) 
        backgroundButton.setStyleSheet(
            "border-radius : " + str((int)(ring/2))
            + "; border : 2px solid " + Values.COLOR_BACKGROUND_BORDER
            + "; background-color : " + Values.COLOR_BACKGROUND_BACKGROUND
            + "; font-size: 18pt;")

    def iniUI(self):
        """Init of all Buttons and UI Elements"""
        w = QtWidgets.QWidget()
        self.setCentralWidget(w)
        grid = QtWidgets.QGridLayout(w)
        self.button_back.setText("")
        self.button_back.setIcon(QtGui.QIcon('res/Back.png'))
        self.button_back.setIconSize(QtCore.QSize(25,25))
        self.button_back.clicked.connect(backClicked)
        self.button_play.setText("")
        self.button_play.setIcon(QtGui.QIcon('res/Play.png'))
        self.button_play.setIconSize(QtCore.QSize(25,25))
        self.button_play.clicked.connect(playClicked)
        self.button_skip.setText("")
        self.button_skip.setIcon(QtGui.QIcon('res/Skip.png'))
        self.button_skip.setIconSize(QtCore.QSize(25,25))
        self.button_skip.clicked.connect(skipClicked)
        self.button_goToStart.setText("")
        self.button_goToStart.setIcon(QtGui.QIcon('res/GoToStart.png'))
        self.button_goToStart.setIconSize(QtCore.QSize(25,25))
        self.button_goToStart.clicked.connect(goToStartClicked)
        self.button_goToEnd.setText("")
        self.button_goToEnd.setIcon(QtGui.QIcon('res/GoToEnd.png'))
        self.button_goToEnd.setIconSize(QtCore.QSize(25,25))
        self.button_goToEnd.clicked.connect(goToEndClicked)

        self.pbar.setStyleSheet(
            "QProgressBar::chunk { background-color: " + Values.COLOR_PBAR_BACKGROUND 
            + "; } QProgressBar{ text-align: center; border : 1px solid " 
            + Values.COLOR_PBAR_BORDER + ";}")
        self.pbar.setGeometry(25, (int)(Values.FIELD_SIZE_Y - 75), (int)(Values.FIELD_SIZE_X - 50), 25)

        grid.addWidget(self.button_goToStart, 0, 0, QtCore.Qt.AlignBottom)
        grid.addWidget(self.button_back, 0, 1, QtCore.Qt.AlignBottom)
        grid.addWidget(self.button_play, 0, 2, QtCore.Qt.AlignBottom)
        grid.addWidget(self.button_skip, 0, 3, QtCore.Qt.AlignBottom)
        grid.addWidget(self.button_goToEnd, 0, 4, QtCore.Qt.AlignBottom)

    def playerStart(self):
        '''Starts Auto-Play in new Thread'''
        self.player.start()

    def changePlayButton(self):
        if not running :
            self.button_play.setIcon(QtGui.QIcon('res/Play.png'))
        else :
            self.button_play.setIcon(QtGui.QIcon('res/Pause.png'))

    def showNodeWindow(self, node, isUpdate):
        """Shows a Node Info TextBrowser with Info of a Node."""
        global tempNode
        if  tempNode != node or (isUpdate and tempNode != None):
            ring = Values.FIELD_SIZE_Y - Values.NODE_SIZE * 2
            midX = Values.FIELD_SIZE_X / 2
            midY = Values.FIELD_SIZE_Y / 2.2
            amountOfNodes =  len(model.getNetwork().getNodes())
            x = (int)((ring / 2 * math.cos(2 * math.pi / amountOfNodes * node.getPid())) \
                + midX - Values.NODE_SIZE / 2)
            y = (int)((-ring / 2 * math.sin(2 * math.pi / amountOfNodes * node.getPid())) \
                + midY - Values.NODE_SIZE / 2)

            self.nodeText.setGeometry(QtCore.QRect(x + 25, y - 20, 150, 200)) # TODO Position und Größe ggf. anpassen
            self.nodeText.setText(
                node.getName() + str(node.getPid()) + ":\n\n" 
                + getNodeTextByTimeStamp(node, model.getNetwork().getTimeStamp()))
            self.nodeText.setStyleSheet(
                "background-color: " + Values.COLOR_NODE_TEXT_BACKGROUND + ";")
            self.nodeText.setVisible(True)
            tempNode = node
        else :
            self.nodeText.setVisible(False)
            tempNode = None

    def showMessageInfo(self, msg):
        """Shows a Message Infotext"""
        global tempMsg
        if tempMsg != msg:
            x = self.buttons_Messages[msg].x() + 50
            y = self.buttons_Messages[msg].y() - 25
            self.messageInfoText.setGeometry(QtCore.QRect(x, y, 150, 75)) # TODO Position und Größe anpassen
            m = model.getNetwork().getMessages()[msg]
            text = (
                "Value:" + m.getContent() + "\nFrom PID:" + str(m.getSender()) 
                + " To PID:" + str(m.getReceiver()) + "\n\n" 
                + "Timestamp Send-Action:" + str(m.getTimeSending()) + "\n"
                + "Timestamp Receive-Action:" + str(m.getTimeReceiving()))
            self.messageInfoText.setText(text)
            self.messageInfoText.setStyleSheet(
                "background-color: " + Values.COLOR_MESSAGE_TEXT_BACKGROUND + ";")
            self.messageInfoText.setVisible(True)
            tempMsg = msg
        else:
            tempMsg = None
            self.messageInfoText.setVisible(False)

    def showMessages(self):
        """Shows a Message-Entity"""
        currentMessages = getCurrentMessages()
        
        for b in self.buttons_Messages:
            b.setVisible(False)
    
        for m in currentMessages:
            x = 0
            y = 0
            if model.getNetwork().getMessages()[m].getTimeReceiving() == model.getNetwork().getTimeStamp():
                x = self.buttons_Nodes[model.getNetwork().getMessages()[m].getReceiver()].x()
                y = self.buttons_Nodes[model.getNetwork().getMessages()[m].getReceiver()].y()
            else:
                x = self.buttons_Nodes[model.getNetwork().getMessages()[m].getSender()].x()
                y = self.buttons_Nodes[model.getNetwork().getMessages()[m].getSender()].y()
            self.buttons_Messages[m].setGeometry(x, y, Values.MESSAGE_SIZE, Values.MESSAGE_SIZE)
            self.buttons_Messages[m].setStyleSheet(
                "border-radius :" + str((int)(Values.MESSAGE_SIZE/2)) 
                + "; border : 2px solid " + Values.COLOR_MESSAGE_BORDER 
                + "; background-color : " + Values.COLOR_MESSAGE_BACKGROUND + ";")
            self.buttons_Messages[m].setVisible(True)

    def updatePbar(self, v):
        self.pbar.setValue(v)
        labelText = Values.LABEL_TIMESTAMP + str(model.getNetwork().getTimeStamp())
        self.timestamp.setText(labelText)

#--------------------------------------------Functions---------------------------------------------

def updatePbar():
    value = (int)(model.getNetwork().getTimeStamp() \
        / (len(model.getNetwork().getEvents()) - 1) * 100)
    ui.updatePbar(value)

def getCurrentMessages():
    """Gets all current messages on the actuel timestamp"""
    currentMessages = []
    count = 0
    for m in model.getNetwork().getMessages():
        if (
                (m.getTimeSending() <= model.getNetwork().getTimeStamp()) 
                and (m.getTimeReceiving() >= model.getNetwork().getTimeStamp())
            ):
            currentMessages.append(count)
        count += 1
    return currentMessages

def createNodes(mw):
    """Creates the Nodes (Buttons) of a net in a circular order."""
    ring = Values.FIELD_SIZE_Y - Values.NODE_SIZE * 2.3
    midX = Values.FIELD_SIZE_X / 2
    midY = Values.FIELD_SIZE_Y / 2.2
    amountOfNodes =  len(model.getNetwork().getNodes())
    nodes = []
    for i in range(0, amountOfNodes):
        x = ring / 2 * math.cos(2 * math.pi / amountOfNodes * i)
        y = -ring / 2 * math.sin(2 * math.pi / amountOfNodes * i)
        name = model.getNetwork().getNodes()[i].getName() \
            + str(model.getNetwork().getNodes()[i].getPid())
        nodeButton = QtWidgets.QPushButton(name, mw)
        nodeButton.clicked.connect(partial(nodeClicked, model.getNetwork().getNodes()[i]))
        nodeButton.setGeometry(
            (int)(x + midX - Values.NODE_SIZE / 2), 
            (int)(y + midY - Values.NODE_SIZE / 2), 
            Values.NODE_SIZE, Values.NODE_SIZE) 
        nodeButton.setStyleSheet(
            "border-radius :" + str((int)(Values.NODE_SIZE/2)) 
            + "; border : 2px solid " + Values.COLOR_NODE_BORDER
            + "; background-color : " + Values.COLOR_NODE_BACKGROUND + ";")
        nodes.append(nodeButton)
    return nodes

def getNodeTextByTimeStamp(node, timeStamp):
    """Generates the text with all local vairalbles and theier state from node and a given timestamp."""
    vars = node.getState().split("\n")
    values = model.getNetwork().getEvents()[timeStamp].split("\n")
    startIndex = len(values) - 1
    for x in range (node.getPid(), len(model.getNetwork().getNodes())):
        startIndex = startIndex - (len(model.getNetwork().getNodes()[x].getState().split("\n"))-1)
    stopIndex = startIndex + len(vars) - 1
    text = ""
    count = 0
    for x in range (startIndex, stopIndex):
        text = text + vars[count] + values[x] + "\n"
        count += 1
    return text

    #--------------------------------------------------------------------------------------------

def window():
    """Creats the App."""
    app = QApplication(sys.argv)
    global ui
    ui = Window()
    ui.show()
    sys.exit(app.exec_())
    
# MAIN - PROGRAM START
if __name__ == "__main__":
    window()

# TODOs
# TODO Parsing receiving time of messages
# TODO Animation for Messages with play button
# TODO Auto-Player buggy
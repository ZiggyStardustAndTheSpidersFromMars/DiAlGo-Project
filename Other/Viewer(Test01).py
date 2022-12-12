import PySimpleGUI as sg
import math

NODES = 8
FIELD_SIZE = 550

pathToModel = "C:/Users/frich/Desktop/Torbens Unsinn/Huang.txt"
pathToTrace = ""

listOfNodes = []

def readFile(path):
    text_file = open(path, "r")
    data = text_file.read()
    text_file.close()
    return data

#modelXml = readFile(pathToModel)
#traceXtr = readFile(pathToTrace)

def createNodes(graph):
    size = 50
    ring = FIELD_SIZE - size * 2.5
    midX = FIELD_SIZE / 2 + size / 2
    midY = FIELD_SIZE / 2 + size / 2
    for i in range(0, NODES):
        x = ring / 2 * math.cos(2 * math.pi / NODES * i)
        y = -ring / 2 * math.sin(2 * math.pi / NODES * i)
        listOfNodes.append(graph.draw_circle((x + midX - size / 2, y + midY - size / 2), size, fill_color='blue', line_color='blue'))
 
layout = [
    [sg.Graph(canvas_size=(FIELD_SIZE, FIELD_SIZE), graph_bottom_left=(0, 0), graph_top_right=(FIELD_SIZE, FIELD_SIZE), background_color='#DAE0E6', enable_events=True, key='graph')],
    [
        sg.Button('Back', key = '-Back-'),
        sg.Button('Stop/Play', key = '-StopPlay-'),    
        sg.Button('Next', key = '-Next-')
    ],
    [sg.Text('Output', key = '-OUTPUT-')]
]
 
window = sg.Window('DiAlGo Viewer', layout, finalize=True)

graph = window['graph']
createNodes(graph)

while True:
    event, values = window.read()
 
    if event == sg.WIN_CLOSED:
        break
 
    if event == '-StopPlay-':
        window['-OUTPUT-'].update('Ich stoppe')
    
    if event == '-Next-':
        window['-OUTPUT-'].update('Ich gehe vor')

    if event == '-Back-':
        window['-OUTPUT-'].update('Ich gehe zurueck')

window.close()
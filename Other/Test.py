# Berechnen von Sender und Empfänger PID anhand des Index im Channel Array

NODES = 3
EDGES = 6

def calcSenderPid(index):
    b = EDGES / NODES
    bi = index / b
    return (int)(bi)

def calcReceiverPid(index, pidSender):
    b = EDGES / NODES
    bt = b * pidSender
    #be = bt + b - 1
    i = index - bt
    if i >= pidSender:
        return (int)(i + 1) 
    else:
        return (int)(i)

print(calcSenderPid(3))
print(calcReceiverPid(3, calcSenderPid(3)))
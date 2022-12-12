import numpy as np
import matplotlib.pyplot as plt


data1 = [('3 Nodes',0.047),('4 Nodes',2.219),('5 Nodes',68.844)]
data2 = [('3 Nodes',13892),('4 Nodes',55220 ),('5 Nodes',1213596 )]

plt.rcParams["figure.figsize"] = [7.00, 3.50]
plt.rcParams["figure.autolayout"] = True

ind1 = []
fre1 = []
for item in data1:
   ind1.append(item[0])
   fre1.append(item[1])

ind2 = []
fre2 = []
for item in data2:
   ind2.append(item[0])
   fre2.append(item[1])

plt.bar(ind1, fre1)
#plt.bar(ind2, fre2)

plt.show()
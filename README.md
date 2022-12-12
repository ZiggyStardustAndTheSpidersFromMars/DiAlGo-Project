# Simulation and Visualisation of Distributed Algorithms using GO and UPPAAL

Master Thesis 2022 <br>
Torben Friedrich Görner <br>
Prof. Dr. rer. nat. Andreas Schäfer

# Description

Within the scope of the master's thesis, it is examined how concepts of distributed algorithms can be modelled, analysed and visualised.
The programming language GO is used to model and specify the algorithm. The specification is then converted into a net of timed automata and analysed using UPPAAL.
As a first step, it is investigated which concepts of distributed algorithms are crucial and how they can be realised in GO. As a second step,  a translation scheme of the aforementioned specification from GO to UPPAAL is developed and prototypically implemented. Lastly, it is determined how traces are used in UPPAAL and how they can be used to visualise the simulation of distributed algorithms.

# Project Structure 

**DiAlGo Translator** This is the Java Maven project for the automated translation from GO to UPPAAL.<br>
**DiAlGo Viewer** This is the Python project for visualising distributed algorithms using UPPAAL traces.<br>
**dialgo.go** This is the GO Framework for specifying distributed algorithms in GO.<br>
**Examples** All examples presented in the thesis, as well as others, are included here.<br>
**Images** All Images used in the thesis are included here.<br>
**dialgo-translator.jar** This is the executable jar file of the DiAlGo Translator.<br>
**Other** Other stuff is included here.

# Usage of Dialgo.go Framework

The dialgo.go file must be included via an import and provides various functions for the implementation of distributed algorithms in GO. The number of nodes and edges in the network must be adjusted in the dialgo.go file. A distributed algorithm is specified by: User Code + dialgo.go.

# Usage of Dialgo Translator

USER MANUAL:

Usage : java -jar DiAlGo.jar [-options] [args]

Options :

	-help			See operating instructions.
	-translate		Translate a distributed algorithm from GO to UPPAAL XML.
	-modify			Modifies a UPPAAL network.

Arguments :

	-translate		arg1:Path to GO File.
				arg2:Amount of Nodes.
				arg3(optional):-m to create marker locations for print statements
	-modify			arg1:Path to Uppaal XML, arg2:List of modifications(mod1,mod2,..modN).


Modifications :

	- add_crasher		Adds a Template for crashing nodes.


# Usage of DiAlGo Viewer

To run the Viewer run Viewer.py in the DiAlGoViewer folder. The Viewer contains an example simulation of a trace from our implementation of Huang's algorithm.

# Example Algorithms
With this work, 3 distributed algorithms from the categories of mutual exclusion, leader election and termination were implemented. These are important fields for the use of distributed algorithms. This examples can be found in the appendix or in the Examples folder.

- Huang's algorithm was implemented for termination, 
- Bully algorithm for leader election and 
- Suzuki-Kasami for mutual exclusion.


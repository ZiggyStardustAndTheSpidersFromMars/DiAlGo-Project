package main

/*
Example for Huang's algorithm.
*/

import (
	"fmt"
	"os"
	"sync"
)

// Weight = 2^12
const WEIGHT int = 4096 // 2^12

// Channel
var int_chanMsg [EDGES]chan interface{} // Array of chan for sending msg with weight

func main() {

	Init()

	// Init chans
	for i := range int_chanMsg {
		int_chanMsg[i] = make(chan interface{}, 1)
	}

	fmt.Println("Nodes: ", NODES)

	// Waitgroup of all nodes
	var wg sync.WaitGroup
	wg.Add(NODES)

	go func() {
		controlAgent(0)
		wg.Done()
	}()
	go func() {
		worker(1)
		wg.Done()
	}()
	go func() {
		worker(2)
		wg.Done()
	}()
	go func() {
		worker(3)
		wg.Done()
	}()

	wg.Wait()
}

// Worker node in a distributed system
func worker(pid int) {

	var active bool = false
	var hasSendActivations bool = false
	var weight int = 0
	var goIdle bool = false
	var doSendActivation bool = false

	for true {

		// Listen for activations
		for nodePid := 0; nodePid < NODES; nodePid++ {
			if nodePid != pid {
				select {
				case msg0 := <-int_chanMsg[GetReceiveIndex(pid, nodePid)]:
					fmt.Println("PID", pid, "received", msg0)
					weight += msg0.(int)
					active = true
				default:
				}
			}
		}

		if active {
			if hasSendActivations {
				goIdle = RandomBool()
				if goIdle { // Go non deterministic to idle
					active = false
					hasSendActivations = false
					fmt.Println("PID", pid, "sending", weight, "to Control-Agent")
					Send(pid, 0, int_chanMsg, weight)
					weight = 0
				}
			} else {
				for i := 1; i < NODES; i++ { // Send non deterministic activations to other nodes
					if i != pid {
						doSendActivation = RandomBool()
						if doSendActivation {
							fmt.Println("PID", pid, "sending", weight/2, "to PID", i)
							Send(pid, i, int_chanMsg, weight/2)
							weight = weight / 2
						}
					}
				}
				hasSendActivations = true
			}
		}
	}
}

// Control agent for worker nodes
func controlAgent(pid int) {

	var terminated bool = false
	var weight int = WEIGHT / 2

	Send(pid, 1, int_chanMsg, WEIGHT/2)

	for !terminated {

		// Listen for getting back weight
		select {
		case msg0 := <-int_chanMsg[GetReceiveIndex(pid, 1)]:
			fmt.Println("PID", pid, "received", msg0, "from PID 1. New weight=", weight+msg0.(int))
			weight += msg0.(int)
		case msg1 := <-int_chanMsg[GetReceiveIndex(pid, 2)]:
			fmt.Println("PID", pid, "received", msg1, "from PID 2. New weight=", weight+msg1.(int))
			weight += msg1.(int)
		case msg2 := <-int_chanMsg[GetReceiveIndex(pid, 3)]:
			fmt.Println("PID", pid, "received", msg2, "from PID 3. New weight=", weight+msg2.(int))
			weight += msg2.(int)
		default:
		}

		if weight == WEIGHT {
			terminated = true
		}
	}

	fmt.Println("Control Agent with PID", pid, ": Seems like everyone is done!")
	fmt.Println("Control Agent with PID", pid, "terminated!")
	os.Exit(3)
}

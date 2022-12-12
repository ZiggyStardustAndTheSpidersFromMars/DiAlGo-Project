package main

import (
	"fmt"
	"os"
	"sync"
	"time"
)

// chans
var chanComputationalMsg [EDGES]chan interface{}  // Array of chan for sending computational msg
var chanAcknowledgmentMsg [EDGES]chan interface{} // Array of chan for sending acknowledgment msg

func main() {

	// init chans
	for i := range chanComputationalMsg {
		chanComputationalMsg[i] = make(chan interface{}, 1)
	}

	for i := range chanAcknowledgmentMsg {
		chanAcknowledgmentMsg[i] = make(chan interface{}, 1)
	}

	// ignore prints in translation to uppaal
	fmt.Println("Nodes: ", NODES)

	// waitgroup of all nodes
	var wg sync.WaitGroup
	wg.Add(NODES)

	go func() {
		rootNode(0)
		wg.Done()
	}()
	go func() {
		node(1)
		wg.Done()
	}()
	go func() {
		node(2)
		wg.Done()
	}()
	go func() {
		node(3)
		wg.Done()
	}()

	wg.Wait()
}

// node in a distributed algorithm
func node(pid int) {
	var pidParent int = 0
	var active bool = false
	var amountOfChildren int = 0
	var childPids [NODES - 1]int
	var childrenRdy [NODES - 1]bool // array has to be in max size for n-1 Nodes

	for {

		// listen on channel chanComputationalMsg for a computational msg
		for i := 0; i < NODES; i++ {
			if i != pid {
				select {
				case msg := <-chanComputationalMsg[GetReceiveIndex(pid, i)]:
					if !active {
						fmt.Println("Worker with PID ", pid, ": recived computational msg from ", msg)
						active = true
						pidParent = msg.(int)
						amountOfChildren = RandomInt(0, 2)
						fmt.Println("Worker with PID ", pid, ": I have ", amountOfChildren, "children")
						// init childrenRdy array with false in range of all children, chose childrens and send cm to childrens
						for i := 0; i < amountOfChildren; i++ {
							var childPid int = RandomOtherPid(1, 3, pid)
							var validChild bool = true
							for j := 0; j < i; j++ {
								if childPids[j] == childPid {
									validChild = false
									i--
								}
							}
							if validChild {
								childPids[i] = childPid
								childrenRdy[i] = false
								Send(pid, childPids[i], chanComputationalMsg, pid)
							}
							time.Sleep(10 * time.Millisecond) // tactical wait because of random seed based on time
						}
					} else {
						fmt.Println("Worker with PID ", pid, ": recived computational msg from ", msg, ". Already working -> sending ack back")
						Send(pid, i, chanAcknowledgmentMsg, true)
					}
				default:
				}
			}
		}

		if active {
			// listen on channel chanAcknowledgmentMsg for a acknowledgment msg from children
			for i := 0; i < amountOfChildren; i++ {
				select {
				case msg := <-chanAcknowledgmentMsg[GetReceiveIndex(pid, childPids[i])]:
					fmt.Println("Worker with PID ", pid, ": recived acknowledgment msg from ", childPids[i])
					childrenRdy[i] = msg.(bool)
				default:
				}
			}

			var allChildrenRdy bool = true

			for i := 0; i < amountOfChildren; i++ {
				if childrenRdy[i] == false {
					allChildrenRdy = false
				}
			}

			if allChildrenRdy {
				active = RandomBool() // going to idle non deterministic
				if !active {
					fmt.Println("Worker with PID ", pid, " now idle")
					Send(pid, pidParent, chanAcknowledgmentMsg, true) // send ack to parent
				}
			}
		}
	}
}

// root node in a distributed algorithm
func rootNode(pid int) {

	var childPids [NODES - 1]int
	var childrenRdy [NODES - 1]bool
	var amountOfChildren int = RandomInt(1, 2)

	for i := 0; i < amountOfChildren; i++ {
		var childPid int = RandomInt(1, 3)
		var validChild bool = true
		for j := 0; j < i; j++ {
			if childPids[j] == childPid {
				validChild = false
				i--
			}
		}
		if validChild {
			childPids[i] = childPid
			childrenRdy[i] = false
			Send(pid, childPids[i], chanComputationalMsg, pid)
		}
	}

	for {

		// listen on channel chanAcknowledgmentMsg for a acknowledgment msg from children
		for i := 0; i < amountOfChildren; i++ {
			select {
			case msg := <-chanAcknowledgmentMsg[GetReceiveIndex(pid, childPids[i])]:
				fmt.Println("Root-Node with PID ", pid, ": recived acknowledgment msg from ", childPids[i])
				childrenRdy[i] = msg.(bool)
			default:
			}
		}

		var allChildrenRdy bool = true

		for i := 0; i < amountOfChildren; i++ {
			if childrenRdy[i] == false {
				allChildrenRdy = false
			}
		}

		if allChildrenRdy { // termination
			fmt.Println("Root-Node with PID ", pid, ": all children terminated!")
			os.Exit(3)
		}
	}

}

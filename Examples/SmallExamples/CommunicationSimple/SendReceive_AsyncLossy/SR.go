package main

/*
Example for asynchron communication.
1 sender sends to 1 receiver
*/

import (
	"fmt"
	"sync"
)

// chans
var chanMsg [EDGES]chan interface{} // Array of chan for sending msg

func main() {

	Init()

	// init chans
	for i := range chanMsg {
		chanMsg[i] = make(chan interface{}, 1)
	}

	// ignore prints in translation to uppaal
	fmt.Println("Nodes: ", NODES)

	// waitgroup of all nodes
	var wg sync.WaitGroup
	wg.Add(NODES)

	go func() {
		sender(0)
		wg.Done()
	}()
	go func() {
		receiver(1)
		wg.Done()
	}()

	wg.Wait()
}

// receiver
func receiver(pid int) {
	i := <-chanMsg[GetReceiveIndex(pid, 0)]
	fmt.Println("PID ", pid, " received ", i)
}

// sender
func sender(pid int) {
	SendLossy(pid, 1, chanMsg, 42)
}

package main

/*
Example for synchron communication.
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
		chanMsg[i] = make(chan interface{})
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
	var x int = 0
	for x < 42 {
		x = x + 1
	}
	fmt.Println("done!")
}

// sender
func sender(pid int) {
	var x int = 42
}

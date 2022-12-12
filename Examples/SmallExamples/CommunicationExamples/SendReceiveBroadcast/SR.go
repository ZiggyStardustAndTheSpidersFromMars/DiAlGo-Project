package main

/*
Example for asynchronous sending broadcast from 1 sender to 3 receivers
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
		receiver(0)
		wg.Done()
	}()
	go func() {
		receiver(1)
		wg.Done()
	}()
	go func() {
		receiver(2)
		wg.Done()
	}()
	go func() {
		sender(3)
		wg.Done()
	}()

	wg.Wait()
}

// receiver
func receiver(pid int) {
	msg1 := <-chanMsg[GetReceiveIndex(pid, 3)]
	fmt.Println("PID ", pid, "received", msg1)
}

// sender
func sender(pid int) {
	SendBroadcast(pid, chanMsg, 42)
}

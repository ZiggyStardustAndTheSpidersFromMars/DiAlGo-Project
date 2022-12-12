package main

/*
Example for synchron communication.
1 sender sends non deterministic 10 time a value to 1 of 2 receiver nodes randomly.
*/

import (
	"fmt"
	"os"
	"sync"
	"time"
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
		sender(2)
		wg.Done()
	}()

	wg.Wait()
}

// receiver
func receiver(pid int) {
	for {
		i := <-chanMsg[GetReceiveIndex(pid, 2)]
		fmt.Println("PID ", pid, " received ", i)
	}
}

// sender
func sender(pid int) {
	for i := 0; i < 10; i++ {
		if RandomBool() {
			Send(pid, RandomInt(0, 1), chanMsg, i)
			time.Sleep(100 * time.Millisecond)
		}
	}
	os.Exit(3)
}

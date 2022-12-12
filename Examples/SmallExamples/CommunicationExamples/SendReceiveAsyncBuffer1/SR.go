package main

/*
Example for asynchronous communication via a channel with a buffer of 1.
2 sender nodes in a cluster sending non deterministic 10 times a value with their pid to one of 2 receiver nodes randomly.
*/

import (
	"fmt"
	"os"
	"sync"
)

// structs
type Msg struct {
	pid   int
	value int
}

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
	go func() {
		sender(3)
		wg.Done()
	}()

	wg.Wait()
}

// receiver
func receiver(pid int) {
	for {
		select {
		case msg0 := <-chanMsg[GetReceiveIndex(pid, 2)]:
			fmt.Println("PID ", pid, "received", msg0)
		case msg1 := <-chanMsg[GetReceiveIndex(pid, 3)]:
			fmt.Println("PID ", pid, "received", msg1)
		default:
		}
	}
}

// sender
func sender(pid int) {
	for i := 0; i < 10; i++ {
		if RandomBool() {
			Send(pid, RandomInt(0, 1), chanMsg, Msg{pid, i})
		}
	}
	os.Exit(3)
}

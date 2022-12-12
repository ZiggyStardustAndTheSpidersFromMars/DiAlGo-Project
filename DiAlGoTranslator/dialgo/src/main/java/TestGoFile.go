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
var int_chanMsg [EDGES]chan interface{} // Array of chan for sending msg

func main() {

	Init()

	// init chans
	for i := range int_chanMsg {
		int_chanMsg[i] = make(chan interface{}, 1)
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

	wg.Wait()
}

// sender
func sender(pid int) {
	var test int = 0
	var queue [NODES]int
	var dings bool = false
	
	for i:=0; i< 3; i++{
		fmt.Println("uwu");
	}
	
	for i:=0; i< 2; i++{
		fmt.Println("uwu");
	}
	
	for !dings{
		fmt.Println("uwu");
		dings = RandomBool()
	}

	select {
	case requestMsg1 := <-int_chanMsg [GetReceiveIndex(pid, 0)]:
		fmt.Println("PID", pid, "received Request", requestMsg1, "from PID", 0)
	default:
	}

	select {
	case requestMsg1 := <-int_chanMsg [GetReceiveIndex(pid, 0)]:
		fmt.Println("PID", pid, "received Request", requestMsg1, "from PID", 0)
	default:
	}
}

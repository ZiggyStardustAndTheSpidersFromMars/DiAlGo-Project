package main

import (
	"fmt"
	"sync"
	"time"
)

// global const. and var.
const NODES int = 3 // amount of nodes

const EDGES int = 6 // amoount of edges(n*(n-1)/2)*2 --kurz--> n*(n-1) 2 Kanäle für jede Art der Kommunikation weil Kanäle hier unidirektional genutzt werden müssen

const (
	YES       = 1
	NO        = 0
	NO_ANSWER = -1
)

var timestamp int = 0 // logical clock (global)

// chans
var chanRequest [EDGES]chan info  // Array of chan for sending requests
var chanResponse [EDGES]chan bool // Array of chan for sending responses

// structs
type info struct {
	id        int
	timestamp int
}

func main() {

	// init chans
	for i := range chanRequest {
		chanRequest[i] = make(chan info, 1)
	}

	for i := range chanResponse {
		chanResponse[i] = make(chan bool, 1)
	}

	// ignore prints in translation to uppaal
	fmt.Println("Nodes: ", NODES)

	// waitgroup of all nodes
	var wg sync.WaitGroup
	wg.Add(NODES)

	go func() {
		node(0, [NODES - 1]int{1, 2}, [NODES - 1]int{4, 5})
		wg.Done()
	}()
	go func() {
		node(1, [NODES - 1]int{0, 5}, [NODES - 1]int{2, 3})
		wg.Done()
	}()
	go func() {
		node(2, [NODES - 1]int{3, 4}, [NODES - 1]int{0, 1})
		wg.Done()
	}()

	wg.Wait()
}

// node that contatins the R-A alg.
func node(pid int, receiving [NODES - 1]int, sending [NODES - 1]int) {

	var inCS bool = false
	var toldWantInCS bool = false
	var wantInCS bool = false
	var wantInCsTimestamp int = -1
	var responseOnCsRequest [NODES - 1]int
	var responseOnCsCount int = 0
	//var waitingList []info // TODO LATER !

	// init responseOnCsRequest array
	for i := range responseOnCsRequest {
		responseOnCsRequest[i] = NO_ANSWER
	}

	for i := 1; i < 100; i++ {

		if !wantInCS {
			wantInCS = RandomBool() // non deterministic decision to enter cs

			if wantInCS {
				wantInCsTimestamp = timestamp
				timestamp++
				fmt.Println("PID ", pid, " wants in CS at ", wantInCsTimestamp)
			}
		}

		if inCS {
			inCS = RandomBool() // non deterministic decision to leave cs
			if !inCS {
				fmt.Println("PID ", pid, " leaving CS")
				// TODO call waiting process from waiting list
			}
		}

		// listen to request chans
		select { // chan 1
		case msg := <-chanRequest[receiving[0]]: // process wants to enter cs
			fmt.Println("PID ", pid, ": recived '", msg, "' from PID ", msg.id)
			var canEnterCS bool = nextPidForCS(pid, wantInCS, wantInCsTimestamp, msg, inCS)
			fmt.Println("PID ", pid, ": ", msg.id, " entering CS ", canEnterCS)
			chanResponse[sending[0]] <- canEnterCS
		default:
		}

		select { // chan 2
		case msg := <-chanRequest[receiving[1]]: // process wants to enter cs
			fmt.Println("PID ", pid, ": recived '", msg, "' from PID ", msg.id)
			var canEnterCS bool = nextPidForCS(pid, wantInCS, wantInCsTimestamp, msg, inCS)
			fmt.Println("PID ", pid, ": ", msg.id, " entering CS ", canEnterCS)
			chanResponse[sending[1]] <- canEnterCS
		default:
		}

		// tell other processes I want to enter cs
		if wantInCS && !toldWantInCS {
			chanRequest[sending[0]] <- info{pid, wantInCsTimestamp}
			chanRequest[sending[1]] <- info{pid, wantInCsTimestamp}
			toldWantInCS = true
		}

		if wantInCS && toldWantInCS {
			select {
			case msg := <-chanResponse[receiving[0]]: // response on wants in cs request
				fmt.Println("PID ", pid, ": recived '", msg)
				responseOnCsCount++
				if msg == true {
					responseOnCsRequest[0] = YES
				} else {
					responseOnCsRequest[0] = NO
				}
			default:
			}

			select {
			case msg := <-chanResponse[receiving[1]]: // response on wants in cs request
				fmt.Println("PID ", pid, ": recived '", msg)
				responseOnCsCount++
				if msg == true {
					responseOnCsRequest[1] = YES
				} else {
					responseOnCsRequest[1] = NO
				}
			default:
			}
		}

		if responseOnCsCount == NODES-1 {
			// enter or not

			var enterCS bool = true

			for i := range responseOnCsRequest {
				if responseOnCsRequest[i] == NO || responseOnCsRequest[i] == NO_ANSWER {
					enterCS = false
				}
			}

			if enterCS {
				inCS = true
				wantInCS = false
				fmt.Println("PID ", pid, " entering CS")
			}

			// Reset vars

			responseOnCsCount = 0

			for i := range responseOnCsRequest {
				responseOnCsRequest[i] = NO_ANSWER
			}

			toldWantInCS = false
		}

		time.Sleep(10 * time.Millisecond)

	}

	fmt.Println("PID ", pid, " terminated!")

}

// checks if another process can enter the cs
func nextPidForCS(pid int, wantInCS bool, wantInCsTimestamp int, msg info, inCS bool) (ret bool) {
	if inCS {
		return false
	}

	if !wantInCS || (wantInCS && wantInCsTimestamp > msg.timestamp) || (wantInCS && wantInCsTimestamp == msg.timestamp && pid > msg.id) {
		return true
	} else {
		return false
	}
}

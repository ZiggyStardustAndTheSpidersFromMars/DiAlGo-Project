package main

/*
Example for Bully election algorithm.
*/

import (
	"fmt"
	"sync"
	"time"
)

// Array of chan for sending heartbeat to the leader
var bool_chanHeartbeat [EDGES]chan interface{}

// Array of chan for sending election msg
var int_chanElection [EDGES]chan interface{}

// Array of chan for sending the election result msg
var int_chanElectionResult [EDGES]chan interface{}

func main() {

	Init()

	// Init chans
	for i := range bool_chanHeartbeat {
		bool_chanHeartbeat[i] = make(chan interface{}, 1)
	}

	for i := range int_chanElection {
		int_chanElection[i] = make(chan interface{}, 10)
	}

	for i := range int_chanElectionResult {
		int_chanElectionResult[i] = make(chan interface{}, 3)
	}

	fmt.Println("Nodes: ", NODES)

	// Waitgroup of all nodes
	var wg sync.WaitGroup
	wg.Add(NODES)

	go func() {
		node(0)
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

	wg.Wait()
}

// Node (can be leader or worker)
func node(pid int) {
	var isLeader bool = false
	var leaderPid int = NODES - 1

	var doingElection bool = false
	var getsElectionResponse bool = false

	var timeoutDetected bool = false

	// Init Leader -> node with highest pid
	if pid == NODES-1 {
		isLeader = true
	}

	fmt.Println("Hello from PID", pid, ". Leader is PID", leaderPid)

	// Send to Leader while working -> working ends after 10 steps
	for i := 0; i < 10; i++ {
		// Listen for new leader
		for j := 0; j < NODES; j++ {
			if j != pid {
				select {
				case msg := <-int_chanElectionResult[GetReceiveIndex(pid, j)]:
					fmt.Println("PID", pid, "received new leader msg", msg, "from PID", j, ".")
					leaderPid = msg.(int)
					isLeader = false
				default:
				}
			}
		}

		if !isLeader && !doingElection { // Sending heartbeat if not leader node to leader
			Send(pid, leaderPid, bool_chanHeartbeat, true)
			// Timeout simulation (wait 2 sec. for response)
			time.Sleep(2000 * time.Millisecond)
			select {
			case msg2 := <-bool_chanHeartbeat[GetReceiveIndex(pid, leaderPid)]:
				fmt.Println("PID", pid, "received heartbeat response", msg2, "from PID", leaderPid, ".")
				timeoutDetected = false
			default:
				timeoutDetected = true
			}

			if timeoutDetected { // Start election
				doingElection = true
				for n := pid + 1; n < NODES; n++ {
					Send(pid, n, int_chanElection, pid)
					getsElectionResponse = false
				}
			}
		} else if isLeader { // Listen for heartbeats if node is leader
			fmt.Println("Leader", pid, "answering heartbeats.")
			for j := 0; j < NODES; j++ {
				if j != pid {
					select {
					case msg2 := <-bool_chanHeartbeat[GetReceiveIndex(pid, j)]:
						fmt.Println("PID", pid, "received received heartbeat", msg2, "from PID", j, ".")
						Send(pid, j, bool_chanHeartbeat, true) // answer
					default:
					}
				}
			}
		}

		// Listen for election
		for j := 0; j < pid; j++ {
			select {
			case msg := <-int_chanElection[GetReceiveIndex(pid, j)]:
				fmt.Println("PID", pid, "received election msg", msg, "from PID", j, ".")
				if pid > j {
					Send(pid, j, int_chanElection, pid) // answer with pid if my pid is greater
					doingElection = true
					for n := pid + 1; n < NODES; n++ {
						Send(pid, n, int_chanElection, pid)
						getsElectionResponse = false
					}
				}
			default:
			}
		}

		if doingElection {
			for k := pid + 1; k < NODES; k++ {
				// Timeout simulation (wait 2 sec. for response)
				time.Sleep(2000 * time.Millisecond)
				select {
				case msg := <-int_chanElection[GetReceiveIndex(pid, k)]:
					fmt.Println("PID", pid, "received election response", msg, "from PID", k, ".")
					getsElectionResponse = true
					leaderPid = k
				default:
				}
			}

			// Tell all that there is a new leader
			if !getsElectionResponse {
				isLeader = true
				leaderPid = pid
				Broadcast(pid, int_chanElectionResult, pid)
			}
			doingElection = false
		}

		// wait after every step
		time.Sleep(500 * time.Millisecond)

	}

	fmt.Println("PID", pid, "terminated!")

}

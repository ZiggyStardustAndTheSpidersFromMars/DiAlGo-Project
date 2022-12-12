package main

/*
Example for Suzuki-Kasami token algorithm for mutal exclusion.
*/

import (
	"fmt"
	"sync"
)

// Channels
var bool_chanRequest [EDGES]chan interface{} // Array of chan for requesting cs
var int_chanReply [EDGES]chan interface{}    // Array of chan for giving access to cs and sending the request queue

func main() {

	Init()

	// Init chans
	for i := range bool_chanRequest {
		bool_chanRequest[i] = make(chan interface{}, 1)
	}

	for i := range int_chanReply {
		int_chanReply[i] = make(chan interface{})
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
	go func() {
		node(3)
		wg.Done()
	}()

	wg.Wait()
}

// Node (all nodes want to enter cs ones)
func node(pid int) {
	var hasToken bool = false
	var wasInCs bool = false
	var queue int = 0
	var tempQueue int = 0
	var queueSize int = 0
	var pidIsInQueue bool = false

	// Init Token on PID 0
	if pid == 0 {
		hasToken = true
	}

	// Work 5 Steps
	for i := 0; i < 5; i++ {

		if !hasToken {
			// Send Broadcast to request CS
			if !wasInCs {
				Broadcast(pid, bool_chanRequest, true)

				// Wait for Token
				for !hasToken {
					for i := 0; i < NODES; i++ {
						if i != pid {
							select {
							case replyMsg := <-int_chanReply[GetReceiveIndex(pid, i)]:
								fmt.Println("PID", pid, "received Token with Queue", replyMsg, "from PID", i)
								hasToken = true
								queue = replyMsg.(int)
							default:
							}
						}
					}
				}

			}

			for i := 0; i < NODES; i++ {
				if i != pid {
					select {
					case requestMsg := <-bool_chanRequest[GetReceiveIndex(pid, i)]:
						fmt.Println("PID", pid, "received Request", requestMsg, "from PID", i)
						tempQueue = queue
						queueSize = 0
						pidIsInQueue = false
						for tempQueue%10 != 0 {
							queueSize++
							if i+1 == tempQueue%10 {
								pidIsInQueue = true
							}
							tempQueue = tempQueue / 10
						}
						if !pidIsInQueue {
							if queueSize == 0 {
								queue += i + 1
							} else if queueSize == 1 {
								queue += (i + 1) * 10
							} else if queueSize == 2 {
								queue += (i + 1) * 100
							} else {
								queue += (i + 1) * 1000
							}
						}
					default:
					}
				}
			}

		} else {
			// Entering CS
			fmt.Println("PID", pid, "entering CS.")
			// Leaving CS
			fmt.Println("PID", pid, "leaving CS.")

			for hasToken {
				// Receiving Broadcast
				for i := 0; i < NODES; i++ {
					if i != pid {
						select {
						case requestMsg := <-bool_chanRequest[GetReceiveIndex(pid, i)]:
							fmt.Println("PID", pid, "received Request", requestMsg, "from PID", i)
							// Store in Queue
							tempQueue = queue
							queueSize = 0
							pidIsInQueue = false
							for tempQueue%10 != 0 {
								queueSize++
								if i+1 == tempQueue%10 {
									pidIsInQueue = true
								}
								tempQueue = tempQueue / 10
							}
							if !pidIsInQueue {
								if queueSize == 0 {
									queue += i + 1
								} else if queueSize == 1 {
									queue += (i + 1) * 10
								} else if queueSize == 2 {
									queue += (i + 1) * 100
								} else {
									queue += (i + 1) * 1000
								}
							}
						default:
						}
					}
				}
				// Send Token to First in Queue
				if queue != 0 {
					// Release Token
					hasToken = false
					wasInCs = true
					Send(pid, (queue%10)-1, int_chanReply, queue/10)
					queue = queue / 10
				}
			}
		}

	}

	fmt.Println("PID", pid, "terminated! I was in CS =", wasInCs)

}

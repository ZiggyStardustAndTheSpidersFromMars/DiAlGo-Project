package main

/*
* Framework DiAlGo (Distributed Algorithms with Go).
* For generating and simulating distributed algorithms.
* Author : Torben Friedrich Goerner
 */

import (
	"math/rand"
	"time"
)

const NODES int = 2 // amount of nodes

const EDGES int = 2 // amoount of edges (n*(n-1)/2)*2 --> n*(n-1)

const (
	TRUE  = 1
	FALSE = 0
)

// init function
func Init() {
	rand.Seed(time.Now().UnixNano())
}

// returns a random bool
func RandomBool() bool {
	ran := rand.Intn(TRUE + 1)
	if ran == TRUE {
		return true
	}
	return false
}

// returns a random int in range min - max (range limits are inclusive)
func RandomInt(min int, max int) (i int) {
	i = rand.Intn(max-min+1) + min
	return
}

// returns a random int in range min - max (range limits are inclusive) without a value 'myPid'
func RandomOtherPid(min int, max int, myPid int) (i int) {
	i = rand.Intn(max-min+1) + min
	if i == myPid {
		i = min
	}
	return
}

// gets the index of a recieving channel were a listens on b
func GetReceiveIndex(pidA int, pidB int) (index int) {
	var in int = 0

	if pidA > pidB {
		in = pidA - 1
	} else {
		in = pidA
	}

	index = ((NODES - 1) * pidB) + in
	return
}

// sends a msg from pid A to pid B via a channel
func Send(pidA int, pidB int, chans [EDGES]chan interface{}, msg interface{}) {
	var in int = 0

	if pidB > pidA {
		in = pidB - 1
	} else {
		in = pidB
	}

	var index int = ((NODES - 1) * pidA) + in

	chans[index] <- msg
}

// sends a msg from pid A to pid B via a channel (The msg could be lost)
func SendLossy(pidA int, pidB int, chans [EDGES]chan interface{}, msg interface{}) {
	var send bool = RandomBool()

	if send {
		Send(pidA, pidB, chans, msg)
	}

}

// broadcasts a msg from pid to all other nodes
func Broadcast(pid int, chans [EDGES]chan interface{}, msg interface{}) {
	for i := 0; i < NODES; i++ {
		if pid != i {
			Send(pid, i, chans, msg)
		}
	}
}

// broadcasts a msg lossy from pid to all other nodes
func BroadcastLossy(pid int, chans [EDGES]chan interface{}, msg interface{}) {
	for i := 0; i < NODES; i++ {
		if pid != i {
			SendLossy(pid, i, chans, msg)
		}
	}
}

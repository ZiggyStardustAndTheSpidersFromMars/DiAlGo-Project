package main

import (
	"fmt"
	"sync"
)

func main() {
	var nodes int = 4

	chA := make(chan int, 1) // Chan auf dem Knoten melden dass sie rdy sind
	chB := make(chan int, 1)
	chC := make(chan int, 1)

	ch2A := make(chan int, 1) // Chan auf dem Master sagt das Worker Knoten wieder arbeiten können
	ch2B := make(chan int, 1)
	ch2C := make(chan int, 1)

	fmt.Println("Nodes: ", nodes)

	var wg sync.WaitGroup
	wg.Add(nodes)

	// Aufruf einer Go Routine über anonyme Funkt.
	// Waitgroup definiert die Knoten (Go Routine = Knoten)
	go func() {
		worker(0, chA, ch2A, 2)
		wg.Done()
	}()
	go func() {
		worker(1, chB, ch2B, 5)
		wg.Done()
	}()
	go func() {
		worker(2, chC, ch2C, 10)
		wg.Done()
	}()
	go func() {
		master(chA, chB, chC, ch2A, ch2B, ch2C)
		wg.Done()
	}()

	wg.Wait()
}

func worker(id int, ch chan int, ch2 chan int, workingTime int) {
	var life int = 1000
	var working bool = true
	var workingState = 0

	for i := 1; i < life; i++ { // endlosschleife eines Knoten -> Logik des Knotens in "main" Loop
		if !working {
			select {
			case msg := <-ch2:
				working = true
				fmt.Println("Local time ", i, " Node ", msg, " working...")
			default:
				// warten bis man wieder starten kann -> sprich wenn alle rdy sind
			}

		} else {
			workingState++
		}

		if workingState == workingTime {
			working = false
			workingState = 0
			fmt.Println("Node ", id, " done at local time ", i)
			ch <- id // Melden das er fertig ist
		}

		//time.Sleep(1 * time.Millisecond) //wait
	}
}

func master(chA chan int, chB chan int, chC chan int, ch2A chan int, ch2B chan int, ch2C chan int) {
	var life int = 1000
	var w0rdy bool = false
	var w1rdy bool = false
	var w2rdy bool = false
	var jobsDone int = 0

	for i := 0; i < life; i++ { // endlosschleife eines Knoten -> Logik des Knotens in "main" Loop
		select {
		case msg := <-chA:
			w0rdy = true
			fmt.Println(msg)
		case msg := <-chB:
			w1rdy = true
			fmt.Println(msg)
		case msg := <-chC:
			w2rdy = true
			fmt.Println(msg)
		default:
		}

		if w0rdy && w1rdy && w2rdy {
			w0rdy = false
			w1rdy = false
			w2rdy = false
			ch2A <- 0
			ch2B <- 1
			ch2C <- 2
			jobsDone++
			fmt.Println(jobsDone, "Jobs done...")
		}

		//time.Sleep(1 * time.Millisecond) //wait
	}

	fmt.Println(jobsDone, "Jobs done!")
}

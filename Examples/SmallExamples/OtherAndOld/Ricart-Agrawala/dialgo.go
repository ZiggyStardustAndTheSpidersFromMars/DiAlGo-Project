package main

import (
	"math/rand"
	"time"
)

const (
	TRUE  = 1
	FALSE = 0
)

func RandomBool() bool {
	rand.Seed(time.Now().UnixNano())
	ran := rand.Intn(TRUE + 1)
	if ran == TRUE {
		return true
	}
	return false
}

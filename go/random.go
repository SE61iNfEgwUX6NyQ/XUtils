package main

import (
	"log"
	"math/rand"
	"time"
)

var chars = []rune{}

func init() {
	for i := 48; i < 58; i++ {
		chars = append(chars, rune(i))
	}
	for i := 65; i < 91; i++ {
		chars = append(chars, rune(i))
	}
	for i := 97; i < 123; i++ {
		chars = append(chars, rune(i))
	}
}

func main() {
	var length = 16
	var result = ""

	rand.Seed(time.Now().UnixNano())

	for i := 0; i < length; i++ {
		index := rand.Intn(62)
		result += string(chars[index])
	}

	log.Println(result)
}

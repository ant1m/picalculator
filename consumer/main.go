package main

import (
	"encoding/json"
	"fmt"
	"log"
	"math/rand"
	"strconv"
	"time"

	"github.com/streadway/amqp"
)

const (
	QueueExchange string = "resultsExchange"
	BrokerHost    string = "amqp://guest:guest@localhost"
	PointsQueue   string = "points"
	ResultsQueue  string = "results"
)

var name string = randIntString()

func randIntString() string {
	rand.Seed(time.Now().UnixNano())
	return strconv.Itoa(rand.Int())
}

type point struct {
	X float64
	Y float64
}

func (p point) string() string {
	return fmt.Sprint("Point : x: %f, y: %f", p.X, p.Y)
}

func connect() (*amqp.Connection, *amqp.Channel, error) {
	connection, err := amqp.Dial(BrokerHost)
	if err != nil {
		fmt.Println("Something went wrong while connecting to queue", err)
		return nil, nil, err
	}
	channel, err := connection.Channel()
	if err != nil {
		return connection, nil, err
		fmt.Println("Something went wrong while opening channel to queue", err)
	}
	return connection, channel, nil
}

func closeConn(connection *amqp.Connection, channel *amqp.Channel) {
	if connection != nil {
		connection.Close()
	}
	if channel != nil {
		channel.Close()
	}
}

func main() {
	senderChan := make(chan bool)
	go sender(senderChan)

	connection, channel, err := connect()
	defer closeConn(connection, channel)
	if err != nil {
		panic("J'en vais comme un prince.")
	}
	consumer, err := channel.Consume(PointsQueue, name, true, false, true, true, nil)
	if err != nil {
		fmt.Println("Something went wrong while binding to queue", err)
	}
	fmt.Println(name, ": Waiting for messages…")
	for {
		d := <-consumer
		var p point
		json.Unmarshal(d.Body, &p)
		fmt.Println("Processing ", p)
		senderChan <- p.X*p.X+p.Y*p.Y < 1
	}
	fmt.Println("Done, exiting.")
}

func sender(c chan bool) {
	fmt.Println("Ready to send…")
	connection, channel, err := connect()
	defer closeConn(connection, channel)
	if err != nil {
		panic("J'en vais comme un prince.")
	}
	err = channel.ExchangeDeclare(QueueExchange, "fanout", true, false, false, true, nil)
	if err != nil {
		log.Fatalf("exchange.declare: %v", err)
	}
	for {
		r := <-c
		rand.Seed(time.Now().UnixNano())
		fmt.Println("Sending ")
		msg := amqp.Publishing{
			DeliveryMode: amqp.Persistent,
			Timestamp:    time.Now(),
			ContentType:  "text/plain",
			Body:         []byte(fmt.Sprintf("%t", r))}
		err := channel.Publish(QueueExchange, "", false, false, msg)
		if err != nil {
			log.Fatalf("basic.publish: %v", err)
		}
	}
}

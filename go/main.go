package main

import (
	"fmt"
	"net/smtp"
	"strings"
)

func main() {
	user := "remainer@163.com"
	password := "a74123"
	host := "smtp.163.com:25"
	to := "remainer@163.com"

	subject := "使用Golang发送邮件"
	body := `
			<html>
			<body>
			<h3>
			"Test send to email"
			</h3>
			</body>
			</html>
			`
	fmt.Print("send Email")
	err := SendMail(user, password, host, to, subject, body, "")
	if err != nil {
		fmt.Println("Send mail error")
		fmt.Println(err)
	} else {
		fmt.Println("send mail success")
	}
}

func SendMail(user, password, host, to, sbject, body, mailtype string) error {
	hp := strings.Split(host, ":")
	auth := smtp.PlainAuth("", user, password, hp[0])
	var content_type string
	if mailtype == "html" {
		content_type = "Content-Type: text/" + mailtype + "; charset=UTF-8"
	} else {
		content_type = "Content-Type: text/plain" + mailtype + "; charset=UTF-8"
	}
	msg := []byte("To: " + to + "\r\nFrom: " + user + ">\r\nSubject: " + "\r\n" + content_type + "\r\n\r\n" + body)
	send_to := strings.Split(to, ";")
	err := smtp.SendMail(host, auth, user, send_to, msg)
	return err
}

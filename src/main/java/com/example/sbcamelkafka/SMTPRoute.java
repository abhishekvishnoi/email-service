package com.example.sbcamelkafka;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.attachment.AttachmentMessage;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import java.io.File;
import java.io.InputStream;

@Component
public class SMTPRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("kafka:{{topic}}?brokers={{broker}}")
                .log("Message received from Kafka : ${body} on the topic ${headers[kafka.TOPIC]}")
                .setHeader("From").jsonpath("$.message.fromEmail")
                .setHeader("To").jsonpath("$.message.toEmail")
                .setHeader("Subject").jsonpath("$.message.subject")
                .setBody(jsonpath("$.message.body"))
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        AttachmentMessage attMsg = exchange.getIn(AttachmentMessage.class);

                        Resource resource = new ClassPathResource("sample.ics");
                        InputStream input = resource.getInputStream();
                        File file = resource.getFile();

                       // File file = new File("classpath:sample.ics");

                      //  File file = new File("classpath:sample.ics");

                        attMsg.addAttachment("message1.xml",
                                new DataHandler(new FileDataSource(file)));

                        exchange.getIn().getBody();
                    }
                })
                .to("smtp://smtp.freesmtpservers.com:25");
        
    }
}

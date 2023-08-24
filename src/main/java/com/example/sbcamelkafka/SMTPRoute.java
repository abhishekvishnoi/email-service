package com.example.sbcamelkafka;

import com.sun.istack.ByteArrayDataSource;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.attachment.AttachmentMessage;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import java.io.File;
import java.io.InputStream;

@Component
public class SMTPRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        Processor processor = new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {

              String content =  exchange.getIn().getHeader("ics" , String.class);

                AttachmentMessage attMsg = exchange.getIn(AttachmentMessage.class);

              //  String content = "This is the content of the string.";

                // Convert the string to a byte array
                byte[] byteArray = content.getBytes();

                // Create a DataSource from the byte array
                DataSource dataSource = new ByteArrayDataSource(byteArray, "text/plain");

                // Create a DataHandler from the DataSource
                DataHandler dataHandler = new DataHandler(dataSource);


                //InputStream input = getClass().getResourceAsStream("/sample.ics");
                Resource resource = new ClassPathResource("sample.ics");
                File file = resource.getFile();
                attMsg.addAttachment("meeting-invite",
                        new DataHandler(dataSource));



             //   DataSource ds = new
              //  exchange.getIn().getBody();
            }
        };

        from("kafka:{{topic}}?brokers={{broker}}")
                .log("Message received from Kafka : ${body} on the topic ${headers[kafka.TOPIC]}")
               // .log("{{mail.ics}}")
                .setHeader("ics").simple("{{mail.ics}}")
                .setHeader("From").jsonpath("$.message.fromEmail")
                .setHeader("To").jsonpath("$.message.toEmail")
                .setHeader("Subject").jsonpath("$.message.subject")
                .setBody(jsonpath("$.message.body"))
                .process(processor)
                .to("smtp://smtp.freesmtpservers.com:25");
        
    }
}

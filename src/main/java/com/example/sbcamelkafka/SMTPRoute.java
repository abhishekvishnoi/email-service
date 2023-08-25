package com.example.sbcamelkafka;

import com.sun.istack.ByteArrayDataSource;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.attachment.AttachmentMessage;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
import java.util.Base64;

@Component
public class SMTPRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        Processor processor = new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {

                exchange.getIn().setBody(new String(Base64.getDecoder()
                        .decode(exchange.getIn().getBody(String.class))));

                String attchementContent =  exchange.getIn().getHeader("ics" , String.class);

                attchementContent = String.format(attchementContent ,
                        exchange.getIn().getHeader("meetingTimeFrom" , String.class) ,
                        exchange.getIn().getHeader("meetingTimeTo" , String.class) ,
                        exchange.getIn().getHeader("meetingLocation" , String.class) ,
                        exchange.getIn().getHeader("meetingLocation" , String.class));

                AttachmentMessage attMsg = exchange.getIn(AttachmentMessage.class);

                attMsg.addAttachment("meeting-invite",
                        new DataHandler( new ByteArrayDataSource(attchementContent.getBytes(),
                                "text/plain")));


            }
        };

        from("kafka:{{topic}}?brokers={{broker}}")
                .log("Email Message received from Kafka : on the topic ${headers[kafka.TOPIC]}")
                .setHeader("ics").simple("{{mail.ics}}")
                .setHeader("From").jsonpath("$.message.fromEmail")
                .setHeader("meetingTimeFrom").jsonpath("$.meetingTimeFrom")
                .setHeader("meetingTimeTo").jsonpath("$.meetingTimeTo")
                .setHeader("meetingLocation").jsonpath("$.meetingLocation")
                .setHeader("From").jsonpath("$.message.fromEmail")
                .setHeader("To").jsonpath("$.message.toEmail")
                .setHeader("Cc").jsonpath("$.message.ccEmail")
                .setHeader("Subject").jsonpath("$.message.subject")
                .setBody(jsonpath("$.message.body"))
                .process(processor)
                .log("Sending Email Message to ${headers[To]} cc to ${headers[Cc]}")
                .to("smtp://{{smtp.server}}:{{smtp.port}}?contentType=text/html");
        
    }
}

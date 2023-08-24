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
import java.util.Base64;

@Component
public class SMTPRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        Processor processor = new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                //20130802T103400
                //yyyymmddTHHmmss


                byte[] decodedBytes = Base64.getDecoder().decode(exchange.getIn().getBody(String.class));
                String decodedHTMLbody = new String(decodedBytes);
                exchange.getIn().setBody(decodedHTMLbody);

                String content =  exchange.getIn().getHeader("ics" , String.class);
                String meetingTimeFrom =  exchange.getIn().getHeader("meetingTimeFrom" , String.class);
                String meetingTimeTo =  exchange.getIn().getHeader("meetingTimeTo" , String.class);
                String meetingLocation =  exchange.getIn().getHeader("meetingLocation" , String.class);

                String.format(content ,meetingTimeFrom ,meetingTimeTo , meetingLocation , meetingLocation);

                AttachmentMessage attMsg = exchange.getIn(AttachmentMessage.class);
                byte[] byteArray = content.getBytes();
                DataSource dataSource = new ByteArrayDataSource(byteArray, "text/plain");
                Resource resource = new ClassPathResource("sample.ics");
                attMsg.addAttachment("meeting-invite",
                        new DataHandler(dataSource));


            }
        };

        from("kafka:{{topic}}?brokers={{broker}}")
                .log("Message received from Kafka : ${body} on the topic ${headers[kafka.TOPIC]}")
                .setHeader("ics").simple("{{mail.ics}}")
                .setHeader("From").jsonpath("$.message.fromEmail")
                .setHeader("meetingTimeFrom").jsonpath("$.meetingTimeFrom")
                .setHeader("meetingTimeTo").jsonpath("$.meetingTimeTo")
                .setHeader("meetingLocation").jsonpath("$.meetingLocation")

                .setHeader("From").jsonpath("$.message.fromEmail")
                .setHeader("To").jsonpath("$.message.toEmail")
                .setHeader("Subject").jsonpath("$.message.subject")
                .setBody(jsonpath("$.message.body"))
                .process(processor)
                .to("smtp://{{smtp.server}}:{{smtp.port}}");
        
    }
}

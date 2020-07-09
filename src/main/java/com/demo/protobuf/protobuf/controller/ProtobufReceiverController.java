package com.demo.protobuf.protobuf.controller;

import com.demo.protobuf.protobuf.bean.PersonProtobuf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/demo/protobuf")
public class ProtobufReceiverController
{

    @PostMapping("/receive")
    public void receiveProtobuf(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            PersonProtobuf.Person person = PersonProtobuf.Person.parseFrom(request.getInputStream());
            PersonProtobuf.Person.Builder personBuilder = PersonProtobuf.Person.newBuilder(person);
            personBuilder.setId(2);
            personBuilder.setName("tiger");
            personBuilder.setEmail("yy@yy.com");
            personBuilder.addPhone(PersonProtobuf.Person.PhoneNumber.newBuilder().setNumber("0987654321").setType(PersonProtobuf.Person.PhoneType.HOME));
            personBuilder.build().writeTo(response.getOutputStream());
            System.out.println(person);
        } catch (IOException e)
        {
            log.error(e.getMessage());
        }
    }
}

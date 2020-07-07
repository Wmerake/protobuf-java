package com.demo.protobuf.protobuf.client;

import com.demo.protobuf.protobuf.bean.PersonProtobuf;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class ProtobufClient {
    public static void main(String[] args) throws IOException {
        PersonProtobuf.Person.Builder personRequest = PersonProtobuf.Person.newBuilder();
        personRequest.setId(1);
        personRequest.setName("jesse");
        personRequest.setEmail("xx@xx.com");
        personRequest.addPhone(PersonProtobuf.Person.PhoneNumber.newBuilder().setNumber("1234567890").setType(PersonProtobuf.Person.PhoneType.HOME));

        //使用java原生URL连接代码生成请求并获得返回值打印
        URL url = new URL("http://localhost:8080/demo/protobuf/receive");
        URLConnection connection = url.openConnection();
        connection.setDoOutput(true);
        personRequest.build().writeTo(connection.getOutputStream());

        PersonProtobuf.Person.parseFrom(connection.getInputStream());
//        System.out.println(personResponse.getId());
//        System.out.println(personResponse.getName());
//        System.out.println(personResponse.getEmail());
//        System.out.println(personResponse.getPhone(0));
//        System.out.println(personResponse.getPhone(1));
    }

}

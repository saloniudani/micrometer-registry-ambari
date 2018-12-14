package com.example.metrics.micrometerregistryambari.controller;


import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

@Slf4j
@RestController
public class SampleController {

    @Autowired
    MeterRegistry meterRegistry;

//    Counter counter;
//
//    @PostConstruct
//    public void setUp(){
//        counter = meterRegistry.counter("app.requests");
//    }

    @Timed(value = "app.requests.timer")
    @RequestMapping(value = {
            "/hit"}, method = RequestMethod.GET)
    public String getName() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Counter counter = meterRegistry.counter("app.requests");
        counter.increment();
        return "You hit me "+counter.count()+" times from last reporting interval.";
    }
}

package com.example.metrics.micrometerregistryambari.controller;


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

    Counter counter;

    @PostConstruct
    public void setUp(){
        counter = meterRegistry.counter("app.requests");
    }

    @RequestMapping(value = {
            "/hit"}, method = RequestMethod.GET)
    public String getName() {
        counter.increment();
        return "You hit me "+counter.count()+" times from last reporting interval.";
    }
}

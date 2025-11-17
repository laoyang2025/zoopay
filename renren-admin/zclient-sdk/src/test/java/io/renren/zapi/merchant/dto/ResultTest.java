package io.renren.zapi.merchant.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
    }

    @Test
    void testToString() {
        Result<String> x = new Result<>();
        x.setData("hello");
        System.out.println(x);
    }
}
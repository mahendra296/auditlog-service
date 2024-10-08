package com.audit.controller;

import com.audit.annotation.Audited;
import com.audit.dto.CustomerDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TestController {

    @GetMapping("/hello")
    @Audited(
            index = 0,
            shouldStoreAll = true,
            activity = "get_hello"

    )
    public String hello() {
        return "Hello";
    }


    @PostMapping("/save")
    @Audited(
            index = 0,
            shouldStoreAll = true,
            fieldsToAudit = {"customerId", "name", "test.customerNumber"},
            activity = "save_customer"
    )
    public ResponseEntity<CustomerDTO> saveCustomer(@RequestBody CustomerDTO customerDTO) {
        return ResponseEntity.ok().body(customerDTO);
    }
}

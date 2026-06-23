package com.example.property_management.controller;

import com.example.property_management.dto.CalculatorDTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/calculator")
public class CalculatorController {
    @GetMapping("/add/{num3}")
    public Double add(@RequestParam("num1") Double a,@RequestParam("num2") Double b,
                      @PathVariable("num3") double c) {
        return a + b;
    }
    @GetMapping("/sub/{num1}/{num2}")
    public Double sub(@PathVariable ("num1") Double a,@PathVariable("num2") Double b) {
        if (a > b){
            return a - b;
        }else {return b - a;}
    }
    @PostMapping("/mul")
    public Double multiply(@RequestBody CalculatorDTO calculatorDTO) {
        return calculatorDTO.getNum1() + calculatorDTO.getNum2() + calculatorDTO.getNum3() +  calculatorDTO.getNum4();
    }
}

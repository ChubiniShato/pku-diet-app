package com.chubini.pku.products;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

  @GetMapping("/georgian")
  public String testGeorgianText() {
    return "გამარჯობა! ეს არის ქართული ტექსტი. Hello! This is Georgian text.";
  }

  @GetMapping("/encoding")
  public String testEncoding() {
    return "UTF-8 Encoding Test: ა ბ გ დ ე ვ ზ თ ი კ ლ მ ნ ო პ ჟ რ ს ტ უ ფ ქ ღ ყ შ ჩ ც ძ წ ჭ ხ ჯ ჰ";
  }
}

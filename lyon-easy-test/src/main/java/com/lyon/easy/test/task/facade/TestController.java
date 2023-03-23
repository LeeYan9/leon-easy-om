package com.lyon.easy.test.task.facade;

import com.lyon.easy.common.base.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Lyon
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/isAlive")
    public R<String> isAlive(){
        return R.success("OK");
    }

}

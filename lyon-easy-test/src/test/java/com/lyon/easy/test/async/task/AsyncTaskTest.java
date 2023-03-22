package com.lyon.easy.test.async.task;

/**
 * @author Lyon
 */

import com.lyon.easy.test.async.task.config.AsyncTaskApp;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = AsyncTaskApp.class)
@ActiveProfiles("async-task-unit")
public class AsyncTaskTest {


}

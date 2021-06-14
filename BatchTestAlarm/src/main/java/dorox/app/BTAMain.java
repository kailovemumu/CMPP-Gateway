package dorox.app;

import org.n3r.idworker.Sid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Order(value=1)
public class BTAMain implements CommandLineRunner{
//	private static final Logger logger = LoggerFactory.getLogger(BTAMain.class);

    @Value("${messageid.prefix}")
    private String messageIdPrefix;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private Sid sid;

    public static ScheduledExecutorService service1 = Executors.newSingleThreadScheduledExecutor();
    public static ScheduledExecutorService service2 = Executors.newSingleThreadScheduledExecutor();


    @Override
    public void run(String... args) throws Exception {

        service1.scheduleAtFixedRate(
                new AlarmAndTest(jdbcTemplate, rabbitTemplate, sid, messageIdPrefix), 0, 5, TimeUnit.SECONDS);

        service2.scheduleAtFixedRate(
                new BatchSend(jdbcTemplate, rabbitTemplate, sid, messageIdPrefix), 0, 30, TimeUnit.SECONDS);

    }

}


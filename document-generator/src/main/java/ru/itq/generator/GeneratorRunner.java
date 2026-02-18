package ru.itq.generator;

import org.slf4j.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.itq.generator.client.DocumentClient;
import ru.itq.generator.config.GeneratorProperties;

@Component
public class GeneratorRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(GeneratorRunner.class);

    private final GeneratorProperties props;
    private final DocumentClient client;

    public GeneratorRunner(GeneratorProperties props, DocumentClient client) {
        this.props = props;
        this.client = client;
    }

    @Override
    public void run(String... args) {
        int n = props.getN();
        String url = props.getServiceUrl();

        log.info("Generator started: N={}, serviceUrl={}", n, url);

        long start = System.currentTimeMillis();
        for (int i = 1; i <= n; i++) {
            client.create(url, "generator", "Document #" + i);

            if (i % 50 == 0 || i == n) {
                log.info("Progress: created {}/{}", i, n);
            }
        }

        long ms = System.currentTimeMillis() - start;
        log.info("Generator finished: created={}, durationMs={}", n, ms);

        System.exit(0);
    }
}

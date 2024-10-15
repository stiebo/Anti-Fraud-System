package antifraud.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = DefaultConfiguration.class)
public class DefaultConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void shouldLoadDefaultMaxAllowedBean() {
        Long defaultMaxAllowed = applicationContext.getBean("defaultMaxAllowed", Long.class);
        assertThat(defaultMaxAllowed).isEqualTo(200L);
    }

    @Test
    public void shouldLoadDefaultMaxManualBean() {
        Long defaultMaxManual = applicationContext.getBean("defaultMaxManual", Long.class);
        assertThat(defaultMaxManual).isEqualTo(1500L);
    }
}
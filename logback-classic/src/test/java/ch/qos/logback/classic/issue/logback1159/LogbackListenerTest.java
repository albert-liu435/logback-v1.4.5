package ch.qos.logback.classic.issue.logback1159;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collections;
import java.util.Set;

//import org.apache.commons.io.FileUtils;
//import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.LoggerFactoryFriend;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class LogbackListenerTest {
    private File logFile = new File("target/test.log");

    private void doConfigure() throws JoranException {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        configurator.doConfigure(new File("src/test/input/issue/logback-1159.xml"));
    }

    @AfterEach
    public void after() {
        logFile.delete();
        LoggerFactoryFriend.reset();
    }

    private void disableLogFileAccess() throws IOException {
        logFile.createNewFile();
        logFile.deleteOnExit();
        Path path = Paths.get(logFile.toURI());
        Set<PosixFilePermission> permissions = Collections.emptySet();
        try {
            Files.setPosixFilePermissions(path, permissions);
        } catch (UnsupportedOperationException e) {
            path.toFile().setReadOnly();
        }
    }

    @Test
    public void testThatErrorIsDetectedAtLogInit() throws Exception {
        Assertions.assertThrows(LoggingError.class, () -> {
            disableLogFileAccess();
            doConfigure();
        });
    }

    @Test
    public void assertThatNonFailSafeAppendersNotAffected() throws JoranException {
        doConfigure();
        Logger logger = LoggerFactory.getLogger("NOTJOURNAL");
        logger.error("This should not fail");
    }

}
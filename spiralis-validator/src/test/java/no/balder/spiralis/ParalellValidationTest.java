package no.balder.spiralis;

import no.difi.vefa.validator.Validator;
import no.difi.vefa.validator.ValidatorBuilder;
import no.difi.vefa.validator.api.Validation;
import no.difi.vefa.validator.api.ValidatorException;
import org.testng.annotations.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 28.11.2016
 *         Time: 16.43
 */
public class ParalellValidationTest {

    final static int FILE_COUNT = 50;

    final CountDownLatch countDownLatch = new CountDownLatch(1);
    final CountDownLatch finishedLatch = new CountDownLatch(FILE_COUNT);

    List<File> files = new ArrayList<>();
    private Validator validator;

    @BeforeClass
    public void setUp() throws IOException {
        try {
            validator = ValidatorBuilder.newValidator().build();
        } catch (ValidatorException e) {
            throw new IllegalStateException(e);
        }

        assertNotNull(validator);
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("hafslund-test-1.xml");
        resourceAsStream.mark(Integer.MAX_VALUE);
        assertNotNull(resourceAsStream);

        for (int i = 0; i < FILE_COUNT; i++) {
            Path tempFile = Files.createTempFile("TEST", ".xml");
            long copy = Files.copy(resourceAsStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            files.add(tempFile.toFile());
            resourceAsStream.reset();
        }
    }

    @AfterClass
    public void tearDown() {
        for (File file : files) {
            file.delete();
            System.out.println("Deleting " + file.toString());
        }

        validator.close();
    }

    @Test
    public void performValidationInParalell() throws InterruptedException, FileNotFoundException {

        List<Future<Validation>> tasks = new ArrayList<>();

        ExecutorService executorService = Executors.newFixedThreadPool(3);

        int idNumber = 0;
        for (File file : files) {
            FileInputStream fileInputStream = new FileInputStream(file);
            Future<Validation> submittedTask = executorService.submit(new ValidationWorker(idNumber, fileInputStream));
            tasks.add(submittedTask);
            idNumber++;
        }

        long start = System.nanoTime();
        System.out.println("Starting at " + new Date());
        countDownLatch.countDown(); // Start

        finishedLatch.await();

        long duration = System.nanoTime() - start;

        System.out.println(TimeUnit.MILLISECONDS.convert(duration, TimeUnit.NANOSECONDS) + "ms");
    }

    class ValidationWorker implements Callable<Validation> {

        private final int no;
        private final FileInputStream file;

        ValidationWorker(int no, FileInputStream file) {
            this.no = no;
            this.file = file;
        }

        @Override
        public Validation call() throws Exception {
            countDownLatch.await();

            assertNotNull("Starting no " + no);
            Validation validation = validator.validate(file);
            finishedLatch.countDown();

            return validation;
        }
    }

}

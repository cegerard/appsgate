/**
 * 
 */
package appsgate.lig.mail.javamail;

import org.junit.*;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import java.util.Arrays;
import java.util.List;

/**
 * @author thibaud
 *
 */
public class MailServiceTest {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(MailServiceTest.class);


    /**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
    @Ignore //Disabled because with too much testing on the same account, gmail suspect hacking
	public void test() throws Exception {
        logger.debug("Begin test");
        MailServiceImpl mailer = new MailServiceImpl();
//        mailer.setAccount("smarthome.inria@gmail.com","smarthome2014");

        logger.debug("Mailer creation OK");

  //      mailer.start();
        logger.debug("Mailer started OK");

        for (Message message:mailer.getMails(5)) {

            List froms = Arrays.asList(message.getFrom());
            logger.debug("Start Message from :" + froms);
            logger.debug("\t> Subject :" + message.getSubject());
            logger.debug(String.format("\t> Sent: %1$te/%1$tm/%1$tY %1$tH:%1$tM:%1$tS",message.getSentDate()));

        }

	}

}

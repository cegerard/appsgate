import java.io.File;

import org.junit.Test;

import appsgate.lig.media.player.jlayer.PlayerWrapperThread;
import junit.framework.TestCase;


public class PlayerTest extends TestCase {

	protected static void setUpBeforeClass() throws Exception {
	}

	protected static void tearDownAfterClass() throws Exception {
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}


	@Test
	public void testPlay() {
		PlayerWrapperThread player = new PlayerWrapperThread();
		File f = new File("src/test/resources");
		
		player.configure("file://"
				+f.getAbsolutePath()
				+"/audioTest.mp3");		
		player.start();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}

package test;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import appsgate.lig.clock.sensor.impl.ConfigurableClockImpl;
import appsgate.lig.clock.sensor.spec.AlarmEventObserver;

import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright 2011-2013 Universite Joseph Fourier, LIG, ADELE team
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 * SwingClockTest.java - 31 juil. 2013
 */

/**
 * @author thibaud
 * 
 */
public class SimpleClockTest implements AlarmEventObserver {

	long errorTolerance = 10;
	long systemTime;
	long clockTime;
	ConfigurableClockImpl clock;
	Set<Integer> receivedAlarm;
	int receivedAlarmCounter;

	private static Logger logger = LoggerFactory
			.getLogger(SimpleClockTest.class);

	// Just testing the GUI
	// public static void main(String[] args) {
	// SwingClock clock = new SwingClock();
	// clock.start();
	// clock.show();
	// }

	@Before
	public void setUp() {
		clock = new ConfigurableClockImpl();
		clock.start();
		receivedAlarm = new HashSet<Integer>();
		receivedAlarmCounter = 0;
	}

	@After
	public void stop() {
		clock.stop();
		clock = null;
	}

	@Test
	public void testBasicSetGetResetCurrentDate() throws JSONException {

		System.out.println("Description " + clock.getDescription().toString());

		// System.out.println("Testing basic system time");
		// systemTime = System.currentTimeMillis();
		// clockTime = clock.getCurrentTimeInMillis();
		// testTimeEqual(systemTime, clockTime);
		//
		// System.out.println("Wait for 4 secs");
		// try{
		// Thread.sleep(4321);
		// }catch (Exception exc) {
		// exc.printStackTrace();
		// }
		// systemTime = System.currentTimeMillis();
		// clockTime = clock.getCurrentTimeInMillis();
		// testTimeEqual(systemTime, clockTime);
		//
		// System.out.println("Set a particular Time");
		// Calendar cal = Calendar.getInstance();
		// cal.set(1977, 4, 1, 9, 54, 10);
		//
		// clock.setCurrentDate(cal);
		// testTimeEqual(cal.getTimeInMillis(),clock.getCurrentTimeInMillis());
		//
		// System.out.println("Test the reset");
		// clock.resetClock();
		// systemTime = System.currentTimeMillis();
		// testTimeEqual(systemTime, clock.getCurrentTimeInMillis());
	}

	@Test
	public void testTimeFlowSlower() {
		System.out.println("Testing time goes Slower");
		clock.setTimeFlowRate(0.5);
		systemTime = System.currentTimeMillis();
		clockTime = clock.getCurrentTimeInMillis();
		testTimeEqual(systemTime, clockTime);

		System.out
				.println("Wait for 4 secs, 2 should have been elapsed in virtual");
		try {
			Thread.sleep(4321);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		systemTime += 4321 / 2;
		clockTime = clock.getCurrentTimeInMillis();
		testTimeEqual(systemTime, clockTime);

	}

	@Test
	public void testTimeFlowFaster() {
		System.out.println("Testing time goes faster");
		clock.setTimeFlowRate(2);
		systemTime = System.currentTimeMillis();
		clockTime = clock.getCurrentTimeInMillis();
		testTimeEqual(systemTime, clockTime);

		System.out
				.println("Wait for 4 secs, 8 should have been elapsed in virtual");
		try {
			Thread.sleep(4321);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		systemTime += 4321 * 2;
		clockTime = clock.getCurrentTimeInMillis();
		testTimeEqual(systemTime, clockTime);

		System.out.println("Set a particular Time in past");
		Calendar cal = Calendar.getInstance();
		cal.set(1977, 4, 1, 9, 54, 10);

		clock.setCurrentDate(cal);
		systemTime = cal.getTimeInMillis();

		testTimeEqual(systemTime, clock.getCurrentTimeInMillis());

		System.out
				.println("Wait for 4 secs, 8 should have been elapsed in virtual");
		try {
			Thread.sleep(4321);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		systemTime += 4321 * 2;
		clockTime = clock.getCurrentTimeInMillis();
		testTimeEqual(systemTime, clockTime);

		System.out.println("Set a particular Time in future");
		cal.set(2022, 12, 31, 29, 59, 999);

		clock.setCurrentDate(cal);
		systemTime = cal.getTimeInMillis();

		testTimeEqual(systemTime, clock.getCurrentTimeInMillis());

		System.out
				.println("Wait for 4 secs, 8 should have been elapsed in virtual");
		try {
			Thread.sleep(4321);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		systemTime += 4321 * 2;
		clockTime = clock.getCurrentTimeInMillis();
		testTimeEqual(systemTime, clockTime);

<<<<<<< HEAD
	cal.setTimeInMillis(currentTime+80000);
	int alarmID2=clock.registerAlarm(cal, this);
	
	cal.setTimeInMillis(currentTime+120000);
	int alarmID3=clock.registerAlarm(cal, this);	
	
	cal.setTimeInMillis(System.currentTimeMillis()+160000);
	int alarmID4=clock.registerAlarm(cal, this);
	
	cal.setTimeInMillis(System.currentTimeMillis()+200000);
	int alarmID5=clock.registerAlarm(cal, this);
	
	
	System.out.println("1° Jumping nearly exact time the first one");
	clock.setCurrentTimeInMillis(currentTime+40000);
	try{
	    Thread.sleep(errorTolerance*10);
	}catch (Exception exc) {
	    exc.printStackTrace();
	}
	Assert.assertFalse("received alarm set should not be empty",receivedAlarm.isEmpty());
	Assert.assertTrue("received alarm set should contain the same event id as registered",receivedAlarm.contains(alarmID1));
	
	
	System.out.println("2° jumping just before exact time");
	clock.setCurrentTimeInMillis(currentTime+(80000-errorTolerance));
	try{
	    Thread.sleep(errorTolerance*20);
	}catch (Exception exc) {
	    exc.printStackTrace();
	}
	Assert.assertFalse("received alarm set should not be empty",receivedAlarm.isEmpty());
	Assert.assertTrue("received alarm set should contain the same event id as registered",receivedAlarm.contains(alarmID2));
	
	
	System.out.println("3° Jumping just after exact time");
	clock.setCurrentTimeInMillis(currentTime+(120000+errorTolerance));
	try{
	    Thread.sleep(errorTolerance*10);
	}catch (Exception exc) {
	    exc.printStackTrace();
=======
>>>>>>> 2bf9d1cd7d927acb21c47e9dfd6d18bb3743a896
	}

	void testTimeEqual(long systemTime, long clockTime) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(systemTime);
		System.out.println("Current System Time : " + cal.get(Calendar.SECOND)
				+ "s " + cal.get(Calendar.MILLISECOND) + "ms");

		cal.setTimeInMillis(clockTime);
		System.out.println("Current clock Time : " + cal.get(Calendar.SECOND)
				+ "s " + cal.get(Calendar.MILLISECOND) + "ms");

		if (clockTime - systemTime > errorTolerance
				|| clockTime - systemTime < -errorTolerance)
			Assert.fail("Latency between system time and clock time too high");
	}

	@Test
	public void testSimpleRegisterAlarm() {
		System.out.println("registering an alarm in 4 secs");
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis() + 4321);
		Integer alarmID = clock.registerAlarm(cal, this);
		try {
			Thread.sleep(4321 + errorTolerance * 10);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		Assert.assertFalse("received alarm set should not be empty",
				receivedAlarm.isEmpty());
		Assert.assertEquals(
				"received alarm set should contain only one element", 1,
				receivedAlarm.size());
		Assert.assertTrue(
				"received alarm set should contain the same event id as registered",
				receivedAlarm.contains(alarmID));

		System.out
				.println("registering an alarm in 4 secs, but we will only wait for 2 secs (should not be fired)");
		receivedAlarm.clear();
		cal.setTimeInMillis(System.currentTimeMillis() + 4321);
		alarmID = clock.registerAlarm(cal, this);
		try {
			Thread.sleep(4321 / 2);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		System.out.println("received alarms : " + receivedAlarm);

		Assert.assertTrue("received alarm set should be empty",
				receivedAlarm.isEmpty());

		System.out.println("unregistering the alarm then wait for 4 secs");
		clock.unregisterAlarm(alarmID.intValue());
		try {
			Thread.sleep(4321);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		Assert.assertTrue("received alarm should still be empty",
				receivedAlarm.isEmpty());
	}

	@Test
	public void testMultipleAlarm() {
		System.out.println("registering an alarm in 4 secs");
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis() + 4321);
		clock.registerAlarm(cal, this);
		try {
			Thread.sleep(500);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		clock.registerAlarm(cal, this);

		try {
			Thread.sleep(500);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		Integer alarmID = clock.registerAlarm(cal, this);

		try {
			Thread.sleep(4321 + errorTolerance * 10);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		Assert.assertFalse("received alarm set should not be empty",
				receivedAlarm.isEmpty());
		Assert.assertEquals("received alarm set should contain three elements",
				3, receivedAlarm.size());
		Assert.assertTrue(
				"received alarm set should contain the same event id as registered",
				receivedAlarm.contains(alarmID));

	}
	
	@Test
	public void testMultipleBis() {

		clock.setTimeFlowRate(60);
		clock.setCurrentTimeInMillis(System.currentTimeMillis() + 2*60*1000);
		
		
		System.out.println("registering an alarm in 4 mins");
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis() + 4*60*1000);
		Integer alarm1 = clock.registerAlarm(cal, this);
		System.out.println(" registered "+alarm1);
		

		
		System.out.println("registering an alarm in 6 mins");
		cal.setTimeInMillis(System.currentTimeMillis() + 6*60*1000);
		Integer alarm2 = clock.registerAlarm(cal, this);
		System.out.println(" registered "+alarm2);

		
		
		
		
		
		try {
			Thread.sleep(10000);
		} catch (Exception exc) {
			exc.printStackTrace();
		}


		Assert.assertFalse("received alarm set should not be empty",
				receivedAlarm.isEmpty());
		Assert.assertEquals("received alarm set should contain two elements",
				2, receivedAlarm.size());

	}	
	

	@Test
	public void testRegisteringAlarmAndJumpingNearly() {
		System.out
				.println("registering an five alarms at 40 secs interval each");
		Calendar cal = Calendar.getInstance();

		long currentTime = System.currentTimeMillis();
		cal.setTimeInMillis(currentTime + 40000);
		int alarmID1 = clock.registerAlarm(cal, this);

		cal.setTimeInMillis(currentTime + 80000);
		int alarmID2 = clock.registerAlarm(cal, this);

		cal.setTimeInMillis(currentTime + 120000);
		int alarmID3 = clock.registerAlarm(cal, this);

		cal.setTimeInMillis(System.currentTimeMillis() + 160000);
		int alarmID4 = clock.registerAlarm(cal, this);

		cal.setTimeInMillis(System.currentTimeMillis() + 200000);
		int alarmID5 = clock.registerAlarm(cal, this);

		System.out.println("1° Jumping nearly exact time the first one");
		clock.setCurrentTimeInMillis(currentTime + 40000);
		try {
			Thread.sleep(errorTolerance * 10);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		Assert.assertFalse("received alarm set should not be empty",
				receivedAlarm.isEmpty());
		Assert.assertTrue(
				"received alarm set should contain the same event id as registered",
				receivedAlarm.contains(alarmID1));

		System.out.println("2° jumping just before exact time");
		clock.setCurrentTimeInMillis(currentTime + (80000 - errorTolerance));
		try {
			Thread.sleep(errorTolerance * 10);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		Assert.assertFalse("received alarm set should not be empty",
				receivedAlarm.isEmpty());
		Assert.assertTrue(
				"received alarm set should contain the same event id as registered",
				receivedAlarm.contains(alarmID2));

		System.out.println("3° Jumping just after exact time");
		clock.setCurrentTimeInMillis(currentTime + (120000 + errorTolerance));
		try {
			Thread.sleep(errorTolerance * 10);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		Assert.assertFalse("received alarm set should not be empty",
				receivedAlarm.isEmpty());
		Assert.assertTrue(
				"received alarm set should contain the same event id as registered",
				receivedAlarm.contains(alarmID3));

		System.out.println("4° Jumping 1 sec before exact time");
		clock.setCurrentTimeInMillis(currentTime + 159000);
		try {
			Thread.sleep(1000 + errorTolerance * 10);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		Assert.assertFalse("received alarm set should not be empty",
				receivedAlarm.isEmpty());
		Assert.assertTrue(
				"received alarm set should contain the same event id as registered",
				receivedAlarm.contains(alarmID4));

		System.out.println("5° Jumping 1 sec after exact time");
		clock.setCurrentTimeInMillis(System.currentTimeMillis() + 201000);

		try {
			Thread.sleep(1000 + errorTolerance * 10);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		Assert.assertFalse(
				"received alarm set should NOT contain the same event id as registered",
				receivedAlarm.contains(alarmID5));

	}

	@Test
	public void testRegisterAlarmWithSimulFlow() {
		System.out.println("Time flow should go 2 time faster");
		clock.setTimeFlowRate(2);
		System.out
				.println("registering an alarm in 4 secs (but we will only wait for 3)");
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis() + 4321);
		Integer alarmID = clock.registerAlarm(cal, this);

		try {
			Thread.sleep(4321 - 1000);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		Assert.assertFalse("received alarm set should not be empty",
				receivedAlarm.isEmpty());
		Assert.assertEquals(
				"received alarm set should contain only one element", 1,
				receivedAlarm.size());
		Assert.assertTrue(
				"received alarm set should contain the same event id as registered",
				receivedAlarm.contains(alarmID));

		System.out.println("Time flow should go 2 time slower");
		receivedAlarm.clear();
		clock.setTimeFlowRate(0.5);
		System.out
				.println("registering an alarm in 4 secs (will only wait for 6)");
		cal.setTimeInMillis(System.currentTimeMillis() + 4321);
		alarmID = clock.registerAlarm(cal, this);

		try {
			Thread.sleep(4321 + 2000);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		Assert.assertTrue("received alarm set should be empty",
				receivedAlarm.isEmpty());

		System.out.println("witing for more 3 secs");
		try {
			Thread.sleep(4321 - 1000);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		Assert.assertFalse("received alarm set should not be empty",
				receivedAlarm.isEmpty());
		Assert.assertEquals(
				"received alarm set should contain only one element", 1,
				receivedAlarm.size());
		Assert.assertTrue(
				"received alarm set should contain the same event id as registered",
				receivedAlarm.contains(alarmID));

	}

	@Test
	public void testRegisterAlarmWithSimulFlowComplex() {
		long currentTime = System.currentTimeMillis();

		System.out.println("Registering an event in 100 secs (real)");
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis() + 100000);
		Integer alarmID = clock.registerAlarm(cal, this);

		System.out.println("Jumping to 90 secs later");
		clock.setCurrentTimeInMillis(currentTime + 90000);

		System.out.println("Time flow should go 10 times faster");
		clock.setTimeFlowRate(10);
		System.out
				.println("Waiting for 0,9 secs (should be 9 secs in simulated time)");

		try {
			Thread.sleep(900);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		Assert.assertTrue("received alarm set should be empty",
				receivedAlarm.isEmpty());
		System.out
				.println("Now, waiting for 0,1 more sec (should be 1 sec in simulated time)");

		try {
			Thread.sleep(100 + errorTolerance);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		Assert.assertFalse("received alarm set should not be empty",
				receivedAlarm.isEmpty());
		Assert.assertEquals(
				"received alarm set should contain only one element", 1,
				receivedAlarm.size());
		Assert.assertTrue(
				"received alarm set should contain the same event id as registered",
				receivedAlarm.contains(alarmID));

		// This test is too long !
		// System.out.println("Time flow should go 10 times slower");
		// clock.setTimeFlowRate(0.1);
		// System.out.println("Waiting for 9 secs (real)");
		//
		// try{
		// Thread.sleep(9000);
		// }catch (Exception exc) {
		// exc.printStackTrace();
		// }
		//
		// Assert.assertTrue("received alarm set should be empty",receivedAlarm.isEmpty());
		// System.out.println("Now, waiting for 1 more sec (real)");
		//
		// try{
		// Thread.sleep(1000+errorTolerance);
		// }catch (Exception exc) {
		// exc.printStackTrace();
		// }
		//
		// Assert.assertFalse("received alarm set should not be empty",receivedAlarm.isEmpty());
		// Assert.assertEquals("received alarm set should contain only one element",1,receivedAlarm.size());
		// Assert.assertTrue("received alarm set should contain the same event id as registered",receivedAlarm.contains(alarmID));
	}

	@Test
	public void testGoAlongUntil() {
		System.out
				.println("testGoAlongUntil(), adding a 6 events in the future (one for each hour, and jump into 4,5 hours)");
		long current = clock.getCurrentTimeInMillis();
		Calendar[] tabCalendar = new Calendar[6];

		int lastAlarmId = -1;
		for (int i = 0; i < tabCalendar.length; i++) {
			tabCalendar[i] = Calendar.getInstance();
			tabCalendar[i]
					.setTimeInMillis(current + ((i + 1) * 1000 * 60 * 60));
			lastAlarmId = clock.registerAlarm(tabCalendar[i], this);
		}

		try {
			Thread.sleep(4321);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		Assert.assertTrue("received alarm set should be empty",
				receivedAlarm.isEmpty());

		clock.goAlongUntil((long) (4.5 * 1000 * 60 * 60));
		Assert.assertEquals("received alarm set should contain 4 elements", 4,
				receivedAlarm.size());
		clock.goAlongUntil((1000 * 60 * 60));
		Assert.assertEquals("received alarm set should contain 5 elements", 5,
				receivedAlarm.size());

		clock.unregisterAlarm(lastAlarmId);
		clock.goAlongUntil((1000 * 60 * 60));
		Assert.assertEquals(
				"received alarm set should still contain 5 elements (last alarm unregistered)",
				5, receivedAlarm.size());
	}

	@Test
	public void testGoAlongUntilWithPeriodics() {
		System.out
				.println("testGoAlongUntil(), adding periodic events for each hour, and jump into 4,5 hours)");
		int alarmId = clock.registerPeriodicAlarm(null, 1000 * 60 * 60, this);

		try {
			Thread.sleep(4321);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		Assert.assertTrue("received alarm set should be empty",
				receivedAlarm.isEmpty());

		clock.goAlongUntil((long) (4.5 * 1000 * 60 * 60));
		Assert.assertEquals(
				"received alarm set should contain 1 element (periodic)", 1,
				receivedAlarm.size());
		Assert.assertEquals("should have received 4 elements", 4,
				receivedAlarmCounter);
		clock.goAlongUntil((1000 * 60 * 60));
		Assert.assertEquals("should have received 5 elements", 5,
				receivedAlarmCounter);

		clock.unregisterAlarm(alarmId);
		clock.goAlongUntil((1000 * 60 * 60));
		Assert.assertEquals(
				"should still have received 5 elements (alarm unregistered)",
				5, receivedAlarmCounter);
	}

	@Test
	public void testSimplePeriodicAlarms() {
		System.out
				.println("testSimplePeriodicAlarms(), adding a periodic events for each seconds");
		clock.registerPeriodicAlarm(null, 1000, this);

		try {
			Thread.sleep(4321);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		Assert.assertFalse("received alarm set should not be empty",
				receivedAlarm.isEmpty());
		Assert.assertEquals(
				"received alarm set should contain 1 element (same id for all events)",
				1, receivedAlarm.size());
		Assert.assertEquals("should receive 4 elements", 4,
				receivedAlarmCounter);

		System.out
				.println("registering a single alarm in 1 sec, then waiting for 4 secs more");
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis() + 1000);
		clock.registerAlarm(cal, this);

		try {
			Thread.sleep(4321);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		Assert.assertEquals(
				"received alarm set should contain 2 element (1 for all periodics + une for single alarm)",
				2, receivedAlarm.size());
		Assert.assertEquals("should have received 9 elements (total)", 9,
				receivedAlarmCounter);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * appsgate.lig.clock.sensor.spec.AlarmEventObserver#alarmEventFired(int)
	 */
	@Override
	public void alarmEventFired(int alarmEventId) {
		logger.debug("received an alarm with id : " + alarmEventId);
		receivedAlarm.add(alarmEventId);
		receivedAlarmCounter++;
	}

}

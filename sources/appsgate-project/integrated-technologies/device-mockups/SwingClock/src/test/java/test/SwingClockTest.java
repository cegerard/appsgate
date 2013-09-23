package test;

import java.util.HashSet;
import java.util.Set;


import appsgate.lig.clock.sensor.impl.SwingClockImpl;
import appsgate.lig.clock.sensor.spec.AlarmEventObserver;


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
public class SwingClockTest implements AlarmEventObserver{
    
    long errorTolerance=10;
    Set<Integer> receivedAlarm = new HashSet<Integer>();
    



// Just testing the GUI
        public static void main(String[] args) {
	SwingClockImpl clock = new SwingClockImpl();
	clock.start();
	clock.show();
    }
    
    


    /* (non-Javadoc)
     * @see appsgate.lig.clock.sensor.spec.AlarmEventObserver#alarmEventFired(int)
     */
    @Override
    public void alarmEventFired(int alarmEventId) {
	receivedAlarm.add(alarmEventId);
    }
    
}

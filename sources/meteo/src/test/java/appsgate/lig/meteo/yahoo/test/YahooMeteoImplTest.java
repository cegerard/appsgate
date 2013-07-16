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
 * YahooMeteoImplTest.java - 16 juil. 2013
 */
package appsgate.lig.meteo.yahoo.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import appsgate.lig.meteo.yahoo.YahooMeteoImplementation;


/**
 * @author thibaud
 *
 */
public class YahooMeteoImplTest {
    
    YahooMeteoImplementation testedMeteo;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
	testedMeteo = new YahooMeteoImplementation();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }


    /**
     * Test method for {@link appsgate.lig.meteo.yahoo.YahooMeteoImplementation#fetch()}.
     */
    @Test
    public void testFetch() {
	testedMeteo.fetch();
	assertNotNull(testedMeteo);
	//fail("Not yet implemented");
    }
}

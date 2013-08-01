import appsgate.lig.colorLight.actuator.swing.impl.SwingColorLightImpl;

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
 * LightTester.java - 1 août 2013
 */

/**
 * @author thibaud
 * 
 */
public class LightTester {

    /**
     * @param args
     */
    public static void main(String[] args) {
	SwingColorLightImpl myLight = new SwingColorLightImpl();
	myLight.show();
	try {

	    Thread.sleep(1000);
	    myLight.On();
	    myLight.setBrightness(255);
	    myLight.setSaturation(255);
	    
	    myLight.setBlue();
	    System.out.println("Light  blue !");
	    Thread.sleep(500);
	    
	    System.out.println("Light  green !");
	    myLight.setGreen();
	    Thread.sleep(500);
	    
	    System.out.println("Light  red !");
	    myLight.setRed();
	    Thread.sleep(500);
	    
	    
	    System.out.println("Light  pink !");
	    myLight.setPink();
	    Thread.sleep(500);
	    
	    System.out.println("Light  yellow !");
	    myLight.setYellow();
	    Thread.sleep(500);

	    System.out.println("Light  purple !");
	    myLight.setPurple();
	    Thread.sleep(500);
	    
	    System.out.println("Light  orange !");
	    myLight.setOrange();
	    Thread.sleep(500);

	    System.out.println("current brightness : "+myLight.getLightBrightness()
		    +", current staturation : "+myLight.getLightColorSaturation()
		    +", current color : "+myLight.getLightColor());
	    
	    
	    System.out.println("Playing with brightness");
	    for(int i=0; i<255;i++) {
		myLight.setBrightness(i);
		Thread.sleep(50);
	    }
	    
	    System.out.println("Playing with saturation");
	    for(int i=0; i<255;i++) {
		myLight.setSaturation(i);
		Thread.sleep(50);
	    }
	    
	    System.out.println("Playing with color");
	    for(int i=0; i<6335;i+=10) {
		myLight.setColor(i);
		Thread.sleep(10);
	    }
	    

	    myLight.hide();
	    
	} catch (InterruptedException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

}

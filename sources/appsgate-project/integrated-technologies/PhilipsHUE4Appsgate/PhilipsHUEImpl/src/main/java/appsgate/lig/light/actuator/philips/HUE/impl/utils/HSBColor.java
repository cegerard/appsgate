package appsgate.lig.light.actuator.philips.HUE.impl.utils;


/**
 * Helper class to manipulate Hue Saturation Brightness as provided by philips
 * @author thibaud
 *
 */
public class HSBColor {
	
	/**
	 * Philips API definition of Hue between 0 and 65535
	 * (The real Hue should be between 0 and 360°)
	 */
	int hue;
	
	/**
	 * Philips API definition of Saturation between 0 and 254
	 * (The real Saturation should be between 0 and 1)
	 */
	int saturation;
	
	/**
	 * Philips API defintion of Brightness between 1 and 254
	 * (The real brightness should be between 0 and 1)
	 */	
	int brightness;	
	
	public int getHue() {
		return hue;
	}

	public int getSaturation() {
		return saturation;
	}

	public int getBrightness() {
		return brightness;
	}

	public void setHue(int hue) {
		if(hue>65535) {
			this.hue = 65535;
		} else if (hue < 0) {
			this.hue = 0;
		} else {
			this.hue = hue;
		}
	}

	public void setSaturation(int saturation) {
		if(saturation>254) {
			this.saturation = 254;
		} else if (saturation < 0) {
			this.saturation = 0;
		} else {
			this.saturation = saturation;
		}
	}

	public void setBrightness(int brightness) {
		if(brightness>254) {
			this.brightness = 254;
		} else if (brightness < 1) {
			this.brightness = 1;
		} else {
			this.brightness = brightness;
		}	
	}

	/**
	 * @param hue
	 * @param saturation
	 * @param brightness
	 */
	public HSBColor(int hue, int saturation, int brightness) {
		setHue(hue);
		setSaturation(saturation);
		setBrightness(brightness);
	}
	
	/**
	 * Using formula from wikipedia
	 * @see http://fr.wikipedia.org/wiki/Teinte_Saturation_Valeur
	 * @return
	 */
	public RGBColor toRGB() {
		RGBColor color=null;
		
        int t = ((hue*360)/65535);
        
        int ti = (t/60)%6;
        
        double h = (hue*360.0)/65535.0;
        double s = saturation/254.0;
        double b = brightness/254.0;
        
        double f = (h/60.0)-ti;
        double l = b*(1-s);
        double m = b*(1-(f*s));
        double n = b*(1-(1-f)*s);
        
        switch(ti) {
        case 0 :
        	color = new RGBColor((int)(b*255), (int)(n*255), (int)(l*255));
        	break;
        case 1 : 
        	color = new RGBColor((int)(m*255), (int)(b*255), (int)(l*255));
        	break;
        case 2 :
        	color = new RGBColor((int)(l*255), (int)(b*255), (int)(n*255));
        	break;
        case 3 :
        	color = new RGBColor((int)(l*255), (int)(m*255), (int)(b*255));
        	break;
        case 4 :
        	color = new RGBColor((int)(n*255), (int)(l*255), (int)(b*255));
        	break;
        case 5 :
        	color = new RGBColor((int)(b*255), (int)(l*255), (int)(m*255));
        	break;        	
        }
		return color;
		
	}

}

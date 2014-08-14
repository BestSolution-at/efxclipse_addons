package at.bestolution.efxclipse.icons.fontawsome;

import java.util.List;

public class AwsomeIconContribution {
	
	public String getName() {
		return "Awesome";
	}
	
	public String getFamily() {
		return "Awesome";
	}
	
	public List<String> getIconNames() {
		return null;
	}
	
	public char getIconCode(String iconName) {
		return AwesomeIcons.get(iconName).getValue();
	}

}

package vlad.utilities;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class GridBagConstraintsBuilder {
	
/**
 * 	A builder for the GridBagConstraints java class
 */
	
	private GridBagConstraints myConstraints;
	
	public GridBagConstraintsBuilder(){
		
		myConstraints = new GridBagConstraints();
	}
	
	public GridBagConstraintsBuilder gridx(int val){ 
		myConstraints.gridx = val; 
		return this;
	}
	
	public GridBagConstraintsBuilder gridy(int val){ 
		myConstraints.gridy = val;
		return this;
	}
	
	public GridBagConstraintsBuilder gridheight(int val){
		myConstraints.gridheight= val;
		return this;
	}
	
	public GridBagConstraintsBuilder gridwidth(int val){
		myConstraints.gridwidth= val;
		return this;
	}
	
	public GridBagConstraintsBuilder fill(int val){
		myConstraints.fill = val;
		return this;
	}
	
	public GridBagConstraintsBuilder ipadx(int val){
		myConstraints.ipadx = val;
		return this;
	}
	
	public GridBagConstraintsBuilder ipady(int val){
		myConstraints.ipady = val;
		return this;
	}
	
	public GridBagConstraintsBuilder insets(Insets insets){
		myConstraints.insets = insets;
		return this;
	}
	
	public GridBagConstraintsBuilder anchor(int val){
		myConstraints.anchor = val;
		return this;
	}
	
	public GridBagConstraintsBuilder weightx(double val){
		myConstraints.weightx = val;
		return this;
	}
	
	public GridBagConstraintsBuilder weighty(double val){
		myConstraints.weighty = val;
		return this;
	}
	
	public GridBagConstraints build(){
		
		return myConstraints;
	}

}

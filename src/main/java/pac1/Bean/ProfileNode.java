package pac1.Bean;

import javax.swing.tree.DefaultMutableTreeNode;

public class ProfileNode extends DefaultMutableTreeNode
{
	public static String className = "ProfileNode";
	DefaultMutableTreeNode defaultMutableTreeNode = null;
	private String name = "";
	private int type;

	public ProfileNode(DefaultMutableTreeNode node) {
		this.defaultMutableTreeNode = node;
	}

	public ProfileNode(String name) {
		super(name);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
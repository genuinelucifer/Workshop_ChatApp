package iiti.progclub.app.chatapp_workshop;

public class CustomListItem {
	private String name;
	private boolean hasNewMsgs;
	
	public CustomListItem(String Name, boolean HasNewMsgs)
	{
		name = Name;
		hasNewMsgs = HasNewMsgs;
	}
	
	public void setName(String Name)
	{
		name = Name;
	}
	public String getName()
	{
		return name;
	}
	
	public void setNewMsgs(boolean HasNewMsgs)
	{
		hasNewMsgs = HasNewMsgs;
	}
	public boolean getHasNewMsgs()
	{
		return hasNewMsgs;
	}
}

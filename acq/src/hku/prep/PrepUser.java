package hku.prep;
import java.util.HashSet;
import java.util.Set;

/**
 * @author yxfang
 *
 * @date 2015-7-19
 */
public class PrepUser {
	private int userID = -1;
	private String userIDStr = "";
	private String username = null;
	private Set<String> keySet = null;
	private Set<Integer> edgeSet = null;
	
	public PrepUser(int userID, String username){
		this.userID = userID;
		this.username = username;
		this.keySet = new HashSet<String>();
		this.edgeSet = new HashSet<Integer>();
	}
	
	public PrepUser(String userIDStr, String username){
		this.userIDStr = userIDStr;
		this.username = username;
		this.keySet = new HashSet<String>();
		this.edgeSet = new HashSet<Integer>();
	}
	
	public int getUserID() {
		return userID;
	}
	public void setUserID(int userID) {
		this.userID = userID;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public Set<String> getKeySet() {
		return keySet;
	}
	public void setKeySet(Set<String> keySet) {
		this.keySet = keySet;
	}
	public Set<Integer> getEdgeSet() {
		return edgeSet;
	}
	public void setEdgeSet(Set<Integer> edgeSet) {
		this.edgeSet = edgeSet;
	}

	public String getUserIDStr() {
		return userIDStr;
	}

	public void setUserIDStr(String userIDStr) {
		this.userIDStr = userIDStr;
	}
	
}

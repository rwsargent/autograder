package autograder.canvas.responses;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

/**
 * POJO to represent the User / Profile object from the Cavnas API
 * @author Ryan
 */
public class User implements Serializable{
	private static final long serialVersionUID = -8318536792879193036L;
	
	public int id;
	public String name;
	
	@SerializedName("sortable_name")
	public String sortableName;
	public String sis_user_id;
	public String sis_import_id;
	public String sis_login_id;
	public String loging_id;
	public String avatar_url;
	public String enrollments;
	public String email;
	public String local;
	
	public static String forFileName(User user) {
		return user.sortableName.replace(", ", "_");
	}
	
	public String asFileName() {
		return sortableName.replace(", ", "_");
	}
}

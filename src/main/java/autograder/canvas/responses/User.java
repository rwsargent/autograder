package autograder.canvas.responses;

import com.google.gson.annotations.SerializedName;

public class User extends BaseReponse{
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
}

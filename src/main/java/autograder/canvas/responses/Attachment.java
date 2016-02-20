package autograder.canvas.responses;

import com.google.gson.annotations.SerializedName;

/**
 * POJO to represent the Attachment object from the Cavnas API
 * @author Ryan
 *
 */
public class Attachment {

	public int id;
	public String filename, display_name;
	public String url;
	@SerializedName("content-type")
	public String contentType;
	
}

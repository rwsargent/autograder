package autograder.canvas;

import java.io.IOException;
import java.util.zip.ZipFile;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClients;

public class CanvasConnection {
	
	public static ZipFile getSubmissionZipFile(String courseId, String assignmentId) {
		HttpClient client = HttpClients.createDefault();
		String uri = createSubmissionURI(courseId, assignmentId);
		HttpGet request = (HttpGet) RequestBuilder.get(uri)
				.addHeader("", "")
				.build();
		try {
			client.execute(request);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String createSubmissionURI(String courseId, String assignmentId) {
		return String.format("/api/v1/courses/%s/assignments/%s/submissions", courseId, assignmentId);
	}
	
}

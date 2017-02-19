package autograder.canvas.responses;

import java.util.Date;

public class Assignment {

	public int id, course_id;
	public String name, description,
	created_at, updated_at; 
	public Date due_at;
	
	@Override
	public boolean equals(Object obj) {
		if( obj instanceof Assignment) {
			Assignment assignment = (Assignment) obj;
			return assignment.id == this.id  && assignment.course_id == this.course_id;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.course_id * this.id;
	}
}

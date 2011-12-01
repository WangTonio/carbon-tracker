package de.unigoettingen.ct.data.io;

/**
 * These objects play the role of 'export' objects as they will directly be marshalled and uploaded to the server.
 * DO NOT TOUCH.
 * @author Fabian Sudau
 *
 */
public class Person {

	private String forename;
	private String lastname;

	public Person(String forename, String lastname) {
		super();
		this.forename = forename;
		this.lastname = lastname;
	}

	public Person() {

	}

	public String getForename() {
		return forename;
	}

	public void setForename(String forename) {
		this.forename = forename;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	
	@Override
	public String toString() {
		return "(Person: forename="+forename+" ,lastname="+lastname+")";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Person other = (Person) obj;
		if (forename == null) {
			if (other.forename != null)
				return false;
		}
		else if (!forename.equals(other.forename))
			return false;
		if (lastname == null) {
			if (other.lastname != null)
				return false;
		}
		else if (!lastname.equals(other.lastname))
			return false;
		return true;
	}

	
}

package de.unigoettingen.ct.data;

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

}

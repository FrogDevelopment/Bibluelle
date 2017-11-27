package fr.frogdevelopment.bibluelle.search.rest.google;


import java.io.Serializable;

public class IndustryIdentifiers implements Serializable {

	public enum Type {
		ISBN_10, ISBN_13
	}

	private Type type;
	private String identifier;

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
}

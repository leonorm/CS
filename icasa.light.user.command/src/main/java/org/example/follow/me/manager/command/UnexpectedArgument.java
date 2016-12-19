package org.example.follow.me.manager.command;

public class UnexpectedArgument extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnexpectedArgument() {
	    super();
	  }

	  public UnexpectedArgument(String s) {
	    super(s);
	  }
}

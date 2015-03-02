package com.example;

import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class RepositoryServlet
 */
@WebServlet("/repository")
public class RepositoryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String SAMPLE_CONTENT_KEY = "sampleContentKey";
	private static final String SAMPLE_CONTENT_VALUE = "Hello Jackrabbit!!!!";
	
	@EJB
	private Warehouse warehouse;
	
	public void init() throws ServletException {
		try {
			warehouse.store(SAMPLE_CONTENT_KEY, SAMPLE_CONTENT_VALUE.getBytes());
		}
		catch (Exception e) {
			throw new ServletException(e);
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		try {
			byte[] contents = warehouse.fetch(SAMPLE_CONTENT_KEY);
			response.getWriter().println(new String(contents));
		}
		catch (Exception e) {
			e.printStackTrace(response.getWriter());
		}
	}

}

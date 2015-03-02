package com.example.rest;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;

import com.example.Warehouse;

@Stateless
@Path("/repo")
public class FedEx {
	
	@EJB
	private Warehouse warehouse;
	
	@PostConstruct
	private void init() {
		if (warehouse == null) {
			System.out.println("Can't inject this!");
		}
	}
	
	// looks like PUT should be used instead of POST for the repo:
	// http://stackoverflow.com/questions/630453/put-vs-post-in-rest
	@PUT
	@Path("{var:.*}")
	public String setContent(@PathParam("var") String path,
			@Context HttpServletRequest request) {
		String ret = "ok\n";
		try {
			warehouse.store(path, IOUtils.toByteArray(request.getInputStream()));
		} catch (Exception e) {
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
		return ret;
	}

	@GET
	@Path("{var:.*}")
	@Produces("*/*")
	public byte[] getContent(@PathParam("var") String path) {
		byte[] ret = null;
		try {
			ret = warehouse.fetch(path);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO implement and register javax.ws.rs.ext.ExceptionMapper
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
		if (ret == null) {
			throw new WebApplicationException(Status.NO_CONTENT);
		}
		return ret;
	}
}

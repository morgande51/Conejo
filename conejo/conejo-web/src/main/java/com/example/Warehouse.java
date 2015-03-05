package com.example;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.jcr.Binary;
import javax.jcr.LoginException;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Node;
import javax.jcr.ValueFactory;
import javax.naming.InitialContext;

import org.apache.commons.io.IOUtils;

@Singleton
@Startup
@LocalBean
public class Warehouse {
	
	private static final String REPO_JNDI_NAME = "jcr/repository";
	private static final String USERNAME = "admin";
	private static final char[] PASSWORD = "admin".toCharArray();
	
	private Repository repository;
	
	@PostConstruct
	private void init() {
		try {
			InitialContext ctx = new InitialContext();
			repository = (Repository)ctx.lookup(REPO_JNDI_NAME);
			System.out.println("We got a repository on JNDI tree!");
			System.out.println(repository);
			
			// perform basic login to force full configuration of Jackrabbit
			System.out.println("Using Jackrabbit worksapce: " + authenticate().getWorkspace().getName());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void store(String path, byte[] content) throws LoginException, RepositoryException {
		set(path, content);
	}

	public byte[] fetch(String path) throws LoginException, RepositoryException, IOException {
		return get(path);
	}

	private String[] explode(String path) {
		if ((path == null) || (path.isEmpty())) {
			throw new IllegalArgumentException("path must not be empty");
		} else if (path.endsWith("/")) {
			throw new IllegalArgumentException("path must not end with '/'");
		} else if (path.startsWith("/")) {
			path = path.substring(1);
		}
		return path.split("/");
	}

	private void set(String path, byte[] content) throws LoginException,
			RepositoryException {
		Session session = null;
		try {
			session = authenticate();
			String[] parts = explode(path);
			Node node = session.getRootNode();
			for (int i = 0; i < parts.length; i++) {
				String part = parts[i];
				if (i == (parts.length - 1)) {
//					ValueFactory vf = ValueFactoryImpl.getInstance();
					ValueFactory vf = session.getValueFactory();
					Binary b = vf
							.createBinary(new ByteArrayInputStream(content));
					node.setProperty(part, b);
				} else {
					// TODO disallow same-name siblings.
					try {
						node = node.getNode(part);
					} catch (PathNotFoundException e) {
						node = node.addNode(part);
					}
				}
			}
			session.save();
		} finally {
			if (session != null) {
				session.logout();
			}
		}
	}

	private byte[] get(String path) throws LoginException, RepositoryException,
			IOException {
		byte[] ret = null;
		Session session = null;
		try {
			session = authenticate();
			Binary b = session.getRootNode().getProperty(path).getBinary();
			ret = IOUtils.toByteArray(b.getStream());
			b.dispose();
		} finally {
			if (session != null) {
				session.logout();
			}
		}
		return ret;
	}
	
	// TODO use oracle backend w/ actual credentials
	private Session authenticate() throws LoginException, RepositoryException {
		return repository.login(new SimpleCredentials(USERNAME, PASSWORD));
	}
}
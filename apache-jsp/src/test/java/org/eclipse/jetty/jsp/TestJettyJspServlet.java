//
//  ========================================================================
//  Copyright (c) 1995-2016 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.jsp;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspFactory;

import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletTester;
import org.eclipse.jetty.toolchain.test.MavenTestingUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.jasper.runtime.JspFactoryImpl;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class TestJettyJspServlet
{
    File _dir;
    ServletTester _tester;
    
    public static class DfltServlet extends HttpServlet
    {

        public DfltServlet()
        {
            super();
        }
        
        /** 
         * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
         */
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
        {
            resp.setContentType("html/text");
            resp.getOutputStream().println("This.Is.The.Default.");
        }
        
    }
    
    @Before
    public void setUp () throws Exception
    {
        JspFactory.setDefaultFactory(new JspFactoryImpl());
        _dir = MavenTestingUtils.getTestResourceDir("base");
        _tester = new ServletTester("/context");
        _tester.getContext().setClassLoader(new URLClassLoader(new URL[0], Thread.currentThread().getContextClassLoader()));
        ServletHolder jspHolder = _tester.getContext().addServlet(JettyJspServlet.class, "/*");
        jspHolder.setInitParameter("scratchdir", MavenTestingUtils.getTargetTestingDir().getAbsolutePath());
        _tester.getContext().setResourceBase(_dir.getAbsolutePath());
        _tester.getContext().setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
        ServletHolder dfltHolder = new ServletHolder();
        dfltHolder.setName("default");
        dfltHolder.setHeldClass( DfltServlet.class);
        _tester.getContext().addServlet(dfltHolder, "/");

        _tester.start();
    }
    
    @After
    public void tearDown() throws Exception
    {
        if (_tester != null)
            _tester.stop();
    }

    @Test
    public void testWithJsp() throws Exception
    {
        //test that an ordinary jsp is served by jsp servlet
        String request = "" +
                "GET /context/foo.jsp HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Connection: close\r\n" +
                "\r\n";
        String response = _tester.getResponses(request);
        assertTrue(!response.contains("This.Is.The.Default."));
    }
    
    
    @Test
    public void testWithDirectory() throws Exception
    {
        //test that a dir is served by the default servlet
        String request = "" +
                "GET /context/dir HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Connection: close\r\n" +
                "\r\n";
        String response = _tester.getResponses(request);
        assertTrue(response.contains("This.Is.The.Default."));
    }

}

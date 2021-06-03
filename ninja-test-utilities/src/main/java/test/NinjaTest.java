/**
 * Copyright (C) the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test;


import java.net.URI;

import test.utils.NinjaTestBrowser;
import test.utils.NinjaTestServer;

import org.junit.After;
import org.junit.Before;

import com.google.inject.Injector;

/**
 * Baseclass for tests that require a running server.
 * 
 * @author rbauer
 * 
 */
public class NinjaTest {

    /** Backend of the test => Starts Ninja */
	private NinjaTestServer ninjaTestServer;
	
	/** A persistent HttpClient that stores cookies to make requests */
	protected NinjaTestBrowser ninjaTestBrowser;
	
	public NinjaTest() {
	    //intentionally left emtpy.
	    //startup stuff is done in @Before method.
    }

    @Before
    public final void startupServerAndBrowser() {
        ninjaTestServer = NinjaTestServer.builder().build();
        ninjaTestBrowser = new NinjaTestBrowser();
    }

    protected Injector getInjector(){
        return ninjaTestServer.getInjector();
    }
    
    protected  <T> T getInstance(Class<T> type){
        return ninjaTestServer.getInjector().getInstance(type);
    }

    protected String to(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Path was null");
        }
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("Path must start with '/'");
        }
        return ninjaTestServer.getBaseUrl() + path;
    }

    /**
     * Something like http://localhost:8080/ 
     * 
     * Will contain trailing slash!
     * @return
     */
    public String getServerAddress() {
		return ninjaTestServer.getServerAddress();
	}

    public URI getServerAddressAsUri() {
        return ninjaTestServer.getServerAddressAsUri();
    }

    @After
    public final void shutdownServerAndBrowser() {
        if (ninjaTestServer != null) {
            ninjaTestServer.shutdown();
        }
        if (ninjaTestBrowser != null) {
            ninjaTestBrowser.shutdown();
        }
    }
}

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

package ninja.utils;

import com.google.common.base.Preconditions;
import java.net.URI;
import java.net.URISyntaxException;
import com.google.inject.Injector;
import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import ninja.standalone.Standalone;
import ninja.standalone.StandaloneHelper;

/**
 * Starts a new server using an embedded standalone. Startup is really fast and thus
 * usable in integration tests.
 */
public class NinjaTestServer implements Closeable {

    private final Standalone<Standalone> standalone;
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Optional<NinjaMode> ninjaModeOpt = Optional.empty();
        private Optional<Integer> portOpt = Optional.empty();
        private Optional<Class<? extends Standalone>> standaloneClassOpt = Optional.empty();
        private Optional<com.google.inject.Module> overrideModuleOpt = Optional.empty();
        private Optional<Map<String, String>> overrideProperties = Optional.empty();
        
        private Builder() {}
        
        /**
         * 
         * @param ninjaMode The mode to start this server in.
         * @return this builder for chaining
         */
        public Builder ninjaMode(NinjaMode ninjaMode) {
            Preconditions.checkNotNull(ninjaMode);
            this.ninjaModeOpt = Optional.of(ninjaMode);
            
            return this;
        }
        
        /**
         * The port where this server will listen.
         * 
         * If not set it will pick an available port in range 1000 to 10000.
         * 
         * @param port The port where this server will listen.
         * @return this builder for chaining
         */
        public Builder port(Integer port) {
            Preconditions.checkNotNull(port);
            this.portOpt = Optional.of(port);
            
            return this;
        }
        
        /**
         * Specify the class to use for starting the server.
         * 
         * Will fallback to NinjaJetty if not used.
         * 
         * @param standaloneClass The standalone class to use for starting the server. 
         * @return this builder for chaining
         */
        public Builder standaloneClass(Class<? extends Standalone> standaloneClass) {
            Preconditions.checkNotNull(standaloneClass);
            this.standaloneClassOpt = Optional.of(standaloneClass);
            
            return this;
        }
        
        /**
         * All bindings defined in this module will override any other bindings.
         * 
         * This is very useful in tests, where certain bindings can be replaced
         * with mocked versions. 
         * 
         * @param overrideModule The module with bindings - all its bindings will override any other bindings.
         * @return this builder for chaining 
         */
        public Builder overrideModule(com.google.inject.Module overrideModule) {
            Preconditions.checkNotNull(overrideModule);
            this.overrideModuleOpt = Optional.of(overrideModule);
            
            return this;
        }
        
        /**
         * These properties will overwrite any other properties. 
         * It for instance overwrites properties in conf/application.conf.
         * 
         * This is very useful in tests where you want to set certain properties
         * like a jdbc connection based on a url that gets defined when the
         * testcontainer gets started.
         * 
         * The key should be written in a dot-separated format. 
         * Eg "application.server.url".
         * 
         * @param overrideProperties A map with keys and values that will overwrite 
         *                            any previously set properties. These properties will
         *                            have the highest priority and will be present when
         *                            the server is running.
         * @return this builder for chaining
         */
        public Builder overrideProperties(Map<String, String> overrideProperties) {
            Preconditions.checkNotNull(overrideProperties);
            this.overrideProperties = Optional.of(overrideProperties);
            
            return this;
        }
        
        public NinjaTestServer build() {
            Class<? extends Standalone> standaloneClass = this.standaloneClassOpt
                    .orElseGet(() -> StandaloneHelper.resolveStandaloneClass());
            Integer port = this.portOpt.orElseGet(() -> StandaloneHelper.findAvailablePort(1000, 10000));
            NinjaMode ninjaMode = ninjaModeOpt.orElseGet(() -> NinjaMode.test);
            
            return new NinjaTestServer(ninjaMode, standaloneClass, port, this.overrideModuleOpt, this.overrideProperties);
        }

    }
    
    private NinjaTestServer(
            NinjaMode ninjaMode, 
            Class<? extends Standalone> standaloneClass, 
            int port,
            Optional<com.google.inject.Module> overrideModuleOpt,
            Optional<Map<String, String>> overridePropertiesOpt) {
        
        this.standalone = StandaloneHelper.create(standaloneClass);
        
        try {
            // configure then start
            this.standalone
                .port(port)
                .ninjaMode(ninjaMode);
            
            if (overrideModuleOpt.isPresent()) {
                this.standalone.overrideModule(overrideModuleOpt.get());
            }
            
            if (overridePropertiesOpt.isPresent()) {
                this.standalone.overrideProperties(overridePropertiesOpt.get());
            }
            
            standalone.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * @deprecated This does not affect a running server -- which happens in
     * the constructor.  You'll want to remove this from your code and include
     * the mode in the constructor.
     */
    @Deprecated
    public NinjaTestServer ninjaMode(NinjaMode ninjaMode) {
        standalone.ninjaMode(ninjaMode);
        return this;
    }
    
    /**
     * @deprecated This does not affect a running server -- which happens in
     * the constructor.  You'll want to remove this from your code and include
     * the mode in the constructor.
     */
    @Deprecated
    public NinjaMode getNinjaMode() {
        return standalone.getNinjaMode();
    }

    /**
     * Gets the guice injector for this test server.
     * @return The guice injector
     */
    public Injector getInjector() {
        return standalone.getInjector();
    }

    /**
     * Gets the url of the running server. It represents the scheme, host, and
     * port, but does not include the context path of the application. It will
     * return something like "http://localhost:8080". You probably want to use
     * getBaseUrl() which does include the context path (if one is configured).
     * @return The url of the server such as "http://localhost:8080" - note it
     *      does NOT include a trailing '/'.
     * @see #getBaseUrl() 
     */
    public String getServerUrl() {
        return standalone.getServerUrls().get(0);
    }
    
    /**
     * Gets the url of the running application. It represents the scheme, host,
     * port, and context path (if one is configured). It will return something
     * like "http://localhost:8080" or "http://localhost:8080/mycontext" if
     * a context of "/mycontext" is configured.
     * @return The url of the application such as "http://localhost:8080" - note
     *      it does NOT include a trailing '/'
     * @see #getServerUrl() 
     */
    public String getBaseUrl() {
        return standalone.getBaseUrls().get(0);
    }
    
    /**
     * @deprecated Does not include a configured context path as part of this
     * uri. Also returns a uri with a trailing '/', while its more common to
     * build a uri as (baseUri + "/path") since "/path" is what an href looks
     * like in html.
     * @see #getServerUrl()
     * @see #getBaseUrl()
     */
    @Deprecated
    public String getServerAddress() {
        return standalone.getServerUrls().get(0) + "/";
    }

    /**
     * @deprecated Does not include a configured context path as part of this
     * uri. Also returns a uri with a trailing '/', while its more common to
     * build a uri as (baseUri + "/path") since "/path" is what an href looks
     * like in html.
     * @see #getServerUrl()
     * @see #getBaseUrl()
     */
    @Deprecated
    public URI getServerAddressAsUri() {
        try {
            return new URI(getServerAddress());
        } catch (URISyntaxException e) {
            // should not be able to happen...
            return null;
        }
    }

    public void shutdown() {
        standalone.shutdown();
    }

    @Override
    public void close() throws IOException {
        // so a test server can be used in a try-with-resources statement
        shutdown();
    }

}

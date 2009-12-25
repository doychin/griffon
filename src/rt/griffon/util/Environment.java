/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package griffon.util;

import groovy.lang.Closure;
import groovy.lang.MissingMethodException;

import java.util.HashMap;
import java.util.Locale;


/**
 * An enum that represents the current environment
 *
 * @author Graeme Rocher
 * @since 1.1
 *        <p/>
 *        Created: Dec 12, 2008
 */
public enum Environment {
    DEVELOPMENT, PRODUCTION, TEST, CUSTOM;
    /**
     * Constant used to resolve the environment via System.getProperty(Environment.KEY)
     */
    public static String KEY = "griffon.env";

    /**
     * Constants that indicates whether this GriffonApplication is running in the default environment
     */
    public static final String DEFAULT = "griffon.env.default";
    private static final String PRODUCTION_ENV_SHORT_NAME = "prod";
    private static final String DEVELOPMENT_ENVIRONMENT_SHORT_NAME = "dev";

    private static final String TEST_ENVIRONMENT_SHORT_NAME = "test";
    private static HashMap<String, String> envNameMappings = new HashMap<String, String>() {{
        put(DEVELOPMENT_ENVIRONMENT_SHORT_NAME, Environment.DEVELOPMENT.getName());
        put(PRODUCTION_ENV_SHORT_NAME, Environment.PRODUCTION.getName());
        put(TEST_ENVIRONMENT_SHORT_NAME, Environment.TEST.getName());
    }};

    /**
     * Returns the current environment which is typcally either DEVELOPMENT, PRODUCTION or TEST.
     * For custom environments CUSTOM type is returned.
     *
     * @return The current environment.
     */
    public static Environment getCurrent() {
        String envName = System.getProperty(Environment.KEY);
        Metadata metadata = Metadata.getCurrent();
        if(metadata!=null && isBlank(envName)) {
            envName = metadata.getEnvironment();
        }

        if(isBlank(envName)) {
            return DEVELOPMENT;
        }
        else {
            Environment env = getEnvironment(envName);
            if(env == null) {
                try {
                    env = Environment.valueOf(envName.toUpperCase());
                }
                catch (IllegalArgumentException e) {
                    // ignore
                }
            }
            if(env == null) {
                env = Environment.CUSTOM;
                env.setName(envName);
            }
            return env;
        }
    }

    /**
     * @see #getCurrent()
     */
    public static Environment getCurrentEnvironment() {
        return getCurrent();        
    }

    /**
     * @return Return true if the environment has been set as a System property
     */
    public static boolean isSystemSet() {
        return System.getProperty(KEY) !=null;
    }

    /**
     * Returns the environment for the given short name
     * @param shortName The short name
     * @return The Environment or null if not known
     */
    public static Environment getEnvironment(String shortName) {
        final String envName = envNameMappings.get(shortName);
        if(envName !=null) {
            return Environment.valueOf(envName.toUpperCase());
        }
        return null;
    }

    /**
     * Takes an environment specific DSL block like:
     *
     * <code>
     * environments {
     *      development {}
     *      production {}
     * }
     * </code>
     *
     * And returns the closure that relates to the current environment
     *
     * @param closure The top level closure
     * @return The environment specific block or null if non exists
     */
    public static Closure getEnvironmentSpecificBlock(Closure closure) {
        final Environment env = getCurrent();
        return getEnvironmentSpecificBlock(env, closure);
    }

    /**
     * Takes an environment specific DSL block like:
     *
     * <code>
     * environments {
     *      development {}
     *      production {}
     * }
     * </code>
     *
     * And returns the closure that relates to the specified
     *
     * @param env The environment to use
     * @param closure The top level closure
     * @return The environment specific block or null if non exists
     */
    public static Closure getEnvironmentSpecificBlock(Environment env, Closure closure) {
        if(closure != null) {
            final EnvironmentBlockEvaluator evaluator = evaluateEnvironmentSpecificBlock(env, closure);
            return evaluator.getCallable();

        }
        return null;
    }

    /**
     * Takes an environment specific DSL block like:
     *
     * <code>
     * environments {
     *      development {}
     *      production {}
     * }
     * </code>
     *
     * And executes the closure that relates to the current environment
     *
     * @param closure The top level closure
     * @return The result of the closure execution
     */
    public static Object executeForCurrentEnvironment(Closure closure) {
        final Environment env = getCurrent();
        return executeForEnvironment(env, closure);

    }

    /**
     * Takes an environment specific DSL block like:
     *
     * <code>
     * environments {
     *      development {}
     *      production {}
     * }
     * </code>
     *
     * And executes the closure that relates to the specified environment
     *
     * @param env The environment to use
     * @param closure The top level closure
     * @return The result of the closure execution
     */
    public static Object executeForEnvironment(Environment env, Closure closure) {
        if(closure != null) {
            final EnvironmentBlockEvaluator evaluator = evaluateEnvironmentSpecificBlock(env, closure);
            return evaluator.execute();

        }
        return null;
    }

    private static EnvironmentBlockEvaluator evaluateEnvironmentSpecificBlock(Environment environment, Closure closure) {
        final EnvironmentBlockEvaluator evaluator = new EnvironmentBlockEvaluator(environment);
        closure.setDelegate(evaluator);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();
        return evaluator;
    }

    private static class EnvironmentBlockEvaluator {
        private Environment current;
        private Closure callable;

        public Closure getCallable() {
            return callable;
        }

        Object execute() {
            if(callable!=null) {
                return callable.call();
            }
            return null;
        }

        private EnvironmentBlockEvaluator(Environment e) {
            this.current = e;
        }

        void environments(Closure callable) {
            if(callable!=null) {
                callable.setDelegate(this);
                callable.setResolveStrategy(Closure.DELEGATE_FIRST);
                callable.call();
            }
        }
        void production(Closure callable) {
            if(current == Environment.PRODUCTION) {
                 this.callable = callable;
            }
        }
        void development(Closure callable) {
            if(current == Environment.DEVELOPMENT) {
                 this.callable = callable;
            }
        }
        void test(Closure callable) {
            if(current == Environment.TEST) {
                 this.callable = callable;
            }
        }

        Object methodMissing(String name, Object[] args) {
            if(args != null && args.length > 0 && (args[0] instanceof Closure)) {
                if(current == Environment.CUSTOM && current.getName().equals(name)) {
                    this.callable = (Closure) args[0];
                }
                return null;
            }
            else {
                throw new MissingMethodException(name, Environment.class, args);
            }

        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }


    private String name;

    /**
     * @return The name of the environment 
     */
    public String getName() {
        if(name == null) {
            return this.toString().toLowerCase(Locale.getDefault());
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

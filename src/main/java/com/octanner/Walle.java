package com.octanner;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class Walle
{
    private static final String PREFIX = "WALL-E: ";
    private static final String WARN = "warn";
    private static final String VERBOSE = "verbose";

    public static final String OCT_VAULT_DB_CA_LOCATION = "OCT_VAULT_DB_CA_LOCATION";
    public static final String OCT_VAULT_DB_LANG_LOCATION = "OCT_VAULT_DB_LANG_LOCATION";
    public static final String OCT_VAULT_DB_NIMBUS_LOCATION = "OCT_VAULT_DB_NIMBUS_LOCATION";
    public static final String OCT_VAULT_DB_ORCH_LOCATION = "OCT_VAULT_DB_ORCH_LOCATION";
    public static final String OCT_VAULT_DB_REPORTS_LOCATION = "OCT_VAULT_DB_REPORTS_LOCATION";
    public static final String OCT_VAULT_QUEUE_XICA_LOCATION = "OCT_VAULT_QUEUE_XICA_LOCATION";
    public static final String OCT_VAULT_SAP_CA_LOCATION = "OCT_VAULT_SAP_CA_LOCATION";

    public static final String OCTPROPERTIES_PROPERTIES = "OCTProperties.properties";
    public static final String DATABASE_NAME = "DatabaseName";

    private static Map<String, Map<String, String>> environments = new HashMap<String, Map<String,String>>() {{
        put("tqa1",
                map(
                        OCT_VAULT_DB_CA_LOCATION, "tqa1",
                        OCT_VAULT_DB_LANG_LOCATION, "lqa1",
                        OCT_VAULT_DB_NIMBUS_LOCATION,"tqa1",
                        OCT_VAULT_DB_ORCH_LOCATION,"opsqa",
                        OCT_VAULT_DB_REPORTS_LOCATION,"rptqa1",
                        OCT_VAULT_QUEUE_XICA_LOCATION, "salpi1",
                        OCT_VAULT_SAP_CA_LOCATION, "salqa1"
                ));
        put("tdv1",
                map(
                        OCT_VAULT_DB_CA_LOCATION, "tdv1",
                        OCT_VAULT_DB_LANG_LOCATION, "ldv1",
                        OCT_VAULT_DB_NIMBUS_LOCATION,"tdv1",
                        OCT_VAULT_DB_ORCH_LOCATION,"opsdev",
                        OCT_VAULT_DB_REPORTS_LOCATION,"rptdv1",
                        OCT_VAULT_QUEUE_XICA_LOCATION, "sdlpi1",
                        OCT_VAULT_SAP_CA_LOCATION, "sdldv1"
                ));
        put("tlit",
                map(
                        OCT_VAULT_DB_CA_LOCATION, "tlit",
                        OCT_VAULT_DB_LANG_LOCATION, "lprod",
                        OCT_VAULT_DB_NIMBUS_LOCATION,"tlit",
                        OCT_VAULT_DB_ORCH_LOCATION,"opsprod",
                        OCT_VAULT_DB_REPORTS_LOCATION,"rptprod",
                        OCT_VAULT_QUEUE_XICA_LOCATION, "splpi1",
                        OCT_VAULT_SAP_CA_LOCATION, "slaoe"
                ));
    }};

    public static void premain(String options, Instrumentation instrumentation){
        Walle walle = new Walle();
        System.out.println(PREFIX + "I'm on the job!");
        String propertyEnv = walle.getPropertyEnv();

        System.out.println(PREFIX + "You have the " + propertyEnv + " environment config jar");

        if (optEnabled(options, VERBOSE)) {
            printEnvironment();
        }

        boolean match = true;
        for (Map.Entry<String, String> entry : environments.get(propertyEnv).entrySet()) {
            match &= environmentContains(entry);
        }

        if (match == false && !optEnabled(options, WARN) ) {
            System.err.println(PREFIX + "Shutting down...");
            System.exit(1);
        }
    }

    private String getPropertyEnv() {
        String propertyEnv = getEnvFromProperty(getClass().getClassLoader());
        if (propertyEnv == null) {
            try {
                propertyEnv = getEnvFromProperty(createTomcatConfigClassLoader());
            } catch (MalformedURLException e ) {
                e.printStackTrace();
            }
        }
        return propertyEnv;
    }

    private static boolean optEnabled(final String options, final String substring) {
        return options != null && options.toLowerCase().contains(substring);
    }

    private String getEnvFromProperty(ClassLoader classLoader) {
        Properties props = new Properties();
        InputStream input = classLoader.getResourceAsStream(OCTPROPERTIES_PROPERTIES);
        try {
            props.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
        }
        return props.getProperty(DATABASE_NAME) == null ?
                null :
                props.getProperty(DATABASE_NAME).toLowerCase();
    }

    private ClassLoader createTomcatConfigClassLoader() throws MalformedURLException {
        String currentDirectory = Paths.get("").toFile().getAbsolutePath();

        Path libFolder = Paths.get("..", "lib");
        System.out.println(PREFIX + "Looking for 'configuration' files under " + libFolder.toFile().getAbsolutePath());
        String[] configJars = libFolder.toFile().list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.contains("configuration");
            }
        });

        URL[] configJarUrls = new URL[configJars.length];
        for (int i = 0; i < configJars.length; i++) {
            URL url = new URL("file:///" + currentDirectory + "/../lib/" + configJars[i]);
            System.out.println(PREFIX + "loading " + url);
            configJarUrls[i] = url;
        }
        ClassLoader classLoader = new URLClassLoader(configJarUrls, getClass().getClassLoader());
        return classLoader;
    }

    private static Map<String, String> map(final String ... entries) {
        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < entries.length; i+=2) {
            map.put(entries[i], entries[i+1]);
        }
        return map;
    }

    private static boolean environmentContains(final Map.Entry<String, String> entry) {
        String envValue = System.getenv(entry.getKey());

        boolean contains = false;
        if (envValue != null && envValue.length() > 0) {
            contains = envValue.contains(entry.getValue());
        }
        if (!contains) {
            System.out.println(PREFIX+"Expected " +  entry.getKey() +
                    " to contain [" + entry.getValue() + "], but the value is [" + envValue + "]");
        }

        return contains;
    }

    private static void printEnvironment() {
        Map<String, String> properties = System.getenv();
        List<String> keys = new ArrayList<String>();
        keys.addAll(properties.keySet());

        Collections.sort(keys);

        for (Object key : keys) {
            System.out.println(PREFIX + key + " : " + properties.get(key));
        }
    }
}

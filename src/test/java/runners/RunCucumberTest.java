package runners;

import org.junit.platform.suite.api.*;
import static io.cucumber.junit.platform.engine.Constants.*;

@Suite
@IncludeEngines("cucumber")
@SelectPackages("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "steps")
@ConfigurationParameter(key = FEATURES_PROPERTY_NAME,
        value = "src/test/resources/features")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
        value = "pretty, json:target/cucumber-reports/cucumber.json, html:target/cucumber-reports/cucumber.html")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@todo")
public class RunCucumberTest { }
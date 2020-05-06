import com.sap.piper.ConfigurationLoader
import com.sap.cloud.sdk.s4hana.pipeline.Analytics

def call(Map parameters) {
    Script script = parameters.script

    checkNotUsingWhiteSourceOrgToken(script)
    checkRenamedMavenStep(script)
    checkMavenGlobalSettings(script)
    checkCloudfoundryDeployment(script)
    checkNeoDeployment(script)
    checkRenamedBackendIntegrationTests(script)
    convertDebugReportConfig(script)
    checkStaticCodeChecksConfig(script)
}

void checkRenamedBackendIntegrationTests(Script script) {
    checkRenamedStage(script, 'integrationTests', 'backendIntegrationTests')
}

void checkRenamedMavenStep(Script script) {
    checkRenamedStep(script, 'executeMaven', 'mavenExecute')
}

void checkMavenGlobalSettings(Script script) {
    Map mavenConfiguration = ConfigurationLoader.stepConfiguration(script, 'mavenExecute')

    // Maven globalSettings obsolete since introduction of DL-Cache
    if (mavenConfiguration?.globalSettingsFile) {
        failWithConfigError("Your pipeline configuration contains the obsolete configuration parameter " +
            "'mavenExecute.globalSettingsFile=${mavenConfiguration.globalSettingsFile}'. " +
            "The SAP Cloud SDK Pipeline uses an own global settings file to inject its download proxy as maven repository mirror. " +
            "Please only specify the settings via the parameter 'projectSettingsFile'")
    }
}

void checkNotUsingWhiteSourceOrgToken(Script script) {
    Map stageConfig = ConfigurationLoader.stageConfiguration(script, 'whitesourceScan')
    if (stageConfig?.orgToken) {
        failWithConfigError("Your pipeline configuration may not use 'orgtoken' in whiteSourceScan stage. " +
            "Store it as a 'Secret Text' in Jenkins and use the 'credentialsId' field instead.")
    }
}

void checkCloudfoundryDeployment(Script script) {
    checkRenamedStep(script, 'deployToCfWithCli', 'cloudFoundryDeploy')
}

void checkNeoDeployment(Script script) {
    checkRenamedStep(script, 'deployToNeoWithCli', 'neoDeploy')
}

boolean convertDebugReportConfig(Script script) {
    if (!ConfigurationLoader.postActionConfiguration(script, 'archiveDebugLog'))
        return

    failWithConfigError("The configuration key archiveDebugLog in the postAction configuration may not be used anymore. " +
        "Please use thek step configuration for debugReportArchive instead.")
}

void checkStaticCodeChecksConfig(Script script) {
    if (ConfigurationLoader.stageConfiguration(script, 'staticCodeChecks')) {
        failWithConfigError("You pipeline configuration contains an entry for the stage staticCodeChecks. " +
            "This configuration option was removed in version v32. " +
            "Please migrate the configuration into your pom.xml file or the configuration for the new step mavenExecuteStaticCodeChecks. " +
            "Details can be found in the release notes as well as in the step documentation: https://sap.github.io/jenkins-library/steps/mavenExecuteStaticCodeChecks/.")
    }
}

private checkRenamedStep(Script script, String oldName, String newName) {
    if (ConfigurationLoader.stepConfiguration(script, oldName)) {
        failWithConfigError("The configuration key ${oldName} in the steps configuration may not be used anymore. " +
            "Please use ${newName} instead.")
    }
}

private checkRenamedStage(Script script, String oldName, String newName) {
    if (ConfigurationLoader.stageConfiguration(script, oldName)){
        failWithConfigError("The configuration key ${oldName} in the stages configuration may not be used anymore. " +
            "Please use ${newName} instead. " +
            "For more information please visit https://github.com/SAP/cloud-s4-sdk-pipeline/blob/master/configuration.md")
    }
}

private failWithConfigError(String errorMessage) {
    Analytics.instance.legacyConfig(true)
    error(errorMessage)
}

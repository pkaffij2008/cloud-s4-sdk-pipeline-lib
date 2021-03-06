import static com.sap.cloud.sdk.s4hana.pipeline.EnvironmentAssertionUtils.assertPluginIsActive

def call(Map parameters) {
    Script script = parameters.script
    if (!env.BRANCH_NAME) {

        String message = "Please use Multibranch Pipeline Job. \n" +
            "The pipeline is designed for the Multibranch Pipeline job type. It appears that it is running in a Singlebranch Pipeline. \n" +
            "Please refer to 'https://jenkins.io/doc/book/pipeline/multibranch/#creating-a-multibranch-pipeline' for information on how to setup a Multibranch Pipeline. \n"

        assertPluginIsActive('badge')
        addBadge(icon: "warning.gif", text: message)
        createSummary(icon: "warning.gif", text: "<h2>Please use multi branch pipeline</h2>\n" + message)
    }
}

package com.blackducksoftware.tools.testhubclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.restlet.Context;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.resource.ClientResource;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.tools.testhubclient.model.component.ComponentVersion;
import com.blackducksoftware.tools.testhubclient.model.component.VulnerableComponentItem;
import com.blackducksoftware.tools.testhubclient.model.notification.ComponentVersionStatus;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.VulnerabilityNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.policy.PolicyRule;
import com.blackducksoftware.tools.testhubclient.model.policy.PolicyStatus;
import com.blackducksoftware.tools.testhubclient.model.projectversion.ProjectVersion;
import com.blackducksoftware.tools.testhubclient.model.projectversion.ProjectVersionItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HubCommonClient {
    private String baseUrl;
    private HubIntRestService svc;

    public static void main(String[] args) throws Exception {
	HubCommonClient client = new HubCommonClient();
	client.run();
    }

    private void run() throws Exception {

	baseUrl = "http://eng-hub-valid03.dc1.lan";
	String username = "sysadmin";
	String password = "blackduck";

	login(baseUrl, username, password);

	String hubVersion = svc.getHubVersion();
	System.out.println("Hub version: " + hubVersion);

	processOneNotifOfEachType(baseUrl, "2016-05-01T20:00:00.000Z",
		"2016-05-06T20:00:00.000Z", 1000);

    }

    private String processOneNotifOfEachType(String hubUrl, String startDate,
	    String endDate, int limit) throws Exception {

	final ClientResource resource = createClientResourceForGetNotifications(
		startDate, endDate, limit);
	System.out.println("Resource: " + resource);
	int responseCode = resource.getResponse().getStatus().getCode();

	if (responseCode == 200 || responseCode == 204 || responseCode == 202) {
	    final String response = readResponseAsString(resource.getResponse());

	    Gson gson = new GsonBuilder().create();
	    JsonParser parser = new JsonParser();
	    JsonObject json = parser.parse(response).getAsJsonObject();
	    JsonArray array = json.get("items").getAsJsonArray();

	    boolean doneProcessingVulnNotifs = false;
	    boolean doneProcessingRuleViolationNotifs = false;
	    boolean doneProcessingPolicyOverrideNotifs = false;
	    for (JsonElement elem : array) {
		NotificationItem genericNotif = gson.fromJson(elem,
			NotificationItem.class);
		// System.out.println("NotificationItem: " + genericNotif);
		if ("VULNERABILITY".equals(genericNotif.getType())) {
		    getVulnerabilityNotificationItem(gson, elem,
			    !doneProcessingVulnNotifs);
		    // doneProcessingVulnNotifs = true;
		} else if ("RULE_VIOLATION".equals(genericNotif.getType())) {
		    getRuleViolationNotificationItem(gson, elem,
			    !doneProcessingRuleViolationNotifs);
		    // doneProcessingRuleViolationNotifs = true;

		} else if ("POLICY_OVERRIDE".equals(genericNotif.getType())) {
		    getPolicyOverrideNotificationItem(gson, elem,
			    !doneProcessingPolicyOverrideNotifs);
		    // doneProcessingPolicyOverrideNotifs = true;
		} else {
		    System.out
			    .println("I don't know how to parse/print this type: "
				    + genericNotif.getType()
				    + ": "
				    + genericNotif);
		}
	    }

	    final Response resp = resource.getResponse();
	    return resp.getEntityAsText();
	} else {
	    throw new BDRestException(
		    "There was a problem getting notifications. Error Code: "
			    + responseCode, resource);
	}
    }

    private ClientResource createClientResourceForGetNotifications(
	    String startDate, String endDate, int limit)
	    throws URISyntaxException {
	final ClientResource resource = svc.createClientResource();
	resource.addSegment("api");
	resource.addSegment("notifications");
	resource.addQueryParameter("startDate", startDate);
	resource.addQueryParameter("endDate", endDate);
	resource.addQueryParameter("limit", String.valueOf(limit));
	resource.setMethod(Method.GET);
	resource.handle();
	return resource;
    }

    private void getVulnerabilityNotificationItem(Gson gson, JsonElement elem,
	    boolean processIt) throws IOException, BDRestException,
	    URISyntaxException {
	VulnerabilityNotificationItem vulnNotif = gson.fromJson(elem,
		VulnerabilityNotificationItem.class);
	if (processIt) {
	    System.out.println("======\n" + vulnNotif + "\n-----");
	    processVulnerabilityNotification(vulnNotif);
	}
    }

    private void getRuleViolationNotificationItem(Gson gson, JsonElement elem,
	    boolean processIt) throws Exception {
	RuleViolationNotificationItem ruleViolationNotif = gson.fromJson(elem,
		RuleViolationNotificationItem.class);
	if (processIt) {
	    System.out.println("======\n" + ruleViolationNotif + "\n-----");
	    processRuleViolationNotification(ruleViolationNotif);
	}
    }

    private void getPolicyOverrideNotificationItem(Gson gson, JsonElement elem,
	    boolean processIt) {
	PolicyOverrideNotificationItem policyOverrideNotif = gson.fromJson(
		elem, PolicyOverrideNotificationItem.class);
	if (processIt) {
	    System.out.println("======\n" + policyOverrideNotif + "\n-----");
	}
    }

    private void processRuleViolationNotification(
	    RuleViolationNotificationItem ruleViolationNotif) throws Exception {
	String projectName = ruleViolationNotif.getContent().getProjectName();
	String projectVersionName = ruleViolationNotif.getContent()
		.getProjectVersionName();
	String componentVersionsInViolation = ruleViolationNotif.getContent()
		.getComponentVersionsInViolation();
	List<ComponentVersionStatus> compStatuses = ruleViolationNotif
		.getContent().getComponentVersionStatuses();
	for (ComponentVersionStatus compStatus : compStatuses) {
	    System.out.println("######################### " + compStatus);
	    String componentName = compStatus.getComponentName();
	    String componentVersion = getComponentVersionNameFromLink(compStatus
		    .getComponentVersion());
	    String policyStatusLink = compStatus
		    .getBomComponentVersionPolicyStatus();
	    // System.out.println("*** policyStatus: " + policyStatusLink);
	    processPolicyViolation(policyStatusLink, projectName,
		    projectVersionName, componentName, componentVersion);
	}
	// System.out.println("*** TBD: Creating ticket for project "
	// + projectName + " / " + projectVersionName
	// + ": Component versions " + componentVersionsInViolation
	// + " violate policy");
    }

    private String getComponentVersionNameFromLink(String componentVersionLink)
	    throws Exception {
	if (componentVersionLink == null) {
	    return "<null>";
	}
	final ClientResource resource = createGetClientResourceWithGivenLink(componentVersionLink);
	System.out.println("Resource: " + resource);
	int responseCode = resource.getResponse().getStatus().getCode();
	if (responseCode == 200 || responseCode == 204 || responseCode == 202) {
	    System.out.println("SUCCESS getting projectVersion");
	    final String response = readResponseAsString(resource.getResponse());

	    Gson gson = new GsonBuilder().create();
	    JsonParser parser = new JsonParser();
	    JsonObject json = parser.parse(response).getAsJsonObject();

	    ComponentVersion componentVersion = gson.fromJson(json,
		    ComponentVersion.class);
	    return componentVersion.getVersionName();
	}
	throw new Exception("Error getting component version name");
    }

    private void processPolicyViolation(String policyStatusLink,
	    String projectName, String projectVersion, String componentName,
	    String componentVersion) throws IOException, URISyntaxException {
	final ClientResource resource = createGetClientResourceWithGivenLink(policyStatusLink);
	System.out.println("Resource: " + resource);
	int responseCode = resource.getResponse().getStatus().getCode();
	if (responseCode == 200 || responseCode == 204 || responseCode == 202) {
	    System.out.println("SUCCESS getting projectVersion");
	    final String response = readResponseAsString(resource.getResponse());

	    Gson gson = new GsonBuilder().create();
	    JsonParser parser = new JsonParser();
	    JsonObject json = parser.parse(response).getAsJsonObject();

	    PolicyStatus policyStatus = gson.fromJson(json, PolicyStatus.class);
	    String policyLink = policyStatus.getLink("policy-rule");
	    processPolicy(policyLink, projectName, projectVersion,
		    componentName, componentVersion);
	}
    }

    private void processPolicy(String policyLink, String projectName,
	    String projectVersion, String componentName, String componentVersion)
	    throws IOException, URISyntaxException {
	final ClientResource resource = createGetClientResourceWithGivenLink(policyLink);
	System.out.println("Resource: " + resource);
	int responseCode = resource.getResponse().getStatus().getCode();
	if (responseCode == 200 || responseCode == 204 || responseCode == 202) {
	    System.out.println("SUCCESS getting projectVersion");
	    final String response = readResponseAsString(resource.getResponse());

	    Gson gson = new GsonBuilder().create();
	    JsonParser parser = new JsonParser();
	    JsonObject json = parser.parse(response).getAsJsonObject();
	    PolicyRule policyRule = gson.fromJson(json, PolicyRule.class);
	    String policyRuleName = policyRule.getName();
	    System.out
		    .println("*** POLICY VIOLATION: Creating ticket for project "
			    + projectName
			    + " / "
			    + projectVersion

			    + " component "
			    + componentName
			    + " / "
			    + componentVersion
			    + " violates policy rule "
			    + policyRuleName);

	}
    }

    private void processVulnerabilityNotification(
	    VulnerabilityNotificationItem vulnNotif) throws IOException,
	    BDRestException, URISyntaxException {
	System.out.println("processVulnerabilityNotification()");

	for (ProjectVersion affectedProjectVersion : vulnNotif.getContent()
		.getAffectedProjectVersions()) {
	    String projectName = affectedProjectVersion.getProjectName();
	    String projectVersionName = affectedProjectVersion
		    .getProjectVersionName();
	    String componentName = vulnNotif.getContent().getComponentName();
	    String componentVersion = vulnNotif.getContent().getVersionName();

	    String projectVersionLink = affectedProjectVersion
		    .getProjectVersion();
	    processProjectVersionLink(projectVersionLink, projectName,
		    componentName, componentVersion);
	}
    }

    private void processProjectVersionLink(String projectVersionLink,
	    String projectName, String targetComponentName,
	    String targetComponentVersion) throws URISyntaxException,
	    IOException {
	final ClientResource resource = createGetClientResourceWithGivenLink(projectVersionLink);
	System.out.println("Resource: " + resource);
	int responseCode = resource.getResponse().getStatus().getCode();
	if (responseCode == 200 || responseCode == 204 || responseCode == 202) {
	    System.out.println("SUCCESS getting projectVersion");
	    final String response = readResponseAsString(resource.getResponse());

	    Gson gson = new GsonBuilder().create();
	    JsonParser parser = new JsonParser();
	    JsonObject json = parser.parse(response).getAsJsonObject();

	    ProjectVersionItem versionItem = gson.fromJson(json,
		    ProjectVersionItem.class);

	    String vulnerableComponentsLink = versionItem
		    .getLink("vulnerable-components");
	    System.out.println("Link to vulnerable components: "
		    + vulnerableComponentsLink);
	    processVulnerableComponentsLink(vulnerableComponentsLink,
		    projectName, versionItem.getVersionName(),
		    targetComponentName, targetComponentVersion);
	}
    }

    // private void processProjectVersionsLink(String versionsLink,
    // String projectName) throws URISyntaxException, IOException {
    //
    // final ClientResource resource =
    // createGetClientResourceWithGivenLink(versionsLink);
    // System.out.println("Resource: " + resource);
    // int responseCode = resource.getResponse().getStatus().getCode();
    // if (responseCode == 200 || responseCode == 204 || responseCode == 202) {
    // System.out.println("SUCCESS getting versions");
    // final String response = readResponseAsString(resource.getResponse());
    //
    // Gson gson = new GsonBuilder().create();
    // JsonParser parser = new JsonParser();
    // JsonObject json = parser.parse(response).getAsJsonObject();
    // JsonArray array = json.get("items").getAsJsonArray();
    // System.out.println("Got " + array.size() + " versions");
    // for (JsonElement elem : array) {
    // ProjectVersionItem versionItem = gson.fromJson(elem,
    // ProjectVersionItem.class);
    // System.out.println(versionItem);
    // String vulnerableComponentsLink = versionItem
    // .getLink("vulnerable-components");
    // System.out.println("Link to vulnerable components: "
    // + vulnerableComponentsLink);
    // processVulnerableComponentsLink(vulnerableComponentsLink,
    // projectName, versionItem.getVersionName());
    // }
    // }
    // }

    private void processVulnerableComponentsLink(
	    String vulnerableComponentsLink, String projectName,
	    String projectVersionName, String targetComponentName,
	    String targetComponentVersion) throws URISyntaxException,
	    IOException {
	final ClientResource resource = createGetClientResourceWithGivenLink(vulnerableComponentsLink);
	System.out.println("Resource: " + resource);
	int responseCode = resource.getResponse().getStatus().getCode();
	if (responseCode == 200 || responseCode == 204 || responseCode == 202) {
	    System.out.println("SUCCESS getting vulnerableComponents");
	    final String response = readResponseAsString(resource.getResponse());
	    Gson gson = new GsonBuilder().create();
	    JsonParser parser = new JsonParser();
	    JsonObject json = parser.parse(response).getAsJsonObject();
	    JsonArray array = json.get("items").getAsJsonArray();
	    System.out.println("Got " + array.size() + " vulnerableComponents");
	    for (JsonElement elem : array) {
		VulnerableComponentItem vulnerableComponentItem = gson
			.fromJson(elem, VulnerableComponentItem.class);
		System.out.println(vulnerableComponentItem);

		if (!vulnerableComponentItem.getComponentName().equals(
			targetComponentName)) {
		    System.out
			    .println("Wrong component name; skipping this one");
		    continue;
		}
		if (!vulnerableComponentItem.getComponentVersionName().equals(
			targetComponentVersion)) {
		    System.out
			    .println("Wrong component version; skipping this one");
		    continue;
		}
		if ("REMEDIATION_REQUIRED".equals(vulnerableComponentItem
			.getVulnerabilityWithRemediation()
			.getRemediationStatus())) {
		    System.out
			    .println("*** REMEDIATION REQUIRED: Creating ticket for project "
				    + projectName
				    + " / "
				    + projectVersionName
				    + ": Vulnerability "
				    + vulnerableComponentItem
					    .getVulnerabilityWithRemediation()
					    .getVulnerabilityName()
				    + " on component "
				    + targetComponentName
				    + " / "
				    + targetComponentVersion
				    + " needs remediating");
		}
		if ("NEEDS_REVIEW".equals(vulnerableComponentItem
			.getVulnerabilityWithRemediation()
			.getRemediationStatus())) {
		    System.out
			    .println("*** NEEDS REVIEW: Creating ticket for project "
				    + projectName
				    + " / "
				    + projectVersionName
				    + ": Vulnerability "
				    + vulnerableComponentItem
					    .getVulnerabilityWithRemediation()
					    .getVulnerabilityName()
				    + " on component "
				    + targetComponentName
				    + " / "
				    + targetComponentVersion
				    + " needs review");
		}
	    }
	}
    }

    private ClientResource createGetClientResourceWithGivenLink(String givenLink)
	    throws URISyntaxException {
	final ClientResource resource = createClientResourceWithGivenLink(givenLink);

	resource.setMethod(Method.GET);
	resource.handle();
	return resource;
    }

    private ClientResource createClientResourceWithGivenLink(String givenLink)
	    throws URISyntaxException {

	final Context context = new Context();

	// the socketTimeout parameter is used in the httpClient extension that
	// we do not use
	// We can probably remove this parameter
	final String stringTimeout = String.valueOf(120000);

	context.getParameters().add("socketTimeout", stringTimeout);

	context.getParameters().add("socketConnectTimeoutMs", stringTimeout);
	context.getParameters().add("readTimeout", stringTimeout);
	// Should throw timeout exception after the specified timeout, default
	// is 2 minutes

	final ClientResource resource = new ClientResource(context, new URI(
		givenLink));
	resource.getRequest().setCookies(svc.getCookies());
	return resource;
    }

    private String readResponseAsString(final Response response)
	    throws IOException {
	final StringBuilder sb = new StringBuilder();
	final Reader reader = response.getEntity().getReader();
	final BufferedReader bufReader = new BufferedReader(reader);
	try {
	    String line;
	    while ((line = bufReader.readLine()) != null) {
		sb.append(line);
		sb.append("\n");
	    }
	} finally {
	    bufReader.close();
	}
	return sb.toString();
    }

    private void login(String hubUrl, String username, String password)
	    throws HubIntegrationException, URISyntaxException, BDRestException {
	svc = new HubIntRestService(hubUrl);
	svc.setCookies(username, password);

    }
}

package com.blackducksoftware.tools.testhubclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.Context;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.resource.ClientResource;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.project.api.ProjectItem;
import com.blackducksoftware.tools.testhubclient.model.component.VulnerableComponentItem;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.VulnerabilityNotificationItem;
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

    public static void main(String[] args) throws HubIntegrationException,
	    URISyntaxException, BDRestException, IOException {
	HubCommonClient client = new HubCommonClient();
	client.run();
    }

    private void run() throws HubIntegrationException, URISyntaxException,
	    BDRestException, IOException {

	baseUrl = "http://eng-hub-valid03.dc1.lan";
	String username = "sbillings";
	String password = "blackduck";

	login(baseUrl, username, password);

	String hubVersion = svc.getHubVersion();
	System.out.println("Hub version: " + hubVersion);

	processOneNotifOfEachType(baseUrl, "2015-01-01T00:00:00.000Z",
		"2016-05-01T20:00:00.000Z", 1000);

    }

    private String processOneNotifOfEachType(String hubUrl, String startDate,
	    String endDate, int limit) throws URISyntaxException,
	    BDRestException, IOException {

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

	    boolean haveProcessedVulnNotif = false;
	    boolean haveProcessedRuleViolationNotif = false;
	    boolean haveProcessedPolicyOverrideNotif = false;
	    for (JsonElement elem : array) {
		NotificationItem genericNotif = gson.fromJson(elem,
			NotificationItem.class);
		// System.out.println("NotificationItem: " + genericNotif);
		if ("VULNERABILITY".equals(genericNotif.getType())) {
		    getVulnerabilityNotificationItem(gson, elem,
			    !haveProcessedVulnNotif);
		    haveProcessedVulnNotif = true;
		} else if ("RULE_VIOLATION".equals(genericNotif.getType())) {
		    getRuleViolationNotificationItem(gson, elem,
			    !haveProcessedRuleViolationNotif);
		    haveProcessedRuleViolationNotif = true;

		} else if ("POLICY_OVERRIDE".equals(genericNotif.getType())) {
		    getPolicyOverrideNotificationItem(gson, elem,
			    !haveProcessedPolicyOverrideNotif);
		    haveProcessedPolicyOverrideNotif = true;
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
	    System.out.println(vulnNotif);
	    processVulnerabilityNotification(vulnNotif);
	}
    }

    private void getRuleViolationNotificationItem(Gson gson, JsonElement elem,
	    boolean processIt) {
	RuleViolationNotificationItem ruleViolationNotif = gson.fromJson(elem,
		RuleViolationNotificationItem.class);
	if (processIt) {
	    System.out.println(ruleViolationNotif);
	}
    }

    private void getPolicyOverrideNotificationItem(Gson gson, JsonElement elem,
	    boolean processIt) {
	PolicyOverrideNotificationItem policyOverrideNotif = gson.fromJson(
		elem, PolicyOverrideNotificationItem.class);
	if (processIt) {
	    System.out.println(policyOverrideNotif);
	}
    }

    private void processVulnerabilityNotification(
	    VulnerabilityNotificationItem vulnNotif) throws IOException,
	    BDRestException, URISyntaxException {
	System.out.println("processVulnerabilityNotification()");
	List<String> projects = new ArrayList<>();
	List<ProjectItem> projectItems = svc.getProjectMatches("");
	System.out.println("Got " + projectItems.size() + " projects");
	for (ProjectItem projectItem : projectItems) {
	    String versionsLink = projectItem.getLink("versions");
	    System.out.println("versionsLink: " + versionsLink);
	    processProjectVersionsLink(versionsLink, projectItem.getName());
	}
    }

    private void processProjectVersionsLink(String versionsLink,
	    String projectName) throws URISyntaxException, IOException {

	final ClientResource resource = createGetClientResourceWithGivenLink(versionsLink);
	System.out.println("Resource: " + resource);
	int responseCode = resource.getResponse().getStatus().getCode();
	if (responseCode == 200 || responseCode == 204 || responseCode == 202) {
	    System.out.println("SUCCESS getting versions");
	    final String response = readResponseAsString(resource.getResponse());

	    Gson gson = new GsonBuilder().create();
	    JsonParser parser = new JsonParser();
	    JsonObject json = parser.parse(response).getAsJsonObject();
	    JsonArray array = json.get("items").getAsJsonArray();
	    System.out.println("Got " + array.size() + " versions");
	    for (JsonElement elem : array) {
		ProjectVersionItem versionItem = gson.fromJson(elem,
			ProjectVersionItem.class);
		System.out.println(versionItem);
		String vulnerableComponentsLink = versionItem
			.getLink("vulnerable-components");
		System.out.println("Link to vulnerable components: "
			+ vulnerableComponentsLink);
		processVulnerableComponentsLink(vulnerableComponentsLink,
			projectName, versionItem.getVersionName());
	    }
	}
    }

    private void processVulnerableComponentsLink(
	    String vulnerableComponentsLink, String projectName,
	    String projectVersionName) throws URISyntaxException, IOException {
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

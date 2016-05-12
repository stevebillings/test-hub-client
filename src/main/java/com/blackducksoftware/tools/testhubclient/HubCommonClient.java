package com.blackducksoftware.tools.testhubclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.restlet.Response;

import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.tools.testhubclient.dao.NotificationDao;
import com.blackducksoftware.tools.testhubclient.dao.NotificationDaoException;
import com.blackducksoftware.tools.testhubclient.dao.hub.HubNotificationDao;
import com.blackducksoftware.tools.testhubclient.model.NameValuePair;
import com.blackducksoftware.tools.testhubclient.model.component.ComponentVersion;
import com.blackducksoftware.tools.testhubclient.model.component.VulnerableComponentItem;
import com.blackducksoftware.tools.testhubclient.model.notification.ComponentVersionStatus;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationResponse;
import com.blackducksoftware.tools.testhubclient.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.VulnerabilityNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.VulnerableComponentsResponse;
import com.blackducksoftware.tools.testhubclient.model.policy.ApprovalStatus;
import com.blackducksoftware.tools.testhubclient.model.policy.PolicyRule;
import com.blackducksoftware.tools.testhubclient.model.policy.PolicyStatus;
import com.blackducksoftware.tools.testhubclient.model.projectversion.ProjectVersion;
import com.blackducksoftware.tools.testhubclient.model.projectversion.ProjectVersionItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class HubCommonClient {

    public static void main(String[] args) throws Exception {
	HubCommonClient client = new HubCommonClient();
	client.run("http://eng-hub-valid03.dc1.lan",
		"2016-05-01T00:00:00.000Z", "2016-07-30T00:00:00.000Z", 1000);
    }

    private ClientLogger log;
    private NotificationDao dao;

    private Map<Integer, JiraTicket> tickets = new HashMap<>();

    private int duplicateCount = 0;
    private int ticketCount = 0;

    public Statistics run(String hubUrl, String startDate, String endDate,
	    int limit) throws Exception {
	log = new ClientLogger();
	log.info("Starting up...");

	String username = "sysadmin";
	String password = "blackduck";

	dao = new HubNotificationDao(hubUrl, username, password);

	String hubVersion = dao.getVersion();
	log.info("Hub version: " + hubVersion);

	int notificationCount = processNotifications(dao, startDate, endDate,
		limit);

	log.info("Done processsing " + notificationCount
		+ " notifications, generating " + ticketCount + " tickets, "
		+ duplicateCount + " of which were duplicates");

	return new Statistics(notificationCount, ticketCount, duplicateCount);

    }

    private int processNotifications(NotificationDao dao, String startDate,
	    String endDate, int limit) throws Exception {

	List<String> urlSegments = new ArrayList<>();
	urlSegments.add("api");
	urlSegments.add("notifications");

	Set<NameValuePair> queryParameters = new HashSet<>();
	queryParameters.add(new NameValuePair("startDate", startDate));
	queryParameters.add(new NameValuePair("endDate", endDate));
	queryParameters.add(new NameValuePair("limit", String.valueOf(limit)));
	NotificationResponse notifResponse = dao.getFromRelativeUrl(
		NotificationResponse.class, urlSegments, queryParameters);

	boolean doneProcessingVulnNotifs = false;
	boolean doneProcessingRuleViolationNotifs = false;
	boolean doneProcessingPolicyOverrideNotifs = false;

	Gson gson = new GsonBuilder().create(); // TODO re-usable?
	JsonObject jsonObject = notifResponse.getJsonObject();
	JsonArray array = jsonObject.get("items").getAsJsonArray();

	for (JsonElement elem : array) {
	    NotificationItem genericNotif = gson.fromJson(elem,
		    NotificationItem.class);
	    String notificationTimeStamp = genericNotif.getCreatedAt();
	    log.info("\n\n======================================================================\n"
		    + "NotificationItem: " + genericNotif);
	    if ("VULNERABILITY".equals(genericNotif.getType())) {
		getVulnerabilityNotificationItem(notificationTimeStamp, gson,
			elem, !doneProcessingVulnNotifs);
		// doneProcessingVulnNotifs = true;
	    } else if ("RULE_VIOLATION".equals(genericNotif.getType())) {
		getRuleViolationNotificationItem(notificationTimeStamp, gson,
			elem, !doneProcessingRuleViolationNotifs);
		// doneProcessingRuleViolationNotifs = true;

	    } else if ("POLICY_OVERRIDE".equals(genericNotif.getType())) {
		getPolicyOverrideNotificationItem(notificationTimeStamp, gson,
			elem, !doneProcessingPolicyOverrideNotifs);
		// doneProcessingPolicyOverrideNotifs = true;
	    } else {
		log.error("I don't know how to parse/print this type: "
			+ genericNotif.getType() + ": " + genericNotif);
	    }
	}

	return array.size();
    }

    private void getVulnerabilityNotificationItem(String notificationTimeStamp,
	    Gson gson, JsonElement elem, boolean processIt) throws IOException,
	    BDRestException, URISyntaxException, NotificationDaoException {
	VulnerabilityNotificationItem vulnNotif = gson.fromJson(elem,
		VulnerabilityNotificationItem.class);
	if (processIt) {
	    log.debug("======\n" + vulnNotif + "\n-----");
	    processVulnerabilityNotification(notificationTimeStamp, vulnNotif);
	}
    }

    private void getRuleViolationNotificationItem(String notificationTimeStamp,
	    Gson gson, JsonElement elem, boolean processIt) throws Exception {
	RuleViolationNotificationItem ruleViolationNotif = gson.fromJson(elem,
		RuleViolationNotificationItem.class);
	if (processIt) {
	    log.debug("======\n" + ruleViolationNotif + "\n-----");
	    processRuleViolationNotification(notificationTimeStamp,
		    ruleViolationNotif);
	}
    }

    private void getPolicyOverrideNotificationItem(
	    String notificationTimeStamp, Gson gson, JsonElement elem,
	    boolean processIt) throws Exception {
	PolicyOverrideNotificationItem policyOverrideNotif = gson.fromJson(
		elem, PolicyOverrideNotificationItem.class);
	if (processIt) {
	    log.debug("======\n" + policyOverrideNotif + "\n-----");
	    processPolicyOverrideNotification(notificationTimeStamp,
		    policyOverrideNotif);
	}
    }

    private void processPolicyOverrideNotification(
	    String notificationTimeStamp,
	    PolicyOverrideNotificationItem policyOverrideNotif)
	    throws Exception {
	String projectName = policyOverrideNotif.getContent().getProjectName();
	String projectVersionName = policyOverrideNotif.getContent()
		.getProjectVersionName();
	String componentName = policyOverrideNotif.getContent()
		.getComponentName();
	String componentVersion = policyOverrideNotif.getContent()
		.getComponentVersionName();
	// String firstName = policyOverrideNotif.getContent().getFirstName();
	// // always empty
	// String lastName = policyOverrideNotif.getContent().getLastName();
	String compPolicyStatusLink = policyOverrideNotif.getContent()
		.getBomComponentVersionPolicyStatus();
	PolicyStatus compPolicyStatus = getCompPolicyStatusFromLink(compPolicyStatusLink);
	String compPolicyStatusString = "<null>";
	if (compPolicyStatus != null) {
	    compPolicyStatusString = compPolicyStatus.getOverallStatus();
	} else {
	    log.error("Component Policy Status is null");
	}
	log.info("Overall policy status: " + compPolicyStatusString);
	log.info("\tWas updated at: " + compPolicyStatus.getUpdatedAt());
	if (!"IN_VIOLATION_OVERRIDDEN".equals(compPolicyStatusString)) {
	    log.info("No ticket being generated");
	    return;
	}

	JiraTicket jiraTicket = new JiraTicket(notificationTimeStamp,
		JiraTicketType.POLICY_OVERRIDE, projectName,
		projectVersionName, componentName, componentVersion, null,
		null, ActionRequired.REVIEW);
	System.out.println(jiraTicket);
	addToMap(jiraTicket);
    }

    private void addToMap(JiraTicket jiraTicket) {
	ticketCount++;
	if (tickets.containsKey(jiraTicket.hashCode())) {
	    duplicateCount++;
	    System.out.println("\tDuplicate "
		    + jiraTicket.getTicketType().name()
		    + " notification (rule: " + jiraTicket.getRuleName()
		    + "): The notification at \n\t\t"
		    + jiraTicket.getDateCreated()
		    + " is a duplicate of notification at \n\t\t"
		    + tickets.get(jiraTicket.hashCode()).getDateCreated());
	} else {
	    tickets.put(jiraTicket.hashCode(), jiraTicket);
	}
    }

    private PolicyStatus getCompPolicyStatusFromLink(String compPolicyStatusLink)
	    throws Exception {

	if (compPolicyStatusLink == null) {
	    return null;
	}

	PolicyStatus policyStatus = null;
	try {
	    policyStatus = dao.getFromUrl(PolicyStatus.class,
		    compPolicyStatusLink);
	} catch (NotificationDaoException e) {
	    log.warn("Error getting policy status from " + compPolicyStatusLink
		    + ": " + e.getMessage()
		    + "; This component was probably removed from this BOM");
	    return null;
	}

	return policyStatus;
    }

    private void processRuleViolationNotification(String notificationTimeStamp,
	    RuleViolationNotificationItem ruleViolationNotif) throws Exception {
	String projectName = ruleViolationNotif.getContent().getProjectName();
	String projectVersionName = ruleViolationNotif.getContent()
		.getProjectVersionName();
	// String componentVersionsInViolation = ruleViolationNotif.getContent()
	// .getComponentVersionsInViolation();
	List<ComponentVersionStatus> compStatuses = ruleViolationNotif
		.getContent().getComponentVersionStatuses();
	for (ComponentVersionStatus compStatus : compStatuses) {
	    log.debug(compStatus.toString());
	    String componentName = compStatus.getComponentName();
	    String componentVersion;
	    try {
		componentVersion = getComponentVersionNameFromLink(compStatus
			.getComponentVersion());
	    } catch (Exception e) {
		componentVersion = "<not specified>";
	    }
	    String policyStatusLink = compStatus
		    .getBomComponentVersionPolicyStatus();
	    // log.debug("policyStatus: " + policyStatusLink);
	    processPolicyViolation(notificationTimeStamp, ruleViolationNotif,
		    policyStatusLink, projectName, projectVersionName,
		    componentName, componentVersion);
	}
    }

    private String getComponentVersionNameFromLink(String componentVersionLink)
	    throws NotificationDaoException {
	if (componentVersionLink == null) {
	    return "<null>";
	}

	ComponentVersion componentVersion;
	try {
	    componentVersion = dao.getFromUrl(ComponentVersion.class,
		    componentVersionLink);
	} catch (NotificationDaoException e) {
	    throw new NotificationDaoException(
		    "Error getting component version name: " + e.getMessage());
	}
	return componentVersion.getVersionName();

	// final ClientResource resource =
	// createGetClientResourceWithGivenLink(componentVersionLink);
	// log.debug("Resource: " + resource);
	// int responseCode = resource.getResponse().getStatus().getCode();
	// if (responseCode == 200 || responseCode == 204 || responseCode ==
	// 202) {
	// log.debug("SUCCESS getting componentVersion");
	// final String response = readResponseAsString(resource.getResponse());
	//
	// Gson gson = new GsonBuilder().create();
	// JsonParser parser = new JsonParser();
	// JsonObject json = parser.parse(response).getAsJsonObject();
	//
	// ComponentVersion componentVersion = gson.fromJson(json,
	// ComponentVersion.class);
	// return componentVersion.getVersionName();
	// }
	// throw new Exception("Error getting component version name");
    }

    private void processPolicyViolation(String notificationTimeStamp,
	    RuleViolationNotificationItem ruleViolationNotificationItem,
	    String policyStatusLink, String projectName, String projectVersion,
	    String componentName, String componentVersion) throws Exception {

	try {
	    ApprovalStatus policyStatus = dao.getFromUrl(ApprovalStatus.class,
		    policyStatusLink);
	    log.info("Approval Status: " + policyStatus);
	    if ("NOT_IN_VIOLATION".equals(policyStatus.getApprovalStatus())) {
		log.info("Not generating a ticket");
		return; // don't need a ticket if it's not in violation
	    }
	    String policyLink = policyStatus.getLink("policy-rule");
	    processPolicy(notificationTimeStamp, ruleViolationNotificationItem,
		    policyLink, projectName, projectVersion, componentName,
		    componentVersion);
	} catch (NotificationDaoException e) {
	    log.warn("Error getting policy status from " + policyStatusLink
		    + ": " + e.getMessage()
		    + "; This component was probably removed from this BOM");
	}

	// final ClientResource resource =
	// createGetClientResourceWithGivenLink(policyStatusLink);
	// log.debug("Resource: " + resource);
	// int responseCode = resource.getResponse().getStatus().getCode();
	// if (responseCode == 200 || responseCode == 204 || responseCode ==
	// 202) {
	// log.info("SUCCESS getting policyStatus");
	// final String response = readResponseAsString(resource.getResponse());
	//
	// Gson gson = new GsonBuilder().create();
	// JsonParser parser = new JsonParser();
	// JsonObject json = parser.parse(response).getAsJsonObject();
	//
	// ApprovalStatus policyStatus = gson.fromJson(json,
	// ApprovalStatus.class);
	// log.info("Approval Status: " + policyStatus);
	// if ("NOT_IN_VIOLATION".equals(policyStatus.getApprovalStatus())) {
	// log.info("Not generating a ticket");
	// return; // don't need a ticket if it's not in violation
	// }
	// String policyLink = policyStatus.getLink("policy-rule");
	// processPolicy(notificationTimeStamp, ruleViolationNotificationItem,
	// policyLink, projectName, projectVersion, componentName,
	// componentVersion);
	// } else {
	// log.warn("Error getting policy status from " + policyStatusLink
	// + ": " + responseCode
	// + "; This component was probably removed from this BOM");
	//
	// return;
	// }
    }

    private void processPolicy(String notificationTimeStamp,
	    RuleViolationNotificationItem ruleViolationNotificationItem,
	    String policyLink, String projectName, String projectVersion,
	    String componentName, String componentVersion) throws IOException,
	    URISyntaxException {
	if (policyLink == null) {
	    log.warn("Policy link is null in notification item: "
		    + ruleViolationNotificationItem);

	    JiraTicket jiraTicket = new JiraTicket(notificationTimeStamp,
		    JiraTicketType.RULE_VIOLATION, projectName, projectVersion,
		    componentName, componentVersion, null, null,
		    ActionRequired.REVIEW);
	    System.out.println(jiraTicket);
	    addToMap(jiraTicket);
	    return;
	}

	try {
	    PolicyRule policyRule = dao
		    .getFromUrl(PolicyRule.class, policyLink);
	    String policyRuleName = policyRule.getName();

	    JiraTicket jiraTicket = new JiraTicket(notificationTimeStamp,
		    JiraTicketType.RULE_VIOLATION, projectName, projectVersion,
		    componentName, componentVersion, null, policyRuleName,
		    ActionRequired.REVIEW);
	    System.out.println(jiraTicket);
	    addToMap(jiraTicket);
	} catch (NotificationDaoException e) {
	    log.error("Error getting violated rule name");
	}

	// final ClientResource resource =
	// createGetClientResourceWithGivenLink(policyLink);
	// log.debug("Resource: " + resource);
	// int responseCode = resource.getResponse().getStatus().getCode();
	// if (responseCode == 200 || responseCode == 204 || responseCode ==
	// 202) {
	// log.info("SUCCESS getting policy");
	// final String response = readResponseAsString(resource.getResponse());
	//
	// Gson gson = new GsonBuilder().create();
	// JsonParser parser = new JsonParser();
	// JsonObject json = parser.parse(response).getAsJsonObject();
	// PolicyRule policyRule = gson.fromJson(json, PolicyRule.class);
	// String policyRuleName = policyRule.getName();
	//
	// JiraTicket jiraTicket = new JiraTicket(notificationTimeStamp,
	// JiraTicketType.RULE_VIOLATION, projectName, projectVersion,
	// componentName, componentVersion, null, policyRuleName,
	// ActionRequired.REVIEW);
	// System.out.println(jiraTicket);
	// addToMap(jiraTicket);
	// } else {
	// log.error("Error getting violated rule name");
	// }
    }

    private void processVulnerabilityNotification(String notificationTimeStamp,
	    VulnerabilityNotificationItem vulnNotif) throws IOException,
	    BDRestException, URISyntaxException, NotificationDaoException {
	log.debug("processVulnerabilityNotification()");

	if (vulnNotif.getContent().getAffectedProjectVersions() == null) {
	    log.error("This vulnerability notification has no affected project versions; this might mean it's an old notification: "
		    + vulnNotif);
	    return;
	}
	for (ProjectVersion affectedProjectVersion : vulnNotif.getContent()
		.getAffectedProjectVersions()) {
	    String projectName = affectedProjectVersion.getProjectName();
	    String projectVersionName = affectedProjectVersion
		    .getProjectVersionName();
	    String componentName = vulnNotif.getContent().getComponentName();
	    String componentVersion = vulnNotif.getContent().getVersionName();

	    String projectVersionLink = affectedProjectVersion
		    .getProjectVersion();
	    processProjectVersionLink(notificationTimeStamp,
		    projectVersionLink, projectName, componentName,
		    componentVersion);
	}
    }

    private void processProjectVersionLink(String notificationTimeStamp,
	    String projectVersionLink, String projectName,
	    String targetComponentName, String targetComponentVersion)
	    throws URISyntaxException, IOException, NotificationDaoException {

	ProjectVersionItem versionItem = dao.getFromUrl(
		ProjectVersionItem.class, projectVersionLink);

	String vulnerableComponentsLink = versionItem
		.getLink("vulnerable-components");
	log.debug("Link to vulnerable components: " + vulnerableComponentsLink);
	processVulnerableComponentsLink(notificationTimeStamp,
		vulnerableComponentsLink, projectName,
		versionItem.getVersionName(), targetComponentName,
		targetComponentVersion);

	// final ClientResource resource =
	// createGetClientResourceWithGivenLink(projectVersionLink);
	// log.debug("Resource: " + resource);
	// int responseCode = resource.getResponse().getStatus().getCode();
	// if (responseCode == 200 || responseCode == 204 || responseCode ==
	// 202) {
	// log.debug("SUCCESS getting projectVersion");
	// final String response = readResponseAsString(resource.getResponse());
	//
	// Gson gson = new GsonBuilder().create();
	// JsonParser parser = new JsonParser();
	// JsonObject json = parser.parse(response).getAsJsonObject();
	//
	// ProjectVersionItem versionItem = gson.fromJson(json,
	// ProjectVersionItem.class);
	//
	// String vulnerableComponentsLink = versionItem
	// .getLink("vulnerable-components");
	// log.debug("Link to vulnerable components: "
	// + vulnerableComponentsLink);
	// processVulnerableComponentsLink(notificationTimeStamp,
	// vulnerableComponentsLink, projectName,
	// versionItem.getVersionName(), targetComponentName,
	// targetComponentVersion);
	// }
    }

    private void processVulnerableComponentsLink(String notificationTimeStamp,
	    String vulnerableComponentsLink, String projectName,
	    String projectVersionName, String targetComponentName,
	    String targetComponentVersion) throws URISyntaxException,
	    IOException, NotificationDaoException {

	VulnerableComponentsResponse vulnCompsResponse = dao.getFromUrl(
		VulnerableComponentsResponse.class, vulnerableComponentsLink);
	// JsonObject json = vulnCompsResponse.getJsonObject();
	// JsonArray array = json.get("items").getAsJsonArray();
	// log.debug("Got " + array.size() + " vulnerableComponents");
	// for (JsonElement elem : array) {
	//
	// }
	for (VulnerableComponentItem vulnerableComponentItem : vulnCompsResponse
		.getItems()) {
	    log.debug(vulnerableComponentItem.toString());

	    if (!vulnerableComponentItem.getComponentName().equals(
		    targetComponentName)) {
		log.debug("Wrong component name; skipping this one");
		continue;
	    }
	    if (!vulnerableComponentItem.getComponentVersionName().equals(
		    targetComponentVersion)) {
		log.debug("Wrong component version; skipping this one");
		continue;
	    }
	    if ("REMEDIATION_REQUIRED".equals(vulnerableComponentItem
		    .getVulnerabilityWithRemediation().getRemediationStatus())) {

		JiraTicket jiraTicket = new JiraTicket(notificationTimeStamp,
			JiraTicketType.VULNERABILITY, projectName,
			projectVersionName, targetComponentName,
			targetComponentVersion, vulnerableComponentItem
				.getVulnerabilityWithRemediation()
				.getVulnerabilityName(), null,
			ActionRequired.REMEDIATION);
		System.out.println(jiraTicket);
		addToMap(jiraTicket);
	    }
	    if ("NEEDS_REVIEW".equals(vulnerableComponentItem
		    .getVulnerabilityWithRemediation().getRemediationStatus())) {

		JiraTicket jiraTicket = new JiraTicket(notificationTimeStamp,
			JiraTicketType.VULNERABILITY, projectName,
			projectVersionName, targetComponentName,
			targetComponentVersion, vulnerableComponentItem
				.getVulnerabilityWithRemediation()
				.getVulnerabilityName(), null,
			ActionRequired.REVIEW);
		System.out.println(jiraTicket);
		addToMap(jiraTicket);
	    }
	}

	// final ClientResource resource =
	// createGetClientResourceWithGivenLink(vulnerableComponentsLink);
	// log.debug("Resource: " + resource);
	// int responseCode = resource.getResponse().getStatus().getCode();
	// if (responseCode == 200 || responseCode == 204 || responseCode ==
	// 202) {
	// log.debug("SUCCESS getting vulnerableComponents");
	// final String response = readResponseAsString(resource.getResponse());
	// Gson gson = new GsonBuilder().create();
	// JsonParser parser = new JsonParser();
	// JsonObject json = parser.parse(response).getAsJsonObject();
	// JsonArray array = json.get("items").getAsJsonArray();
	// log.debug("Got " + array.size() + " vulnerableComponents");
	// for (JsonElement elem : array) {
	// VulnerableComponentItem vulnerableComponentItem = gson
	// .fromJson(elem, VulnerableComponentItem.class);
	// log.debug(vulnerableComponentItem.toString());
	//
	// if (!vulnerableComponentItem.getComponentName().equals(
	// targetComponentName)) {
	// log.debug("Wrong component name; skipping this one");
	// continue;
	// }
	// if (!vulnerableComponentItem.getComponentVersionName().equals(
	// targetComponentVersion)) {
	// log.debug("Wrong component version; skipping this one");
	// continue;
	// }
	// if ("REMEDIATION_REQUIRED".equals(vulnerableComponentItem
	// .getVulnerabilityWithRemediation()
	// .getRemediationStatus())) {
	//
	// JiraTicket jiraTicket = new JiraTicket(
	// notificationTimeStamp,
	// JiraTicketType.VULNERABILITY, projectName,
	// projectVersionName, targetComponentName,
	// targetComponentVersion, vulnerableComponentItem
	// .getVulnerabilityWithRemediation()
	// .getVulnerabilityName(), null,
	// ActionRequired.REMEDIATION);
	// System.out.println(jiraTicket);
	// addToMap(jiraTicket);
	// }
	// if ("NEEDS_REVIEW".equals(vulnerableComponentItem
	// .getVulnerabilityWithRemediation()
	// .getRemediationStatus())) {
	//
	// JiraTicket jiraTicket = new JiraTicket(
	// notificationTimeStamp,
	// JiraTicketType.VULNERABILITY, projectName,
	// projectVersionName, targetComponentName,
	// targetComponentVersion, vulnerableComponentItem
	// .getVulnerabilityWithRemediation()
	// .getVulnerabilityName(), null,
	// ActionRequired.REVIEW);
	// System.out.println(jiraTicket);
	// addToMap(jiraTicket);
	// }
	// }
	// }
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
}

package com.blackducksoftware.tools.testhubclient;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.tools.testhubclient.dao.NotificationDao;
import com.blackducksoftware.tools.testhubclient.dao.NotificationDaoException;
import com.blackducksoftware.tools.testhubclient.dao.hub.HubNotificationDao;
import com.blackducksoftware.tools.testhubclient.json.JsonModelParser;
import com.blackducksoftware.tools.testhubclient.model.component.ComponentVersion;
import com.blackducksoftware.tools.testhubclient.model.component.VulnerableComponentItem;
import com.blackducksoftware.tools.testhubclient.model.notification.ComponentVersionStatus;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.VulnerabilityNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.VulnerableComponentsResponse;
import com.blackducksoftware.tools.testhubclient.model.policy.ApprovalStatus;
import com.blackducksoftware.tools.testhubclient.model.policy.PolicyRule;
import com.blackducksoftware.tools.testhubclient.model.policy.PolicyStatus;
import com.blackducksoftware.tools.testhubclient.model.projectversion.ProjectVersion;
import com.blackducksoftware.tools.testhubclient.model.projectversion.ProjectVersionItem;
import com.blackducksoftware.tools.testhubclient.service.NotificationService;
import com.blackducksoftware.tools.testhubclient.service.NotificationServiceException;
import com.blackducksoftware.tools.testhubclient.service.impl.NotificationServiceImpl;

public class HubCommonClient {

    public static void main(String[] args) throws Exception {

	System.out.println("Starting up...");

	String username = "sysadmin";
	String password = "blackduck";

	NotificationDao dao = new HubNotificationDao(
		"http://eng-hub-valid03.dc1.lan", username, password);
	NotificationService svc = new NotificationServiceImpl(dao);
	HubCommonClient client = new HubCommonClient(svc, dao);

	client.run("2016-05-01T00:00:00.000Z", "2016-07-30T00:00:00.000Z", 10);
    }

    private final ClientLogger log;
    private final NotificationService svc;
    private final NotificationDao dao;
    private final JsonModelParser jsonModelParser;

    private Map<Integer, JiraTicket> tickets = new HashMap<>();

    private int duplicateCount = 0;
    private int ticketCount = 0;

    public HubCommonClient(NotificationService svc, NotificationDao dao)
	    throws Exception {
	this.svc = svc;
	this.dao = dao;
	log = new ClientLogger();
	String hubVersion = dao.getVersion();
	log.info("Hub version: " + hubVersion);
	jsonModelParser = new JsonModelParser();
    }

    public Statistics run(String startDate, String endDate, int limit)
	    throws Exception {

	int notificationCount = processNotifications(startDate, endDate, limit);

	log.info("Done processsing " + notificationCount
		+ " notifications, generating " + ticketCount + " tickets, "
		+ duplicateCount + " of which were duplicates");

	return new Statistics(notificationCount, ticketCount, duplicateCount);

    }

    private int processNotifications(String startDate, String endDate, int limit)
	    throws Exception {

	List<NotificationItem> notificationItems = svc.getNotifications(
		startDate, endDate, limit);
	for (NotificationItem notificationItem : notificationItems) {
	    String notificationTimeStamp = notificationItem.getCreatedAt();
	    log.info("\n\n======================================================================\n"
		    + "NotificationItem: " + notificationItem);
	    if (notificationItem instanceof VulnerabilityNotificationItem) {
		processVulnerabilityNotification(notificationTimeStamp,
			(VulnerabilityNotificationItem) notificationItem);
	    } else if (notificationItem instanceof RuleViolationNotificationItem) {
		processRuleViolationNotification(notificationTimeStamp,
			(RuleViolationNotificationItem) notificationItem);
	    } else if (notificationItem instanceof PolicyOverrideNotificationItem) {
		processPolicyOverrideNotification(notificationTimeStamp,
			(PolicyOverrideNotificationItem) notificationItem);
	    } else {
		log.error("Unknown notification type: "
			+ notificationItem.getType() + ": " + notificationItem);
	    }
	}
	return notificationItems.size();
    }

    private void processPolicyOverrideNotification(
	    String notificationTimeStamp,
	    PolicyOverrideNotificationItem policyOverrideNotif)
	    throws Exception {
	PolicyStatus compPolicyStatus = getCompPolicyStatusFromLink(policyOverrideNotif
		.getContent().getBomComponentVersionPolicyStatus());
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
		JiraTicketType.POLICY_OVERRIDE, policyOverrideNotif
			.getContent().getProjectName(), policyOverrideNotif
			.getContent().getProjectVersionName(),
		policyOverrideNotif.getContent().getComponentName(),
		policyOverrideNotif.getContent().getComponentVersionName(),
		null, null, ActionRequired.REVIEW);
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
	    policyStatus = svc.getPolicyStatusFromLink(compPolicyStatusLink);
	} catch (NotificationServiceException e) {
	    log.warn("Error getting policy status from " + compPolicyStatusLink
		    + ": " + e.getMessage()
		    + "; This component was probably removed from this BOM");
	    return null;
	}

	return policyStatus;
    }

    private void processRuleViolationNotification(String notificationTimeStamp,
	    RuleViolationNotificationItem ruleViolationNotif) throws Exception {
	List<ComponentVersionStatus> compStatuses = ruleViolationNotif
		.getContent().getComponentVersionStatuses();
	for (ComponentVersionStatus compStatus : compStatuses) {
	    log.debug(compStatus.toString());
	    String componentVersion;
	    try {
		componentVersion = getComponentVersionNameFromLink(compStatus
			.getComponentVersion());
	    } catch (Exception e) {
		componentVersion = "<not specified>";
	    }

	    processPolicyViolation(notificationTimeStamp, ruleViolationNotif,
		    compStatus.getBomComponentVersionPolicyStatus(),
		    ruleViolationNotif.getContent().getProjectName(),
		    ruleViolationNotif.getContent().getProjectVersionName(),
		    compStatus.getComponentName(), componentVersion);
	}
    }

    private String getComponentVersionNameFromLink(String componentVersionLink)
	    throws NotificationDaoException {
	if (componentVersionLink == null) {
	    return "<null>";
	}

	ComponentVersion componentVersion;
	try {
	    componentVersion = dao.getFromAbsoluteUrl(ComponentVersion.class,
		    componentVersionLink);
	} catch (NotificationDaoException e) {
	    throw new NotificationDaoException(
		    "Error getting component version name: " + e.getMessage());
	}
	return componentVersion.getVersionName();
    }

    private void processPolicyViolation(String notificationTimeStamp,
	    RuleViolationNotificationItem ruleViolationNotificationItem,
	    String policyStatusLink, String projectName, String projectVersion,
	    String componentName, String componentVersion) throws Exception {

	try {
	    ApprovalStatus policyStatus = dao.getFromAbsoluteUrl(
		    ApprovalStatus.class, policyStatusLink);
	    log.info("Approval Status: " + policyStatus);
	    if ("NOT_IN_VIOLATION".equals(policyStatus.getApprovalStatus())) {
		log.info("Not generating a ticket");
		return; // don't need a ticket if it's not in violation
	    }
	    processPolicy(notificationTimeStamp, ruleViolationNotificationItem,
		    policyStatus.getLink("policy-rule"), projectName,
		    projectVersion, componentName, componentVersion);
	} catch (NotificationDaoException e) {
	    log.warn("Error getting policy status from " + policyStatusLink
		    + ": " + e.getMessage()
		    + "; This component was probably removed from this BOM");
	}
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
	    PolicyRule policyRule = dao.getFromAbsoluteUrl(PolicyRule.class,
		    policyLink);
	    JiraTicket jiraTicket = new JiraTicket(notificationTimeStamp,
		    JiraTicketType.RULE_VIOLATION, projectName, projectVersion,
		    componentName, componentVersion, null,
		    policyRule.getName(), ActionRequired.REVIEW);
	    System.out.println(jiraTicket);
	    addToMap(jiraTicket);
	} catch (NotificationDaoException e) {
	    log.error("Error getting violated rule name");
	}
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
	    processProjectVersionLink(notificationTimeStamp,
		    affectedProjectVersion.getProjectVersion(),
		    affectedProjectVersion.getProjectName(), vulnNotif
			    .getContent().getComponentName(), vulnNotif
			    .getContent().getVersionName());
	}
    }

    private void processProjectVersionLink(String notificationTimeStamp,
	    String projectVersionLink, String projectName,
	    String targetComponentName, String targetComponentVersion)
	    throws URISyntaxException, IOException, NotificationDaoException {

	ProjectVersionItem versionItem = dao.getFromAbsoluteUrl(
		ProjectVersionItem.class, projectVersionLink);

	String vulnerableComponentsLink = versionItem
		.getLink("vulnerable-components");
	log.debug("Link to vulnerable components: " + vulnerableComponentsLink);
	processVulnerableComponentsLink(notificationTimeStamp,
		vulnerableComponentsLink, projectName,
		versionItem.getVersionName(), targetComponentName,
		targetComponentVersion);
    }

    private void processVulnerableComponentsLink(String notificationTimeStamp,
	    String vulnerableComponentsLink, String projectName,
	    String projectVersionName, String targetComponentName,
	    String targetComponentVersion) throws URISyntaxException,
	    IOException, NotificationDaoException {

	VulnerableComponentsResponse vulnCompsResponse = dao
		.getFromAbsoluteUrl(VulnerableComponentsResponse.class,
			vulnerableComponentsLink);

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
    }
}
